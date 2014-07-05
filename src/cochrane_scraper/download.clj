(ns cochrane-scraper.download
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [cochrane-scraper.io :as writer]
            [cochrane-scraper.parse :as parser]
            [cochrane-scraper.settings :as settings]))


(defn fetch-url 
  "Fetches and parses a webpage, or false if page doesn't exist"
  [url]
  (try 
    (html/html-resource (java.net.URL. url))
    (catch java.io.FileNotFoundException e false)
    (catch java.io.IOException e (println url ": HTTP 500 Server Error"))
    (finally false)))

(defn recent-link 
  "Gets the link to the most recent dataset, or false"
  [url]
  (if-let [page (fetch-url url)]
    (let [action (html/select-nodes* page [(html/attr= :class "action")])
          abstract-replace #(clojure.string/replace % #"abstract" 
                                                    "downloadstats")]
      (if-not (empty? action)
        (abstract-replace (str settings/*wiley-base* 
                               (:href (:attrs (first action)))))
        (abstract-replace url)))
    false))

(defn download-link
  "Returns a string representation of the download link for the review"
  [review-id]
  (let [pub-end 4
        next-pub (fn [id pub]
                   (if (>= pub-end pub)
                     (let [url (str settings/*wiley-base*
                                    settings/*cochrane-base*
                                    (format "%06d" id)
                                    ".pub" pub "/abstract")
                           link (recent-link url)]
                       (or link (recur id (inc pub))))
                     false))]
    (next-pub review-id 2)))

(defn review-data
    "Downloads the data for a cochrane review in .rm format, checking the checkbox"
    [link]
    (try
      (http/post link {:body "string"
                       :body-encoding "UTF-8"
                       :form-params {:tAndCs "on"}})
      (catch Exception e (println "Unknown server error")))) 
  

  (defn download-all-rm5
    "Attempts to download all revman files from a given range of ids
   from start to end. 
   sleep determines how long to wait between downloads (seconds)
   dir is the path to the download directory.
   rm5 and csv are flags for whether or not to download rm5 or csv files"
    [start end sleep dir rm5 csv]
    (let [download-rm5 (fn [i]
                         (if-let [link (download-link i)]
                           (let [rm5-fname (str dir "/rm5/rm" i ".rm5")
                                 csv-fname (str dir "/csv/rm" i ".csv")
                                 rm5-data (review-data link)]
                             (do
                               (if rm5-data
                                 (do
                                   (when rm5
                                     (println "saving " rm5-fname)
                                     (writer/save-rm5 rm5-fname rm5-data))
                                   (when csv
                                     (println "exporting " csv-fname)
                                     (writer/write-csv csv-fname 
                                                       (parser/parse-csv rm5-data) 
                                                       settings/*first-cols*)))
                                 (println "No data found for " link)))
                             (Thread/sleep (* sleep 1000)))
                           (do 
                             (println "File " i " not found")
                             (Thread/sleep (* sleep 1000)))))]
      (do
        (writer/create-dir (str dir "/rm5"))
        (writer/create-dir (str dir "/csv"))
        (dorun (map download-rm5 (range start end))))))

  




;(def *my-link* "http://onlinelibrary.wiley.com/doi/10.1002/14651858.CD000006.pub2/downloadstats")

;(def ^:dynamic *dat* (review-data *my-link*))    
;(def ^dynamic csv-data (parse/revman-data *dat))
;(def ^:dynamic adat (http/post *my-link*
;           {:body "string"
;            :body-encoding "UTF-8"
;            :form-params {:tAndCs "on"}}))

;(def ^:dynamic xdat (parse-xml adat))
;(def ^:dynamic xenlive (pars e-enlive adat))

