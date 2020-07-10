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

#include <string.h>

#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_odbc.h"
#include "misc.h"
#include "qresult.h"
#include "statement.h"
#include "unicode_support.h"

static RETCODE set_statement_option(ConnectionClass *conn, StatementClass *stmt,
                                    SQLUSMALLINT fOption, SQLULEN vParam) {
    CSTR func = "set_statement_option";
    char changed = FALSE;
    ConnInfo *ci = NULL;
    SQLULEN setval;

    if (conn)
        ci = &(conn->connInfo);
    else
        ci = &(SC_get_conn(stmt)->connInfo);
    switch (fOption) {
        case SQL_ASYNC_ENABLE: /* ignored */
            break;

        case SQL_BIND_TYPE:
            /* now support multi-column and multi-row binding */
            if (conn)
                conn->ardOptions.bind_size = (SQLUINTEGER)vParam;
            if (stmt)
                SC_get_ARDF(stmt)->bind_size = (SQLUINTEGER)vParam;
            break;

        case SQL_CONCURRENCY:
            /*
             * positioned update isn't supported so cursor concurrency is
             * read-only
             */
            MYLOG(ES_DEBUG, "SQL_CONCURRENCY = " FORMAT_LEN " ", vParam);
            setval = SQL_CONCUR_READ_ONLY;
            if (conn)
                conn->stmtOptions.scroll_concurrency = (SQLUINTEGER)setval;
            else if (stmt) {
                if (SC_get_Result(stmt)) {
                    SC_set_error(
                        stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                        "The attr can't be changed because the cursor is open.",
                        func);
                    return SQL_ERROR;
                }
                stmt->options.scroll_concurrency =
                    stmt->options_orig.scroll_concurrency = (SQLUINTEGER)setval;
            }
            if (setval != vParam)
                changed = TRUE;
            MYPRINTF(0, "-> " FORMAT_LEN "\n", setval);
            break;

        case SQL_CURSOR_TYPE:
            /*
             * if declare/fetch, then type can only be forward. otherwise,
             * it can only be forward or static.
             */
            MYLOG(ES_DEBUG, "SQL_CURSOR_TYPE = " FORMAT_LEN " ", vParam);
            setval = SQL_CURSOR_FORWARD_ONLY;
            if (SQL_CURSOR_STATIC == vParam)
                setval = vParam;
            else if (SQL_CURSOR_KEYSET_DRIVEN == vParam) {
                setval = SQL_CURSOR_STATIC; /* at least scrollable */
            } else if (SQL_CURSOR_DYNAMIC == vParam) {
                setval = SQL_CURSOR_STATIC; /* at least scrollable */
            }
            if (conn)
                conn->stmtOptions.cursor_type = (SQLUINTEGER)setval;
            else if (stmt) {
                if (SC_get_Result(stmt)) {
                    SC_set_error(
                        stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                        "The attr can't be changed because the cursor is open.",
                        func);
                    return SQL_ERROR;
                }
                stmt->options_orig.cursor_type = stmt->options.cursor_type =
                    (SQLUINTEGER)setval;
            }
            if (setval != vParam)
                changed = TRUE;
            MYPRINTF(0, "-> " FORMAT_LEN "\n", setval);
            break;

        case SQL_KEYSET_SIZE: /* ignored, but saved and returned	*/
            MYLOG(ES_DEBUG, "SQL_KEYSET_SIZE, vParam = " FORMAT_LEN "\n",
                  vParam);

            if (conn)
                conn->stmtOptions.keyset_size = vParam;
            if (stmt) {
                stmt->options_orig.keyset_size = vParam;
                if (!SC_get_Result(stmt))
                    stmt->options.keyset_size = vParam;
                if (stmt->options.keyset_size != (SQLLEN)vParam)
                    changed = TRUE;
            }

            break;

        case SQL_MAX_LENGTH: /* ignored, but saved */
            MYLOG(ES_DEBUG, "SQL_MAX_LENGTH, vParam = " FORMAT_LEN "\n",
                  vParam);
            if (conn)
                conn->stmtOptions.maxLength = vParam;
            if (stmt) {
                stmt->options_orig.maxLength = vParam;
                if (!SC_get_Result(stmt))
                    stmt->options.maxLength = vParam;
                if (stmt->options.maxLength != (SQLLEN)vParam)
                    changed = TRUE;
            }
            break;

        case SQL_MAX_ROWS: /* ignored, but saved */
            MYLOG(ES_DEBUG, "SQL_MAX_ROWS, vParam = " FORMAT_LEN "\n", vParam);
            if (conn)
                conn->stmtOptions.maxRows = vParam;
            if (stmt) {
                stmt->options_orig.maxRows = vParam;
                if (!SC_get_Result(stmt))
                    stmt->options.maxRows = vParam;
                if (stmt->options.maxRows != (SQLLEN)vParam)
                    changed = TRUE;
            }
            break;

        case SQL_NOSCAN: /* ignored */
            MYLOG(ES_DEBUG, "SQL_NOSCAN, vParam = " FORMAT_LEN "\n", vParam);
            break;

        case SQL_QUERY_TIMEOUT: /* ignored */
            MYLOG(ES_DEBUG, "SQL_QUERY_TIMEOUT, vParam = " FORMAT_LEN "\n",
                  vParam);
            if (conn)
                conn->stmtOptions.stmt_timeout = (SQLULEN)vParam;
            if (stmt)
                stmt->options.stmt_timeout = (SQLULEN)vParam;
            break;

        case SQL_RETRIEVE_DATA:
            MYLOG(ES_DEBUG, "SQL_RETRIEVE_DATA, vParam = " FORMAT_LEN "\n",
                  vParam);
            if (conn)
                conn->stmtOptions.retrieve_data = (SQLUINTEGER)vParam;
            if (stmt)
                stmt->options.retrieve_data = (SQLUINTEGER)vParam;
            break;

        case SQL_ROWSET_SIZE:
            MYLOG(ES_DEBUG, "SQL_ROWSET_SIZE, vParam = " FORMAT_LEN "\n",
                  vParam);

            if (vParam < 1) {
                vParam = 1;
                changed = TRUE;
            }

            if (conn)
                conn->ardOptions.size_of_rowset_odbc2 = vParam;
            if (stmt)
                SC_get_ARDF(stmt)->size_of_rowset_odbc2 = vParam;
            break;

        case SQL_SIMULATE_CURSOR: /* NOT SUPPORTED */
            if (stmt) {
                SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                             "Simulated positioned update/delete not "
                             "supported.  Use the cursor library.",
                             func);
            }
            if (conn) {
                CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR,
                             "Simulated positioned update/delete not "
                             "supported.  Use the cursor library.",
                             func);
            }
            return SQL_ERROR;

        case SQL_USE_BOOKMARKS:
            if (stmt) {
                MYLOG(
                    ES_DEBUG, "USE_BOOKMARKS %s\n",
                    (vParam == SQL_UB_OFF)
                        ? "off"
                        : ((vParam == SQL_UB_VARIABLE) ? "variable" : "fixed"));
                setval = vParam;
                stmt->options.use_bookmarks = (SQLUINTEGER)setval;
            }
            if (conn)
                conn->stmtOptions.use_bookmarks = (SQLUINTEGER)vParam;
            break;

        case 1204: /* SQL_COPT_SS_PRESERVE_CURSORS ? */
            if (stmt) {
                SC_set_error(stmt, STMT_OPTION_NOT_FOR_THE_DRIVER,
                             "The option may be for MS SQL Server(Set)", func);
            } else if (conn) {
                CC_set_error(conn, CONN_OPTION_NOT_FOR_THE_DRIVER,
                             "The option may be for MS SQL Server(Set)", func);
            }
            return SQL_ERROR;
        case 1227: /* SQL_SOPT_SS_HIDDEN_COLUMNS ? */
        case 1228: /* SQL_SOPT_SS_NOBROWSETABLE ? */
            if (stmt) {
#ifndef NOT_USED
                if (0 != vParam)
                    changed = TRUE;
                break;
#else
                SC_set_error(stmt, STMT_OPTION_NOT_FOR_THE_DRIVER,
                             "The option may be for MS SQL Server(Set)", func);
#endif /* NOT_USED */
            } else if (conn) {
                CC_set_error(conn, CONN_OPTION_NOT_FOR_THE_DRIVER,
                             "The option may be for MS SQL Server(Set)", func);
            }
            return SQL_ERROR;
        default: {
            char option[64];

            if (stmt) {
                SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                             "Unknown statement option (Set)", func);
                SPRINTF_FIXED(option, "fOption=%d, vParam=" FORMAT_ULEN,
                              fOption, vParam);
                SC_log_error(func, option, stmt);
            }
            if (conn) {
                CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR,
                             "Unknown statement option (Set)", func);
                SPRINTF_FIXED(option, "fOption=%d, vParam=" FORMAT_ULEN,
                              fOption, vParam);
                CC_log_error(func, option, conn);
            }

            return SQL_ERROR;
        }
    }

    if (changed) {
        if (stmt) {
            SC_set_error(stmt, STMT_OPTION_VALUE_CHANGED,
                         "Requested value changed.", func);
        }
        if (conn) {
            CC_set_error(conn, CONN_OPTION_VALUE_CHANGED,
                         "Requested value changed.", func);
        }
        return SQL_SUCCESS_WITH_INFO;
    } else
        return SQL_SUCCESS;
}

