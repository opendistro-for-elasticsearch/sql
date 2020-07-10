/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

#include "es_communication.h"

// odfesqlodbc needs to be included before mylog, otherwise mylog will generate
// compiler warnings
// clang-format off
#include "es_odbc.h"
#include "mylog.h"
#include <aws/core/utils/StringUtils.h>
#include <aws/core/client/RetryStrategy.h>
#include <aws/core/client/AWSClient.h>
#include <aws/core/utils/HashingUtils.h>
#include <aws/core/auth/AWSAuthSigner.h>
#include <aws/core/auth/AWSCredentialsProvider.h>
#include <aws/core/http/HttpClient.h>
// clang-format on

static const std::string ctype = "application/json";
static const std::string SQL_ENDPOINT_FORMAT_JDBC =
    "/_opendistro/_sql?format=jdbc";
static const std::string SQL_ENDPOINT_CLOSE_CURSOR = "/_opendistro/_sql/close";
static const std::string PLUGIN_ENDPOINT_FORMAT_JSON =
    "/_cat/plugins?format=json";
static const std::string OPENDISTRO_SQL_PLUGIN_NAME = "opendistro_sql";
static const std::string ALLOCATION_TAG = "AWS_SIGV4_AUTH";
static const std::string SERVICE_NAME = "es";
static const std::string ESODBC_PROFILE_NAME = "elasticsearchodbc";
static const std::string JSON_SCHEMA =
    "{"  // This was generated from the example elasticsearch data
    "\"type\": \"object\","
    "\"properties\": {"
    "\"schema\": {"
    "\"type\": \"array\","
    "\"items\": [{"
    "\"type\": \"object\","
    "\"properties\": {"
    "\"name\": { \"type\": \"string\" },"
    "\"type\": { \"type\": \"string\" }"
    "},"
    "\"required\": [ \"name\", \"type\" ]"
    "}]"
    "},"
    "\"cursor\": { \"type\": \"string\" },"
    "\"total\": { \"type\": \"integer\" },"
    "\"datarows\": {"
    "\"type\": \"array\","
    "\"items\": {}"
    "},"
    "\"size\": { \"type\": \"integer\" },"
    "\"status\": { \"type\": \"integer\" }"
    "},"
    "\"required\": [\"schema\", \"total\", \"datarows\", \"size\", \"status\"]"
    "}";
static const std::string CURSOR_JSON_SCHEMA =
    "{"  // This was generated from the example elasticsearch data
    "\"type\": \"object\","
    "\"properties\": {"
    "\"cursor\": { \"type\": \"string\" },"
    "\"datarows\": {"
    "\"type\": \"array\","
    "\"items\": {}"
    "},"
    "\"status\": { \"type\": \"integer\" }"
    "},"
    "\"required\":  [\"datarows\"]"
    "}";

void ESCommunication::AwsHttpResponseToString(
    std::shared_ptr< Aws::Http::HttpResponse > response, std::string& output) {
    // This function has some unconventional stream operations because we need
    // performance over readability here. Equivalent code done in conventional
    // ways (using stringstream operators) takes ~30x longer than this code
    // below and bottlenecks our query performance

    // Get streambuffer from response and set position to start
    std::streambuf* stream_buffer = response->GetResponseBody().rdbuf();
    stream_buffer->pubseekpos(0);

    // Get size of streambuffer and reserver that much space in the output
    size_t avail = static_cast< size_t >(stream_buffer->in_avail());
    std::vector< char > buf(avail, '\0');
    output.clear();
    output.reserve(avail);

    // Directly copy memory from buffer into our string buffer
    stream_buffer->sgetn(buf.data(), avail);
    output.assign(buf.data(), avail);
}

void ESCommunication::PrepareCursorResult(ESResult& es_result) {
    // Prepare document and validate result
    try {
        LogMsg(ES_DEBUG, "Parsing result JSON with cursor.");
        es_result.es_result_doc.parse(es_result.result_json,
                                      CURSOR_JSON_SCHEMA);
    } catch (const rabbit::parse_error& e) {
        // The exception rabbit gives is quite useless - providing the json
        // will aid debugging for users
        std::string str = "Exception obtained '" + std::string(e.what())
                          + "' when parsing json string '"
                          + es_result.result_json + "'.";
        throw std::runtime_error(str.c_str());
    }
}

