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

#define _MYLOG_FUNCS_IMPLEMENT_
#include <ctype.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include "dlg_specific.h"
#include "es_helper.h"
#include "es_odbc.h"
#include "misc.h"

#ifndef WIN32
#include <errno.h>
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>
#define GENERAL_ERRNO (errno)
#define GENERAL_ERRNO_SET(e) (errno = e)
#else
#define GENERAL_ERRNO (GetLastError())
#define GENERAL_ERRNO_SET(e) SetLastError(e)
#include <process.h> /* Byron: is this where Windows keeps def.
								 * of getpid ? */
#endif

#ifdef WIN32
#define DIRSEPARATOR "\\"
#define ES_BINARY O_BINARY
#define ES_BINARY_R "rb"
#define ES_BINARY_W "wb"
#define ES_BINARY_A "ab"
#else
#define DIRSEPARATOR "/"
#define ES_BINARY 0
#define ES_BINARY_R "r"
#define ES_BINARY_W "w"
#define ES_BINARY_A "a"
#endif /* WIN32 */

static char *logdir = NULL;

void generate_filename(const char *dirname, const char *prefix, char *filename,
                       size_t filenamelen) {
    const char *exename = GetExeProgramName();
#ifdef WIN32
    int pid;

    pid = _getpid();
#else
    pid_t pid;
    struct passwd *ptr;

    ptr = getpwuid(getuid());
    pid = getpid();
#endif
    if (dirname == 0 || filename == 0)
        return;

    snprintf(filename, filenamelen, "%s%s", dirname, DIRSEPARATOR);
    if (prefix != 0)
        strlcat(filename, prefix, filenamelen);
    if (exename[0])
        snprintfcat(filename, filenamelen, "%s_", exename);
#ifndef WIN32
    if (ptr)
        strlcat(filename, ptr->pw_name, filenamelen);
#endif
    snprintfcat(filename, filenamelen, "%u%s", pid, ".log");
    return;
}

static void generate_homefile(const char *prefix, char *filename,
                              size_t filenamelen) {
    char dir[PATH_MAX];
#ifdef WIN32
    const char *ptr;

    dir[0] = '\0';
    if (ptr = getenv("HOMEDRIVE"), NULL != ptr)
        strlcat(dir, ptr, filenamelen);
    if (ptr = getenv("HOMEPATH"), NULL != ptr)
        strlcat(dir, ptr, filenamelen);
#else
    STRCPY_FIXED(dir, "~");
#endif /* WIN32 */
    generate_filename(dir, prefix, filename, filenamelen);

    return;
}

#ifdef WIN32
static char exename[_MAX_FNAME];
#elif defined MAXNAMELEN
static char exename[MAXNAMELEN];
#else
static char exename[256];
#endif

const char *GetExeProgramName() {
    static int init = 1;

    if (init) {
        UCHAR *p;
#ifdef WIN32
        char pathname[_MAX_PATH];

        if (GetModuleFileName(NULL, pathname, sizeof(pathname)) > 0)
            _splitpath(pathname, NULL, NULL, exename, NULL);
#else
        CSTR flist[] = {"/proc/self/exe", "/proc/curproc/file",
                        "/proc/curproc/exe"};
        unsigned long i;
        char path_name[256];

        for (i = 0; i < sizeof(flist) / sizeof(flist[0]); i++) {
            if (readlink(flist[i], path_name, sizeof(path_name)) > 0) {
                /* fprintf(stderr, "i=%d pathname=%s\n", i, path_name); */
                STRCPY_FIXED(exename, po_basename(path_name));
                break;
            }
        }
#endif /* WIN32 */
        for (p = (UCHAR *)exename; '\0' != *p; p++) {
            if (isalnum(*p))
                continue;
            switch (*p) {
                case '_':
                case '-':
                    continue;
            }
            *p = '\0'; /* avoid multi bytes for safety */
            break;
        }
        init = 0;
    }
    return exename;
}

static void *qlog_cs, *mylog_cs;

static int mylog_on = ES_WARNING, qlog_on = ES_WARNING;

