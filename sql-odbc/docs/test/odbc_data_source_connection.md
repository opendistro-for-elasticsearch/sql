## ODBC as Data Source Connection

* Open blank workbook in Microsoft Excel.
* Click on **Data** > **Get Data** > **From Other Sources** > **From ODBC**

<img src="img/odbc_data_source.png" width="400">

* Select **ODFE SQL ODBC DSN**. Click **OK**.

<img src="img/from_odbc_dsn.png" width="600">

* Select **Default or Custom** in connection credentials windows and click on **Connect**.

<img src="img/from_odbc_auth.png" width="550">

* Select a table from list to load data preview. Click on **Load**.

<img src="img/from_odbc_table_list.png" width="750">

#### Test Advanced options

* Click on **Advanced options** after selecting DSN.
* Add some connection options in **Connection string**.
* Enter any supported **SQL statement**.

<img src="img/from_odbc_advanced_options.png" width="600">

* Click on **OK**. 
* Select Default option for authentication and Click on **Connect**.

<img src="img/from_odbc_advanced_options_auth.png" width="600">

* Data preview should be available. **Load** data in spreadsheet.

<img src="img/from_odbc_advanced_options_load_data.png" width="750">

* Data will be loaded in the spreadsheet

<img src="img/from_odbc_loaded_data.png">

NOTE: Check driver logs for verify modified connection string options.