void ESCommunication::GetJsonSchema(ESResult& es_result) {
    // Prepare document and validate schema
    try {
        LogMsg(ES_DEBUG, "Parsing result JSON with schema.");
        es_result.es_result_doc.parse(es_result.result_json, JSON_SCHEMA);
    } catch (const rabbit::parse_error& e) {
        // The exception rabbit gives is quite useless - providing the json
        // will aid debugging for users
        std::string str = "Exception obtained '" + std::string(e.what())
                          + "' when parsing json string '"
                          + es_result.result_json + "'.";
        throw std::runtime_error(str.c_str());
    }
}

ESCommunication::ESCommunication()
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wreorder"
#endif  // __APPLE__
    : m_status(ConnStatusType::CONNECTION_BAD),
      m_valid_connection_options(false),
      m_is_retrieving(false),
      m_error_message(""),
      m_result_queue(2),
      m_client_encoding(m_supported_client_encodings[0])
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
{
    LogMsg(ES_ALL, "Initializing Aws API.");
    Aws::InitAPI(m_options);
}

ESCommunication::~ESCommunication() {
    LogMsg(ES_ALL, "Shutting down Aws API.");
    Aws::ShutdownAPI(m_options);
}

std::string ESCommunication::GetErrorMessage() {
    // TODO #35 - Check if they expect NULL or "" when there is no error.
    return m_error_message;
}

bool ESCommunication::ConnectionOptions(runtime_options& rt_opts,
                                        bool use_defaults, int expand_dbname,
                                        unsigned int option_count) {
    (void)(expand_dbname);
    (void)(option_count);
    (void)(use_defaults);
    m_rt_opts = rt_opts;
    return CheckConnectionOptions();
}

bool ESCommunication::ConnectionOptions2() {
    return true;
}

bool ESCommunication::ConnectDBStart() {
    LogMsg(ES_ALL, "Starting DB connection.");
    m_status = ConnStatusType::CONNECTION_BAD;
    if (!m_valid_connection_options) {
        m_error_message =
            "Invalid connection options, unable to connect to DB.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        DropDBConnection();
        return false;
    }

    m_status = ConnStatusType::CONNECTION_NEEDED;
    if (!EstablishConnection()) {
        m_error_message = "Failed to establish connection to DB.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        DropDBConnection();
        return false;
    }

    LogMsg(ES_DEBUG, "Connection established.");
    m_status = ConnStatusType::CONNECTION_OK;
    return true;
}

ConnStatusType ESCommunication::GetConnectionStatus() {
    return m_status;
}

void ESCommunication::DropDBConnection() {
    LogMsg(ES_ALL, "Dropping DB connection.");
    if (m_http_client) {
        m_http_client.reset();
    }

    m_status = ConnStatusType::CONNECTION_BAD;
    StopResultRetrieval();
}

bool ESCommunication::CheckConnectionOptions() {
    LogMsg(ES_ALL, "Verifying connection options.");
    m_error_message = "";
    if (m_rt_opts.auth.auth_type != AUTHTYPE_NONE
        && m_rt_opts.auth.auth_type != AUTHTYPE_IAM) {
        if (m_rt_opts.auth.auth_type == AUTHTYPE_BASIC) {
            if (m_rt_opts.auth.username.empty()
                || m_rt_opts.auth.password.empty()) {
                m_error_message = AUTHTYPE_BASIC
                    " authentication requires a username and password.";
            }
        } else {
            m_error_message = "Unknown authentication type: '"
                              + m_rt_opts.auth.auth_type + "'";
        }
    } else if (m_rt_opts.conn.server == "") {
        m_error_message = "Host connection option was not specified.";
    }

    if (m_error_message != "") {
        LogMsg(ES_ERROR, m_error_message.c_str());
        m_valid_connection_options = false;
        return false;
    } else {
        LogMsg(ES_DEBUG, "Required connection option are valid.");
        m_valid_connection_options = true;
    }
    return m_valid_connection_options;
}

