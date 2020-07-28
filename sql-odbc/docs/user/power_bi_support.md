#  Connecting Open Distro For ElasticSearch to Microsoft Power BI Desktop

**NOTE**: **The connector is under development. All connection options are not available yet. There could be issues while loading data**

## Prerequisites
* Microsoft Power BI Desktop
* [Open Distro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/)
* [Open Distro for Elasticsearch SQL ODBC driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/)
* [OdfeSqlOdbcPBIConnector.mez](../../src/PowerBIConnector/bin/Release/) 

## Setup
* Copy `OdfeSqlOdbcPBIConnector.mez` file in the `<User>\Documents\Power BI Desktop\Custom Connectors\` folder. This will let Power BI access custom connector.
* Open Power BI Desktop.
* Change the security settings. Click on **Files** > **Options and settings** > **Options** > **Security** > Select **Allow any extension to load without validation or warning** for Data Extensions. This will allow the custom connector to load data into Power BI.

<img src="img/pbi_settings.png" width="500">

* Restart Power BI Desktop.

## Load Data

* Open Power BI Desktop.
* Click on **Home** > **Get Data** > **More** > **Other**. Select **Open Distro For Elasticsearch (Beta)**. Click on **Connect**.

<img src="img/pbi_select_connector.png" width="500">

* You will get a warning for using third-party service. Click on **Continue**.

<img src="img/pbi_third_party_warning.png" width="500">

* Enter host and port values. Click on **OK**.

<img src="img/pbi_connection_string_options.png" width="500">

* Select authentication option. Enter credentials if required and click on **Connect**.

<img src="img/pbi_auth.png" width="500">

* Select required table. Data preview will be loaded.

<img src="img/pbi_data_preview.png">

* Click on **Load**.

## Troubleshooting 

* If you get an following error, please install [Open Distro For Elasticsearch SQL ODBC Driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/).

<img src="img/pbi_error_driver_not_installed.png" width="350">

* If you get an following error,

<img src="img/pbi_error_conn.png" width="350">

1. Check if host and port values are correct.
2. Check if auth credentials are correct.
3. Check if server is running.

