# Benchmark

## Supported Operating System

* Ubuntu 20.4 LTS

## Prerequisites

* Git 2.0 or later
* Gradle 6.5 or later

## Run the benchmarking test

* Get the source code of benchmark.
```
git clone https://github.com/opendistro-for-elasticsearch/sql.git
```
* Run the [configuration script](#running-the-configuration-script).
* [Configure the inputs](#configuring-the-inputs) using `config.json`. Please make sure to update the values of `systemPassword`, `mysqlUsername` and `mysqlPassword`.
* Verify if you can connect to MySQL using above credentials.
```
mysql -u root -p 
```
* Run BenchmarkServiceTest: (`clean` ensures that the tests are run with a fresh environment)
```
./gradlew -Pbenchmark clean benchmark:test --tests BenchmarkServiceTest.runbenchmarkService
```

## Run the configuration script

The script will install Open Distro for Elasticsearch, MySQL, Cassandra, and all prerequisites.

* Open the benchmark folder in the terminal and run the configuration script, providing the Elasticsearch version
  you want to test as an argument. 

```
bash scripts/configure-environment.sh <elasticsearch-version>
```

* Follow the guidelines until installation is complete. 

## Configure the inputs

* You can configure following options in `/benchmark/config.json`

| Option | Description | Default Value |
| --- | --- | --- |
| types | List of databases to test. The acceptable values are elasticsearch, mysql and cassandra. | cassandra,mysql,elasticsearch|
| outputFile | Name of the result's file | index.html |
| scaleFactors | List of fractional scale factors for dataset (1.0 indicates 1 GB). The maximum limit is 100000. | 0.1 |
| systemPassword | Password of the system | password |
| mysqlUsername | Username for connecting to MySQL | root|
| mysqlPassword | Password for connecting to MySQL | password |

**NOTE**: Default username and password `admin` is used for connecting to Open Distro For Elasticsearch SQL.

## Troubleshooting

* "Cannot set maxParallelForks to a value less than 1."
  * Add `-Dtests.jvms=n` to the test command (where n is 1 ≤ n ≤ # of processor cores.) 

* If you get NoHostAvailableException for Cassandra, rerun the test.

* If you get Access Denied for MySQL, make sure user has all privileges.

`ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';`