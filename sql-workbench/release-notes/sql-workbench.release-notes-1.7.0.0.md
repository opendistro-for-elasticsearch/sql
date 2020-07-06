## 2020-05-04 Version 1.7.0.0 

### Initial Release

This is the first official release of Open Distro SQL Workbench.

To use the SQL Workbench, you will need the [Open Distro SQL plugin](https://github.com/opendistro-for-elasticsearch/sql). This plugin you will be able to write SQL queries from a console UI directly through Kibana.


### Features

- Initialize SQL Kibana plugin ([#1](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/1))
- SQL Kibana plugin functionality implementation ([#2](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/2))
- Update plugin name and adjust dependency version ([#3](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/3))
- Added kibana plugin helper configuration ([#4](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/4))
- Adjust the service routes to call SQL plugin ([#5](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/5))
- Bumped up the version to support v7.3 compatibility ([#6](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/6))
- Updated configureation for v7.3 compatibility ([#7](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/7))
- Opendistro-1.3 compatible with ES and Kibana 7.3 ([#8](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/8))
- Changed kibana version to v7.3.2 ([#9](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/9))
- Support v7.1.1 Compatibility ([#13](v13))
- Bump pr-branch to v1.3 ([#21](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/21))
- Support v7.4 compatibility for kibana and sql-plugin ([#23](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/23))
- Improve the performance by ridding of sending redundant requests ([#24](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/24))
- Added raw format in download options ([#25](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/25))
- Update the release notes for the first official release ([#27](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/27))
- Updated test cases and snapshots ([#28](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/28))
- Initial merge from development branches to master ([#31](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/31))
- Support the result table to display results of DESCRIBE/SHOW/DELETE queries ([#37](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/37))
- Adjust the appearance of SQL Workbench UI ([#38](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/38))
- Migrated the default request format from Json to JDBC ([#41](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/41))
- Support v7.6.1 compatibility ([#42](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/42))
- Removed the overriding settings in css ([#44](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/44))
- Updated outdated dependencies ([#47](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/47))
- Updated open source code compliance ([#48](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/48))
- Opendistro Release v1.7.0 ([#56](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/56))
- Set up workflows for testing, building artifacts and releasing ([#58](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/58))
- Update copyright from year 2019 to 2020 ([#59](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/59))
- Moved release notes to dedicated folder ([#60](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/60))
- Added contributors file ([#61](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/61))
- Updated lastest yarn.lock for accurate dependency alert ([#62](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/62))
- Updated snapshot ([#63](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/63))

### Bug Fixes

- Fixed the issue that response failed to be delivered in the console ([#10](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/10))
- Fixed the issue that aggregation result not delivered in the table ([#26](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/26))
- Fixed the issue that the table remains delivering the cached results table from the last query ([#30](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/30))
- Fixed some dependency issues ([#36](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/36))
- Fixed: fatal errors occur when import @elastic/eui modules in docker images ([#43](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/43))
- BugFix: Corrected the downloaded .csv/.txt files to proper format ([#50](https://github.com/opendistro-for-elasticsearch/sql-workbench/pull/50))
