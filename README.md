Elasticsearch-SQL
=================

Query elasticsearch using familiar SQL syntax.
You can also use ES functions in SQL.


## SETUP

Install as plugin:
TODO

After doing this, you need to restart the Elasticsearch server. Otherwise you may get errors like `Invalid index name [sql], must not start with '']; ","status":400}`.


## Basic Usage

On elasticsearch 1.x / 2.x, visit the elasticsearch-sql web front-end:

````
http://localhost:9200/_plugin/sql/
````

On elasticsearch 5.x/6.x, [download and extract site](https://github.com/NLPchina/elasticsearch-sql/releases/download/5.4.1.0/es-sql-site-standalone.zip).

Then start the web front-end like this:

```shell
cd site-server
npm install express --save
node node-server.js 
```

* Simple query
````
http://localhost:9200/_opendistro/_sql?sql=select * from indexName limit 10
````

* Explain SQL to elasticsearch query DSL
````
http://localhost:9200/_opendistro/_sql/_explain?sql=select * from indexName limit 10
```` 


## SQL Usage

* Query

        SELECT * FROM bank WHERE age >30 AND gender = 'm'

* Aggregation

        select COUNT(*),SUM(age),MIN(age) as m, MAX(age),AVG(age)
        FROM bank GROUP BY gender ORDER BY SUM(age), m DESC

* Delete

        DELETE FROM bank WHERE age >30 AND gender = 'm'


## Beyond SQL

* Search

        SELECT address FROM bank WHERE address = matchQuery('880 Holmes Lane') ORDER BY _score DESC LIMIT 3
        

* Aggregations

	+ range age group 20-25,25-30,30-35,35-40

			SELECT COUNT(age) FROM bank GROUP BY range(age, 20,25,30,35,40)

	+ range date group by day

			SELECT online FROM online GROUP BY date_histogram(field='insert_time','interval'='1d')

	+ range date group by your config

			SELECT online FROM online GROUP BY date_range(field='insert_time','format'='yyyy-MM-dd' ,'2014-08-18','2014-08-17','now-8d','now-7d','now-6d','now')

* ES Geographic
		
		SELECT * FROM locations WHERE GEO_BOUNDING_BOX(fieldname,100.0,1.0,101,0.0)

* Select type

        SELECT * FROM indexName/type


## SQL Features

*  SQL Select
*  SQL Delete
*  SQL Where
*  SQL Order By
*  SQL Group By
*  SQL AND & OR
*  SQL Like
*  SQL COUNT distinct
*  SQL In
*  SQL Between
*  SQL Aliases
*  SQL Not Null
*  SQL(ES) Date
*  SQL avg()
*  SQL count()
*  SQL last()
*  SQL max()
*  SQL min()
*  SQL sum()
*  SQL Nulls
*  SQL isnull()
*  SQL now()
*  SQL floor
*  SQL split
*  SQL trim
*  SQL log
*  SQL log10
*  SQL substring
*  SQL round
*  SQL sqrt
*  SQL concat_ws
*  SQL union and minus

## JDBC Support (Experimental feature)

Check details : [JDBC Support](https://github.com/NLPchina/elasticsearch-sql/pull/283) 

## Beyond sql features

*  ES TopHits
*  ES MISSING
*  ES STATS
*  ES GEO_INTERSECTS
*  ES GEO_BOUNDING_BOX
*  ES GEO_DISTANCE
*  ES GEOHASH_GRID aggregation



