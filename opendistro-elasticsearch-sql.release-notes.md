## 2020-03-10 Version 1.6.0.0 (Current)

### Features
#### Elasticsearch Compatibility
* Feature [#376](https://github.com/opendistro-for-elasticsearch/sql/issues/376): Elasticsearch 7.6.1 compatibility

#### Testing
* Feature [#374](https://github.com/opendistro-for-elasticsearch/sql/pull/374): Integration test with external ES cluster (issue: [353](https://github.com/opendistro-for-elasticsearch/sql/issues/353))

#### Documentation
* Feature [#366](https://github.com/opendistro-for-elasticsearch/sql/pull/366): Documentation for simple query (issue: [#363](https://github.com/opendistro-for-elasticsearch/sql/issues/363)) 

### Enhancements
#### SQL Features
* Enhancement [#367](https://github.com/opendistro-for-elasticsearch/sql/pull/367): Report date data as a standardized format (issue: [#342](https://github.com/opendistro-for-elasticsearch/sql/issues/342))


### Bugfixes
* BugFix [#365](https://github.com/opendistro-for-elasticsearch/sql/pull/365): Return Correct Type Information for Fields (issue: [#316](https://github.com/opendistro-for-elasticsearch/sql/issues/316))


## 2020-01-24, Version 1.4.0

### Features
#### Elasticsearch Compatibility
* Feature [#312](https://github.com/opendistro-for-elasticsearch/sql/issues/312): Elasticsearch 7.4.2 compatibility 

#### Testing
* Feature ([#335](https://github.com/opendistro-for-elasticsearch/sql/pull/335), [#343](https://github.com/opendistro-for-elasticsearch/sql/pull/343)): Sql test bench bug fix, improvement and more test cases (issues: [#335](https://github.com/opendistro-for-elasticsearch/sql/issues/335), [#339](https://github.com/opendistro-for-elasticsearch/sql/issues/339))

#### Documentation
* Feature ([#302](https://github.com/opendistro-for-elasticsearch/sql/pull/302), [#305](https://github.com/opendistro-for-elasticsearch/sql/pull/305), [#303](https://github.com/opendistro-for-elasticsearch/sql/pull/303)): Documentation for basic usage of plugin & improvement of contributing docs (issues: [#293](https://github.com/opendistro-for-elasticsearch/sql/issues/293), [#243](https://github.com/opendistro-for-elasticsearch/sql/issues/243))

#### Github Actions
* Feature ([#283](https://github.com/opendistro-for-elasticsearch/sql/pull/283), [#287](https://github.com/opendistro-for-elasticsearch/sql/pull/287)): Added github action to build and run tests. Gradle build will publish compiled plugin, that is ready to install into elastic

### Enhancements
#### Lexer and Semantic
* Enhancement [#345](https://github.com/opendistro-for-elasticsearch/sql/pull/345): Syntax and semantic exceptions handling for unsupported features (issue: [#320](https://github.com/opendistro-for-elasticsearch/sql/issues/320))

#### SQL Feature
* Enhancement ([#346](https://github.com/opendistro-for-elasticsearch/sql/pull/346), [#352](https://github.com/opendistro-for-elasticsearch/sql/pull/352)): Support function over aggregation result (issues: [#194](https://github.com/opendistro-for-elasticsearch/sql/issues/194), [#229](https://github.com/opendistro-for-elasticsearch/sql/issues/229), [#270](https://github.com/opendistro-for-elasticsearch/sql/issues/270), [#292](https://github.com/opendistro-for-elasticsearch/sql/issues/292))
* Enhancement [#273](https://github.com/opendistro-for-elasticsearch/sql/pull/273): Support conditional functions: IF, IFNULL, ISNULL (issues: [#224](https://github.com/opendistro-for-elasticsearch/sql/issues/224), [#235](https://github.com/opendistro-for-elasticsearch/sql/issues/235)) 
* Enhancement [#274](https://github.com/opendistro-for-elasticsearch/sql/pull/274): Support JOIN without table alias (issue: [#232](https://github.com/opendistro-for-elasticsearch/sql/issues/232))
* Enhancement [#278](https://github.com/opendistro-for-elasticsearch/sql/pull/278): Support subquery in from with parent only has select (issue: [#230](https://github.com/opendistro-for-elasticsearch/sql/issues/230))
* Enhancement [#282](https://github.com/opendistro-for-elasticsearch/sql/pull/282): Support datetime functions: MONTH, DAYOFMONTH, DATE, MONTHNAME, TIMESTAMP, MAKETIME, NOW, CURDATE (issue: [#235](https://github.com/opendistro-for-elasticsearch/sql/issues/235))
* Enhancement [#300](https://github.com/opendistro-for-elasticsearch/sql/pull/300): Support DISTINCT feature in SELECT clause (issue: [#294](https://github.com/opendistro-for-elasticsearch/sql/issues/294))

#### Response
* Enhancement [#334](https://github.com/opendistro-for-elasticsearch/sql/pull/334): Change the default response format to JDBC (issue: [#159](https://github.com/opendistro-for-elasticsearch/sql/issues/159))

### Bugfixes
* BugFix [#267](https://github.com/opendistro-for-elasticsearch/sql/pull/267): Fixed operatorReplace Integration Test (issue: [#266](https://github.com/opendistro-for-elasticsearch/sql/issues/266))
* BugFix [#275](https://github.com/opendistro-for-elasticsearch/sql/pull/275): Fix issue that IP type cannot pass JDBC formatter (issue: [#272](https://github.com/opendistro-for-elasticsearch/sql/issues/272))
* BugFix [#284](https://github.com/opendistro-for-elasticsearch/sql/pull/284): Fixed flaky test suite, that was breaking Github action build
* BugFix [#295](https://github.com/opendistro-for-elasticsearch/sql/pull/295): Corrected the selected field names displayed in the schema of JDBC formatted response (issue: [#290](https://github.com/opendistro-for-elasticsearch/sql/issues/290))
* BugFix [#296](https://github.com/opendistro-for-elasticsearch/sql/pull/296): Fixed functions work improperly with fieldvalue/constant param for current use (issues: [#279](https://github.com/opendistro-for-elasticsearch/sql/issues/279), [#291](https://github.com/opendistro-for-elasticsearch/sql/issues/291), [#224](https://github.com/opendistro-for-elasticsearch/sql/issues/224))
* BugFix [#298](https://github.com/opendistro-for-elasticsearch/sql/pull/298): Fixed issue of log10 function gets inaccurate results （issue: [#297](https://github.com/opendistro-for-elasticsearch/sql/issues/297))
* BugFix [#307](https://github.com/opendistro-for-elasticsearch/sql/pull/307): Fix the issue of column alias not working for GROUP BY (issue: [#299](https://github.com/opendistro-for-elasticsearch/sql/issues/299))
* BugFix [#333](https://github.com/opendistro-for-elasticsearch/sql/pull/333): Fixed the issue of substring not working correctly when fieldname is put as <table>.<column> (issue: [#330](https://github.com/opendistro-for-elasticsearch/sql/issues/330))
* BugFix [#337](https://github.com/opendistro-for-elasticsearch/sql/pull/337): Fix JDBC response for delete query (issue: [#131](https://github.com/opendistro-for-elasticsearch/sql/issues/131))

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

## 2019-10-15, Version 1.2.1

### Notable changes

* Feature [#202](https://github.com/opendistro-for-elasticsearch/sql/issues/202): Elasticsearch 7.2.1 compatibility

## 2019-07-23, Version 1.2.0

### Notable changes

* Feature [#125](https://github.com/opendistro-for-elasticsearch/sql/issues/125): Elasticsearch 7.2.0 compatibility
* Feature [#103](https://github.com/opendistro-for-elasticsearch/sql/issues/103): Sort on custom script based functions 

* BugFix [#104](https://github.com/opendistro-for-elasticsearch/sql/issues/104): GROUP BY and ORDER BY for custom script based functions 
generated wrong DSL
* BugFix [#95](https://github.com/opendistro-for-elasticsearch/sql/issues/95): NPE thrown when selecting all in query with nested fields involved
* BugFix [#108](https://github.com/opendistro-for-elasticsearch/sql/issues/108): Custom script based functions weren't supported in JDBC

and other minor bugfixes

## 2019-06-21, Version 1.1.0

### Notable Changes

* Feature [#90](https://github.com/opendistro-for-elasticsearch/sql/issues/90): Elasticsearch 7.1.1 compatibility
* Migration [#17](https://github.com/opendistro-for-elasticsearch/sql/issues/17): Finished integ test migration and deprecated Maven build.
* Enhancement [#34](https://github.com/opendistro-for-elasticsearch/sql/issues/34): Detached request ID from SqlRequest object.

* BugFix [#46](https://github.com/opendistro-for-elasticsearch/sql/issues/46): Multi-index queries require identical mappings for indices.
* BugFix [#92](https://github.com/opendistro-for-elasticsearch/sql/issues/92): Improper handling of SELECT statement
* BugFix [#93](https://github.com/opendistro-for-elasticsearch/sql/issues/93): Blank query causes IndexOutOfBoundsException


## 2019-06-10, Version 1.0.0

### Notable Changes

* Feature [#47](https://github.com/opendistro-for-elasticsearch/sql/issues/47): Support for Elasticsearch 7.0.1.
* Feature [#56](https://github.com/opendistro-for-elasticsearch/sql/issues/56): Adding coverage report.
* Feature [#65](https://github.com/opendistro-for-elasticsearch/sql/issues/65): Support for enabling/disabling SQL feature.

* BugFix [#44](https://github.com/opendistro-for-elasticsearch/sql/issues/44): Fixing the order of fields in csv output
* BugFix [#68](https://github.com/opendistro-for-elasticsearch/sql/issues/68): Support number field
* BugFix [#37](https://github.com/opendistro-for-elasticsearch/sql/issues/37): Fix for PERCENTILES query result in csv output


## 2019-04-19, Version 0.9.0

### Notable Changes

* Feature [#29](https://github.com/opendistro-for-elasticsearch/sql/issues/29): Add support for Elasticsearch 6.7.1
* Migration [#17](https://github.com/opendistro-for-elasticsearch/sql/issues/17): Migrate legacy integration tests in Maven to ES test in Gradle.


## 2019-04-02, Version 0.8.0

### Notable Changes

* Feature [#12](https://github.com/opendistro-for-elasticsearch/sql/issues/12): Add support for Elasticsearch 6.6.2
* Bug fix [#9](https://github.com/opendistro-for-elasticsearch/sql/issues/9): Fix issue for query by index pattern in JDBC driver.
* Bug fix [#10](https://github.com/opendistro-for-elasticsearch/sql/issues/10): Return friendly error message instead of NPE for illegal query and other exception cases.


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
