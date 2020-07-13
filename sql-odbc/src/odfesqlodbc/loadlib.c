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

#include <ctype.h>
#include <stdio.h>
#include <string.h>
#ifndef WIN32
#include <errno.h>
#endif /* WIN32 */

#include "elasticenlist.h"
#include "loadlib.h"
#include "misc.h"

#ifdef WIN32
#ifdef _MSC_VER
#pragma comment(lib, "Delayimp")
#ifdef _HANDLE_ENLIST_IN_DTC_
#ifdef UNICODE_SUPPORT
#pragma comment(lib, "elasticenlist")
#else
#pragma comment(lib, "elasticenlista")
#endif /* UNICODE_SUPPORT */
#endif /* _HANDLE_ENLIST_IN_DTC_ */
// The followings works under VC++6.0 but doesn't work under VC++7.0.
// Please add the equivalent linker options using command line etc.
#if (_MSC_VER == 1200) && defined(DYNAMIC_LOAD)  // VC6.0
#ifdef UNICODE_SUPPORT
#pragma comment(linker, "/Delayload:elasticenlist.dll")
#else
#pragma comment(linker, "/Delayload:elasticenlista.dll")
#endif /* UNICODE_SUPPORT */
#pragma comment(linker, "/Delay:UNLOAD")
#endif /* _MSC_VER */
#endif /* _MSC_VER */

#if defined(DYNAMIC_LOAD)
#define WIN_DYN_LOAD
#ifdef UNICODE_SUPPORT
CSTR elasticenlist = "elasticenlist";
CSTR elasticenlistdll = "elasticenlist.dll";
CSTR elasticodbc = "odfesqlodbc35w";
CSTR elasticodbcdll = "odfesqlodbc35w.dll";
#else
CSTR elasticenlist = "elasticenlista";
CSTR elasticenlistdll = "elasticenlista.dll";
CSTR elasticodbc = "odfesqlodbc30a";
CSTR elasticodbcdll = "odfesqlodbc30a.dll";
#endif /* UNICODE_SUPPORT */
#if defined(_MSC_VER) && (_MSC_VER >= 1200)
#define _MSC_DELAY_LOAD_IMPORT
#endif /* MSC_VER */
#endif /* DYNAMIC_LOAD */
#endif /* WIN32 */

#if defined(_MSC_DELAY_LOAD_IMPORT)
/*
 *	Error hook function for delay load import.
 *	Try to load a DLL based on elasticodbc path.
 */
