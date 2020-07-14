# Microsoft Excel

## Prerequisites

* Microsoft Excel 2016 and higher
* [Open Distro for Elasticsearch](https://opendistro.github.io/for-elasticsearch-docs/docs/install/)
* [Open Distro for Elasticsearch SQL ODBC driver](https://opendistro.github.io/for-elasticsearch-docs/docs/sql/odbc/)
* A preconfigured [User or System DSN](../../README.md)

## Load data 

* Open blank workbook in Microsoft Excel.
* Click on **Data** > **Get Data** > **From Other Sources** > **From ODBC**

<img src="img/excel_select_odbc.png" width="400">

* Select **ODFE SQL ODBC DSN**. Click **OK**.

<img src="img/excel_select_dsn.png" width="400">

* Select **Default or Custom** in connection credentials windows and click on **Connect**.

<img src="img/excel_auth.png" width="500">

* Select a table from list to load data preview. Click on **Load**.

<img src="img/excel_data_preview.png">

* Data will be loaded in the spreadsheet.

<img src="img/excel_load_data.png">

## Refresh Data

To refresh the data click on **Query** > **Refresh**.

<img src="img/excel_refresh.png" width=500>

Alternately, **Data** > **Refresh** option can also be used to refresh the data.

## Export as CSV files

* Click on **File** > **Save As**.
* Select Location to Save file.
* Type the file name.
* Set type as **CSV UTF-8(Comma delimited)(*.csv)**.

<img src="img/excel_export.png" width=750>

* Click **Save**.
* Data will be exported to selected location in CSV format.

## Troubleshooting

* If the table has large number of datarows, increase [the keepalive](https://github.com/opendistro-for-elasticsearch/sql/blob/master/docs/dev/Pagination.md#opendistrosqlcursorkeep_alive) value accordlingly. 

