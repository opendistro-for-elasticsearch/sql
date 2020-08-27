# ODFE SQL ODBC Driver Build Instructions

## Windows

### Dependencies

* [cmake](https://cmake.org/install/)
* [Visual Studio 2019](https://visualstudio.microsoft.com/vs/) (Other versions may work, but only 2019 has been tested)
* [ODBC Driver source code](https://github.com/opendistro-for-elasticsearch/sql/tree/master/sql-odbc)

### Build

#### with Visual Studio

Run `./build_win_<config><bitness>.ps1` to generate a VS2019 project for building/testing the driver. (the build may fail, but should still generate a `.sln` file)

The solution can be found at `<odbc-root>\build\odbc\build\global_make_list.sln`.

#### with Developer Powershell

Use `./build_win_<config><bitness>.ps1` to build the driver from a Developer Powershell prompt.

> A shortcut is installed on your system with Visual Studio (search for **"Developer Powershell for VS 2019"**)

> Programs launched with this prompt (ex: VS Code) also have access to the Developer shell.

### Build Output

```
build
└-- <Configuration><Bitness>
  └-- odbc
    └-- bin
      └-- <Configuration>
    └-- build
    └-- lib
  └-- aws-sdk
    └-- build
    └-- install
```

* Driver DLL: `.\build\<Config><Bitness>\odbc\bin\<Config>\odfesqlodbc.dll`
* Test binaries folder: `.\build\<Config><Bitness>\odbc\bin\<Config>`

### Packaging

From a Developer Powershell, run:
```
msbuild .\build\Release<Bitness>\odbc\PACKAGE.vcxproj -p:Configuration=Release
```

An installer named as `Open Distro for Elasticsearch SQL ODBC Driver-<version>-Windows-<Bitness>-bit.msi` will be generated in the build directory.


## Mac
(TODO: upgrade build scripts & documentation for Mac)

### Dependencies

Homebrew must be installed to manage packages, to install homebrew see the [homebrew homepage](https://brew.sh/).
Using homebrew, install the following packages using the command provided:
>brew install [package]
>
>* curl
>* cmake
>* libiodbc

### Building the Driver

From a Bash shell:

`./build_mac_<config><bitness>.sh`

### Output Location on Mac

Compiling on Mac will output the tests to **bin64** and the driver to **lib64**. There are also some additional test infrastructure files which output to the **lib64** directory.

### Packaging

Run below command from the project's build directory.
>cpack .

Installer named as `Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg` will be generated in the build directory.

## General Build Info

### ODBC Driver CMake Options

**BUILD_WITH_TESTS**

(Defaults to ON) If disabled, all tests and and test dependencies will be excluded from build which will optimize the installer package size. This option can set with the command line (using `-D`).

### Working With SSL/TLS

To disable SSL/TLS in the tests, the main CMakeLists.txt file must be edited. This can be found in the project 'src' directory. In the 'General compiler definitions' in the CMakeLists.txt file, USE_SSL is set. Remove this from the add_compile_definitions function to stop SSL/TLS from being used in the tests.

To enable SSL/TLS on Elasticsearch, you must edit the Elasticsearch.yml file, found in the config directory of Elasticsearch. An example Elasticsearch yml file can be found in the dev folder of this project. The certificates specified MUST be in the config directory of the Elasticsearch instance. For more information, please refer to the [Elasticsearch security settings documentation](https://www.elastic.co/guide/en/elasticsearch/reference/current/security-settings.html).

If you plan to use Kibana, as suggested for this project, you must also edit the Kibana settings. Notice, when specifying a certificate for Kibana, you do not need to place it in the Kibana config directory, but instead must provide the absolute path to it. An example Kibana.yml file can be found in the dev folder of this project. For more information, please refer to the [Kibana settings documentation](https://www.elastic.co/guide/en/kibana/current/settings.html).

### Setting up a DSN

A **D**ata **S**ouce **N**ame is used to store driver information in the system. By storing the information in the system, the information does not need to be specified each time the driver connects.

#### Windows

> To setup DSN, add following keys in the Registry
>
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBC.INI** : Contains a key for each Data Source Name (DSN)
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBC.INI/ODBC Data Sources** : Lists the data sources
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBCINST.INI** :  Define each driver's name and setup location
   >* **HKEY_LOCAL_MACHINE/SOFTWARE/ODBC/ODBCINST.INI/ODBC Drivers** : Lists the installed drivers.
>
>These keys can be added manually in the Registry Editor (Start > Run > Regedit) one by one. Alternatively, keys can be added together as follows:
>
>1. Modify the appropriate values for these keys in `src/IntegrationTests/ITODBCConnection/test_dsn.reg`
>2. Double click on the `test_dsn.reg` file.
>3. Click `Yes` on the confirmation window to add keys in the registry.
