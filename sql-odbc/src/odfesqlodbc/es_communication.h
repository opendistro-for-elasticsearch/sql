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

#ifndef ES_COMMUNICATION
#define ES_COMMUNICATION

// clang-format off
#include <memory>
#include <queue>
#include <future>
#include "es_types.h"
#include "es_result_queue.h"

//Keep rabbit at top otherwise it gives build error because of some variable names like max, min
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif // __APPLE__
#include "rabbit.hpp"
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif // __APPLE__
#include <map>
#include <string>
#include <aws/core/Aws.h>
#include <aws/core/http/HttpRequest.h>
#include <aws/core/http/HttpResponse.h>
#include <aws/core/http/HttpClientFactory.h>
#include <aws/core/http/HttpClient.h>
#include <aws/core/client/ClientConfiguration.h>
// clang-format on

class ESCommunication {
   public:
    ESCommunication();
    ~ESCommunication();

    // Create function for factory
    std::string GetErrorMessage();
    bool ConnectionOptions(runtime_options& rt_opts, bool use_defaults,
                           int expand_dbname, unsigned int option_count);
    bool ConnectionOptions2();
    bool ConnectDBStart();
    ConnStatusType GetConnectionStatus();
    void DropDBConnection();
    void LogMsg(ESLogLevel level, const char* msg);
    int ExecDirect(const char* query, const char* fetch_size_);
    void SendCursorQueries(std::string cursor);
    ESResult* PopResult();
    std::string GetClientEncoding();
    bool SetClientEncoding(std::string& encoding);
    bool IsSQLPluginInstalled(const std::string& plugin_response);
    std::string GetServerVersion();
    std::string GetClusterName();
    std::shared_ptr< Aws::Http::HttpResponse > IssueRequest(
        const std::string& endpoint, const Aws::Http::HttpMethod request_type,
        const std::string& content_type, const std::string& query,
        const std::string& fetch_size = "", const std::string& cursor = "");
    void AwsHttpResponseToString(
        std::shared_ptr< Aws::Http::HttpResponse > response,
        std::string& output);
    void SendCloseCursorRequest(const std::string& cursor);
    void StopResultRetrieval();

   private:
    void InitializeConnection();
    bool CheckConnectionOptions();
    bool EstablishConnection();
    void ConstructESResult(ESResult& result);
    void GetJsonSchema(ESResult& es_result);
    void PrepareCursorResult(ESResult& es_result);

    // TODO #35 - Go through and add error messages on exit conditions
    std::string m_error_message;  
    const std::vector< std::string > m_supported_client_encodings = {"UTF8"};

    ConnStatusType m_status;
    bool m_valid_connection_options;
    bool m_is_retrieving;
    ESResultQueue m_result_queue;
    runtime_options m_rt_opts;
    std::string m_client_encoding;
    Aws::SDKOptions m_options;
    std::string m_response_str;
    std::shared_ptr< Aws::Http::HttpClient > m_http_client;
};

#endif
