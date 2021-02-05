# An Introduction to the New SQL Query Engine

---
## 1.Motivations

The current SQL query engine provides users the basic query capability for using familiar SQL rather than complex Elasticsearch DSL. Based on NLPchina ES-SQL, many new features have been added additionally, such as semantic analyzer, semi-structured data query support, Hash Join etc. However, as we looked into more advanced SQL features, challenges started emerging especially in terms of correctness and extensibility (see [Attributions](../attributions.md)). After thoughtful consideration, we decided to develop a new query engine to address all the problems met so far.


---
## 2.What's New

With the architecture and extensibility improved significantly, the following SQL features are able to be introduced in the new query engine:

* [Identifiers](/docs/user/general/identifiers.rst): Support for identifier names with special characters
* [Data types](/docs/user/general/datatypes.rst): New data types such as date time and interval
* [Expressions](/docs/user/dql/expressions.rst): New expression system that can represent and evaluate complex expressions
* [SQL functions](/docs/user/dql/functions.rst): Many more string and date functions added
* [Basic queries](/docs/user/dql/basics.rst)
    * Ordering by Aggregate Functions section
    * NULLS FIRST/LAST in section Specifying Order for Null
* [Aggregations](/docs/user/dql/aggregations.rst):
    * Aggregation over expression
    * Selective aggregation by FILTER function
* [Complex queries](/docs/user/dql/complex.rst)
    * Improvement on Subqueries in FROM clause
* [Window functions](/docs/user/dql/window.rst)
    * Ranking window functions
    * Aggregate window functions

As for correctness, besides full coverage of unit and integration test, we developed a new comparison test framework to ensure correctness by comparing with other databases. Please find more details in [Testing](./Testing.md).


---
## 3.What're Changed

### 3.1 Breaking Changes

#### 3.1.1 Response
Because of implementation changed internally, you can expect Explain output in a different format. For query protocol, there are slightly changes on two fields' value in the default response format:

* **Schema**: Previously the `name` and `alias` value differed for different queries. For consistency, name is always the original text now and alias is its alias defined in SELECT clause or absent if none.
* **Total**: The `total` field represented how many documents matched in total no matter how many returned (indicated by `size` field). However, this field becomes meaningless because of post processing on DSL response in the new query engine. Thus, for now the total number is always same as size field.

#### 3.1.2 Syntax and Semantic

* **field.keyword**: In Elasticsearch, user could assign different type for one field. The most common use case is assign `text` and `keyword` type for string field. In the legacy engine, user could select the `field.keyword` from the index. e.g. `SELECT addr.state FROM account WHERE addr.state = 'WA' `. In the new engine, user don't have access the to the `addr.state` field, instead, the query engine will handle it automatically. 


### 3.2 Limitations

You can find all the limitations in [Limitations](/docs/user/limitations/limitations.rst). For these unsupported features, the query will be forwarded to the old query engine by fallback mechanism. To avoid impact on your side, normally you won't see any difference in a query response. If you want to check if and why your query falls back to be handled by old SQL engine, please explain your query and check Elasticsearch log for "Request is falling back to old SQL engine due to ...".

Basically, here is a list of the features common though not supported in the new query engine yet:

* **Cursor**: request with `fetch_size` parameter
* **JSON response format**: will not be supported anymore in the new engine
* **Nested field query**: including supports for object field or nested field query
* **JOINs**: including all types of join queries
* **Elasticsearch functions**: fulltext search, metric and bucket functions

### 3.3 What if Something Wrong

No panic! You can roll back to old query engine easily by a plugin setting change. Simply run the command to disable it by [plugin setting](/docs/user/admin/settings.rst#opendistro-sql-engine-new-enabled). Same as other cluster setting change, no need to restart Elasticsearch and the change will take effect on next incoming query. Later on please report the issue to us.


---
## 4.How it's Implemented

If you're interested in the new query engine, please find more details in [Develop Guide](../developing.rst), [Architecture](./Architecture.md) and other docs in the dev folder.


---
## 5.What's Next

As mentioned in section 3.2 Limitations, there are still very popular SQL features unsupported yet in the new query engine yet. In particular, the following items are on our roadmap with high priority:

1. Object/Nested field queries
2. JOIN support
3. Elasticsearch functions
