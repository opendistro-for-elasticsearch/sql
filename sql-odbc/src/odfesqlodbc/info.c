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

#include "es_odbc.h"
#include "unicode_support.h"

#include <stdio.h>
#include <string.h>

#ifndef WIN32
#include <ctype.h>
#endif

#include "dlg_specific.h"
#include "es_types.h"
#include "tuple.h"

#include "bind.h"
#include "catfunc.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_info.h"
#include "es_types.h"
#include "misc.h"
#include "multibyte.h"
#include "qresult.h"
#include "statement.h"
#include "tuple.h"

/*	Trigger related stuff for SQLForeign Keys */
#define TRIGGER_SHIFT 3
#define TRIGGER_MASK 0x03
#define TRIGGER_DELETE 0x01
#define TRIGGER_UPDATE 0x02

RETCODE SQL_API ESAPI_GetInfo(HDBC hdbc, SQLUSMALLINT fInfoType,
                              PTR rgbInfoValue, SQLSMALLINT cbInfoValueMax,
                              SQLSMALLINT *pcbInfoValue) {
    CSTR func = "ESAPI_GetInfo";
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    ConnInfo *ci;
    const char *p = NULL;
    char tmp[MAX_INFO_STRING];
    SQLULEN len = 0, value = 0;
    RETCODE ret = SQL_ERROR;
    char odbcver[16];

    MYLOG(ES_TRACE, "entering...fInfoType=%d\n", fInfoType);

    if (!conn) {
        CC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    ci = &(conn->connInfo);

    switch (fInfoType) {
        case SQL_ACCESSIBLE_PROCEDURES: /* ODBC 1.0 */
            p = "N";
            break;

        case SQL_ACCESSIBLE_TABLES: /* ODBC 1.0 */
            p = "N";
            break;

        case SQL_ACTIVE_CONNECTIONS: /* ODBC 1.0 */
            len = 2;
            value = 0;
            break;

        case SQL_ACTIVE_STATEMENTS: /* ODBC 1.0 */
            len = 2;
            value = 0;
            break;

        case SQL_ALTER_TABLE: /* ODBC 2.0 */
            len = 4;
            value = SQL_AT_ADD_COLUMN | SQL_AT_DROP_COLUMN
                    | SQL_AT_ADD_COLUMN_SINGLE | SQL_AT_ADD_CONSTRAINT
                    | SQL_AT_ADD_TABLE_CONSTRAINT
                    | SQL_AT_CONSTRAINT_INITIALLY_DEFERRED
                    | SQL_AT_CONSTRAINT_INITIALLY_IMMEDIATE
                    | SQL_AT_CONSTRAINT_DEFERRABLE
                    | SQL_AT_DROP_TABLE_CONSTRAINT_RESTRICT
                    | SQL_AT_DROP_TABLE_CONSTRAINT_CASCADE
                    | SQL_AT_DROP_COLUMN_RESTRICT | SQL_AT_DROP_COLUMN_CASCADE;
            break;

        case SQL_BOOKMARK_PERSISTENCE: /* ODBC 2.0 */
            /* very simple bookmark support */
            len = 4;
            value = SQL_BP_SCROLL | SQL_BP_DELETE | SQL_BP_UPDATE
                    | SQL_BP_TRANSACTION;
            break;

        case SQL_COLUMN_ALIAS: /* ODBC 2.0 */
            p = "Y";
            break;

        case SQL_CONCAT_NULL_BEHAVIOR: /* ODBC 1.0 */
            len = 2;
            value = SQL_CB_NULL;
            break;

        case SQL_CONVERT_GUID:
        case SQL_CONVERT_INTEGER:
        case SQL_CONVERT_SMALLINT:
        case SQL_CONVERT_TINYINT:
        case SQL_CONVERT_BIT:
        case SQL_CONVERT_VARCHAR:
        case SQL_CONVERT_BIGINT:
        case SQL_CONVERT_DECIMAL:
        case SQL_CONVERT_DOUBLE:
        case SQL_CONVERT_FLOAT:
        case SQL_CONVERT_NUMERIC:
        case SQL_CONVERT_REAL:
        case SQL_CONVERT_DATE:
        case SQL_CONVERT_TIME:
        case SQL_CONVERT_TIMESTAMP:
        case SQL_CONVERT_BINARY:
        case SQL_CONVERT_LONGVARBINARY:
        case SQL_CONVERT_VARBINARY: /* ODBC 1.0 */
        case SQL_CONVERT_CHAR:
        case SQL_CONVERT_LONGVARCHAR:
#ifdef UNICODE_SUPPORT
        case SQL_CONVERT_WCHAR:
        case SQL_CONVERT_WLONGVARCHAR:
        case SQL_CONVERT_WVARCHAR:
#endif /* UNICODE_SUPPORT */
            len = sizeof(SQLUINTEGER);
            value = 0; /* CONVERT is unavailable */
            break;

        case SQL_CONVERT_FUNCTIONS: /* ODBC 1.0 */
            len = sizeof(SQLUINTEGER);
            value = SQL_FN_CVT_CAST;
            MYLOG(ES_DEBUG, "CONVERT_FUNCTIONS=" FORMAT_ULEN "\n", value);
            break;

        case SQL_CORRELATION_NAME: /* ODBC 1.0 */

            /*
             * Saying no correlation name makes Query not work right.
             * value = SQL_CN_NONE;
             */
            len = 2;
            value = SQL_CN_ANY;
            break;

        case SQL_CURSOR_COMMIT_BEHAVIOR: /* ODBC 1.0 */
            len = 2;
            value = SQL_CB_CLOSE;
            break;

        case SQL_CURSOR_ROLLBACK_BEHAVIOR: /* ODBC 1.0 */
            len = 2;
            value = SQL_CB_PRESERVE;
            break;

        case SQL_DATA_SOURCE_NAME: /* ODBC 1.0 */
            p = CC_get_DSN(conn);
            break;

        case SQL_DATA_SOURCE_READ_ONLY: /* ODBC 1.0 */
            p = "Y";
            break;

        case SQL_DATABASE_NAME: /* Support for old ODBC 1.0 Apps */

            /*
             * Returning the database name causes problems in MS Query. It
             * generates query like: "SELECT DISTINCT a FROM byronnbad3
             * bad3"
             *
             * p = CC_get_database(conn);
             */
            p = CurrCatString(conn);
            break;

        case SQL_DBMS_NAME: /* ODBC 1.0 */
            p = "Elasticsearch";
            break;

        case SQL_DBMS_VER: /* ODBC 1.0 */
            STRCPY_FIXED(tmp, conn->es_version);
            p = tmp;
            break;

        case SQL_DEFAULT_TXN_ISOLATION: /* ODBC 1.0 */
            len = 4;
            if (0 == conn->default_isolation)
                conn->isolation = CC_get_isolation(conn);
            value = conn->default_isolation;
            break;

        case SQL_DRIVER_NAME: /* ODBC 1.0 */
            p = DRIVER_FILE_NAME;
            break;

        case SQL_DRIVER_ODBC_VER:
            SPRINTF_FIXED(odbcver, "%02x.%02x", ODBCVER / 256, ODBCVER % 256);
            /* p = DRIVER_ODBC_VER; */
            p = odbcver;
            break;

        case SQL_DRIVER_VER: /* ODBC 1.0 */
            p = ELASTICSEARCHDRIVERVERSION;
            break;

        case SQL_EXPRESSIONS_IN_ORDERBY: /* ODBC 1.0 */
            p = "Y";
            break;

        case SQL_FETCH_DIRECTION: /* ODBC 1.0 */
            len = 4;
            value = (SQL_FD_FETCH_NEXT | SQL_FD_FETCH_FIRST | SQL_FD_FETCH_LAST
                     | SQL_FD_FETCH_PRIOR | SQL_FD_FETCH_ABSOLUTE
                     | SQL_FD_FETCH_RELATIVE | SQL_FD_FETCH_BOOKMARK);
            break;

        case SQL_FILE_USAGE: /* ODBC 2.0 */
            len = 2;
            value = SQL_FILE_NOT_SUPPORTED;
            break;

        case SQL_GETDATA_EXTENSIONS: /* ODBC 2.0 */
            len = 4;
            value = (SQL_GD_ANY_COLUMN | SQL_GD_ANY_ORDER | SQL_GD_BOUND
                     | SQL_GD_BLOCK);
            break;

        case SQL_GROUP_BY: /* ODBC 2.0 */
            len = 2;
            value = SQL_GB_GROUP_BY_EQUALS_SELECT;
            break;

        case SQL_IDENTIFIER_CASE: /* ODBC 1.0 */

            /*
             * are identifiers case-sensitive (yes, but only when quoted.
             * If not quoted, they default to lowercase)
             */
            len = 2;
            value = SQL_IC_LOWER;
            break;

        case SQL_IDENTIFIER_QUOTE_CHAR: /* ODBC 1.0 */
            /* the character used to quote "identifiers" */
            p = "`";
            break;

        case SQL_KEYWORDS: /* ODBC 2.0 */
            p = NULL_STRING;
            break;

        case SQL_LIKE_ESCAPE_CLAUSE: /* ODBC 2.0 */
            p = "Y";
            break;

        case SQL_LOCK_TYPES: /* ODBC 2.0 */
            len = 4;
            value = SQL_LCK_NO_CHANGE;
            break;

        case SQL_MAX_BINARY_LITERAL_LEN: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_MAX_CHAR_LITERAL_LEN: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_MAX_COLUMN_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            value = CC_get_max_idlen(conn);
            if (0 == value)
                value = NAMEDATALEN_V73 - 1;
            break;

        case SQL_MAX_COLUMNS_IN_GROUP_BY: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_COLUMNS_IN_INDEX: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_COLUMNS_IN_ORDER_BY: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_COLUMNS_IN_SELECT: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_COLUMNS_IN_TABLE: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_CURSOR_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            value = MAX_CURSOR_LEN;
            break;

        case SQL_MAX_INDEX_SIZE: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_MAX_OWNER_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_PROCEDURE_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_QUALIFIER_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_ROW_SIZE: /* ODBC 2.0 */
            len = 4;
            /* No limit with tuptoaster in 7.1+ */
            value = 0;
            break;

        case SQL_MAX_STATEMENT_LEN: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_MAX_TABLE_NAME_LEN: /* ODBC 1.0 */
            len = 2;
            if (ES_VERSION_GT(conn, 7.4))
                value = CC_get_max_idlen(conn);
#ifdef MAX_TABLE_LEN
            else
                value = MAX_TABLE_LEN;
#endif /* MAX_TABLE_LEN */
            if (0 == value)
                value = NAMEDATALEN_V73 - 1;
            break;

        case SQL_MAX_TABLES_IN_SELECT: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_MAX_USER_NAME_LEN:
            len = 2;
            value = 0;
            break;

        case SQL_MULT_RESULT_SETS: /* ODBC 1.0 */
            /* Don't support multiple result sets but say yes anyway? */
            p = "Y";
            break;

        case SQL_MULTIPLE_ACTIVE_TXN: /* ODBC 1.0 */
            p = "Y";
            break;

        case SQL_NEED_LONG_DATA_LEN: /* ODBC 2.0 */

            /*
             * Don't need the length, SQLPutData can handle any size and
             * multiple calls
             */
            p = "N";
            break;

        case SQL_NON_NULLABLE_COLUMNS: /* ODBC 1.0 */
            len = 2;
            value = SQL_NNC_NON_NULL;
            break;

        case SQL_NULL_COLLATION: /* ODBC 2.0 */
            /* where are nulls sorted? */
            len = 2;
            value = SQL_NC_HIGH;
            break;

        case SQL_NUMERIC_FUNCTIONS: /* ODBC 1.0 */
            len = 4;
            value = SQL_FN_NUM_ABS | SQL_FN_NUM_ATAN | SQL_FN_NUM_ATAN2
                    | SQL_FN_NUM_COS | SQL_FN_NUM_COT | SQL_FN_NUM_DEGREES
                    | SQL_FN_NUM_FLOOR | SQL_FN_NUM_LOG | SQL_FN_NUM_LOG10
                    | SQL_FN_NUM_PI | SQL_FN_NUM_POWER | SQL_FN_NUM_RADIANS
                    | SQL_FN_NUM_ROUND | SQL_FN_NUM_SIGN | SQL_FN_NUM_SIN
                    | SQL_FN_NUM_SQRT | SQL_FN_NUM_TAN;
            break;

        case SQL_ODBC_API_CONFORMANCE: /* ODBC 1.0 */
            len = 2;
            value = SQL_OAC_LEVEL1;
            break;

        case SQL_ODBC_SAG_CLI_CONFORMANCE: /* ODBC 1.0 */
            len = 2;
            value = SQL_OSCC_NOT_COMPLIANT;
            break;

        case SQL_ODBC_SQL_CONFORMANCE: /* ODBC 1.0 */
            len = 2;
            value = SQL_OSC_CORE;
            break;

        case SQL_ODBC_SQL_OPT_IEF: /* ODBC 1.0 */
            p = "N";
            break;

        case SQL_OJ_CAPABILITIES: /* ODBC 2.01 */
            len = 4;
            value = SQL_OJ_LEFT | SQL_OJ_RIGHT | SQL_OJ_NOT_ORDERED
                    | SQL_OJ_ALL_COMPARISON_OPS;
            break;

        case SQL_ORDER_BY_COLUMNS_IN_SELECT: /* ODBC 2.0 */
            p = "Y";
            break;

        case SQL_OUTER_JOINS: /* ODBC 1.0 */
            p = "Y";
            break;

        case SQL_OWNER_TERM: /* ODBC 1.0 */
            p = "";
            break;

        case SQL_OWNER_USAGE: /* ODBC 2.0 */
            // Elasticsearch does not support schemas.
            // This will disable showing an empty schema box in Tableau.
            len = 4;
            value = 0;
            break;

        case SQL_POS_OPERATIONS: /* ODBC 2.0 */
            len = 4;
            value = (SQL_POS_POSITION | SQL_POS_REFRESH);
            break;

        case SQL_POSITIONED_STATEMENTS: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_PROCEDURE_TERM: /* ODBC 1.0 */
            p = "procedure";
            break;

        case SQL_PROCEDURES: /* ODBC 1.0 */
            p = "Y";
            break;

        case SQL_QUALIFIER_LOCATION: /* ODBC 2.0 */
            len = 2;
            value = 0;
            break;

        case SQL_QUALIFIER_NAME_SEPARATOR: /* ODBC 1.0 */
            p = "";
            break;

        case SQL_QUALIFIER_TERM: /* ODBC 1.0 */
            p = "";
            break;

        case SQL_QUALIFIER_USAGE: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_QUOTED_IDENTIFIER_CASE: /* ODBC 2.0 */
            /* are "quoted" identifiers case-sensitive?  YES! */
            len = 2;
            value = SQL_IC_SENSITIVE;
            break;

        case SQL_ROW_UPDATES: /* ODBC 1.0 */

            /*
             * Driver doesn't support keyset-driven or mixed cursors, so
             * not much point in saying row updates are supported
             */
            p = "N";
            break;

        case SQL_SCROLL_CONCURRENCY: /* ODBC 1.0 */
            len = 4;
            value = SQL_SCCO_READ_ONLY;
            break;

        case SQL_SCROLL_OPTIONS: /* ODBC 1.0 */
            len = 4;
            value = SQL_SO_FORWARD_ONLY | SQL_SO_STATIC;
            break;

        case SQL_SEARCH_PATTERN_ESCAPE: /* ODBC 1.0 */
            p = "";
            break;

        case SQL_SERVER_NAME: /* ODBC 1.0 */
            p = CC_get_server(conn);
            break;

        case SQL_SPECIAL_CHARACTERS: /* ODBC 2.0 */
            p = "_";
            break;

        case SQL_STATIC_SENSITIVITY: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_STRING_FUNCTIONS: /* ODBC 1.0 */
            len = 4;
            value = SQL_FN_STR_ASCII | SQL_FN_STR_LENGTH | SQL_FN_STR_LTRIM
                    | SQL_FN_STR_REPLACE | SQL_FN_STR_RTRIM
                    | SQL_FN_STR_SUBSTRING;
            break;

        case SQL_SUBQUERIES: /* ODBC 2.0 */
            len = 4;
            value = (SQL_SQ_QUANTIFIED | SQL_SQ_IN | SQL_SQ_EXISTS
                     | SQL_SQ_COMPARISON);
            break;

        case SQL_SYSTEM_FUNCTIONS: /* ODBC 1.0 */
            len = 4;
            value = SQL_FN_SYS_IFNULL;
            break;

        case SQL_TABLE_TERM: /* ODBC 1.0 */
            p = "table";
            break;

        case SQL_TIMEDATE_ADD_INTERVALS: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_TIMEDATE_DIFF_INTERVALS: /* ODBC 2.0 */
            len = 4;
            value = 0;
            break;

        case SQL_TIMEDATE_FUNCTIONS: /* ODBC 1.0 */
            len = 4;
            value = SQL_FN_TD_CURDATE | SQL_FN_TD_DAYOFMONTH | SQL_FN_TD_MONTH
                    | SQL_FN_TD_MONTHNAME | SQL_FN_TD_NOW | SQL_FN_TD_YEAR;
            break;

        case SQL_TXN_CAPABLE: /* ODBC 1.0 */
            /*
             * Elasticsearch does not support transactions.
             */
            len = 2;
            value = SQL_TC_NONE;
            break;

        case SQL_TXN_ISOLATION_OPTION: /* ODBC 1.0 */
            len = 4;
            value = SQL_TXN_READ_UNCOMMITTED | SQL_TXN_READ_COMMITTED
                    | SQL_TXN_REPEATABLE_READ | SQL_TXN_SERIALIZABLE;
            break;

        case SQL_UNION: /* ODBC 2.0 */
            len = 4;
            value = (SQL_U_UNION | SQL_U_UNION_ALL);
            break;

        case SQL_USER_NAME: /* ODBC 1.0 */
            p = CC_get_username(conn);
            break;

        /* Keys for ODBC 3.0 */
        case SQL_DYNAMIC_CURSOR_ATTRIBUTES1:
            len = 4;
            value = 0;
            break;
        case SQL_DYNAMIC_CURSOR_ATTRIBUTES2:
            len = 4;
            value = 0;
            break;
        case SQL_FORWARD_ONLY_CURSOR_ATTRIBUTES1:
            len = 4;
            value = SQL_CA1_NEXT; /* others aren't allowed in ODBC spec */
            break;
        case SQL_FORWARD_ONLY_CURSOR_ATTRIBUTES2:
            len = 4;
            value = SQL_CA2_READ_ONLY_CONCURRENCY | SQL_CA2_CRC_EXACT;
            break;
        case SQL_KEYSET_CURSOR_ATTRIBUTES1:
            len = 4;
            value = 0;
            break;
        case SQL_KEYSET_CURSOR_ATTRIBUTES2:
            len = 4;
            value = 0;
            break;

        case SQL_STATIC_CURSOR_ATTRIBUTES1:
            len = 4;
            value = SQL_CA1_NEXT | SQL_CA1_ABSOLUTE | SQL_CA1_RELATIVE
                    | SQL_CA1_BOOKMARK | SQL_CA1_LOCK_NO_CHANGE
                    | SQL_CA1_POS_POSITION | SQL_CA1_POS_REFRESH;
            break;
        case SQL_STATIC_CURSOR_ATTRIBUTES2:
            len = 4;
            value = SQL_CA2_READ_ONLY_CONCURRENCY | SQL_CA2_CRC_EXACT;
            break;

        case SQL_ODBC_INTERFACE_CONFORMANCE:
            len = 4;
            value = SQL_OIC_CORE;
            break;
        case SQL_ACTIVE_ENVIRONMENTS:
            len = 2;
            value = 0;
            break;
        case SQL_AGGREGATE_FUNCTIONS:
            len = 4;
            value = SQL_AF_ALL;
            break;
        case SQL_ALTER_DOMAIN:
            len = 4;
            value = 0;
            break;
        case SQL_ASYNC_MODE:
            len = 4;
            value = SQL_AM_NONE;
            break;
        case SQL_BATCH_ROW_COUNT:
            len = 4;
            value = SQL_BRC_EXPLICIT;
            break;
        case SQL_BATCH_SUPPORT:
            len = 4;
            value = SQL_BS_SELECT_EXPLICIT | SQL_BS_ROW_COUNT_EXPLICIT;
            break;
        case SQL_CATALOG_NAME:
            p = "N";
            break;
        case SQL_COLLATION_SEQ:
            p = "";
            break;
        case SQL_CREATE_ASSERTION:
            len = 4;
            value = 0;
            break;
        case SQL_CREATE_CHARACTER_SET:
            len = 4;
            value = 0;
            break;
        case SQL_CREATE_COLLATION:
            len = 4;
            value = 0;
            break;
        case SQL_CREATE_DOMAIN:
            len = 4;
            value = 0;
            break;
        case SQL_CREATE_SCHEMA:
            len = 4;
            value = SQL_CS_CREATE_SCHEMA | SQL_CS_AUTHORIZATION;
            break;
        case SQL_CREATE_TABLE:
            len = 4;
            value = SQL_CT_CREATE_TABLE | SQL_CT_COLUMN_CONSTRAINT
                    | SQL_CT_COLUMN_DEFAULT | SQL_CT_GLOBAL_TEMPORARY
                    | SQL_CT_TABLE_CONSTRAINT
                    | SQL_CT_CONSTRAINT_NAME_DEFINITION
                    | SQL_CT_CONSTRAINT_INITIALLY_DEFERRED
                    | SQL_CT_CONSTRAINT_INITIALLY_IMMEDIATE
                    | SQL_CT_CONSTRAINT_DEFERRABLE;
            break;
        case SQL_CREATE_TRANSLATION:
            len = 4;
            value = 0;
            break;
        case SQL_CREATE_VIEW:
            len = 4;
            value = SQL_CV_CREATE_VIEW;
            break;
        case SQL_DDL_INDEX:
            len = 4;
            value = SQL_DI_CREATE_INDEX | SQL_DI_DROP_INDEX;
            break;
        case SQL_DESCRIBE_PARAMETER:
            p = "N";
            break;
        case SQL_DROP_ASSERTION:
            len = 4;
            value = 0;
            break;
        case SQL_DROP_CHARACTER_SET:
            len = 4;
            value = 0;
            break;
        case SQL_DROP_COLLATION:
            len = 4;
            value = 0;
            break;
        case SQL_DROP_DOMAIN:
            len = 4;
            value = 0;
            break;
        case SQL_DROP_SCHEMA:
            len = 4;
            value = SQL_DS_DROP_SCHEMA | SQL_DS_RESTRICT | SQL_DS_CASCADE;
            break;
        case SQL_DROP_TABLE:
            len = 4;
            value = SQL_DT_DROP_TABLE;
            value |= (SQL_DT_RESTRICT | SQL_DT_CASCADE);
            break;
        case SQL_DROP_TRANSLATION:
            len = 4;
            value = 0;
            break;
        case SQL_DROP_VIEW:
            len = 4;
            value = SQL_DV_DROP_VIEW;
            value |= (SQL_DV_RESTRICT | SQL_DV_CASCADE);
            break;
        case SQL_INDEX_KEYWORDS:
            len = 4;
            value = SQL_IK_NONE;
            break;
        case SQL_INFO_SCHEMA_VIEWS:
            len = 4;
            value = 0;
            break;
        case SQL_INSERT_STATEMENT:
            len = 4;
            value = SQL_IS_INSERT_LITERALS | SQL_IS_INSERT_SEARCHED
                    | SQL_IS_SELECT_INTO;
            break;
        case SQL_MAX_IDENTIFIER_LEN:
            len = 2;
            value = CC_get_max_idlen(conn);
            if (0 == value)
                value = NAMEDATALEN_V73 - 1;
            break;
        case SQL_MAX_ROW_SIZE_INCLUDES_LONG:
            p = "Y";
            break;
        case SQL_PARAM_ARRAY_ROW_COUNTS:
            len = 4;
            value = SQL_PARC_BATCH;
            break;
        case SQL_PARAM_ARRAY_SELECTS:
            len = 4;
            value = SQL_PAS_BATCH;
            break;
        case SQL_SQL_CONFORMANCE:
            // SQL plugin currently does not support this level,
            // but Tableau requires at least Entry level reported for retrieving
            // row data
            len = 4;
            value = SQL_SC_SQL92_ENTRY;
            break;
        case SQL_SQL92_DATETIME_FUNCTIONS:
            len = 4;
            value = 0;
            break;
        case SQL_SQL92_FOREIGN_KEY_DELETE_RULE:
            len = 4;
            value = SQL_SFKD_CASCADE | SQL_SFKD_NO_ACTION | SQL_SFKD_SET_DEFAULT
                    | SQL_SFKD_SET_NULL;
            break;
        case SQL_SQL92_FOREIGN_KEY_UPDATE_RULE:
            len = 4;
            value = SQL_SFKU_CASCADE | SQL_SFKU_NO_ACTION | SQL_SFKU_SET_DEFAULT
                    | SQL_SFKU_SET_NULL;
            break;
        case SQL_SQL92_GRANT:
            len = 4;
            value = SQL_SG_DELETE_TABLE | SQL_SG_INSERT_TABLE
                    | SQL_SG_REFERENCES_TABLE | SQL_SG_SELECT_TABLE
                    | SQL_SG_UPDATE_TABLE;
            break;
        case SQL_SQL92_NUMERIC_VALUE_FUNCTIONS:
            len = 4;
            value = 0;
            break;
        case SQL_SQL92_PREDICATES:
            len = 4;
            value = SQL_SP_BETWEEN | SQL_SP_COMPARISON | SQL_SP_IN
                    | SQL_SP_ISNULL | SQL_SP_LIKE;
            break;
        case SQL_SQL92_RELATIONAL_JOIN_OPERATORS:
            len = 4;
            value = SQL_SRJO_CROSS_JOIN | SQL_SRJO_INNER_JOIN
                    | SQL_SRJO_LEFT_OUTER_JOIN | SQL_SRJO_RIGHT_OUTER_JOIN;
            break;
        case SQL_SQL92_REVOKE:
            len = 4;
            value = SQL_SR_DELETE_TABLE | SQL_SR_INSERT_TABLE
                    | SQL_SR_REFERENCES_TABLE | SQL_SR_SELECT_TABLE
                    | SQL_SR_UPDATE_TABLE;
            break;
        case SQL_SQL92_ROW_VALUE_CONSTRUCTOR:
            len = 4;
            value = SQL_SRVC_VALUE_EXPRESSION | SQL_SRVC_NULL;
            break;
        case SQL_SQL92_STRING_FUNCTIONS:
            len = 4;
            value = SQL_SSF_LOWER | SQL_SSF_UPPER;
            break;
        case SQL_SQL92_VALUE_EXPRESSIONS:
            len = 4;
            value = SQL_SVE_CASE | SQL_SVE_CAST;
            break;
#ifdef SQL_DTC_TRANSACTION_COST
        case SQL_DTC_TRANSACTION_COST:
#else
        case 1750:
#endif
            len = 4;
            break;
        case SQL_DATETIME_LITERALS:
        case SQL_DRIVER_HDESC:
        case SQL_MAX_ASYNC_CONCURRENT_STATEMENTS:
        case SQL_STANDARD_CLI_CONFORMANCE:
        case SQL_CONVERT_INTERVAL_DAY_TIME:
            len = 4;
            value = 0;
            break;
        case SQL_DM_VER:
        case SQL_XOPEN_CLI_YEAR:
            len = 0;
            value = 0;
            break;

        default:
            /* unrecognized key */
            CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR,
                         "Unrecognized key passed to ESAPI_GetInfo.", NULL);
            goto cleanup;
    }

    ret = SQL_SUCCESS;

    MYLOG(ES_DEBUG, "p='%s', len=" FORMAT_ULEN ", value=" FORMAT_ULEN ", cbMax=%d\n",
          p ? p : "<NULL>", len, value, cbInfoValueMax);

    /*
     * NOTE, that if rgbInfoValue is NULL, then no warnings or errors
     * should result and just pcbInfoValue is returned, which indicates
     * what length would be required if a real buffer had been passed in.
     */
    if (p) {
        /* char/binary data */
        len = strlen(p);

        if (rgbInfoValue) {
#ifdef UNICODE_SUPPORT
            if (CC_is_in_unicode_driver(conn)) {
                len = utf8_to_ucs2(p, len, (SQLWCHAR *)rgbInfoValue,
                                   cbInfoValueMax / WCLEN);
                len *= WCLEN;
            } else
#endif /* UNICODE_SUPPORT */
                strncpy_null((char *)rgbInfoValue, p, (size_t)cbInfoValueMax);

            if (len >= (SQLULEN)cbInfoValueMax) {
                ret = SQL_SUCCESS_WITH_INFO;
                CC_set_error(conn, CONN_TRUNCATED,
                             "The buffer was too small for the InfoValue.",
                             func);
            }
        }
#ifdef UNICODE_SUPPORT
        else if (CC_is_in_unicode_driver(conn))
            len *= WCLEN;
#endif /* UNICODE_SUPPORT */
    } else {
        /* numeric data */
        if (rgbInfoValue) {
            if (len == sizeof(SQLSMALLINT))
                *((SQLUSMALLINT *)rgbInfoValue) = (SQLUSMALLINT)value;
            else if (len == sizeof(SQLINTEGER))
                *((SQLUINTEGER *)rgbInfoValue) = (SQLUINTEGER)value;
        }
    }

    if (pcbInfoValue)
        *pcbInfoValue = (SQLSMALLINT)len;
cleanup:

    return ret;
}