#if (_MSC_VER >= 1900) /* vc14 or later */
#define TRY_DLI_HOOK __try {
#define RELEASE_NOTIFY_HOOK
#elif (_MSC_VER < 1300) /* vc6 */
extern PfnDliHook __pfnDliFailureHook;
extern PfnDliHook __pfnDliNotifyHook;
#define TRY_DLI_HOOK                        \
    __try {                                 \
        __pfnDliFailureHook = DliErrorHook; \
        __pfnDliNotifyHook = DliErrorHook;
#define RELEASE_NOTIFY_HOOK __pfnDliNotifyHook = NULL;
#else /* vc7 ~ 12 */
extern PfnDliHook __pfnDliFailureHook2;
extern PfnDliHook __pfnDliNotifyHook2;
#define TRY_DLI_HOOK                         \
    __try {                                  \
        __pfnDliFailureHook2 = DliErrorHook; \
        __pfnDliNotifyHook2 = DliErrorHook;
#define RELEASE_NOTIFY_HOOK __pfnDliNotifyHook2 = NULL;
#endif /* _MSC_VER */
#else
#define TRY_DLI_HOOK __try {
#define RELEASE_NOTIFY_HOOK
#endif /* _MSC_DELAY_LOAD_IMPORT */

#if defined(_MSC_DELAY_LOAD_IMPORT)
static BOOL loaded_elasticenlist = FALSE;
static HMODULE enlist_module = NULL;
static BOOL loaded_elasticodbc = FALSE;
/*
 *	Load a DLL based on elasticodbc path.
 */
HMODULE MODULE_load_from_elasticodbc_path(const char *module_name) {
    extern HINSTANCE s_hModule;
    HMODULE hmodule = NULL;
    char szFileName[MAX_PATH];

    if (GetModuleFileName(s_hModule, szFileName, sizeof(szFileName)) > 0) {
        char drive[_MAX_DRIVE], dir[_MAX_DIR], sysdir[MAX_PATH];

        _splitpath(szFileName, drive, dir, NULL, NULL);
        GetSystemDirectory(sysdir, MAX_PATH);
        SPRINTF_FIXED(szFileName, "%s%s%s.dll", drive, dir, module_name);
        if (_strnicmp(szFileName, sysdir, strlen(sysdir)) != 0) {
            hmodule =
                LoadLibraryEx(szFileName, NULL, LOAD_WITH_ALTERED_SEARCH_PATH);
            MYLOG(ES_DEBUG, "elasticodbc path based %s loaded module=%p\n",
                  module_name, hmodule);
        }
    }
    return hmodule;
}

static FARPROC WINAPI DliErrorHook(unsigned dliNotify, PDelayLoadInfo pdli) {
    HMODULE hmodule = NULL;
    const char *call_module = NULL;

    MYLOG(ES_DEBUG, "Dli%sHook %s Notify=%d\n",
          (dliFailLoadLib == dliNotify || dliFailGetProc == dliNotify)
              ? "Error"
              : "Notify",
          NULL != pdli->szDll ? pdli->szDll : pdli->dlp.szProcName, dliNotify);
    switch (dliNotify) {
        case dliNotePreLoadLibrary:
        case dliFailLoadLib:
            RELEASE_NOTIFY_HOOK
            if (_strnicmp(pdli->szDll, elasticodbc, strlen(elasticodbc)) == 0)
                call_module = elasticodbc;
            if (call_module) {
                if (hmodule = MODULE_load_from_elasticodbc_path(call_module),
                    NULL == hmodule)
                    hmodule = LoadLibrary(call_module);
                if (NULL != hmodule) {
                    if (elasticenlist == call_module)
                        loaded_elasticenlist = TRUE;
                    else if (elasticodbc == call_module)
                        loaded_elasticodbc = TRUE;
                }
            }
            break;
    }
    return (FARPROC)hmodule;
}

void AlreadyLoadedElasticsearchodbc(void) {
    loaded_elasticodbc = TRUE;
}

/*
 *	unload delay loaded libraries.
 */

typedef BOOL(WINAPI *UnloadFunc)(LPCSTR);
void CleanupDelayLoadedDLLs(void) {
    BOOL success;
#if (_MSC_VER < 1300) /* VC6 DELAYLOAD IMPORT */
    UnloadFunc func = __FUnloadDelayLoadedDLL;
#else
    UnloadFunc func = __FUnloadDelayLoadedDLL2;
#endif
    /* The dll names are case sensitive for the unload helper */
    if (loaded_elasticenlist) {
        if (enlist_module != NULL) {
            MYLOG(ES_DEBUG, "Freeing Library %s\n", elasticenlistdll);
            FreeLibrary(enlist_module);
        }
        MYLOG(ES_DEBUG, "%s unloading\n", elasticenlistdll);
        success = (*func)(elasticenlistdll);
        MYLOG(ES_DEBUG, "%s unloaded success=%d\n", elasticenlistdll, success);
        loaded_elasticenlist = FALSE;
    }
    if (loaded_elasticodbc) {
        MYLOG(ES_DEBUG, "%s unloading\n", elasticodbcdll);
        success = (*func)(elasticodbcdll);
        MYLOG(ES_DEBUG, "%s unloaded success=%d\n", elasticodbcdll, success);
        loaded_elasticodbc = FALSE;
    }
    return;
}
#else
void CleanupDelayLoadedDLLs(void) {
    return;
}
#endif /* _MSC_DELAY_LOAD_IMPORT */

#ifdef _HANDLE_ENLIST_IN_DTC_
RETCODE CALL_EnlistInDtc(ConnectionClass *conn, void *pTra, int method) {
    RETCODE ret;
    BOOL loaded = TRUE;

#if defined(_MSC_DELAY_LOAD_IMPORT)
    if (!loaded_elasticenlist) {
        TRY_DLI_HOOK
        ret = EnlistInDtc(conn, pTra, method);
    }
    __except ((GetExceptionCode() & 0xffff) == ERROR_MOD_NOT_FOUND
                  ? EXCEPTION_EXECUTE_HANDLER
                  : EXCEPTION_CONTINUE_SEARCH) {
        if (enlist_module = MODULE_load_from_elasticodbc_path(elasticenlist),
            NULL == enlist_module)
            loaded = FALSE;
        else
            ret = EnlistInDtc(conn, pTra, method);
    }
    if (loaded)
        loaded_elasticenlist = TRUE;
    RELEASE_NOTIFY_HOOK
}
else ret = EnlistInDtc(conn, pTra, method);
#else
    ret = EnlistInDtc(conn, pTra, method);
    loaded_elasticenlist = TRUE;
#endif /* _MSC_DELAY_LOAD_IMPORT */
return ret;
}
RETCODE CALL_DtcOnDisconnect(ConnectionClass *conn) {
    if (loaded_elasticenlist)
        return DtcOnDisconnect(conn);
    return FALSE;
}
RETCODE CALL_IsolateDtcConn(ConnectionClass *conn, BOOL continueConnection) {
    if (loaded_elasticenlist)
        return IsolateDtcConn(conn, continueConnection);
    return FALSE;
}

void *CALL_GetTransactionObject(HRESULT *hres) {
    void *ret = NULL;
    BOOL loaded = TRUE;

#if defined(_MSC_DELAY_LOAD_IMPORT)
    if (!loaded_elasticenlist) {
        TRY_DLI_HOOK
        ret = GetTransactionObject(hres);
    }
    __except ((GetExceptionCode() & 0xffff) == ERROR_MOD_NOT_FOUND
                  ? EXCEPTION_EXECUTE_HANDLER
                  : EXCEPTION_CONTINUE_SEARCH) {
        if (enlist_module = MODULE_load_from_elasticodbc_path(elasticenlist),
            NULL == enlist_module)
            loaded = FALSE;
        else
            ret = GetTransactionObject(hres);
    }
    if (loaded)
        loaded_elasticenlist = TRUE;
    RELEASE_NOTIFY_HOOK
}
else ret = GetTransactionObject(hres);
#else
    ret = GetTransactionObject(hres);
    loaded_elasticenlist = TRUE;
#endif /* _MSC_DELAY_LOAD_IMPORT */
return ret;
}
void CALL_ReleaseTransactionObject(void *pObj) {
    if (loaded_elasticenlist)
        ReleaseTransactionObject(pObj);
    return;
}
#endif /* _HANDLE_ENLIST_IN_DTC_ */
