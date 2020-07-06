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

#ifndef ES_TYPES
#define ES_TYPES

#include "dlg_specific.h"
#include "es_odbc.h"
#ifdef __cplusplus
extern "C" {
#endif

/* the type numbers are defined by the OID's of the types' rows */
/* in table es_type */

#ifdef NOT_USED
#define ES_TYPE_LO ? ? ? ? /* waiting for permanent type */
#endif

#define MS_ACCESS_SERIAL "int identity"
#define ES_TYPE_BOOL 16
#define ES_TYPE_BYTEA 17
#define ES_TYPE_CHAR 18
#define ES_TYPE_NAME 19
#define ES_TYPE_INT8 20
#define ES_TYPE_INT2 21
#define ES_TYPE_INT2VECTOR 22
#define ES_TYPE_INT4 23
#define ES_TYPE_REGPROC 24
#define ES_TYPE_TEXT 25
#define ES_TYPE_OID 26
#define ES_TYPE_TID 27
#define ES_TYPE_XID 28
#define ES_TYPE_CID 29
#define ES_TYPE_OIDVECTOR 30
#define ES_TYPE_INT1 31
#define ES_TYPE_HALF_FLOAT 32
#define ES_TYPE_SCALED_FLOAT 33
#define ES_TYPE_KEYWORD 34
#define ES_TYPE_NESTED 35
#define ES_TYPE_OBJECT 36
#define ES_TYPE_UNSUPPORTED 37
#define ES_TYPE_XML 142
#define ES_TYPE_XMLARRAY 143
#define ES_TYPE_CIDR 650
#define ES_TYPE_FLOAT4 700
#define ES_TYPE_FLOAT8 701
#define ES_TYPE_ABSTIME 702
#define ES_TYPE_UNKNOWN 705
#define ES_TYPE_MONEY 790
#define ES_TYPE_MACADDR 829
#define ES_TYPE_INET 869
#define ES_TYPE_TEXTARRAY 1009
#define ES_TYPE_BPCHARARRAY 1014
#define ES_TYPE_VARCHARARRAY 1015
#define ES_TYPE_BPCHAR 1042
#define ES_TYPE_VARCHAR 1043
#define ES_TYPE_DATE 1082
#define ES_TYPE_TIME 1083
#define ES_TYPE_TIMESTAMP_NO_TMZONE 1114 /* since 7.2 */
#define ES_TYPE_DATETIME 1184            /* timestamptz */
#define ES_TYPE_INTERVAL 1186
#define ES_TYPE_TIME_WITH_TMZONE 1266 /* since 7.1 */
#define ES_TYPE_TIMESTAMP 1296        /* deprecated since 7.0 */
#define ES_TYPE_BIT 1560
#define ES_TYPE_NUMERIC 1700
#define ES_TYPE_REFCURSOR 1790
#define ES_TYPE_RECORD 2249
#define ES_TYPE_ANY 2276
#define ES_TYPE_VOID 2278
#define ES_TYPE_UUID 2950
#define INTERNAL_ASIS_TYPE (-9999)

#define TYPE_MAY_BE_ARRAY(type) \
    ((type) == ES_TYPE_XMLARRAY || ((type) >= 1000 && (type) <= 1041))
/* extern Int4 es_types_defined[]; */
extern SQLSMALLINT sqlTypes[];

/*	Defines for estype_precision */
#define ES_ATP_UNSET (-3)   /* atttypmod */
#define ES_ADT_UNSET (-3)   /* adtsize_or_longestlen */
#define ES_UNKNOWNS_UNSET 0 /* UNKNOWNS_AS_MAX */
#define ES_WIDTH_OF_BOOLS_AS_CHAR 5

/*
 *	SQL_INTERVAL support is disabled because I found
 *	some applications which are unhappy with it.
 *
#define	ES_INTERVAL_AS_SQL_INTERVAL
 */

OID es_true_type(const ConnectionClass *, OID, OID);
OID sqltype_to_estype(const ConnectionClass *conn, SQLSMALLINT fSqlType);
const char *sqltype_to_escast(const ConnectionClass *conn,
                              SQLSMALLINT fSqlType);

SQLSMALLINT estype_to_concise_type(const StatementClass *stmt, OID type,
                                   int col, int handle_unknown_size_as);
SQLSMALLINT estype_to_sqldesctype(const StatementClass *stmt, OID type, int col,
                                  int handle_unknown_size_as);
const char *estype_to_name(const StatementClass *stmt, OID type, int col,
                           BOOL auto_increment);

SQLSMALLINT estype_attr_to_concise_type(const ConnectionClass *conn, OID type,
                                        int typmod, int adtsize_or_longestlen,
                                        int handle_unknown_size_as);
SQLSMALLINT estype_attr_to_sqldesctype(const ConnectionClass *conn, OID type,
                                       int typmod, int adtsize_or_longestlen,
                                       int handle_unknown_size_as);
SQLSMALLINT estype_attr_to_datetime_sub(const ConnectionClass *conn, OID type,
                                        int typmod);
SQLSMALLINT estype_attr_to_ctype(const ConnectionClass *conn, OID type,
                                 int typmod);
const char *estype_attr_to_name(const ConnectionClass *conn, OID type,
                                int typmod, BOOL auto_increment);
Int4 estype_attr_column_size(const ConnectionClass *conn, OID type,
                             int atttypmod, int adtsize_or_longest,
                             int handle_unknown_size_as);
Int4 estype_attr_buffer_length(const ConnectionClass *conn, OID type,
                               int atttypmod, int adtsize_or_longestlen,
                               int handle_unknown_size_as);
Int4 estype_attr_display_size(const ConnectionClass *conn, OID type,
                              int atttypmod, int adtsize_or_longestlen,
                              int handle_unknown_size_as);
Int2 estype_attr_decimal_digits(const ConnectionClass *conn, OID type,
                                int atttypmod, int adtsize_or_longestlen,
                                int handle_unknown_size_as);
Int4 estype_attr_transfer_octet_length(const ConnectionClass *conn, OID type,
                                       int atttypmod,
                                       int handle_unknown_size_as);
SQLSMALLINT estype_attr_precision(const ConnectionClass *conn, OID type,
                                  int atttypmod, int adtsize_or_longest,
                                  int handle_unknown_size_as);
Int4 estype_attr_desclength(const ConnectionClass *conn, OID type,
                            int atttypmod, int adtsize_or_longestlen,
                            int handle_unknown_size_as);
Int2 estype_attr_scale(const ConnectionClass *conn, OID type, int atttypmod,
                       int adtsize_or_longestlen, int handle_unknown_size_as);

/*	These functions can use static numbers or result sets(col parameter) */
Int4 estype_column_size(
    const StatementClass *stmt, OID type, int col,
    int handle_unknown_size_as); /* corresponds to "precision" in ODBC 2.x */
SQLSMALLINT estype_precision(
    const StatementClass *stmt, OID type, int col,
    int handle_unknown_size_as); /* "precsion in ODBC 3.x */
/* the following size/length are of Int4 due to ES restriction */
Int4 estype_display_size(const StatementClass *stmt, OID type, int col,
                         int handle_unknown_size_as);
Int4 estype_buffer_length(const StatementClass *stmt, OID type, int col,
                          int handle_unknown_size_as);
Int4 estype_desclength(const StatementClass *stmt, OID type, int col,
                       int handle_unknown_size_as);
// Int4		estype_transfer_octet_length(const ConnectionClass *conn, OID type,
// int column_size);

SQLSMALLINT estype_decimal_digits(
    const StatementClass *stmt, OID type,
    int col); /* corresponds to "scale" in ODBC 2.x */
SQLSMALLINT estype_min_decimal_digits(
    const ConnectionClass *conn,
    OID type); /* corresponds to "min_scale" in ODBC 2.x */
SQLSMALLINT estype_max_decimal_digits(
    const ConnectionClass *conn,
    OID type); /* corresponds to "max_scale" in ODBC 2.x */
SQLSMALLINT estype_scale(const StatementClass *stmt, OID type,
                         int col); /* ODBC 3.x " */
Int2 estype_radix(const ConnectionClass *conn, OID type);
Int2 estype_nullable(const ConnectionClass *conn, OID type);
Int2 estype_auto_increment(const ConnectionClass *conn, OID type);
Int2 estype_case_sensitive(const ConnectionClass *conn, OID type);
Int2 estype_money(const ConnectionClass *conn, OID type);
Int2 estype_searchable(const ConnectionClass *conn, OID type);
Int2 estype_unsigned(const ConnectionClass *conn, OID type);
const char *estype_literal_prefix(const ConnectionClass *conn, OID type);
const char *estype_literal_suffix(const ConnectionClass *conn, OID type);
const char *estype_create_params(const ConnectionClass *conn, OID type);

SQLSMALLINT sqltype_to_default_ctype(const ConnectionClass *stmt,
                                     SQLSMALLINT sqltype);
Int4 ctype_length(SQLSMALLINT ctype);

SQLSMALLINT ansi_to_wtype(const ConnectionClass *self, SQLSMALLINT ansitype);

#ifdef __cplusplus
}
#endif

