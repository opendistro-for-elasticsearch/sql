// This file contains Data Connector logic
section OdfeSqlOdbcPBIConnector;

// When set to true, additional trace information will be written out to the User log. 
// This should be set to false before release. Tracing is done through a call to 
// Diagnostics.LogValue(). When EnableTraceOutput is set to false, the call becomes a 
// no-op and simply returns the original value.
EnableTraceOutput = true;

[DataSource.Kind="OdfeSqlOdbcPBIConnector", Publish="OdfeSqlOdbcPBIConnector.Publish"]
shared OdfeSqlOdbcPBIConnector.Contents = Value.ReplaceType(OdfeSqlOdbcPBIConnectorImpl, OdfeSqlOdbcPBIConnectorType); 

// Wrapper function to provide additional UI customization.
OdfeSqlOdbcPBIConnectorType = type function (
        Host as (type text meta [
            Documentation.FieldCaption = "Host",
            Documentation.FieldDescription = "The hostname of the Open Distro For Elasticsearch server.",
            Documentation.SampleValues = { "localhost" }
        ]),
        optional Port as (type number meta [
            Documentation.FieldCaption = "Port",
            Documentation.FieldDescription = "The port of the Open Distro For Elasticsearch server is running on.",
            Documentation.SampleValues = { 9200 }
        ])
    )
    as table meta [
        Documentation.Name = "Open Distro For Elasticsearch"
    ];

OdfeSqlOdbcPBIConnectorImpl = (Host as text, optional Port as number) as table =>
    let
        Credential = Extension.CurrentCredential(),
        AuthenticationMode = Credential[AuthenticationKind],

        // Sets connection string properties for authentication.
        CredentialConnectionString =
            if AuthenticationMode = "UsernamePassword" then
                [
                    Auth = "BASIC",
                    UID = Credential[Username],
                    PWD = Credential[Password]
                ]
            else if AuthenticationMode = "Key" then
                [
                    Auth = "AWS_SIGV4",
                    Region = Credential[Key]
                ]
            else 
                [
                    Auth = "NONE"
                ],

        // Sets connection string properties for encrypted connections.
        EncryptedConnectionString =
            if Credential[EncryptConnection] = null or Credential[EncryptConnection] = true then
                [
                    UseSSL = 1
                ]
            else
                [
                    UseSSL = 0
                ],

        // Set host & port in connection string. 
        // Do not include port in connection string for aws server connection.
        Server = 
            if Port <> null then
            [
                Host = Host,
                Port = Port
            ]
            else
            [
                Host = Host
            ],

        ConnectionString = [
            Driver = "ODFE SQL ODBC Driver"
        ],

        SQLGetInfo = Diagnostics.LogValue("SQLGetInfo_Options", [
            SQL_AGGREGATE_FUNCTIONS = ODBC[SQL_AF][All]
        ]),

        SQLGetTypeInfo = (types) => 
            if (EnableTraceOutput <> true) then types else
            let
                // Outputting the entire table might be too large, and result in the value being truncated.
                // We can output a row at a time instead with Table.TransformRows()
                rows = Table.TransformRows(types, each Diagnostics.LogValue("SQLGetTypeInfo " & _[TYPE_NAME], _)),
                toTable = Table.FromRecords(rows)
            in
                Value.ReplaceType(toTable, Value.Type(types)),

        // SQLColumns is a function handler that receives the results of an ODBC call to SQLColumns().
        SQLColumns = (catalogName, schemaName, tableName, columnName, source) =>
            if (EnableTraceOutput <> true) then source else
            // the if statement conditions will force the values to evaluated/written to diagnostics
            if (Diagnostics.LogValue("SQLColumns.TableName", tableName) <> "***" and Diagnostics.LogValue("SQLColumns.ColumnName", columnName) <> "***") then
                let
                    // Outputting the entire table might be too large, and result in the value being truncated.
                    // We can output a row at a time instead with Table.TransformRows()
                    rows = Table.TransformRows(source, each Diagnostics.LogValue("SQLColumns", _)),
                    toTable = Table.FromRecords(rows)
                in
                    Value.ReplaceType(toTable, Value.Type(source))
            else
                source,

        OdbcDatasource = Odbc.DataSource(ConnectionString & Server & CredentialConnectionString & EncryptedConnectionString, [
            // Do not view the tables grouped by their schema names.
            HierarchicalNavigation = false,
            // Prevents execution of native SQL statements. Extensions should set this to true.
            HideNativeQuery = true,
            // Allows upconversion of numeric types
            SoftNumbers = true,
            // Allow upconversion / resizing of numeric and string types
            TolerateConcatOverflow = true,
            // Enables connection pooling via the system ODBC manager
            ClientConnectionPooling = true,

            // These values should be set by previous steps
            SQLColumns = SQLColumns,
            SQLGetTypeInfo = SQLGetTypeInfo,
            SQLGetInfo = SQLGetInfo,

            OnError = OnOdbcError,

            // Connection string properties used for encrypted connections.
            CredentialConnectionString = EncryptedConnectionString
        ])
    in
        OdbcDatasource;

