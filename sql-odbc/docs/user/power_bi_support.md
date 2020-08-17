#  Connecting Open Distro For ElasticSearch to Microsoft Power BI Desktop

## Prerequisites
* Microsoft Power BI Desktop
* [Open Distro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/)
* [Open Distro for Elasticsearch SQL ODBC driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/)
* [OdfeSqlOdbcPBIConnector.mez](../../src/PowerBIConnector/bin/Release/) 
* Optional: [odfesqlodbc_import.pbids](../../src/PowerBIConnector/PBIDSExamples) to help with repeated connections to the same server 

## Setup
* Copy `OdfeSqlOdbcPBIConnector.mez` file in the `<User>\Documents\Power BI Desktop\Custom Connectors\` folder. This will let Power BI access custom connector.
* Open Power BI Desktop.
* Change the security settings. Click on **Files** > **Options and settings** > **Options** > **Security** > Select **Allow any extension to load without validation or warning** for Data Extensions. This will allow the custom connector to load data into Power BI.

<img src="img/pbi_settings.png" width="500">

* Restart Power BI Desktop.

## Load Data

> **NOTE**: Currently only import mode is supported. Direct query support will be added soon.

* Open Power BI Desktop.

* Disable parallel loading of tables. Click on **Files** > **Options and settings** > **Options** > **CURRENT FILE** > **Data Load** > Deselect **Enable parallel loading of tables** and click **OK**.

<img src="img/pbi_disable_parallel_loading_tables.png"  width="500">

* Click on **Home** > **Get Data** > **More** > **Other**. Select **Open Distro For Elasticsearch (Beta)**. Click on **Connect**.

<img src="img/pbi_select_connector.png" width="500">

* You will get a warning for using third-party service. Click on **Continue**.

<img src="img/pbi_third_party_warning.png" width="500">

* Enter server value. Click on **OK**.

<img src="img/pbi_connection_string_options.png" width="500">

* Select authentication option. Enter credentials if required and click on **Connect**.

<img src="img/pbi_auth.png" width="500">

* Select required table. Data preview will be loaded.

<img src="img/pbi_data_preview.png">

* Click on **Load**.

* Select required columns for creating graph.

<img src="img/pbi_simple_graph.png">

## Using .PBIDS Files

More info: https://docs.microsoft.com/en-us/power-bi/connect-data/desktop-data-sources#using-pbids-files-to-get-data

Example PBIDS file for Open Distro for Elasticsearch: (available here: [odfesqlodbc_import.pbids](../../src/PowerBIConnector/PBIDSExamples/odfesqlodbc_import.pbids))
```json
{
    "version": "0.1",
    "connections": [
        {
            "details": {
                "protocol": "odfesqlodbc",
                "address": {
                    "server": "localhost:9200"
                }
            },
            "mode": "Import"
        }
    ]
}
```

The only part you should change is the `server` attribute, to point to the location of your ODFE server.
* For AWS connections, this will be the full path of your ODFE instance (ex: `https://aws-odfe-instance.us-west-1.com`).
* Otherwise, this will be the `host:port` combination for your instance (ex: `localhost:9200`).

Save this as a `.pbids` file. Double-click on it to open up your connection in Power BI Desktop.
It will take you straight to the **Navigator** window for selecting the tables from the ODFE server.
* If this is the first time you are connecting to this instance, you will be prompted for your credentials.

<img src="img/pbi_auth.png" width="500">

## Troubleshooting 

* If you get an following error, please install [Open Distro For Elasticsearch SQL ODBC Driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/).

<img src="img/pbi_error_driver_not_installed.png" width="350">

* If you get an following error,

<img src="img/pbi_error_conn.png" width="350">

1. Check if host and port values are correct.
2. Check if auth credentials are correct.
3. Check if server is running.

