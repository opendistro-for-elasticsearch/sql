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

#ifndef __ES_DRIVER_CONNECT_H__
#define __ES_DRIVER_CONNECT_H__
#include "es_connection.h"

// C Interface
#ifdef __cplusplus
extern "C" {
#endif
RETCODE ESAPI_DriverConnect(HDBC hdbc, HWND hwnd, SQLCHAR *conn_str_in,
                            SQLSMALLINT conn_str_in_len, SQLCHAR *conn_str_out,
                            SQLSMALLINT conn_str_out_len,
                            SQLSMALLINT *pcb_conn_str_out,
                            SQLUSMALLINT driver_completion);
#ifdef __cplusplus
}
#endif

#endif /* __ES_DRIVER_CONNECT_H__ */
