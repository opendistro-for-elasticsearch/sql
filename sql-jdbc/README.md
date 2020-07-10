## Open Distro for ElasticSearch - JDBC

This is the driver for JDBC connectivity to a cluster running with Open Distro for Elasticsearch SQL support.

## Specifications

The driver is compatible with JDBC 4.2 specification and requires a minimum of Java 8.

## Using the driver

The driver comes in the form of a single jar file. To use it, simply place it on the classpath of the
Java application that needs to use it.

If using with JDBC compatible BI tools, refer to the tool documentation on configuring a new JDBC driver. Typically,
all that's required is to make the tool aware of the location of the driver jar and then use it to setup database (i.e
Elasticsearch) connections.

### Connection URL and other settings

To setup a connection, the driver requires a JDBC connection URL. The connection URL is of the form:
```
    jdbc:elasticsearch://[scheme://][host][:port][/context-path]?[property-key=value]&[property-key2=value2]..&[property-keyN=valueN]
```


* scheme

  Can be one of *http* or *https*. Default is *http*.

* host

  Hostname or IP address of the target cluster.  Default is *localhost*.

* port

  Port number on which the cluster's REST interface is listening. Default value depends on the *scheme* selected. For
  *http*, the default is 9200. For *https*, the default is 443.

* context-path

  The context path at which the cluster REST interface is rooted. Not needed if the REST interface is simply available on the '/' context path.

* property key=value

  The query string portion of the connection URL can contain desired connection settings in the form of one or more
  *property-key=value* pairs. The possible configuration properties are provided in the table below. The property keys are case sensitive but values are not unless otherwise indicated.

  Note that JDBC provides multiple APIs for specifying connection properties of which specifying them in the connection
  URL is just one. When directly coding with the driver you can choose any of the other options (refer sample
  code below). If you are setting up a connection via a tool, it is likely the tool will allow you to specify the
  connection URL with just the scheme, host, port and context-path components) while the the connection properties are provided separately.
  For example, you may not wish to place the user and password in the connection URL. Check the tool you are using for
  such support.

  The configurable connection properties are:

  | Property Key  | Description | Accepted Value(s)    | Default value  |
  | ------------- |-------------| -----|---------|
  | user      | Connection username. mandatory if `auth` property selects a authentication scheme that mandates a username value | any string   | `null` |
  | password      | Connection password. mandatory if `auth` property selects a authentication scheme that mandates a password value | any string     |   `null` |
  | fetchSize      | Cursor page size | positive integer value. Max value is limited by `index.max_result_window` Elasticsearch setting  |   `0` (for non-paginated response) |
  | logOutput | location where driver logs should be emitted | a valid file path     |    `null` (logs are disabled) |
  | logLevel | severity level for which driver logs should be emitted | in order from highest(least logging) to lowest(most logging): OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL  |    OFF (logs are disabled) |
  | auth     | authentication mechanism to use | `NONE` (no auth), `BASIC` (HTTP Basic), `AWS_SIGV4` (AWS SIGV4) | `basic` if username and/or password is specified, `NONE` otherwise |
  | awsCredentialsProvider | The AWS credential provider to be used when authentication mechanism is `AWS_SIGV4` (AWS SIGV4). If not set, the driver will use DefaultAWSCredentialsProviderChain to sign the request. Note that the driver renamed the namespaces of its dependencies, so the value has to be an instance of com.amazonaws.opendistro.elasticsearch.sql.jdbc.shadow.com.amazonaws.auth.AWSCredentialsProvider| Instance of an AWSCredentialProvider | DefaultAWSCredentialsProviderChain |
  | region | if authentication type is `aws_sigv4`, then this is the region value to use when signing requests. Only needed if the driver can not determine the region for the host endpoint. The driver will detect the region if the host endpoint matches a known url pattern. | a valid AWS region value e.g. us-east-1 | `null` (auto-detected if possible from the host endpoint) |
  | requestCompression | whether to indicate acceptance of compressed (gzip) responses when making server requests | `true` or `false` | `false` |
  | useSSL   | whether to establish the connection over SSL/TLS | `true` or `false` | `false` if scheme is `http`, `true` if scheme is `https` |
  | trustStoreLocation | location of the SSL/TLS truststore to use | file path or URL as appropriate to the type of truststore | `null` |
  | trustStoreType     | type of the truststore | valid truststore type recognized by available Java security providers | JKS |
  | trustStorePassword | password to access the Trust Store | any string | `null` |
  | keyStoreLocation   | location of the SSL/TLS keystore to use | file path or URL as appropriate to the type of keystore | `null` |
  | keyStoreType       | type of the keystore | valid keystore type recognized by available Java security providers | JKS |
  | keyStorePassword   | password to access the keystore | any string | `null` |
  | trustSelfSigned    | shortcut way to indicate that any self-signed certificate should be accepted. A truststore is not required to be configured. | `true` or `false` | `false` |
  | hostnameVerification    | indicate whether certificate hostname verification should be performed when using SSL/TLS | `true` or `false` | `true` |

