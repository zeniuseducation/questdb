(ns questdb.core-test
  (:require [questdb.core :refer :all]
            [expectations :refer :all]
            [clojure.java.io :as io]))

(def db "testdb")
(def ^:private dir "resources/questdb/")
(def ^:private data-dir "data/")
(def ^:private index-dir "index/")
(def ^:private query-dir "query/")
(def people (atom []))

(expect true
        (:status (create! db)))

(expect  true
         (and (.exists (io/as-file (str dir db "/")))
              (.exists (io/as-file (str dir db "/" index-dir)))
              (.exists (io/as-file (str dir db "/" data-dir)))
              (.exists (io/as-file (str dir db "/" query-dir)))))

(expect []
        (uuids db))

(expect {:status false}
        (dissoc (create! db)
                :message))

(expect '()
        (get-docs "helloworld"))

(expect nil
        (get-doc db "clojure is super kewl!"))

(def person {:name "blangsakan"
             :rec-type :person
             :live-motto "I'm Brian and so is my wife!"
             :age 34
             :wives ["brigida" "joyous"]})

(expect person
        (let [added-person (put-doc! db person)]
          (do (swap! people conj (:uuid added-person))
              (dissoc added-person :uuid))))

(expect (more coll? not-empty)
        (let [added-person (put-doc! db person)]
          (do (swap! people conj (:uuid added-person))
              added-person)))

(def pdata {:name "Sir Sam Seaborn"
            :rec-type :person
            :status "The one that bravely chickening out"
            :varian {:species "Xandarese" :addresses ["Xandar" "Tortor"]}})

(defn p1-data
  []
  (let [data (put-doc! db pdata)]
    (do (swap! people conj (:uuid data))
        data)))

(expect #{:name :rec-type :status :varian :uuid}
        (into #{} (keys (p1-data))))

(expect {:rec-type :person}
        (in (p1-data)))

(expect-let [data (p1-data)]
            data
            (get-doc db (:uuid data)))

(expect @people
        (uuids db))

(expect (for [uuid (uuids db)]
          (get-doc db uuid))
        (get-docs db))

(expect-let [data (p1-data)]
            (assoc data :brigade "xarxar")
            (do (let [this (put-doc! db (assoc data :brigade "xarxar"))]
                  (Thread/sleep 500)
                  this)))

(defn num-uuids
  []
  (put-docs! db [{:n 24 :rec-type :number "factors" [1 2 3 4 6 8 12 24]}
                 {:n 100 :rec-type :number "factors" [1 2 4 5 10 20 25 50 100]}]))

(expect (uuids db)
        (map :uuid (get-docs db)))

(expect (into #{} (num-uuids))
        (clojure.set/difference (into #{} (uuids db))
                                (into #{} @people)))

(defn prime?
  [n]
  (if (some #(= % n) [2 3 5 7 11 13 17 19 23 29 31 37 41 43 47])
    true
    false))

(defn factors
  [p]
  (filter #(zero? (rem p %))
          (range 1 (inc p))))

(def numbers (for [a (range 1 21)]
               {:n a
                :sqr (* a a)
                :factors (factors a)
                :prime? (prime? a)
                :type :number}))

(expect 20
        (count (put-docs! db numbers)))

(expect (into #{} numbers)
        (into #{} (map #(dissoc % :uuid)
                       (find-docs db {:type :number}))))

(expect-let [x (del-doc!! db (find-doc db {:prime? true}))]
            19
            (count (find-docs db {:type :number})))

(expect-let [x (del-docs!! db (find-docs db {:prime? true}))]
            0
            (count (find-docs db {:prime? true})))

(expect true
        (db-exists? db))

(expect false
        (db-exists? "helloworld"))

(expect 5
        (count (put-docs! db [{:type :animal :class "mamalia" :species "chimps"}
                              {:type :animal :class "pisces" :species "jojoba"}
                              {:type :animal :class "reptile" :species "crocs"}
                              {:type :animal :class "mamalia" :species "dolphins"}
                              {:type :animal :class "reptile" :species "iguana"}])))

(expect 2
        (count (find-docs db {:class "mamalia"})))

(expect #{{:type :animal :class "pisces" :species "jojoba"}
          {:type :animal :class "reptile" :species "crocs"}
          {:type :animal :class "reptile" :species "iguana"}}
        (into #{} (map #(dissoc % :uuid)
                       (find-docs db {:or {:class "reptile"
                                           :species "jojoba"}}))))

;; This wouldn't delete anything because the default boolean operator
;; in query is :and
(expect-let [x (del-docs!! db (find-docs db {:class "reptile" :species "dolphins"}))]
            5
            (count (find-docs db {:type :animal})))

(expect-let [x (del-docs!! db (find-docs db {:class "reptile"}))]
            3
            (count (find-docs db {:type :animal})))


;; Test with quite a lot of data in a sequence
(expect-let [this (put-docs! db (for [a (range 1 101)
                                      b ["jojon" "let" "this" "become" "briliant"]]
                                  {:i a :nama b :type :asal}))]
            [500 500]
            [(count this)
             (count (find-docs db {:type :asal}))])

(expect true
        (destroy!! db))

























