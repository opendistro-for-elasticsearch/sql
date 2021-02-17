# SQL Engine V2 - Release Notes

---
## 1.Motivations

The current SQL query engine provides users the basic query capability for using familiar SQL rather than complex Elasticsearch DSL. Based on NLPchina ES-SQL, many new features have been added additionally, such as semantic analyzer, semi-structured data query support, Hash Join etc. However, as we looked into more advanced SQL features, challenges started emerging especially in terms of correctness and extensibility (see [Attributions](../attributions.md)). After thoughtful consideration, we decided to develop a new query engine to address all the problems met so far.


---
## 2.What's New

With the architecture and extensibility improved significantly, the following SQL features are able to be introduced in the new query engine:

* **Language Structure**
    * [Identifiers](/docs/user/general/identifiers.rst): added support for identifier names with special characters
    * [Data types](/docs/user/general/datatypes.rst): added support for date and interval types
    * [Expressions](/docs/user/dql/expressions.rst): complex nested expression support
    * [SQL functions](/docs/user/dql/functions.rst): more date function support, `ADDDATE`, `DATE_ADD`, `DATE_SUB`, `DAY`, `DAYNAME`, `DAYOFMONTH`, `DAYOFWEEK`, `DAYOFYEAR`, `FROM_DAYS`, `HOUR`, `MICROSECOND`, `MINUTE`, `QUARTER`, `SECOND`, `SUBDATE`, `TIME`, `TIME_TO_SEC`, `TO_DAYS`, `WEEK`
    * [Comments](/docs/user/general/comments.rst): SQL comment support
* **Basic queries**
    * [HAVING without GROUP BY clause](/docs/user/dql/aggregations.rst#having-without-group-by)
    * [Aggregate over arbitrary expression](/docs/user/dql/aggregations.rst#expression)
    * [Ordering by NULLS FIRST/LAST](/docs/user/dql/basics.rst#example-2-specifying-order-for-null)
    * [Ordering by aggregate function](/docs/user/dql/basics.rst#example-3-ordering-by-aggregate-functions)
* **Complex queries**
    * [Subqueries in FROM clause](/docs/user/dql/complex.rst#example-2-subquery-in-from-clause): support arbitrary nesting level and aggregation
* **Advanced Features**
    * [Window functions](/docs/user/dql/window.rst): ranking and aggregate window functions
    * [Selective aggregation](/docs/user/dql/aggregations.rst#filter-clause): by standard `FILTER` function
* **Beyond SQL**
    * [Semi-structured data query](/docs/user/beyond/partiql.rst#example-2-selecting-deeper-levels): support querying Elasticsearch object fields on arbitrary level
    * Elasticsearch multi-field: handled automatically and users won't have the access, ex. `text` is converted to `text.keyword` if it’s a multi-field

As for correctness, besides full coverage of unit and integration test, we developed a new comparison test framework to ensure correctness by comparing with other databases. Please find more details in [Testing](./Testing.md).


---
## 3.What're Changed

### 3.1 Breaking Changes

Because of implementation changed internally, you can expect Explain output in a different format. For query protocol, there are slightly changes in the default response format:

* **Total**: The `total` field represented how many documents matched in total no matter how many returned (indicated by `size` field). However, this field becomes meaningless because of post processing on DSL response in the new query engine. Thus, for now the total number is always same as size field.

### 3.2 Fallback Mechanism

For these unsupported features, the query will be forwarded to the old query engine by fallback mechanism. To avoid impact on your side, normally you won't see any difference in a query response. If you want to check if and why your query falls back to be handled by old SQL engine, please explain your query and check Elasticsearch log for "Request is falling back to old SQL engine due to ...".

For the following features unsupported in the new engine, the query will be forwarded to the old query engine and thus you cannot use new features listed above:

* **Cursor**: request with `fetch_size` parameter
* **JSON response format**: was used to return ES DSL which is not accessible now. Replaced by default format in the new engine which is also in JSON.
* **Nested field query**: including supports for nested field query
* **JOINs**: including all types of JOIN queries
* **Elasticsearch functions**: fulltext search, metric and bucket functions

### 3.3 Limitations

You can find all the limitations in [Limitations](/docs/user/limitations/limitations.rst). 

### 3.4 What if Something Wrong

No panic! You can roll back to old query engine easily by a plugin setting change. Simply run the command to disable it by [plugin setting](/docs/user/admin/settings.rst#opendistro-sql-engine-new-enabled). Same as other cluster setting change, no need to restart Elasticsearch and the change will take effect on next incoming query. Later on please report the issue to us.


---
## 4.How it's Implemented

If you're interested in the new query engine, please find more details in [Develop Guide](../developing.rst), [Architecture](./Architecture.md) and other docs in the dev folder.


---
## 5.What's Next

As aforementioned, there are still popular SQL features unsupported in the new query engine yet. In particular, the following items are on our roadmap with high priority:

1. Nested field queries
2. JOIN support
3. Elasticsearch functions