/*
 *	macros for estype_xxxx() calls which have ES_ATP_UNSET parameters
 */
#define ESTYPE_COLUMN_SIZE(conn, esType)                              \
    estype_attr_column_size(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET, \
                            ES_UNKNOWNS_UNSET)
#define ESTYPE_TO_CONCISE_TYPE(conn, esType)                              \
    estype_attr_to_concise_type(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET, \
                                ES_UNKNOWNS_UNSET)
#define ESTYPE_TO_SQLDESCTYPE(conn, esType)                              \
    estype_attr_to_sqldesctype(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET, \
                               ES_UNKNOWNS_UNSET)
#define ESTYPE_BUFFER_LENGTH(conn, esType)                              \
    estype_attr_buffer_length(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET, \
                              ES_UNKNOWNS_UNSET)
#define ESTYPE_DECIMAL_DIGITS(conn, esType)                              \
    estype_attr_decimal_digits(conn, esType, ES_ATP_UNSET, ES_ADT_UNSET, \
                               ES_UNKNOWNS_UNSET)
#define ESTYPE_TRANSFER_OCTET_LENGTH(conn, esType)                \
    estype_attr_transfer_octet_length(conn, esType, ES_ATP_UNSET, \
                                      ES_UNKNOWNS_UNSET)
