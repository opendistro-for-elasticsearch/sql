## 20201-2-3 Version 1.13.0.0

### Breaking Changes
* Refine PPL head command syntax ([#1022](https://github.com/opendistro-for-elasticsearch/sql/pull/1022))
* Disable access to the field keyword in the new SQL engine ([#1025](https://github.com/opendistro-for-elasticsearch/sql/pull/1025))

### Features
* Added keywords option as alias identifier in SQL parser ([#866](https://github.com/opendistro-for-elasticsearch/sql/pull/866))
* Support show and describe statement ([#907](https://github.com/opendistro-for-elasticsearch/sql/pull/907))
* Support cast function in SQL ([#926](https://github.com/opendistro-for-elasticsearch/sql/pull/926))
* Support NULLS FIRST/LAST ordering for window functions ([#929](https://github.com/opendistro-for-elasticsearch/sql/pull/929))
* Project operator pushdown ([#933](https://github.com/opendistro-for-elasticsearch/sql/pull/933))
* add string function RIGHT ([#938](https://github.com/opendistro-for-elasticsearch/sql/pull/938))
* Add Flow control function IF(expr1, expr2, expr3) ([#990](https://github.com/opendistro-for-elasticsearch/sql/pull/990))
* Support Struct Data Query in SQL/PPL ([#1018](https://github.com/opendistro-for-elasticsearch/sql/pull/1018))

### Enhancements
* Enable new SQL query engine ([#989](https://github.com/opendistro-for-elasticsearch/sql/pull/989))
* Add metrics for SQL query requests in new engine ([#905](https://github.com/opendistro-for-elasticsearch/sql/pull/905))
* Enable failed test logging and fix flaky UT ([#910](https://github.com/opendistro-for-elasticsearch/sql/pull/910))
* Improve logging in new SQL engine ([#912](https://github.com/opendistro-for-elasticsearch/sql/pull/912))
* Enable date type input in function Count() ([#931](https://github.com/opendistro-for-elasticsearch/sql/pull/931))
* Use name and alias in JDBC format ([#932](https://github.com/opendistro-for-elasticsearch/sql/pull/932))
* Throw exception when access unknown field type ([#942](https://github.com/opendistro-for-elasticsearch/sql/pull/942))
* Fill hyphen strings to workbench table cells that have null and missing values ([#944](https://github.com/opendistro-for-elasticsearch/sql/pull/944))
* Support aggregate window functions ([#946](https://github.com/opendistro-for-elasticsearch/sql/pull/946))
* Support filter clause in aggregations ([#960](https://github.com/opendistro-for-elasticsearch/sql/pull/960))
* Enable sql function ifnull, nullif and isnull ([#962](https://github.com/opendistro-for-elasticsearch/sql/pull/962))
* Double quoted as string literal instead of identifier ([#974](https://github.com/opendistro-for-elasticsearch/sql/pull/974))
* [PPL] Support index name with date suffix ([#983](https://github.com/opendistro-for-elasticsearch/sql/pull/983))
* Support NULL literal as function argument ([#985](https://github.com/opendistro-for-elasticsearch/sql/pull/985))
* Allow Timestamp/Datetime values to use up to 6 digits of microsecond precision ([#988](https://github.com/opendistro-for-elasticsearch/sql/pull/988))
* Protect window operator by circuit breaker ([#1006](https://github.com/opendistro-for-elasticsearch/sql/pull/1006))
* Removed stack trace when catches anonymizing data error ([#1014](https://github.com/opendistro-for-elasticsearch/sql/pull/1014))
* Disable access to the field keyword in the new SQL engine ([#1025](https://github.com/opendistro-for-elasticsearch/sql/pull/1025))
* Only keep the first element of multivalue field response ([#1026](https://github.com/opendistro-for-elasticsearch/sql/pull/1026))
* Remove request id in response listener logging ([#1027](https://github.com/opendistro-for-elasticsearch/sql/pull/1027))

### Bug Fixes
* Fix round fix issue when input is negative and end with .5 ([#914](https://github.com/opendistro-for-elasticsearch/sql/pull/914))
* Add fix to handle functions applied to literal ([#913](https://github.com/opendistro-for-elasticsearch/sql/pull/913))
* Fetch error message in root cause for new default formatter ([#1001](https://github.com/opendistro-for-elasticsearch/sql/pull/1001))
* Fixed interval type null/missing check failure ([#1011](https://github.com/opendistro-for-elasticsearch/sql/pull/1011))
* Fix workbench issue that csv result not written to downloaded file ([#1024](https://github.com/opendistro-for-elasticsearch/sql/pull/1024))

### Infrastructure
* Bump ini from 1.3.5 to 1.3.7 in /workbench ([#911](https://github.com/opendistro-for-elasticsearch/sql/pull/911))
* Backport workbench fixes to 7.9.1 ([#937](https://github.com/opendistro-for-elasticsearch/sql/pull/937))
* Backport workbench fixes to 7.9 on old platform ([#940](https://github.com/opendistro-for-elasticsearch/sql/pull/940))
* Backporting latest change from develop to opendistro-1.11 ([#945](https://github.com/opendistro-for-elasticsearch/sql/pull/945))
* Bump jackson-databind version to 2.10.5.1 ([#984](https://github.com/opendistro-for-elasticsearch/sql/pull/984))
* Rename sql release artifacts ([#1007](https://github.com/opendistro-for-elasticsearch/sql/pull/1007))
* Rename odbc release artifacts ([#1010](https://github.com/opendistro-for-elasticsearch/sql/pull/1010))
* Rename sql-cli wheel to use dashes instead of underscore ([#1015](https://github.com/opendistro-for-elasticsearch/sql/pull/1015))

### Documentation
* Keep development doc sync with latest code ([#961](https://github.com/opendistro-for-elasticsearch/sql/pull/961))
* Update ODBC documentation ([#1012](https://github.com/opendistro-for-elasticsearch/sql/pull/1012))

### Maintenance
* Fix workbench issue in backported 1.11 branch: error message cannot display ([#943](https://github.com/opendistro-for-elasticsearch/sql/pull/943))
* Fix URI Encoding in 1.12 ([#955](https://github.com/opendistro-for-elasticsearch/sql/pull/955))
