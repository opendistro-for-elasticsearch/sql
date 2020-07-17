All files are available in '/usr/local/lib/odfe-sql-odbc' after installation.

To setup a connection, you can use DSN to store your data source connection information,
1. Open 'iODBC Data Source Administrator'.
2. Go to 'User DSN'.
3. Select 'ODFE SQL ODBC DSN' and click on 'Configure'.
4. Update the connection string values. For the list of all supported options, check '/usr/local/lib/odfe-sql-odbc/doc/README.md'.
5. Click 'Ok' to save changes.

If using with ODBC compatible BI tools, refer to the tool documentation on configuring a new ODBC driver. The typical requirement is to make the tool aware of the location of the driver library file and then use it to setup database (i.e Elasticsearch) connections.

For example, if you want to use Tableau with Elasticsearch Server,
1. Open 'Tableau'.
2. Click on 'Other Databases (ODBC)'.
3. Select 'ODFE SQL ODBC DSN' from the DSN list or 'ODFE SQL ODBC Driver' from the driver list. If using driver, you need to enter connection string values.
4. Click on 'Connect'. All connection attributes will be retrived.
5. Click on 'Sign In'. You will be successfully connected to elasticsearch server.

For more details, check 'https://github.com/opendistro-for-elasticsearch/sql-odbc'.