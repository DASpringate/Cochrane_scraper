(ns cochrane-scraper.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [cochrane-scraper.download :as cs])
  (:gen-class))

(def cli-options
  [["-d" "--dir DIR" "Output directory "
    :default "output"]
   ["-s" "--start START" "Start index for downloading"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 100000) "Must be a number between 1 and 100000"]]
   ["-e" "--end END" "End index for downloading"
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 100000) "Must be a number between 2 and 100000"]]
   ["-z" "--sleep SLEEP" "Number of seconds to wait between downloads"
    :default 2
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 100000) "Must be a number between 2 and 100000"]]
   ["-h" "--help HELP"
    :default false]])


(defn -main
  "Download Revman files from the cochrane database"
  [& args]
  (let [{:keys [help sleep start end dir]} (:options 
                                            (parse-opts args cli-options))
        summary (:summary (parse-opts args cli-options))]
    (if-not help 
      (do
        (println "Downloading Cochrane IDs from " start " to " end)
        (println "Sleeping for " sleep " seconds between downloads...")
        (cs/download-all-rm5 start end sleep dir))
      (println summary))))
    