#define ESTYPE_TO_NAME(conn, esType, auto_increment) \
    estype_attr_to_name(conn, esType, ES_ATP_UNSET, auto_increment)

RETCODE SQL_API ESAPI_GetFunctions(HDBC hdbc, SQLUSMALLINT fFunction,
                                   SQLUSMALLINT *pfExists) {
    UNUSED(hdbc);
    MYLOG(ES_TRACE, "entering...%u\n", fFunction);

    if (fFunction == SQL_API_ALL_FUNCTIONS) {
        memset(pfExists, 0, sizeof(pfExists[0]) * 100);

        /* ODBC core functions */
        pfExists[SQL_API_SQLALLOCCONNECT] = TRUE;
        pfExists[SQL_API_SQLALLOCENV] = TRUE;
        pfExists[SQL_API_SQLALLOCSTMT] = TRUE;
        pfExists[SQL_API_SQLBINDCOL] = TRUE;
        pfExists[SQL_API_SQLCANCEL] = TRUE;
        pfExists[SQL_API_SQLCOLATTRIBUTES] = TRUE;
        pfExists[SQL_API_SQLCONNECT] = TRUE;
        pfExists[SQL_API_SQLDESCRIBECOL] = TRUE; /* partial */
        pfExists[SQL_API_SQLDISCONNECT] = TRUE;
        pfExists[SQL_API_SQLERROR] = TRUE;
        pfExists[SQL_API_SQLEXECDIRECT] = TRUE;
        pfExists[SQL_API_SQLEXECUTE] = TRUE;
        pfExists[SQL_API_SQLFETCH] = TRUE;
        pfExists[SQL_API_SQLFREECONNECT] = TRUE;
        pfExists[SQL_API_SQLFREEENV] = TRUE;
        pfExists[SQL_API_SQLFREESTMT] = TRUE;
        pfExists[SQL_API_SQLGETCURSORNAME] = TRUE;
        pfExists[SQL_API_SQLNUMRESULTCOLS] = TRUE;
        pfExists[SQL_API_SQLPREPARE] = TRUE; /* complete? */
        pfExists[SQL_API_SQLROWCOUNT] = TRUE;
        pfExists[SQL_API_SQLSETCURSORNAME] = TRUE;
        pfExists[SQL_API_SQLSETPARAM] = FALSE; /* odbc 1.0 */
        pfExists[SQL_API_SQLTRANSACT] = TRUE;

        /* ODBC level 1 functions */
        pfExists[SQL_API_SQLBINDPARAMETER] = TRUE;
        pfExists[SQL_API_SQLCOLUMNS] = TRUE;
        pfExists[SQL_API_SQLDRIVERCONNECT] = TRUE;
        pfExists[SQL_API_SQLGETCONNECTOPTION] = TRUE; /* partial */
        pfExists[SQL_API_SQLGETDATA] = TRUE;
        pfExists[SQL_API_SQLGETFUNCTIONS] = TRUE;
        pfExists[SQL_API_SQLGETINFO] = TRUE;
        pfExists[SQL_API_SQLGETSTMTOPTION] = TRUE; /* partial */
        pfExists[SQL_API_SQLGETTYPEINFO] = TRUE;
        pfExists[SQL_API_SQLPARAMDATA] = TRUE;
        pfExists[SQL_API_SQLPUTDATA] = TRUE;
        pfExists[SQL_API_SQLSETCONNECTOPTION] = TRUE; /* partial */
        pfExists[SQL_API_SQLSETSTMTOPTION] = TRUE;
        pfExists[SQL_API_SQLSPECIALCOLUMNS] = TRUE;
        pfExists[SQL_API_SQLSTATISTICS] = TRUE;
        pfExists[SQL_API_SQLTABLES] = TRUE;

        /* ODBC level 2 functions */
        pfExists[SQL_API_SQLBROWSECONNECT] = FALSE;
        pfExists[SQL_API_SQLCOLUMNPRIVILEGES] = FALSE;
        pfExists[SQL_API_SQLDATASOURCES] = FALSE; /* only implemented by
                                                   * DM */
        if (SUPPORT_DESCRIBE_PARAM(ci))
            pfExists[SQL_API_SQLDESCRIBEPARAM] = TRUE;
        else
            pfExists[SQL_API_SQLDESCRIBEPARAM] = FALSE; /* not properly
                                                         * implemented */
        pfExists[SQL_API_SQLDRIVERS] = FALSE;           /* only implemented by
                                                         * DM */
        pfExists[SQL_API_SQLEXTENDEDFETCH] = TRUE;
        pfExists[SQL_API_SQLFOREIGNKEYS] = TRUE;
        pfExists[SQL_API_SQLMORERESULTS] = TRUE;
        pfExists[SQL_API_SQLNATIVESQL] = TRUE;
        pfExists[SQL_API_SQLNUMPARAMS] = TRUE;
        pfExists[SQL_API_SQLPARAMOPTIONS] = TRUE;
        pfExists[SQL_API_SQLPRIMARYKEYS] = TRUE;
        pfExists[SQL_API_SQLPROCEDURECOLUMNS] = TRUE;
        pfExists[SQL_API_SQLPROCEDURES] = TRUE;
        pfExists[SQL_API_SQLSETPOS] = TRUE;
        pfExists[SQL_API_SQLSETSCROLLOPTIONS] = TRUE; /* odbc 1.0 */
        pfExists[SQL_API_SQLTABLEPRIVILEGES] = TRUE;
        pfExists[SQL_API_SQLBULKOPERATIONS] = FALSE;
    } else {
        switch (fFunction) {
            case SQL_API_SQLBINDCOL:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLCANCEL:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLCOLATTRIBUTE:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLCONNECT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLDESCRIBECOL:
                *pfExists = TRUE;
                break; /* partial */
            case SQL_API_SQLDISCONNECT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLEXECDIRECT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLEXECUTE:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLFETCH:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLFREESTMT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETCURSORNAME:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLNUMRESULTCOLS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPREPARE:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLROWCOUNT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLSETCURSORNAME:
                *pfExists = TRUE;
                break;

                /* ODBC level 1 functions */
            case SQL_API_SQLBINDPARAMETER:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLCOLUMNS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLDRIVERCONNECT:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETDATA:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETFUNCTIONS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETINFO:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETTYPEINFO:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPARAMDATA:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPUTDATA:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLSPECIALCOLUMNS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLSTATISTICS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLTABLES:
                *pfExists = TRUE;
                break;

                /* ODBC level 2 functions */
            case SQL_API_SQLBROWSECONNECT:
                *pfExists = FALSE;
                break;
            case SQL_API_SQLCOLUMNPRIVILEGES:
                *pfExists = FALSE;
                break;
            case SQL_API_SQLDATASOURCES:
                *pfExists = FALSE;
                break; /* only implemented by DM */
            case SQL_API_SQLDESCRIBEPARAM:
                if (SUPPORT_DESCRIBE_PARAM(ci))
                    *pfExists = TRUE;
                else
                    *pfExists = FALSE;
                break; /* not properly implemented */
            case SQL_API_SQLDRIVERS:
                *pfExists = FALSE;
                break; /* only implemented by DM */
            case SQL_API_SQLEXTENDEDFETCH:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLFOREIGNKEYS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLMORERESULTS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLNATIVESQL:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLNUMPARAMS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPRIMARYKEYS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPROCEDURECOLUMNS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLPROCEDURES:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLSETPOS:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLTABLEPRIVILEGES:
                *pfExists = TRUE;
                break;
            case SQL_API_SQLBULKOPERATIONS: /* 24 */
            case SQL_API_SQLALLOCHANDLE:    /* 1001 */
            case SQL_API_SQLBINDPARAM:      /* 1002 */
            case SQL_API_SQLCLOSECURSOR:    /* 1003 */
            case SQL_API_SQLENDTRAN:        /* 1005 */
            case SQL_API_SQLFETCHSCROLL:    /* 1021 */
            case SQL_API_SQLFREEHANDLE:     /* 1006 */
            case SQL_API_SQLGETCONNECTATTR: /* 1007 */
            case SQL_API_SQLGETDESCFIELD:   /* 1008 */
            case SQL_API_SQLGETDIAGFIELD:   /* 1010 */
            case SQL_API_SQLGETDIAGREC:     /* 1011 */
            case SQL_API_SQLGETENVATTR:     /* 1012 */
            case SQL_API_SQLGETSTMTATTR:    /* 1014 */
            case SQL_API_SQLSETCONNECTATTR: /* 1016 */
            case SQL_API_SQLSETDESCFIELD:   /* 1017 */
            case SQL_API_SQLSETENVATTR:     /* 1019 */
            case SQL_API_SQLSETSTMTATTR:    /* 1020 */
                *pfExists = TRUE;
                break;
            case SQL_API_SQLGETDESCREC: /* 1009 */
            case SQL_API_SQLSETDESCREC: /* 1018 */
            case SQL_API_SQLCOPYDESC:   /* 1004 */
                *pfExists = FALSE;
                break;
            default:
                *pfExists = FALSE;
                break;
        }
    }
    return SQL_SUCCESS;
}