#define INIT_QLOG_CS XPlatformInitializeCriticalSection(&qlog_cs)
#define ENTER_QLOG_CS XPlatformEnterCriticalSection(qlog_cs)
#define LEAVE_QLOG_CS XPlatformLeaveCriticalSection(qlog_cs)
#define DELETE_QLOG_CS XPlatformDeleteCriticalSection(&qlog_cs)
#define INIT_MYLOG_CS XPlatformInitializeCriticalSection(&mylog_cs)
#define ENTER_MYLOG_CS XPlatformEnterCriticalSection(mylog_cs)
#define LEAVE_MYLOG_CS XPlatformLeaveCriticalSection(mylog_cs)
#define DELETE_MYLOG_CS XPlatformDeleteCriticalSection(&mylog_cs)

#define MYLOGFILE "mylog_"
#ifndef WIN32
#define MYLOGDIR "/tmp"
#else
#define MYLOGDIR "c:"
#endif /* WIN32 */

#define QLOGFILE "elasticodbc_"
#ifndef WIN32
#define QLOGDIR "/tmp"
#else
#define QLOGDIR "c:"
#endif /* WIN32 */

int get_mylog(void) {
    return mylog_on;
}
int get_qlog(void) {
    return qlog_on;
}

const char *po_basename(const char *path) {
    char *p;

    if (p = strrchr(path, DIRSEPARATOR[0]), NULL != p)
        return p + 1;
    return path;
}

void logs_on_off(int cnopen, int mylog_onoff, int qlog_onoff) {
    static int mylog_on_count = 0, mylog_off_count = 0, qlog_on_count = 0,
               qlog_off_count = 0;

    ENTER_MYLOG_CS;
    if (mylog_onoff)
        mylog_on_count += cnopen;
    else
        mylog_off_count += cnopen;
    if (mylog_on_count > 0) {
        if (mylog_onoff > mylog_on)
            mylog_on = mylog_onoff;
        else if (mylog_on < 1)
            mylog_on = 1;
    } else if (mylog_off_count > 0)
        mylog_on = 0;
    else if (getGlobalDebug() > 0)
        mylog_on = getGlobalDebug();
    LEAVE_MYLOG_CS;

    ENTER_QLOG_CS;
    if (qlog_onoff)
        qlog_on_count += cnopen;
    else
        qlog_off_count += cnopen;
    if (qlog_on_count > 0) {
        if (qlog_onoff > qlog_on)
            qlog_on = qlog_onoff;
        else if (qlog_on < 1)
            qlog_on = 1;
    } else if (qlog_off_count > 0)
        qlog_on = 0;
    else if (getGlobalCommlog() > 0)
        qlog_on = getGlobalCommlog();
    LEAVE_QLOG_CS;
    MYLOG(ES_DEBUG, "mylog_on=%d qlog_on=%d\n", mylog_on, qlog_on);
}

#ifdef WIN32
#define LOGGING_PROCESS_TIME
#include <direct.h>
#endif /* WIN32 */
#ifdef LOGGING_PROCESS_TIME
#include <mmsystem.h>
static DWORD start_time = 0;
#endif /* LOGGING_PROCESS_TIME */
static FILE *MLOGFP = NULL;

static void MLOG_open() {
    char filebuf[80], errbuf[160];
    BOOL open_error = FALSE;

    if (MLOGFP)
        return;

    generate_filename(logdir ? logdir : MYLOGDIR, MYLOGFILE, filebuf,
                      sizeof(filebuf));
    MLOGFP = fopen(filebuf, ES_BINARY_A);
    if (!MLOGFP) {
        int lasterror = GENERAL_ERRNO;

        open_error = TRUE;
        SPRINTF_FIXED(errbuf, "%s open error %d\n", filebuf, lasterror);
        generate_homefile(MYLOGFILE, filebuf, sizeof(filebuf));
        MLOGFP = fopen(filebuf, ES_BINARY_A);
    }
    if (MLOGFP) {
        if (open_error)
            fputs(errbuf, MLOGFP);
    }
}

