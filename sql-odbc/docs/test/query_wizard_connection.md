## Query Wizard Connection

* Open blank workbook in Microsoft Excel.
* Click on **Data** > **Get Data** > **From Other Sources** > **From Microsoft Query**

<img src="img/select_microsoft_query.png" width="400">

* Select **Databases** > **ODFE SQL ODBC DSN**. 
* Ensure the **Use the Query Wizard to create/edit queries** check box is selected, and then click **OK**.

<img src="img/query_wizard_enable_use_the_query_wizard_option.png" width="400">

* You might get an popup with a message `This data source contains no visible tables`. Click on **OK**.

<img src="img/query_wizard_error_popup.png" width="350">

* Click on **Options** in Query Wizard window.

<img src="img/query_wizard_choose_coulms.png" width="450">

* Select checkbox **Tables**. Clear all other checkboxes. Click on **OK**.

<img src="img/query_wizard_table_options.png" width="300">

* You will see list of available tables & columns. Select required tables/columns and click on **>**. 
* After selecting all required columns, Click on **Next**.

<img src="img/query_wizard_select_tables.png" width="500">

* Specify conditions to apply filter if needed. Ensure selected operations are supported by Elasticsearch. Click on **Next**.

<img src="img/query_wizard_filter_data.png" width="500">

* Specify sorting options if required. Ensure selected operations are supported by the [OpenDistro for Elasticsearch SQL plugin](https://github.com/opendistro-for-elasticsearch/sql). Click on **Next**.

<img src="img/query_wizard_sort_order.png" width="500">

* Select **Return Data to Microsoft Excel** and click on **Finish**.

<img src="img/query_wizard_finish.png" width="500">

*  Select worksheet and click on **OK**.

<img src="img/query_wizard_import_data.png" width="300">

* Data will be loaded in the spreadsheet

<img src="img/query_wizard_loaded_data.png">