typedef enum {
    CONNECTION_OK,
    CONNECTION_BAD,
    /* Non-blocking mode only below here */

    /*
     * The existence of these should never be relied upon - they should only
     * be used for user feedback or similar purposes.
     */
    CONNECTION_STARTED,           /* Waiting for connection to be made.  */
    CONNECTION_MADE,              /* Connection OK; waiting to send.     */
    CONNECTION_AWAITING_RESPONSE, /* Waiting for a response from the postmaster.
                                   */
    CONNECTION_AUTH_OK, /* Received authentication; waiting for backend startup.
                         */
    CONNECTION_SETENV,  /* Negotiating environment. */
    CONNECTION_SSL_STARTUP,    /* Negotiating SSL. */
    CONNECTION_NEEDED,         /* Internal state: connect() needed */
    CONNECTION_CHECK_WRITABLE, /* Check if we could make a writable connection.
                                */
    CONNECTION_CONSUME,    /* Wait for any pending message and consume them. */
    CONNECTION_GSS_STARTUP /* Negotiating GSSAPI. */
} ConnStatusType;

// Only expose this to C++ code, this will be passed through the C interface as
// a void*
#ifdef __cplusplus
#include <stdint.h>

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif // __APPLE__
#include "rabbit.hpp"
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif // __APPLE__

