(ns robinhood.clj.utils
  (:require [clj-http.client :as client]
            [hiccup.util :as hic]
            [clojure.data.json :as json]
            [clojure.string :as string])
  (:import [java.net URLEncoder]
           [java.lang System]))

;; This is the node.js client-id used by Robinhood, same for all users
(def client-id "c82SH0WZOsabOXGP2sxqcj34FxkvfnWRZBKlBjFS")

(defn- post-data [data]
  (letfn [(k  [x] (name (key x)))
          (v  [x] (URLEncoder/encode (str (val x))))
          (kv [x] (str (k x) "=" (v x) "utf8"))]
         (string/join "&" (map kv data))))

(defn- sanitize
  "Replaces spaces and underscores with hyphens"
  [response]
  (-> response
      (string/replace #"_| |:" {" " "-" "_" "-" ":" ""})
      string/lower-case))

(defn response->body
  [response]
  (clojure.pprint/pprint
   {:at ::response->body
    :response-status (:status response)})
  (if (#{200 201} (:status response))
    (-> (:body response)
        (json/read-str :key-fn #(keyword (sanitize %))))
    nil))

(defn- build-get-params
  [{:keys [robinhood/access-token] :as auth}]
  (let [default-params {:content-type :application/json :accept :*/*}
                        ;:debug true :debug-body true}
        header (when access-token
                 {:authority "api.robinhood.com"
                  :authorization (str "Bearer " access-token)})]
    (if header
        (assoc default-params :headers header)
        default-params)))

(defn- handle-paginated-results
  [response-body auth]
  {:results
   (loop [results '()
          requests #{}
          body response-body]
     (let [results (into results (:results body))]
       (if (or (empty? (:next body))
               (contains? requests (:next body)))
         results
         (do (clojure.pprint/pprint
              {:at ::handle-paginated-results
               :requests (count requests)
               :next (:next body)})
             (recur results
                    (conj requests (:next body))
                    (response->body
                     (client/get
                      (:next body)
                      (build-get-params auth))))))))})

(defn tap-print [s at k]
  (clojure.pprint/pprint {:at at
                          k (str s)})
  (str s))

(defn- maybe-paginate
  [response auth]
  (if (and (:results response)
           (:next response))
    (handle-paginated-results response auth)
    response))

(defn urlopen
  ([url]
   (urlopen url nil nil))
  ;; A surprising amount of the robinhood api works w/o auth :)
  ([url query-params]
   (urlopen url query-params nil))
  ;; For any account related endpoint (and sometimes others) we must
  ;; 1. Setup our robinhood username & password in our env vars (see Readme)
  ;; 2. Pass `robinhood.clj.auth/auth` in when calling urls that require auth
  ([url query-params auth]
   (clojure.pprint/pprint {:at ::urlopen
                           :url url
                           :query-params query-params})
   (-> url
       (hic/url query-params)
       str
       (tap-print ::urlopen :url)
       (client/get (build-get-params auth))
       response->body
       (maybe-paginate auth))))

(defn urlpost
  ([url query-params]
   (urlpost url query-params {}))
  ([url query-params form-params]
   (let [device-token (System/getenv "ROBINHOOD_DEVICE_TOKEN")
         _ (when (not device-token)
             (throw (Exception. "ROBINHOOD_DEVICE_TOKEN must be provided in env vars")))
         default-form-params {:expires_in 86400
                              :grant_type "password"
                              :client_id client-id
                              :device_token device-token
                              :scope "internal"}]
     (-> url
         (hic/url query-params)
         str
         (client/post
           {:content-type :json
            :accept :json
            ;:debug true
            ;:debug-body true
            :form-params (merge default-form-params form-params)})
         response->body))))

(defn post-body
 ([url body] (post-body url body nil))
 ([url body auth]
  (-> url
      (client/post
       {:content-type :application/json
        :accept :*/*
        ; :debug true
        ; :debug-body true
        :headers {:authority "api.robinhood.com"
                  :authorization (str "Bearer " (:robinhood/access-token auth))}
        :form-params body})
      response->body)))

;;https://stackoverflow.com/questions/43722091/clojure-programmatically-namespace-map-keys)
(defn map->nsmap [m n]
  (reduce-kv (fn [acc k v]
               (let [new-kw (if (and (keyword? k)
                                     (not (qualified-keyword? k)))
                              (keyword (str n) (name k))
                              k)]
                 (assoc acc new-kw v)))
             {} m))
