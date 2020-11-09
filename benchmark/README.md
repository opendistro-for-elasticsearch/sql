# Benchmark

## Supported Operating System

* Ubuntu 20.4 LTS

## Run the benchmarking test

* Get the source code of benchmark.
* Run the [configuration script](#running-the-configuration-script).
* Configure the [`config.json`](#configuring-the-inputs). Please make sure to update the values of `systemPassword`, `mysqlUsername` and `mysqlPassword`.
* Build the project.
* Run `BenchmarkServiceTest`.

## Running the configuration script

The script will install Open Distro for Elasticsearch, MySQL, Cassandra, and all prerequisites.

* Open the benchmark folder in the terminal and run the configuration script.

```
bash scripts/configure-environment.sh
```

* Follow the guidelines until installation is complete. 

## Configuring the inputs

* You can configure following options in `/benchmark/config.json`

| Option | Description | Default Value |
| --- | --- | --- |
| types | List of databases to test | cassandra,mysql,elasticsearch|
| outputFile | Name of the result's file | index.html |
| scaleFactors | List of scale factors for dataset (1.0 indicates 1 GB) | 1.0 |
| systemPassword | Password of the system | password |
| mysqlUsername | Username for connecting to MySQL | root|
| mysqlPassword | Password for connecting to MySQL | password |

**NOTE**: Default username and password `admin` is used for connecting to Open Distro For Elasticsearch SQL.

## Troubleshooting

* If you get NoHostAvailableException for cassandra, rerun the test.

* If you get Access Denied for MySQL, make sure user has all privileges.

`ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';`