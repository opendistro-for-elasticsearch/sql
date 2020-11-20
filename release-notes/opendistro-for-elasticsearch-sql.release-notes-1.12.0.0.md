## 2020-11-20 Version 1.12.0.0

### Features
* Support subquery in FROM clause in new engine ([#822](https://github.com/opendistro-for-elasticsearch/sql/pull/822))
* Support CASE clause in new engine ([#818](https://github.com/opendistro-for-elasticsearch/sql/pull/818))
* Support COUNT star and literal in new engine ([#802](https://github.com/opendistro-for-elasticsearch/sql/pull/802))
* Adding example of nested() for more complex nested queries ([#801](https://github.com/opendistro-for-elasticsearch/sql/pull/801))
* Revert "Adding example of nested() for more complex nested queries" ([#800](https://github.com/opendistro-for-elasticsearch/sql/pull/800))
* Adding example of nested() for more complex nested queries ([#799](https://github.com/opendistro-for-elasticsearch/sql/pull/799))
* Support HAVING in new SQL engine ([#798](https://github.com/opendistro-for-elasticsearch/sql/pull/798))
* Add ppl request log ([#796](https://github.com/opendistro-for-elasticsearch/sql/pull/796))

### Enhancements
* Seperate the logical plan optimization rule from core to storage engine ([#836](https://github.com/opendistro-for-elasticsearch/sql/pull/836))

### Bug Fixes
* Fix symbol error and Fix SSLError when connect es. ([#831](https://github.com/opendistro-for-elasticsearch/sql/pull/831))
* Bug fix, using Local.Root when format the string in DateTimeFunctionIT ([#794](https://github.com/opendistro-for-elasticsearch/sql/pull/794))

### Infrastructure
* add codecov for sql plugin ([#835](https://github.com/opendistro-for-elasticsearch/sql/pull/835))
* update odbc workflow ([#828](https://github.com/opendistro-for-elasticsearch/sql/pull/828))
* Updated workbench snapshots to fix broken workflow ([#823](https://github.com/opendistro-for-elasticsearch/sql/pull/823))
* Updated Mac version for GitHub action build ([#804](https://github.com/opendistro-for-elasticsearch/sql/pull/804))
* Fix unstable integration tests ([#793](https://github.com/opendistro-for-elasticsearch/sql/pull/793))
* Update cypress tests and increase delay time ([#792](https://github.com/opendistro-for-elasticsearch/sql/pull/792))

### Documentation
* Add doc for ODFE SQL demo ([#826](https://github.com/opendistro-for-elasticsearch/sql/pull/826))
* Update out-of-date documentation ([#820](https://github.com/opendistro-for-elasticsearch/sql/pull/820))

### Maintenance
* SQL release for Elasticsearch 7.10 ([#834](https://github.com/opendistro-for-elasticsearch/sql/pull/834))
* Migrate Query Workbench to new Platform ([#812](https://github.com/opendistro-for-elasticsearch/sql/pull/812))