/* Implements only SQL_AUTOCOMMIT */
RETCODE SQL_API ESAPI_SetConnectOption(HDBC hdbc, SQLUSMALLINT fOption,
                                       SQLULEN vParam) {
    CSTR func = "ESAPI_SetConnectOption";
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    char changed = FALSE;
    RETCODE retval;
    BOOL autocomm_on;

    MYLOG(ES_TRACE, "entering fOption = %d vParam = " FORMAT_LEN "\n", fOption,
          vParam);
    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    switch (fOption) {
        /*
         * Statement Options (apply to all stmts on the connection and
         * become defaults for new stmts)
         */
        case SQL_ASYNC_ENABLE:
        case SQL_BIND_TYPE:
        case SQL_CONCURRENCY:
        case SQL_CURSOR_TYPE:
        case SQL_KEYSET_SIZE:
        case SQL_MAX_LENGTH:
        case SQL_MAX_ROWS:
        case SQL_NOSCAN:
        case SQL_QUERY_TIMEOUT:
        case SQL_RETRIEVE_DATA:
        case SQL_ROWSET_SIZE:
        case SQL_SIMULATE_CURSOR:
        case SQL_USE_BOOKMARKS:
            /*
             * Become the default for all future statements on this
             * connection
             */
            retval = set_statement_option(conn, NULL, fOption, vParam);

            if (retval == SQL_SUCCESS_WITH_INFO)
                changed = TRUE;
            else if (retval == SQL_ERROR)
                return SQL_ERROR;

            break;

            /*
             * Connection Options
             */

        case SQL_ACCESS_MODE: /* ignored */
            break;

        case SQL_AUTOCOMMIT:
            switch (vParam) {
                case SQL_AUTOCOMMIT_ON:
                    autocomm_on = TRUE;
                    break;
                default:
                    CC_set_error(conn, CONN_INVALID_ARGUMENT_NO,
                                 "Illegal parameter value for SQL_AUTOCOMMIT. "
                                 "Turning SQL_AUTOCOMMIT off requires "
                                 "transactions, which are not supported.",
                                 func);
                    return SQL_ERROR;
            }
            if (autocomm_on && SQL_AUTOCOMMIT_OFF != conn->autocommit_public)
                break;
            else if (!autocomm_on
                     && SQL_AUTOCOMMIT_OFF == conn->autocommit_public)
                break;
            conn->autocommit_public =
                (autocomm_on ? SQL_AUTOCOMMIT_ON : SQL_AUTOCOMMIT_OFF);
            MYLOG(ES_DEBUG,
                  "AUTOCOMMIT: transact_status=%d, vparam=" FORMAT_LEN "\n",
                  conn->transact_status, vParam);

            CC_set_autocommit(conn, autocomm_on);
            break;

        case SQL_CURRENT_QUALIFIER: /* ignored */
            break;

        case SQL_LOGIN_TIMEOUT:
            conn->login_timeout = (SQLUINTEGER)vParam;
            break;

        case SQL_PACKET_SIZE: /* ignored */
            break;

        case SQL_QUIET_MODE: /* ignored */
            break;

        case SQL_TXN_ISOLATION:
            if (conn->isolation == vParam)
                break;
            /*
             * If the connection is not established, just record the setting to
             * reflect it upon connection.
             */
            if (CC_not_connected(conn)) {
                conn->isolation = (UInt4)vParam;
                break;
            }

            conn->isolation = (UInt4)vParam;
            break;

        /* These options should be handled by driver manager */
        case SQL_ODBC_CURSORS:
        case SQL_OPT_TRACE:
        case SQL_OPT_TRACEFILE:
        case SQL_TRANSLATE_DLL:
        case SQL_TRANSLATE_OPTION:
            CC_log_error(
                func,
                "This connect option (Set) is only used by the Driver Manager",
                conn);
            break;

        default: {
            char option[64];

            CC_set_error(conn, CONN_UNSUPPORTED_OPTION,
                         "Unknown connect option (Set)", func);
            SPRINTF_FIXED(option, "fOption=%d, vParam=" FORMAT_LEN, fOption,
                          vParam);
#ifdef WIN32
            if (fOption == 30002 && vParam) {
                int cmp;
#ifdef UNICODE_SUPPORT
                if (CC_is_in_unicode_driver(conn)) {
                    char *asPara =
                        ucs2_to_utf8((SQLWCHAR *)vParam, SQL_NTS, NULL, FALSE);
                    cmp = strcmp(asPara, "Microsoft Jet");
                    free(asPara);
                } else
#endif /* UNICODE_SUPPORT */
                    cmp = strncmp((char *)vParam, "Microsoft Jet", 13);
                if (0 == cmp) {
                    MYLOG(ES_DEBUG, "Microsoft Jet !!!!\n");
                    CC_set_errornumber(conn, 0);
                    conn->ms_jet = 1;
                    return SQL_SUCCESS;
                }
            }
#endif /* WIN32 */
            CC_log_error(func, option, conn);
            return SQL_ERROR;
        }
    }

    if (changed) {
        CC_set_error(conn, CONN_OPTION_VALUE_CHANGED,
                     "Requested value changed.", func);
        return SQL_SUCCESS_WITH_INFO;
    } else
        return SQL_SUCCESS;
}

