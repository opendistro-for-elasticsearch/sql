// This file contains Data Connector logic
section #"Open Distro For Elasticsearch";

[DataSource.Kind="PowerBIConnector", Publish="PowerBIConnector.Publish"]
shared PowerBIConnector.Contents = Value.ReplaceType(PowerBIConnectorImpl, PowerBIConnectorType); 

// Wrapper function to provide additional UI customization.
PowerBIConnectorType = type function (
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

PowerBIConnectorImpl = (Host as text, Port as number) as table =>
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
PowerBIConnector = [
    Authentication = [
        Implicit = []
    ],
    Label = Extension.LoadString("DataSourceLabel")
];

// Data Source UI publishing description
PowerBIConnector.Publish = [
    Beta = true,
    Category = "Other",
    ButtonText = { Extension.LoadString("ButtonTitle"), Extension.LoadString("ButtonHelp") },
    LearnMoreUrl = "https://opendistro.github.io/for-elasticsearch/",
    SourceImage = PowerBIConnector.Icons,
    SourceTypeImage = PowerBIConnector.Icons
];

PowerBIConnector.Icons = [
    Icon16 = { Extension.Contents("PowerBIConnector16.png"), Extension.Contents("PowerBIConnector20.png"), Extension.Contents("PowerBIConnector24.png"), Extension.Contents("PowerBIConnector32.png") },
    Icon32 = { Extension.Contents("PowerBIConnector32.png"), Extension.Contents("PowerBIConnector40.png"), Extension.Contents("PowerBIConnector48.png"), Extension.Contents("PowerBIConnector64.png") }
];
