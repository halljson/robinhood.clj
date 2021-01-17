(ns robinhood.clj.spec
  (:require [clojure.string :as string]
            [clojure.spec.alpha :as s]))

;; --- Auth -------------------------------------------------------------------

(s/def :robinhood/access-token string?)

(s/def :robinhood/auth (s/keys :req [:robinhood/access-token]))

;; --- General Domain ---------------------------------------------------------
(s/def :robinhood/state-name string?)
(s/def :robinhood/country string?)
(s/def :robinhood/city-name string?)
(s/def :robinhood/id string?)
(s/def :robinhood/url string?)
(s/def :robinhood/timestamp string?)
(s/def :robinhood/short-date string?) ; "2018-10-01")

(s/def :robinhood/instrument-url :robinhood/url)
(s/def :robinhood/instrument-urls (s/coll-of :robinhood/instrument-url))

(s/def :robinhood/ticker string?)
(s/def :robinhood/symbol string?)
(s/def :robinhood/symbols (s/coll-of :robinhood/symbol))
(s/def :robinhood/strike-price float?)

(s/def :robinhood/direction       #{"up" "down"})
(s/def :robinhood/type            #{"put" "call"})
(s/def :robinhood/bounds          #{"regular" "trading" "24_7"})
(s/def :robinhood/time-in-force   #{"gfd" "gtc"})
(s/def :robinhood/interval        #{"5minute" "10minute" "week" "day"})
(s/def :robinhood/session         #{"reg" "post"})
(s/def :robinhood/span            #{"day" "week" "year" "5year"})
(s/def :robinhood/tradability     #{"tradable" "untradable"})
(s/def :robinhood/rhs-tradability #{"tradable" "untradable"})
(s/def :robinhood/state string?)
;; #{"active" "inactive" "expired" "pending" "queued"}

;; --- Client Input Validation ------------------------------------------------

(s/def :symbol-query/symbols string?)
(s/def :robinhood.client/symbol-query (s/keys :req-un [:symbol-query/symbols]))
(s/def :robinhood.client/symbol   string?)
(s/def :robinhood.client/symbols  string?) ;NOTE should be comma sep'd & plural
(s/def :robinhood.client/symbol-list (s/coll-of string?))

(s/def :robinhood.client/direction :robinhood/direction)
(s/def :robinhood.client/type      :robinhood/type)
(s/def :robinhood.client/bounds    :robinhood/bounds)
(s/def :robinhood.client/interval  :robinhood/interval)
(s/def :robinhood.client/span      :robinhood/span)

;; --- Quotes -----------------------------------------------------------------

(s/def :robinhood.quote/symbol :robinhood/symbol)
(s/def :robinhood.quote/updated-at :robinhood/timestamp)
(s/def :robinhood.quote/bid-size int?)
(s/def :robinhood.quote/ask-size int?)
(s/def :robinhood.quote/bid-price float?)
(s/def :robinhood.quote/ask-price float?)
(s/def :robinhood.quote/open-price float?)
(s/def :robinhood.quote/close-price float?)
(s/def :robinhood.quote/high-price float?)
(s/def :robinhood.quote/low-price float?)
(s/def :robinhood.quote/last-trade-price float?)
(s/def :robinhood.quote/last-trade-price-source string?)
(s/def :robinhood.quote/has-traded boolean?)
(s/def :robinhood.quote/trading-halted boolean?)
(s/def :robinhood.quote/previous-close float?)
(s/def :robinhood.quote/previous-close-date :robinhood/short-date)
(s/def :robinhood.quote/last-extended-hours-trade-price (s/nilable float?))
(s/def :robinhood.quote/adjusted-previous-close float?)
(s/def :robinhood.quote/instrument :robinhood/instrument-url)

(s/def :robinhood/quote
  (s/keys
   :opt-un [:robinhood.quote/symbol
            :robinhood.quote/instrument
            :robinhood.quote/updated-at
            :robinhood.quote/has-traded
            :robinhood.quote/bid-size
            :robinhood.quote/bid-price
            :robinhood.quote/ask-size
            :robinhood.quote/ask-price
            :robinhood.quote/previous-close
            :robinhood.quote/previous-close-date
            :robinhood.quote/last-extended-hours-trade-price
            :robinhood.quote/trading-halted
            :robinhood.quote/last-trade-price
            :robinhood.quote/last-trade-price-source
            :robinhood.quote/adjusted-previous-close]))

(s/def :robinhood/quotes (s/coll-of :robinhood/quote))

;; --- Instruments ------------------------------------------------------------

(s/def :robinhood.instrument/splits string?)
(s/def :robinhood.instrument/name string?)
(s/def :robinhood.instrument/symbol :robinhood/symbol)
(s/def :robinhood.instrument/tradeable boolean?)
(s/def :robinhood.instrument/margin-initial-ratio float?)
;(s/double-in :min 0 :max 1.0 :NaN? false :infinite? false))
(s/def :robinhood.instrument/maintenance-ratio float?)
;(s/double-in :min 0 :max 1.0 :NaN? false :infinite? false))
(s/def :robinhood.instrument/day-trade-ratio float?)
;(s/double-in :min 0 :max 1.0 :NaN? false :infinite? false))
(s/def :robinhood.instrument/bloomberg-unique string?)         ;"EQ0010169500001000"
(s/def :robinhood.instrument/state string? #_:robinhood/state)
(s/def :robinhood.instrument/tradability string? #_:robinhood/tradability)
(s/def :robinhood.instrument/rhs-tradability string? #_:robinhood/rhs-tradability)
(s/def :robinhood.instrument/tradable-chain-id string? #_:robinhood/id)   ;"cee01a93-626e-4ee6-9b04-60e2fd1392d1"
(s/def :robinhood.instrument/min-tick-size (s/nilable string?))
(s/def :robinhood.instrument/simple-name string?)
(s/def :robinhood.instrument/list-date string?)                 ;"1990-01-02"
(s/def :robinhood.instrument/country :robinhood/country)
(s/def :robinhood.instrument/market string?)                    ;"https://api.robinhood.com/markets/XNAS/"
(s/def :robinhood.instrument/type #{"stock" "etp" "adr"})
(s/def :robinhood.instrument/quote string?)                     ;"https://api.robinhood.com/quotes/AAPL/"
(s/def :robinhood.instrument/fundamentals string?)              ;"https://api.robinhood.com/fundamentals/AAPL/"
(s/def :robinhood.instrument/id :robinhood/id)
(s/def :robinhood.instrument/url string?)
(s/def :robinhood/instrument
  (s/keys
   :opt-un [:robinhood.instrument/splits
            :robinhood.instrument/symbol
            :robinhood.instrument/name
            :robinhood.instrument/tradeable
            :robinhood.instrument/bloomberg-unique
            :robinhood.instrument/day-trade-ratio
            :robinhood.instrument/type
            :robinhood.instrument/state
            :robinhood.instrument/margin-initial-ratio
            :robinhood.instrument/tradable-chain-id
            :robinhood.instrument/min-tick-size
            :robinhood.instrument/simple-name
            :robinhood.instrument/tradability
            :robinhood.instrument/id
            :robinhood.instrument/list-date
            :robinhood.instrument/quote
            :robinhood.instrument/fundamentals
            :robinhood.instrument/market
            :robinhood.instrument/maintenance-ratio
            :robinhood.instrument/rhs-tradability
            :robinhood.instrument/url
            :robinhood.instrument/country]))

(s/def :robinhood/instruments (s/coll-of :robinhood/instrument))

;; --- Fundamentals -----------------------------------------------------------

(s/def :robinhood.fundamental/description string?)
(s/def :robinhood.fundamental/pe-ratio (s/nilable float?))
(s/def :robinhood.fundamental/instrument :robinhood/instrument-url)
(s/def :robinhood.fundamental/year-founded (s/nilable (s/int-in 1500 2100)))
(s/def :robinhood.fundamental/headquarters-state :robinhood/state-name)
(s/def :robinhood.fundamental/headquarters-city :robinhood/city-name)
(s/def :robinhood.fundamental/shares-outstanding float?)
(s/def :robinhood.fundamental/market-cap float?)
(s/def :robinhood.fundamental/dividend-yield (s/nilable float?))
(s/def :robinhood.fundamental/industry string?)
(s/def :robinhood.fundamental/sector string?)
(s/def :robinhood.fundamental/ceo string?)
(s/def :robinhood.fundamental/num-employees (s/nilable int?))
(s/def :robinhood.fundamental/volume float?)
(s/def :robinhood.fundamental/high-52-weeks float?)
(s/def :robinhood.fundamental/low-52-weeks float?)
(s/def :robinhood.fundamental/high float?)
(s/def :robinhood.fundamental/low float?)
(s/def :robinhood.fundamental/open float?)
(s/def :robinhood.fundamental/close float?)
(s/def :robinhood.fundamental/average-volume float?)
(s/def :robinhood.fundamental/average-volume-2-weeks float?)

(s/def :robinhood/fundamental
  (s/keys
   :opt-un [:robinhood.fundamental/description
            :robinhood.fundamental/open
            :robinhood.fundamental/year-founded
            :robinhood.fundamental/headquarters-state
            :robinhood.fundamental/shares-outstanding
            :robinhood.fundamental/pe-ratio
            :robinhood.fundamental/high-52-weeks
            :robinhood.fundamental/low-52-weeks
            :robinhood.fundamental/high
            :robinhood.fundamental/low
            :robinhood.fundamental/dividend-yield
            :robinhood.fundamental/instrument
            :robinhood.fundamental/industry
            :robinhood.fundamental/headquarters-city
            :robinhood.fundamental/sector
            :robinhood.fundamental/ceo
            :robinhood.fundamental/market-cap
            :robinhood.fundamental/num-employees
            :robinhood.fundamental/volume
            :robinhood.fundamental/average-volume-2-weeks
            :robinhood.fundamental/average-volume]))

(s/def :robinhood/fundamentals (s/coll-of :robinhood/fundamental))

;; --- Historicals ------------------------------------------------------------

(s/def :robinhood.historical.item/begins-at :robinhood/timestamp)
(s/def :robinhood.historical.item/session :robinhood/session)
(s/def :robinhood.historical.item/interpolated boolean?)
(s/def :robinhood.historical.item/volume int?)
(s/def :robinhood.historical.item/high-price float?)
(s/def :robinhood.historical.item/low-price float?)
(s/def :robinhood.historical.item/open-price float?)
(s/def :robinhood.historical.item/close-price float?)

(s/def :robinhood.historical/item
  (s/keys
   :opt-un [:robinhood.historical.item/begins-at
            :robinhood.historical.item/open-price
            :robinhood.historical.item/close-price
            :robinhood.historical.item/high-price
            :robinhood.historical.item/low-price
            :robinhood.historical.item/volume
            :robinhood.historical.item/session
            :robinhood.historical.item/interpolated]))

(s/def :robinhood.historical/interval     string?)
(s/def :robinhood.historical/bounds       string?)
(s/def :robinhood.historical/span         string?)
(s/def :robinhood.historical/instrumentid string?)
(s/def :robinhood.historical/instrument   :robinhood/instrument-url)
(s/def :robinhood.historical/symbol       :robinhood/symbol)
(s/def :robinhood.historical/quote        :robinhood/url)

(s/def :robinhood.historical/historicals
  (s/coll-of :robinhood.historical/item))

(s/def :robinhood/historical
  (s/keys
   :opt-un [:robinhood.historical/historicals
            :robinhood.historical/interval
            :robinhood.historical/span
            :robinhood.historical/bounds
            :robinhood.historical/instrument
            :robinhood.historical/instrumentid
            :robinhood.historical/quote
            :robinhood.historical/symbol]))

(s/def :robinhood/historicals (s/coll-of :robinhood/historical))

;; --- Option Chain Bases -----------------------------------------------------

;option-chain-base.underlying-instrument
(s/def :robinhood.option-chain-base.underlying-instrument/id :robinhood/id)
(s/def :robinhood.option-chain-base.underlying-instrument/quantity int?)
(s/def :robinhood.option-chain-base.underlying-instrument/instrument
  :robinhood/instrument-url)

(s/def :robinhood.option-chain-base/underlying-instrument
  (s/keys
   :opt-un [:robinhood.option-chain-base.underlying-instrument/instrument
            :robinhood.option-chain-base.underlying-instrument/id
            :robinhood.option-chain-base.underlying-instrument/quantity]))

(s/def :robinhood.option-chain-base/underlying-instruments
  (s/coll-of :robinhood.option-chain-base/underlying-instrument))

;option-chain-base.min-tick
(s/def :robinhood.option-chain-base.min-ticks/cutoff-price float?)
(s/def :robinhood.option-chain-base.min-ticks/below-tick float?)
(s/def :robinhood.option-chain-base.min-ticks/above-tick float?)

(s/def :robinhood.option-chain-base/min-ticks
  (s/keys
   :opt-un [:robinhood.option-chain-base.min-ticks/cutoff-price
            :robinhood.option-chain-base.min-ticks/below-tick
            :robinhood.option-chain-base.min-ticks/above-tick]))

;option-chain-base
(s/def :robinhood.option-chain-base/can-open-position boolean?)
(s/def :robinhood.option-chain-base/trade-value-multiplier float?)
(s/def :robinhood.option-chain-base/symbol :robinhood/symbol)
(s/def :robinhood.option-chain-base/id :robinhood/id)
(s/def :robinhood.option-chain-base/expiration-dates
  (s/coll-of :robinhood/short-date))

(s/def :robinhood/option-chain-base
  (s/keys
   :opt-un [:robinhood.option-chain-base/can-open-position
            :robinhood.option-chain-base/symbol
            :robinhood.option-chain-base/trade-value-multiplier
            :robinhood.option-chain-base/underlying-instruments
            :robinhood.option-chain-base/expiration-dates
            :robinhood.option-chain-base/min-ticks
            :robinhood.option-chain-base/id]))

(s/def :robinhood/option-chain-bases
  (s/coll-of :robinhood/option-chain-base))

;; --- Option Date Chains -----------------------------------------------------

(s/def :robinhood.option-date-chain.min-ticks/cutoff-price float?)
(s/def :robinhood.option-date-chain.min-ticks/below-tick float?)
(s/def :robinhood.option-date-chain.min-ticks/above-tick float?)

(s/def :robinhood.option-date-chain/min-ticks
  (s/keys
   :opt-un [:robinhood.option-date-chain.min-ticks/cutoff-price
            :robinhood.option-date-chain.min-ticks/below-tick
            :robinhood.option-date-chain.min-ticks/above-tick]))

(s/def :robinhood.option-date-chain/updated-at :robinhood/timestamp)
(s/def :robinhood.option-date-chain/strike-price :robinhood/strike-price)
(s/def :robinhood.option-date-chain/issue-date :robinhood/short-date)
(s/def :robinhood.option-date-chain/type :robinhood/type)
(s/def :robinhood.option-date-chain/state :robinhood/state)
(s/def :robinhood.option-date-chain/chain-id :robinhood/id)
(s/def :robinhood.option-date-chain/tradability :robinhood/tradability)
(s/def :robinhood.option-date-chain/id string?)
(s/def :robinhood.option-date-chain/url string?)
(s/def :robinhood.option-date-chain/expiration-date :robinhood/short-date)
(s/def :robinhood.option-date-chain/created-at :robinhood/timestamp)
(s/def :robinhood.option-date-chain/sellout-datetime :robinhood/timestamp)
(s/def :robinhood.option-date-chain/chain-symbol :robinhood/symbol)

(s/def :robinhood/option-date-chain
  (s/keys
   :opt-un [:robinhood.option-date-chain/updated-at
            :robinhood.option-date-chain/strike-price
            :robinhood.option-date-chain/issue-date
            :robinhood.option-date-chain/type
            :robinhood.option-date-chain/state
            :robinhood.option-date-chain/chain-id
            :robinhood.option-date-chain/min-ticks
            :robinhood.option-date-chain/tradability
            :robinhood.option-date-chain/id
            :robinhood.option-date-chain/url
            :robinhood.option-date-chain/expiration-date
            :robinhood.option-date-chain/created-at
            :robinhood.option-date-chain/chain-symbol]))

(s/def :robinhood/option-date-chains (s/coll-of :robinhood/option-date-chain))

;; --- Option Contract Data ---------------------------------------------------

(s/def :robinhood.option-contract/adjusted-mark-price (s/nilable float?))
(s/def :robinhood.option-contract/instrument string?)
(s/def :robinhood.option-contract/implied-volatility (s/nilable float?))
(s/def :robinhood.option-contract/high-price (s/nilable float?))
(s/def :robinhood.option-contract/low-price (s/nilable float?))
(s/def :robinhood.option-contract/volume (s/nilable int?))
(s/def :robinhood.option-contract/ask-size (s/nilable int?))
(s/def :robinhood.option-contract/ask-price (s/nilable float?))
(s/def :robinhood.option-contract/mark-price (s/nilable float?))
(s/def :robinhood.option-contract/bid-price (s/nilable float?))
(s/def :robinhood.option-contract/bid-size (s/nilable int?))
(s/def :robinhood.option-contract/chance-of-profit-short (s/nilable float?))
(s/def :robinhood.option-contract/chance-of-profit-long (s/nilable float?))
(s/def :robinhood.option-contract/delta (s/nilable float?))
(s/def :robinhood.option-contract/theta (s/nilable float?))
(s/def :robinhood.option-contract/rho (s/nilable float?))
(s/def :robinhood.option-contract/gamma (s/nilable float?))
(s/def :robinhood.option-contract/vega (s/nilable float?))
(s/def :robinhood.option-contract/previous-close-price (s/nilable float?))
(s/def :robinhood.option-contract/break-even-price (s/nilable float?))
(s/def :robinhood.option-contract/previous-close-date string?)
(s/def :robinhood.option-contract/open-interest (s/nilable int?))
(s/def :robinhood.option-contract/last-trade-price (s/nilable float?))
(s/def :robinhood.option-contract/last-trade-size (s/nilable int?))
(s/def :robinhood.option-contract/high-fill-rate-buy-price (s/nilable float?))
(s/def :robinhood.option-contract/high-fill-rate-sell-price (s/nilable float?))
(s/def :robinhood.option-contract/low-fill-rate-buy-price (s/nilable float?))
(s/def :robinhood.option-contract/low-fill-rate-sell-price (s/nilable float?))

(s/def :robinhood/option-contract
  (s/keys
   :opt-un [:robinhood.option-contract/adjusted-mark-price
            :robinhood.option-contract/high-fill-rate-buy-price
            :robinhood.option-contract/high-fill-rate-sell-price
            :robinhood.option-contract/low-fill-rate-buy-price
            :robinhood.option-contract/low-fill-rate-sell-price
            :robinhood.option-contract/instrument
            :robinhood.option-contract/break-even-price
            :robinhood.option-contract/open-interest
            :robinhood.option-contract/implied-volatility
            :robinhood.option-contract/low-price
            :robinhood.option-contract/high-price
            :robinhood.option-contract/chance-of-profit-short
            :robinhood.option-contract/chance-of-profit-long
            :robinhood.option-contract/ask-size
            :robinhood.option-contract/ask-price
            :robinhood.option-contract/volume
            :robinhood.option-contract/bid-size
            :robinhood.option-contract/bid-price
            :robinhood.option-contract/mark-price
            :robinhood.option-contract/rho
            :robinhood.option-contract/theta
            :robinhood.option-contract/delta
            :robinhood.option-contract/gamma
            :robinhood.option-contract/vega
            :robinhood.option-contract/previous-close-price
            :robinhood.option-contract/previous-close-date
            :robinhood.option-contract/last-trade-price
            :robinhood.option-contract/last-trade-size]))

(s/def :robinhood/option-contracts (s/coll-of :robinhood/option-contract))

(s/def :robinhood/price (s/merge :robinhood/option-contract :robinhood/option-date-chain))
(s/def :robinhood/prices (s/coll-of :robinhood/price))

;; --- Account Info -----------------------------------------------------------

(s/def :robinhood.account-info.margin-balances/updated-at :robinhood/timestamp)
(s/def :robinhood.account-info.margin-balances/margin-limit float?)
(s/def :robinhood.account-info.margin-balances/unsettled-debit float?)
(s/def :robinhood.account-info.margin-balances/uncleared-nummus-deposits float?)
(s/def :robinhood.account-info.margin-balances/sma float?)
(s/def :robinhood.account-info.margin-balances/overnight-buying-power float?)
(s/def :robinhood.account-info.margin-balances/cash-held-for-orders float?)
(s/def :robinhood.account-info.margin-balances/cash-pending-from-options-events float?)
(s/def :robinhood.account-info.margin-balances/cash-held-for-dividends float?)
(s/def :robinhood.account-info.margin-balances/unsettled-funds float?)
(s/def :robinhood.account-info.margin-balances/unallocated-margin-cash float?)
(s/def :robinhood.account-info.margin-balances/overnight-ratio float?)
(s/def :robinhood.account-info.margin-balances/day-trade-ratio float?)
(s/def :robinhood.account-info.margin-balances/overnight-buying-power-held-for-orders float?)
(s/def :robinhood.account-info.margin-balances/uncleared-deposits float?)
(s/def :robinhood.account-info.margin-balances/cash float?)
(s/def :robinhood.account-info.margin-balances/cash-held-for-options-collateral float?)
(s/def :robinhood.account-info.margin-balances/day-trade-buying-power-held-for-orders float?)
(s/def :robinhood.account-info.margin-balances/gold-equity-requirement float?)
(s/def :robinhood.account-info.margin-balances/start-of-day-dtbp float?)
(s/def :robinhood.account-info.margin-balances/day-trade-buying-power float?)
(s/def :robinhood.account-info.margin-balances/cash-held-for-nummus-restrictions float?)
(s/def :robinhood.account-info.margin-balances/outstanding-interest float?)
(s/def :robinhood.account-info.margin-balances/day-trades-protection boolean?)
(s/def :robinhood.account-info.margin-balances/created-at :robinhood/timestamp)
(s/def :robinhood.account-info.margin-balances/marked-pattern-day-trader-date
  (s/nilable :robinhood/timestamp))
(s/def :robinhood.account-info.margin-balances/start-of-day-overnight-buying-power float?)
(s/def :robinhood.account-info.margin-balances/cash-available-for-withdrawal float?)

(s/def :robinhood.account-info/margin-balances
  (s/keys
   :opt-un [:robinhood.account-info.margin-balances/updated-at
            :robinhood.account-info.margin-balances/margin-limit
            :robinhood.account-info.margin-balances/unsettled-debit
            :robinhood.account-info.margin-balances/uncleared-nummus-deposits
            :robinhood.account-info.margin-balances/sma
            :robinhood.account-info.margin-balances/overnight-buying-power
            :robinhood.account-info.margin-balances/cash-held-for-orders
            :robinhood.account-info.margin-balances/cash-pending-from-options-events
            :robinhood.account-info.margin-balances/cash-held-for-dividends
            :robinhood.account-info.margin-balances/unsettled-funds
            :robinhood.account-info.margin-balances/unallocated-margin-cash
            :robinhood.account-info.margin-balances/overnight-ratio
            :robinhood.account-info.margin-balances/day-trade-ratio
            :robinhood.account-info.margin-balances/overnight-buying-power-held-for-orders
            :robinhood.account-info.margin-balances/uncleared-deposits
            :robinhood.account-info.margin-balances/cash
            :robinhood.account-info.margin-balances/cash-held-for-options-collateral
            :robinhood.account-info.margin-balances/day-trade-buying-power-held-for-orders
            :robinhood.account-info.margin-balances/gold-equity-requirement
            :robinhood.account-info.margin-balances/start-of-day-dtbp
            :robinhood.account-info.margin-balances/day-trade-buying-power
            :robinhood.account-info.margin-balances/cash-held-for-nummus-restrictions
            :robinhood.account-info.margin-balances/outstanding-interest
            :robinhood.account-info.margin-balances/day-trades-protection
            :robinhood.account-info.margin-balances/created-at
            :robinhood.account-info.margin-balances/marked-pattern-day-trader-date
            :robinhood.account-info.margin-balances/start-of-day-overnight-buying-power
            :robinhood.account-info.margin-balances/cash-available-for-withdrawal]))

(s/def :robinhood.account-info/only-position-closing-trades boolean?)
(s/def :robinhood.account-info/withdrawal-halted boolean?)
(s/def :robinhood.account-info/state :robinhood/state)
(s/def :robinhood.account-info/portfolio :robinhood/url)
(s/def :robinhood.account-info/can-downgrade-to-cash :robinhood/url)
(s/def :robinhood.account-info/cash-available-for-withdrawal float?)
(s/def :robinhood.account-info/account-number string?)
(s/def :robinhood.account-info/created-at :robinhood/timestamp)
(s/def :robinhood.account-info/deposit-halted boolean?)
(s/def :robinhood.account-info/uncleared-deposits float?)
(s/def :robinhood.account-info/rhs-account-number int?)
(s/def :robinhood.account-info/positions :robinhood/url)
(s/def :robinhood.account-info/url :robinhood/url)
(s/def :robinhood.account-info/unsettled-debit float?)
(s/def :robinhood.account-info/deactivated boolean?)
(s/def :robinhood.account-info/buying-power float?)
(s/def :robinhood.account-info/max-ach-early-access-amount float?)
(s/def :robinhood.account-info/unsettled-funds float?)
(s/def :robinhood.account-info/type #{"margin"}) ;; NOTE need non-margin?
(s/def :robinhood.account-info/user :robinhood/url)
(s/def :robinhood.account-info/option-level #{"option_level_0"
                                              "option_level_1"
                                              "option_level_2"
                                              "option_level_3"})
(s/def :robinhood.account-info/cash-held-for-orders float?)
(s/def :robinhood.account-info/updated-at :robinhood/timestamp)
(s/def :robinhood.account-info/is-pinnacle-account boolean?)
(s/def :robinhood.account-info/sma float?)
(s/def :robinhood.account-info/sweep-enabled boolean?)
(s/def :robinhood.account-info/cash float?)
(s/def :robinhood.account-info/sma-held-for-orders float?)
(s/def :robinhood.account-info/cash-balances any?)

(s/def :robinhood/account-info
  (s/keys
   :opt-un [:robinhood.account-info/only-position-closing-trades
            :robinhood.account-info/withdrawal-halted
            :robinhood.account-info/state
            :robinhood.account-info/portfolio
            :robinhood.account-info/can-downgrade-to-cash
            :robinhood.account-info/cash-available-for-withdrawal
            :robinhood.account-info/account-number
            :robinhood.account-info/created-at
            :robinhood.account-info/deposit-halted
            :robinhood.account-info/uncleared-deposits
            :robinhood.account-info/rhs-account-number
            :robinhood.account-info/positions
            :robinhood.account-info/url
            :robinhood.account-info/unsettled-debit
            :robinhood.account-info/deactivated
            :robinhood.account-info/buying-power
            :robinhood.account-info/max-ach-early-access-amount
            :robinhood.account-info/unsettled-funds
            :robinhood.account-info/type
            :robinhood.account-info/user
            :robinhood.account-info/option-level
            :robinhood.account-info/margin-balances
            :robinhood.account-info/cash-held-for-orders
            :robinhood.account-info/updated-at
            :robinhood.account-info/is-pinnacle-account
            :robinhood.account-info/sma
            :robinhood.account-info/sweep-enabled
            #_:robinhood.account-info/instant-eligibility
            :robinhood.account-info/cash
            :robinhood.account-info/sma-held-for-orders
            :robinhood.account-info/cash-balances]))

;; --- Equity Historicals -----------------------------------------------------

(s/def :robinhood.equity-historicals.item/net-return float?)
(s/def :robinhood.equity-historicals.item/open-market-value float?)
(s/def :robinhood.equity-historicals.item/open-equity float?)
(s/def :robinhood.equity-historicals.item/close-equity float?)
(s/def :robinhood.equity-historicals.item/adjusted-close-equity float?)
(s/def :robinhood.equity-historicals.item/adjusted-open-equity float?)
(s/def :robinhood.equity-historicals.item/close-market-value float?)
(s/def :robinhood.equity-historicals.item/begins-at :robinhood/timestamp)
(s/def :robinhood.equity-historicals.item/session :robinhood/session)

(s/def :robinhood.equity-historicals/item
  (s/keys
   :opt-un [:robinhood.equity-historicals.item/net-return
            :robinhood.equity-historicals.item/open-market-value
            :robinhood.equity-historicals.item/open-equity
            :robinhood.equity-historicals.item/close-equity
            :robinhood.equity-historicals.item/adjusted-close-equity
            :robinhood.equity-historicals.item/adjusted-open-equity
            :robinhood.equity-historicals.item/close-market-value
            :robinhood.equity-historicals.item/begins-at
            :robinhood.equity-historicals.item/session]))

(s/def :robinhood.equity-historicals/equity-historicals
  (s/coll-of :robinhood.equity-historicals/item))

(s/def :robinhood.equity-historicals/adjusted-previous-close-equity float?)
(s/def :robinhood.equity-historicals/previous-close-equity float?)
(s/def :robinhood.equity-historicals/open-equity float?)
(s/def :robinhood.equity-historicals/open-time :robinhood/timestamp)
(s/def :robinhood.equity-historicals/use-new-hp boolean?)
(s/def :robinhood.equity-historicals/bounds :robinhood/bounds)
(s/def :robinhood.equity-historicals/adjusted-open-equity float?)
(s/def :robinhood.equity-historicals/interval :robinhood/interval)
(s/def :robinhood.equity-historicals/total-return float?)
(s/def :robinhood.equity-historicals/span :robinhood/span)

(s/def :robinhood/equity-historicals
  (s/keys
   :opt-un [:robinhood.equity-historicals/adjusted-previous-close-equity
            :robinhood.equity-historicals/equity-historicals
            :robinhood.equity-historicals/previous-close-equity
            :robinhood.equity-historicals/open-equity
            :robinhood.equity-historicals/open-time
            :robinhood.equity-historicals/use-new-hp
            :robinhood.equity-historicals/bounds
            :robinhood.equity-historicals/adjusted-open-equity
            :robinhood.equity-historicals/interval
            :robinhood.equity-historicals/total-return
            :robinhood.equity-historicals/span]))

;; --- Order Common -----------------------------------------------------------

(s/def :order/type #{"market" "limit"})
(s/def :order/trigger #{"immediate" "stop"})
(s/def :order/side #{"sell" "buy"})
(s/def :order/quantity int?)
(s/def :order/state
  #{"filled" "cancelled" "confirmed" "rejected" "pending" "queued" "failed"})
(s/def :order/response-category
  #{"success"
    "unknown"
    "end_of_day"
    "invalid_limit"
    "invalid_stop"
    "failed_before_mainst"
    nil})

;; --- Orders -----------------------------------------------------------------

(s/def :robinhood.order.execution/timestamp :robinhood/timestamp)
(s/def :robinhood.order.execution/price float?)
(s/def :robinhood.order.execution/settlement-date :robinhood/short-date)
(s/def :robinhood.order.execution/id :robinhood/id)
(s/def :robinhood.order.execution/quantity float?)

(s/def :robinhood.order/execution
  (s/keys
   :opt-un [:robinhood.order.execution/timestamp
            :robinhood.order.execution/price
            :robinhood.order.execution/settlement-date
            :robinhood.order.execution/id
            :robinhood.order.execution/quantity]))

(s/def :robinhood.order/executions (s/coll-of :robinhood.order/execution))

(s/def :robinhood.order/updated-at :robinhood/timestamp )
(s/def :robinhood.order/time-in-force :robinhood/time-in-force)
(s/def :robinhood.order/instrument :robinhood/url)
(s/def :robinhood.order/cumulative-quantity float?)
(s/def :robinhood.order/stop-price (s/nilable float?))
(s/def :robinhood.order/extended-hours boolean?)
(s/def :robinhood.order/type :order/type)
(s/def :robinhood.order/reject-reason (s/nilable string?))
(s/def :robinhood.order/state :order/state)
(s/def :robinhood.order/account :robinhood/url)
(s/def :robinhood.order/last-transaction-at :robinhood/timestamp)
(s/def :robinhood.order/average-price (s/nilable float?))
(s/def :robinhood.order/id :robinhood/id)
(s/def :robinhood.order/url :robinhood/url)
(s/def :robinhood.order/override-dtbp-checks boolean?)
(s/def :robinhood.order/side :order/side)
(s/def :robinhood.order/position :robinhood/url)
(s/def :robinhood.order/fees float?)
(s/def :robinhood.order/cancel (s/nilable :robinhood/url))
(s/def :robinhood.order/response-category :order/response-category)
(s/def :robinhood.order/quantity float?)
(s/def :robinhood.order/trigger :order/trigger)
(s/def :robinhood.order/override-day-trade-checks boolean?)
(s/def :robinhood.order/created-at :robinhood/timestamp)
(s/def :robinhood.order/ref-id (s/nilable :robinhood/id))
(s/def :robinhood.order/price (s/nilable float?))

(s/def :robinhood/order
  (s/keys
   :opt-un [:robinhood.order/updated-at
            :robinhood.order/time-in-force
            :robinhood.order/instrument
            :robinhood.order/cumulative-quantity
            :robinhood.order/stop-price
            :robinhood.order/extended-hours
            :robinhood.order/type
            :robinhood.order/reject-reason
            :robinhood.order/state
            :robinhood.order/account
            :robinhood.order/last-transaction-at
            :robinhood.order/average-price
            :robinhood.order/id
            :robinhood.order/url
            :robinhood.order/override-dtbp-checks
            :robinhood.order/side
            :robinhood.order/position
            :robinhood.order/fees
            :robinhood.order/cancel
            :robinhood.order/response-category
            :robinhood.order/quantity
            :robinhood.order/trigger
            :robinhood.order/override-day-trade-checks
            :robinhood.order/created-at
            :robinhood.order/ref-id
            :robinhood.order/price
            :robinhood.order/executions]))

(s/def :robinhood/orders (s/coll-of :robinhood/order))

;; --- Option Orders ----------------------------------------------------------

(s/def :robinhood.option-order.legs.executions/timestamp :robinhood/timestamp)
(s/def :robinhood.option-order.legs.executions/price float?)
(s/def :robinhood.option-order.legs.executions/settlement-date :robinhood/short-date)
(s/def :robinhood.option-order.legs.executions/id :robinhood/id)
(s/def :robinhood.option-order.legs.executions/quantity float?)

(s/def :robinhood.option-order.legs/execution
  (s/keys
   :opt-un [:robinhood.option-order.legs.executions/timestamp
            :robinhood.option-order.legs.executions/price
            :robinhood.option-order.legs.executions/settlement-date
            :robinhood.option-order.legs.executions/id
            :robinhood.option-order.legs.executions/quantity]))

(s/def :robinhood.option-order.legs/executions
  (s/coll-of :robinhood.option-order.legs/execution))

(s/def :robinhood.option-order.legs/option :robinhood/url)
(s/def :robinhood.option-order.legs/side :order/side)
(s/def :robinhood.option-order.legs/position-effect #{"open" "close"})
(s/def :robinhood.option-order.legs/id :robinhood/id)
(s/def :robinhood.option-order.legs/ratio-quantity int?)

(s/def :robinhood.option-order/leg
  (s/keys
   :opt-un [:robinhood.option-order.legs/executions
            :robinhood.option-order.legs/option
            :robinhood.option-order.legs/side
            :robinhood.option-order.legs/position-effect
            :robinhood.option-order.legs/id
            :robinhood.option-order.legs/ratio-quantity]))

(s/def :robinhood.option-order/legs (s/coll-of :robinhood.option-order/leg))

(s/def :robinhood.option-order/updated-at :robinhood/id)
(s/def :robinhood.option-order/time-in-force :robinhood/time-in-force)
(s/def :robinhood.option-order/pending-quantity float?)
(s/def :robinhood.option-order/processed-quantity float?)
(s/def :robinhood.option-order/closing-strategy (s/nilable string?))
(s/def :robinhood.option-order/type :order/type)
(s/def :robinhood.option-order/state :order/state)
(s/def :robinhood.option-order/opening-strategy (s/nilable string?))
(s/def :robinhood.option-order/processed-premium float?)
(s/def :robinhood.option-order/chain-id :robinhood/id)
(s/def :robinhood.option-order/id :robinhood/id)
(s/def :robinhood.option-order/premium float?)
(s/def :robinhood.option-order/response-category :order/response-category)
(s/def :robinhood.option-order/cancel-url (s/nilable :robinhood/url))
(s/def :robinhood.option-order/quantity float?)
(s/def :robinhood.option-order/created-at :robinhood/timestamp)
(s/def :robinhood.option-order/ref-id :robinhood/id)
(s/def :robinhood.option-order/price float?)
(s/def :robinhood.option-order/trigger :order/trigger)
(s/def :robinhood.option-order/direction #{"debit" "credit"})
(s/def :robinhood.option-order/canceled-quantity float?)
(s/def :robinhood.option-order/chain-symbol :robinhood/symbol)

(s/def :robinhood/option-order
  (s/keys
   :opt-un [:robinhood.option-order/updated-at
            :robinhood.option-order/time-in-force
            :robinhood.option-order/pending-quantity
            :robinhood.option-order/processed-quantity
            :robinhood.option-order/closing-strategy
            :robinhood.option-order/type
            :robinhood.option-order/state
            :robinhood.option-order/opening-strategy
            :robinhood.option-order/processed-premium
            :robinhood.option-order/chain-id
            :robinhood.option-order/id
            :robinhood.option-order/premium
            :robinhood.option-order/response-category
            :robinhood.option-order/cancel-url
            :robinhood.option-order/quantity
            :robinhood.option-order/created-at
            :robinhood.option-order/ref-id
            :robinhood.option-order/price
            :robinhood.option-order/trigger
            :robinhood.option-order/legs
            :robinhood.option-order/direction
            :robinhood.option-order/canceled-quantity
            :robinhood.option-order/chain-symbol]))

(s/def :robinhood/option-orders (s/coll-of :robinhood/option-order))

;; --- Option Events ----------------------------------------------------------

(s/def :robinhood.option-event.equity-components/symbol string?)
(s/def :robinhood.option-event.equity-components/side string?)
(s/def :robinhood.option-event.equity-components/instrument :robinhood/url)
(s/def :robinhood.option-event.equity-components/id string?)
(s/def :robinhood.option-event.equity-components/price float?)
(s/def :robinhood.option-event.equity-components/quantity float?)

(s/def :robinhood.option-event/equity-component
  (s/keys
   :opt-un [:robinhood.option-event.equity-components/symbol
            :robinhood.option-event.equity-components/side
            :robinhood.option-event.equity-components/instrument
            :robinhood.option-event.equity-components/id
            :robinhood.option-event.equity-components/price
            :robinhood.option-event.equity-components/quantity]))

(s/def :robinhood.option-event/equity-components
  (s/coll-of :robinhood.option-event/equity-component))

(s/def :robinhood.option-event/updated-at :robinhood/timestamp)
(s/def :robinhood.option-event/type string?)
(s/def :robinhood.option-event/state string?)
(s/def :robinhood.option-event/underlying-price float?)
(s/def :robinhood.option-event/total-cash-amount float?)
(s/def :robinhood.option-event/account :robinhood/url)
(s/def :robinhood.option-event/option :robinhood/url)
(s/def :robinhood.option-event/position :robinhood/url)
(s/def :robinhood.option-event/chain-id string?)
(s/def :robinhood.option-event/id string?)
(s/def :robinhood.option-event/quantity float?)
(s/def :robinhood.option-event/event-date :robinhood/timestamp)
(s/def :robinhood.option-event/created-at :robinhood/timestamp)
(s/def :robinhood.option-event/direction :robinhood/direction)
(s/def :robinhood.option-event/cash-component (s/nilable nil))
;; TODO :robinhood.option-event/cash-component ???
;; BODY we might have to come back to this when we're trading risk-undefined?

(s/def :robinhood/option-event
  (s/keys
   :opt-un [:robinhood.option-event/updated-at
            :robinhood.option-event/type
            :robinhood.option-event/state
            :robinhood.option-event/underlying-price
            :robinhood.option-event/total-cash-amount
            :robinhood.option-event/account
            :robinhood.option-event/option
            :robinhood.option-event/position
            :robinhood.option-event/chain-id
            :robinhood.option-event/id
            :robinhood.option-event/quantity
            :robinhood.option-event/event-date
            :robinhood.option-event/created-at
            :robinhood.option-event/direction
            :robinhood.option-event/cash-component
            :robinhood.option-event/equity-components]))

(s/def :robinhood/option-events (s/coll-of :robinhood/option-event))

(comment
  :robinhood.option-event.equity-components/symbol
  :robinhood.option-event.equity-components/side
  :robinhood.option-event.equity-components/instrument
  :robinhood.option-event.equity-components/id
  :robinhood.option-event.equity-components/price
  :robinhood.option-event.equity-components/quantity
  :robinhood.option-event/updated-at
  :robinhood.option-event/type
  :robinhood.option-event/state
  :robinhood.option-event/underlying-price
  :robinhood.option-event/total-cash-amount
  :robinhood.option-event/account
  :robinhood.option-event/option
  :robinhood.option-event/position
  :robinhood.option-event/chain-id
  :robinhood.option-event/id
  :robinhood.option-event/quantity
  :robinhood.option-event/event-date
  :robinhood.option-event/created-at
  :robinhood.option-event/direction
  :robinhood.option-event/cash-component
  :robinhood.option-event/equity-components)
