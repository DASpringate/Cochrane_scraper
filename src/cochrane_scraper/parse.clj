(ns cochrane-scraper.parse
  (:require [net.cgrand.enlive-html :as html]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [cochrane-scraper.download :as dl]))


(defn comparisons
  "Extracts the comparisons from the  analysis and data section from a parsed Revman file"
  [dat]
  (let [parsed (first (html/select dat [:ANALYSES_AND_DATA]))]
    (if (= "YES" (-> parsed :attrs :calculated_data))
      (html/select parsed [:COMPARISON])
      false)))

(defn match
  "Is the pattern in the tag? Tag can be a keyword "
  [tag pattern]
  ((complement nil?) 
   (re-find (re-pattern pattern) (name tag))))

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

(defn extract 
  "Extracts data from the ANALYSES_AND_DATA section of a Revman file.
   Returns a sequence of maps contaning the data for each comparison,
   outcome, subgroup and data branch."
  [m]
  (let [data-type #(second (re-find #"([A-Z]+)_" (name %)))
        ext 
        (fn ext [m comp outcome subgroup]
          (cond 
           (map? m) (let [{:keys [tag attrs content]} m
                          my-name (-> (html/select m [:NAME])
                                      first :content first)]
                      (cond 
                       (match tag "COMPARISON") (vector (merge 
                                                    {:comparison (inc comp)
                                                     :outcome outcome
                                                     :subgroup subgroup
                                                     :name my-name
                                                     :data_type (data-type tag)}
                                                    attrs) 
                                                   (ext content (inc comp) 
                                                        outcome subgroup))
                       (match tag "OUTCOME") (vector (merge 
                                                      {:comparison comp
                                                       :outcome outcome
                                                       :subgroup subgroup
                                                       :name my-name
                                                       :data_type (data-type tag)}
                                                      attrs 
                                                      (get-labels m))
                                                     (ext content comp
                                                          (inc outcome) subgroup))
                       (match tag "SUBGROUP") (vector (merge 
                                                        {:comparison comp
                                                         :outcome outcome
                                                         :subgroup (inc subgroup)
                                                         :name my-name
                                                      :data_type (data-type tag)}
                                                        attrs)
                                                       (ext content comp
                                                            outcome 
                                                            (inc subgroup)))
                       (match tag "DATA") (merge 
                                           {:comparison comp
                                            :outcome outcome
                                            :subgroup subgroup
                                            :data_type (data-type tag)}
                                           attrs)))
           (seq m) (let [[x & xs] (filter map? m)]
                     (vector (ext x comp outcome subgroup)
                             (ext xs comp outcome subgroup)))
           :else nil))]
        (filter identity (flatten (ext m 0 0 0)))))

(defn revman-data
  "Take a Revman file string, parse it, extract the data and merge in
   missing data fields"
  [revman]
  (if-let [rev-comps (-> (dl/parse-enlive revman) comparisons)]
    (let [rev-data (extract rev-comps)
          rev-blank (zipmap (keys (apply merge rev-data)) (repeat ""))]
      (for [m rev-data] (merge rev-blank m)))
    (println "No data")))
    


(def ^:dynamic rev (revman-data (slurp "/home/david/clojure/cochrane_scraper/data/rm6.rm5")))
(def ^:dynamic *comparisons* 
  (comparisons (dl/parse-enlive (slurp "data/rm6.rm5"))))
(def ^:dynamic *comp* (first *comparisons*))
(def ^:dynamic *seq* (filter map? (:content *comp*)))
(def ^:dynamic *outcome* (second *seq*))
(def ^:dynamic *subgroup* (nth  (filter  map? (-> *outcome* :content)) 5))
(def ^:dynamic *data* (nth (filter map? (-> *subgroup* :content)) 5))
