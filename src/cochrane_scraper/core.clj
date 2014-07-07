(ns cochrane-scraper.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [cochrane-scraper.download :as dl]
            [cochrane-scraper.settings :as settings]
            [cochrane-scraper.io :as io])
  (:gen-class))

(def cli-options
  [["-d" "--dir DIR" "Output directory "
    :default "output"]
   ["-s" "--start START" "Start index for downloading"
    :default 1
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 100000) "Must be a number between 1 and 100000"]]
   ["-e" "--end END" "End index for downloading"
    :default 10800
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 100000) "Must be a number between 2 and 100000"]]
   ["-z" "--sleep SLEEP" "Number of seconds to wait between downloads"
    :default 2
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 1 % 100000) "Must be a number between 2 and 100000"]]
   ["-h" "--help" "Show these options"
    :default false]
   ["-c" "--csv" "Output review data in csv format"
    :default false]
   ["-r" "--rm5" "Output review data in rm5 (Revman) format"
    :default false]
   ["-m" "--minimal" "Produce minimal output during runs (All run info stored in logfile)"
    :default false]
   ["-l" "--logfile LOGFILE" "logfile name"
    :default false]])


(defn -main
  "Download Revman files from the cochrane database"
  [& args]
  (let [cmd-options (parse-opts args cli-options)
        {:keys [help sleep start end dir csv rm5 minimal logfile]} (:options cmd-options) 
        summary (:summary cmd-options)]
    (if-not (or help (not (or csv rm5))) 
      (do
        (when minimal (alter-var-root 
                           (var settings/*verbose*) 
                           (fn [x] false)))
        (when logfile (alter-var-root 
                           (var settings/*logfile*) 
                           (fn [x] logfile)))
        (io/logprint "Downloading Cochrane IDs from " start " to " end)
        (io/logprint "Sleeping for " 
                 sleep " seconds between downloads...")
        (cond 
         (and csv rm5) (io/logprint "Saving Rm5 files to " dir "/rm5"  
                                " and exporting csv files to " dir "/csv")
         csv (io/logprint "Exporting csv files to " dir "/csv")
         rm5 (io/logprint "Exporting rm5 to " dir "/rm5"))
        (dl/download-all-rm5 start end sleep dir rm5 csv))
      (do (println summary)
          (println "You must specify output as one of 
                    --csv
                    --rm5
                    --csv --rm5")))))
    
