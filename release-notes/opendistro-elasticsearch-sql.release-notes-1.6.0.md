## 2020-03-24 Version 1.6.0.0

### Features
#### Elasticsearch Compatibility
* Feature [#376](https://github.com/opendistro-for-elasticsearch/sql/issues/376): Elasticsearch 7.6.1 compatibility

#### Testing
* Feature [#374](https://github.com/opendistro-for-elasticsearch/sql/pull/374): Integration test with external ES cluster (issue: [353](https://github.com/opendistro-for-elasticsearch/sql/issues/353))
* Feature [#384](https://github.com/opendistro-for-elasticsearch/sql/pull/384): CI/CD using github Actions workflow

#### Documentation
* Feature [#366](https://github.com/opendistro-for-elasticsearch/sql/pull/366): Documentation for simple query (issue: [#363](https://github.com/opendistro-for-elasticsearch/sql/issues/363)) 
* Feature [#379](https://github.com/opendistro-for-elasticsearch/sql/pull/379): Documentation for Pagination (issue: [#16](https://github.com/opendistro-for-elasticsearch/sql/issues/16))

### Enhancements
#### SQL Features
* Enhancement [#367](https://github.com/opendistro-for-elasticsearch/sql/pull/367): Report date data as a standardized format (issue: [#342](https://github.com/opendistro-for-elasticsearch/sql/issues/342))

#### Exception Handling
* Enhancement [#362](https://github.com/opendistro-for-elasticsearch/sql/pull/362): Handle the elasticsearch exceptions in JDBC formatted outputs(issues: [#320](https://github.com/opendistro-for-elasticsearch/sql/issues/320), [308](https://github.com/opendistro-for-elasticsearch/sql/issues/308))
* Enhancement [#372](https://github.com/opendistro-for-elasticsearch/sql/pull/372): Modified the wording of exception messages and created the troubleshooting page(issue: [#320](https://github.com/opendistro-for-elasticsearch/sql/issues/320))

### Bugfixes
* Bugfix [#310](https://github.com/opendistro-for-elasticsearch/sql/pull/310): Add DATETIME cast support (issue: [#268](https://github.com/opendistro-for-elasticsearch/sql/issues/268))
* BugFix [#365](https://github.com/opendistro-for-elasticsearch/sql/pull/365): Return Correct Type Information for Fields (issue: [#316](https://github.com/opendistro-for-elasticsearch/sql/issues/316))
* BugFix [#377](https://github.com/opendistro-for-elasticsearch/sql/pull/377): Return object type for field which has implicit object datatype when describe the table (issue:[sql-jdbc#57](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/57))
* BugFix [#381](https://github.com/opendistro-for-elasticsearch/sql/pull/381): FIX field function name letter case preserved in select with group by (issue: [#373](https://github.com/opendistro-for-elasticsearch/sql/issues/373))
