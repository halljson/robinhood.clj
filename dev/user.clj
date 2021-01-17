(ns user
  (:require [robinhood.clj.spec]
            [robinhood.clj.client]
            [expound.alpha :as expound]
            [clojure.spec.alpha :as s]
            [orchestra.spec.test :as stest]
            [clojure.tools.namespace.repl
             :refer [refresh set-refresh-dirs]]
            [reloaded.repl
             :refer [go init reset-all start stop system]]))

(set-refresh-dirs "dev/" "src/" "test/")

(alter-var-root #'s/*explain-out* (constantly expound/printer))

#_(stest/unstrument)

(stest/instrument)

(defn reset []
  (refresh)
  (stest/instrument))

#_(reset)
