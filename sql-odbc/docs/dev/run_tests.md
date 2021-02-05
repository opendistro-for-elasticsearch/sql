# ODFE SQL ODBC Driver Testing

## Requirements

* Latest version of [Open Distro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/)
* [Required datasets loaded](#set-up-test-datasets)
* [DSN configured](#set-up-dsn)

### Set up test datasets

Loading a dataset requires an [OpenDistro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/) service running with [Kibana](https://opendistro.github.io/for-elasticsearch-docs/docs/kibana/). If either of these are missing, please refer to the documentation on how to set them up.

Note, if you wish to work with SSL/TLS, you need to configure ODFE and Kibana to support it. See the [build instructions](./BUILD_INSTRUCTIONS.md) for more info.

First load the sample datasets provided by kibana.

1. Select home (top left corner)
2. Select 'Load a data set and a Kibana dashboard'
3. Select 'Add data' under 'Sample flight data'
4. Select 'Add data' under 'Sample eCommerce orders'
5. Select 'Add data' under 'Sample web logs'

Then load the following custom data sets using the kibana console.
Select the wrench on the left control panel. Enter the following commands into the console and hit the play button after each one.

* [kibana_sample_data_types](./datasets/kibana_sample_data_types.md)

### Set up DSN

A test DSN named `test_dsn` must be set up in order for certain tests in ITODBCConnection to pass. To configure the DSN, see the instructions below.

### Windows Test DSN Setup

1. Open `src/IntegrationTests/ITODBCConnection/test_dsn.reg`.
   * This contains the registry entries needed for setting up `test_dsn`.
2. Do one of the following:
   * As an Administrator, run a command prompt or Powershell and run `reg import <.reg-file>` to add the entries to your registry.
   * Manually add the entries to your registry using Registry Editor.

### Mac Test DSN Setup

1. Open `src/IntegrationTests/ITODBCConnection/test_odbc.ini` and `src/IntegrationTests/ITODBCConnection/test_odbcinst.ini`
   * These contain the minimal configuration necessary for setting up `test_dsn`.
2. Do one of the following:
   * Add the following lines to your .bash_profile to point the driver to these files.
      * `export ODBCINI=<project-dir>/src/IntegrationTests/ITODBCConnection/test_odbc.ini`
      * `export ODBCINSTINI=<project-dir>/src/IntegrationTests/ITODBCConnection/test_odbcinst.ini`
   * Manually add the entries to your existing `odbc.ini` and `odbcinst.ini` entries. (normally found at `~/.odbc.ini` and `~/.odbcinst.ini`)

## Running Tests
Tests can be executed directly, or by using the **Test Runner**.

### Direct

#### Windows

Tests can be executed directly using **Visual Studio** by setting the desired test as a **Start up Project**

>* **Right click** the desired test project in the **Solution Explorer**
>* Select **Set as Startup Project**
>* Run the test by selecting **Local Windows Debugger** in the toolbar at the top of the application

For more information, see the [Visual Studio Console Application documentation](https://docs.microsoft.com/en-us/cpp/build/vscpp-step-2-build?view=vs-2019).

#### Mac

Tests can be executed using a command line interface. From the project root directory, enter:
> **bin64/<test_name>**

### Test Runner

The **Test Runner** requires [python](https://wiki.python.org/moin/BeginnersGuide/Download) to be installed on the system. Running the **Test Runner** will execute all the tests and compile a report with the results. The report indicates the execution status of all tests along with the execution time. To find error details of any failed test, hover over the test.

#### Windows

Open the project's root directory in a command line interface of your choice. Execute
>**.\run_test_runner.bat**

The **Test Runner** has been tried and tested with [Python3.8](https://www.python.org/downloads/release/python-380/) on **Windows systems**. Other versions of Python may work, but are untested.

#### Mac

Open the project's root directory in a command line interface of your choice. Execute
>**./run_test_runner.sh**

The **Test Runner** has been tried and tested with [Python3.7.6](https://www.python.org/downloads/release/python-376/) on **Mac systems**. Other versions of Python may work, but are untested.

### Run with Coverage (Mac only)

(using a CMake script provided by George Cave (StableCoder) under the Apache 2.0 license, found [here](https://github.com/StableCoder/cmake-scripts/blob/master/code-coverage.cmake))

#### Requirements
* `llvm-cov` in your PATH environment variable
   * Possible locations for this binary:
      * `/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin`
      * `/Library/Developer/CommandLineTools/usr/bin`

To build the tests with code coverage enabled, set the `CODE_COVERAGE` variable to `ON` when preparing your CMake build.
```bash
cmake -DBUILD_WITH_TESTS=ON -DCODE_COVERAGE=ON <rest-of-cmake-command>
```

To get coverage for the driver library, you must use the `ccov-all` target, which runs all test suites and components with coverage.
```bash
make ccov-all
```

This will generate an HTML report at `<cmake-build-dir>/ccov/all-merged/index.html`, which can be opened in a web browser to view a summary of the overall code coverage, as well as line-by-line coverage for individual files.

For more information interpreting this report, see https://clang.llvm.org/docs/SourceBasedCodeCoverage.html#interpreting-reports.