static int mylog_misc(unsigned int option, const char *fmt, va_list args) {
    // va_list		args;
    int gerrno;
    BOOL log_threadid = option;

    gerrno = GENERAL_ERRNO;
    ENTER_MYLOG_CS;
#ifdef LOGGING_PROCESS_TIME
    if (!start_time)
        start_time = timeGetTime();
#endif /* LOGGING_PROCESS_TIME */
    // va_start(args, fmt);

    if (!MLOGFP) {
        MLOG_open();
        if (!MLOGFP)
            mylog_on = 0;
    }

    if (MLOGFP) {
        if (log_threadid) {
#ifdef WIN_MULTITHREAD_SUPPORT
#ifdef LOGGING_PROCESS_TIME
            DWORD proc_time = timeGetTime() - start_time;
            fprintf(MLOGFP, "[%u-%d.%03d]", GetCurrentThreadId(),
                    proc_time / 1000, proc_time % 1000);
#else
            fprintf(MLOGFP, "[%u]", GetCurrentThreadId());
#endif /* LOGGING_PROCESS_TIME */
#endif /* WIN_MULTITHREAD_SUPPORT */
#if defined(POSIX_MULTITHREAD_SUPPORT)
            fprintf(MLOGFP, "[%lx]", (unsigned long int)pthread_self());
#endif /* POSIX_MULTITHREAD_SUPPORT */
        }
        vfprintf(MLOGFP, fmt, args);
        fflush(MLOGFP);
    }

    // va_end(args);
    LEAVE_MYLOG_CS;
    GENERAL_ERRNO_SET(gerrno);

    return 1;
}

DLL_DECLARE int mylog(const char *fmt, ...) {
    int ret = 0;
    unsigned int option = 1;
    va_list args;

    if (!mylog_on)
        return ret;

    va_start(args, fmt);
    ret = mylog_misc(option, fmt, args);
    va_end(args);
    return ret;
}

DLL_DECLARE int myprintf(const char *fmt, ...) {
    int ret = 0;
    va_list args;

    va_start(args, fmt);
    ret = mylog_misc(0, fmt, args);
    va_end(args);
    return ret;
}

static void mylog_initialize(void) {
    INIT_MYLOG_CS;
}
static void mylog_finalize(void) {
    mylog_on = 0;
    if (MLOGFP) {
        fclose(MLOGFP);
        MLOGFP = NULL;
    }
    DELETE_MYLOG_CS;
}

static FILE *QLOGFP = NULL;

static int qlog_misc(unsigned int option, const char *fmt, va_list args) {
    char filebuf[80];
    int gerrno;

    if (!qlog_on)
        return 0;

    gerrno = GENERAL_ERRNO;
    ENTER_QLOG_CS;
#ifdef LOGGING_PROCESS_TIME
    if (!start_time)
        start_time = timeGetTime();
#endif /* LOGGING_PROCESS_TIME */

    if (!QLOGFP) {
        generate_filename(logdir ? logdir : QLOGDIR, QLOGFILE, filebuf,
                          sizeof(filebuf));
        QLOGFP = fopen(filebuf, ES_BINARY_A);
        if (!QLOGFP) {
            generate_homefile(QLOGFILE, filebuf, sizeof(filebuf));
            QLOGFP = fopen(filebuf, ES_BINARY_A);
        }
        if (!QLOGFP)
            qlog_on = 0;
    }

    if (QLOGFP) {
        if (option) {
#ifdef LOGGING_PROCESS_TIME
            DWORD proc_time = timeGetTime() - start_time;
            fprintf(QLOGFP, "[%d.%03d]", proc_time / 1000, proc_time % 1000);
#endif /* LOGGING_PROCESS_TIME */
        }
        vfprintf(QLOGFP, fmt, args);
        fflush(QLOGFP);
    }

    LEAVE_QLOG_CS;
    GENERAL_ERRNO_SET(gerrno);

    return 1;
}
int qlog(const char *fmt, ...) {
    int ret = 0;
    unsigned int option = 1;
    va_list args;

    if (!qlog_on)
        return ret;

    va_start(args, fmt);
    ret = qlog_misc(option, fmt, args);
    va_end(args);
    return ret;
}
int qprintf(char *fmt, ...) {
    int ret = 0;
    va_list args;

    va_start(args, fmt);
    ret = qlog_misc(0, fmt, args);
    va_end(args);
    return ret;
}

