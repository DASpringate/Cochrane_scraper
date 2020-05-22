(ns cochrane-scraper.settings)

(def ^:dynamic *cochrane-base* "/doi/10.1002/14651858.CD")
(def ^:dynamic *wiley-base* "http://cochranelibrary-wiley.com")
(def ^:dynamic *first-cols* [:comparison :outcome :subgroup 
                             :name :study_id :data_type :method
                             :effect_measure :random :totals])
(def ^:dynamic *verbose* true)
(def ^:dynamic *logfile* (clojure.string/replace 
                          (str (new java.util.Date) ".log") #"[: ]" "_"))
