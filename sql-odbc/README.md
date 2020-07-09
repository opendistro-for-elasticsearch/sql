# Open Distro for Elasticsearch ODBC Driver

ElasticsearchODBC is a read-only ODBC driver for Windows and Mac for connecting to Open Distro for Elasticsearch SQL support.

## Specifications

The driver is compatible with ODBC 3.51.

## Supported Versions


  | Operating System  | Version | Supported Bitness |
  | ------------- |-------------| ----------------- |
  |  Windows    |  Windows 10   | 32-bit, 64-bit |
  |  MacOS    |  Catalina 10.15.4, Mojave 10.14.6 | 64-bit |

## Installing the Driver

You can use the installers generated as part of the most recent release.

### Windows

1. Run the `.msi` installer to install the Open Distro for Elasticsearch SQL ODBC Driver.

To use the driver with Tableau:
1. Copy the `.tdc` file from `<driver-install-dir>/resources` to `<windows-user-dir>/Documents/My Tableau Repository/Datasources`.

This will customize the connection from Tableau to Open Distro for Elasticsearch, ensuring that the correct forms of queries are used. 

### Mac

iODBC Driver Manager should be installed before installing the Open Distro for Elasticsearch SQL ODBC Driver on Mac.

1. Run the `.pkg` installer to install the Open Distro for Elasticsearch SQL ODBC Driver.
2. Configure a Driver and DSN entry for the Open Distro for Elasticsearch SQL ODBC Driver, following the instructions [here](./docs/user/mac_configure_dsn.md).

To use the driver with Tableau:
1. Copy the `.tdc` file from `<driver-install-dir>/resources` to `<mac-user-dir>/Documents/My Tableau Repository/Datasources`.

This will customize the connection from Tableau to Open Distro for Elasticsearch, ensuring that the correct forms of queries are used.

## Using the Driver

The driver comes in the form of a library file:
* Windows: `odfesqlodbc.dll`
* Mac: `libodfesqlodbc.dylib`

If using with ODBC compatible BI tools, refer to the tool documentation on configuring a new ODBC driver. In most cases, you will need to make the tool aware of the location of the driver library file and then use it to setup Open Distro for Elasticsearch database connections.

### Connection Strings and Configuring the Driver

A list of options available for configuring driver behaviour is available [here](./docs/user/configuration_options.md).

To setup a connection, the driver uses an ODBC connection string. Connection strings are semicolon-delimited strings specifying the set of options to use for a connection. Typically, a connection string will either:

1. specify a Data Source Name containing a pre-configured set of options (`DSN=xxx;User=xxx;Password=xxx;`)
2. or configure options explicitly using the string (`Host=xxx;Port=xxx;LogLevel=ES_DEBUG;...`)

## Building from source

### Building

Please refer to the [build instructions](./BUILD_INSTRUCTIONS.md) for detailed build instructions on your platform.
If your PC is already setup to build the library, you can simply invoke cmake using

> cmake ./src

From the projects root directory, then build the project using Visual Studio (Windows) or make (Mac). 

* Visual Studio: Open **./global_make_list.sln**
* Make: Run `make` from the build root.

### Testing

**NOTE**: Some tests in ITODBCConnection will fail if a test DSN (Data Source Name) is not configured on your system. Refer to "Running Tests" in the [build instructions](./BUILD_INSTRUCTIONS.md) for more information on configuring this.

## Documentation

Please refer to the [documentation](https://opendistro.github.io/for-elasticsearch-docs/) for detailed information on installing and configuring Open Distro for Elasticsearch.

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](https://opendistro.github.io/for-elasticsearch/codeofconduct.html).

## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.

## Licensing

See the [LICENSE](./LICENSE) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
