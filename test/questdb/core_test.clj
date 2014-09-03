(ns questdb.core-test
  (:require [questdb.core :refer :all]
            [expectations :refer :all]
            [clojure.java.io :as io]))


(def db "testdb")

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
            (put-doc! db (assoc data :brigade "xarxar")))

(defn num-uuids
  []
  (put-docs! db [{:n 24 :rec-type :number "factors" [1 2 3 4 6 8 12 24]}
                 {:n 100 :rec-type :number "factors" [1 2 4 5 10 20 25 50 100]}]))

(expect (uuids db)
        (map :uuid (get-docs db)))

(expect (into #{} (num-uuids))
        (clojure.set/difference (into #{} (uuids db))
                                (into #{} @people)))

(expect true
        (destroy!! db))

























