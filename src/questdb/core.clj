(ns questdb.core
  (:require [clojure.java.io :as io]
            [clojure.core.async :refer [go]]
            [me.raynes.fs :as fs]
            [clojure.set :as cs]))

(defn uuid
  "A useful function to generate a random uuid."
  []
  (str (java.util.UUID/randomUUID)))

(def ^:private dir "resources/questdb/")
(def ^:private data-dir "data/")
(def ^:private index-dir "index/")
(def ^:private query-dir "query/")

(defn- $uuids
  "Simeple thing"
  [dbname]
  (str dir dbname "/uuids.edn"))

(defn uuids
  "Returns a vector of uuids of docs exist in database"
  [dbname]
  (let [res (try (read-string (slurp ($uuids dbname)))
                 (catch Exception e))]
    res))

(defn- add-uuid!
  [dbname uuid]
  (let [old-data (uuids dbname)
        new-data (conj old-data uuid)]
    (spit ($uuids dbname) new-data)))

(defn- mkdir!
  [fname]
  (.mkdirs (io/as-file fname)))

(defn- db-path
  [dbname]
  (str dir dbname "/"))

(defn- id-path
  [dbname uuid]
  (str dir dbname "/" data-dir uuid ".edn"))

(defn- meta-index
  [dbname]
  (str (db-path dbname) "index.edn"))

(defn- update-meta
  [dbname data]
  (spit (meta-index dbname) data))

(defn- index-path
  [dbname dbkey]
  (let [base (str (db-path dbname) index-dir)
        fdata (read-string (slurp (meta-index dbname)))]
    (if-let [uuid-key (get fdata dbkey)]
      (str base uuid-key ".edn")
      (let [id (uuid)]
        (do (update-meta dbname
                         (assoc fdata dbkey id))
            (str base id ".edn"))))))

(defn- add-index!
  "Create an index file for a certain key"
  [dbname dbkey]
  (if (.exists (io/as-file (index-path dbname dbkey)))
    false
    (spit (index-path dbname dbkey)
          {})))

(defn- read-index-file
  [dbname dbkey]
  (if (.exists (io/as-file (index-path dbname dbkey)))
    (read-string (slurp (index-path dbname dbkey)))
    nil))

