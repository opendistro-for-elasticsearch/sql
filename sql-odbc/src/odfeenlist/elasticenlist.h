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

#ifndef __ESENLIST_H__
#define __ESENLIST_H__

#ifdef __cplusplus
extern "C" {
#endif
#ifdef WIN32
#ifdef _HANDLE_ENLIST_IN_DTC_

#undef DLL_DECLARE
#ifdef _ESENLIST_FUNCS_IMPLEMENT_
#define DLL_DECLARE _declspec(dllexport)
#else
#ifdef _ESENLIST_FUNCS_IMPORT_
#define DLL_DECLARE _declspec(dllimport)
#else
#define DLL_DECLARE
#endif /* _ESENLIST_FUNCS_IMPORT_ */
#endif /* _ESENLIST_FUNCS_IMPLEMENT_ */

RETCODE EnlistInDtc(void *conn, void *pTra, int method);
RETCODE DtcOnDisconnect(void *);
RETCODE IsolateDtcConn(void *, BOOL continueConnection);
//	for testing
DLL_DECLARE void *GetTransactionObject(HRESULT *hres);
DLL_DECLARE void ReleaseTransactionObject(void *);

#endif /* _HANDLE_ENLIST_IN_DTC_ */
#endif /* WIN32 */

#ifdef __cplusplus
}
#endif
#endif /* __ESENLIST_H__ */
