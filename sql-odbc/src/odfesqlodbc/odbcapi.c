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

#include <stdio.h>
#include <string.h>

#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_driver_connect.h"
#include "es_info.h"
#include "es_odbc.h"
#include "es_statement.h"
#include "loadlib.h"
#include "misc.h"
#include "qresult.h"
#include "statement.h"

BOOL SC_connection_lost_check(StatementClass *stmt, const char *funcname) {
    ConnectionClass *conn = SC_get_conn(stmt);
    char message[64];

    if (NULL != conn->esconn)
        return FALSE;
    SC_clear_error(stmt);
    SPRINTF_FIXED(message, "%s unable due to the connection lost", funcname);
    SC_set_error(stmt, STMT_COMMUNICATION_ERROR, message, funcname);
    return TRUE;
}

RETCODE SQL_API SQLBindCol(HSTMT StatementHandle, SQLUSMALLINT ColumnNumber,
                           SQLSMALLINT TargetType, PTR TargetValue,
                           SQLLEN BufferLength, SQLLEN *StrLen_or_Ind) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_BindCol(StatementHandle, ColumnNumber, TargetType, TargetValue,
                        BufferLength, StrLen_or_Ind);
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLCancel(HSTMT StatementHandle) {
    MYLOG(ES_TRACE, "entering\n");
    if (!StatementHandle)
        return SQL_INVALID_HANDLE;
    if (SC_connection_lost_check((StatementClass *)StatementHandle,
                                 __FUNCTION__))
        return SQL_ERROR;
    return ESAPI_Cancel(StatementHandle);
}