static void qlog_initialize(void) {
    INIT_QLOG_CS;
}
static void qlog_finalize(void) {
    qlog_on = 0;
    if (QLOGFP) {
        fclose(QLOGFP);
        QLOGFP = NULL;
    }
    DELETE_QLOG_CS;
}

static int globalDebug = -1;
int getGlobalDebug() {
    char temp[16];

    if (globalDebug >= 0)
        return globalDebug;
    /* Debug is stored in the driver section */
    SQLGetPrivateProfileString(DBMS_NAME, INI_LOG_LEVEL, "", temp, sizeof(temp),
                               ODBCINST_INI);
    if (temp[0])
        globalDebug = atoi(temp);
    else
        globalDebug = DEFAULT_LOGLEVEL;

    return globalDebug;
}

int setGlobalDebug(int val) {
    return (globalDebug = val);
}

static int globalCommlog = -1;
int getGlobalCommlog() {
    char temp[16];

    if (globalCommlog >= 0)
        return globalCommlog;
    /* Commlog is stored in the driver section */
    SQLGetPrivateProfileString(DBMS_NAME, INI_LOG_LEVEL, "", temp, sizeof(temp),
                               ODBCINST_INI);
    if (temp[0])
        globalCommlog = atoi(temp);
    else
        globalCommlog = DEFAULT_LOGLEVEL;

    return globalCommlog;
}

int setGlobalCommlog(int val) {
    return (globalCommlog = val);
}

int writeGlobalLogs() {
    char temp[10];

    ITOA_FIXED(temp, globalDebug);
    SQLWritePrivateProfileString(DBMS_NAME, INI_LOG_LEVEL, temp, ODBCINST_INI);
    ITOA_FIXED(temp, globalCommlog);
    SQLWritePrivateProfileString(DBMS_NAME, INI_LOG_LEVEL, temp, ODBCINST_INI);
    return 0;
}

void logInstallerError(int ret, const char *dir) {
    DWORD err = (DWORD)ret;
    char msg[SQL_MAX_MESSAGE_LENGTH] = "";
    msg[0] = '\0';
    ret = SQLInstallerError(1, &err, msg, sizeof(msg), NULL);
    if (msg[0] != '\0')
        MYLOG(ES_DEBUG, "Dir= %s ErrorMsg = %s\n", dir, msg);
}

int getLogDir(char *dir, int dirmax) {
    int ret = SQLGetPrivateProfileString(DBMS_NAME, INI_LOG_OUTPUT, "",
                                                dir, dirmax, ODBCINST_INI);
    if (!ret)
        logInstallerError(ret, dir);
    return ret;
}

int setLogDir(const char *dir) {
    int ret = SQLWritePrivateProfileString(DBMS_NAME, INI_LOG_OUTPUT, dir,
                                           ODBCINST_INI);
    if (!ret) 
        logInstallerError(ret, dir);
    return ret;
}

/*
 *	This function starts a logging out of connections according the ODBCINST.INI
 *	portion of the DBMS_NAME registry.
 */
static void start_logging() {
    /*
     * GlobalDebug or GlobalCommlog means whether take mylog or commlog
     * out of the connection time or not but doesn't mean the default of
     * ci->drivers.debug(commlog).
     */
    logs_on_off(0, 0, 0);
    mylog("\t%s:Global.debug&commlog=%d&%d\n", __FUNCTION__, getGlobalDebug(),
          getGlobalCommlog());
}

void InitializeLogging(void) {
    char dir[PATH_MAX];
    getLogDir(dir, sizeof(dir));
    if (dir[0])
        logdir = strdup(dir);
    mylog_initialize();
    qlog_initialize();
    start_logging();
    MYLOG(ES_DEBUG, "Log Output Dir: %s\n", logdir);
}

void FinalizeLogging(void) {
    mylog_finalize();
    qlog_finalize();
    if (logdir) {
        free(logdir);
        logdir = NULL;
    }
}
