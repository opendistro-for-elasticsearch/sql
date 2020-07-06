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

#include "es_odbc.h"
#include "misc.h"

#ifndef WIN32
#include <ctype.h>
#endif /* WIN32 */

#include "bind.h"
#include "convert.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_statement.h"
#include "es_types.h"
#include "qresult.h"
#include "statement.h"

RETCODE SQL_API ESAPI_Prepare(HSTMT hstmt, const SQLCHAR *stmt_str,
                              SQLINTEGER stmt_sz) {
    if (hstmt == NULL)
        return SQL_ERROR;

    // We know cursor is not open at this point
    StatementClass *stmt = (StatementClass *)hstmt;

    // PrepareStatement deallocates memory if necessary
    RETCODE ret = PrepareStatement(stmt, stmt_str, stmt_sz);
    if (ret != SQL_SUCCESS)
        return ret;

    // Execute the statement
    ret = ExecuteStatement(stmt, FALSE);
    if (ret == SQL_SUCCESS)
        stmt->prepared = PREPARED;

    return ret;
}

RETCODE SQL_API ESAPI_Execute(HSTMT hstmt) {
    if (hstmt == NULL)
        return SQL_ERROR;

    // We know cursor is not open at this point
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE ret = SQL_ERROR;
    switch (stmt->prepared) {
        case PREPARED:
            ret = AssignResult(stmt);
            stmt->prepared = EXECUTED;
            break;
        case EXECUTED:
            ret = RePrepareStatement(stmt);
            if (ret != SQL_SUCCESS)
                break;
            ret = ExecuteStatement(stmt, TRUE);
            if (ret != SQL_SUCCESS)
                break;
            stmt->prepared = EXECUTED;
            break;
        case NOT_PREPARED:
            ret = SQL_ERROR;
            break;
        default:
            break;
    }
    return ret;
}

RETCODE SQL_API ESAPI_ExecDirect(HSTMT hstmt, const SQLCHAR *stmt_str,
                                 SQLINTEGER stmt_sz, BOOL commit) {
    if (hstmt == NULL)
        return SQL_ERROR;

    // We know cursor is not open at this point
    StatementClass *stmt = (StatementClass *)hstmt;
    RETCODE ret = PrepareStatement(stmt, stmt_str, stmt_sz);
    if (ret != SQL_SUCCESS)
        return ret;

    // Execute statement
    ret = ExecuteStatement(hstmt, commit);
    if (ret != SQL_SUCCESS)
        return ret;
    stmt->prepared = NOT_PREPARED;
    return ret;
}

/*
 *	Returns the SQL string as modified by the driver.
 *	Currently, just copy the input string without modification
 *	observing buffer limits and truncation.
 */
RETCODE SQL_API ESAPI_NativeSql(HDBC hdbc, const SQLCHAR *szSqlStrIn,
                                SQLINTEGER cbSqlStrIn, SQLCHAR *szSqlStr,
                                SQLINTEGER cbSqlStrMax, SQLINTEGER *pcbSqlStr) {
    CSTR func = "ESAPI_NativeSql";
    size_t len = 0;
    char *ptr;
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    RETCODE result;

    MYLOG(ES_TRACE, "entering...cbSqlStrIn=" FORMAT_INTEGER "\n", cbSqlStrIn);

    ptr = (cbSqlStrIn == 0) ? "" : make_string(szSqlStrIn, cbSqlStrIn, NULL, 0);
    if (!ptr) {
        CC_set_error(conn, CONN_NO_MEMORY_ERROR,
                     "No memory available to store native sql string", func);
        return SQL_ERROR;
    }

    result = SQL_SUCCESS;
    len = strlen(ptr);

    if (szSqlStr) {
        strncpy_null((char *)szSqlStr, ptr, cbSqlStrMax);

        if (len >= (size_t)cbSqlStrMax) {
            result = SQL_SUCCESS_WITH_INFO;
            CC_set_error(conn, CONN_TRUNCATED,
                         "The buffer was too small for the NativeSQL.", func);
        }
    }

    if (pcbSqlStr)
        *pcbSqlStr = (SQLINTEGER)len;

    if (cbSqlStrIn)
        free(ptr);

    return result;
}
