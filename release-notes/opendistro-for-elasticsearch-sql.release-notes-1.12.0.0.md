## 2020-11-20 Version 1.12.0.0

### Features
* Add count() support for PPL ([#894](https://github.com/opendistro-for-elasticsearch/sql/pull/894))
* Add like, isnull, isnotnull function in PPL ([#893](https://github.com/opendistro-for-elasticsearch/sql/pull/893))
* Revert Error Messages for Run/Explain ([#889](https://github.com/opendistro-for-elasticsearch/sql/pull/889))
* Extend the PPL identifier defintion ([#888](https://github.com/opendistro-for-elasticsearch/sql/pull/888))
* Add Error Message for Explain and Run ([#872](https://github.com/opendistro-for-elasticsearch/sql/pull/872))
* Remove workbench side nav logo and fix download link ([#869](https://github.com/opendistro-for-elasticsearch/sql/pull/869))
* For ODFE 1.12 change position for sql workbench plugin (remove DEFAULT_APP_CATEGORIES) ([#857](https://github.com/opendistro-for-elasticsearch/sql/pull/857))
* For ODFE 1.12 change position for sql workbench plugin ([#855](https://github.com/opendistro-for-elasticsearch/sql/pull/855))
* add support for HH:mm:ss ([#850](https://github.com/opendistro-for-elasticsearch/sql/pull/850))
* Support NULLS FIRST/LAST in new engine ([#843](https://github.com/opendistro-for-elasticsearch/sql/pull/843))
* Support subquery in FROM clause in new engine ([#822](https://github.com/opendistro-for-elasticsearch/sql/pull/822))
* Support CASE clause in new engine ([#818](https://github.com/opendistro-for-elasticsearch/sql/pull/818))
* Support COUNT star and literal in new engine ([#802](https://github.com/opendistro-for-elasticsearch/sql/pull/802))
* Adding example of nested() for more complex nested queries ([#801](https://github.com/opendistro-for-elasticsearch/sql/pull/801))
* Adding example of nested() for more complex nested queries ([#799](https://github.com/opendistro-for-elasticsearch/sql/pull/799))
* Support HAVING in new SQL engine ([#798](https://github.com/opendistro-for-elasticsearch/sql/pull/798))
* Add ppl request log ([#796](https://github.com/opendistro-for-elasticsearch/sql/pull/796))

### Enhancements
* Sort field push down ([#848](https://github.com/opendistro-for-elasticsearch/sql/pull/848))
* Seperate the logical plan optimization rule from core to storage engine ([#836](https://github.com/opendistro-for-elasticsearch/sql/pull/836))

### Bug Fixes
* Fix workbench version number for ODFE 1.12.0.0 ([#903](https://github.com/opendistro-for-elasticsearch/sql/pull/903))
* Disable sorting on workbench ([#900](https://github.com/opendistro-for-elasticsearch/sql/pull/900))
* Fix select all from subquery issue ([#902](https://github.com/opendistro-for-elasticsearch/sql/pull/902))
* Fix for ExprValueFactory construct issue ([#898](https://github.com/opendistro-for-elasticsearch/sql/pull/898))
* Fix issue result table in workbench not displaying values of boolean type ([#891](https://github.com/opendistro-for-elasticsearch/sql/pull/891))
* Fix key "id" in result table issue of the workbench ([#890](https://github.com/opendistro-for-elasticsearch/sql/pull/890))
* Fix workbench bugs from plugin platform upgrade ([#886](https://github.com/opendistro-for-elasticsearch/sql/pull/886))
* Fix ExprCollectionValue serialization bug ([#859](https://github.com/opendistro-for-elasticsearch/sql/pull/859))
* Fix issue: sort order keyword is case sensitive ([#853](https://github.com/opendistro-for-elasticsearch/sql/pull/853))
* Config the default locale for gradle as en_US ([#847](https://github.com/opendistro-for-elasticsearch/sql/pull/847))
* Fix bug of nested field format issue in JDBC response ([#846](https://github.com/opendistro-for-elasticsearch/sql/pull/846))
* Fix symbol error and Fix SSLError when connect es. ([#831](https://github.com/opendistro-for-elasticsearch/sql/pull/831))
* Bug fix, using Local.Root when format the string in DateTimeFunctionIT ([#794](https://github.com/opendistro-for-elasticsearch/sql/pull/794))

### Infrastructure
* Revert java version in jdbc release workflow ([#871](https://github.com/opendistro-for-elasticsearch/sql/pull/871))
* fix for odbc build failure ([#885](https://github.com/opendistro-for-elasticsearch/sql/pull/885))
* Fix release workflow for workbench ([#868](https://github.com/opendistro-for-elasticsearch/sql/pull/868))
* Add workflow to rename and upload odbc to s3 ([#865](https://github.com/opendistro-for-elasticsearch/sql/pull/865))
* add codecov for sql plugin ([#835](https://github.com/opendistro-for-elasticsearch/sql/pull/835))
* update odbc workflow ([#828](https://github.com/opendistro-for-elasticsearch/sql/pull/828))
* Updated workbench snapshots to fix broken workflow ([#823](https://github.com/opendistro-for-elasticsearch/sql/pull/823))
* Updated Mac version for GitHub action build ([#804](https://github.com/opendistro-for-elasticsearch/sql/pull/804))
* Fix unstable integration tests ([#793](https://github.com/opendistro-for-elasticsearch/sql/pull/793))
* Update cypress tests and increase delay time ([#792](https://github.com/opendistro-for-elasticsearch/sql/pull/792))

### Documentation
* Add identifier and datatype documentation for PPL ([#873](https://github.com/opendistro-for-elasticsearch/sql/pull/873))
* Add doc for ODFE SQL demo ([#826](https://github.com/opendistro-for-elasticsearch/sql/pull/826))
* Update out-of-date documentation ([#820](https://github.com/opendistro-for-elasticsearch/sql/pull/820))
* Add release notes for ODFE 1.12 ([#841](https://github.com/opendistro-for-elasticsearch/sql/pull/841))

### Maintenance
* SQL release for Elasticsearch 7.10 ([#834](https://github.com/opendistro-for-elasticsearch/sql/pull/834))
* Migrate Query Workbench to new Platform ([#812](https://github.com/opendistro-for-elasticsearch/sql/pull/812))
* Migrate Query Workbench to 7.10 ([#840](https://github.com/opendistro-for-elasticsearch/sql/pull/840))
* Bump version number to 1.12.0.0 for [JDBC, ODBC, SQL-CLI] ([#838](https://github.com/opendistro-for-elasticsearch/sql/pull/838))

