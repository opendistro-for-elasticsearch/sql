## 2020-10-16 Version 1.11.0.0

### Features
* Add support of basic data types byte, short, binary and add data type testing ([#780](https://github.com/opendistro-for-elasticsearch/sql/pull/780))
* Support ranking window functions ([#753](https://github.com/opendistro-for-elasticsearch/sql/pull/753))
* Add LogicalPlan optimization ([#763](https://github.com/opendistro-for-elasticsearch/sql/pull/763))
* Support DATE_FORMAT function ([#764](https://github.com/opendistro-for-elasticsearch/sql/pull/764))
* Support date and time function: week ([#757](https://github.com/opendistro-for-elasticsearch/sql/pull/757))
* Support aggregations min, max ([#541](https://github.com/opendistro-for-elasticsearch/sql/pull/541))
* Support date and time functions ([#746](https://github.com/opendistro-for-elasticsearch/sql/pull/746))

### Enhancements
* Support describe index alias ([#775](https://github.com/opendistro-for-elasticsearch/sql/pull/775))
* Restyle workbench SQL/PPL UI ([#781](https://github.com/opendistro-for-elasticsearch/sql/pull/781))
* UI Separate SQL and PPL pages ([#761](https://github.com/opendistro-for-elasticsearch/sql/pull/761))

### Bug Fixes
* Fix bug, support multiple aggregation ([#771](https://github.com/opendistro-for-elasticsearch/sql/pull/771))
* Fix cast statement in aggregate function return 0 ([#773](https://github.com/opendistro-for-elasticsearch/sql/pull/773))
* Bug fix, using Local.Root when format the string ([#767](https://github.com/opendistro-for-elasticsearch/sql/pull/767))
* Bug fix, order by doesn't work when group by field has alias ([#766](https://github.com/opendistro-for-elasticsearch/sql/pull/766))
* Bug fix, using matcher to compare the Json result ([#762](https://github.com/opendistro-for-elasticsearch/sql/pull/762))

### Infrastructure
* Enforce 100% branch coverage for sql and es module ([#774](https://github.com/opendistro-for-elasticsearch/sql/pull/774))

