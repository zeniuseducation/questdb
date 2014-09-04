# QuestDB

QuestDB is a disk-persisted lightweight embedded edn-based nosql
database made for clojure.

WARNING: This is intended for teaching/learning/toying with Clojure,
although we also use for non-critical part of applications.

## Installation

Put this in your leiningen project.clj :dependencies vector

![Clojars Project](http://clojars.org/zenedu.squest/questdb/latest-version.svg)

## Concept & Rationale

Learning the applicability of a language sometime requires a way to be able to do 'real-world' stuffs easily, especially to make the language more playful for beginners without having to do some setups. QuestDB intends to serve the needs for beginners requiring a simple nosql database that can be embedded directly into the project but provides good enough standard CRUDs and basic querying (view in couches' terms).

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

Let's put some data to the db. We can use put-doc! for one doc or
put-docs! for multiple docs.  

The doc in questdb is a simple valid clojure map.

put-doc! receives two arguments, dbname & a map (in which data resided)  

put-docs! receives 2 arguments, dbname & a vector of data maps.

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

### Get doc/docs

To retrieve data, use get-doc for one doc or get-docs for multiple docs.  

get-doc must be called with 2 arguments, dbname and uuid string of the doc.  

get-docs however can be called with one argument dbname and returns all docs in db.  

When calling get-docs with two arguments, the second argument is a list/vector of
uuids intended to be retrieved.  


```clojure

questdb.core> (get-docs db)
=> ({:uuid "aa5057ee-a91c-4529-bed0-1446836c073d",
     :age 35,
     :name "Sam Seaborn",
     :type :person}
 {:uuid "7bd756a6-d2d8-46db-a88f-50f66a65be12",
  :i 1,
  :sqr 1,
  :type :number}
 {:uuid "7ad9d7b8-aad2-4471-9a6e-4b2b90a667a1",
  :i 2,
  :sqr 4,
  :type :number}
 {:uuid "fcf01920-db3b-4e45-972f-3acd053c11c2",
  :i 3,
  :sqr 9,
  :type :number}
 {:uuid "c3af924e-406f-4d48-9b66-3c99eb665349",
  :i 4,
  :sqr 16,
  :type :number})

questdb.core> (get-doc db "c3af924e-406f-4d48-9b66-3c99eb665349",)
=> {:uuid "c3af924e-406f-4d48-9b66-3c99eb665349", :i 4, :sqr 16, :type :number}


questdb.core> (get-docs db ["fcf01920-db3b-4e45-972f-3acd053c11c2"
"c3af924e-406f-4d48-9b66-3c99eb665349",]) 

=> ({:uuid "fcf01920-db3b-4e45-972f-3acd053c11c2", :i 3, :sqr 9, :type :number}
{:uuid "c3af924e-406f-4d48-9b66-3c99eb665349", :i 4, :sqr 16, :type :number})

```

### 'Querying' data

QuestDB provides a simple querying based on matching key-value pair.

Use `(find-doc dbname kv-map)` to retrieve one arbitrary doc that match the kv-pair.

Or use `(find-docs dbname kv-map)` to retrieve all docs that match the kv-map.

```clojure

questdb.core> (find-doc db {:i 2})
=> {:uuid "7ad9d7b8-aad2-4471-9a6e-4b2b90a667a1", :i 2, :sqr 4, :type :number} 

questdb.core> (find-doc db {:type :number}) 
=> {:uuid "7ad9d7b8-aad2-4471-9a6e-4b2b90a667a1", :i 2, :sqr 4, :type :number}

questdb.core> (find-docs db {:type :number}) 
=> ({:uuid "7ad9d7b8-aad2-4471-9a6e-4b2b90a667a1", :i 2, :sqr 4, :type :number}
{:uuid "fcf01920-db3b-4e45-972f-3acd053c11c2", :i 3, :sqr 9, :type :number} 
{:uuid "7bd756a6-d2d8-46db-a88f-50f66a65be12", :i 1, :sqr 1, :type :number} 
{:uuid "c3af924e-406f-4d48-9b66-3c99eb665349", :i 4, :sqr 16, :type :number})

```

Notice the difference between calling find-doc and find-docs even   
if we use the same 'query'.

`find-doc` and `find-docs` can also be called with a simple boolean :and :or  


```clojure

;; To better show these capabilities let's add some more data


questdb.core> (put-docs! db (for [i (range 1 11)] {:number i :even?
			      (even? i) :multiple-of-3 (zero? (rem i
			      3))}))

;; In your REPL, you will see the list of uuids for the newly added docs
;; To see the uuids for all docs you can use (uuids dbname)

;; Let's start querying these data

questdb.core> (find-docs db {:or {:even? true :multiple-of-3 true}})
=> ({:uuid "2cbea7fc-549d-431d-bf66-51a02cf17ad8", :number 2, :even? true, 
:multiple-of-3 false} 
{:uuid "5ded07d0-2589-41b0-b49b-04f9b85491fc", :number 8, :even? true,
:multiple-of-3 false} 
{:uuid "1fd98c14-2741-4506-b8d5-71dbb8d8d0de", :number 3, :even? false, 
:multiple-of-3 true} 
{:uuid "841d174a-1a81-4238-ba83-de32097eed40", :number 4, :even? true,
:multiple-of-3 false} 
{:uuid "4a8addd8-da1e-47b4-861e-c1508e60d546", :number 9, :even? false, 
:multiple-of-3 true} 
{:uuid "c10d6f43-6c6b-4952-b7b9-fad72bde25c5", :number 10, :even? true,
:multiple-of-3 false} 
{:uuid "7ba32673-f01f-4242-b87b-84d1870aa7cb", :number 6, :even? true, 
:multiple-of-3 true})

;; Let's calling it again without the :or 'operator'

questdb.core> (find-docs db {:even? true :multiple-of-3 true}) 
=> ({:uuid "7ba32673-f01f-4242-b87b-84d1870aa7cb", :number 6, :even? true,
:multiple-of-3 true})

;; As you can see here, without :or the query by-default using :and

;; You can also add option 'false' as third argument if you just need   
the uuids not the whole data of the docs


questdb.core> (find-docs db {:or {:even? true :multiple-of-3 true}} false) 
=> #{"2cbea7fc-549d-431d-bf66-51a02cf17ad8"
"5ded07d0-2589-41b0-b49b-04f9b85491fc"
"1fd98c14-2741-4506-b8d5-71dbb8d8d0de"
"841d174a-1a81-4238-ba83-de32097eed40"
"4a8addd8-da1e-47b4-861e-c1508e60d546"
"c10d6f43-6c6b-4952-b7b9-fad72bde25c5"
"7ba32673-f01f-4242-b87b-84d1870aa7cb"}

;; Notice it returns a set instead of a list


``` 

### Updating doc/docs

To update the document, you simply use `put-doc!` or `put-docs!` in almost similar
manner as when you're adding doc/docs. The significant difference here is that
you need to supply the :uuid of the doc/docs.

However, if you supply data with :uuid key, but the key does not exist in db, then
it will add the doc with uuid supplied by the system and replacing the uuid you
supply in the document.

```clojure


questdb.core> (put-doc! db (merge (find-doc db {:number 2}) {:sqr 4}) 
=> {:sqr 4, :uuid "2cbea7fc-549d-431d-bf66-51a02cf17ad8",
    :number 2, :even? true, :multiple-of-3 false}

:: Here we use the :uuid from above doc, but change the :number value to 23
;; The result as you can see is the old-doc merged with the newly supplied data.

questdb.core> (put-doc! db {:number 23 :uuid "2cbea7fc-549d-431d-bf66-51a02cf17ad8"})
=> {:sqr 4, :uuid "2cbea7fc-549d-431d-bf66-51a02cf17ad8", :number 23,
    :even? true, :multiple-of-3 false}

```

### Delete doc/docs

To delete doc/docs, we use `del-doc!!` and `del-docs!!`, notice that we use 2 '!'s.
All 'add' and 'update' functions use one '!' whereas deletion and destroy use 2 '!'s.

Beside deleting the doc/docs both deletion functions also delete the entry in   
indexing files.

```clojure

questdb.core> (del-doc!! db "2cbea7fc-549d-431d-bf66-51a02cf17ad8")
=> true

questdb.core> (get-doc db "2cbea7fc-549d-431d-bf66-51a02cf17ad8") 
=> nil

;; We can also use the data map as long as we supply the :uuid for that doc.

questdb.core> (def datum (get-doc db "7ba32673-f01f-4242-b87b-84d1870aa7cb")) 
=> #<Var@59a1d3ff: 
{:uuid "7ba32673-f01f-4242-b87b-84d1870aa7cb", :number 6, :even? true,
:multiple-of-3 true}>

questdb.core> datum 
=> {:uuid "7ba32673-f01f-4242-b87b-84d1870aa7cb", :number 6, :even? true,
:multiple-of-3 true} 

questdb.core> (del-doc!! db datum) 
=> true

questdb.core> (get-doc db "7ba32673-f01f-4242-b87b-84d1870aa7cb") 
=> nil

```

### Destroying the database

Finally kill the database. You can create as many databases in an application as
long as they have different names.

```clojure

questdb.core> (destroy!! db)
=> true 

```

## Namespace

questdb.core 

Refer to the codox-style docs in the doc directory for further documentation of public vars/functions 

## Notes

QuestDB automatically indexes data according to its key-value pair. It supposes   
to make querying process faster. The indexing process happens incrementally   
each time there's update to the database. Since we assume this db used for   
small sets of data, then we assume performance-wise it's an acceptable behaviour.

## Contributor(s)

Sabda PS (squest)

## License

Copyright © 2014 PT Zenius Education

Distributed under the Eclipse Public License version 1.0.

