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

#include "es_apifunc.h"
#include "es_connection.h"
#include "es_driver_connect.h"
#include "es_info.h"
#include "es_odbc.h"
#include "statement.h"
#include "unicode_support.h"

RETCODE SQL_API SQLColumnsW(HSTMT StatementHandle, SQLWCHAR *CatalogName,
                            SQLSMALLINT NameLength1, SQLWCHAR *SchemaName,
                            SQLSMALLINT NameLength2, SQLWCHAR *TableName,
                            SQLSMALLINT NameLength3, SQLWCHAR *ColumnName,
                            SQLSMALLINT NameLength4) {
    CSTR func = "SQLColumnsW";
    RETCODE ret;
    char *ctName, *scName, *tbName, *clName;
    SQLLEN nmlen1, nmlen2, nmlen3, nmlen4;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = PODBC_SEARCH_PUBLIC_SCHEMA;
    ConnInfo *ci;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    ci = &(conn->connInfo);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(CatalogName, NameLength1, &nmlen1, lower_id);
    scName = ucs2_to_utf8(SchemaName, NameLength2, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(TableName, NameLength3, &nmlen3, lower_id);
    clName = ucs2_to_utf8(ColumnName, NameLength4, &nmlen4, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Columns(StatementHandle, (SQLCHAR *)ctName,
                            (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
                            (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName,
                            (SQLSMALLINT)nmlen3, (SQLCHAR *)clName,
                            (SQLSMALLINT)nmlen4, flag, 0, 0);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    if (clName)
        free(clName);
    return ret;
}

RETCODE SQL_API SQLConnectW(HDBC ConnectionHandle, SQLWCHAR *ServerName,
                            SQLSMALLINT NameLength1, SQLWCHAR *UserName,
                            SQLSMALLINT NameLength2, SQLWCHAR *Authentication,
                            SQLSMALLINT NameLength3) {
    char *svName, *usName, *auth;
    SQLLEN nmlen1, nmlen2, nmlen3;
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    CC_set_in_unicode_driver(conn);
    svName = ucs2_to_utf8(ServerName, NameLength1, &nmlen1, FALSE);
    usName = ucs2_to_utf8(UserName, NameLength2, &nmlen2, FALSE);
    auth = ucs2_to_utf8(Authentication, NameLength3, &nmlen3, FALSE);
    ret =
        ESAPI_Connect(ConnectionHandle, (SQLCHAR *)svName, (SQLSMALLINT)nmlen1,
                      (SQLCHAR *)usName, (SQLSMALLINT)nmlen2, (SQLCHAR *)auth,
                      (SQLSMALLINT)nmlen3);
    LEAVE_CONN_CS(conn);
    if (svName)
        free(svName);
    if (usName)
        free(usName);
    if (auth)
        free(auth);
    return ret;
}

RETCODE SQL_API SQLDriverConnectW(HDBC hdbc, HWND hwnd, SQLWCHAR *szConnStrIn,
                                  SQLSMALLINT cbConnStrIn,
                                  SQLWCHAR *szConnStrOut,
                                  SQLSMALLINT cbConnStrOutMax,
                                  SQLSMALLINT *pcbConnStrOut,
                                  SQLUSMALLINT fDriverCompletion) {
    CSTR func = "SQLDriverConnectW";
    char *szIn, *szOut = NULL;
    SQLSMALLINT maxlen, obuflen = 0;
    SQLLEN inlen;
    SQLSMALLINT olen, *pCSO;
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    CC_set_in_unicode_driver(conn);
    szIn = ucs2_to_utf8(szConnStrIn, cbConnStrIn, &inlen, FALSE);
    maxlen = cbConnStrOutMax;
    pCSO = NULL;
    olen = 0;
    if (maxlen > 0) {
        obuflen = maxlen + 1;
        szOut = malloc(obuflen);
        if (!szOut) {
            CC_set_error(conn, CONN_NO_MEMORY_ERROR,
                         "Could not allocate memory for output buffer", func);
            ret = SQL_ERROR;
            goto cleanup;
        }
        pCSO = &olen;
    } else if (pcbConnStrOut)
        pCSO = &olen;
    ret =
        ESAPI_DriverConnect(hdbc, hwnd, (SQLCHAR *)szIn, (SQLSMALLINT)inlen,
                            (SQLCHAR *)szOut, maxlen, pCSO, fDriverCompletion);
    if (ret != SQL_ERROR && NULL != pCSO) {
        SQLLEN outlen = olen;

        if (olen < obuflen)
            outlen = utf8_to_ucs2(szOut, olen, szConnStrOut, cbConnStrOutMax);
        else
            utf8_to_ucs2(szOut, maxlen, szConnStrOut, cbConnStrOutMax);
        if (outlen >= cbConnStrOutMax && NULL != szConnStrOut
            && NULL != pcbConnStrOut) {
            MYLOG(ES_ALL, "cbConnstrOutMax=%d pcb=%p\n",
                  cbConnStrOutMax, pcbConnStrOut);
            if (SQL_SUCCESS == ret) {
                CC_set_error(conn, CONN_TRUNCATED,
                             "the ConnStrOut is too small", func);
                ret = SQL_SUCCESS_WITH_INFO;
            }
        }
        if (pcbConnStrOut)
            *pcbConnStrOut = (SQLSMALLINT)outlen;
    }
cleanup:
    LEAVE_CONN_CS(conn);
    if (szOut)
        free(szOut);
    if (szIn)
        free(szIn);
    return ret;
}
RETCODE SQL_API SQLBrowseConnectW(HDBC hdbc, SQLWCHAR *szConnStrIn,
                                  SQLSMALLINT cbConnStrIn,
                                  SQLWCHAR *szConnStrOut,
                                  SQLSMALLINT cbConnStrOutMax,
                                  SQLSMALLINT *pcbConnStrOut) {
    CSTR func = "SQLBrowseConnectW";
    char *szIn, *szOut;
    SQLLEN inlen;
    SQLUSMALLINT obuflen;
    SQLSMALLINT olen = 0;
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    CC_set_in_unicode_driver(conn);
    szIn = ucs2_to_utf8(szConnStrIn, cbConnStrIn, &inlen, FALSE);
    obuflen = cbConnStrOutMax + 1;
    szOut = malloc(obuflen);
    if (szOut)
        ret = ESAPI_BrowseConnect(hdbc, (SQLCHAR *)szIn, (SQLSMALLINT)inlen,
                                  (SQLCHAR *)szOut, cbConnStrOutMax, &olen);
    else {
        CC_set_error(conn, CONN_NO_MEMORY_ERROR,
                     "Could not allocate memory for output buffer", func);
        ret = SQL_ERROR;
    }
    LEAVE_CONN_CS(conn);
    if (ret != SQL_ERROR) {
        SQLLEN outlen =
            utf8_to_ucs2(szOut, olen, szConnStrOut, cbConnStrOutMax);
        if (pcbConnStrOut)
            *pcbConnStrOut = (SQLSMALLINT)outlen;
    }
    free(szOut);
    if (szIn)
        free(szIn);
    return ret;
}

RETCODE SQL_API SQLDataSourcesW(HENV EnvironmentHandle, SQLUSMALLINT Direction,
                                SQLWCHAR *ServerName, SQLSMALLINT BufferLength1,
                                SQLSMALLINT *NameLength1, SQLWCHAR *Description,
                                SQLSMALLINT BufferLength2,
                                SQLSMALLINT *NameLength2) {
    UNUSED(EnvironmentHandle, Direction, ServerName, BufferLength1, NameLength1,
           Description, BufferLength2, NameLength2);
    MYLOG(ES_TRACE, "entering\n");
    return SQL_ERROR;
}

RETCODE SQL_API SQLDescribeColW(HSTMT StatementHandle,
                                SQLUSMALLINT ColumnNumber, SQLWCHAR *ColumnName,
                                SQLSMALLINT BufferLength,
                                SQLSMALLINT *NameLength, SQLSMALLINT *DataType,
                                SQLULEN *ColumnSize, SQLSMALLINT *DecimalDigits,
                                SQLSMALLINT *Nullable) {
    CSTR func = "SQLDescribeColW";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    SQLSMALLINT buflen, nmlen = 0;
    char *clName = NULL, *clNamet = NULL;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    buflen = 0;
    if (BufferLength > 0)
        buflen = BufferLength * 3;
    else if (NameLength)
        buflen = 32;
    if (buflen > 0)
        clNamet = malloc(buflen);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    for (;; buflen = nmlen + 1, clNamet = realloc(clName, buflen)) {
        if (!clNamet) {
            SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                         "Could not allocate memory for column name", func);
            ret = SQL_ERROR;
            break;
        }
        clName = clNamet;
        ret = ESAPI_DescribeCol(StatementHandle, ColumnNumber,
                                (SQLCHAR *)clName, buflen, &nmlen, DataType,
                                ColumnSize, DecimalDigits, Nullable);
        if (SQL_SUCCESS_WITH_INFO != ret || nmlen < buflen)
            break;
    }
    if (SQL_SUCCEEDED(ret)) {
        SQLLEN nmcount = nmlen;

        if (nmlen < buflen)
            nmcount = utf8_to_ucs2(clName, nmlen, ColumnName, BufferLength);
        if (SQL_SUCCESS == ret && BufferLength > 0 && nmcount > BufferLength) {
            ret = SQL_SUCCESS_WITH_INFO;
            SC_set_error(stmt, STMT_TRUNCATED, "Column name too large", func);
        }
        if (NameLength)
            *NameLength = (SQLSMALLINT)nmcount;
    }
    LEAVE_STMT_CS(stmt);
    if (clName)
        free(clName);
    return ret;
}

RETCODE SQL_API SQLExecDirectW(HSTMT StatementHandle, SQLWCHAR *StatementText,
                               SQLINTEGER TextLength) {
    if(StatementHandle == NULL)
        return SQL_ERROR;

    StatementClass *stmt = (StatementClass *)StatementHandle;
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    // Get query string
    SQLLEN slen = 0;
    char *stxt = ucs2_to_utf8(StatementText, TextLength, &slen, FALSE);

    // Enter critical
    ENTER_STMT_CS(stmt);

    // Clear error and rollback
    SC_clear_error(stmt);

    // Execute statement if statement is ready
    RETCODE ret = SQL_ERROR;
    if (!SC_opencheck(stmt, "SQLExecDirectW"))
        ret = ESAPI_ExecDirect(StatementHandle, (const SQLCHAR *)stxt, (SQLINTEGER)slen, 1);

    // Exit critical
    LEAVE_STMT_CS(stmt);

    if (stxt)
        free(stxt);
    return ret;
}

RETCODE SQL_API SQLGetCursorNameW(HSTMT StatementHandle, SQLWCHAR *CursorName,
                                  SQLSMALLINT BufferLength,
                                  SQLSMALLINT *NameLength) {
    CSTR func = "SQLGetCursorNameW";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    char *crName = NULL, *crNamet;
    SQLSMALLINT clen = 0, buflen;

    MYLOG(ES_TRACE, "entering\n");
    if (BufferLength > 0)
        buflen = BufferLength * 3;
    else
        buflen = 32;
    crNamet = malloc(buflen);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    for (;; buflen = clen + 1, crNamet = realloc(crName, buflen)) {
        if (!crNamet) {
            SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                         "Could not allocate memory for cursor name", func);
            ret = SQL_ERROR;
            break;
        }
        crName = crNamet;
        ret = ESAPI_GetCursorName(StatementHandle, (SQLCHAR *)crName, buflen,
                                  &clen);
        if (SQL_SUCCESS_WITH_INFO != ret || clen < buflen)
            break;
    }
    if (SQL_SUCCEEDED(ret)) {
        SQLLEN nmcount = clen;

        if (clen < buflen)
            nmcount = utf8_to_ucs2(crName, clen, CursorName, BufferLength);
        if (SQL_SUCCESS == ret && nmcount > BufferLength) {
            ret = SQL_SUCCESS_WITH_INFO;
            SC_set_error(stmt, STMT_TRUNCATED, "Cursor name too large", func);
        }
        if (NameLength)
            *NameLength = (SQLSMALLINT)nmcount;
    }
    LEAVE_STMT_CS(stmt);
    free(crName);
    return ret;
}

RETCODE SQL_API SQLGetInfoW(HDBC ConnectionHandle, SQLUSMALLINT InfoType,
                            PTR InfoValue, SQLSMALLINT BufferLength,
                            SQLSMALLINT *StringLength) {
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;
    RETCODE ret;

    ENTER_CONN_CS(conn);
    CC_set_in_unicode_driver(conn);
    CC_clear_error(conn);
    MYLOG(ES_TRACE, "entering\n");
    if ((ret = ESAPI_GetInfo(ConnectionHandle, InfoType, InfoValue,
                             BufferLength, StringLength))
        == SQL_ERROR)
        CC_log_error("SQLGetInfoW", "", conn);
    LEAVE_CONN_CS(conn);
    return ret;
}

RETCODE SQL_API SQLPrepareW(HSTMT StatementHandle, SQLWCHAR *StatementText,
                            SQLINTEGER TextLength) {
    if(StatementHandle == NULL)
        return SQL_ERROR;

    CSTR func = "SQLPrepareW";
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    SQLLEN slen;
    char *stxt = ucs2_to_utf8(StatementText, TextLength, &slen, FALSE);

    // Enter critical
    ENTER_STMT_CS(stmt);

    // Clear error and rollback
    SC_clear_error(stmt);

    // Prepare statement if statement is ready
    RETCODE ret = SQL_ERROR;
    if (!SC_opencheck(stmt, func))
        ret = ESAPI_Prepare(StatementHandle, (const SQLCHAR *)stxt, (SQLINTEGER)slen);

    // Exit critical
    LEAVE_STMT_CS(stmt);

    // Release memory
    if (stxt)
        free(stxt);
    return ret;
}

RETCODE SQL_API SQLSetCursorNameW(HSTMT StatementHandle, SQLWCHAR *CursorName,
                                  SQLSMALLINT NameLength) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    char *crName;
    SQLLEN nlen;

    MYLOG(ES_TRACE, "entering\n");
    crName = ucs2_to_utf8(CursorName, NameLength, &nlen, FALSE);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_SetCursorName(StatementHandle, (SQLCHAR *)crName,
                              (SQLSMALLINT)nlen);
    LEAVE_STMT_CS(stmt);
    if (crName)
        free(crName);
    return ret;
}

RETCODE SQL_API SQLSpecialColumnsW(
    HSTMT StatementHandle, SQLUSMALLINT IdentifierType, SQLWCHAR *CatalogName,
    SQLSMALLINT NameLength1, SQLWCHAR *SchemaName, SQLSMALLINT NameLength2,
    SQLWCHAR *TableName, SQLSMALLINT NameLength3, SQLUSMALLINT Scope,
    SQLUSMALLINT Nullable) {
    CSTR func = "SQLSpecialColumnsW";
    RETCODE ret;
    char *ctName, *scName, *tbName;
    SQLLEN nmlen1, nmlen2, nmlen3;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    ConnectionClass *conn;
    BOOL lower_id;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(CatalogName, NameLength1, &nmlen1, lower_id);
    scName = ucs2_to_utf8(SchemaName, NameLength2, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(TableName, NameLength3, &nmlen3, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_SpecialColumns(
            StatementHandle, IdentifierType, (SQLCHAR *)ctName,
            (SQLSMALLINT)nmlen1, (SQLCHAR *)scName, (SQLSMALLINT)nmlen2,
            (SQLCHAR *)tbName, (SQLSMALLINT)nmlen3, Scope, Nullable);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    return ret;
}

RETCODE SQL_API SQLStatisticsW(HSTMT StatementHandle, SQLWCHAR *CatalogName,
                               SQLSMALLINT NameLength1, SQLWCHAR *SchemaName,
                               SQLSMALLINT NameLength2, SQLWCHAR *TableName,
                               SQLSMALLINT NameLength3, SQLUSMALLINT Unique,
                               SQLUSMALLINT Reserved) {
    CSTR func = "SQLStatisticsW";
    RETCODE ret;
    char *ctName, *scName, *tbName;
    SQLLEN nmlen1, nmlen2, nmlen3;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    ConnectionClass *conn;
    BOOL lower_id;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(CatalogName, NameLength1, &nmlen1, lower_id);
    scName = ucs2_to_utf8(SchemaName, NameLength2, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(TableName, NameLength3, &nmlen3, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Statistics(StatementHandle, (SQLCHAR *)ctName,
                               (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
                               (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName,
                               (SQLSMALLINT)nmlen3, Unique, Reserved);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    return ret;
}

RETCODE SQL_API SQLTablesW(HSTMT StatementHandle, SQLWCHAR *CatalogName,
                           SQLSMALLINT NameLength1, SQLWCHAR *SchemaName,
                           SQLSMALLINT NameLength2, SQLWCHAR *TableName,
                           SQLSMALLINT NameLength3, SQLWCHAR *TableType,
                           SQLSMALLINT NameLength4) {
    CSTR func = "SQLTablesW";
    RETCODE ret;
    char *ctName, *scName, *tbName, *tbType;
    SQLLEN nmlen1, nmlen2, nmlen3, nmlen4;
    StatementClass *stmt = (StatementClass *)StatementHandle;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(CatalogName, NameLength1, &nmlen1, lower_id);
    scName = ucs2_to_utf8(SchemaName, NameLength2, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(TableName, NameLength3, &nmlen3, lower_id);
    tbType = ucs2_to_utf8(TableType, NameLength4, &nmlen4, FALSE);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Tables(
            StatementHandle, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1,
            (SQLCHAR *)scName, (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName,
            (SQLSMALLINT)nmlen3, (SQLCHAR *)tbType, (SQLSMALLINT)nmlen4, flag);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    if (tbType)
        free(tbType);
    return ret;
}

RETCODE SQL_API SQLColumnPrivilegesW(
    HSTMT hstmt, SQLWCHAR *szCatalogName, SQLSMALLINT cbCatalogName,
    SQLWCHAR *szSchemaName, SQLSMALLINT cbSchemaName, SQLWCHAR *szTableName,
    SQLSMALLINT cbTableName, SQLWCHAR *szColumnName, SQLSMALLINT cbColumnName) {
    CSTR func = "SQLColumnPrivilegesW";
    RETCODE ret;
    char *ctName, *scName, *tbName, *clName;
    SQLLEN nmlen1, nmlen2, nmlen3, nmlen4;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szCatalogName, cbCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szSchemaName, cbSchemaName, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(szTableName, cbTableName, &nmlen3, lower_id);
    clName = ucs2_to_utf8(szColumnName, cbColumnName, &nmlen4, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ColumnPrivileges(
            hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
            (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName, (SQLSMALLINT)nmlen3,
            (SQLCHAR *)clName, (SQLSMALLINT)nmlen4, flag);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    if (clName)
        free(clName);
    return ret;
}

RETCODE SQL_API SQLForeignKeysW(
    HSTMT hstmt, SQLWCHAR *szPkCatalogName, SQLSMALLINT cbPkCatalogName,
    SQLWCHAR *szPkSchemaName, SQLSMALLINT cbPkSchemaName,
    SQLWCHAR *szPkTableName, SQLSMALLINT cbPkTableName,
    SQLWCHAR *szFkCatalogName, SQLSMALLINT cbFkCatalogName,
    SQLWCHAR *szFkSchemaName, SQLSMALLINT cbFkSchemaName,
    SQLWCHAR *szFkTableName, SQLSMALLINT cbFkTableName) {
    CSTR func = "SQLForeignKeysW";
    RETCODE ret;
    char *ctName, *scName, *tbName, *fkctName, *fkscName, *fktbName;
    SQLLEN nmlen1, nmlen2, nmlen3, nmlen4, nmlen5, nmlen6;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szPkCatalogName, cbPkCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szPkSchemaName, cbPkSchemaName, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(szPkTableName, cbPkTableName, &nmlen3, lower_id);
    fkctName =
        ucs2_to_utf8(szFkCatalogName, cbFkCatalogName, &nmlen4, lower_id);
    fkscName = ucs2_to_utf8(szFkSchemaName, cbFkSchemaName, &nmlen5, lower_id);
    fktbName = ucs2_to_utf8(szFkTableName, cbFkTableName, &nmlen6, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ForeignKeys(
            hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
            (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName, (SQLSMALLINT)nmlen3,
            (SQLCHAR *)fkctName, (SQLSMALLINT)nmlen4, (SQLCHAR *)fkscName,
            (SQLSMALLINT)nmlen5, (SQLCHAR *)fktbName, (SQLSMALLINT)nmlen6);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    if (fkctName)
        free(fkctName);
    if (fkscName)
        free(fkscName);
    if (fktbName)
        free(fktbName);
    return ret;
}

RETCODE SQL_API SQLNativeSqlW(HDBC hdbc, SQLWCHAR *szSqlStrIn,
                              SQLINTEGER cbSqlStrIn, SQLWCHAR *szSqlStr,
                              SQLINTEGER cbSqlStrMax, SQLINTEGER *pcbSqlStr) {
    CSTR func = "SQLNativeSqlW";
    RETCODE ret;
    char *szIn, *szOut = NULL, *szOutt = NULL;
    SQLLEN slen;
    SQLINTEGER buflen, olen = 0;
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering\n");
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    CC_set_in_unicode_driver(conn);
    szIn = ucs2_to_utf8(szSqlStrIn, cbSqlStrIn, &slen, FALSE);
    buflen = 3 * cbSqlStrMax;
    if (buflen > 0)
        szOutt = malloc(buflen);
    for (;; buflen = olen + 1, szOutt = realloc(szOut, buflen)) {
        if (!szOutt) {
            CC_set_error(conn, CONN_NO_MEMORY_ERROR,
                         "Could not allocate memory for output buffer", func);
            ret = SQL_ERROR;
            break;
        }
        szOut = szOutt;
        ret = ESAPI_NativeSql(hdbc, (SQLCHAR *)szIn, (SQLINTEGER)slen,
                              (SQLCHAR *)szOut, buflen, &olen);
        if (SQL_SUCCESS_WITH_INFO != ret || olen < buflen)
            break;
    }
    if (szIn)
        free(szIn);
    if (SQL_SUCCEEDED(ret)) {
        SQLLEN szcount = olen;

        if (olen < buflen)
            szcount = utf8_to_ucs2(szOut, olen, szSqlStr, cbSqlStrMax);
        if (SQL_SUCCESS == ret && szcount > cbSqlStrMax) {
            ret = SQL_SUCCESS_WITH_INFO;
            CC_set_error(conn, CONN_TRUNCATED, "Sql string too large", func);
        }
        if (pcbSqlStr)
            *pcbSqlStr = (SQLINTEGER)szcount;
    }
    LEAVE_CONN_CS(conn);
    free(szOut);
    return ret;
}

RETCODE SQL_API SQLPrimaryKeysW(HSTMT hstmt, SQLWCHAR *szCatalogName,
                                SQLSMALLINT cbCatalogName,
                                SQLWCHAR *szSchemaName,
                                SQLSMALLINT cbSchemaName, SQLWCHAR *szTableName,
                                SQLSMALLINT cbTableName) {
    CSTR func = "SQLPrimaryKeysW";
    RETCODE ret;
    char *ctName, *scName, *tbName;
    SQLLEN nmlen1, nmlen2, nmlen3;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szCatalogName, cbCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szSchemaName, cbSchemaName, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(szTableName, cbTableName, &nmlen3, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_PrimaryKeys(hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1,
                                (SQLCHAR *)scName, (SQLSMALLINT)nmlen2,
                                (SQLCHAR *)tbName, (SQLSMALLINT)nmlen3, 0);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    return ret;
}

RETCODE SQL_API SQLProcedureColumnsW(
    HSTMT hstmt, SQLWCHAR *szCatalogName, SQLSMALLINT cbCatalogName,
    SQLWCHAR *szSchemaName, SQLSMALLINT cbSchemaName, SQLWCHAR *szProcName,
    SQLSMALLINT cbProcName, SQLWCHAR *szColumnName, SQLSMALLINT cbColumnName) {
    CSTR func = "SQLProcedureColumnsW";
    RETCODE ret;
    char *ctName, *scName, *prName, *clName;
    SQLLEN nmlen1, nmlen2, nmlen3, nmlen4;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szCatalogName, cbCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szSchemaName, cbSchemaName, &nmlen2, lower_id);
    prName = ucs2_to_utf8(szProcName, cbProcName, &nmlen3, lower_id);
    clName = ucs2_to_utf8(szColumnName, cbColumnName, &nmlen4, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_ProcedureColumns(
            hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
            (SQLSMALLINT)nmlen2, (SQLCHAR *)prName, (SQLSMALLINT)nmlen3,
            (SQLCHAR *)clName, (SQLSMALLINT)nmlen4, flag);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (prName)
        free(prName);
    if (clName)
        free(clName);
    return ret;
}

RETCODE SQL_API SQLProceduresW(HSTMT hstmt, SQLWCHAR *szCatalogName,
                               SQLSMALLINT cbCatalogName,
                               SQLWCHAR *szSchemaName, SQLSMALLINT cbSchemaName,
                               SQLWCHAR *szProcName, SQLSMALLINT cbProcName) {
    CSTR func = "SQLProceduresW";
    RETCODE ret;
    char *ctName, *scName, *prName;
    SQLLEN nmlen1, nmlen2, nmlen3;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szCatalogName, cbCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szSchemaName, cbSchemaName, &nmlen2, lower_id);
    prName = ucs2_to_utf8(szProcName, cbProcName, &nmlen3, lower_id);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_Procedures(hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1,
                               (SQLCHAR *)scName, (SQLSMALLINT)nmlen2,
                               (SQLCHAR *)prName, (SQLSMALLINT)nmlen3, flag);
    LEAVE_STMT_CS(stmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (prName)
        free(prName);
    return ret;
}

RETCODE SQL_API SQLTablePrivilegesW(HSTMT hstmt, SQLWCHAR *szCatalogName,
                                    SQLSMALLINT cbCatalogName,
                                    SQLWCHAR *szSchemaName,
                                    SQLSMALLINT cbSchemaName,
                                    SQLWCHAR *szTableName,
                                    SQLSMALLINT cbTableName) {
    CSTR func = "SQLTablePrivilegesW";
    RETCODE ret;
    char *ctName, *scName, *tbName;
    SQLLEN nmlen1, nmlen2, nmlen3;
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    BOOL lower_id;
    UWORD flag = 0;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    conn = SC_get_conn(stmt);
    lower_id = DEFAULT_LOWERCASEIDENTIFIER;
    ctName = ucs2_to_utf8(szCatalogName, cbCatalogName, &nmlen1, lower_id);
    scName = ucs2_to_utf8(szSchemaName, cbSchemaName, &nmlen2, lower_id);
    tbName = ucs2_to_utf8(szTableName, cbTableName, &nmlen3, lower_id);
    ENTER_STMT_CS((StatementClass *)hstmt);
    SC_clear_error(stmt);
    if (stmt->options.metadata_id)
        flag |= PODBC_NOT_SEARCH_PATTERN;
    if (SC_opencheck(stmt, func))
        ret = SQL_ERROR;
    else
        ret = ESAPI_TablePrivileges(
            hstmt, (SQLCHAR *)ctName, (SQLSMALLINT)nmlen1, (SQLCHAR *)scName,
            (SQLSMALLINT)nmlen2, (SQLCHAR *)tbName, (SQLSMALLINT)nmlen3, flag);
    LEAVE_STMT_CS((StatementClass *)hstmt);
    if (ctName)
        free(ctName);
    if (scName)
        free(scName);
    if (tbName)
        free(tbName);
    return ret;
}

RETCODE SQL_API SQLGetTypeInfoW(SQLHSTMT StatementHandle,
                                SQLSMALLINT DataType) {
    CSTR func = "SQLGetTypeInfoW";
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
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