void ESCommunication::InitializeConnection() {
    Aws::Client::ClientConfiguration config;
    config.scheme = (m_rt_opts.crypt.use_ssl ? Aws::Http::Scheme::HTTPS
                                             : Aws::Http::Scheme::HTTP);
    config.verifySSL = m_rt_opts.crypt.verify_server;
    long response_timeout =
        static_cast< long >(DEFAULT_RESPONSE_TIMEOUT) * 1000L;
    try {
        response_timeout =
            std::stol(m_rt_opts.conn.timeout, nullptr, 10) * 1000L;
    } catch (...) {
    }
    config.connectTimeoutMs = response_timeout;
    config.httpRequestTimeoutMs = response_timeout;
    config.requestTimeoutMs = response_timeout;
    m_http_client = Aws::Http::CreateHttpClient(config);
}

std::shared_ptr< Aws::Http::HttpResponse > ESCommunication::IssueRequest(
    const std::string& endpoint, const Aws::Http::HttpMethod request_type,
    const std::string& content_type, const std::string& query,
    const std::string& fetch_size, const std::string& cursor) {
    // Generate http request
    std::shared_ptr< Aws::Http::HttpRequest > request =
        Aws::Http::CreateHttpRequest(
            Aws::String(
                m_rt_opts.conn.server
                + (m_rt_opts.conn.port.empty() ? "" : ":" + m_rt_opts.conn.port)
                + endpoint),
            request_type,
            Aws::Utils::Stream::DefaultResponseStreamFactoryMethod);

    // Set header type
    if (!content_type.empty())
        request->SetHeaderValue(Aws::Http::CONTENT_TYPE_HEADER, ctype);

    // Set body
    if (!query.empty() || !cursor.empty()) {
        rabbit::object body;
        if (!query.empty()) {
            body["query"] = query;
            if (!fetch_size.empty() && fetch_size != "-1")
                body["fetch_size"] = fetch_size;
        } else if (!cursor.empty()) {
            body["cursor"] = cursor;
        }
        std::shared_ptr< Aws::StringStream > aws_ss =
            Aws::MakeShared< Aws::StringStream >("RabbitStream");
        *aws_ss << std::string(body.str());
        request->AddContentBody(aws_ss);
        request->SetContentLength(std::to_string(body.str().size()));
    }

    // Handle authentication
    if (m_rt_opts.auth.auth_type == AUTHTYPE_BASIC) {
        std::string userpw_str =
            m_rt_opts.auth.username + ":" + m_rt_opts.auth.password;
        Aws::Utils::Array< unsigned char > userpw_arr(
            reinterpret_cast< const unsigned char* >(userpw_str.c_str()),
            userpw_str.length());
        std::string hashed_userpw =
            Aws::Utils::HashingUtils::Base64Encode(userpw_arr);
        request->SetAuthorization("Basic " + hashed_userpw);
    } else if (m_rt_opts.auth.auth_type == AUTHTYPE_IAM) {
        std::shared_ptr< Aws::Auth::ProfileConfigFileAWSCredentialsProvider >
            credential_provider =
                Aws::MakeShared< Aws::Auth::ProfileConfigFileAWSCredentialsProvider >(
                    ALLOCATION_TAG.c_str(), ESODBC_PROFILE_NAME.c_str());
        Aws::Client::AWSAuthV4Signer signer(credential_provider,
                                            SERVICE_NAME.c_str(),
                                            m_rt_opts.auth.region.c_str());
        signer.SignRequest(*request);
    }

    // Issue request and return response
    return m_http_client->MakeRequest(request);
}

