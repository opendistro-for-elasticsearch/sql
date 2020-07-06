## 2020-05-05, Version 1.7

This is the first release of OpenDistro For ELasticsearch ODBC driver.

OpenDistro ODBC provides a driver for ODBC connectivity for OpenDistro SQL plugin. The driver has been developed from scratch and offers the following features in this initial release:

* ODBC API implementation as per ODBC 3.51 specifications
* Support for MacOS and Windows installers
* Support for HTTP BASIC and AWS SIGV4 authentication mechanisms
* Full support for Elasticsearch Datatypes: BOOLEAN, BYTE, SHORT, INTEGER, LONG, HALF_FLOAT, FLOAT, DOUBLE, SCALED_FLOAT, KEYWORD, TEXT

### Features

* Feature[#7](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/7): Add support for connection string abbreviations

* Feature[#2](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/2): Connection string refactoring and registry updates

* Feature[#27](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/27): Simple Windows Installer


* Feature[#78](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/78): Add fetch_size for pagination support


### Documentation

* [Pagination support design document](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/40) 
* [Update README for authentication & encryption configuration options](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/33) 
* [Instructions for signing installers](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/84) 



### BugFixes

* [Fix AWS authentication for Tableau on Mac](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/9) 

* [Mac installer fixes](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/73) 


* [Fix General installer components](https://github.com/opendistro-for-elasticsearch/sql-odbc/pull/69) 









