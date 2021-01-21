# An Introduction to the New SQL Query Engine

---
## 1.Motivations

### 1.1 Why Another Query Engine

The current SQL query engine provides users the basic query capability for using familiar SQL rather than complex Elasticsearch DSL. Based on NLPchina ES-SQL, many new features have been added additionally, such as semantic analyzer, semi-structured data query support, Hash Join etc. However, as we looked into more advanced SQL features, challenges started emerging especially in terms of correctness and extensibility (see [Attributions](../attributions.md). After thoughtful consideration, we decided to develop a new query engine to address all the problems met so far.

### 1.2 Why Not Apache Calcite

Apache Calcite is a very popular choice for those who wants to introduce SQL query capability with minimal effort. The codebase is well-structured and extensible. Many SQL features and data store adaptors are already built-to for different databases. However, we decide not to develop on top of Apache Calcite because:

1. **New language**: As an immediate requirement, we needed to introduce a new Piped Processing Language (PPL).
2. **Semi-structured data**: We needed to follow PartiQL spec to support query with semi-structured data.
3. **Elasticsearch support**: The Elasticsearch adaptor only provided very basic query capability. It would still take many efforts to support Elasticsearch specific data types, identifiers, queries and make it production-ready.
4. **Optimization**: The optimizer on single table query couldn't help much because eventually we depend on Elasticsearch DSL and its execution cost is unknown.
5. **Codebase**: Most importantly, for the problems mentioned above, the codebase is too huge and complex to figure out how much flexibility we have for future requirements. It's possible that Calcite is extensible sufficiently, though if not, we would take a risk of having to either touch Calcite's source or start over by ourselves.
6. **Other examples** for unknown challenges include pagination support, thread model, performance comparison, circuit breaking mechanism, security plugin integration, metric collecting.


---
## 2.What's New

### 2.1 SQL Features

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
* [Window functions](/docs/user/dql/window.rst): ranking and aggregate window function support

As for correctness, besides full coverage of unit and integration test, we developed a new comparison test framework to ensure correctness by comparing with other databases. Find more details in [Testing](./Testing.md).


---
## 3.What're Changed

### 3.1 Limitations

You can find all the limitations in [Limitations](/docs/user/limitations/limitations.rst). For these unsupported features, the query will be sent to the old query engine by fallback mechanism. To avoid impact on your side, normally you won't see any difference in query response. If you want to check if and why your query falls back to be handled by old SQL engine, please explain your query and check Elasticsearch log for "Request is falling back to old SQL engine due to ...".

Basically, here is a list of the features common though not supported in new query engine yet:

* **Cursor**: request with fetch size parameter
* **JSON response format**: will not be supported anymore in new engine
* **Nested field query**: include both object and nested field query
* **JOINs**: include all types of join queries
* **Elasticsearch functions**: fulltext search, metric and bucket functions

### 3.2 Breaking Changes

Because of implementation changed internally, you can expect Explain output in a different format. For query protocol, there are slightly changes for two fields' value in the default response format.

* Schema: Previously the `name` and `alias` value differs for different queries. For consistency, now name is always the original text and alias is its alias defined in SELECT clause or absent if none.
* Total: Previously the `total` field is how many documents in total no matter how many returned (specified by `size` field). However, this field becomes meaningless because of post processing on DSL response in the new query engine. Thus, the total number is always same as size field.

### 3.3 What if Something Wrong

No panic! You can roll back to old query engine easily by a plugin setting change. Simply run the command to disable it by [plugin setting](/docs/user/admin/settings.rst#opendistro-sql-engine-new-enabled). Same as other cluster setting change, no need to restart Elasticsearch and the change will take effects on next incoming query. Later on please report the issue to us for a fix as well as making our new query engine better.


---
## 4.How it's Implemented

If you're interested in the new query engine, please find more details in [Architecture](./Architecture.md) and other docs in the dev folder.


---
## 5.What's Next

The following items are major work on our roadmap:

1. Nested field query
2. JOIN support
3. Elasticsearch functions
