(defproject zenedu.squest/questdb "0.2.2"
  :description "A lightweight disk-persisted embedded
  nosql db using edn inspired by couch"
  :url "https://github.com/zeniuseducation/questdb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [me.raynes/fs "1.4.6"]
                 [expectations "2.0.9"]
                 [org.clojure/core.async "0.1.338.0-5c5012-alpha"]]
  :plugins [[codox "0.8.10"]
            [lein-expectations "0.0.8"]
            [lein-autoexpect "1.2.2"]]
  :repositories [["releases" {:url "http://clojars.org/repo"
                              :creds :gpg}]])

