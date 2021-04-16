#  Connecting Open Distro For ElasticSearch to Microsoft Power BI Desktop

## Prerequisites
* Microsoft Power BI Desktop
* [Open Distro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/)
* [Open Distro for Elasticsearch SQL ODBC driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/)
* [OdfeSqlOdbcPBIConnector.mez](../../src/PowerBIConnector/bin/Release/) 
* Optional: [odfesqlodbc_import.pbids](../../src/PowerBIConnector/PBIDSExamples) to help with repeated connections to the same server 

## Setup
* Copy `OdfeSqlOdbcPBIConnector.mez` file to the `<User>\Documents\Power BI Desktop\Custom Connectors\` folder. This will let Power BI access custom connector. If you have installed the On-premises data gateway, you should be able to see the ODBC is detected as a custom connector in the **Connectors** tab.
  
<img src="img/pbi_gateway_connectors.png" width="500">

## Load Data By Import

* Open Power BI Desktop.

* Disable parallel loading of tables. Click on **Files** > **Options and settings** > **Options** > **CURRENT FILE** > **Data Load** > Deselect **Enable parallel loading of tables** and click **OK**.

<img src="img/pbi_disable_parallel_loading_tables.png"  width="500">

* Click on **Home** > **Get Data** > **More** > **Other**. Select **ODBC**. Click on **Connect**.

<img src="img/pbi_select_connector.png" width="500">

* If you are running the ES instance in localhost, select the default **ODFE SQL ODBC DSN** as the data source.

<img src="img/pbi_select_dsn.png" width="500">

* Click on **OK**. Select authentication option. Enter credentials if required and click on **Connect**.

<img src="img/pbi_default_dsn_auth.png" width="500">

* You can also connect a remote ES instance as the data source by manually adding the DSN and authentication options through **ODBC Data Source Administrator**. Select the DSN you configured in the administrator, and click **OK**.

<img src="img/pbi_select_custom_dsn.png" width="500">

* Select required table. Data preview will be loaded.

<img src="img/pbi_data_preview.png">

* Click on **Load**.

* Select required columns for creating graph.

<img src="img/pbi_simple_graph.png">

## Load Data By Query

* Direct query is supported to load data now. When selecting the DSN, expand the **Advanced options** and write your SQL query in the `SQL statement (optional)` field.

<img src="img/pbi_query.png"> 

* Click **OK** and you will get a preview with resulted data set. Then click **Load** to load the result table.

<img src="img/pbi_query_result.png">

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

* If you get an following error, please install [Open Distro For Elasticsearch SQL ODBC Driver](https://opendistro.github.io/for-elasticsearch/downloads.html#connect).

<img src="img/pbi_error_driver_not_installed.png" width="350">

* If you get an following error,

<img src="img/pbi_error_conn.png" width="350">

1. Check if host and port values are correct.
2. Check if auth credentials are correct.
3. Check if server is running.

