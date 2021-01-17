(ns robinhood.clj.auth
  (:require [robinhood.clj.utils :as u]
            [robinhood.clj.spec]
            [clojure.spec.alpha :as s] ; TODO remove
            [clj-http.client :as client])
  (:import [java.lang System]))

;; --- UNAUTHED ---------------------------------------------------------------

(defn has-token? [s]
  (re-find #"window\.auth.*" s))

(defn get-token [s]
  (-> s (clojure.string/split #"\"") (nth 3)))

(defn unauthed-login->token []
  {:post [(s/valid? :robinhood/auth %)]}
  {:robinhood/access-token
   (->> "https://robinhood.com/stocks/AAPL"
        client/get
        :body
        clojure.string/split-lines
        (filter has-token?)
        first
        get-token)})

(defn attempt-unauthed-login []
  (try (unauthed-login->token)
       (catch Exception e
         (println :at ::auth
                  :error (str "INVALID_CREDENTIALS\n" (.getMessage e)))
         nil)))

;; --- AUTHED -----------------------------------------------------------------

(defn username [] (System/getenv "ROBINHOOD_USER"))

(defn password [] (System/getenv "ROBINHOOD_PASS"))

(defn authed-login->token [username password]
  {:post [(s/valid? :robinhood/auth %)]}
  {:robinhood/access-token
   (-> "https://api.robinhood.com/oauth2/token/"
       (u/urlpost nil {:username username :password password})
       :access-token)})

(defn attempt-authed-login
  ([]
   (attempt-authed-login (username) (password)))
  ([username password]
   (try (authed-login->token username password)
        (catch Exception e
          (println :at ::auth
                   :error (str "INVALID_CREDENTIALS\n" (.getMessage e)))
          nil))))

;; --- INTERFACE --------------------------------------------------------------

(defn logout
  "Prevents further use of the given authorization be telling Robinhood to
  revoke the specified token."
  [token])

(defn login
  "Login to robinhood, returns auth"
  ([]
   (attempt-unauthed-login))
  ([username password]
   (attempt-authed-login username password)))

(defn authed-client []
  (attempt-authed-login (username) (password)))

(defn unauthed-client []
  (attempt-unauthed-login))

;; --- DEV ---------------

#_(login)

#_(login (username) (password))

#_(unauthed-login->token)

#_(attempt-unauthed-login)

#_(attempt-authed-login)