bool ESCommunication::IsSQLPluginInstalled(const std::string& plugin_response) {
    try {
        rabbit::document doc;
        doc.parse(plugin_response);

        rabbit::array plugin_array = doc;
        for (auto it : plugin_array) {
            if (it.has("component") && it.has("version")) {
                std::string plugin_name = it.at("component").as_string();
                if (!plugin_name.compare(OPENDISTRO_SQL_PLUGIN_NAME)) {
                    std::string sql_plugin_version =
                        it.at("version").as_string();
                    LogMsg(ES_ERROR, std::string("Found SQL plugin version '"
                                                 + sql_plugin_version + "'.")
                                         .c_str());
                    return true;
                }
            } else {
                m_error_message =
                    "Could not find all necessary fields in the plugin "
                    "response object. "
                    "(\"component\", \"version\")";
                throw std::runtime_error(m_error_message.c_str());
            }
        }
    } catch (const rabbit::type_mismatch& e) {
        m_error_message =
            "Error parsing endpoint response: " + std::string(e.what());
    } catch (const rabbit::parse_error& e) {
        m_error_message =
            "Error parsing endpoint response: " + std::string(e.what());
    } catch (const std::exception& e) {
        m_error_message =
            "Error parsing endpoint response: " + std::string(e.what());
    } catch (...) {
        m_error_message =
            "Unknown exception thrown when parsing plugin endpoint response.";
    }

    LogMsg(ES_ERROR, m_error_message.c_str());
    return false;
}

bool ESCommunication::EstablishConnection() {
    // Generate HttpClient Connection class if it does not exist
    LogMsg(ES_ALL, "Attempting to establish DB connection.");
    if (!m_http_client) {
        InitializeConnection();
    }

    // Check whether SQL plugin has been installed on the Elasticsearch server.
    // This is required for executing driver queries with the server.
    LogMsg(ES_ALL, "Checking for SQL plugin");
    std::shared_ptr< Aws::Http::HttpResponse > response =
        IssueRequest(PLUGIN_ENDPOINT_FORMAT_JSON,
                     Aws::Http::HttpMethod::HTTP_GET, "", "", "");
    if (response == nullptr) {
        m_error_message =
            "The SQL plugin must be installed in order to use this driver. "
            "Received NULL response.";
    } else {
        AwsHttpResponseToString(response, m_response_str);
        if (response->GetResponseCode() != Aws::Http::HttpResponseCode::OK) {
            m_error_message =
                "The SQL plugin must be installed in order to use this driver.";
            if (response->HasClientError())
                m_error_message += " Client error: '"
                                   + response->GetClientErrorMessage() + "'.";
            if (!m_response_str.empty())
                m_error_message += " Response error: '" + m_response_str + "'.";
        } else {
            if (IsSQLPluginInstalled(m_response_str)) {
                return true;
            } else {
                m_error_message =
                    "The SQL plugin must be installed in order to use this "
                    "driver. Response body: '"
                    + m_response_str + "'";
            }
        }
    }
    LogMsg(ES_ERROR, m_error_message.c_str());
    return false;
}

int ESCommunication::ExecDirect(const char* query, const char* fetch_size_) {
    if (!query) {
        m_error_message = "Query is NULL";
        LogMsg(ES_ERROR, m_error_message.c_str());
        return -1;
    } else if (!m_http_client) {
        m_error_message = "Unable to connect. Please try connecting again.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        return -1;
    }

    // Prepare statement
    std::string statement(query);
    std::string fetch_size(fetch_size_);
    std::string msg = "Attempting to execute a query \"" + statement + "\"";
    LogMsg(ES_DEBUG, msg.c_str());

    // Issue request
    std::shared_ptr< Aws::Http::HttpResponse > response =
        IssueRequest(SQL_ENDPOINT_FORMAT_JDBC, Aws::Http::HttpMethod::HTTP_POST,
                     ctype, statement, fetch_size);

    // Validate response
    if (response == nullptr) {
        m_error_message =
            "Failed to receive response from query. "
            "Received NULL response.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        return -1;
    }

    // Convert body from Aws IOStream to string
    std::unique_ptr< ESResult > result = std::make_unique< ESResult >();
    AwsHttpResponseToString(response, result->result_json);

    // If response was not valid, set error
    if (response->GetResponseCode() != Aws::Http::HttpResponseCode::OK) {
        m_error_message =
            "Http response code was not OK. Code received: "
            + std::to_string(static_cast< long >(response->GetResponseCode()))
            + ".";
        if (response->HasClientError())
            m_error_message +=
                " Client error: '" + response->GetClientErrorMessage() + "'.";
        if (!result->result_json.empty()) {
            m_error_message +=
                " Response error: '" + result->result_json + "'.";
        }
        LogMsg(ES_ERROR, m_error_message.c_str());
        return -1;
    }

    // Add to result queue and return
    try {
        ConstructESResult(*result);
    } catch (std::runtime_error& e) {
        m_error_message = "Received runtime exception: " + std::string(e.what());
        if (!result->result_json.empty()) {
            m_error_message += " Result body: " + result->result_json;
        }
        LogMsg(ES_ERROR, m_error_message.c_str());
        return -1;
    }

    const std::string cursor = result->cursor;
    while (!m_result_queue.push(QUEUE_TIMEOUT, result.get())) {
        if (ConnStatusType::CONNECTION_OK == m_status) {
            return -1;
        }
    }

    result.release();

    if (!cursor.empty()) {
        // If the response has a cursor, this thread will retrieve more result pages asynchronously.
        std::thread([&, cursor]() {
            SendCursorQueries(cursor);
        }).detach();
    }

    return 0;
}

