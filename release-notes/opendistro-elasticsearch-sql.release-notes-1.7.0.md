## 2020-05-04 Version 1.7.0.0

#### Breaking Changes
* Change [#414](https://github.com/opendistro-for-elasticsearch/sql/pull/414): Invalidate HTTP GET method.

### Features
#### SQL Features
* Feature [#390](https://github.com/opendistro-for-elasticsearch/sql/pull/390): Simple Query Cursor support. (issue: [#16](https://github.com/opendistro-for-elasticsearch/sql/issues/16))
* Feature [#400](https://github.com/opendistro-for-elasticsearch/sql/pull/400): New SQL cluster settings endpoint.
#### Documentation
* Feature [#417](https://github.com/opendistro-for-elasticsearch/sql/pull/417): More docs in reference manual and add architecture doc. (issue: [#380](https://github.com/opendistro-for-elasticsearch/sql/issues/380)) 
#### Security
* Change [#456](https://github.com/opendistro-for-elasticsearch/sql/pull/456): Escape comma for CSV header and all queries. (issue: [#455](https://github.com/opendistro-for-elasticsearch/sql/issues/455))
* Change [#447](https://github.com/opendistro-for-elasticsearch/sql/pull/447): Fix CSV injection issue. (issue: [#449](https://github.com/opendistro-for-elasticsearch/sql/issues/449))
* Change [#419](https://github.com/opendistro-for-elasticsearch/sql/pull/419): Anonymize sensitive data in queries exposed to RestSqlAction logs. (issue: [#97](https://github.com/opendistro-for-elasticsearch/sql/issues/97))

### Bugfixes
* Bugfix [#452](https://github.com/opendistro-for-elasticsearch/sql/pull/452): Support using aggregation function in order by clause. (issue: [#277](https://github.com/opendistro-for-elasticsearch/sql/issues/277))
* Bugfix [#442](https://github.com/opendistro-for-elasticsearch/sql/pull/442): Count(distinct field) should translate to cardinality aggregation. (issue: [#439](https://github.com/opendistro-for-elasticsearch/sql/issues/439))
* Bugfix [#437](https://github.com/opendistro-for-elasticsearch/sql/pull/437): Enforce AVG return double data type. (issue: [#408](https://github.com/opendistro-for-elasticsearch/sql/issues/408))
* Bugfix [#425](https://github.com/opendistro-for-elasticsearch/sql/pull/425): Ignore the term query rewrite if there is no index found. (issue: [#355](https://github.com/opendistro-for-elasticsearch/sql/issues/355))
* Bugfix [#418](https://github.com/opendistro-for-elasticsearch/sql/pull/418): Support subquery in from doesn't have alias. (issue: [#416](https://github.com/opendistro-for-elasticsearch/sql/issues/416))
* Bugfix [#412](https://github.com/opendistro-for-elasticsearch/sql/pull/412): Add support for strict_date_optional_time. (issue: [#411](https://github.com/opendistro-for-elasticsearch/sql/issues/411))
* Bugfix [#381](https://github.com/opendistro-for-elasticsearch/sql/pull/381): field function name letter case preserved in select with group by. (issue: [#373](https://github.com/opendistro-for-elasticsearch/sql/issues/373))
