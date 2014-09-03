(defproject zenedu.squest/questdb "0.1.12-SNAPSHOT"
  :description "A lightweight implementation of file-persisted couch-like embedded
  nosql database using edn only data structure"
  :url "https://github.com/squest/questdb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [expectations "2.0.9"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :plugins [[codox "0.8.10"]
            [lein-expectations "0.0.8"]])

