(defproject cochrane_scraper "1.0.0"
  :description "An app to download and export data from the Cochrane library of Systematic reviews"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/data.csv "0.1.2"]
                 [enlive "1.1.5"]
                 [clj-http "0.9.2"]]
  :main ^:skip-aot cochrane-scraper.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
