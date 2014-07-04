(ns cochrane-scraper.io
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn in-coll? 
  "Is x in the collection?"
  [coll]
  (fn [x]
    (some #(= x %) coll)))

(defn create-dir
  "creates a directory at the given path"
  [dir-name]
  (.mkdir (java.io.File. dir-name)))

(defn save-rm5
  "Saves an rm5 file to disk"
  [file rm5]
   (spit file (:body rm5)))

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

;(def ^:dynamic rev-data  (csp/revman-data (slurp "data/rm5/rm4.rm5")))
;(write-csv "data/rev-data.csv" rev-data 
;           [:comparison :outcome :subgroup :name :study_id :data_type :method
;            :effect_measure :random :totals])
