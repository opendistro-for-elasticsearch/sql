# Elasticsearch ODBC Driver Build Instructions

The ElasticsearchODBC driver can be build on Windows and Mac.

## Setting up Dependencies

The driver [source code](https://github.com/opendistro-for-elasticsearch/sql-odbc) must be downloaded onto the system to build it.

### Windows Dependencies

Windows requires the following dependencies

* [cmake](https://cmake.org/install/)
* [Visual Studio 2019](https://visualstudio.microsoft.com/vs/) (Other versions may work, but only 2019 has been tested)

### Mac Dependencies

Homebrew must be installed to manage packages, to install homebrew see the [homebrew homepage](https://brew.sh/).
Using homebrew, install the following packages using the command provided:
>brew install [package]
>
>* curl
>* cmake
>* libiodbc

## Building the Driver

Before building the driver, the build files for the system must be generated, this is done using cmake.

### Providing AWS Credentials

As project uses AWS services for AWS SIGV4 authentication, you must [provide AWS credentials](https://docs.aws.amazon.com/sdk-for-cpp/v1/developer-guide/credentials.html). 

### Setting up AWS SDK 

#### Windows 
* Open Developer PowerShell for VS.
* Run aws_sdk_cpp_setup.ps1 script from the project's root directory.

#### Mac 
* Run aws_sdk_cpp_setup.sh script from the project's root directory.

### Generating the Build Files

Open the project's root directory in a command line interface of your choice. Execute
>**cmake ./src -D CMAKE_INSTALL_PREFIX=\<Project Directory>/AWSSDK/**

**Note:** It is desirable to not run cmake directly in the 'src' directory, because it will generate build files inline with code.

### General CMake Options

**BUILD_WITH_TESTS**

(Defaults to ON) If disabled, all tests and and test dependencies will be excluded from build which will optimize the installer package size. This option can set with the command line (using `-D`).

### Building with Windows

Building the driver on Windows is done using **Visual Studio**.
>Open **global_make_list.sln** with **Visual Studio 2019**.
>
>* Set the **Solution Configuration** to **Release**
>* Set the **Solution Platform** to **x64**
>
>**Build the solution** by right clicking it in the **Solution Explorer** and selecting **Build Solution**

### Building with Mac

Building the driver on Mac is done using make. Using the CLI, enter:
>**make**

## Output Files

Building the driver will yield the driver, tests, and a library files (Windows only).

### Output Location on Windows

Compiling on Windows will output the tests and the driver to **bin64/Release** and the driver library file to **lib64/Release** directory. There are also some additional test infrastructure files which output to the **bin64/Release** directory and the **lib64/Release** directory.

The driver can be consumed by linking to it using the library file (elasticodbc.lib in lib64/Release). BI tools can consume the driver by specifying the location of the dll (elasticodbc.dll in bin64/Release) in the [DSN](#setting-up-a-dsn).

### Output Location on Mac

Compiling on Mac will output the tests to **bin64** and the driver to **lib64**. There are also some additional test infrastructure files which output to the **lib64** directory.

## Packaging installer

Build the driver with `BUILD_WITH_TESTS` option disabled.

#### Windows

Open the project's build directory in Developer PowerShell for VS.
> msbuild .\PACKAGE.vcxproj -p:Configuration=Release

Installer named as `Open Distro for Elasticsearch SQL ODBC Driver-<version>-Windows.msi` will be generated in the build directory.

#### Mac

Run below command from the project's build directory.
>cpack .

Installer named as `Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg` will be generated in the build directory.

## Running Tests

Tests can be **executed directly**, or by using the **Test Runner**.

**NOTES:**

* A test DSN named `test_dsn` must be set up in order for certain tests in ITODBCConnection to pass. To configure the DSN, see the instructions, below.
* Datasets must be loaded into Elasticsearch using [kibana](https://www.elastic.co/guide/en/kibana/current/connect-to-elasticsearch.html). See the section on loading datasets below.

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

### Loading Test Datasets

Loading a dataset requires an [elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/) service running with [kibana](https://opendistro.github.io/for-elasticsearch-docs/docs/kibana/). If either of these are missing, please refer to the documentation on how to set them up.

Note, if you wish to work with SSL/TLS, you need to configure Elasticsearch and Kibana to support it. See Working With SSL/TLS below.

First load the sample datasets provided by kibana.

1. Select home (top left corner)
2. Select 'Load a data set and a Kibana dashboard'
3. Select 'Add data' under 'Sample flight data'
4. Select 'Add data' under 'Sample eCommerce orders'
5. Select 'Add data' under 'Sample web logs'

Then load custom data sets using the kibana console.
Select the wrench on the left control panel. Enter the following commands into the console and hit the play button after each one.

```json
PUT /kibana_sample_data_types
   {  
      "mappings": {  
         "properties": {  
            "type_boolean" : { "type": "boolean"},  
            "type_byte" : { "type": "byte"},  
            "type_short" : { "type": "short"},  
            "type_integer" : { "type": "integer"},  
            "type_long" : { "type": "long"},  
            "type_half_float" : { "type": "half_float"},  
            "type_float" : { "type": "float"},  
            "type_double" : { "type": "double"},  
            "type_scaled_float" : { "type": "scaled_float", "scaling_factor": 100 },  
            "type_keyword" : { "type": "keyword"},  
            "type_text" : { "type": "text"},  
            "type_date" : { "type": "date"},  
            "type_object" : { "type": "object"},  
            "type_nested" : { "type": "nested"}  
         }  
      }
   }
```

```json
POST /kibana_sample_data_types/_doc
{
   "type_boolean": true,
   "type_byte" : -120,
   "type_short" : -2000,
   "type_integer" :-350000000,
   "type_long" : -8010000000,
   "type_half_float" : -2.115,
   "type_float" : -3.1512,
   "type_double" : -5335.2215,
   "type_scaled_float" : -100.1,
   "type_keyword" : "goodbye",
   "type_text" : "planet",
   "type_date" : "2016-02-21T12:23:52.803Z",
   "type_object" : { "foo" : "bar" },
   "type_nested" : {"foo":"bar"}
}
```

```json
POST /kibana_sample_data_types/_doc  
{
   "type_boolean": false,
   "type_byte" : 100,
   "type_short" : 1000,
   "type_integer" : 250000000,
   "type_long" : 8000000000,
   "type_half_float" : 1.115,
   "type_float" : 2.1512,
   "type_double" : 25235.2215,
   "type_scaled_float" : 100,
   "type_keyword" : "hello",
   "type_text" : "world",
   "type_date" : "2018-07-22T12:23:52.803Z",
   "type_object" : { "foo" : "bar" },
   "type_nested" : {"foo":"bar"}
}
```

### Working With SSL/TLS

To disable SSL/TLS in the tests, the main CMakeLists.txt file must be edited. This can be found in the project 'src' directory. In the 'General compiler definitions' in the CMakeLists.txt file, USE_SSL is set. Remove this from the add_compile_definitions function to stop SSL/TLS from being used in the tests.

To enable SSL/TLS on Elasticsearch, you must edit the Elasticsearch.yml file, found in the config directory of Elasticsearch. An example Elasticsearch yml file can be found in the dev folder of this project. The certificates specified MUST be in the config directory of the Elasticsearch instance. For more information, please refer to the [Elasticsearch security settings documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/security-settings.html).

If you plan to use Kibana, as suggested for this project, you must also edit the Kibana settings. Notice, when specifying a certificate for Kibana, you do not need to place it in the Kibana config directory, but instead must provide the absolute path to it. An example Kibana.yml file can be found in the dev folder of this project. For more information, please refer to the [Kibana settings documentation](https://www.elastic.co/guide/en/kibana/current/settings.html).

### Running Tests directly on Windows

Tests can be executed directly using **Visual Studio** by setting the desired test as a **Start up Project**

>* **Right click** the desired test project in the **Solution Explorer**
>* Select **Set as Startup Project**
>* Run the test by selecting **Local Windows Debugger** in the toolbar at the top of the application

For more information, see the [Visual Studio Console Application documentation](https://docs.microsoft.com/en-us/cpp/build/vscpp-step-2-build?view=vs-2019).

### Running Tests directly on Mac

Tests can be executed using a command line interface. From the project root directory, enter:
> **bin64/<test_name>**

To execute a test.

### Running Tests using the Test Runner

The **Test Runner** requires [python](https://wiki.python.org/moin/BeginnersGuide/Download) to be installed on the system. Running the **Test Runner** will execute all the tests and compile a report with the results. The report indicates the execution status of all tests along with the execution time. To find error details of any failed test, hover over the test.

#### Running Tests using the Test Runner on Windows

Open the project's root directory in a command line interface of your choice. Execute
>**.\run_test_runner.bat**

The **Test Runner** has been tried and tested with [Python3.8](https://www.python.org/downloads/release/python-380/) on **Windows systems**. Other versions of Python may work, but are untested.

#### Running Tests using the Test Runner on Mac

Open the project's root directory in a command line interface of your choice. Execute
>**./run_test_runner.sh**

The **Test Runner** has been tried and tested with [Python3.7.6](https://www.python.org/downloads/release/python-376/) on **Mac systems**. Other versions of Python may work, but are untested.

### Running Tests with Coverage (Mac only)

(using a CMake script provided by George Cave (StableCoder) under the Apache 2.0 license, found [here](https://github.com/StableCoder/cmake-scripts/blob/master/code-coverage.cmake))

> **NOTE**: Before building with coverage, make sure the following directory is in your PATH environment variable:
> `/Applications/Xcode.app/Contents/Developer/Toolchains/XcodeDefault.xctoolchain/usr/bin`

To build the tests with code coverage enabled, set the `CODE_COVERAGE` variable to `ON` when preparing your CMake build.
```bash
cmake ... -DBUILD_WITH_TESTS=ON -DCODE_COVERAGE=ON
```

To get coverage for the driver library, you must use the `ccov-all` target, which runs all test suites and components with coverage.
```bash
make ccov-all
```

This will generate an HTML report at `<cmake-build-dir>/ccov/all-merged/index.html`, which can be opened in a web browser to view a summary of the overall code coverage, as well as line-by-line coverage for individual files.

For more information interpreting this report, see https://clang.llvm.org/docs/SourceBasedCodeCoverage.html#interpreting-reports.

## Setting up a DSN

A **D**ata **S**ouce **N**ame is used to store driver information in the system. By storing the information in the system, the information does not need to be specified each time the driver connects.

### Windows

> To setup DSN, add following keys in the Registry
>
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ ODBC/ODBC.INI** : Contains a key for each Data Source Name (DSN)
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ ODBC/ODBC.INI/ODBC Data Sources** : Lists the data sources
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBCINST.INI** :  Define each driver's name and setup location
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBCINST.INI/ODBC Drivers** : Lists the installed drivers.
>
>These keys can be added manually in the Registry Editor (Start > Run > Regedit) one by one. Alternatively, keys can be added together as follows:
>
>1. Modify the appropriate values for these keys in `src/IntegrationTests/ITODBCConnection/test_dsn.reg`
>2. Double click on the `test_dsn.reg` file.
>3. Click `Yes` on the confirmation window to add keys in the registry.

### Mac

**iODBC Administrator** can be used to setup a **DSN** on Mac.

> 1. Open **iODBC Administrator**
   >      * **iODBC Administrator** is installed with **iODBC Driver Manager** and can be found by searching the **Spotlight** (or found in **/Applications**)
> 2. Go to the **ODBC Drivers** tab
> 3. Click **Add a Driver**
   >      * **Description of the Driver**: The driver name used for the **ODBC connections** (ex. *ElasticsearchODBC*)
   >      * **Driver File Name**: The path to the **driver file** (*< Project Directory >/lib64/libelasticodbc.dylib*)
   >      * **Setup File Name**: The path to the **driver file** (*< Project Directory >/lib64/libelasticodbc.dylib*)
   >      * Set as a **User** driver
   >      * Select **OK** to save the options
> 4. Go to the **User DSN** tab
> 5. Select **Add**
   >      * Choose the driver that was added in **Step 3**
   >      * **Data Source Name (DSN)**: The name of the DSN used to store connection options (ex. *ElasticsearchODBC*)
   >      * **Comment**: Not required
   >      * Add the following **key-value pairs** using the **'+'** button
   >         * **Host** | **localhost** // Or a different server endpoint
   >         * **Port** | **9200** // Or whatever your endpoints port is
   >         * **Username** | **admin** // Or whatever your endpoints username is
   >         * **Password** | **admin** // Or whatever your endpoints password is
   >      * Select **OK** to **save options**
> 6. Select **OK** to exit the **Administrator**

If “General installer error” is encountered when saving the ODBC Driver, see Troubleshooting, below.

## Working with Tableau

[Tableau Desktop](https://www.tableau.com/products/desktop) must be installed on the target machine.

   1. Open **Tableau Desktop**
   2. Select **More…**
   3. Select **Other Databases (ODBC)**
   4. In the **DSN drop-down**, select the *Elasticsearch DSN* you set up in the previous set of steps
   5. The options you added will *automatically* be filled into the **Connection Attributes**
   6. Select **Sign In**
   7. After a few seconds, Tableau will connect to your Elasticsearch server

## Troubleshooting

### iODBC Administrator: “General installer error” when saving new ODBC Driver

Try the following:

1. Create the folder ~/Library/ODBC, then try again
2. Create two files in ~/Library/ODBC, then open iODBC Administrator and verify the contents of **odbcinst.ini** and **odbc.ini** align with the format below.
   * **odbcinst.ini** (will be found in **ODBC Drivers**)
      >[ODBC Drivers]
      \<Driver Name> = Installed
      >
      >[\<Driver Name>]
      Driver = \<Project Directory>/lib64/libelasticodbc.dylib
      Setup = \<Project Directory>/lib64/libelasticodbc.dylib

   * **odbc.ini** (will be found in **User DSNs**)
      >[ODBC Data Sources]  
       \<DSN Name> = \<Driver Name>
      >
      >[\<DSN Name>]
      Driver = \<Project Directory>/lib64/libelasticodbc.dylib
      Description =
      Host = localhost
      Port = 9200
      Username = admin
      Password = admin
