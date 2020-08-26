## 2020-07-29 Version 1.9.0.1

### Feature
* ODBC: Change Tableau connector version, support Tableau and Excel ([#622](https://github.com/opendistro-for-elasticsearch/sql/pull/622))
* Support trignometric functions acos, asin, atan, atan2, cos, cot, degrees, radians, sin, tan ([#599](https://github.com/opendistro-for-elasticsearch/sql/pull/599))
* Support mathmatical functions rand and constants e, pi ([#591](https://github.com/opendistro-for-elasticsearch/sql/pull/591))
* Support SELET * and FROM clause in new SQL parser ([#573](https://github.com/opendistro-for-elasticsearch/sql/pull/573))
* Support mathmatical functions: conv, crc32, mod, pow/power, round, sign, sqrt, truncate ([#577](https://github.com/opendistro-for-elasticsearch/sql/pull/577))
* add date and time support ([#568](https://github.com/opendistro-for-elasticsearch/sql/pull/568))
* Support mathematical functions ceil/ceiling, exp, floor, ln, log ([#540](https://github.com/opendistro-for-elasticsearch/sql/pull/540))

### Enhancement
* Support queres end with semi colon ([#609](https://github.com/opendistro-for-elasticsearch/sql/pull/609))
* ODBC: Adding BASIC & AWS_SIGV4 auth in M Connector  ([#610](https://github.com/opendistro-for-elasticsearch/sql/pull/610))
* ODBC: adding manual test plan for Microsoft Excel testing ([#604](https://github.com/opendistro-for-elasticsearch/sql/pull/604))
* ODBC: Report error from Excel when executing an invalid query ([#611](https://github.com/opendistro-for-elasticsearch/sql/pull/611))
* Add ElasticsarchExprValueFactory in StorageEngine ([#608](https://github.com/opendistro-for-elasticsearch/sql/pull/608))
* Using UTC asdefault timezone for date_format function if not provided ([#605](https://github.com/opendistro-for-elasticsearch/sql/pull/605))
* ODBC: AddingPower BI M connector  ([#596](https://github.com/opendistro-for-elasticsearch/sql/pull/596))
* ODBC: add ODC 2.x functions called by Excel for Mac ([#592](https://github.com/opendistro-for-elasticsearch/sql/pull/592))
* ODBC: Remove catalog support from driver ([#566](https://github.com/opendistro-for-elasticsearch/sql/pull/566))
* ODBC: Build driver files in parallel ([#570](https://github.com/opendistro-for-elasticsearch/sql/pull/570))
* Keep mismatch results when error occurs in comparison test ([#557](https://github.com/opendistro-for-elasticsearch/sql/pull/557))

### Bug Fixes
* Move workbench down in kibana nav ([#578](https://github.com/opendistro-for-elasticsearch/sql/pull/578))
* ODBC: Fix fo data loading failure in Power BI Desktop ([#627](https://github.com/opendistro-for-elasticsearch/sql/pull/627))
* Issue 623, fix security vulnerability regarding to depedencies commons-codec and Guava ([#624](https://github.com/opendistro-for-elasticsearch/sql/pull/624))
* Extra fixes or Mac ODBC driver ([#602](https://github.com/opendistro-for-elasticsearch/sql/pull/602))
* Fix CAST boo field to integer issue ([#600](https://github.com/opendistro-for-elasticsearch/sql/pull/600))
* Bumped lodash version to fix dependency security vulnerability ([#598](https://github.com/opendistro-for-elasticsearch/sql/pull/598))
* Fix object/nsted field select issue ([#584](https://github.com/opendistro-for-elasticsearch/sql/pull/584))
* Remove columns from result which are not returned by SELECT * query but returned by DESCRIBE query ([#556](https://github.com/opendistro-for-elasticsearch/sql/pull/556))

### Infrastructure
* Add workflow to draft release on push ([#572](https://github.com/opendistro-for-elasticsearch/sql/pull/572))
* Merge all SQL repos and adjust workflows ([#549](https://github.com/opendistro-for-elasticsearch/sql/pull/549))

### Documentation
* Update docs fter merging repos ([#563](https://github.com/opendistro-for-elasticsearch/sql/pull/563))
* ODBC: Updatig Microsoft Excel connection documents ([#581](https://github.com/opendistro-for-elasticsearch/sql/pull/581))
* ODBC: Add usr documentation for using Microsoft Excel on Mac ([#594](https://github.com/opendistro-for-elasticsearch/sql/pull/594))
* ODBC: Update documentation for using Microsoft Excel with Open Distro For Elasticsearch ([#576](https://github.com/opendistro-for-elasticsearch/sql/pull/576))
* ODBC: Add douments for `Refresh` & `Export as CSV files` options in Microsoft Excel ([#571](https://github.com/opendistro-for-elasticsearch/sql/pull/571))
