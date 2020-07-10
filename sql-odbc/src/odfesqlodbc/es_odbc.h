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

#ifndef __ESODBC_H__
#define __ESODBC_H__
#include <stdarg.h>

/* #define	__MS_REPORTS_ANSI_CHAR__ */
void unused_vargs(int cnt, ...);
#define UNUSED(...) unused_vargs(0, __VA_ARGS__)

#ifdef WIN32
#define WIN32_LEAN_AND_MEAN
#include <windows.h>
#elif __APPLE__

#elif __linux__
#include "linux/kconfig.h"
#endif

#include <stdio.h> /* for FILE* pointers: see GLOBAL_VALUES */

#include "version.h"

#ifdef WIN32
#ifdef _DEBUG
#ifndef _MEMORY_DEBUG_
#include <stdlib.h>
#if (_MSC_VER < 1400) /* in case of VC7 or under */
#include <malloc.h>
#endif /* _MSC_VER */
#define _CRTDBG_MAP_ALLOC
#include <crtdbg.h>
#endif /* _MEMORY_DEBUG_ */
#else  /* _DEBUG */
#include <stdlib.h>
#endif /* _DEBUG */
#else  /* WIN32 */
#include <stdlib.h>
#endif /* WIN32 */

#if defined(__GNUC__) || defined(__IBMC__)
#if ((__GNUC__ * 100) + __GNUC_MINOR__) >= 404
#define ES_PRINTF_ATTRIBUTE gnu_printf
#else
#define ES_PRINTF_ATTRIBUTE printf
#endif
#define es_attribute_printf(f, a) \
    __attribute__((format(ES_PRINTF_ATTRIBUTE, f, a)))
#else
#define __attribute__(x)
#define es_attribute_printf(f, a)
#endif /* __GNUC__ || __IBMC__ */

#ifdef _MEMORY_DEBUG_
void *esdebug_alloc(size_t);
void *esdebug_calloc(size_t, size_t);
void *esdebug_realloc(void *, size_t);
char *esdebug_strdup(const char *);
void *esdebug_memcpy(void *, const void *, size_t);
void *esdebug_memset(void *, int c, size_t);
char *esdebug_strcpy(char *, const char *);
char *esdebug_strncpy(char *, const char *, size_t);
char *esdebug_strncpy_null(char *, const char *, size_t);
void esdebug_free(void *);
void debug_memory_check(void);

#ifdef WIN32
#undef strdup
#endif /* WIN32 */
#define malloc esdebug_alloc
#define realloc esdebug_realloc
#define calloc esdebug_calloc
#define strdup esdebug_strdup
#define free esdebug_free
#define strcpy esdebug_strcpy
#define strncpy esdebug_strncpy
/* #define strncpy_null	esdebug_strncpy_null */
#define memcpy esdebug_memcpy
#define memset esdebug_memset
#endif /* _MEMORY_DEBUG_ */

#ifdef WIN32
#pragma warning(push)
#pragma warning(disable : 4201)  // nonstandard extension used: nameless
                                 // struct/union warning
#include <delayimp.h>
#pragma warning(pop)
#endif /* WIN32 */
/* Must come before sql.h */
#define ODBCVER 0x0351

/*
 * Default NAMEDATALEN value in the server. The server can be compiled with
 * a different value, but this will do.
 */
#define NAMEDATALEN_V73 64

#ifndef NAMESTORAGELEN
#define NAMESTORAGELEN 64
#endif /* NAMESTORAGELEN */

#if defined(WIN32) || defined(WITH_UNIXODBC) || defined(WITH_IODBC)
#ifdef WIN32
#pragma warning(push)
#pragma warning(disable : 4201)  // nonstandard extension used: nameless
                                 // struct/union warning
#endif                           // WIN32
#include <sql.h>
#include <sqlext.h>
#include <sqltypes.h>
#if WIN32
#pragma warning(pop)
#endif                                  // WIN32
#if defined(WIN32) && (_MSC_VER < 1300) /* in case of VC6 or under */
#define SQLLEN SQLINTEGER
#define SQLULEN SQLUINTEGER
#define SQLSETPOSIROW SQLUSMALLINT
/* VC6 bypasses 64bit mode. */
#define DWLP_USER DWL_USER
#define ULONG_PTR ULONG
#define LONG_PTR LONG
#define SetWindowLongPtr(hdlg, DWLP_USER, lParam) \
    SetWindowLong(hdlg, DWLP_USER, lParam)