void ESCommunication::SendCursorQueries(std::string cursor) {
    if (cursor.empty()) {
        return;
    }
    m_is_retrieving = true;

    try {
        while (!cursor.empty() && m_is_retrieving) {
            std::shared_ptr< Aws::Http::HttpResponse > response = IssueRequest(
                SQL_ENDPOINT_FORMAT_JDBC, Aws::Http::HttpMethod::HTTP_POST,
                ctype, "", "", cursor);
            if (response == nullptr) {
                m_error_message =
                    "Failed to receive response from cursor. "
                    "Received NULL response.";
                LogMsg(ES_ERROR, m_error_message.c_str());
                return;
            }

            std::unique_ptr<ESResult> result = std::make_unique<ESResult>();
            AwsHttpResponseToString(response, result->result_json);
            PrepareCursorResult(*result);

            if (result->es_result_doc.has("cursor")) {
                cursor = result->es_result_doc["cursor"].as_string();
                result->cursor = result->es_result_doc["cursor"].as_string();
            } else {
                SendCloseCursorRequest(cursor);
                cursor.clear();
            }

            while (m_is_retrieving
                   && !m_result_queue.push(QUEUE_TIMEOUT, result.get())) {
            }

            // Don't release when attempting to push to the queue as it may take multiple tries.
            result.release();
        }
    } catch (std::runtime_error& e) {
        m_error_message =
            "Received runtime exception: " + std::string(e.what());
        LogMsg(ES_ERROR, m_error_message.c_str());
    }

    if (!m_is_retrieving) {
        m_result_queue.clear();
    } else {
        m_is_retrieving = false;
    }
}

void ESCommunication::SendCloseCursorRequest(const std::string& cursor) {
    std::shared_ptr< Aws::Http::HttpResponse > response =
        IssueRequest(SQL_ENDPOINT_CLOSE_CURSOR,
                     Aws::Http::HttpMethod::HTTP_POST, ctype, "", "", cursor);
    if (response == nullptr) {
        m_error_message =
            "Failed to receive response from cursor. "
            "Received NULL response.";
        LogMsg(ES_ERROR, m_error_message.c_str());
    }
}

void ESCommunication::StopResultRetrieval() {
    m_is_retrieving = false;
    m_result_queue.clear();
}

void ESCommunication::ConstructESResult(ESResult& result) {
    GetJsonSchema(result);
    rabbit::array schema_array = result.es_result_doc["schema"];
    for (rabbit::array::iterator it = schema_array.begin();
         it != schema_array.end(); ++it) {
        std::string column_name = it->at("name").as_string();

        ColumnInfo col_info;
        col_info.field_name = column_name;
        col_info.type_oid = KEYWORD_TYPE_OID;
        col_info.type_size = KEYWORD_TYPE_SIZE;
        col_info.display_size = KEYWORD_DISPLAY_SIZE;
        col_info.length_of_str = KEYWORD_TYPE_SIZE;
        col_info.relation_id = 0;
        col_info.attribute_number = 0;

        result.column_info.push_back(col_info);
    }
    if (result.es_result_doc.has("cursor")) {
        result.cursor = result.es_result_doc["cursor"].as_string();
    }
    result.command_type = "SELECT";
    result.num_fields = (uint16_t)schema_array.size();
}