### Connecting using the DriverManager interface

The main Driver class is `com.amazon.opendistroforelasticsearch.jdbc.Driver`. If the driver jar is on the application classpath, no other configuration is required.

Code samples to open a connection for some typical scenarios are given below:

* Connect to localhost on port 9200 with no authentication over a plain connection

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://localhost:9200";

Connection con = DriverManager.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with no authentication

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

Connection con = DriverManager.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

or,

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://remote-host-name";

Properties properties = new Properties();
properties.put("useSSL", "true");

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host with HTTP Basic authentication over an SSL/TLS connection on the default SSL/TLS port. Note - if a username and password are provided and `auth` property is not provided, basic auth is implicitly used.

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name";
String user = "username";
String password = "password";

Connection con = DriverManager.getConnection(url, user, password);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

or,

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://remote-host-name";

Properties properties = new Properties();
properties.put("useSSL", "true");
properties.put("user", "username");
properties.put("password", "password");

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host with HTTP Basic authentication over an SSL/TLS connection, allowing any self-signed certificate and optionally turning off hostname verification. This may be useful for a dev/test setup.

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://remote-host-name";

Properties properties = new Properties();
properties.put("useSSL", "true");
properties.put("trustSelfSigned", "true");

// uncomment below to turn off hostname verification
// properties.put("hostnameVerification", "false");

properties.put("user", "username");
properties.put("password", "password");

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication. The driver will determine the credentials used to sign the request just like the standard aws-sdk i.e. in standard directories, environment variables etc.


```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name?auth=aws_sigv4";

Connection con = DriverManager.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```
or,

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

Properties properties = new Properties();
properties.put("auth", "aws_sigv4");

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication, explicitly specifying the AWSCredentialProvider to use

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

Properties properties = new Properties();
properties.put("awsCredentialsProvider", new EnvironmentVariableCredentialsProvider());

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication, explicitly specifying the region to use in the request signing.

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name?auth=aws_sigv4&region=us-west-1";

Connection con = DriverManager.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

or,

```
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

Properties properties = new Properties();
properties.put("auth", "aws_sigv4");
properties.put("region", "us-west-2");

Connection con = DriverManager.getConnection(url, properties);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```
### Connecting using the DataSource interface

The driver also provides a javax.sql.DataSource implementation via the `com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource` class that can be used to obtain a connection. Here are some typical code samples:


* Connect to localhost on port 9200 with no authentication over a plain connection

```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://localhost:9200";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);

Connection con = ds.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with no authentication

```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);

Connection con = ds.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host with HTTP Basic authentication over an SSL/TLS connection on the default SSL/TLS port.

```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://https://remote-host-name";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);

Connection con = ds.getConnection(url, "user", "password");
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication. The driver will determine the credentials used to sign the request just like the standard aws-sdk i.e. in standard directories, environment variables etc.


```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://https://remote-host-name?auth=aws_sigv4";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);

Connection con = ds.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication, explicitly specifying the AWSCredentialProvider to use

```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://https://remote-host-name?auth=aws_sigv4&region=us-west-1";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);
ds.setAwsCredentialProvider(new EnvironmentVariableCredentialsProvider());

Connection con = ds.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

* Connect to a remote host on default SSL port with AWS Sig V4 authentication, explicitly specifying the region to use in the request signing.

```
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;

import com.amazon.opendistroforelasticsearch.jdbc.ElasticsearchDataSource;

.
.
String url = "jdbc:elasticsearch://https://remote-host-name?auth=aws_sigv4&region=us-west-1";

ElasticsearchDataSource ds = new ElasticsearchDataSource();
ds.setUrl(url);

Connection con = ds.getConnection(url);
Statement st = con.createStatement();
.
// use the connection
.
// close connection
con.close();
```

## Download and Installation

The driver will be available through standard open source repositories for Java artifacts.

## Building from source

The driver is built as a shadow jar so that its dependencies are bundled within itself. This way no additional libraries besides the driver jar need to be placed on an application classpath for the driver to be used. The namespaces of the bundled dependencies are modified to ensure they do not conflict with other classes on the application classpath.  

### Run unit tests and build the driver jar

```
./gradlew clean test shadowJar
```

### Build the driver jar without unit tests

```
./gradlew shadowJar
```

### Publish the built driver jar to local maven repo

```
./gradlew publishToMavenLocal
```

## Documentation

Please refer to the [documentation](https://opendistro.github.io/for-elasticsearch-docs/) for detailed information on installing and configuring opendistro-elasticsearch-security plugin.

## Code of Conduct

This project has adopted an [Open Source Code of Conduct](https://opendistro.github.io/for-elasticsearch/codeofconduct.html).


## Security issue notifications

If you discover a potential security issue in this project we ask that you notify AWS/Amazon Security via our [vulnerability reporting page](http://aws.amazon.com/security/vulnerability-reporting/). Please do **not** create a public GitHub issue.


## Licensing

See the [LICENSE](./LICENSE) file for our project's licensing. We will ask you to confirm the licensing of your contribution.

## Copyright

Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.

