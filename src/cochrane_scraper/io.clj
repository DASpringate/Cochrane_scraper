(ns cochrane-scraper.io
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [cochrane-scraper.parse :as csp]))

(defn in-coll? 
  "Is x in the collection?"
  [coll]
  (fn [x]
    (some #(= x %) coll)))

(defn write-csv
  "Take a sequence of maps and write as a csv.
   The first-columns arg is used to move whichever 
   columns you want to the front"
  [path rev-data first-columns]
  (let [columns (concat first-columns
                        (vec (remove (in-coll? first-columns)  
                                (keys (first rev-data)))))
        headers (map name columns)
        rows (mapv #(mapv % columns) rev-data)]
    (with-open [file (io/writer path)]
      (csv/write-csv file (cons headers rows)))))

;(def ^:dynamic rev-data  (csp/revman-data (slurp "data/rm6.rm5")))
;(write-csv "data/rm6.csv" (csp/revman-data (slurp "data/rm6.rm5")) 
;           [:comparison :outcome :subgroup :name :study_id :data_type :method
;            :effect_measure :random :totals])
