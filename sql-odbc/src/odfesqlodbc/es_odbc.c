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

#ifdef WIN32
#ifdef _DEBUG
#include <crtdbg.h>
#endif /* _DEBUG */
#endif /* WIN32 */
#include <string.h>
#include "dlg_specific.h"
#include "environ.h"
#include "es_odbc.h"
#include "misc.h"

#ifdef WIN32
#include "loadlib.h"
#else
#include <libgen.h>
#endif

void unused_vargs(int cnt, ...) {
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-value"
#endif  // __APPLE__
    (void)(cnt);
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
}

static int exeesm = 0;
BOOL isMsAccess(void) {
    return 1 == exeesm;
}
BOOL isMsQuery(void) {
    return 2 == exeesm;
}
BOOL isSqlServr(void) {
    return 3 == exeesm;
}

RETCODE SQL_API SQLDummyOrdinal(void);

extern void *conns_cs, *common_cs;

int initialize_global_cs(void) {
    static int init = 1;

    if (!init)
        return 0;
    init = 0;
#ifdef WIN32
#ifdef _DEBUG
#ifdef _MEMORY_DEBUG_
    _CrtSetDbgFlag(_CRTDBG_ALLOC_MEM_DF | _CRTDBG_LEAK_CHECK_DF);
#endif /* _MEMORY_DEBUG_ */
#endif /* _DEBUG */
#endif /* WIN32 */
#ifdef POSIX_THREADMUTEX_SUPPORT
    getMutexAttr();
#endif /* POSIX_THREADMUTEX_SUPPORT */
    InitializeLogging();
    INIT_CONNS_CS;
    INIT_COMMON_CS;

    return 0;
}

static void finalize_global_cs(void) {
    DELETE_COMMON_CS;
    DELETE_CONNS_CS;
    FinalizeLogging();
#ifdef _DEBUG
#ifdef _MEMORY_DEBUG_
    // _CrtDumpMemoryLeaks();
#endif /* _MEMORY_DEBUG_ */
#endif /* _DEBUG */
}

#ifdef WIN32
HINSTANCE s_hModule; /* Saved module handle. */
/*	This is where the Driver Manager attaches to this Driver */
BOOL WINAPI DllMain(HANDLE hInst, ULONG ul_reason_for_call, LPVOID lpReserved) {
    const char *exename = GetExeProgramName();

    switch (ul_reason_for_call) {
        case DLL_PROCESS_ATTACH:
            s_hModule = hInst; /* Save for dialog boxes */

            if (stricmp(exename, "msaccess") == 0)
                exeesm = 1;
            else if (strnicmp(exename, "msqry", 5) == 0)
                exeesm = 2;
            else if (strnicmp(exename, "sqlservr", 8) == 0)
                exeesm = 3;
            initialize_global_cs();
            MYLOG(ES_DEBUG, "exe name=%s\n", exename);
            break;

        case DLL_THREAD_ATTACH:
            break;

        case DLL_PROCESS_DETACH:
            MYLOG(ES_DEBUG, "DETACHING %s\n", DRIVER_FILE_NAME);
            CleanupDelayLoadedDLLs();
            /* my(q)log is unavailable from here */
            finalize_global_cs();
            return TRUE;

        case DLL_THREAD_DETACH:
            break;

        default:
            break;
    }

    return TRUE;

    UNREFERENCED_PARAMETER(lpReserved);
}

#else /* not WIN32 */

#if defined(__GNUC__) || defined(__SUNPRO_C)

/* Shared library initializer and destructor, using gcc's attributes */

static void __attribute__((constructor)) elasticodbc_init(void) {
    initialize_global_cs();
}

static void __attribute__((destructor)) elasticodbc_fini(void) {
    finalize_global_cs();
}

#else  /* not __GNUC__ */

/* Shared library initialization on non-gcc systems. */
BOOL _init(void) {
    initialize_global_cs();
    return TRUE;
}

BOOL _fini(void) {
    finalize_global_cs();
    return TRUE;
}
#endif /* not __GNUC__ */
#endif /* not WIN32 */

/*
 *	This function is used to cause the Driver Manager to
 *	call functions by number rather than name, which is faster.
 *	The ordinal value of this function must be 199 to have the
 *	Driver Manager do this.  Also, the ordinal values of the
 *	functions must match the value of fFunction in SQLGetFunctions()
 */
RETCODE SQL_API SQLDummyOrdinal(void) {
    return SQL_SUCCESS;
}
