# QuestDB

QuestDB is a disk-persisted lightweight embedded edn-based nosql
database.

WARNING: This is a learning project for now, many of the intended features have not been implemented yet.

## Installation

Put this in your leiningen project.clj :dependencies vector

![Clojars Project](http://clojars.org/zenedu.squest/questdb/latest-version.svg)

## Concept & Rationale

Learning the applicability of a language sometime requires an easy start-up way to be able to do 'real-world' stuffs, especially to make the language more playful for beginners without having to do some setups. QuestDB intends to serve the needs for beginners requiring a simple nosql database that can be embedded directly into the project but provides good enough standard CRUDs and basic querying (view in couches' terms).

## Usage 

### Create database

```clojure

;; in REPL

(use 'questdb.core)

;; in ns form 

(require [questdb.core :refer :all])

questdb.core> (def db "hellodb") 
=> #<Var@74c806dc: "hellodb">
questdb.core> (create! db) 
=> {:status true, :message "hellodb has been created!"} 
questdb.core> (db-exists? db) 
=> true

```

### Add doc/docs

Let's put some data to the db. You can use put-doc! for one doc or
put-docs! for multiple docs.

```clojure

questdb.core> (put-doc! db {:name "Sam Seaborn" :age 35 :type :person}) 
=> {:uuid "aa5057ee-a91c-4529-bed0-1446836c073d", :age 35,
    :name "Sam Seaborn", :type :person}

questdb.core> (put-docs! db (for [i (range 1 5)] 
	      		    	 {:i i :sqr (* i i) :type :number} 
=> ("7bd756a6-d2d8-46db-a88f-50f66a65be12"
    "7ad9d7b8-aad2-4471-9a6e-4b2b90a667a1"
    "fcf01920-db3b-4e45-972f-3acd053c11c2"
    "c3af924e-406f-4d48-9b66-3c99eb665349")

```

## Namespace

questdb.core 

Refer to the codox-style docs in the doc directory for further documentation of public vars/functions 

## Contributor(s)

Sabda PS (squest)

## License

Copyright © 2014 PT Zenius Education

Distributed under the Eclipse Public License either version 1.0 
same as clojure.
