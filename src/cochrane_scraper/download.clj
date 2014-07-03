(ns cochrane-scraper.download
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clj-http.client :as http]
            [clojure.xml :as xml]
            [clojure.zip :as zip]))


; [x] get url
; [x] check to pub[x]
; [x] check for latest version
; [x] get download link
; [x] tick checkbox
; [x] download rm file

(def ^:dynamic *cochrane-base* "/doi/10.1002/14651858.CD")
(def ^:dynamic *wiley-base* "http://onlinelibrary.wiley.com")

(defn fetch-url 
  "Fetches and parses a webpage, or false if page doesn't exist"
  [url]
  (try 
    (html/html-resource (java.net.URL. url))
    (catch java.io.FileNotFoundException e false)))

(defn recent-link 
  "Gets the link to the most recent dataset, or false"
  [url]
  (if-let [page (fetch-url url)]
    (let [action (html/select-nodes* page [(html/attr= :class "action")])
          abstract-replace #(clojure.string/replace % #"abstract" 
                                                    "downloadstats")]
      (if-not (empty? action)
        (abstract-replace (str *wiley-base* 
                               (:href (:attrs (first action)))))
        (abstract-replace url)))
    false))

(defn download-link
  "Returns a string representation of the download link for the review"
  [review-id]
  (let [pub-end 4
        next-pub (fn [id pub]
                   (if (>= pub-end pub)
                     (let [url (str *wiley-base*
                                    *cochrane-base*
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
    (catch Exception e false)))
  
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
  

(defn save-rm5
  "Saves an rm5 file to disk"
  [f rm5]
  (if rm5 
    (spit f (:body rm5))
    (println "Data not available for " f)))
  
(defn download-all-rm5
  "Attempts to download all revman files in a given range of ids"
  [start end sleeper dir]
  (let [download-rm5 (fn [i]
                       (let [fname (str dir "/rm" i ".rm5")
                             link (download-link i)]
                         (if link (do 
                                    (println "saving " fname)
                                    (save-rm5 fname (review-data link))
                                    (Thread/sleep (* sleeper 1000)))
                             (do 
                               (println "File " i " not found")
                               (Thread/sleep (* sleeper 1000))))))]
    (dorun (map download-rm5 (range start end)))))



;(def *my-link* "http://onlinelibrary.wiley.com/doi/10.1002/14651858.CD000005.pub2/downloadstats")

;(def *dat* (review-data *my-link*))    

;(def ^:dynamic adat (http/post *my-link*
;           {:body "string"
;            :body-encoding "UTF-8"
;            :form-params {:tAndCs "on"}}))

;(def ^:dynamic xdat (parse-xml adat))

;(def ^:dynamic xenlive (pars e-enlive adat))

