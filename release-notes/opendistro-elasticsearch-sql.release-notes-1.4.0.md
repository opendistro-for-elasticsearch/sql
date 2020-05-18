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
