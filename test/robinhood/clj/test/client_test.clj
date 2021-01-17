(ns robinhood.clj.test.client-test
  (:require [orchestra.spec.test :as st]
            [cartridge.core :as cartridge :refer [cartridge-playback-fixture]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [robinhood.clj.auth :as auth]
            [robinhood.clj.client :refer :all]
            [clojure.test :refer :all]))

;; --- Fixtures ---------------------------------------------------------------

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))

(defn my-key-fn [request]
  (-> request
      (dissoc-in [:raw-request :auth-token])
      (dissoc-in [:raw-request :headers])
      cartridge/saved-request-key))

(use-fixtures :once
  (cartridge-playback-fixture "dev-fixtures/cartridge" my-key-fn))

(def path "dev-fixtures/cartridge")
(def file (io/file path))

#_(cartridge/read-responses-from-disk path)

#_(.delete file)

;; --- Helpers ----------------------------------------------------------------

(defn rand-symbol []
  (rand-nth ["UNG" "YELP" "QCOM" "HAL" "DDD" "QQQ" "SMH" "AAL" "EWZ" "XOM" "DIS" "CMCSA" "TWTR" "INTC" "XOP" "GM" "XRT" "VXX" "ADBE" "EEM" "MU" "AKAM" "NFLX" "FSLR" "GOOGL" "XLV" "KO" "TGT" "NCR" "CVS" "FB" "BABA" "JCP" "DECK" "T" "BAC" "BIDU" "BA" "SLB" "IYR" "SBUX" "URBN" "EFA" "GPRO" "VLO" "TBT" "JNPR" "C" "XHB" "GG" "UPS" "GNC" "SPY" "BBY" "RIG" "WBA" "GPS" "EBAY" "GE" "JNJ" "LOW" "XLF" "GDX" "HLF" "USO" "BKNG" "MRK" "COP" "UAL"]))