(defn- write-index-key!
  "Write an index file with a given set of data"
  [dbname dbkey datum]
  (do (add-index! dbname dbkey)
      (let [fdata (read-index-file dbname dbkey)]
        (if-let [key-data (->> (get datum dbkey)
                               (get fdata))]
          (let [new-key-data (conj key-data
                                   (:uuid datum))]
            (spit (index-path dbname dbkey)
                  (merge fdata
                         {(get datum dbkey) new-key-data})))
          (spit (index-path dbname dbkey)
                (merge fdata
                       {(get datum dbkey) #{(:uuid datum)}}))))))

(defn- write-index-keys!
  "Write all keys in datum to index-key files"
  [dbname datum]
  (doseq [datum-key (keys (dissoc datum :uuid))]
    (write-index-key! dbname datum-key datum)))

(defn db-exists?
  "Returns true if a certain dbname exists, and false otherwise"
  [dbname]
  (.exists (io/as-file (str dir dbname "/"))))

(defn create!
  "Creates a new database with dbname and make initial directories and
  files. Example usage (create! \"mydb\")"
  [dbname]
  (if (.exists (io/as-file dir))
    (if (.exists (io/as-file (str dir dbname "/")))
      {:status false :message "DB already exists in project"}
      (do (doseq [a [(str dir dbname "/")
                     (str dir dbname "/data/")
                     (str dir dbname "/query/")
                     (str dir dbname "/" index-dir)]]
            (mkdir! a))
          (do (spit ($uuids dbname)
                    [])
              (spit (meta-index dbname)
                    {}))
          {:status true :message (str dbname " has been created!")}))
    (do (doseq [a [(str dir dbname "/")
                   (str dir dbname "/data/")
                   (str dir dbname "/query/")
                   (str dir dbname "/" index-dir)]]
          (mkdir! a))
        (do (spit (meta-index dbname)
                  {})
            (spit ($uuids dbname)
                  []))
        {:status true :message (str dbname " has been created")})))

(declare get-docs find-docs)

(defn get-doc
  "Get one doc from dbname, returns the doc in
  dbname with a specified uuid."
  ([dbname uuid]
     (let [fname (id-path dbname uuid)]
       (let [res-doc (try (read-string (slurp fname))
                          (catch Exception e))]
         res-doc))))

(defn find-doc
  "Returns the first doc in dbname which match the supplied key-value
  in the kv map. Please refer to find-docs, the difference is only
  that find-doc returns one arbitrary element from find-docs."
  [dbname kv]
  (first (find-docs dbname kv)))

(defn get-docs
  "When invoked with one argument it returns all docs in dbname. When
  invoked with two arguments it returns all docs in dbname with uuids
  supplied (which must be a vector of uuid strings). Returns a list of
  uuids string or doc maps."
  ([dbname]
     (for [uuid (uuids dbname)]
       (get-doc dbname uuid)))
  ([dbname uuids]
     (for [uuid uuids]
       (get-doc dbname uuid))))

(defn- match-index
  [dbname kv]
  (cond (some #(= :or %) (keys kv))
        (apply cs/union
               (map #(-> (read-index-file dbname (key %))
                         (get (val %)))
                    (:or kv)))
        (some #(= :and %) (keys kv))
        (apply cs/intersection
               (map #(-> (read-index-file dbname (key %))
                         (get (val %)))
                    (:and kv)))
        :else
        (apply cs/intersection
               (map #(-> (read-index-file dbname (key %))
                         (get (val %)))
                    kv))))

(defn find-docs
  "Returns uuids or docs in dbname which match the kv pair supplied.
  kv is a clojure map. Option is expected to be a boolean, when set to
  true or blank than this function returns all docs that match kv, if
  set to false than returns only the uuids. Usage examples: (find-docs
  db {:n 123}) (find-docs db {:n 123} false). (find-docs db {:or {:n
  123 :t 32}). The default boolean operator is :and, if you want to
  find with 'or' than simply use :or keyword for the 'query'."
  [dbname kv & option]
  (let [uuids (match-index dbname kv)]
    (if (= false (first option))
      uuids
      (get-docs dbname uuids))))

(defn put-doc!
  "Put data into dbname, data must be a clojure map. However the
  values inside the data can be any valid clojure collection. Usage
  examples (put-doc! db {:number 24 :factors [1 2 3 4 6 8 12 24]}. If
  the supplied data contains a uuid that exists in database, then data
  will be merged into existing entry instead. Index-keys is a set of
  keys you want this data to be indexed with for future querying
  purpose. Usage example (put-doc! db {:rec-type :number :n 123 :p 23}
  #{:n :rec-type}. You can also supply false as third argument to tell
  the function not to index any key in the data."
  [dbname data & index-keys]
  (if (and (:uuid data)
           (.exists (io/as-file (id-path dbname (:uuid data)))))
    (let [uuid (:uuid data)
          fname (id-path dbname uuid)
          old-data (get-doc dbname uuid)
          final (merge old-data data)]
      (do (cond (coll? (first index-keys))
                (map #(write-index-key! dbname % final)
                     index-keys)
                (= false (first index-keys))
                nil
                :else
                (write-index-keys! dbname final))
          (spit fname final)
          final))
    (let [uuid (uuid)
          fname (id-path dbname uuid)
          final (assoc data :uuid uuid)]
      (do (add-uuid! dbname uuid)
          (cond (coll? (first index-keys))
                (map #(write-index-key! dbname % final)
                     index-keys)
                (= false (first index-keys))
                nil
                :else
                (write-index-keys! dbname final))
          (spit fname final)
          final)))) 

(defn put-docs!
  "Put multiple docs in a vector of data to dbname, data must be a
  collection of maps. Usage example (put-docs! db [{:n 100 :nfactors
  9} {:n 24 :nfactors 8}]. Returns the list of uuids of the added data."
  [dbname data & index-keys]
  (if-let [idx (first index-keys)]
    (for [datum data]
      (:uuid (put-doc! dbname datum idx)))
    (for [datum data]
      (:uuid (put-doc! dbname datum)))))

(defn- del-uuid!!
  [dbname uuid]
  (let [old-uuids (uuids dbname)
        final (vec (remove #(= uuid %)
                       old-uuids))]
    (spit ($uuids dbname) final)))

(defn- remove-index!!
  [dbname kv uuid]
  (let [[dbkey dbval] (first kv)
        fdata (read-index-file dbname dbkey)
        kdata (get fdata dbval)
        final (into #{} (remove #(= % uuid) kdata))]
    (spit (index-path dbname dbkey)
          (merge fdata
                 {dbval final}))))

(defn- del-index!!
  [dbname datum]
  (let [uuid (:uuid datum)]
    (doseq [kv (dissoc datum :uuid)]
      (remove-index!! dbname {(key kv) (val kv)} uuid))))

(defn- del-file!!
  [dbname uuid]
  (io/delete-file (id-path dbname uuid)))

(defn del-doc!!
  "Delete the doc with supplied doc or uuid, if supplying doc, it must
  contain :uuid."
  [dbname doc-or-uuid]
  (if-let [uuid (:uuid doc-or-uuid)]
    (let [old-data (get-doc dbname uuid)]
      (do (del-uuid!! dbname uuid)
          (del-index!! dbname old-data)
          (del-file!! dbname uuid)))
    (let [old-data (get-doc dbname doc-or-uuid)]
      (do (del-uuid!! dbname doc-or-uuid)
          (del-index!! dbname old-data)
          (del-file!! dbname doc-or-uuid)))))

(defn del-docs!!
  "Delete multiple docs given in the list of docs or uuids"
  [dbname docs-or-uuids]
  (doseq [datum docs-or-uuids]
    (del-doc!! dbname datum)))

(defn destroy!!
  "Destroy the database including all data in the dbname."
  [dbname]
  (fs/delete-dir (str dir dbname "/")))