/* This function just can tell you whether you are in Autcommit mode or not */
RETCODE SQL_API ESAPI_GetConnectOption(HDBC hdbc, SQLUSMALLINT fOption,
                                       PTR pvParam, SQLINTEGER *StringLength,
                                       SQLINTEGER BufferLength) {
    CSTR func = "ESAPI_GetConnectOption";
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    const char *p = NULL;
    SQLLEN len = sizeof(SQLINTEGER);
    SQLRETURN result = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");

    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    switch (fOption) {
        case SQL_ACCESS_MODE: /* NOT SUPPORTED */
            *((SQLUINTEGER *)pvParam) = SQL_MODE_READ_WRITE;
            break;

        case SQL_AUTOCOMMIT:
            *((SQLUINTEGER *)pvParam) = conn->autocommit_public;
            break;

        case SQL_CURRENT_QUALIFIER: /* don't use qualifiers */
            len = 0;
            p = CurrCatString(conn);
            break;

        case SQL_LOGIN_TIMEOUT:
            *((SQLUINTEGER *)pvParam) = conn->login_timeout;
            break;

        case SQL_PACKET_SIZE: /* NOT SUPPORTED */
            *((SQLUINTEGER *)pvParam) = 4096;
            break;

        case SQL_QUERY_TIMEOUT:
            *((SQLULEN *)pvParam) = conn->stmtOptions.stmt_timeout;
            break;

        case SQL_QUIET_MODE: /* NOT SUPPORTED */
            *((SQLULEN *)pvParam) = 0;
            break;

        case SQL_TXN_ISOLATION:
            if (conn->isolation == 0) {
                if (CC_not_connected(conn))
                    return SQL_NO_DATA;
                conn->isolation = CC_get_isolation(conn);
            }
            *((SQLUINTEGER *)pvParam) = conn->isolation;
            break;

#ifdef SQL_ATTR_CONNECTION_DEAD
        case SQL_ATTR_CONNECTION_DEAD:
#else
        case 1209:
#endif /* SQL_ATTR_CONNECTION_DEAD */
            MYLOG(ES_DEBUG, "CONNECTION_DEAD status=%d", conn->status);
            *((SQLUINTEGER *)pvParam) = CC_not_connected(conn);
            MYPRINTF(0, " val=" FORMAT_UINTEGER "\n",
                     *((SQLUINTEGER *)pvParam));
            break;

        case SQL_ATTR_ANSI_APP:
            *((SQLUINTEGER *)pvParam) = CC_is_in_ansi_app(conn);
            MYLOG(ES_DEBUG, "ANSI_APP val=" FORMAT_UINTEGER "\n",
                  *((SQLUINTEGER *)pvParam));
            break;

            /* These options should be handled by driver manager */
        case SQL_ODBC_CURSORS:
        case SQL_OPT_TRACE:
        case SQL_OPT_TRACEFILE:
        case SQL_TRANSLATE_DLL:
        case SQL_TRANSLATE_OPTION:
            CC_log_error(
                func,
                "This connect option (Get) is only used by the Driver Manager",
                conn);
            break;

        default: {
            char option[64];

            CC_set_error(conn, CONN_UNSUPPORTED_OPTION,
                         "Unknown connect option (Get)", func);
            SPRINTF_FIXED(option, "fOption=%d", fOption);
            CC_log_error(func, option, conn);
            return SQL_ERROR;
            break;
        }
    }

    if (NULL != p && 0 == len) {
        /* char/binary data */
        len = strlen(p);

        if (pvParam) {
#ifdef UNICODE_SUPPORT
            if (CC_is_in_unicode_driver(conn)) {
                len = utf8_to_ucs2(p, len, (SQLWCHAR *)pvParam,
                                   BufferLength / WCLEN);
                len *= WCLEN;
            } else
#endif /* UNICODE_SUPPORT */
                strncpy_null((char *)pvParam, p, (size_t)BufferLength);

            if (len >= BufferLength) {
                result = SQL_SUCCESS_WITH_INFO;
                CC_set_error(conn, CONN_TRUNCATED,
                             "The buffer was too small for the pvParam.", func);
            }
        }
    }
    if (StringLength)
        *StringLength = (SQLINTEGER)len;
    return result;
}