static BOOL theResultIsEmpty(const StatementClass *stmt) {
    QResultClass *res = SC_get_Result(stmt);
    if (NULL == res)
        return FALSE;
    return (0 == QR_get_num_total_tuples(res));
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLColumns(HSTMT StatementHandle, SQLCHAR *CatalogName,
                           SQLSMALLINT NameLength1, SQLCHAR *SchemaName,
                           SQLSMALLINT NameLength2, SQLCHAR *TableName,
                           SQLSMALLINT NameLength3, SQLCHAR *ColumnName,
                           SQLSMALLINT NameLength4) {
    CSTR func = "SQLColumns";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    SQLCHAR *ctName = CatalogName, *scName = SchemaName, *tbName = TableName,
            *clName = ColumnName;
    UWORD flag = PODBC_SEARCH_PUBLIC_SCHEMA;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Columns(StatementHandle, ctName, NameLength1, scName,
                            NameLength2, tbName, NameLength3, clName,
                            NameLength4, flag, 0, 0);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL, *newCl = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, CatalogName, NameLength1,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, SchemaName, NameLength2,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb =
                make_lstring_ifneeded(conn, TableName, NameLength3, ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (newCl = make_lstring_ifneeded(conn, ColumnName, NameLength4,
                                          ifallupper),
            NULL != newCl) {
            clName = newCl;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_Columns(StatementHandle, ctName, NameLength1, scName,
                                NameLength2, tbName, NameLength3, clName,
                                NameLength4, flag, 0, 0);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
            if (newCl)
                free(newCl);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLConnect(HDBC ConnectionHandle, SQLCHAR *ServerName,
                           SQLSMALLINT NameLength1, SQLCHAR *UserName,
                           SQLSMALLINT NameLength2, SQLCHAR *Authentication,
                           SQLSMALLINT NameLength3) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret = ESAPI_Connect(ConnectionHandle, ServerName, NameLength1, UserName,
                        NameLength2, Authentication, NameLength3);
    LEAVE_CONN_CS(conn);
    return ret;
}

RETCODE SQL_API SQLDriverConnect(HDBC hdbc, HWND hwnd, SQLCHAR *szConnStrIn,
                                 SQLSMALLINT cbConnStrIn, SQLCHAR *szConnStrOut,
                                 SQLSMALLINT cbConnStrOutMax,
                                 SQLSMALLINT *pcbConnStrOut,
                                 SQLUSMALLINT fDriverCompletion) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret =
        ESAPI_DriverConnect(hdbc, hwnd, szConnStrIn, cbConnStrIn, szConnStrOut,
                            cbConnStrOutMax, pcbConnStrOut, fDriverCompletion);
    LEAVE_CONN_CS(conn);
    return ret;
}
RETCODE SQL_API SQLBrowseConnect(HDBC hdbc, SQLCHAR *szConnStrIn,
                                 SQLSMALLINT cbConnStrIn, SQLCHAR *szConnStrOut,
                                 SQLSMALLINT cbConnStrOutMax,
                                 SQLSMALLINT *pcbConnStrOut) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret = ESAPI_BrowseConnect(hdbc, szConnStrIn, cbConnStrIn, szConnStrOut,
                              cbConnStrOutMax, pcbConnStrOut);
    LEAVE_CONN_CS(conn);
    return ret;
}

RETCODE SQL_API SQLDataSources(HENV EnvironmentHandle, SQLUSMALLINT Direction,
                               SQLCHAR *ServerName, SQLSMALLINT BufferLength1,
                               SQLSMALLINT *NameLength1, SQLCHAR *Description,
                               SQLSMALLINT BufferLength2,
                               SQLSMALLINT *NameLength2) {
    UNUSED(EnvironmentHandle, Direction, ServerName, BufferLength1, NameLength1,
           Description, BufferLength2, NameLength2);
    MYLOG(ES_TRACE, "entering\n");
    return SQL_ERROR;
}

RETCODE SQL_API SQLDescribeCol(HSTMT StatementHandle, SQLUSMALLINT ColumnNumber,
                               SQLCHAR *ColumnName, SQLSMALLINT BufferLength,
                               SQLSMALLINT *NameLength, SQLSMALLINT *DataType,
                               SQLULEN *ColumnSize, SQLSMALLINT *DecimalDigits,
                               SQLSMALLINT *Nullable) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_DescribeCol(StatementHandle, ColumnNumber, ColumnName,
                            BufferLength, NameLength, DataType, ColumnSize,
                            DecimalDigits, Nullable);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLDisconnect(HDBC ConnectionHandle) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    MYLOG(ES_TRACE, "entering for %p\n", ConnectionHandle);
#ifdef _HANDLE_ENLIST_IN_DTC_
    if (CC_is_in_global_trans(conn))
        CALL_DtcOnDisconnect(conn);
#endif /* _HANDLE_ENLIST_IN_DTC_ */
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret = ESAPI_Disconnect(ConnectionHandle);
    LEAVE_CONN_CS(conn);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLExecDirect(HSTMT StatementHandle, SQLCHAR *StatementText,
                              SQLINTEGER TextLength) {
    if(StatementHandle == NULL)
        return SQL_ERROR;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;
    
    // Enter critical
    ENTER_STMT_CS(stmt);

    // Clear error and rollback
    SC_clear_error(stmt);

    // Execute statement if statement is ready
    RETCODE ret = SQL_ERROR;
    if (!SC_opencheck(stmt, "SQLExecDirect"))
        ret = ESAPI_ExecDirect(StatementHandle, StatementText, TextLength, 1);

    // Exit critical
    LEAVE_STMT_CS(stmt);

    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLExecute(HSTMT StatementHandle) {
    if(StatementHandle == NULL)
        return SQL_ERROR;

    StatementClass *stmt = (StatementClass *)StatementHandle;
    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    // Enter critical
    ENTER_STMT_CS(stmt);

    // Clear error and rollback
    SC_clear_error(stmt);
    RETCODE ret = SQL_ERROR;
    if (!SC_opencheck(stmt, "SQLExecute"))
        ret = ESAPI_Execute(StatementHandle);

    // Exit critical
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLFetch(HSTMT StatementHandle) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    IRDFields *irdopts = SC_get_IRDF(stmt);
    ARDFields *ardopts = SC_get_ARDF(stmt);
    SQLUSMALLINT *rowStatusArray = irdopts->rowStatusArray;
    SQLULEN *pcRow = irdopts->rowsFetched;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_ExtendedFetch(StatementHandle, SQL_FETCH_NEXT, 0, pcRow,
                              rowStatusArray, 0, ardopts->size_of_rowset);
    stmt->transition_status = STMT_TRANSITION_FETCH_SCROLL;

    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLFreeStmt(HSTMT StatementHandle, SQLUSMALLINT Option) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    ConnectionClass *conn = NULL;

    MYLOG(ES_TRACE, "entering\n");

    if (stmt) {
        if (Option == SQL_DROP) {
            conn = stmt->hdbc;
            if (conn)
                ENTER_CONN_CS(conn);
        } else
            ENTER_STMT_CS(stmt);
    }

    ret = ESAPI_FreeStmt(StatementHandle, Option);

    if (stmt) {
        if (Option == SQL_DROP) {
            if (conn)
                LEAVE_CONN_CS(conn);
        } else
            LEAVE_STMT_CS(stmt);
    }

    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLGetCursorName(HSTMT StatementHandle, SQLCHAR *CursorName,
                                 SQLSMALLINT BufferLength,
                                 SQLSMALLINT *NameLength) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_GetCursorName(StatementHandle, CursorName, BufferLength,
                              NameLength);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLGetData(HSTMT StatementHandle, SQLUSMALLINT ColumnNumber,
                           SQLSMALLINT TargetType, PTR TargetValue,
                           SQLLEN BufferLength, SQLLEN *StrLen_or_Ind) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_GetData(StatementHandle, ColumnNumber, TargetType, TargetValue,
                        BufferLength, StrLen_or_Ind);
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLGetFunctions(HDBC ConnectionHandle, SQLUSMALLINT FunctionId,
                                SQLUSMALLINT *Supported) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    if (FunctionId == SQL_API_ODBC3_ALL_FUNCTIONS)
        ret = ESAPI_GetFunctions30(ConnectionHandle, FunctionId, Supported);
    else
        ret = ESAPI_GetFunctions(ConnectionHandle, FunctionId, Supported);

    LEAVE_CONN_CS(conn);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLGetInfo(HDBC ConnectionHandle, SQLUSMALLINT InfoType,
                           PTR InfoValue, SQLSMALLINT BufferLength,
                           SQLSMALLINT *StringLength) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    MYLOG(ES_TRACE, "entering\n");
    if ((ret = ESAPI_GetInfo(ConnectionHandle, InfoType, InfoValue,
                             BufferLength, StringLength))
        == SQL_ERROR)
        CC_log_error("SQLGetInfo(30)", "", conn);
    LEAVE_CONN_CS(conn);
    return ret;
}

RETCODE SQL_API SQLGetTypeInfo(HSTMT StatementHandle, SQLSMALLINT DataType) {
    CSTR func = "SQLGetTypeInfo";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check((StatementClass *)StatementHandle,
                                 __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_GetTypeInfo(StatementHandle, DataType);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLNumResultCols(HSTMT StatementHandle,
                                 SQLSMALLINT *ColumnCount) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_NumResultCols(StatementHandle, ColumnCount);
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLParamData(HSTMT StatementHandle, PTR *Value) {
    UNUSED(Value);
    StatementClass *stmt = (StatementClass *)StatementHandle;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.", "SQLParamData");
    return SQL_ERROR;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLPrepare(HSTMT StatementHandle, SQLCHAR *StatementText,
                           SQLINTEGER TextLength) {
    if(StatementHandle == NULL)
        return SQL_ERROR;

    CSTR func = "SQLPrepare";
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    // Enter critical
    ENTER_STMT_CS(stmt);

    // Clear error and rollback
    SC_clear_error(stmt);

    // Prepare statement if statement is ready
    RETCODE ret = SQL_ERROR;
    if (!SC_opencheck(stmt, func))
        ret = ESAPI_Prepare(StatementHandle, StatementText, TextLength);

    // Exit critical
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLPutData(HSTMT StatementHandle, PTR Data,
                           SQLLEN StrLen_or_Ind) {
    UNUSED(Data, StrLen_or_Ind);
    StatementClass *stmt = (StatementClass *)StatementHandle;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.", "SQLPutData");
    return SQL_ERROR;
}

RETCODE SQL_API SQLRowCount(HSTMT StatementHandle, SQLLEN *RowCount) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_RowCount(StatementHandle, RowCount);
    LEAVE_STMT_CS(stmt);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLSetCursorName(HSTMT StatementHandle, SQLCHAR *CursorName,
                                 SQLSMALLINT NameLength) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_SetCursorName(StatementHandle, CursorName, NameLength);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLSetParam(HSTMT StatementHandle, SQLUSMALLINT ParameterNumber,
                            SQLSMALLINT ValueType, SQLSMALLINT ParameterType,
                            SQLULEN LengthPrecision, SQLSMALLINT ParameterScale,
                            PTR ParameterValue, SQLLEN *StrLen_or_Ind) {
    UNUSED(ParameterNumber, ValueType, ParameterType, LengthPrecision,
           ParameterScale, ParameterValue, StrLen_or_Ind);
    StatementClass *stmt = (StatementClass *)StatementHandle;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.", "SQLSetParam");
    return SQL_ERROR;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLSpecialColumns(HSTMT StatementHandle,
                                  SQLUSMALLINT IdentifierType,
                                  SQLCHAR *CatalogName, SQLSMALLINT NameLength1,
                                  SQLCHAR *SchemaName, SQLSMALLINT NameLength2,
                                  SQLCHAR *TableName, SQLSMALLINT NameLength3,
                                  SQLUSMALLINT Scope, SQLUSMALLINT Nullable) {
    CSTR func = "SQLSpecialColumns";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    SQLCHAR *ctName = CatalogName, *scName = SchemaName, *tbName = TableName;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_SpecialColumns(StatementHandle, IdentifierType, ctName,
                                   NameLength1, scName, NameLength2, tbName,
                                   NameLength3, Scope, Nullable);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, CatalogName, NameLength1,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, SchemaName, NameLength2,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb =
                make_lstring_ifneeded(conn, TableName, NameLength3, ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_SpecialColumns(StatementHandle, IdentifierType, ctName,
                                       NameLength1, scName, NameLength2, tbName,
                                       NameLength3, Scope, Nullable);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLStatistics(HSTMT StatementHandle, SQLCHAR *CatalogName,
                              SQLSMALLINT NameLength1, SQLCHAR *SchemaName,
                              SQLSMALLINT NameLength2, SQLCHAR *TableName,
                              SQLSMALLINT NameLength3, SQLUSMALLINT Unique,
                              SQLUSMALLINT Reserved) {
    CSTR func = "SQLStatistics";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    SQLCHAR *ctName = CatalogName, *scName = SchemaName, *tbName = TableName;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Statistics(StatementHandle, ctName, NameLength1, scName,
                               NameLength2, tbName, NameLength3, Unique,
                               Reserved);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, CatalogName, NameLength1,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, SchemaName, NameLength2,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb =
                make_lstring_ifneeded(conn, TableName, NameLength3, ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_Statistics(StatementHandle, ctName, NameLength1, scName,
                                   NameLength2, tbName, NameLength3, Unique,
                                   Reserved);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLTables(HSTMT StatementHandle, SQLCHAR *CatalogName,
                          SQLSMALLINT NameLength1, SQLCHAR *SchemaName,
                          SQLSMALLINT NameLength2, SQLCHAR *TableName,
                          SQLSMALLINT NameLength3, SQLCHAR *TableType,
                          SQLSMALLINT NameLength4) {
    CSTR func = "SQLTables";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    SQLCHAR *ctName = CatalogName, *scName = SchemaName, *tbName = TableName;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Tables(StatementHandle, ctName, NameLength1, scName,
                           NameLength2, tbName, NameLength3, TableType,
                           NameLength4, flag);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, CatalogName, NameLength1,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, SchemaName, NameLength2,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb =
                make_lstring_ifneeded(conn, TableName, NameLength3, ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_Tables(StatementHandle, ctName, NameLength1, scName,
                               NameLength2, tbName, NameLength3, TableType,
                               NameLength4, flag);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLColumnPrivileges(
    HSTMT hstmt, SQLCHAR *szCatalogName, SQLSMALLINT cbCatalogName,
    SQLCHAR *szSchemaName, SQLSMALLINT cbSchemaName, SQLCHAR *szTableName,
    SQLSMALLINT cbTableName, SQLCHAR *szColumnName, SQLSMALLINT cbColumnName) {
    CSTR func = "SQLColumnPrivileges";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *ctName = szCatalogName, *scName = szSchemaName,
            *tbName = szTableName, *clName = szColumnName;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ColumnPrivileges(hstmt, ctName, cbCatalogName, scName,
                                     cbSchemaName, tbName, cbTableName, clName,
                                     cbColumnName, flag);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL, *newCl = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, szCatalogName, cbCatalogName,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, szSchemaName, cbSchemaName,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb = make_lstring_ifneeded(conn, szTableName, cbTableName,
                                          ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (newCl = make_lstring_ifneeded(conn, szColumnName, cbColumnName,
                                          ifallupper),
            NULL != newCl) {
            clName = newCl;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_ColumnPrivileges(hstmt, ctName, cbCatalogName, scName,
                                         cbSchemaName, tbName, cbTableName,
                                         clName, cbColumnName, flag);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
            if (newCl)
                free(newCl);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLDescribeParam(HSTMT hstmt, SQLUSMALLINT ipar,
                                 SQLSMALLINT *pfSqlType, SQLULEN *pcbParamDef,
                                 SQLSMALLINT *pibScale,
                                 SQLSMALLINT *pfNullable) {
    UNUSED(ipar, pfSqlType, pcbParamDef, pibScale, pfNullable);
    StatementClass *stmt = (StatementClass *)hstmt;
    SC_clear_error(stmt);

    // COLNUM_ERROR translates to 'invalid descriptor index'
    SC_set_error(stmt, STMT_COLNUM_ERROR,
                 "Elasticsearch does not support parameters.", "SQLNumParams");
    return SQL_ERROR;
}

RETCODE SQL_API SQLExtendedFetch(HSTMT hstmt, SQLUSMALLINT fFetchType,
                                 SQLLEN irow,
#if defined(WITH_UNIXODBC) && (SIZEOF_LONG_INT != 8)
                                 SQLROWSETSIZE *pcrow,
#else
                                 SQLULEN *pcrow,
#endif /* WITH_UNIXODBC */
                                 SQLUSMALLINT *rgfRowStatus) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
#ifdef WITH_UNIXODBC
    {
        SQLULEN retrieved;

        ret = ESAPI_ExtendedFetch(hstmt, fFetchType, irow, &retrieved,
                                  rgfRowStatus, 0,
                                  SC_get_ARDF(stmt)->size_of_rowset_odbc2);
        if (pcrow)
            *pcrow = retrieved;
    }
#else
    ret = ESAPI_ExtendedFetch(hstmt, fFetchType, irow, pcrow, rgfRowStatus, 0,
                              SC_get_ARDF(stmt)->size_of_rowset_odbc2);
#endif /* WITH_UNIXODBC */
    stmt->transition_status = STMT_TRANSITION_EXTENDED_FETCH;
    LEAVE_STMT_CS(stmt);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLForeignKeys(
    HSTMT hstmt, SQLCHAR *szPkCatalogName, SQLSMALLINT cbPkCatalogName,
    SQLCHAR *szPkSchemaName, SQLSMALLINT cbPkSchemaName, SQLCHAR *szPkTableName,
    SQLSMALLINT cbPkTableName, SQLCHAR *szFkCatalogName,
    SQLSMALLINT cbFkCatalogName, SQLCHAR *szFkSchemaName,
    SQLSMALLINT cbFkSchemaName, SQLCHAR *szFkTableName,
    SQLSMALLINT cbFkTableName) {
    CSTR func = "SQLForeignKeys";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *pkctName = szPkCatalogName, *pkscName = szPkSchemaName,
            *pktbName = szPkTableName, *fkctName = szFkCatalogName,
            *fkscName = szFkSchemaName, *fktbName = szFkTableName;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ForeignKeys(hstmt, pkctName, cbPkCatalogName, pkscName,
                                cbPkSchemaName, pktbName, cbPkTableName,
                                fkctName, cbFkCatalogName, fkscName,
                                cbFkSchemaName, fktbName, cbFkTableName);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newPkct = NULL, *newPksc = NULL, *newPktb = NULL,
                *newFkct = NULL, *newFksc = NULL, *newFktb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newPkct = make_lstring_ifneeded(conn, szPkCatalogName,
                                            cbPkCatalogName, ifallupper),
            NULL != newPkct) {
            pkctName = newPkct;
            reexec = TRUE;
        }
        if (newPksc = make_lstring_ifneeded(conn, szPkSchemaName,
                                            cbPkSchemaName, ifallupper),
            NULL != newPksc) {
            pkscName = newPksc;
            reexec = TRUE;
        }
        if (newPktb = make_lstring_ifneeded(conn, szPkTableName, cbPkTableName,
                                            ifallupper),
            NULL != newPktb) {
            pktbName = newPktb;
            reexec = TRUE;
        }
        if (newFkct = make_lstring_ifneeded(conn, szFkCatalogName,
                                            cbFkCatalogName, ifallupper),
            NULL != newFkct) {
            fkctName = newFkct;
            reexec = TRUE;
        }
        if (newFksc = make_lstring_ifneeded(conn, szFkSchemaName,
                                            cbFkSchemaName, ifallupper),
            NULL != newFksc) {
            fkscName = newFksc;
            reexec = TRUE;
        }
        if (newFktb = make_lstring_ifneeded(conn, szFkTableName, cbFkTableName,
                                            ifallupper),
            NULL != newFktb) {
            fktbName = newFktb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_ForeignKeys(hstmt, pkctName, cbPkCatalogName, pkscName,
                                    cbPkSchemaName, pktbName, cbPkTableName,
                                    fkctName, cbFkCatalogName, fkscName,
                                    cbFkSchemaName, fktbName, cbFkTableName);
            if (newPkct)
                free(newPkct);
            if (newPksc)
                free(newPksc);
            if (newPktb)
                free(newPktb);
            if (newFkct)
                free(newFkct);
            if (newFksc)
                free(newFksc);
            if (newFktb)
                free(newFktb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLMoreResults(HSTMT hstmt) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_MoreResults(hstmt);
    LEAVE_STMT_CS(stmt);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLNativeSql(HDBC hdbc, SQLCHAR *szSqlStrIn,
                             SQLINTEGER cbSqlStrIn, SQLCHAR *szSqlStr,
                             SQLINTEGER cbSqlStrMax, SQLINTEGER *pcbSqlStr) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret = ESAPI_NativeSql(hdbc, szSqlStrIn, cbSqlStrIn, szSqlStr, cbSqlStrMax,
                          pcbSqlStr);
    LEAVE_CONN_CS(conn);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLNumParams(HSTMT hstmt, SQLSMALLINT *pcpar) {
    if (pcpar != NULL)
        *pcpar = 0;

    StatementClass *stmt = (StatementClass *)hstmt;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.", "SQLNumParams");
    return SQL_SUCCESS_WITH_INFO;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLPrimaryKeys(HSTMT hstmt, SQLCHAR *szCatalogName,
                               SQLSMALLINT cbCatalogName, SQLCHAR *szSchemaName,
                               SQLSMALLINT cbSchemaName, SQLCHAR *szTableName,
                               SQLSMALLINT cbTableName) {
    CSTR func = "SQLPrimaryKeys";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *ctName = szCatalogName, *scName = szSchemaName,
            *tbName = szTableName;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_PrimaryKeys(hstmt, ctName, cbCatalogName, scName,
                                cbSchemaName, tbName, cbTableName, 0);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, szCatalogName, cbCatalogName,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, szSchemaName, cbSchemaName,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb = make_lstring_ifneeded(conn, szTableName, cbTableName,
                                          ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_PrimaryKeys(hstmt, ctName, cbCatalogName, scName,
                                    cbSchemaName, tbName, cbTableName, 0);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLProcedureColumns(
    HSTMT hstmt, SQLCHAR *szCatalogName, SQLSMALLINT cbCatalogName,
    SQLCHAR *szSchemaName, SQLSMALLINT cbSchemaName, SQLCHAR *szProcName,
    SQLSMALLINT cbProcName, SQLCHAR *szColumnName, SQLSMALLINT cbColumnName) {
    CSTR func = "SQLProcedureColumns";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *ctName = szCatalogName, *scName = szSchemaName,
            *prName = szProcName, *clName = szColumnName;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ProcedureColumns(hstmt, ctName, cbCatalogName, scName,
                                     cbSchemaName, prName, cbProcName, clName,
                                     cbColumnName, flag);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newPr = NULL, *newCl = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, szCatalogName, cbCatalogName,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, szSchemaName, cbSchemaName,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newPr =
                make_lstring_ifneeded(conn, szProcName, cbProcName, ifallupper),
            NULL != newPr) {
            prName = newPr;
            reexec = TRUE;
        }
        if (newCl = make_lstring_ifneeded(conn, szColumnName, cbColumnName,
                                          ifallupper),
            NULL != newCl) {
            clName = newCl;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_ProcedureColumns(hstmt, ctName, cbCatalogName, scName,
                                         cbSchemaName, prName, cbProcName,
                                         clName, cbColumnName, flag);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newPr)
                free(newPr);
            if (newCl)
                free(newCl);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}

RETCODE SQL_API SQLProcedures(HSTMT hstmt, SQLCHAR *szCatalogName,
                              SQLSMALLINT cbCatalogName, SQLCHAR *szSchemaName,
                              SQLSMALLINT cbSchemaName, SQLCHAR *szProcName,
                              SQLSMALLINT cbProcName) {
    CSTR func = "SQLProcedures";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *ctName = szCatalogName, *scName = szSchemaName,
            *prName = szProcName;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Procedures(hstmt, ctName, cbCatalogName, scName,
                               cbSchemaName, prName, cbProcName, flag);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newPr = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, szCatalogName, cbCatalogName,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, szSchemaName, cbSchemaName,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newPr =
                make_lstring_ifneeded(conn, szProcName, cbProcName, ifallupper),
            NULL != newPr) {
            prName = newPr;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_Procedures(hstmt, ctName, cbCatalogName, scName,
                                   cbSchemaName, prName, cbProcName, flag);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newPr)
                free(newPr);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLSetPos(HSTMT hstmt, SQLSETPOSIROW irow, SQLUSMALLINT fOption,
                          SQLUSMALLINT fLock) {
    UNUSED(irow, fOption, fLock);
    StatementClass *stmt = (StatementClass *)hstmt;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "SQLSetPos is not supported.", "SQLSetPos");
    return SQL_ERROR;
}

#ifndef UNICODE_SUPPORTXX
RETCODE SQL_API SQLTablePrivileges(HSTMT hstmt, SQLCHAR *szCatalogName,
                                   SQLSMALLINT cbCatalogName,
                                   SQLCHAR *szSchemaName,
                                   SQLSMALLINT cbSchemaName,
                                   SQLCHAR *szTableName,
                                   SQLSMALLINT cbTableName) {
    CSTR func = "SQLTablePrivileges";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)hstmt;
    SQLCHAR *ctName = szCatalogName, *scName = szSchemaName,
            *tbName = szTableName;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_TablePrivileges(hstmt, ctName, cbCatalogName, scName,
                                    cbSchemaName, tbName, cbTableName, flag);
    if (SQL_SUCCESS == ret && theResultIsEmpty(stmt)) {
        BOOL ifallupper = TRUE, reexec = FALSE;
        SQLCHAR *newCt = NULL, *newSc = NULL, *newTb = NULL;
        ConnectionClass *conn = SC_get_conn(stmt);

        if (newCt = make_lstring_ifneeded(conn, szCatalogName, cbCatalogName,
                                          ifallupper),
            NULL != newCt) {
            ctName = newCt;
            reexec = TRUE;
        }
        if (newSc = make_lstring_ifneeded(conn, szSchemaName, cbSchemaName,
                                          ifallupper),
            NULL != newSc) {
            scName = newSc;
            reexec = TRUE;
        }
        if (newTb = make_lstring_ifneeded(conn, szTableName, cbTableName,
                                          ifallupper),
            NULL != newTb) {
            tbName = newTb;
            reexec = TRUE;
        }
        if (reexec) {
            ret = ESAPI_TablePrivileges(hstmt, ctName, cbCatalogName, scName,
                                        cbSchemaName, tbName, cbTableName, 0);
            if (newCt)
                free(newCt);
            if (newSc)
                free(newSc);
            if (newTb)
                free(newTb);
        }
    }
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

RETCODE SQL_API SQLBindParameter(HSTMT hstmt, SQLUSMALLINT ipar,
                                 SQLSMALLINT fParamType, SQLSMALLINT fCType,
                                 SQLSMALLINT fSqlType, SQLULEN cbColDef,
                                 SQLSMALLINT ibScale, PTR rgbValue,
                                 SQLLEN cbValueMax, SQLLEN *pcbValue) {
    UNUSED(ipar, fParamType, fCType, fSqlType, cbColDef, ibScale, rgbValue,
           cbValueMax, pcbValue);
    StatementClass *stmt = (StatementClass *)hstmt;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.",
                 "SQLBindParameter");
    return SQL_ERROR;
}
