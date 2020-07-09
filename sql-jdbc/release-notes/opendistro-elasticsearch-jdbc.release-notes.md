## 2020-04-29, Version 1.6.1.0
### Enhancement
* Enhancement [#72](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/72): Use default holdability for prepareStatement. (issue: [#63](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/63))

## 2020-03-24, Version 1.6.0.0
### Enhancement
* Enhancement [#49](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/49): Implementation of the execute method in the PreparedStatementImpl class (issue: [#62](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/62))

### Bugfix
* BugFix [#68](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/68): Change the request body encoding to UTF-8 (issues: [#54](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/54), [#66](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/66), [#opendistro for elasticsearch/sql#392](https://github.com/opendistro-for-elasticsearch/sql/issues/392) )

## 2020-1-26, Version 1.4.0

### Features

#### Documentation
* Feature [#37](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/37): Tableau documentation
* Feature [#35](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/35): Add documentation for connecting Tableau with OpenDistro for Elasticsearch using JDBC Driver

### Bugfixes
* BugFix [#47](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/47): Result set metadata returns Elasticsearch type (issue: [#43](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/43))
* BugFix [#45](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/45): Add missing Elasticsearch type : object (issue: [#44](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/43))
* BugFix [#32](https://github.com/opendistro-for-elasticsearch/sql-jdbc/pull/32): Added IP type and mapped with JDBC type of varchar

## 2019-10-29, Version 1.3.0

### Changes

* Elasticsearch 7.3.2 compatibility
* BugFix: support negative float

## 2019-08-16, Version 1.2.0

### Changes

* Elasticsearch 7.2.0 compatibility
* Support for custom AWS Credentials providers

## 2019-06-24, Version 1.1.0 

### Changes

* Elasticsearch 7.1.1 compatibility

## 2019-06-06, Version 1.0.0

### Changes

* Updated the LocalDateTime to Timestamp conversion to support timezone [issue #6]
* Updated the connection URL template in README.md

## 2019-04-19, Version 0.9.0

No update in this release.


## 2019-04-02, Version 0.8.0

### Notable Changes

* Feature [#4](https://github.com/opendistro-for-elasticsearch/sql-jdbc/issues/4): Add support for Elasticsearch 6.6


## 2019-03-11, Version 0.7.0

### Notable Changes

This is the first release of OpenES-JDBC.

OpenES-JDBC provides a driver for JDBC connectivity for OpenES-SQL. The driver has been developed from scratch and offers the following features in this initial release:

* JDBC API implementation as per JDBC 4.2 specifications
* java.sql.DriverManager and javax.sql.DataSource interface implementation for creating JDBC connections to Elasticsearch clusters running with OpenES-SQL plugin
* java.sql.Statement implementation to allow creation and submission of SQL queries to OpenES-SQL
* java.sql.ResultSet and java.sql.ResultSetMetadata implementation for parsing query results
* java.sql.PreparedStatement implementation for creation and submission of parameterized SQL queries to OpenES-SQL
* Support for HTTP BASIC and AWS SIGV4 authentication mechanisms
* Full support for Elasticsearch Datatypes: BOOLEAN, BYTE, SHORT, INTEGER, LONG, HALF_FLOAT, FLOAT, DOUBLE, SCALED_FLOAT, KEYWORD, TEXT
* Support Elasticsearch DATE data type with some limitations


### Commits
The code has been developed from scratch so their are numerous commits over the course of development work. 
A single squash commit shall be created for the first release.
