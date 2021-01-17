# robinhood.clj

A lightweight clojure wrapper for the [Robinhood Web API](https://github.com/sanko/Robinhood/).

All use cases must follow the [Robinhood TOS](
https://brokerage-static.s3.amazonaws.com/assets/robinhood/legal/Robinhood%20Terms%20and%20Conditions.pdf). For example, you may not use this client to scrape Robinhood data for display on your own website, among other things. Read the TOS.

### Authenticating with environment variables

You'll have to set up your machine's environment variables before hitting any endpoints that require authentication. 

You can get your device token by monitoring your browser's login request to robinhood. Open chrome developer tools > navigate to network tab > login to Robinhood in browser > search network tab for the `token/` request > view request payload > copy device_token.

```
export ROBINHOOD_USER="..."
export ROBINHOOD_PASS="..."
export ROBINHOOD_DEVICE_TOKEN="XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX"
;Don't forget to restart any running repls
```

After adding the above environment variables, `lein test` should run without any failures.

Be sure not to commit any of the above information to this repo for the world to see! If you do, change your passwords immediately and enable 2FA.

### Caution

At the time of writing, the Robinhood Web API lacks official documentation. Hence, working with this wrapper (at least for now) demands some reverse engineering of the robinhood UI. @Sanko's unofficial docs at [Robinhood Web API](https://github.com/sanko/Robinhood/) are good but not 100% up to date.

I've added some notes on how I'm scraping by in the `Dev` section at the bottom.

### REPL Usage

Cd into robinhood.clj and run `lein repl` in order to follow along:

Login:
``` clojure
(use 'robinhood.clj.auth)

# Get a robinhood client
(def rc' (authed-client))

;; output:
:robinhood{:access-token "xxxxxxxxx"}
```

Fetch Quotes:
``` clojure
(use 'robinhood.clj.auth)
(use 'robinhood.clj.client)

(quotes (authed-client) {:symbols "AAPL,MSFT"})

;; output:
(#:robinhood.quote{:updated-at "2021-01-16T01:00:00Z",
                   :instrument
                   "https://api.robinhood.com/instruments/450dfc6d-5510-4d40-abfb-f633b7d9be3e/",
                   :bid-price 126.41,
                   :last-trade-price-source "consolidated",
                   :symbol "AAPL",
                   :last-trade-price 127.14,
                   :ask-price 500.0,
                   :bid-size 21,
                   :ask-size 596,
                   :last-extended-hours-trade-price 126.82,
                   :previous-close 128.91,
                   :has-traded true,
                   :trading-halted false,
                   :adjusted-previous-close 128.91,
                   :previous-close-date "2021-01-14"}
 #:robinhood.quote{:updated-at "2021-01-16T01:00:00Z",
                   :instrument
                   "https://api.robinhood.com/instruments/50810c35-d215-4866-9758-0ada4ac79ffa/",
                   :bid-price 210.91,
                   :last-trade-price-source "consolidated",
                   :symbol "MSFT",
                   :last-trade-price 212.65,
                   :ask-price 215.86,
                   :bid-size 30,
                   :ask-size 200,
                   :last-extended-hours-trade-price 212.55,
                   :previous-close 213.02,
                   :has-traded true,
                   :trading-halted false,
                   :adjusted-previous-close 213.02,
                   :previous-close-date "2021-01-14"})
```



See call options on a ticker:
``` clojure
(use 'robinhood.clj.auth)
(use 'robinhood.clj.client)

(take 2 ;for brevity
 (get-option-chain-prices (authed-client) {:symbols "EAF"} "call"))

;; output (you'll see some logs of all the requests made):
({:robinhood.option-contract/bid-size 101,
  :robinhood.option-contract/break-even-price 11.1,
  :robinhood.option-contract/high-price 3.7,
  :robinhood.option-contract/low-price 3.4,
  :robinhood.option-date-chain/id
  "f7e8bfbe-26a1-4300-952b-038d21f332ae",
  :robinhood.option-contract/instrument
  "https://api.robinhood.com/options/instruments/f7e8bfbe-26a1-4300-952b-038d21f332ae/",
  :robinhood.option-date-chain/tradability "tradable",
  :robinhood.option-contract/last-trade-size 10,
  :robinhood.option-contract/high-fill-rate-buy-price 3.74,
  :robinhood.option-date-chain/sellout-datetime
  "2021-07-16T19:00:00+00:00",
  :robinhood.option-contract/adjusted-mark-price 3.6,
  :robinhood.option-contract/bid-price 3.4,
  :robinhood.option-contract/implied-volatility 0.674616,
  :robinhood.option-date-chain/chain-id
  "b0707f5c-839a-44cd-ba9d-a92fb15ab932",
  :robinhood.option-date-chain/url
  "https://api.robinhood.com/options/instruments/f7e8bfbe-26a1-4300-952b-038d21f332ae/",
  :robinhood.option-date-chain/issue-date "2018-05-15",
  :robinhood.option-date-chain/expiration-date "2021-07-16",
  :robinhood.option-date-chain/created-at
  "2020-11-19T04:36:07.713986Z",
  :robinhood.option-contract/delta 0.828468,
  :robinhood.option-contract/low-fill-rate-sell-price 3.62,
  :robinhood.option-contract/high-fill-rate-sell-price 3.45,
  :robinhood.option-contract/chance-of-profit-short 0.637302,
  :robinhood.option-contract/volume 15,
  :robinhood.option-contract/chance-of-profit-long 0.362698,
  :robinhood.option-date-chain/updated-at
  "2020-11-19T04:36:07.713996Z",
  :robinhood.option-date-chain/chain-symbol "EAF",
  :robinhood.option-contract/theta -0.003522,
  :robinhood.option-date-chain/min-ticks
  #:robinhood.option-date-chain.min-ticks{:above-tick 0.1,
                                          :below-tick 0.05,
                                          :cutoff-price 3.0},
  :robinhood.option-contract/vega 0.018886,
  :robinhood.option-contract/open-interest 598,
  :robinhood.option-contract/ask-price 3.8,
  :robinhood.option-date-chain/strike-price 7.5,
  :robinhood.option-contract/mark-price 3.6,
  :robinhood.option-contract/gamma 0.05084,
  :robinhood.option-contract/last-trade-price 3.7,
  :robinhood.option-date-chain/rhs-tradability "untradable",
  :robinhood.option-contract/low-fill-rate-buy-price 3.58,
  :robinhood.option-contract/rho 0.02546,
  :robinhood.option-contract/previous-close-price 4.5,
  :robinhood.option-contract/ask-size 31,
  :robinhood.option-date-chain/type "call",
  :robinhood.option-contract/previous-close-date "2021-01-14",
  :robinhood.option-date-chain/state "active"}
 {:robinhood.option-contract/bid-size 34,
  :robinhood.option-contract/break-even-price 11.38,
  :robinhood.option-contract/high-price 1.4,
  :robinhood.option-contract/low-price 1.33,
  :robinhood.option-date-chain/id
  "e6dec319-3971-4fbb-8f3b-f74ad455b60f",
  :robinhood.option-contract/instrument
  "https://api.robinhood.com/options/instruments/e6dec319-3971-4fbb-8f3b-f74ad455b60f/",
  :robinhood.option-date-chain/tradability "tradable",
  :robinhood.option-contract/last-trade-size 3,
  :robinhood.option-contract/high-fill-rate-buy-price 1.43,
  :robinhood.option-date-chain/sellout-datetime
  "2021-03-19T19:00:00+00:00",
  :robinhood.option-contract/adjusted-mark-price 1.38,
  :robinhood.option-contract/bid-price 1.3,
  :robinhood.option-contract/implied-volatility 0.652445,
  :robinhood.option-date-chain/chain-id
  "b0707f5c-839a-44cd-ba9d-a92fb15ab932",
  :robinhood.option-date-chain/url
  "https://api.robinhood.com/options/instruments/e6dec319-3971-4fbb-8f3b-f74ad455b60f/",
  :robinhood.option-date-chain/issue-date "2018-05-15",
  :robinhood.option-date-chain/expiration-date "2021-03-19",
  :robinhood.option-date-chain/created-at
  "2021-01-14T05:30:06.546505Z",
  :robinhood.option-contract/delta 0.62552,
  :robinhood.option-contract/low-fill-rate-sell-price 1.38,
  :robinhood.option-contract/high-fill-rate-sell-price 1.31,
  :robinhood.option-contract/chance-of-profit-short 0.665686,
  :robinhood.option-contract/volume 6,
  :robinhood.option-contract/chance-of-profit-long 0.334314,
  :robinhood.option-date-chain/updated-at
  "2021-01-14T05:30:06.546519Z",
  :robinhood.option-date-chain/chain-symbol "EAF",
  :robinhood.option-contract/theta -0.008594,
  :robinhood.option-date-chain/min-ticks
  #:robinhood.option-date-chain.min-ticks{:above-tick 0.1,
                                          :below-tick 0.05,
                                          :cutoff-price 3.0},
  :robinhood.option-contract/vega 0.016545,
  :robinhood.option-contract/open-interest 1,
  :robinhood.option-contract/ask-price 1.45,
  :robinhood.option-date-chain/strike-price 10.0,
  :robinhood.option-contract/mark-price 1.375,
  :robinhood.option-contract/gamma 0.13309,
  :robinhood.option-contract/last-trade-price 1.35,
  :robinhood.option-date-chain/rhs-tradability "untradable",
  :robinhood.option-contract/low-fill-rate-buy-price 1.36,
  :robinhood.option-contract/rho 0.008959,
  :robinhood.option-contract/previous-close-price 2.13,
  :robinhood.option-contract/ask-size 49,
  :robinhood.option-date-chain/type "call",
  :robinhood.option-contract/previous-close-date "2021-01-14",
  :robinhood.option-date-chain/state "active"})
```

### dev

My current development flow involves reverse engineering 1 robinhood screen at a time via Chrome's devtools and elbow grease. Whatever works in your browser will work dropped directly into a call to `client/get` or `client/post` (see utils.clj).

Steps to add any Robinhood API endpoint;

1. Find the data I am interested in within chrome dev tools (the network tools bar is great for this, especially via the filter bar > `Find all`).
2. Click any name in the list of network requests. (PROTIP: robinhood makes loads of requests; cycle your selection here with up/down arrow keys.)
3. Look at the `Headers` > Scroll to bottom > Open `Request Headers` if its closed.
4. Go ahead and copy/paste this into robinhood.clj/utils.clj for some experimentation
5. If you copied a `GET`, try to get a bare call to [`clj-http.client/get`](https://github.com/dakrone/clj-http#get) to work (use `clj-http.client/post` for a `POST`!)
6. Transform the working `client/get` call block (with static data) into a function by allowing the passing of a parameter in place of each dynamic field.
7. Trace information to its source calls by searching for unique strings/numbers back in the network-tab/filter-bar.
8. If any of the source calls that pull prerequisite data are not yet implemented, then wash/rinse/repeat.

TODO; add to clojars, then add installation & usage instructs
