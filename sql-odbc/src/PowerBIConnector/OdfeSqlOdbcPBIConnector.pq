// This file contains your Data Connector logic
section OdfeSqlOdbcPBIConnector;

[DataSource.Kind="OdfeSqlOdbcPBIConnector", Publish="OdfeSqlOdbcPBIConnector.Publish"]
shared OdfeSqlOdbcPBIConnector.Contents = Value.ReplaceType(OdfeSqlOdbcPBIConnectorImpl, OdfeSqlOdbcPBIConnectorType); 

// Wrapper function to provide additional UI customization.
OdfeSqlOdbcPBIConnectorType = type function (
        Host as (type text meta [
            Documentation.FieldCaption = "Host",
            Documentation.FieldDescription = "The hostname of the Open Distro For Elasticsearc server.",
            Documentation.SampleValues = { "localhost" }
        ]),
        Port as (type number meta [
            Documentation.FieldCaption = "Port",
            Documentation.FieldDescription = "The port the Open Distro For Elasticsearc server is running on.",
            Documentation.SampleValues = { 9200 }
        ])
    )
    as table meta [
        Documentation.Name = "Open Distro For Elasticsearch"
    ];

OdfeSqlOdbcPBIConnectorImpl = (Host as text, Port as number) as table =>
    let
        ConnectionString = [
            Driver = "ODFE SQL ODBC Driver",
            Host = Host,
            Port = Port
        ],
        OdbcDatasource = Odbc.DataSource(ConnectionString)
    in
        OdbcDatasource;

// Data Source Kind description
OdfeSqlOdbcPBIConnector = [
    Authentication = [
        Implicit = []
    ],
    Label = Extension.LoadString("DataSourceLabel")
];

// Data Source UI publishing description
OdfeSqlOdbcPBIConnector.Publish = [
    Beta = true,
    Category = "Other",
    ButtonText = { Extension.LoadString("ButtonTitle"), Extension.LoadString("ButtonHelp") },
    LearnMoreUrl = "https://opendistro.github.io/for-elasticsearch/",
    SourceImage = OdfeSqlOdbcPBIConnector.Icons,
    SourceTypeImage = OdfeSqlOdbcPBIConnector.Icons
];

OdfeSqlOdbcPBIConnector.Icons = [
    Icon16 = { Extension.Contents("OdfeSqlOdbcPBIConnector16.png"), Extension.Contents("OdfeSqlOdbcPBIConnector20.png"), Extension.Contents("OdfeSqlOdbcPBIConnector24.png"), Extension.Contents("OdfeSqlOdbcPBIConnector32.png") },
    Icon32 = { Extension.Contents("OdfeSqlOdbcPBIConnector32.png"), Extension.Contents("OdfeSqlOdbcPBIConnector40.png"), Extension.Contents("OdfeSqlOdbcPBIConnector48.png"), Extension.Contents("OdfeSqlOdbcPBIConnector64.png") }
];