inline void ESCommunication::LogMsg(ESLogLevel level, const char* msg) {
#if WIN32
#pragma warning(push)
#pragma warning(disable : 4551)
#endif  // WIN32
    // cppcheck outputs an erroneous missing argument error which breaks build.
    // Disable for this function call
    MYLOG(level, "%s\n", msg);
#if WIN32
#pragma warning(pop)
#endif  // WIN32
}

ESResult* ESCommunication::PopResult() {
    ESResult* result = NULL;
    while (!m_result_queue.pop(QUEUE_TIMEOUT, result) && m_is_retrieving) {
    }

    return result;
}

// TODO #36 - Send query to database to get encoding
std::string ESCommunication::GetClientEncoding() {
    return m_client_encoding;
}

// TODO #36 - Send query to database to set encoding
bool ESCommunication::SetClientEncoding(std::string& encoding) {
    if (std::find(m_supported_client_encodings.begin(),
                  m_supported_client_encodings.end(), encoding)
        != m_supported_client_encodings.end()) {
        m_client_encoding = encoding;
        return true;
    }
    LogMsg(ES_ERROR,
           std::string("Failed to find encoding " + encoding).c_str());
    return false;
}

std::string ESCommunication::GetServerVersion() {
    if (!m_http_client) {
        InitializeConnection();
    }

    // Issue request
    std::shared_ptr< Aws::Http::HttpResponse > response =
        IssueRequest("", Aws::Http::HttpMethod::HTTP_GET, "", "", "");
    if (response == nullptr) {
        m_error_message =
            "Failed to receive response from query. "
            "Received NULL response.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        return "";
    }

    // Parse server version
    if (response->GetResponseCode() == Aws::Http::HttpResponseCode::OK) {
        try {
            AwsHttpResponseToString(response, m_response_str);
            rabbit::document doc;
            doc.parse(m_response_str);
            if (doc.has("version") && doc["version"].has("number")) {
                return doc["version"]["number"].as_string();
            }

        } catch (const rabbit::type_mismatch& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (const rabbit::parse_error& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (const std::exception& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (...) {
            LogMsg(ES_ERROR,
                   "Unknown exception thrown when parsing main endpoint "
                   "response.");
        }
    }
    LogMsg(ES_ERROR, m_error_message.c_str());
    return "";
}

std::string ESCommunication::GetClusterName() {
    if (!m_http_client) {
        InitializeConnection();
    }

    // Issue request
    std::shared_ptr< Aws::Http::HttpResponse > response =
        IssueRequest("", Aws::Http::HttpMethod::HTTP_GET, "", "", "");
    if (response == nullptr) {
        m_error_message =
            "Failed to receive response from query. "
            "Received NULL response.";
        LogMsg(ES_ERROR, m_error_message.c_str());
        return "";
    }

    // Parse cluster name
    if (response->GetResponseCode() == Aws::Http::HttpResponseCode::OK) {
        try {
            AwsHttpResponseToString(response, m_response_str);
            rabbit::document doc;
            doc.parse(m_response_str);
            if (doc.has("cluster_name")) {
                return doc["cluster_name"].as_string();
            }

        } catch (const rabbit::type_mismatch& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (const rabbit::parse_error& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (const std::exception& e) {
            m_error_message = "Error parsing main endpoint response: "
                              + std::string(e.what());
            LogMsg(ES_ERROR, m_error_message.c_str());
        } catch (...) {
            LogMsg(ES_ERROR,
                   "Unknown exception thrown when parsing main endpoint "
                   "response.");
        }
    }
    LogMsg(ES_ERROR, m_error_message.c_str());
    return "";
}
