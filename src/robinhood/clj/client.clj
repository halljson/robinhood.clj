(ns robinhood.clj.client
  (:require [clj-http.client :as client]
            [clojure.core.reducers :as r]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [com.rpl.specter :refer :all]
            [orchestra.core :refer [defn-spec]]
            [orchestra.spec.test :as stest]
            [robinhood.clj.auth :as auth]
            [robinhood.clj.spec]
            [robinhood.clj.utils :as u]
            [spec-tools.core :as st])
  (:import [java.net URLEncoder]))

;----- HELPERS ----------------------------------------------------------------

#_(def rc' (auth/authed-client))

(defn instrument-strs [part]
  (u/tap-print (first part) ::instrument-strs :part)
  (string/join "," (map :robinhood.quote/instrument (filter not-empty part))))

(defn date-chain->urls [part]
  (->> part (map :robinhood.option-date-chain/url) (remove nil?) (string/join ",")))

(defn string-coerce [spec items]
  (st/coerce spec items st/string-transformer))

(defn coerce-quotes [quotes']
  (->> (string-coerce :robinhood/quotes quotes')
       (map #(u/map->nsmap % "robinhood.quote"))))

(defn coerce-instrument [instrument']
  (-> (string-coerce :robinhood/instrument instrument')
      (u/map->nsmap "robinhood.instrument")))

(defn coerce-fundamentals [fundamentals']
  (->> (string-coerce :robinhood/fundamentals fundamentals')
       (map #(u/map->nsmap % "robinhood.fundamental"))))

(defn coerce-historical [historical']
  (letfn [(coerce-historical-item [historical-item']
            (-> (string-coerce :robinhood.historical/item historical-item')
                (u/map->nsmap "robinhood.historical.item")))]
    (-> (string-coerce :robinhood/historical historical')
        (u/map->nsmap "robinhood.historical")
        (->> (transform [:robinhood.historical/historicals
                         (walker #(s/valid? :robinhood.historical/item %))]
                        coerce-historical-item)))))

(defn coerce-option-chain-base [ocb']
  (letfn [(coerce-ocb-min-ticks [min-ticks']
            (-> (string-coerce :robinhood.option-chain-base/min-ticks min-ticks')
                (u/map->nsmap "robinhood.option-chain-base.min-ticks")))
          (coerce-underlying-instrument [underlying-instrument']
            (-> (string-coerce :robinhood.option-chain-base/underlying-instrument
                               underlying-instrument')
                (u/map->nsmap "robinhood.option-chain-base.underlying-instrument")))]
    (-> (string-coerce :robinhood/option-chain-base ocb')
        (u/map->nsmap "robinhood.option-chain-base")
        (dissoc :robinhood.option-chain-base/cash-component)
        (->> (transform [:robinhood.option-chain-base/min-ticks
                         (walker #(s/valid? :robinhood.option-chain-base/min-ticks %))]
                        coerce-ocb-min-ticks))
        (->> (transform [:robinhood.option-chain-base/underlying-instruments
                         (walker #(s/valid? :robinhood.option-chain-base/underlying-instrument %))]
                        coerce-underlying-instrument)))))

(defn coerce-option-date-chain [odc']
  (letfn [(coerce-odc-min-ticks [min-ticks']
            (-> (string-coerce :robinhood.option-date-chain/min-ticks min-ticks')
                (u/map->nsmap "robinhood.option-date-chain.min-ticks")))]
    (->> (string-coerce :robinhood/option-date-chains odc')
         (map #(u/map->nsmap % "robinhood.option-date-chain"))
         (transform [ALL
                     :robinhood.option-date-chain/min-ticks
                     (walker #(s/valid? :robinhood.option-date-chain/min-ticks %))]
                    coerce-odc-min-ticks))))

(defn coerce-option-contracts [option-contracts']
  (->> option-contracts'
       (into [])
       (string-coerce :robinhood/option-contracts)
       (map #(u/map->nsmap % "robinhood.option-contract"))))

(defn coerce-account-info [account-info']
  (letfn [(coerce-account-margin-balances [margin-balances']
            (-> (string-coerce :robinhood.account-info/margin-balances margin-balances')
                (u/map->nsmap "robinhood.account-info.margin-balances")))]
    (-> (string-coerce :robinhood/account-info account-info')
        (u/map->nsmap "robinhood.account-info")
        (->> (transform [:robinhood.account-info/margin-balances
                         (walker #(s/valid? :robinhood.account-info/margin-balances %))]
                        coerce-account-margin-balances)))))

(defn coerce-equity-historicals [equity-historicals']
  (letfn [(coerce-equity-historical-items [eh-item']
            (-> (string-coerce :robinhood.equity-historicals/item eh-item')
                (u/map->nsmap "robinhood.equity-historicals.item")))]
    (-> (string-coerce :robinhood/equity-historicals equity-historicals')
        (u/map->nsmap "robinhood.equity-historicals")
        (->> (transform [:robinhood.equity-historicals/equity-historicals
                         (walker #(s/valid? :robinhood.equity-historicals/item %))]
                        coerce-equity-historical-items)))))

(defn coerce-orders [orders']
  (letfn [(coerce-execution [execution']
            (-> (string-coerce :robinhood.order/execution execution')
                (u/map->nsmap "robinhood.order.executions")))]
    (->> (string-coerce :robinhood/orders orders')
         (map #(u/map->nsmap % "robinhood.order"))
         (transform [ALL
                     :robinhood.order/executions
                     (walker #(s/valid? :robinhood.order/execution %))]
                    coerce-execution))))

(defn coerce-option-orders [option-orders']
  (letfn [(coerce-option-order-leg [option-order-leg']
            (-> (string-coerce :robinhood.option-order/leg option-order-leg')
                (u/map->nsmap "robinhood.option-order.legs")))

          (coerce-execution [execution']
            (-> (string-coerce :robinhood.option-order.legs/execution execution')
                (u/map->nsmap "robinhood.option-order.legs.executions")))]
    (->> (string-coerce :robinhood/option-orders option-orders')
         (map #(u/map->nsmap % "robinhood.option-order"))
         (transform [ALL
                     :robinhood.option-order/legs
                     (walker #(s/valid? :robinhood.option-order/leg %))]
                    coerce-option-order-leg)
         (transform [ALL
                     :robinhood.option-order/legs
                     ALL
                     :robinhood.option-order.legs/executions
                     (walker #(s/valid? :robinhood.option-order.legs/execution %))]
                    coerce-execution))))

(defn coerce-option-events [option-events']
  (letfn [(coerce-option-event-equity-cpt [option-event']
            (-> :robinhood.option-event/equity-component
                (string-coerce option-event')
                (u/map->nsmap "robinhood.option-event.equity-components")))]
    (->> (string-coerce :robinhood/option-events option-events')
         (map #(u/map->nsmap % "robinhood.option-event"))
         (transform [ALL
                     :robinhood.option-event/equity-components
                     (walker #(s/valid? :robinhood.option-event/equity-component %))]
                    coerce-option-event-equity-cpt))))

;----- GENERAL ----------------------------------------------------------------

(defn-spec quotes :robinhood/quotes
  [auth :robinhood/auth
   query-params :robinhood.client/symbol-query]
  (-> (u/urlopen "https://api.robinhood.com/quotes/" query-params auth)
      :results
      coerce-quotes))

(defn-spec instrument :robinhood/instrument
  [auth :robinhood/auth
   query-params :robinhood.client/symbol-query]
  (->> (quotes auth query-params)
       first
       :robinhood.quote/instrument
       u/urlopen
       coerce-instrument))

(defn-spec fundamentals :robinhood/fundamentals
  [auth :robinhood/auth
   symbols :robinhood.client/symbol-list]
  (let [symbols-str (string/join "," symbols)
        quotes' (quotes auth {:symbols symbols-str})]
    (->> (for [part (partition 30 30 nil quotes')
               :let [instrument-str (instrument-strs part)]
               :when (not-empty instrument-str)]
           (-> "https://api.robinhood.com/marketdata/fundamentals/"
               (u/urlopen {:instruments instrument-str} auth)
               :results))
         flatten
         (remove nil?)
         (into '())
         coerce-fundamentals)))

(defn-spec movers any?
  [direction :robinhood.client/direction]
  (:results
   (u/urlopen "https://api.robinhood.com/midlands/movers/sp500/"
              {:direction direction})))

;; --- INSTRUMENT-URLS->QUOTES ------------------------------------------------

(defn-spec instrument-urls->quotes :robinhood/quotes
  [auth :robinhood/auth
   instrument-urls :robinhood/instrument-urls]
  (->> (for [part (partition 30 30 nil instrument-urls)
             :let [part (string/join "," (not-empty part))]
             :when (not-empty part)]
         (:results
          (u/urlopen "https://api.robinhood.com/marketdata/quotes/"
                     {:instruments part}
                     auth)))
       doall
       flatten
       (remove nil?)
       (into '())
       coerce-quotes))

#_(def many-instruments
    '("https://api.robinhood.com/instruments/99271df1-5cce-47f4-9480-af09900904c6/"
      "https://api.robinhood.com/instruments/99271df1-5cce-47f4-9480-af09900904c6/"
      "https://api.robinhood.com/instruments/99271df1-5cce-47f4-9480-af09900904c6/"
      "https://api.robinhood.com/instruments/01f33471-51d9-4afe-850f-b5f13d58c459/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/a4f0cca4-79dc-4297-9c02-5bce1909cd4b/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/59a8d284-5274-4ba6-b1af-dd889b36da0d/"
      "https://api.robinhood.com/instruments/52b39774-3707-45ac-86e9-7bc9de8397e7/"
      "https://api.robinhood.com/instruments/59a8d284-5274-4ba6-b1af-dd889b36da0d/"
      "https://api.robinhood.com/instruments/52b39774-3707-45ac-86e9-7bc9de8397e7/"
      "https://api.robinhood.com/instruments/c0058645-aac4-487e-99d3-f533f6607fd8/"
      "https://api.robinhood.com/instruments/e39ed23a-7bd1-4587-b060-71988d9ef483/"
      "https://api.robinhood.com/instruments/2e848fe2-857d-4a9e-b69e-e36481a3d054/"
      "https://api.robinhood.com/instruments/2e848fe2-857d-4a9e-b69e-e36481a3d054/"
      "https://api.robinhood.com/instruments/2b456f6a-3287-4757-abf9-327383d2c708/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/e39ed23a-7bd1-4587-b060-71988d9ef483/"
      "https://api.robinhood.com/instruments/d40e3279-300a-4555-a5b9-ce09b0bf28a3/"
      "https://api.robinhood.com/instruments/caaf9831-7a38-4bb1-b5dc-5350d57ebde1/"
      "https://api.robinhood.com/instruments/104a5137-e213-4132-bd34-1a1c8771c3f9/"
      "https://api.robinhood.com/instruments/e39ed23a-7bd1-4587-b060-71988d9ef483/"
      "https://api.robinhood.com/instruments/e39ed23a-7bd1-4587-b060-71988d9ef483/"
      "https://api.robinhood.com/instruments/2e848fe2-857d-4a9e-b69e-e36481a3d054/"
      "https://api.robinhood.com/instruments/c0058645-aac4-487e-99d3-f533f6607fd8/"
      "https://api.robinhood.com/instruments/c0058645-aac4-487e-99d3-f533f6607fd8/"
      "https://api.robinhood.com/instruments/c0058645-aac4-487e-99d3-f533f6607fd8/"
      "https://api.robinhood.com/instruments/104a5137-e213-4132-bd34-1a1c8771c3f9/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"
      "https://api.robinhood.com/instruments/2f30ec68-bb19-44aa-a289-b50b43c2257c/"))

#_(def iq (instrument-urls->quotes rc' many-instruments))

;; --- INSTRUMENT-URLS->FUNDAMENTALS -------------------------------------------

(defn-spec instrument-urls->fundamentals :robinhood/fundamentals
  [auth :robinhood/auth
   instrument-urls :robinhood/instrument-urls]
  (->> (for [part (partition 30 30 nil instrument-urls)
             :let [instrument-str (string/join "," part)]
             :when (not-empty instrument-str)]
         (-> "https://api.robinhood.com/marketdata/fundamentals/"
             (u/urlopen {:instruments instrument-str} auth)
             :results))
       doall
       flatten
       (remove nil?)
       (into '())
       coerce-fundamentals))

#_(def some-fundamentals
    (instrument-urls->fundamentals rc' (take 50 many-instruments)))

;; --- HISTORICALS -------------------------------------------------------------

(defn-spec historicals :robinhood/historical
  [auth     :robinhood/auth
   interval :robinhood.client/interval
   span     :robinhood.client/span
   symbol   :robinhood.client/symbol]
  (-> (str "https://api.robinhood.com/marketdata/historicals/" symbol "/")
      (u/urlopen
       {:bounds "regular"   ;"trading"
        :interval interval  ;"5minute" "10minute" "week" "day"
        :span span          ;"day" "week" "year" "5year"
        :symbol symbol}
       auth)
      coerce-historical))

#_(def historicals' (historicals rc' "5minute" "day" "EAF"))

;; --- OPTIONS -----------------------------------------------------------------

(defn-spec instrument->option-chain-base :robinhood/option-chain-base
  "Creates an option chain url from an instrument map"
  [instrument :robinhood/instrument]
  (->> instrument
       :robinhood.instrument/tradable-chain-id
       (#(str "https://api.robinhood.com/options/chains/" % "/"))
       u/urlopen
       coerce-option-chain-base))

(defn-spec option-chain-base :robinhood/option-chain-base
  [auth :robinhood/auth
   query-params :robinhood.client/symbol-query]
  (->> (instrument auth query-params)
       instrument->option-chain-base))

#_(def ocb (option-chain-base rc' {:symbols "AAPL"}))

#_(s/explain :robinhood/option-chain-base ocb)

(defn-spec option-date-chain :robinhood/option-date-chains
  [opt-chain :robinhood/option-chain-base
   date :robinhood/short-date
   type :robinhood.client/type]
  (let [{:keys [robinhood.option-chain-base/id]} opt-chain]
    (when id
      (-> "https://api.robinhood.com/options/instruments/"
          (u/urlopen
           {:expiration_date date
            :chain_id id
            :state "active"
            :tradability "tradable"
            :type type})
          :results
          coerce-option-date-chain))))

#_(def odc (option-date-chain
            ocb
            (first (:robinhood.option-chain-base/expiration-dates ocb))
            "put"))

#_(s/explain :robinhood/option-date-chains odc)

#_(clojure.pprint/pprint odc)

(defn-spec opt-chain->date-chain :robinhood/option-date-chains
  "Takes an option chain (with n :expiration-dates) and pulls back all options
  for each expiration date on the option chain, 1 by 1. Each option contract
  lists its expiration date so we flatten results for easy consumption."
  [opt-chain :robinhood/option-chain-base
   type :robinhood.client/type]
  (->> (let [date (first (:robinhood.option-chain-base/expiration-dates opt-chain))]
         (when date (option-date-chain opt-chain date type)))
       (remove nil?)
       (into '())))

#_(def dc (opt-chain->date-chain ocb "put"))

;-----    AUTHED -- OPTIONS     --------------------------------------------

(defn-spec date-chain->prices :robinhood/prices
  "Takes a date-chain of (often 100+) option instruments and pulls back the
  bid-size, ask-size, implied volatility, the greeks (rho/delta/gamma/vega/theta),
  high and low prices, chance of profit short/long, et al. Makes batched requests
  for 30 contracts at a time and merges each result item into the original
  data item that caused the (batched-)request to fire in the first place."
  [auth :robinhood/auth
   date-chain :robinhood/option-date-chains]
  (letfn [(oc-equals-item [oc item]
            (= (:robinhood.option-date-chain/id item)
               (-> oc
                   :robinhood.option-contract/instrument
                   (string/split #"/")
                   last)))
          (oc->date-chain [oc]
            (r/reduce
             (fn [r item]
               (if (oc-equals-item oc item) (conj r item) r))
             '() date-chain))]
    (->> (for [part (partition 30 30 nil date-chain)
               :let [instruments (date-chain->urls part)]
               :when (not-empty instruments)]
           (-> "https://api.robinhood.com/marketdata/options/"
               (u/urlopen {:instruments instruments} auth)
               :results))
         r/flatten
         (r/remove nil?)
         coerce-option-contracts
         (r/reduce
          (fn [new-date-chain oc]
            (conj new-date-chain
                  (apply merge oc (oc->date-chain oc))))
          '()))))

#_(def dcp (date-chain->prices rc' dc))

#_(s/explain :robinhood/prices (take 4 dcp))

#_(count dcp)

(defn-spec opt-chain->prices :robinhood/prices
  [auth :robinhood/auth
   opt-chain :robinhood/option-chain-base
   type :robinhood.client/type]
  (->> (opt-chain->date-chain opt-chain type)
       (date-chain->prices auth)))

(defn-spec get-option-chain-prices :robinhood/prices
  [auth :robinhood/auth
   query-params :robinhood.client/symbol-query
   type :robinhood.client/type]
  (as-> (option-chain-base auth query-params) $
        (opt-chain->prices auth $ type)))

(defn get-options
  [auth query-params]
  {:pre [(s/cat :robinhood/auth auth)]}
  {:put (get-option-chain-prices auth query-params "put")
   :call (get-option-chain-prices auth query-params "call")})

(defn-spec get-options-events :robinhood/option-events
  "Get option-events for the given auth"
  [auth :robinhood/auth]
  (-> (u/urlopen "https://api.robinhood.com/options/events/" nil auth)
      :results
      coerce-option-events))

;-----    AUTHED -- GENERAL     --------------------------------------------

(defn news
  [auth symbol]
  {:pre [(s/cat :robinhood/auth auth)]}
  (-> (str "https://api.robinhood.com/midlands/news/" symbol "/")
      (u/urlopen nil auth)
      :results))

(defn-spec account-info :robinhood/account-info
  "Get account info for given auth"
  [auth :robinhood/auth]
  (-> (u/urlopen "https://api.robinhood.com/accounts/" nil auth)
      :results
      first
      (dissoc :instant-eligibility)
      coerce-account-info))

#_(def account-info' (account-info rc'))

(defn-spec equity-historicals :robinhood/equity-historicals
  "Gets account balance (as timeseries) for given auth"
  [auth :robinhood/auth
   account-number :robinhood.account-info/account-number]
  (-> (str "https://api.robinhood.com/portfolios/historicals/" account-number "/")
      (u/urlopen {:account account-number
                  :bounds "24_7"
                  :interval "5minute"
                  :span "day"}
                 auth)
      coerce-equity-historicals))

#_(def equity-historials'
    (equity-historicals rc' (:robinhood.account-info/account-number account-info')))

(defn-spec account-nummus-info any?
  "Get account nummus(?) info for given auth"
  [auth :robinhood/auth]
  (-> (u/urlopen "https://nummus.robinhood.com/accounts/" nil auth)
      :results))

#_(account-nummus-info rc')

;-----    AUTHED -- WATCHLIST     --------------------------------------------

(defn watchlist-instruments
  "Get all instruments on the given auth's watchlist"
  [auth]
  {:pre [(s/cat :robinhood/auth auth)]}
  (map
   (comp u/urlopen :instrument)
   (remove
    nil?
    (:results
     (u/urlopen "https://api.robinhood.com/watchlists/Default/"
                {:name "Default"}
                auth)))))

(defn- watchlist-option-chains
  "Get the option-chains for all instruments on the given auth's watchlist"
  [auth]
  {:pre [(s/cat :robinhood/auth auth)]}
  (->> (watchlist-instruments auth)
       (remove nil?)
       (map instrument->option-chain-base)
       (remove nil?)))

(defn watchlist-option-chain-prices
  "Get the option-chains for all instruments on the given auth's watchlist"
  [auth type]
  {:pre [(s/cat :robinhood/auth auth)]}
  ; {:post [(s/valid? ::option-prices %)]}
  (->> (watchlist-option-chains auth)
       (map #(opt-chain->prices auth % type))))

(defn watchlist-options
  "Get the puts and calls for all instruments on the given auth's watchlist"
  [auth]
  {:pre [(s/cat :robinhood/auth auth)]}
  {:put (watchlist-option-chain-prices auth "put")
   :call (watchlist-option-chain-prices auth "call")})

;-----    AUTHED -- ORDERS     --------------------------------------------

;(s/def ::account string?) ;; Account URL of the account you're attempting to buy or sell with.
;(s/def ::instrument string?) ;; Instrument URL of the security you're attempting to buy or sell
;(s/def ::symbol string?) ;; The ticker symbol of the security you're attempting to buy or sell
;(s/def ::type #{"market" "limit"})
;(s/def ::time_in_force #{"gfd" "gtc" "ioc" "opg"})
;(s/def ::trigger #{"immediate" "stop"})
;(s/def ::price float?) ;; The price you're willing to accept in a sell or pay in a buy
;(s/def :order/quantity int?) ;; Number of shares you would like to buy or sell
;(s/def ::side #{"buy" "sell"})

;; Required only when trigger equals "stop"
;(s/def ::stop-price float?) ;; The price at which an order with a stop trigger converts

;(s/def ::client-id string?) ;; Only available for OAuth applications No
;(s/def ::extended-hours boolean?) ;; Would/Should order execute when exchanges are closed No
;(s/def ::override-day-trade-checks boolean?)
;(s/def ::override-dtbp-checks boolean?)

; (s/def ::order
;  (s/keys :req-un [::instrument ::symbol ::type ::time_in_force
;                   ::trigger ::price ::side :order/quantity]
;          :opt-un [::account ::stop-price ::client-id ::extended-hours
;                   ::override-day-trade-checks ::override-dtbp-checks]))

;(s/def :robinhood/auth ;(s/keys :req-un [::access-token]))

(defn-spec orders :robinhood/orders
  "Gets current /orders for the given auth."
  [auth :robinhood/auth]
  (-> (u/urlopen  "https://api.robinhood.com/orders/" nil auth)
      :results
      coerce-orders))

#_(orders rc')

(defn-spec option-orders :robinhood/option-orders
  "Gets current /options/orders for the given auth."
  [auth :robinhood/auth]
  (-> (u/urlopen "https://api.robinhood.com/options/orders/" nil auth)
      :results
      coerce-option-orders))

#_(option-orders rc')

(defn-spec place-order! :robinhood/order
  "Places an order on the given Robinhood account."
  [auth :robinhood/auth
   order any?]
  (let [account-url (:url (first (account-info auth)))
        order (merge order {:account account-url})]
    (->> (u/post-body "https://api.robinhood.com/orders/" order auth)
         (conj [])
         coerce-orders
         first)))

(defn-spec place-option-order! :robinhood/option-order
  [auth :robinhood/auth
   order any?]
  (->> (u/post-body "https://api.robinhood.com/options/orders/" order auth)
       (conj [])
       coerce-option-orders
       first))

;;-----    AUTHED -- CRYPTO ORDERS     --------------------------------------------

;; (s/def ::currency_pair_id uuid?)
;; (s/def ::account_id uuid?)
;; (s/def ::ref_id uuid?)
;; (s/def :crypto-order/quantity float?)
;;
;; (s/def ::crypto-order
;;   (s/keys :req-un [::currency_pair_id ::ref_id ::type ::time_in_force
;;                    ::trigger ::price ::side :crypto-order/quantity]
;;           :opt-un [::account_id ::stop-price ::client-id ::extended-hours]))

(defn place-crypto-order!
  [auth crypto-order]
  ;; {:pre [(s/valid? :robinhood/auth auth) (s/valid? ::crypto-order crypto-order)]}
  (let [account-id (:id (first (account-nummus-info auth)))]
    (u/post-body "https://nummus.robinhood.com/orders/"
                 (merge crypto-order {:account_id account-id})
                 auth)))

;; (s/fdef place-crypto-order!
;;   :args (s/cat :auth :robinhood/auth :crypto-order ::crypto-order)
;;   :ret map?)

;;-----    TODO     --------------------------------------------
;;     Browse robinhood more and add to this list of TODO's
;;     https://api.robinhood.com/marketdata/options/historicals/200041ff-60ca-4dec-a5e9-0d4a02732a30/?span=day&interval=5minute