RETCODE SQL_API ESAPI_SetStmtOption(HSTMT hstmt, SQLUSMALLINT fOption,
                                    SQLULEN vParam) {
    CSTR func = "ESAPI_SetStmtOption";
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE retval;

    MYLOG(ES_DEBUG, " entering...\n");

    /*
     * Though we could fake Access out by just returning SQL_SUCCESS all
     * the time, but it tries to set a huge value for SQL_MAX_LENGTH and
     * expects the driver to reduce it to the real value.
     */
    if (!stmt) {
        SC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    retval = set_statement_option(NULL, stmt, fOption, vParam);
    return retval;
}

RETCODE SQL_API ESAPI_GetStmtOption(HSTMT hstmt, SQLUSMALLINT fOption,
                                    PTR pvParam, SQLINTEGER *StringLength,
                                    SQLINTEGER BufferLength) {
    UNUSED(BufferLength);
    CSTR func = "ESAPI_GetStmtOption";
    StatementClass *stmt = (StatementClass *)hstmt;
    QResultClass *res;
    SQLLEN ridx;
    SQLINTEGER len = sizeof(SQLINTEGER);
    Int4 bookmark;

    MYLOG(ES_TRACE, "entering...\n");

    /*
     * thought we could fake Access out by just returning SQL_SUCCESS all
     * the time, but it tries to set a huge value for SQL_MAX_LENGTH and
     * expects the driver to reduce it to the real value
     */
    if (!stmt) {
        SC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    switch (fOption) {
        case SQL_GET_BOOKMARK:
        case SQL_ROW_NUMBER:

            res = SC_get_Curres(stmt);
            if (!res) {
                SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                             "The cursor has no result.", func);
                return SQL_ERROR;
            }

            ridx = GIdx2CacheIdx(stmt->currTuple, stmt, res);
            if (!SC_is_fetchcursor(stmt)) {
                /* make sure we're positioned on a valid row */
                if ((ridx < 0)
                    || (((SQLULEN)ridx) >= QR_get_num_cached_tuples(res))) {
                    SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                                 "Not positioned on a valid row.", func);
                    return SQL_ERROR;
                }
            } else {
                if (stmt->currTuple < 0 || !res->tupleField) {
                    SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                                 "Not positioned on a valid row.", func);
                    return SQL_ERROR;
                }
            }

            if (fOption == SQL_GET_BOOKMARK
                && stmt->options.use_bookmarks == SQL_UB_OFF) {
                SC_set_error(
                    stmt, STMT_OPERATION_INVALID,
                    "Operation invalid because use bookmarks not enabled.",
                    func);
                return SQL_ERROR;
            }

            bookmark = (int)SC_make_int4_bookmark(stmt->currTuple);
            memcpy(pvParam, &bookmark, sizeof(UInt4));

            break;

        case SQL_ASYNC_ENABLE: /* NOT SUPPORTED */
            *((SQLINTEGER *)pvParam) = SQL_ASYNC_ENABLE_OFF;
            break;

        case SQL_BIND_TYPE:
            *((SQLINTEGER *)pvParam) = SC_get_ARDF(stmt)->bind_size;
            break;

        case SQL_CONCURRENCY: /* NOT REALLY SUPPORTED */
            MYLOG(ES_DEBUG, "SQL_CONCURRENCY " FORMAT_INTEGER "\n",
                  stmt->options.scroll_concurrency);
            *((SQLINTEGER *)pvParam) = stmt->options.scroll_concurrency;
            break;

        case SQL_CURSOR_TYPE: /* PARTIAL SUPPORT */
            MYLOG(ES_DEBUG, "SQL_CURSOR_TYPE " FORMAT_INTEGER "\n",
                  stmt->options.cursor_type);
            *((SQLINTEGER *)pvParam) = stmt->options.cursor_type;
            break;

        case SQL_KEYSET_SIZE: /* NOT SUPPORTED, but saved */
            MYLOG(ES_DEBUG, "SQL_KEYSET_SIZE\n");
            *((SQLLEN *)pvParam) = stmt->options.keyset_size;
            break;

        case SQL_MAX_LENGTH: /* NOT SUPPORTED, but saved */
            *((SQLLEN *)pvParam) = stmt->options.maxLength;
            break;

        case SQL_MAX_ROWS: /* NOT SUPPORTED, but saved */
            *((SQLLEN *)pvParam) = stmt->options.maxRows;
            MYLOG(ES_DEBUG, "MAX_ROWS, returning " FORMAT_LEN "\n",
                  stmt->options.maxRows);
            break;

        case SQL_NOSCAN: /* NOT SUPPORTED */
            *((SQLINTEGER *)pvParam) = SQL_NOSCAN_ON;
            break;

        case SQL_QUERY_TIMEOUT: /* NOT SUPPORTED */
            *((SQLULEN *)pvParam) = stmt->options.stmt_timeout;
            break;

        case SQL_RETRIEVE_DATA:
            *((SQLINTEGER *)pvParam) = stmt->options.retrieve_data;
            break;

        case SQL_ROWSET_SIZE:
            *((SQLLEN *)pvParam) = SC_get_ARDF(stmt)->size_of_rowset_odbc2;
            break;

        case SQL_SIMULATE_CURSOR: /* NOT SUPPORTED */
            *((SQLINTEGER *)pvParam) = SQL_SC_NON_UNIQUE;
            break;

        case SQL_USE_BOOKMARKS:
            *((SQLINTEGER *)pvParam) = stmt->options.use_bookmarks;
            break;
        case 1227: /* SQL_SOPT_SS_HIDDEN_COLUMNS ? */
        case 1228: /* SQL_SOPT_SS_NOBROWSETABLE ? */
            *((SQLINTEGER *)pvParam) = 0;
            break;

        default: {
            char option[64];

            SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                         "Unknown statement option (Get)", func);
            SPRINTF_FIXED(option, "fOption=%d", fOption);
            SC_log_error(func, option, stmt);
            return SQL_ERROR;
        }
    }
    if (StringLength)
        *StringLength = len;

    return SQL_SUCCESS;
}
