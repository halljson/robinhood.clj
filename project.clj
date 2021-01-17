(defproject org.clojars.halljson/robinhood.clj "1.0.6"
  :description "A clojure wrapper for the Robinhood API"
  :author "hall.alex.json@gmail.com"
  :url "https://github.com/halljson/robinhood.clj"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[clj-http "3.9.1"]
                 [hiccup "1.0.5"]
                 [cheshire "5.8.1" :exclusions [org.clojure/clojure]]
                 [com.rpl/specter "1.1.2"]
                 [org.clojure/data.json "0.2.6" :exclusions [org.clojure/clojure]]]

  :pedantic? :warn
  :plugins [[cider/cider-nrepl "0.21.1"]]

  :source-paths ["src"]
  :profiles
  {:dev {:repl-options {:init-ns user}
         :source-paths ["dev" "test"]
         :dependencies [[lein-autodoc "0.9.0"]
                        [cartridge "1.0.0"]
                        [reloaded.repl "0.2.4"]
                        [expound "0.7.2"]
                        [orchestra "2018.12.06-2"]
                        [metosin/spec-tools "0.8.2"
                         :exclusions [com.fasterxml.jackson.core/jackson-core]]
                        [org.clojure/clojure "1.9.0"]]}})