char *identifierEscape(const SQLCHAR *src, SQLLEN srclen,
                       const ConnectionClass *conn, char *buf, size_t bufsize,
                       BOOL double_quote) {
    int i;
    size_t outlen;
    UCHAR tchar;
    char *dest = NULL, escape_ch = CC_get_escape(conn);
    encoded_str encstr;

    if (!src || srclen == SQL_NULL_DATA)
        return dest;
    else if (srclen == SQL_NTS)
        srclen = (SQLLEN)strlen((char *)src);
    if (srclen <= 0)
        return dest;
    MYLOG(ES_TRACE, "entering in=%s(" FORMAT_LEN ")\n", src, srclen);
    if (NULL != buf && bufsize > 0)
        dest = buf;
    else {
        bufsize = 2 * srclen + 1;
        dest = malloc(bufsize);
    }
    if (!dest)
        return NULL;
    encoded_str_constr(&encstr, conn->ccsc, (char *)src);
    outlen = 0;
    if (double_quote)
        dest[outlen++] = IDENTIFIER_QUOTE;
    for (i = 0, tchar = (UCHAR)encoded_nextchar(&encstr);
         i < srclen && outlen < bufsize - 1;
         i++, tchar = (UCHAR)encoded_nextchar(&encstr)) {
        if (MBCS_NON_ASCII(encstr)) {
            dest[outlen++] = tchar;
            continue;
        }
        if (LITERAL_QUOTE == tchar || escape_ch == tchar)
            dest[outlen++] = tchar;
        else if (double_quote && IDENTIFIER_QUOTE == tchar)
            dest[outlen++] = tchar;
        dest[outlen++] = tchar;
    }
    if (double_quote)
        dest[outlen++] = IDENTIFIER_QUOTE;
    dest[outlen] = '\0';
    MYLOG(ES_TRACE, "leaving output=%s(%d)\n", dest, (int)outlen);
    return dest;
}

