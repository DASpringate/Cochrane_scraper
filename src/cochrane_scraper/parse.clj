(ns cochrane-scraper.parse
  (:require [net.cgrand.enlive-html :as html]
            [clojure.xml :as xml]
            [clojure.zip :as zip]))

(defn zip-str 
  "Parses an xml string to clojure data structures"
  [s]
  (zip/xml-zip (xml/parse (java.io.ByteArrayInputStream. (.getBytes s)))))

(defn parse-xml
  "Parse data from Cochrane data Revman string using zip and xml"
  [dat]
  (if (map? dat)
    (zip-str (:body dat))
    (zip-str dat)))
  

(defn parse-enlive
  "Parse data from Cochrane data Revman string using enlive"
  [dat]
  (if (map? dat)
    (html/html-snippet (:body dat))
    (html/html-snippet dat)))
  
(defn match-tag
  "Is the pattern in the tag keyword of the map?"
  [m pattern]
  ((complement nil?) 
   (re-find (re-pattern pattern) (name (:tag m)))))

(defn keywords->lower
  "Convert a sequence of keywords to lower case"
  [kwds]
  (map #(-> % name clojure.string/lower-case keyword) kwds))

(defn get-labels
  "Extract names for the group labels in outcomes"
  [outcome]
  (let [keywords [:GROUP_LABEL_1 :GROUP_LABEL_2
                 :GRAPH_LABEL_1 :GRAPH_LABEL_2
                 :EFFECT_MEASURE]
        un-nil (fn [x] (or x ""))]
    (zipmap 
     (keywords->lower keywords)
     (for [k keywords] (-> (html/select outcome [k]) 
                           first :content first un-nil)))))

(defn data-type
  "Gets the type of data indicated in the name tag"
  [x]
  (second (re-find #"([A-Z]+)_" (name x))))

(defn extract-data-data
  [data comp-num outcome-num subgroup-num data-num]
  (let [{:keys [tag attrs content]} data]
        (merge 
          {:comparison comp-num
           :outcome outcome-num
           :subgroup subgroup-num
           :study data-num
           :data_type (data-type tag)}
          attrs)))

(defn extract-subgroup-data
  [subgroup comp-num outcome-num subgroup-num]
  (let [{:keys [tag attrs content]} subgroup
        my-name (-> (html/select subgroup [:NAME])
                    first :content first)]
    (flatten (concat [(merge 
              {:comparison comp-num
               :outcome outcome-num
               :subgroup subgroup-num
               :name my-name
               :data_type (data-type tag)}
              attrs)]
            (map extract-data-data 
                 (filter #(and (map? %) (match-tag % "DATA")) 
                         content)
                 (repeat comp-num)
                 (repeat outcome-num)
                 (repeat subgroup-num)
                 (iterate inc 1))))))


(defn extract-outcome-data
  [outcome comp-num outcome-num]
  (let [{:keys [tag attrs content]} outcome
        my-name (-> (html/select outcome [:NAME])
                    first :content first)]
        (flatten (concat [(merge 
                  {:comparison comp-num
                   :outcome outcome-num
                   :subgroup 0
                   :name my-name
                   :data_type (data-type tag)}
                  attrs 
                  (get-labels outcome))]
                (map extract-subgroup-data 
                     (filter #(and (map? %) (match-tag % "SUBGROUP")) 
                             content)
                     (repeat comp-num)
                     (repeat outcome-num)
                     (iterate inc 1))
                (map extract-data-data 
                     (filter #(and (map? %) (match-tag % "DATA")) 
                             content)
                     (repeat comp-num)
                     (repeat outcome-num)
                     (repeat 0) ; if data not in subgroup
                     (iterate inc 1))))))

(defn extract-comparison-data
  [comparison comp-num]
  (let [{:keys [tag attrs content]} comparison
        my-name (-> (html/select comparison [:NAME])
                    first :content first)]
    (flatten (concat [(merge 
             {:comparison comp-num
              :outcome 0
              :subgroup 0
              :name my-name}
              attrs)]
            (map extract-outcome-data 
                 (filter #(and (map? %) (match-tag % "OUTCOME")) 
                         content)
                 (repeat comp-num)
                 (iterate inc 1))))))

(defn extract-all
  "Extract data for all comparisons, outcomes, subgroups and data"
  [comparisons]
  (flatten (map extract-comparison-data comparisons (iterate inc 1))))

(defn comparisons
  "Extracts the comparisons from the  analysis and data section
   from a parsed Revman file"
  [dat]
  (let [parsed (first (html/select dat [:ANALYSES_AND_DATA]))]
    (if (= "YES" (-> parsed :attrs :calculated_data))
      (html/select parsed [:COMPARISON])
      false)))

(defn parse-csv
  "Take a Revman file string, parse it, extract the data and merge in
   missing data fields"
  [revman]
  (if-let [rev-comps (-> (parse-enlive revman) comparisons)]
    (let [rev-data (extract-all rev-comps)
          rev-blank (zipmap (keys (apply merge rev-data)) (repeat ""))]
      (for [m rev-data] (merge rev-blank m)))
    (println "No data")))
    
(defn get-section
  [d tag]
  (html/select d [tag]))

;(def ^:dynamic rev (revman-data (slurp "/home/david/clojure/cochrane_scraper/data/rm6.rm5")))
;(def ^:dynamic comps 
;  (comparisons (parse-enlive (slurp "data/rm6.rm5"))))
;(def ^:dynamic comp (second comps))

;(def ^:dynamic *seq* (filter map? (:content *comp*)))
;(def ^:dynamic outcomes (extract-outcome-data comp 0 0))
;(def ^:dynamic *subgroup* (nth  (filter  map? (-> *outcome* :content)) 5))
;(def ^:dynamic *data* (nth (filter map? (-> *subgroup* :content)) 5))