(defn n-rand-syms [n]
  (->> rand-symbol (repeatedly n) (into #{})))

;; --- REPL -------------------------------------------------------------------

#_(def symbol (rand-symbol))
#_(def rc' (auth/authed-client))

;; --- TEST -------------------------------------------------------------------

(deftest test-quotes
  (let [rc' (auth/authed-client)
        quotes' (quotes rc' {:symbols "EAF,AAPL"})
        [eaf msft] quotes']
    (is (map? eaf))
    (is (map? msft))
    (is (not (nil? (:robinhood.quote/instrument eaf))))
    (is (not (nil? (:robinhood.quote/instrument msft))))
    (is (int? (:robinhood.quote/bid-size eaf)))
    (is (double? (:robinhood.quote/bid-price eaf)))
    (is (not (nil? (:robinhood.quote/ask-size eaf))))
    (is (not (nil? (:robinhood.quote/symbol eaf))))))

(deftest test-news
  (let [rc' (auth/authed-client)
        news (news rc' "EAF")]
    (is (< 0 (count news)))
    (is (not (some #(= % nil) (map :num-clicks news))))))

(deftest test-orders
  (let [rc' (auth/authed-client)
        orders' (orders rc')]
    (is (every?
         float?
         (->> (map :robinhood.order/executions orders')
              flatten
              (map :robinhood.order.executions/price)
              (into []))))
    (is (every?
         float?
         (filter
          some?
          (map :robinhood.order/average-price orders'))))))

(deftest test-option-orders
  (let [rc' (auth/authed-client)
        option-orders' (option-orders rc')]
    (is (every?
         float?
         (->> (map :robinhood.option-order/legs option-orders')
              flatten
              (map :robinhood.option-order.legs/executions)
              flatten
              (map :robinhood.option-order.legs.executions/price)
              (into []))))
    (is (every?
         float?
         (filter
          some?
          (map :robinhood.option-order/premium option-orders'))))))

(deftest test-account-info
  (let [rc' (auth/authed-client)
        account-info' (account-info rc')]
    (is (pos?
         (get-in
          account-info'
          [:robinhood.account-info/margin-balances
           :robinhood.account-info.margin-balances/overnight-buying-power])))
    (is (some? (:robinhood.account-info/account-number account-info')))))

(deftest test-equity-historicals
  (let [rc' (auth/authed-client)
        account-number (:robinhood.account-info/account-number (account-info rc'))
        equity-historicals' (equity-historicals rc' account-number)]
    (is (some? (:robinhood.equity-historicals/adjusted-open-equity equity-historicals')))
    (is (not-empty (:robinhood.equity-historicals/equity-historicals equity-historicals')))
    (is (every?
         number?
         (map
          :robinhood.equity-historicals.item/open-market-value
          (:robinhood.equity-historicals/equity-historicals equity-historicals'))))))

(deftest test-movers-up
  (let [movers (movers "up")]
    (is (< 0 (count movers)))))

(deftest test-movers-down
  (let [movers (movers "down")]
    (is (< 0 (count movers)))))

(deftest test-instrument
  (let [symbol (rand-symbol)
        rc' (auth/authed-client)
        test-instrument' (instrument rc' {:symbols symbol})]
    (is (= symbol (:robinhood.instrument/symbol test-instrument')))
    (is (double? (:robinhood.instrument/day-trade-ratio test-instrument')))))

(deftest test-fundamentals
  (let [syms (into '() (n-rand-syms 6))
        rc' (auth/authed-client)
        fundamentals' (fundamentals rc' syms)]
    (is (not-empty fundamentals'))
    (is (double? (:robinhood.fundamental/open (first fundamentals'))))
    (is (double? (:robinhood.fundamental/high (first fundamentals'))))
    (is (double? (:robinhood.fundamental/market-cap (first fundamentals'))))
    (is (not-empty (:robinhood.fundamental/sector (first fundamentals'))))))

(deftest test-short-historicals
  (let [symbol (rand-symbol)
        rc' (auth/authed-client)
        historicals' (historicals rc' "day" "year" symbol)]
    (is (= symbol (:robinhood.historical/symbol historicals')))
    (is (< 100 (count (:robinhood.historical/historicals historicals'))))
    (is (double?
         (:robinhood.historical.item/low-price
          (first (:robinhood.historical/historicals historicals')))))))

(deftest test-long-historicals
  (let [symbol (rand-symbol)
        rc' (auth/authed-client)
        historicals' (historicals rc' "week" "5year" symbol)]
    (is (= symbol (:robinhood.historical/symbol historicals')))
    (is (< 100 (count (:robinhood.historical/historicals historicals'))))
    (is (double?
         (:robinhood.historical.item/low-price
          (first (:robinhood.historical/historicals historicals')))))))

(deftest test-option-chain-base
  (let [rc' (auth/authed-client)
        opt-chain (option-chain-base rc' {:symbols "EAF"})]
    (is (not (empty? opt-chain)))
    (is (not-empty (:robinhood.option-chain-base/expiration-dates opt-chain)))
    (is (double?
         (:robinhood.option-chain-base.min-ticks/cutoff-price
          (:robinhood.option-chain-base/min-ticks
           opt-chain))))
    (is (int?
         (:robinhood.option-chain-base.underlying-instrument/quantity
          (first
           (:robinhood.option-chain-base/underlying-instruments
            opt-chain)))))
    (is (double?
         (:robinhood.option-chain-base/trade-value-multiplier
          opt-chain)))))

(deftest test-option-date-chain
  (let [rc' (auth/authed-client)
        opt-chain (option-chain-base rc' {:symbols "EAF"})
        some-date
        (rand-nth (:robinhood.option-chain-base/expiration-dates opt-chain))
        date-chain-items (option-date-chain opt-chain some-date "put")]
    (is (not (empty? date-chain-items)))
    (is (double?
         (:robinhood.option-date-chain.min-ticks/cutoff-price
          (:robinhood.option-date-chain/min-ticks
           (first date-chain-items)))))
    (is (map :robinhood.option-date-chain/chain-symbol date-chain-items))
    (is (map :robinhood.option-date-chain/url date-chain-items))
    (is (map :robinhood.option-date-chain/id date-chain-items))))

(deftest test-opt-chain->date-chain
  (let [rc' (auth/authed-client)
        opt-chain (option-chain-base rc' {:symbols "AAPL"})
        date-chain (opt-chain->date-chain opt-chain "put")]
    (is (not (empty? date-chain)))
    (is (< (count date-chain) 1000))
    (is (> (count date-chain) 10))
    (is (group-by :robinhood.option-date-chain/expiration-date date-chain))
    (is (every?
         float?
         (map :robinhood.option-date-chain/strike-price date-chain)))
    (is (map :robinhood.option-date-chain/chain-symbol date-chain))
    (is (map :robinhood.option-date-chain/url date-chain))
    (is (map :robinhood.option-date-chain/id date-chain))))

(deftest test-date-chain->prices
  (let [rc' (auth/authed-client)
        opt-chain (option-chain-base rc' {:symbols "AAPL"})
        date-chain (opt-chain->date-chain opt-chain "put")
        prices (date-chain->prices rc' (into [] (take 10 date-chain)))
        [price1 price2 & rest] prices]
    (is (every?
         float?
         (->> prices
              (take 30)
              (into [])
              (map :robinhood.option-contract/break-even-price))))
    (is (not (nil? (:robinhood.option-contract/bid-price price1))) "price missing: `:bid-price`")
    (is (not (nil? (:robinhood.option-contract/ask-price price2))) "price missing: `:ask-price`")
    (is (not (nil? (:robinhood.option-contract/delta price1))) "price missing: `:delta`")
    (is (not (nil? (:robinhood.option-contract/theta price2))) "price missing: `:theta`")
    (is (not (nil? (:robinhood.option-contract/vega price1))) "price missing: `:vega`")
    (is (not (nil? (:robinhood.option-contract/rho price2))) "price missing: `:rho`")
    (is (not (nil? (:robinhood.option-contract/gamma price1))) "price missing: `:gamma`")
    (is (not (empty? prices)) "Prices should have contents")))

#_(deftest test-watchlist-option-chain-prices
  (let [watchlist-options (watchlist-option-chain-prices rc' "put")
        [chain1 chain2 & prices] watchlist-options
        [price1 price2 & rest] chain1]
    (is (not (nil? (:bid-price price1))) "price missing: `:bid-price`")
    (is (not (nil? (:ask-price price2))) "price missing: `:ask-price`")
    (is (not (nil? (:delta price1))) "price missing: `:delta`")
    (is (not (nil? (:theta price2))) "price missing: `:theta`")
    (is (not (nil? (:vega price1))) "price missing: `:vega`")
    (is (not (nil? (:rho price2))) "price missing: `:rho`")
    (is (not (nil? (:gamma price1))) "price missing: `:gamma`")
    (is (not (empty? chain1)) "chain1 should have contents")))
    ; (is (not (empty? price1)) "price1 should have contents")))

#_(deftest test-get-options
  (let [options' (:put (get-options rc' {:symbols "EAF"}))
        [price1 price2 & rest] options']
    (is (not (nil? (:bid-price price1))) "price missing: `:bid-price`")
    (is (not (nil? (:ask-price price2))) "price missing: `:ask-price`")
    (is (not (nil? (:delta price1))) "price missing: `:delta`")
    (is (not (nil? (:theta price2))) "price missing: `:theta`")
    (is (not (nil? (:vega price1))) "price missing: `:vega`")
    (is (not (nil? (:rho price2))) "price missing: `:rho`")
    (is (not (nil? (:gamma price1))) "price missing: `:gamma`")
    (is (not (empty? price1)) "chain1 should have contents")))

#_(deftest test-watchlist-options
  (let [watchlist-options' (:put (watchlist-options rc'))
        [chain1 chain2 & prices] watchlist-options'
        [price1 price2 & rest] chain1]
    (is (not (nil? (:bid-price price1))) "price missing: `:bid-price`")
    (is (not (nil? (:ask-price price2))) "price missing: `:ask-price`")
    (is (not (nil? (:delta price1))) "price missing: `:delta`")
    (is (not (nil? (:theta price2))) "price missing: `:theta`")
    (is (not (nil? (:vega price1))) "price missing: `:vega`")
    (is (not (nil? (:rho price2))) "price missing: `:rho`")
    (is (not (nil? (:gamma price1))) "price missing: `:gamma`")
    (is (not (empty? chain1)) "chain1 should have contents")))
    ; (is (not (empty? price1)) "price1 should have contents")))