#define CSTR_SYS_TABLE "SYSTEM TABLE"
#define CSTR_TABLE "TABLE"
#define CSTR_VIEW "VIEW"
#define CSTR_FOREIGN_TABLE "FOREIGN TABLE"
#define CSTR_MATVIEW "MATVIEW"

#define IS_VALID_NAME(str) ((str) && (str)[0])
#define TABLE_IN_RELKIND "('r', 'v', 'm', 'f', 'p')"

/*
 *	macros for estype_attr_xxxx() calls which have
 *		ES_ADT_UNSET or ES_UNKNOWNS_UNSET parameters
 */
#define ESTYPE_ATTR_COLUMN_SIZE(conn, esType, atttypmod)           \
    estype_attr_column_size(conn, esType, atttypmod, ES_ADT_UNSET, \
                            ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_TO_CONCISE_TYPE(conn, esType, atttypmod)           \
    estype_attr_to_concise_type(conn, esType, atttypmod, ES_ADT_UNSET, \
                                ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_TO_SQLDESCTYPE(conn, esType, atttypmod)           \
    estype_attr_to_sqldesctype(conn, esType, atttypmod, ES_ADT_UNSET, \
                               ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_DISPLAY_SIZE(conn, esType, atttypmod)           \
    estype_attr_display_size(conn, esType, atttypmod, ES_ADT_UNSET, \
                             ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_BUFFER_LENGTH(conn, esType, atttypmod)           \
    estype_attr_buffer_length(conn, esType, atttypmod, ES_ADT_UNSET, \
                              ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_DECIMAL_DIGITS(conn, esType, atttypmod)           \
    estype_attr_decimal_digits(conn, esType, atttypmod, ES_ADT_UNSET, \
                               ES_UNKNOWNS_UNSET)
#define ESTYPE_ATTR_TRANSFER_OCTET_LENGTH(conn, esType, atttypmod) \
    estype_attr_transfer_octet_length(conn, esType, atttypmod,     \
                                      ES_UNKNOWNS_UNSET)

RETCODE SQL_API ESAPI_SpecialColumns(
    HSTMT hstmt, SQLUSMALLINT fColType, const SQLCHAR *szTableQualifier,
    SQLSMALLINT cbTableQualifier, const SQLCHAR *szTableOwner, /* OA E*/
    SQLSMALLINT cbTableOwner, const SQLCHAR *szTableName,      /* OA(R) E*/
    SQLSMALLINT cbTableName, SQLUSMALLINT fScope, SQLUSMALLINT fNullable) {
    UNUSED(fColType, szTableQualifier, cbTableQualifier, szTableOwner,
           cbTableOwner, szTableName, cbTableName, fScope, fNullable);
    CSTR func = "ESAPI_SpecialColumns";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE result;
    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (!res) {
        SC_set_error(
            stmt, STMT_NO_MEMORY_ERROR,
            "Couldn't allocate memory for ESAPI_SpecialColumns result.", func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and connection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_SPECOLS_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_SPECOLS_FIELDS);
    QR_set_field_info_v(res, SPECOLS_SCOPE, "SCOPE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, SPECOLS_COLUMN_NAME, "COLUMN_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, SPECOLS_DATA_TYPE, "DATA_TYPE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, SPECOLS_TYPE_NAME, "TYPE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, SPECOLS_COLUMN_SIZE, "COLUMN_SIZE", ES_TYPE_INT4,
                        4);
    QR_set_field_info_v(res, SPECOLS_BUFFER_LENGTH, "BUFFER_LENGTH",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, SPECOLS_DECIMAL_DIGITS, "DECIMAL_DIGITS",
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, SPECOLS_PSEUDO_COLUMN, "PSEUDO_COLUMN",
                        ES_TYPE_INT2, 2);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return SQL_SUCCESS;
}

#define INDOPTION_DESC 0x0001 /* values are in reverse order */
RETCODE SQL_API ESAPI_Statistics(
    HSTMT hstmt, const SQLCHAR *szTableQualifier,              /* OA X*/
    SQLSMALLINT cbTableQualifier, const SQLCHAR *szTableOwner, /* OA E*/
    SQLSMALLINT cbTableOwner, const SQLCHAR *szTableName,      /* OA(R) E*/
    SQLSMALLINT cbTableName, SQLUSMALLINT fUnique, SQLUSMALLINT fAccuracy) {
    UNUSED(szTableQualifier, cbTableQualifier, szTableOwner, cbTableOwner,
           szTableName, cbTableName, fUnique, fAccuracy);
    CSTR func = "ESAPI_Statistics";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE result;
    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (!res) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for ESAPI_Statistics result.",
                     func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and connection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_STATS_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_STATS_FIELDS);
    QR_set_field_info_v(res, STATS_CATALOG_NAME, "TABLE_QUALIFIER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_SCHEMA_NAME, "TABLE_OWNER", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_TABLE_NAME, "TABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_NON_UNIQUE, "NON_UNIQUE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, STATS_INDEX_QUALIFIER, "INDEX_QUALIFIER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_INDEX_NAME, "INDEX_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_TYPE, "TYPE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, STATS_SEQ_IN_INDEX, "SEQ_IN_INDEX", ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, STATS_COLUMN_NAME, "COLUMN_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, STATS_COLLATION, "COLLATION", ES_TYPE_CHAR, 1);
    QR_set_field_info_v(res, STATS_CARDINALITY, "CARDINALITY", ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, STATS_PAGES, "PAGES", ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, STATS_FILTER_CONDITION, "FILTER_CONDITION",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_ColumnPrivileges(
    HSTMT hstmt, const SQLCHAR *szTableQualifier,              /* OA X*/
    SQLSMALLINT cbTableQualifier, const SQLCHAR *szTableOwner, /* OA E*/
    SQLSMALLINT cbTableOwner, const SQLCHAR *szTableName,      /* OA(R) E*/
    SQLSMALLINT cbTableName, const SQLCHAR *szColumnName,      /* PV E*/
    SQLSMALLINT cbColumnName, UWORD flag) {
    UNUSED(szTableQualifier, cbTableQualifier, szTableOwner, cbTableOwner,
           szTableName, cbTableName, szColumnName, cbColumnName, flag);
    CSTR func = "ESAPI_ColumnPrivileges";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE result;
    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (!res) {
        SC_set_error(
            stmt, STMT_NO_MEMORY_ERROR,
            "Couldn't allocate memory for ESAPI_ColumnPrivileges result.",
            func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and connection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_COLPRIV_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_COLPRIV_FIELDS);
    QR_set_field_info_v(res, COLPRIV_TABLE_CAT, "TABLE_CAT", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_TABLE_SCHEM, "TABLE_SCHEM",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_TABLE_NAME, "TABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_COLUMN_NAME, "COLUMN_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_GRANTOR, "GRANTOR", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_GRANTEE, "GRANTEE", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_PRIVILEGE, "PRIVILEGE", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, COLPRIV_IS_GRANTABLE, "IS_GRANTABLE",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return SQL_SUCCESS;
}

/*
 *	SQLPrimaryKeys()
 *
 *	Retrieve the primary key columns for the specified table.
 */
RETCODE SQL_API ESAPI_PrimaryKeys(HSTMT hstmt,
                                  const SQLCHAR *szTableQualifier, /* OA X*/
                                  SQLSMALLINT cbTableQualifier,
                                  const SQLCHAR *szTableOwner, /* OA E*/
                                  SQLSMALLINT cbTableOwner,
                                  const SQLCHAR *szTableName, /* OA(R) E*/
                                  SQLSMALLINT cbTableName, OID reloid) {
    UNUSED(szTableQualifier, cbTableQualifier, szTableOwner, cbTableOwner,
           szTableName, cbTableName, reloid);
    CSTR func = "ESAPI_PrimaryKeys";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE ret = SC_initialize_and_recycle(stmt);
    if (ret != SQL_SUCCESS)
        return ret;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (res == NULL) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for ESAPI_PrimaryKeys result.",
                     func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and cnnection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_PKS_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_PKS_FIELDS);
    QR_set_field_info_v(res, PKS_TABLE_CAT, "TABLE_QUALIFIER", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PKS_TABLE_SCHEM, "TABLE_OWNER", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PKS_TABLE_NAME, "TABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PKS_COLUMN_NAME, "COLUMN_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PKS_KEY_SQ, "KEY_SEQ", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PKS_PK_NAME, "PK_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->currTuple = -1;
    stmt->status = STMT_FINISHED;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return ret;
}

RETCODE SQL_API ESAPI_ForeignKeys(
    HSTMT hstmt, const SQLCHAR *szPkTableQualifier,                /* OA X*/
    SQLSMALLINT cbPkTableQualifier, const SQLCHAR *szPkTableOwner, /* OA E*/
    SQLSMALLINT cbPkTableOwner, const SQLCHAR *szPkTableName,      /* OA(R) E*/
    SQLSMALLINT cbPkTableName, const SQLCHAR *szFkTableQualifier,  /* OA X*/
    SQLSMALLINT cbFkTableQualifier, const SQLCHAR *szFkTableOwner, /* OA E*/
    SQLSMALLINT cbFkTableOwner, const SQLCHAR *szFkTableName,      /* OA(R) E*/
    SQLSMALLINT cbFkTableName) {
    UNUSED(szPkTableQualifier, cbPkTableQualifier, szPkTableOwner,
           cbPkTableOwner, szPkTableName, cbPkTableName, szFkTableQualifier,
           cbFkTableQualifier, szFkTableOwner, cbFkTableOwner, szFkTableName,
           cbFkTableName);
    CSTR func = "ESAPI_ForeignKeys";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE result;
    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (!res) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for ESAPI_ForeignKeys result.",
                     func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and connection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_FKS_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_FKS_FIELDS);
    QR_set_field_info_v(res, FKS_PKTABLE_CAT, "PKTABLE_QUALIFIER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_PKTABLE_SCHEM, "PKTABLE_OWNER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_PKTABLE_NAME, "PKTABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_PKCOLUMN_NAME, "PKCOLUMN_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_FKTABLE_CAT, "FKTABLE_QUALIFIER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_FKTABLE_SCHEM, "FKTABLE_OWNER",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_FKTABLE_NAME, "FKTABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_FKCOLUMN_NAME, "FKCOLUMN_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_KEY_SEQ, "KEY_SEQ", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, FKS_UPDATE_RULE, "UPDATE_RULE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, FKS_DELETE_RULE, "DELETE_RULE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, FKS_FK_NAME, "FK_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_PK_NAME, "PK_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, FKS_DEFERRABILITY, "DEFERRABILITY", ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, FKS_TRIGGER_NAME, "TRIGGER_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return SQL_SUCCESS;
}

#define PRORET_COUNT
#define DISPLAY_ARGNAME

RETCODE SQL_API ESAPI_ProcedureColumns(
    HSTMT hstmt, const SQLCHAR *szProcQualifier,             /* OA X*/
    SQLSMALLINT cbProcQualifier, const SQLCHAR *szProcOwner, /* PV E*/
    SQLSMALLINT cbProcOwner, const SQLCHAR *szProcName,      /* PV E*/
    SQLSMALLINT cbProcName, const SQLCHAR *szColumnName,     /* PV X*/
    SQLSMALLINT cbColumnName, UWORD flag) {
    UNUSED(szProcQualifier, cbProcQualifier, szProcOwner, cbProcOwner,
           szProcName, cbProcName, szColumnName, cbColumnName, flag);
    CSTR func = "ESAPI_ProcedureColumns";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE ret = SC_initialize_and_recycle(stmt);
    if (ret != SQL_SUCCESS)
        return ret;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (res == NULL) {
        SC_set_error(
            stmt, STMT_NO_MEMORY_ERROR,
            "Couldn't allocate memory for ESAPI_ProcedureColumns result.",
            func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and cnnection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_PROCOLS_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_PROCOLS_FIELDS);
    QR_set_field_info_v(res, PROCOLS_PROCEDURE_CAT, "PROCEDURE_CAT",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_PROCEDURE_SCHEM, "PROCEDUR_SCHEM",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_PROCEDURE_NAME, "PROCEDURE_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_COLUMN_NAME, "COLUMN_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_COLUMN_TYPE, "COLUMN_TYPE", ES_TYPE_INT2,
                        2);
    QR_set_field_info_v(res, PROCOLS_DATA_TYPE, "DATA_TYPE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_TYPE_NAME, "TYPE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_COLUMN_SIZE, "COLUMN_SIZE", ES_TYPE_INT4,
                        4);
    QR_set_field_info_v(res, PROCOLS_BUFFER_LENGTH, "BUFFER_LENGTH",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, PROCOLS_DECIMAL_DIGITS, "DECIMAL_DIGITS",
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_NUM_PREC_RADIX, "NUM_PREC_RADIX",
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_NULLABLE, "NULLABLE", ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_REMARKS, "REMARKS", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_COLUMN_DEF, "COLUMN_DEF", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PROCOLS_SQL_DATA_TYPE, "SQL_DATA_TYPE",
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_SQL_DATETIME_SUB, "SQL_DATETIME_SUB",
                        ES_TYPE_INT2, 2);
    QR_set_field_info_v(res, PROCOLS_CHAR_OCTET_LENGTH, "CHAR_OCTET_LENGTH",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, PROCOLS_ORDINAL_POSITION, "ORDINAL_POSITION",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, PROCOLS_IS_NULLABLE, "IS_NULLABLE",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->currTuple = -1;
    stmt->status = STMT_FINISHED;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return ret;
}

RETCODE SQL_API ESAPI_Procedures(HSTMT hstmt,
                                 const SQLCHAR *szProcQualifier, /* OA X*/
                                 SQLSMALLINT cbProcQualifier,
                                 const SQLCHAR *szProcOwner, /* PV E*/
                                 SQLSMALLINT cbProcOwner,
                                 const SQLCHAR *szProcName, /* PV E*/
                                 SQLSMALLINT cbProcName, UWORD flag) {
    UNUSED(szProcQualifier, cbProcQualifier, szProcOwner, cbProcOwner,
           szProcName, cbProcName, flag);
    CSTR func = "ESAPI_Procedures";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE ret = SC_initialize_and_recycle(stmt);
    if (ret != SQL_SUCCESS)
        return ret;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (res == NULL) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for ESAPI_Procedures result.",
                     func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and cnnection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_PRO_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_PRO_FIELDS);
    QR_set_field_info_v(res, PRO_PROCEDURE_CAT, "PRO_PROCEDURE_CAT",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PRO_PROCEDURE_SCHEM, "PRO_PROCEDURE_SCHEM",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PRO_PROCEDURE_NAME, "PRO_PROCEDURE_NAME",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, PRO_NUM_INPUT_PARAMS, "PRO_NUM_INPUT_PARAMS",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, PRO_NUM_OUTPUT_PARAMS, "PRO_NUM_OUTPUT_PARAMS",
                        ES_TYPE_INT4, 4);
    QR_set_field_info_v(res, PRO_RESULT_SETS, "PRO_RESULT_SETS", ES_TYPE_INT4,
                        4);
    QR_set_field_info_v(res, PRO_REMARKS, "PRO_REMARKS", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, PRO_PROCEDURE_TYPE, "PRO_PROCEDURE_TYPE",
                        ES_TYPE_INT2, 2);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->currTuple = -1;
    stmt->status = STMT_FINISHED;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return ret;
}

#define ACLMAX 8
#define ALL_PRIVILIGES "arwdRxt"

RETCODE SQL_API ESAPI_TablePrivileges(HSTMT hstmt,
                                      const SQLCHAR *szTableQualifier, /* OA X*/
                                      SQLSMALLINT cbTableQualifier,
                                      const SQLCHAR *szTableOwner, /* PV E*/
                                      SQLSMALLINT cbTableOwner,
                                      const SQLCHAR *szTableName, /* PV E*/
                                      SQLSMALLINT cbTableName, UWORD flag) {
    UNUSED(szTableQualifier, cbTableQualifier, szTableOwner, cbTableOwner,
           szTableName, cbTableName, flag);
    CSTR func = "ESAPI_TablePrivileges";

    // Initialize Statement
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE result;
    if (result = SC_initialize_and_recycle(stmt), SQL_SUCCESS != result)
        return result;

    // Initialize QResultClass
    QResultClass *res = QR_Constructor();
    if (!res) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Couldn't allocate memory for ESAPI_Statistics result.",
                     func);
        return SQL_ERROR;
    }

    // Link QResultClass to statement and connection
    QR_set_conn(res, SC_get_conn(stmt));
    SC_set_Result(stmt, res);

    // Set number of fields and declare as catalog result
    extend_column_bindings(SC_get_ARDF(stmt), NUM_OF_TABPRIV_FIELDS);
    stmt->catalog_result = TRUE;

    // Setup fields
    QR_set_num_fields(res, NUM_OF_TABPRIV_FIELDS);
    QR_set_field_info_v(res, TABPRIV_TABLE_CAT, "TABLE_CAT", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_TABLE_SCHEM, "TABLE_SCHEM",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_TABLE_NAME, "TABLE_NAME", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_GRANTOR, "GRANTOR", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_GRANTEE, "GRANTEE", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_PRIVILEGE, "PRIVILEGE", ES_TYPE_VARCHAR,
                        MAX_INFO_STRING);
    QR_set_field_info_v(res, TABPRIV_IS_GRANTABLE, "IS_GRANTABLE",
                        ES_TYPE_VARCHAR, MAX_INFO_STRING);

    // Set result to okay and adjust fields if keys exist
    QR_set_rstatus(res, PORES_FIELDS_OK);
    res->num_fields = CI_get_num_fields(QR_get_fields(res));
    if (QR_haskeyset(res))
        res->num_fields -= res->num_key_fields;

    // Finalize data
    stmt->status = STMT_FINISHED;
    stmt->currTuple = -1;
    SC_set_rowset_start(stmt, -1, FALSE);
    SC_set_current_col(stmt, -1);

    return SQL_SUCCESS;
}
