## 2020-05-04 Version 1.7.0.0

This is the first official release of Open Distro for Elasticsearch SQL CLI

ODFE SQL CLI is a stand alone Python application and can be launched by a wake word `odfesql`. It serves as a support only for 
[Open Distro SQL plugin for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/). User must have ODFE SQL
plugin installed to the Elasticsearch instance for connection. Usr can run this CLI from MacOS and Linux, and connect to any valid Elasticsearch 
endpoint such as AWS Elasticsearch.

### Features
#### CLI Features
* Feature [#12](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/12): Initial development for SQL CLI
    * prototype launch: app -> check connection -> take input -> query ES -> serve raw results(format=jdbc)
    * enrich auto-completion corpus
    * Convert to vertical output format if fields length is larger than terminal window
    * Add style to output fields name. Add logic to confirm choice from user for vertical output
    * Add single query without getting into console. Integrate "_explain" api
    * Add config base logic. Add pagination for long output
    * Add nice little welcome banner.
    * Add params -f for format_output (jdbc/raw/csv), -v for vertical display
    * Initial implementation of connection to OD cluster and AES with auth
    * Create test module and write first test
    * Add fake test data. Add test utils to set up connection
    * [Test] Add pagination test and query test
    * Add Test plan and dependency list
    * [Test] Add test case for ConnectionFailExeption
    * [Feature] initial implementation of index suggestion during auto-completion
    * [Feature] display (data retrieved / total hits), and tell user to use "limit" to get more than 200 lines of data
    * Added legal and copyright files,
    * Added THIRD PARTY file
    * Added setup.py for packaging and releasing
* Feature [#24](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/24): Provide user option to toggle to use AWS sigV4 authentication 
(issue: [#23](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/23))

#### Testing
* Feature [#28](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/28) :Added tox scripts for testing automation

#### Documentation
* Change [#22](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/22): Update documentation and CLI naming 
(issues: [#21](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/21), [#7](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/17))
* Change [#32](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/32): Update copyright to 2020
* Change [#33](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/33): Updated package naming and created folder for release notes
* Change [#34](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/34): Added CONTRIBUTORS.md
* Change [#36](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/36): Polish README.md and test_plan.md


### Enhancements
* Enhancement [#31](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/31): Added github action workflow for CI/CD
(issue: [#20](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/21))
* Enhancement [#35](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/35): Update github action test and build workflow to spin up ES instance


### BugFixes
* BugFix[#12](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/12): Initial development for SQL CLI
    * Fix the logic of passing self-constructed settings
    * [Fix] get rid of unicode warning. Fix meta info display
    * [fix] Refactor executor code
    * [Fix] Fix test cases corresponding to fraction display.
    * [Fix] fix code style using Black, update documentation and comments
* BugFix[#18](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/18): Fix typos, remove unused dependencies, add .gitignore and legal file
(issue: [#15](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/15))
* BugFix[#19](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/19): Fix test failures 
(issue: [#16](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/16))
* BugFix[#26](https://github.com/opendistro-for-elasticsearch/sql-cli/pull/26): Update usage gif, fix http/https issue when connect to AWS Elasticsearch (issue: [#25](https://github.com/opendistro-for-elasticsearch/sql-cli/issues/25))