#define GetWindowLongPtr(hdlg, DWLP_USER) GetWindowLong(hdlg, DWLP_USER);
#endif
#else
#include "iodbc.h"
#include "isql.h"
#include "isqlext.h"
#endif /* WIN32 */

#if defined(WIN32)
#include <odbcinst.h>
#elif defined(WITH_UNIXODBC)
#include <odbcinst.h>
#elif defined(WITH_IODBC)
#include <iodbcinst.h>
#endif

#ifdef __cplusplus
extern "C" {
#endif

#define Int4 int
#define UInt4 unsigned int
#define Int2 short
#define UInt2 unsigned short
typedef SQLBIGINT Int8;
typedef UInt4 OID;

#ifndef SQL_TRUE
#define SQL_TRUE TRUE
#endif /* SQL_TRUE */
#ifndef SQL_FALSE
#define SQL_FALSE FALSE
#endif /* SQL_FALSE */

#define FORMAT_SMALLI "%d"  /* SQLSMALLINT */
#define FORMAT_USMALLI "%u" /* SQLUSMALLINT */
#ifdef WIN32
#ifndef SSIZE_T_DEFINED
#define ssize_t SSIZE_T
#define SSIZE_T_DEFINED
#endif                        /* SSIZE_T */
#define FORMAT_SIZE_T "%Iu"   /* size_t */
#define FORMAT_SSIZE_T "%Id"  /* ssize_t */
#define FORMAT_INTEGER "%ld"  /* SQLINTEGER */
#define FORMAT_UINTEGER "%lu" /* SQLUINTEGER */
#define FORMATI64 "%I64d"     /* SQLBIGINT */
#define FORMATI64U "%I64u"    /* SQLUBIGINT */
#ifdef _WIN64
#define FORMAT_LEN "%I64d"  /* SQLLEN */
#define FORMAT_ULEN "%I64u" /* SQLULEN */
#define FORMAT_POSIROW "%I64u"
#else                     /* _WIN64 */
#define FORMAT_LEN "%ld"  /* SQLLEN */
#define FORMAT_ULEN "%lu" /* SQLULEN */
#define FORMAT_POSIROW "%hu"
#endif                       /* _WIN64 */
#else                        /* WIN32 */
#define FORMAT_SIZE_T "%zu"  /* size_t */
#define FORMAT_SSIZE_T "%zd" /* ssize_t */
#ifndef HAVE_SSIZE_T
typedef long ssize_t;
#endif /* HAVE_SSIZE_T */

#ifndef SIZEOF_VOID_P
#ifdef __APPLE__
#define SIZEOF_VOID_P 8
#else
#error "SIZEOF_VOID_P must be defined"
#endif  // __APPLE__
#endif  // SIZEOF_VOID_P

#ifndef SIZEOF_LONG
#ifdef __APPLE__
#define SIZEOF_LONG 8
#else
#error "SIZEOF_LONG must be defined"
#endif  // __APPLE__
#endif  // SIZEOF_LONG

#if (SIZEOF_VOID_P == SIZEOF_LONG) /* ILP32 or LP64 */
typedef long LONG_PTR;
typedef unsigned long ULONG_PTR;
#elif defined(HAVE_LONG_LONG)      /* LLP64 */
typedef long long LONG_PTR;
typedef unsigned long long ULONG_PTR;
#else                              /* SIZEOF_VOID_P */
#error appropriate long pointer type not found
#endif                       /* SIZEOF_VOID_P */
#if (SIZEOF_LONG == 8)       /* LP64 */
#define FORMAT_INTEGER "%d"  /* SQLINTEGER */
#define FORMAT_UINTEGER "%u" /* SQLUINTEGER */
#define FORMATI64 "%ld"      /* SQLBIGINT */
#define FORMATI64U "%lu"     /* SQLUBIGINT */
#if defined(WITH_UNIXODBC) && defined(BUILD_LEGACY_64_BIT_MODE)
#define FORMAT_LEN "%d"       /* SQLLEN */
#define FORMAT_ULEN "%u"      /* SQLULEN */
#else                         /* WITH_UNIXODBC */
#define FORMAT_LEN "%ld"      /* SQLLEN */
#define FORMAT_ULEN "%lu"     /* SQLULEN */
#endif                        /* WITH_UNIXODBC */
#else                         /* SIZEOF_LONG */
#define FORMAT_INTEGER "%ld"  /* SQLINTEGER */
#define FORMAT_UINTEGER "%lu" /* SQLUINTEGER */
#if defined(HAVE_LONG_LONG)
#define FORMATI64 "%lld"   /* SQLBIGINT */
#define FORMATI64U "%llu"  /* SQLUBIGINT */
#if (SIZEOF_VOID_P == 8)   /* LLP64 */
#define FORMAT_LEN "%lld"  /* SQLLEN */
#define FORMAT_ULEN "%llu" /* SQLULEN */
#else                      /* SIZEOF_VOID_P ILP32 */
#define FORMAT_LEN "%ld"   /* SQLLEN */
#define FORMAT_ULEN "%lu"  /* SQLULEN */
#endif                     /* SIZEOF_VOID_P */
#else                      /* HAVE_LONG_LONG */
#define FORMAT_LEN "%ld"   /* SQLLEN */
#define FORMAT_ULEN "%lu"  /* SQLULEN */
#endif                     /* HAVE_LONG_LONG */
#endif                     /* SIZEOF_LONG */

#if (SIZEOF_VOID_P == 8) && !defined(WITH_IODBC)
#define FORMAT_POSIROW FORMAT_ULEN
#else
#define FORMAT_POSIROW "%u"
#endif

#endif /* WIN32 */

#define CAST_PTR(type, ptr) (type)((LONG_PTR)(ptr))
#define CAST_UPTR(type, ptr) (type)((ULONG_PTR)(ptr))
#ifndef SQL_IS_LEN
#define SQL_IS_LEN (-1000)
#endif /* SQL_IS_LEN */
#ifdef HAVE_SIGNED_CHAR
typedef signed char po_ind_t;
#else
typedef char po_ind_t;
#endif /* HAVE_SIGNED_CHAR */

#ifndef WIN32
#if !defined(WITH_UNIXODBC) && !defined(WITH_IODBC)
typedef float SFLOAT;
typedef double SDOUBLE;
#endif /* WITH_UNIXODBC */

#ifndef CALLBACK
#define CALLBACK
#endif /* CALLBACK */
#endif /* WIN32 */

#ifndef WIN32
#define stricmp strcasecmp
#define strnicmp strncasecmp
#ifndef TRUE
#define TRUE (BOOL)1
#endif /* TRUE */
#ifndef FALSE
#define FALSE (BOOL)0
#endif /* FALSE */
#else

#if (_MSC_VER < 1900) /* vc12 or under */
#define POSIX_SNPRINTF_REQUIRED
#define snprintf posix_snprintf
extern int posix_snprintf(char *buf, size_t size, const char *format, ...);
#endif /* _MSC_VER */
#ifndef strdup
#define strdup _strdup
#endif /* strdup */
#define strnicmp _strnicmp
#define stricmp _stricmp
#endif /* WIN32 */

#define IS_NOT_SPACE(c) ((c) && !isspace((UCHAR)c))

#ifndef SQL_ATTR_APP_ROW_DESC
#define SQL_ATTR_APP_ROW_DESC 10010
#endif
#ifndef SQL_ATTR_APP_PARAM_DESC
#define SQL_ATTR_APP_PARAM_DESC 10011
#endif
#ifndef SQL_ATTR_IMP_ROW_DESC
#define SQL_ATTR_IMP_ROW_DESC 10012
#endif
#ifndef SQL_ATTR_IMP_PARAM_DESC
#define SQL_ATTR_IMP_PARAM_DESC 10013
#endif

/* Driver stuff */

#define DRIVERNAME "Elasticsearch ODBC"

#define DBMS_NAME_UNICODE "Elasticsearch Unicode"
#define DBMS_NAME_ANSI "Elasticsearch ANSI"

#define DRIVER_ODBC_VER "03.51"

#ifdef UNICODE_SUPPORT
#define WCLEN sizeof(SQLWCHAR)
SQLULEN ucs2strlen(const SQLWCHAR *);
#else
#undef SQL_WCHAR
#undef SQL_WVARCHAR
#undef SQL_WLONGVARCHAR
#undef SQL_C_WCHAR
#define SQL_WCHAR SQL_WCHAR_IS_INHIBITED
#define SQL_WVARCHAR SQL_WVARCHAR_IS_INHIBITED
#define SQL_WLONGVARCHAR SQL_WLONGVARCHAR_IS_INHIBITED
#define SQL_C_WCHAR SQL_C_WCHAR_IS_INHIBITED
#endif

#ifndef DBMS_NAME
#ifdef _WIN64
#ifdef UNICODE_SUPPORT
#define DBMS_NAME DBMS_NAME_UNICODE "(x64)"
#else
#define DBMS_NAME DBMS_NAME_ANSI "(x64)"
#endif /* UNICODE_SUPPORT */
#else  /* _WIN64 */
#ifdef UNICODE_SUPPORT
#define DBMS_NAME DBMS_NAME_UNICODE
#else
#define DBMS_NAME DBMS_NAME_ANSI
#endif /* UNICODE_SUPPORT */
#endif /* _WIN64 */
#endif /* DBMS_NAME */

#ifndef DBMS_NAME
#define DBMS_NAME "Elasticsearch Legacy"
#endif /* DBMS_NAME */
#ifdef WIN32
#ifdef UNICODE_SUPPORT
#define DRIVER_FILE_NAME "odfesqlodbc.dll"
#else
#define DRIVER_FILE_NAME "odfesqlodbc.dll"
#endif /* UNICODE_SUPPORT */
#else
#ifdef UNICODE_SUPPORT
#define DRIVER_FILE_NAME "libodfesqlodbc.dylib"
#else
#define DRIVER_FILE_NAME "libodfesqlodbc.dylib"
#endif
#endif /* WIN32 */
BOOL isMsAccess(void);
BOOL isMsQuery(void);
BOOL isSqlServr(void);

/* ESCAPEs */
#define ESCAPE_IN_LITERAL '\\'
#define BYTEA_ESCAPE_CHAR '\\'
#define SEARCH_PATTERN_ESCAPE '\\'
#define LITERAL_QUOTE '\''
#define IDENTIFIER_QUOTE '\"'
#define ODBC_ESCAPE_START '{'
#define ODBC_ESCAPE_END '}'
#define DOLLAR_QUOTE '$'
#define LITERAL_EXT 'E'
#define ES_CARRIAGE_RETURN '\r'
#define ES_LINEFEED '\n'

/* Limits */
#define MAXESPATH 1024

/* see an easy way round this - DJP 24-1-2001 */
#define MAX_CONNECT_STRING 4096
#define FETCH_MAX                            \
    100 /* default number of rows to cache \ \
         * for declare/fetch */
#define TUPLE_MALLOC_INC 100
#define MAX_CONNECTIONS            \
    128 /* conns per environment \ \
         * (arbitrary)	*/

#ifdef NAMEDATALEN
#define MAX_SCHEMA_LEN NAMEDATALEN
#define MAX_TABLE_LEN NAMEDATALEN
#define MAX_COLUMN_LEN NAMEDATALEN
#define NAME_FIELD_SIZE NAMEDATALEN /* size of name fields */
#if (NAMEDATALEN > NAMESTORAGELEN)
#undef NAMESTORAGELEN
#define NAMESTORAGELEN NAMEDATALEN
#endif
#endif /* NAMEDATALEN */
#define MAX_CURSOR_LEN 32

#define SCHEMA_NAME_STORAGE_LEN NAMESTORAGELEN
#define TABLE_NAME_STORAGE_LEN NAMESTORAGELEN
#define COLUMN_NAME_STORAGE_LEN NAMESTORAGELEN
#define INDEX_KEYS_STORAGE_COUNT 32

/*	Registry length limits */
#define LARGE_REGISTRY_LEN 4096 /* used for special cases */
#define MEDIUM_REGISTRY_LEN                        \
    256                       /* normal size for \ \
                               * user,database,etc. */
#define SMALL_REGISTRY_LEN 10 /* for 1/0 settings */

/*	These prefixes denote system tables */
#define ELASTIC_SYS_PREFIX "es_"

/*	Info limits */
#define MAX_INFO_STRING 128

/* POSIX defines a PATH_MAX.( wondows is _MAX_PATH ..) */
#ifndef PATH_MAX
#ifdef _MAX_PATH
#define PATH_MAX _MAX_PATH
#else
#define PATH_MAX 1024
#endif /* _MAX_PATH */
#endif /* PATH_MAX */

typedef struct ConnectionClass_ ConnectionClass;
typedef struct StatementClass_ StatementClass;
typedef struct QResultClass_ QResultClass;
typedef struct BindInfoClass_ BindInfoClass;
typedef struct ParameterInfoClass_ ParameterInfoClass;
typedef struct ParameterImplClass_ ParameterImplClass;
typedef struct ColumnInfoClass_ ColumnInfoClass;
typedef struct EnvironmentClass_ EnvironmentClass;
typedef struct TupleField_ TupleField;
typedef struct KeySet_ KeySet;
typedef struct Rollback_ Rollback;
typedef struct ARDFields_ ARDFields;
typedef struct APDFields_ APDFields;
typedef struct IRDFields_ IRDFields;
typedef struct IPDFields_ IPDFields;

typedef struct col_info COL_INFO;
typedef struct lo_arg LO_ARG;

/*	esNAME type define */
typedef struct {
    char *name;
} esNAME;
#define GET_NAME(the_name) ((the_name).name)
#define SAFE_NAME(the_name) ((the_name).name ? (the_name).name : NULL_STRING)
#define PRINT_NAME(the_name) ((the_name).name ? (the_name).name : PRINT_NULL)
#define NAME_IS_NULL(the_name) (NULL == (the_name).name)
#define NAME_IS_VALID(the_name) (NULL != (the_name).name)
#define INIT_NAME(the_name) ((the_name).name = NULL)
#define NULL_THE_NAME(the_name)    \
    do {                           \
        if ((the_name).name)       \
            free((the_name).name); \
        (the_name).name = NULL;    \
    } while (0)
#define STR_TO_NAME(the_name, str)                      \
    do {                                                \
        if ((the_name).name)                            \
            free((the_name).name);                      \
        (the_name).name = (str ? strdup((str)) : NULL); \
    } while (0)
#define STRX_TO_NAME(the_name, str)      \
    do {                                 \
        if ((the_name).name)             \
            free((the_name).name);       \
        (the_name).name = strdup((str)); \
    } while (0)
#define STRN_TO_NAME(the_name, str, n)             \
    do {                                           \
        if ((the_name).name)                       \
            free((the_name).name);                 \
        if (str) {                                 \
            (the_name).name = malloc((n) + 1);     \
            if ((the_name).name) {                 \
                memcpy((the_name).name, str, (n)); \
                (the_name).name[(n)] = '\0';       \
            }                                      \
        } else                                     \
            (the_name).name = NULL;                \
    } while (0)
#define NAME_TO_NAME(to, from)             \
    do {                                   \
        if ((to).name)                     \
            free((to).name);               \
        if ((from).name)                   \
            (to).name = strdup(from.name); \
        else                               \
            (to).name = NULL;              \
    } while (0)
#define MOVE_NAME(to, from)      \
    do {                         \
        if ((to).name)           \
            free((to).name);     \
        (to).name = (from).name; \
        (from).name = NULL;      \
    } while (0)
#define SET_NAME_DIRECTLY(the_name, str) ((the_name).name = (str))

#define NAMECMP(name1, name2) (strcmp(SAFE_NAME(name1), SAFE_NAME(name2)))
#define NAMEICMP(name1, name2) (stricmp(SAFE_NAME(name1), SAFE_NAME(name2)))
/*	esNAME define end */

typedef struct GlobalValues_ {
    esNAME drivername;
    char output_dir[LARGE_REGISTRY_LEN];
    int loglevel;
} GLOBAL_VALUES;

void copy_globals(GLOBAL_VALUES *to, const GLOBAL_VALUES *from);
void init_globals(GLOBAL_VALUES *glbv);
void finalize_globals(GLOBAL_VALUES *glbv);

typedef struct StatementOptions_ {
    SQLLEN maxRows;
    SQLLEN maxLength;
    SQLLEN keyset_size;
    SQLUINTEGER cursor_type;
    SQLUINTEGER scroll_concurrency;
    SQLUINTEGER retrieve_data;
    SQLUINTEGER use_bookmarks;
    void *bookmark_ptr;
    SQLUINTEGER metadata_id;
    SQLULEN stmt_timeout;
} StatementOptions;

/*	Used to pass extra query info to send_query */
typedef struct QueryInfo_ {
    SQLLEN row_size;
    SQLLEN fetch_size;
    QResultClass *result_in;
    const char *cursor;
} QueryInfo;

/*	Used to save the error information */
typedef struct {
    UInt4 status;
    Int2 errorsize;
    Int2 recsize;
    Int2 errorpos;
    char sqlstate[6];
    SQLLEN diag_row_count;
    char __error_message[40];
} ES_ErrorInfo;
ES_ErrorInfo *ER_Constructor(SDWORD errornumber, const char *errormsg);
ES_ErrorInfo *ER_Dup(const ES_ErrorInfo *from);
void ER_Destructor(ES_ErrorInfo *);
RETCODE SQL_API ER_ReturnError(ES_ErrorInfo *, SQLSMALLINT, UCHAR *,
                               SQLINTEGER *, UCHAR *, SQLSMALLINT,
                               SQLSMALLINT *, UWORD);

void logs_on_off(int cnopen, int, int);

#define ES_TYPE_LO_UNDEFINED         \
    (-999) /* hack until permanent \ \
            * type available */
#define ES_TYPE_LO_NAME "lo"
#define CTID_ATTNUM (-1) /* the attnum of ctid */
#define OID_ATTNUM (-2)  /* the attnum of oid */
#define XMIN_ATTNUM (-3) /* the attnum of xmin */

/* sizes */
#define TEXT_FIELD_SIZE                   \
    8190 /* size of default text fields \ \
          * (not including null term) */
#define MAX_VARCHAR_SIZE             \
    512 /* default maximum size of \ \
         * varchar fields (not including null term) */
#define INFO_VARCHAR_SIZE       \
    254 /* varchar field size \ \
         * used in info.c */

#define ES_NUMERIC_MAX_PRECISION 1000
#define ES_NUMERIC_MAX_SCALE 1000

/* Sufficient digits to recover original float values */
#define ES_REAL_DIGITS 9
#define ES_DOUBLE_DIGITS 17

#define INFO_INQUIRY_LEN                                                  \
    8192                             /* this seems sufficiently big for \ \
                                      * queries used in info.c inoue    \ \
                                      * 2001/05/17 */
#define LENADDR_SHIFT(x, sft) ((x) ? (SQLLEN *)((char *)(x) + (sft)) : NULL)

/*	Structure to hold all the connection attributes for a specific
    connection (used for both registry and file, DSN and DRIVER)
*/
typedef struct {
    // Connection
    char dsn[MEDIUM_REGISTRY_LEN];
    char desc[MEDIUM_REGISTRY_LEN];
    char drivername[MEDIUM_REGISTRY_LEN];
    char server[MEDIUM_REGISTRY_LEN];
    char port[SMALL_REGISTRY_LEN];
    char response_timeout[SMALL_REGISTRY_LEN];
    char fetch_size[SMALL_REGISTRY_LEN];

    // Authentication
    char authtype[MEDIUM_REGISTRY_LEN];
    char username[MEDIUM_REGISTRY_LEN];
    esNAME password;
    char region[MEDIUM_REGISTRY_LEN];

    // Encryption
    char use_ssl;
    char verify_server;

    GLOBAL_VALUES drivers; /* moved from driver's option */
} ConnInfo;

#define SUPPORT_DESCRIBE_PARAM(conninfo_) (1)

int initialize_global_cs(void);
enum {                        /* CC_conninfo_init option */
       CLEANUP_FOR_REUSE = 1L /* reuse the info */
       ,
       INIT_GLOBALS = (1L << 1) /* init globals memebers */
};
void CC_conninfo_init(ConnInfo *conninfo, UInt4 option);
void CC_conninfo_release(ConnInfo *conninfo);
void CC_copy_conninfo(ConnInfo *ci, const ConnInfo *sci);
const char *GetExeProgramName();

/* Define a type for defining a constant string expression */
#ifndef CSTR
#define CSTR static const char *const
#endif /* CSTR */

CSTR NULL_STRING = "";
CSTR PRINT_NULL = "(null)";
#define OID_NAME "oid"
#define XMIN_NAME "xmin"
#define TABLEOID_NAME "tableoid"

enum {
    DISALLOW_UPDATABLE_CURSORS = 0, /* No cursors are updatable */
    ALLOW_STATIC_CURSORS = 1L,      /* Static cursors are updatable */
    ALLOW_KEYSET_DRIVEN_CURSORS =
        (1L << 1),                     /* Keyset-driven cursors are updatable */
    ALLOW_DYNAMIC_CURSORS = (1L << 2), /* Dynamic cursors are updatable */
    ALLOW_BULK_OPERATIONS = (1L << 3), /* Bulk operations available */
    SENSE_SELF_OPERATIONS = (1L << 4), /* Sense self update/delete/add */
};

#ifdef __cplusplus
}
#endif

#include "mylog.h"

#endif /* __ESODBC_H__ */
