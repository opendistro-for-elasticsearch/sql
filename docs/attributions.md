This project is based on the Apache 2.0-licensed [elasticsearch-sql](https://github.com/NLPchina/elasticsearch-sql) project. Thank you [eliranmoyal](https://github.com/eliranmoyal), [shi-yuan](https://github.com/shi-yuan), [ansjsun](https://github.com/ansjsun) and everyone else who contributed great code to that project. 

## What’s OpenDistro SQL?

The Open Distro for Elaticsearch SQL plugin launched early this year which lets you write queries in SQL rather than the Elasticsearch query domain-specific language (DSL). While the majority of our codebase is on top of ES-SQL initially, there are a lot of new features and bug fixes introduced in our implementation. And in the following releases this year, we keep improving and refactoring our code as well as maintaining version currency of Elasticsearch. Basically OpenDistro SQL is superset of ES-SQL and it’s more reliable and up-to-date.


## The problems we found in NLPchina-ES-SQL.

The ES-SQL codebase has clear architecture and abstraction for a basic query engine, such as SQL parser, DSL generator and domain model. However as we dived deep, we identified the following major problems and resolved them before launch:

1. The JDBC driver used Elasticsearch proprietary API `PreBuiltXPackTransportClient`.
2. JOIN capability matters but the Hash JOIN implementation was not scalable for production use because it loads all data into memory.
3. For complex queries like JOIN or Multi-query, there was no concept of a query plan for planning and optimizing.
4. ES-SQL uses a handwritten SQL parser Druid which is not extensible, so many queries that not supported or has semantical error can still pass the parsing but ended up throwing runtime error.



## What major improvements we made?

Apart from the problems we identified earlier, we made significant improvement in terms of functionality and reliability:

1. *Integration Test*: We migrated all integrate tests to standard Elasticsearch IT framework which spins up in-memory cluster for testing. Now all test cases treat plugin code as blackbox and verify functionality from externally.
2. *New JDBC Driver*: We developed our own JDBC driver without any dependency on Elasticsearch proprietary code.
    [sql-jdbc](https://github.com/opendistro-for-elasticsearch/sql/tree/master/sql-jdbc)
3. *Better Hash JOIN*: OpenDistro SQL launched with Block Hash Join implementation with circuit break mechanism to protect your Elasticsearch memory. Performance testing showed our implementation is 1.5 ~ 2x better than old hash join in terms of throughput and latency and much lower error rate under heavy pressure.
4. *Query Planner*: Logical and physical planner was added to support JOIN query in efficient and extendible way.
5. *PartiQL Compatibility*: we are partially compatible with PartiQL specification which allows for query involved in nested JSON documents.
6. *New ANTLR Parser*: A new ANTLR4 parser was generated from grammar based on what we support along with a new semantic analyzer to perform scope and type checking.

