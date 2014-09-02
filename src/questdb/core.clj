(ns questdb.core
  (:require [clojure.java.io :as io]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def ^:private dir "resources/questdb/")
(def ^:private data-dir "data/")
(def ^:private query-dir "query/")

(defn- $uuids
  [dbname]
  (str dir dbname "/uuids.edn"))

(defn- uuids
  [dbname]
  (read-string (slurp ($uuids dbname))))

(defn- add-uuid!
  [dbname uuid]
  (let [old-data (uuids dbname)
        new-data (conj old-data uuid)]
    (spit ($uuids dbname) new-data)))

(defn- mkdir!
  [fname]
  (.mkdirs (io/as-file fname)))

(defn- id-path
  [dbname uuid]
  (str dir dbname "/" data-dir uuid ".edn"))

(defn create!
  "Creates a new database with dbname and make initial directories"
  [dbname]
  (if (.exists (io/as-file dir))
    (if (.exists (io/as-file (str dir dbname "/")))
      {:status false :message "DB already exists in project"}
      (do (doseq [a [(str dir dbname "/")
                     (str dir dbname "/data/")
                     (str dir dbname "/query/")]]
            (mkdir! a))
          (spit ($uuids dbname)
                [])
          {:status true :message (str dbname " has been created!")}))
    (do (doseq [a [(str dir dbname "/")
                   (str dir dbname "/data/")
                   (str dir dbname "/query/")]]
          (mkdir! a))
        (spit ($uuids dbname)
              [])
        {:status true :message (str dbname " has been created")})))

(defn put-doc!
  "Put data into dbname"
  [dbname data]
  (let [uuid (uuid)
        fname (id-path dbname uuid)
        final (assoc data :uuid uuid)]
    (do (spit fname final)
        (add-uuid! dbname uuid)
        final)))

(defn put-docs!
  "Put multiple docs in a vector of data to dbname, data must be a seq"
  [dbname data]
  (doseq [datum data]
    (put-doc! dbname datum)))

(defn get-doc
  "Get doc from dbname, with two arguments it returns the doc in
  dbname with a specified uuid , with three arguments it returns the
  first doc in dbname with a match KV pair."
  ([dbname uuid]
     (let [fname (id-path dbname uuid)]
       (read-string (slurp fname))))
  ([dbname uuid col]
     nil))

(defn get-docs
  "With one argument it returns all docs in dbname, with two arguments
  it returns all docs in dbname with uuids supplied (which must
  be a vector of uuid strings"
  ([dbname]
     (for [uuid (uuids dbname)]
       (get-doc dbname uuid)))
  ([dbname uuids]
     (for [uuid uuids]
       (get-doc dbname uuid))))









