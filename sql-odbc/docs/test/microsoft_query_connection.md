## Microsoft Query Connection

* Open blank workbook in Microsoft Excel.
* Click on **Data** > **Get Data** > **From Other Sources** > **From Microsoft Query**

<img src="img/select_microsoft_query.png" width="400">

* Select **Databases** > **ODFE SQL ODBC DSN**. 
* Clear the **Use the Query Wizard to create/edit queries** check box, and then click on **OK**.

<img src="img/microsoft_query_disable_use_the_query_wizard_option.png" width="500">

* Select table and click on **Add**.

<img src="img/microsoft_query_select_tables.png" width="300">
 
* Click on **Close**.

* Double click on required columns. You can double-click the asterisk (*) to select all the columns from a table.

<img src="img/microsoft_query_select_colums.png" width="600">

* You can select different options to define query here. Ensure the query is supported by the [OpenDistro for Elasticsearch SQL plugin](https://github.com/opendistro-for-elasticsearch/sql). After defining query, click on **Return Data** to retrieve the result set.
* Select worksheet and click on **OK**.

<img src="img/microsoft_query_import_data.png" width="300">

* Data will be loaded in the spreadsheet

<img src="img/microsoft_query_loaded_data.png" width="350">
