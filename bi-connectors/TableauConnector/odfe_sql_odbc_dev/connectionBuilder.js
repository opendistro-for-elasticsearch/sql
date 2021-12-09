(function dsbuilder(attr){
    var params = {};

    // Set host information in connection string
    params["SERVER"] = attr[connectionHelper.attributeServer];
    params["PORT"] = attr[connectionHelper.attributePort];

    // Set authentication values in connection string
    var authAttrValue = attr[connectionHelper.attributeAuthentication];
    params["Auth"] = attr[connectionHelper.attributeAuthentication];
    if (authAttrValue == "AWS_SIGV4"){
        params["Region"] = attr[connectionHelper.attributeVendor1];
    } else if (authAttrValue == "BASIC"){
        params["UID"] = attr[connectionHelper.attributeUsername];
        params["PWD"] = attr[connectionHelper.attributePassword];
    }

    // Set SSL value in connection string 
    if (attr[connectionHelper.attributeSSLMode] == "require"){
        params["useSSL"] = "1";
    } else {
        params["useSSL"] = "0";
    }

    // Parse additional options and add in connection string
    var odbcConnectStringExtrasMap = {};
    const attributeODBCConnectStringExtras = "vendor2";
    if (attributeODBCConnectStringExtras in attr){
        odbcConnectStringExtrasMap = connectionHelper.ParseODBCConnectString(attr[attributeODBCConnectStringExtras]);
    }
    for (var key in odbcConnectStringExtrasMap){
        params[key] = odbcConnectStringExtrasMap[key];
    }

    // Format the attributes as 'key=value'
    var formattedParams = [];
    formattedParams.push(connectionHelper.formatKeyValuePair(driverLocator.keywordDriver, driverLocator.locateDriver(attr)));
    for (var key in params){
        formattedParams.push(connectionHelper.formatKeyValuePair(key, params[key]));
    }
    return formattedParams;
})
