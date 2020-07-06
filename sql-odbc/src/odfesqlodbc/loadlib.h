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

#ifndef __LOADLIB_H__
#define __LOADLIB_H__

#include "es_odbc.h"
#ifdef HAVE_LIBLTDL
#include <ltdl.h>
#else
#ifdef HAVE_DLFCN_H
#include <dlfcn.h>
#endif /* HAVE_DLFCN_H */
#endif /* HAVE_LIBLTDL */

#include <stdlib.h>
#ifdef __cplusplus
extern "C" {
#endif

#ifdef _HANDLE_ENLIST_IN_DTC_
RETCODE CALL_EnlistInDtc(ConnectionClass *conn, void *pTra, int method);
RETCODE CALL_DtcOnDisconnect(ConnectionClass *);
RETCODE CALL_IsolateDtcConn(ConnectionClass *, BOOL);
void *CALL_GetTransactionObject(HRESULT *);
void CALL_ReleaseTransactionObject(void *);
#endif /* _HANDLE_ENLIST_IN_DTC_ */
/* void	UnloadDelayLoadedDLLs(BOOL); */
void CleanupDelayLoadedDLLs(void);
#ifdef WIN32
HMODULE MODULE_load_from_elasticodbc_path(const char *module_name);
void AlreadyLoadedElasticsearchodbc(void);
#endif /* WIN32 */

#ifdef __cplusplus
}
#endif
#endif /* __LOADLIB_H__ */
