## 2019-10-29, Version 1.3.0

### Notable changes

* Feature [#258](https://github.com/opendistro-for-elasticsearch/sql/issues/185): Elasticsearch 7.3.2 compatibility
* Feature [#201](https://github.com/opendistro-for-elasticsearch/sql/pull/201): Improve query verification by adding semantic analyzer

* Enhancement [#262](https://github.com/opendistro-for-elasticsearch/sql/pull/262): Support CASE statement in one more grammer
* Enhancement [#253](https://github.com/opendistro-for-elasticsearch/sql/pull/253): Support Cast function
* Enhancement [#260](https://github.com/opendistro-for-elasticsearch/sql/pull/260): Support string operators: ASCII, RTRIM, LTRIM, LOCATE, LENGTH, REPLACE
* Enhancement [#254](https://github.com/opendistro-for-elasticsearch/sql/issues/254): Support SELECT <number_literal>
* Enhancement [#251](https://github.com/opendistro-for-elasticsearch/sql/pull/251): Support number operators: POWER, ATAN2, COT, SIGN/SIGNUM
* Enhancement [#215](https://github.com/opendistro-for-elasticsearch/sql/issues215): Support ordinal in GROUP/ORDER BY clause
* Enhancement [#213](https://github.com/opendistro-for-elasticsearch/sql/issues/213): Support `<table>.<column>` syntax
* Enhancement [#212](https://github.com/opendistro-for-elasticsearch/sql/issues/212): Support Quoted identifiers
* Enhancement [#199](https://github.com/opendistro-for-elasticsearch/sql/issues/199): Support NOT operator with nested field query
* Enhancement [#166](https://github.com/opendistro-for-elasticsearch/sql/issues/166): Support pretty option for explain request
* Enhancement [#162](https://github.com/opendistro-for-elasticsearch/sql/issues/162): Support ORDER BY for 2 or more columns sorts only by last column in aggregation query
* Enhancement [#151](https://github.com/opendistro-for-elasticsearch/sql/issues/151): Improve query verification and exception handling
* Enhancement [#128](https://github.com/opendistro-for-elasticsearch/sql/issues/128): Support case-changing functions
* Enhancement [#120](https://github.com/opendistro-for-elasticsearch/sql/issues/120): Support aggregation function in HAVING
* Enhancement [#82](https://github.com/opendistro-for-elasticsearch/sql/issues/82): Pre-verification before actual execution of query
* Enhancement [#75](https://github.com/opendistro-for-elasticsearch/sql/issues/75): Support order by on SQL functions like SUM etc

* BugFix [#265](https://github.com/opendistro-for-elasticsearch/sql/pull/265): Fix the LOG function that delivered inaccurate result
* BugFix [#233](https://github.com/opendistro-for-elasticsearch/sql/issues/233): Function names are case-sensitive
* BugFix [#191](https://github.com/opendistro-for-elasticsearch/sql/issues/191): Fix new syntax check for LEFT JOIN on nested field and metadata field
* BugFix [#188](https://github.com/opendistro-for-elasticsearch/sql/issues/188): Having doesn't working on nested field
* BugFix [#187](https://github.com/opendistro-for-elasticsearch/sql/issues/187): Aggregation on Nested Array Field return unexpected value
* BugFix [#186](https://github.com/opendistro-for-elasticsearch/sql/issues/186): Alias of group key is not returned correctly
* BugFix [#176](https://github.com/opendistro-for-elasticsearch/sql/issues/176): ORDER BY expects function names written only in lowercase
* BugFix [#175](https://github.com/opendistro-for-elasticsearch/sql/issues/175): ORDER BY doesn't respect table aliases
* BugFix [#170](https://github.com/opendistro-for-elasticsearch/sql/issues/170): Some flaky test cases fail and pass after retry
* BugFix [#158](https://github.com/opendistro-for-elasticsearch/sql/issues/158): Aliases aren't working for ORDER BY and GROUP BY
* BugFix [#132](https://github.com/opendistro-for-elasticsearch/sql/issues/132): Integration Test Cluster doesn't terminated when integration test failed
* BugFix [#125](https://github.com/opendistro-for-elasticsearch/sql/issues/125): Parsing Exception when WHERE clause has true/false in CONDITION
* BugFix [#122](https://github.com/opendistro-for-elasticsearch/sql/issues/122): Query with custom-function doesn't respect LIMIT
* BugFix [#121](https://github.com/opendistro-for-elasticsearch/sql/issues/121): Dot/period at start of index name fails to parse
* BugFix [#111](https://github.com/opendistro-for-elasticsearch/sql/issues/111): JDBC format of aggregation query with date_format adds unnecessary column bug