// Handles ODBC errors.
OnOdbcError = (errorRecord as record) =>
    let
        ErrorMessage = errorRecord[Message],
        ConnectionHostPort = errorRecord[Detail][DataSourcePath], 
        
        IsDriverNotInstalled = Text.Contains(ErrorMessage, "doesn't correspond to an installed ODBC driver"),
        
        OdbcError = errorRecord[Detail][OdbcErrors]{0},
        OdbcErrorCode = OdbcError[NativeError],
        
        // Failed to connect to given host
        IsHostUnreachable = 
            OdbcErrorCode = 202
    in
        if IsDriverNotInstalled then
            error Error.Record("", "The Open Distro For Elasticsearch SQL ODBC driver is not installed. Please install the driver")
        else if IsHostUnreachable then 
            error Error.Record("", "Couldn't reach server. Please double-check the host, port and auth.")
        else 
            error errorRecord;

// Data Source Kind description
OdfeSqlOdbcPBIConnector = [
    // Required for use with Power BI Service.
    TestConnection = (dataSourcePath) => 
        let
            json = Json.Document(dataSourcePath),
            Host = json[Host],
            Port = json[Port]
        in
            { "OdfeSqlOdbcPBIConnector.Contents", Host, Port },

    // Authentication modes
    Authentication = [
        Implicit = [
            Label = "NONE"
        ],
        UsernamePassword = [
            Label = "BASIC"
        ],
        Key = [
            Label = "AWS_SIGV4",
            KeyLabel = "Region"
        ]
    ],

    // Enable Encryption
    SupportsEncryption = true,

    Label = Extension.LoadString("DataSourceLabel")
];

// Data Source UI publishing description
OdfeSqlOdbcPBIConnector.Publish = [
    Beta = true,
    Category = "Other",
    ButtonText = { Extension.LoadString("ButtonTitle"), Extension.LoadString("ButtonHelp") },
    LearnMoreUrl = "https://opendistro.github.io/for-elasticsearch/",
    SupportsDirectQuery = true,
    SourceImage = OdfeSqlOdbcPBIConnector.Icons,
    SourceTypeImage = OdfeSqlOdbcPBIConnector.Icons
];

OdfeSqlOdbcPBIConnector.Icons = [
    Icon16 = { Extension.Contents("OdfeSqlOdbcPBIConnector16.png"), Extension.Contents("OdfeSqlOdbcPBIConnector20.png"), Extension.Contents("OdfeSqlOdbcPBIConnector24.png"), Extension.Contents("OdfeSqlOdbcPBIConnector32.png") },
    Icon32 = { Extension.Contents("OdfeSqlOdbcPBIConnector32.png"), Extension.Contents("OdfeSqlOdbcPBIConnector40.png"), Extension.Contents("OdfeSqlOdbcPBIConnector48.png"), Extension.Contents("OdfeSqlOdbcPBIConnector64.png") }
];

// Load common library functions
Extension.LoadFunction = (name as text) =>
    let
        binary = Extension.Contents(name),
        asText = Text.FromBinary(binary)
    in
        Expression.Evaluate(asText, #shared);

// Diagnostics module contains multiple functions. .
Diagnostics = Extension.LoadFunction("Diagnostics.pqm");
Diagnostics.LogValue = if (EnableTraceOutput) then Diagnostics[LogValue] else (prefix, value) => value;

// OdbcConstants contains numeric constants from the ODBC header files, and helper function to create bitfield values.
ODBC = Extension.LoadFunction("OdbcConstants.pqm");
