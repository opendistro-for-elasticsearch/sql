## 2019-03-11, Version 0.7.0

### Notable Changes

In this release, the following features are added with many other minor improvements and bug fixes.

* **SQL HAVING**:
  * Add support for SQL HAVING to filter aggregated result after GROUP BY.
* **SQL Functions**:
  * Common math functions as well as date function are available.
* **Nested Field Query**:
  * Query nested field in SQL++ syntax instead of explicit nested() function.
* **JSON in RESTful Request**:
  * Support JSON payload to allow for ESRally benchmark and parameters in JDBC request. And also pretty format and flatten native Elasticsearch DSL as response.
* **Block Hash Join**:
  * Introduced new query planning framework and new hash join algorithm with memory protection mechanism to perform hash join block by block safely.


### Major Refactoring & Architecture Changes

* **Avoid I/O Operation in NIO Thread**
  * Move I/O blocking operations to custom worker thread pool or prefetch to avoid blocking. Meanwhile non-blocking operations still run in Elasticsearch transport thread for efficiency.


### Commits

* [[`56dd269`](https://github.com/mauve-hedgehog/opendistro-elasticsearch-sql/commit/56dd269871259dae9fb77ab512005b9a714d728e)] Initial commit
