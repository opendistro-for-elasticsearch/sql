## 2020-09-08 Version 1.10.1.0

### Features
* Support Top/Rare Command In PPL ([#720](https://github.com/opendistro-for-elasticsearch/sql/pull/720))
* support Elasticsearch geo_point and ip data type ([#719](https://github.com/opendistro-for-elasticsearch/sql/pull/719))
* add serialization support for expression ([#712](https://github.com/opendistro-for-elasticsearch/sql/pull/712))
* Optimize filter expression script ([#707](https://github.com/opendistro-for-elasticsearch/sql/pull/707))
* Add PPL stats endpoint ([#706](https://github.com/opendistro-for-elasticsearch/sql/pull/706))
* Support WHERE clause in new SQL parser ([#682](https://github.com/opendistro-for-elasticsearch/sql/pull/682))
* Add Cypress testing for SQL Workbench ([#562](https://github.com/opendistro-for-elasticsearch/sql/pull/562))
* Lucene query pushdown optimization ([#671](https://github.com/opendistro-for-elasticsearch/sql/pull/671))
* ODBC: Add PBIDS support ([#676](https://github.com/opendistro-for-elasticsearch/sql/pull/676))
* Add PPL enable/disable setting ([#681](https://github.com/opendistro-for-elasticsearch/sql/pull/681))
* Add query size limit ([#679](https://github.com/opendistro-for-elasticsearch/sql/pull/679)
* Expression pushdown optimization ([#663](https://github.com/opendistro-for-elasticsearch/sql/pull/663))
* changes required for using to Power BI Service with Open Distro For Elasticsearch ([#669](https://github.com/opendistro-for-elasticsearch/sql/pull/669))
* Support NULL and MISSING value in response ([#667](https://github.com/opendistro-for-elasticsearch/sql/pull/667))
* ODBC: Use literals instead of parameters in Power BI data connector ([#652](https://github.com/opendistro-for-elasticsearch/sql/pull/652))
* Support select fields and alias in new query engine ([#636](https://github.com/opendistro-for-elasticsearch/sql/pull/636))
* Add comparision operator for SQL ([#635](https://github.com/opendistro-for-elasticsearch/sql/pull/635))

### Enhancements
* Parse backtick strings (``) as identifiers instead of string literals ([#678](https://github.com/opendistro-for-elasticsearch/sql/pull/678))
* add error details for all server communication errors ([#645](https://github.com/opendistro-for-elasticsearch/sql/pull/645))

### Bug Fixes
* Fixed sql workbench loading issue ([#723](https://github.com/opendistro-for-elasticsearch/sql/pull/723))
* ODBC: Fix Windows 64-bit workflows ([#703](https://github.com/opendistro-for-elasticsearch/sql/pull/703))
* Fix for query folding issue while applying filter in PBID ([#666](https://github.com/opendistro-for-elasticsearch/sql/pull/666))
* Fix for query folding issue with direct query mode in Power BI data connector ([#640](https://github.com/opendistro-for-elasticsearch/sql/pull/640))

### Infrastructure
* Changed rpm and deb artifact name ([#705](https://github.com/opendistro-for-elasticsearch/sql/pull/705))
* Adjust release drafter to follow ODFE standards ([#700](https://github.com/opendistro-for-elasticsearch/sql/pull/700))
* ODBC: improve Windows build process ([#661](https://github.com/opendistro-for-elasticsearch/sql/pull/661))
* Skip doctest in github release actions ([#648](https://github.com/opendistro-for-elasticsearch/sql/pull/648))

### Documentation
* update user documentation for testing odbc driver connection on windows([#722](https://github.com/opendistro-for-elasticsearch/sql/pull/722))
* Added workaround for identifiers with special characters in troubleshooting page([#718](https://github.com/opendistro-for-elasticsearch/sql/pull/718))
* Update release notes for OD 1.10.1 release([#699](https://github.com/opendistro-for-elasticsearch/sql/pull/699))

### Maintenance
* Bump ES and Kibana to 7.9.1 and release ODFE 1.10.1.0 ([#732](https://github.com/opendistro-for-elasticsearch/sql/pull/732))