#include <string>
#include <vector>

typedef struct authentication_options {
    std::string auth_type;
    std::string username;
    std::string password;
    std::string region;
} authentication_options;

typedef struct encryption_options {
    bool use_ssl;
    bool verify_server;
    std::string certificate_type;
    std::string certificate;
    std::string key;
    std::string key_pw;
} encryption_options;

typedef struct connection_options {
    std::string server;
    std::string port;
    std::string timeout;
    std::string fetch_size;
} connection_options;

typedef struct runtime_options {
    connection_options conn;
    authentication_options auth;
    encryption_options crypt;
} runtime_options;

#define INVALID_OID 0
#define KEYWORD_TYPE_OID 1043
#define KEYWORD_TYPE_SIZE 255
#define KEYWORD_DISPLAY_SIZE 255
#define KEYWORD_LENGTH_OF_STR 255

// Copied from ColumnInfoClass's 'srvr_info' struct. Comments are the relevant
// name in 'srvr_info'
typedef struct ColumnInfo {
    std::string field_name;    // name
    uint32_t type_oid;         // adtid
    int16_t type_size;         // adtsize
    int32_t display_size;      // longest row
    int32_t length_of_str;     // the length of bpchar/varchar
    uint32_t relation_id;      // relid
    int16_t attribute_number;  // attid
    ColumnInfo() {
        field_name = "";
        type_oid = INVALID_OID;
        type_size = 0;      // ?
        display_size = 0;   // ?
        length_of_str = 0;  // ?
        relation_id = INVALID_OID;
        attribute_number = INVALID_OID;
    }
} ColumnInfo;

typedef struct ESResult {
    uint32_t ref_count;  // reference count. A ColumnInfo can be shared by
                         // several qresults.
    uint16_t num_fields;
    std::vector< ColumnInfo > column_info;
    std::string cursor;
    std::string result_json;
    std::string command_type;  // SELECT / FETCH / etc
    rabbit::document es_result_doc;
    ESResult() {
        ref_count = 0;
        num_fields = 0;
        result_json = "";
        command_type = "";
    }
} ESResult;

#endif
#endif
