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

#ifndef _DRVCONN_H_
#define _DRVCONN_H_

#include <stdio.h>
#include <stdlib.h>

#include "es_connection.h"
#include "es_odbc.h"
#include "misc.h"

#ifndef WIN32
#include <sys/socket.h>
#include <sys/types.h>
#else
#include <winsock2.h>
#endif

#include <string.h>

#ifdef WIN32
#include <windowsx.h>

#include "resource.h"
#endif
#include "dlg_specific.h"
#include "es_apifunc.h"

#define PASSWORD_IS_REQUIRED 1

#ifdef __cplusplus
extern "C" {
#endif
char *hide_password(const char *str);
BOOL dconn_get_connect_attributes(const char *connect_string, ConnInfo *ci);
BOOL dconn_get_DSN_or_Driver(const char *connect_string, ConnInfo *ci);
int paramRequired(const ConnInfo *ci, int reqs);
#ifdef WIN32
RETCODE dconn_DoDialog(HWND hwnd, ConnInfo *ci);
#endif
#ifdef __cplusplus
}
#endif

#endif
