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

#ifndef __ES_HELPER_H__
#define __ES_HELPER_H__

#include "es_types.h"

#ifdef __cplusplus
// C++ interface
std::string ESGetClientEncoding(void* es_conn);
bool ESSetClientEncoding(void* es_conn, std::string& encoding);
ESResult* ESGetResult(void* es_conn);
void ESClearResult(ESResult* es_result);
void* ESConnectDBParams(runtime_options& rt_opts, int expand_dbname,
                        unsigned int option_count);
std::string GetServerVersion(void* es_conn);
std::string GetErrorMsg(void* es_conn);

// C Interface
extern "C" {
#endif
void XPlatformInitializeCriticalSection(void** critical_section_helper);
void XPlatformEnterCriticalSection(void* critical_section_helper);
void XPlatformLeaveCriticalSection(void* critical_section_helper);
void XPlatformDeleteCriticalSection(void** critical_section_helper);
ConnStatusType ESStatus(void* es_conn);
int ESExecDirect(void* es_conn, const char* statement, const char* fetch_size);
void ESSendCursorQueries(void* es_conn, const char* cursor);
void ESDisconnect(void* es_conn);
void ESStopRetrieval(void* es_conn);
#ifdef __cplusplus
}
#endif

void* InitializeESConn();

#endif  // __ES_HELPER_H__
