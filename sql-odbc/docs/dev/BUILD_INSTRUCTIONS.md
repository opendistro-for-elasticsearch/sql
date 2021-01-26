# ODFE SQL ODBC Driver Build Instructions

## Windows

### Requirements

* Install [cmake](https://cmake.org/install/)
* Install [Visual Studio 2019](https://visualstudio.microsoft.com/vs/) 
  * Other versions may work, but only 2019 has been tested

**NOTE:** All Windows/`.ps1` scripts must be run from a [Developer Powershell](https://devblogs.microsoft.com/visualstudio/the-powershell-you-know-and-love-now-with-a-side-of-visual-studio/).

> A shortcut is installed on your system with Visual Studio (**"Developer Powershell for VS 2019"**)

> Programs launched with this prompt (ex: VS Code) also have access to the Developer shell.

> For more information regarding the Developer Powershell: https://docs.microsoft.com/en-us/cpp/build/building-on-the-command-line?view=msvc-160

### Build

```
.\build_win_<config><bitness>.ps1
```

A Visual Studio 2019 solution will also be generated for building/testing the driver with Visual Studio. (Note: if the build fails, the solution will still be generated.)

Solution file: `<odbc-root>\build\odbc\cmake\global_make_list.sln`.

### Output

```
build
└-- odbc
  └-- bin
    └-- <Configuration>
  └-- cmake
  └-- lib
└-- aws-sdk
  └-- build
  └-- install
```

* Driver DLL: `.\build\odbc\bin\<Config>\odfesqlodbc.dll`
* Test binaries folder: `.\build\odbc\bin\<Config>\`

### Packaging

**Note:** If you make changes to the driver code or CMake project files, re-run the `build_windows_<config><bitness>.sh` script before running the following command.

```
msbuild .\build\odbc\PACKAGE.vcxproj -p:Configuration=Release
```

`Open Distro for Elasticsearch SQL ODBC Driver-<version>-Windows-<Bitness>-bit.msi` will be generated in the build directory.

### Testing
See [run_tests.md](./run_tests.md)

## Mac

### Requirements

[Homebrew](https://brew.sh/) is required for installing the following necessary packages.
```
brew install curl
brew install cmake
brew install libiodbc
```

### Build

```
./build_mac_<config><bitness>.sh
```

### Output

```
build
└-- odbc
  └-- bin
  └-- lib
└-- cmake-build64
  └-- <build files>
```

* Driver library: `./build/odbc/lib/libodfesqlodbc.dylib`
* Test binaries folder: `./build/odbc/bin/`

### Packaging

**Note:** If you make changes to the driver code or CMake project files, re-run the `build_mac_<config><bitness>.sh` script before running the following command.

```
cd cmake-build64/
cpack .
```

`Open Distro for Elasticsearch SQL ODBC Driver-<version>-Darwin.pkg` will be generated in the build directory.

### Testing
See [run_tests.md](./run_tests.md)

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
