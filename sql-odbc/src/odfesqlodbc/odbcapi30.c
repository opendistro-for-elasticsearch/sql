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
#include "es_odbc.h"
#include "misc.h"
#include "statement.h"

/*	SQLAllocConnect/SQLAllocEnv/SQLAllocStmt -> SQLAllocHandle */
RETCODE SQL_API SQLAllocHandle(SQLSMALLINT HandleType, SQLHANDLE InputHandle,
                               SQLHANDLE *OutputHandle) {
    RETCODE ret;
    ConnectionClass *conn;

    MYLOG(ES_TRACE, "entering\n");
    switch (HandleType) {
        case SQL_HANDLE_ENV:
            ret = ESAPI_AllocEnv(OutputHandle);
            break;
        case SQL_HANDLE_DBC:
            ENTER_ENV_CS((EnvironmentClass *)InputHandle);
            ret = ESAPI_AllocConnect(InputHandle, OutputHandle);
            LEAVE_ENV_CS((EnvironmentClass *)InputHandle);
            break;
        case SQL_HANDLE_STMT:
            conn = (ConnectionClass *)InputHandle;
            ENTER_CONN_CS(conn);
            ret = ESAPI_AllocStmt(
                InputHandle, OutputHandle,
                PODBC_EXTERNAL_STATEMENT | PODBC_INHERIT_CONNECT_OPTIONS);
            if (*OutputHandle)
                ((StatementClass *)(*OutputHandle))->external = 1;
            LEAVE_CONN_CS(conn);
            break;
        case SQL_HANDLE_DESC:
            conn = (ConnectionClass *)InputHandle;
            ENTER_CONN_CS(conn);
            ret = ESAPI_AllocDesc(InputHandle, OutputHandle);
            LEAVE_CONN_CS(conn);
            MYLOG(ES_DEBUG, "OutputHandle=%p\n", *OutputHandle);
            break;
        default:
            ret = SQL_ERROR;
            break;
    }
    return ret;
}

/*	SQLBindParameter/SQLSetParam -> SQLBindParam */
RETCODE SQL_API SQLBindParam(HSTMT StatementHandle,
                             SQLUSMALLINT ParameterNumber,
                             SQLSMALLINT ValueType, SQLSMALLINT ParameterType,
                             SQLULEN LengthPrecision,
                             SQLSMALLINT ParameterScale, PTR ParameterValue,
                             SQLLEN *StrLen_or_Ind) {
    UNUSED(ParameterNumber, ValueType, ParameterType, LengthPrecision,
           ParameterScale, ParameterValue, StrLen_or_Ind);
    StatementClass *stmt = (StatementClass *)StatementHandle;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Elasticsearch does not support parameters.", "SQLBindParam");
    return SQL_ERROR;
}

/*	New function */
RETCODE SQL_API SQLCloseCursor(HSTMT StatementHandle) {
    StatementClass *stmt = (StatementClass *)StatementHandle;
    if(stmt == NULL)
        return SQL_ERROR;

    RETCODE ret;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_FreeStmt(StatementHandle, SQL_CLOSE);
    LEAVE_STMT_CS(stmt);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
/*	SQLColAttributes -> SQLColAttribute */
SQLRETURN SQL_API SQLColAttribute(SQLHSTMT StatementHandle,
                                  SQLUSMALLINT ColumnNumber,
                                  SQLUSMALLINT FieldIdentifier,
                                  SQLPOINTER CharacterAttribute,
                                  SQLSMALLINT BufferLength,
                                  SQLSMALLINT *StringLength,
#if defined(_WIN64) || defined(SQLCOLATTRIBUTE_SQLLEN)
                                  SQLLEN *NumericAttribute
#else
                                  SQLPOINTER NumericAttribute
#endif
) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering\n");
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_ColAttributes(StatementHandle, ColumnNumber, FieldIdentifier,
                              CharacterAttribute, BufferLength, StringLength,
                              NumericAttribute);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

/*	new function */
RETCODE SQL_API SQLCopyDesc(SQLHDESC SourceDescHandle,
                            SQLHDESC TargetDescHandle) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering\n");
    ret = ESAPI_CopyDesc(SourceDescHandle, TargetDescHandle);
    return ret;
}

/*	SQLTransact -> SQLEndTran */
RETCODE SQL_API SQLEndTran(SQLSMALLINT HandleType, SQLHANDLE Handle,
                           SQLSMALLINT CompletionType) {
    UNUSED(CompletionType);
    if (HandleType == SQL_HANDLE_STMT) {
        StatementClass *stmt = (StatementClass *)Handle;
        if (stmt == NULL)
            return SQL_ERROR;
        SC_clear_error(stmt);
        SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                     "Transactions are not supported.", "SQLEndTran");
    } else if (HandleType == SQL_HANDLE_DBC) {
        ConnectionClass *conn = (ConnectionClass *)Handle;
        if (conn == NULL)
            return SQL_ERROR;
        CC_clear_error(conn);
        CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR,
                     "Transactions are not supported.", "SQLEndTran");
    }
    return SQL_ERROR;
}

/*	SQLExtendedFetch -> SQLFetchScroll */
RETCODE SQL_API SQLFetchScroll(HSTMT StatementHandle,
                               SQLSMALLINT FetchOrientation,
                               SQLLEN FetchOffset) {
    CSTR func = "SQLFetchScroll";
    StatementClass *stmt = (StatementClass *)StatementHandle;
    RETCODE ret = SQL_SUCCESS;
    IRDFields *irdopts = SC_get_IRDF(stmt);
    SQLUSMALLINT *rowStatusArray = irdopts->rowStatusArray;
    SQLULEN *pcRow = irdopts->rowsFetched;
    SQLLEN bkmarkoff = 0;

    MYLOG(ES_TRACE, "entering %d," FORMAT_LEN "\n", FetchOrientation,
          FetchOffset);
    if (SC_connection_lost_check(stmt, __FUNCTION__))
        return SQL_ERROR;

    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    if (FetchOrientation == SQL_FETCH_BOOKMARK) {
        if (stmt->options.bookmark_ptr) {
            bkmarkoff = FetchOffset;
            FetchOffset = *((Int4 *)stmt->options.bookmark_ptr);
            MYLOG(ES_DEBUG,
                  "bookmark=" FORMAT_LEN " FetchOffset = " FORMAT_LEN "\n",
                  FetchOffset, bkmarkoff);
        } else {
            SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                         "Bookmark isn't specifed yet", func);
            ret = SQL_ERROR;
        }
    }
    if (SQL_SUCCESS == ret) {
        ARDFields *opts = SC_get_ARDF(stmt);

        ret = ESAPI_ExtendedFetch(StatementHandle, FetchOrientation,
                                  FetchOffset, pcRow, rowStatusArray, bkmarkoff,
                                  opts->size_of_rowset);
        stmt->transition_status = STMT_TRANSITION_FETCH_SCROLL;
    }
    LEAVE_STMT_CS(stmt);
    if (ret != SQL_SUCCESS)
        MYLOG(ES_TRACE, "leaving return = %d\n", ret);
    return ret;
}

/*	SQLFree(Connect/Env/Stmt) -> SQLFreeHandle */
RETCODE SQL_API SQLFreeHandle(SQLSMALLINT HandleType, SQLHANDLE Handle) {
    RETCODE ret;
    StatementClass *stmt;
    ConnectionClass *conn = NULL;

    MYLOG(ES_TRACE, "entering\n");

    switch (HandleType) {
        case SQL_HANDLE_ENV:
            ret = ESAPI_FreeEnv(Handle);
            break;
        case SQL_HANDLE_DBC:
            ret = ESAPI_FreeConnect(Handle);
            break;
        case SQL_HANDLE_STMT:
            stmt = (StatementClass *)Handle;

            if (stmt) {
                conn = stmt->hdbc;
                if (conn)
                    ENTER_CONN_CS(conn);
            }

            ret = ESAPI_FreeStmt(Handle, SQL_DROP);

            if (conn)
                LEAVE_CONN_CS(conn);

            break;
        case SQL_HANDLE_DESC:
            ret = ESAPI_FreeDesc(Handle);
            break;
        default:
            ret = SQL_ERROR;
            break;
    }
    return ret;
}

#ifndef UNICODE_SUPPORTXX
/*	new function */
RETCODE SQL_API SQLGetDescField(SQLHDESC DescriptorHandle,
                                SQLSMALLINT RecNumber,
                                SQLSMALLINT FieldIdentifier, PTR Value,
                                SQLINTEGER BufferLength,
                                SQLINTEGER *StringLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering\n");
    ret = ESAPI_GetDescField(DescriptorHandle, RecNumber, FieldIdentifier,
                             Value, BufferLength, StringLength);
    return ret;
}

/*	new function */
RETCODE SQL_API SQLGetDescRec(SQLHDESC DescriptorHandle, SQLSMALLINT RecNumber,
                              SQLCHAR *Name, SQLSMALLINT BufferLength,
                              SQLSMALLINT *StringLength, SQLSMALLINT *Type,
                              SQLSMALLINT *SubType, SQLLEN *Length,
                              SQLSMALLINT *Precision, SQLSMALLINT *Scale,
                              SQLSMALLINT *Nullable) {
    UNUSED(DescriptorHandle, RecNumber, Name, BufferLength, StringLength, Type,
           SubType, Length, Precision, Scale, Nullable);
    MYLOG(ES_TRACE, "entering\n");
    MYLOG(ES_DEBUG, "Error not implemented\n");
    return SQL_ERROR;
}

/*	new function */
RETCODE SQL_API SQLGetDiagField(SQLSMALLINT HandleType, SQLHANDLE Handle,
                                SQLSMALLINT RecNumber,
                                SQLSMALLINT DiagIdentifier, PTR DiagInfo,
                                SQLSMALLINT BufferLength,
                                SQLSMALLINT *StringLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering Handle=(%u,%p) Rec=%d Id=%d info=(%p,%d)\n",
          HandleType, Handle, RecNumber, DiagIdentifier, DiagInfo,
          BufferLength);
    ret = ESAPI_GetDiagField(HandleType, Handle, RecNumber, DiagIdentifier,
                             DiagInfo, BufferLength, StringLength);
    return ret;
}

/*	SQLError -> SQLDiagRec */
RETCODE SQL_API SQLGetDiagRec(SQLSMALLINT HandleType, SQLHANDLE Handle,
                              SQLSMALLINT RecNumber, SQLCHAR *Sqlstate,
                              SQLINTEGER *NativeError, SQLCHAR *MessageText,
                              SQLSMALLINT BufferLength,
                              SQLSMALLINT *TextLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering\n");
    ret = ESAPI_GetDiagRec(HandleType, Handle, RecNumber, Sqlstate, NativeError,
                           MessageText, BufferLength, TextLength);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

/*	new function */
RETCODE SQL_API SQLGetEnvAttr(HENV EnvironmentHandle, SQLINTEGER Attribute,
                              PTR Value, SQLINTEGER BufferLength,
                              SQLINTEGER *StringLength) {
    UNUSED(BufferLength, StringLength);
    RETCODE ret;
    EnvironmentClass *env = (EnvironmentClass *)EnvironmentHandle;

    MYLOG(ES_TRACE, "entering " FORMAT_INTEGER "\n", Attribute);
    ENTER_ENV_CS(env);
    ret = SQL_SUCCESS;
    switch (Attribute) {
        case SQL_ATTR_CONNECTION_POOLING:
            *((unsigned int *)Value) =
                EN_is_pooling(env) ? SQL_CP_ONE_PER_DRIVER : SQL_CP_OFF;
            break;
        case SQL_ATTR_CP_MATCH:
            *((unsigned int *)Value) = SQL_CP_RELAXED_MATCH;
            break;
        case SQL_ATTR_ODBC_VERSION:
            *((unsigned int *)Value) =
                EN_is_odbc2(env) ? SQL_OV_ODBC2 : SQL_OV_ODBC3;
            break;
        case SQL_ATTR_OUTPUT_NTS:
            *((unsigned int *)Value) = SQL_TRUE;
            break;
        default:
            env->errornumber = CONN_INVALID_ARGUMENT_NO;
            ret = SQL_ERROR;
    }
    LEAVE_ENV_CS(env);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
/*	SQLGetConnectOption -> SQLGetconnectAttr */
RETCODE SQL_API SQLGetConnectAttr(HDBC ConnectionHandle, SQLINTEGER Attribute,
                                  PTR Value, SQLINTEGER BufferLength,
                                  SQLINTEGER *StringLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering " FORMAT_UINTEGER "\n", Attribute);
    ENTER_CONN_CS((ConnectionClass *)ConnectionHandle);
    CC_clear_error((ConnectionClass *)ConnectionHandle);
    ret = ESAPI_GetConnectAttr(ConnectionHandle, Attribute, Value, BufferLength,
                               StringLength);
    LEAVE_CONN_CS((ConnectionClass *)ConnectionHandle);
    return ret;
}

/*	SQLGetStmtOption -> SQLGetStmtAttr */
RETCODE SQL_API SQLGetStmtAttr(HSTMT StatementHandle, SQLINTEGER Attribute,
                               PTR Value, SQLINTEGER BufferLength,
                               SQLINTEGER *StringLength) {
    RETCODE ret;
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE, "entering Handle=%p " FORMAT_INTEGER "\n", StatementHandle,
          Attribute);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_GetStmtAttr(StatementHandle, Attribute, Value, BufferLength,
                            StringLength);
    LEAVE_STMT_CS(stmt);
    return ret;
}

/*	SQLSetConnectOption -> SQLSetConnectAttr */
RETCODE SQL_API SQLSetConnectAttr(HDBC ConnectionHandle, SQLINTEGER Attribute,
                                  PTR Value, SQLINTEGER StringLength) {
    RETCODE ret;
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;

    MYLOG(ES_TRACE, "entering " FORMAT_INTEGER "\n", Attribute);
    ENTER_CONN_CS(conn);
    CC_clear_error(conn);
    ret =
        ESAPI_SetConnectAttr(ConnectionHandle, Attribute, Value, StringLength);
    LEAVE_CONN_CS(conn);
    return ret;
}

/*	new function */
RETCODE SQL_API SQLSetDescField(SQLHDESC DescriptorHandle,
                                SQLSMALLINT RecNumber,
                                SQLSMALLINT FieldIdentifier, PTR Value,
                                SQLINTEGER BufferLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering h=%p rec=%d field=%d val=%p\n", DescriptorHandle,
          RecNumber, FieldIdentifier, Value);
    ret = ESAPI_SetDescField(DescriptorHandle, RecNumber, FieldIdentifier,
                             Value, BufferLength);
    return ret;
}

/*	new fucntion */
RETCODE SQL_API SQLSetDescRec(SQLHDESC DescriptorHandle, SQLSMALLINT RecNumber,
                              SQLSMALLINT Type, SQLSMALLINT SubType,
                              SQLLEN Length, SQLSMALLINT Precision,
                              SQLSMALLINT Scale, PTR Data, SQLLEN *StringLength,
                              SQLLEN *Indicator) {
    UNUSED(DescriptorHandle, RecNumber, Type, SubType, Length, Precision, Scale,
           Data, StringLength, Indicator);
    MYLOG(ES_TRACE, "entering\n");
    MYLOG(ES_DEBUG, "Error not implemented\n");
    return SQL_ERROR;
}
#endif /* UNICODE_SUPPORTXX */

/*	new function */
RETCODE SQL_API SQLSetEnvAttr(HENV EnvironmentHandle, SQLINTEGER Attribute,
                              PTR Value, SQLINTEGER StringLength) {
    UNUSED(StringLength);
    RETCODE ret;
    EnvironmentClass *env = (EnvironmentClass *)EnvironmentHandle;

    MYLOG(ES_TRACE, "entering att=" FORMAT_INTEGER "," FORMAT_ULEN "\n",
          Attribute, (SQLULEN)Value);
    ENTER_ENV_CS(env);
    switch (Attribute) {
        case SQL_ATTR_CONNECTION_POOLING:
            switch ((ULONG_PTR)Value) {
                case SQL_CP_OFF:
                    EN_unset_pooling(env);
                    ret = SQL_SUCCESS;
                    break;
                case SQL_CP_ONE_PER_DRIVER:
                    EN_set_pooling(env);
                    ret = SQL_SUCCESS;
                    break;
                default:
                    ret = SQL_SUCCESS_WITH_INFO;
            }
            break;
        case SQL_ATTR_CP_MATCH:
            /* *((unsigned int *) Value) = SQL_CP_RELAXED_MATCH; */
            ret = SQL_SUCCESS;
            break;
        case SQL_ATTR_ODBC_VERSION:
            if (SQL_OV_ODBC2 == CAST_UPTR(SQLUINTEGER, Value))
                EN_set_odbc2(env);
            else
                EN_set_odbc3(env);
            ret = SQL_SUCCESS;
            break;
        case SQL_ATTR_OUTPUT_NTS:
            if (SQL_TRUE == CAST_UPTR(SQLUINTEGER, Value))
                ret = SQL_SUCCESS;
            else
                ret = SQL_SUCCESS_WITH_INFO;
            break;
        default:
            env->errornumber = CONN_INVALID_ARGUMENT_NO;
            ret = SQL_ERROR;
    }
    if (SQL_SUCCESS_WITH_INFO == ret) {
        env->errornumber = CONN_OPTION_VALUE_CHANGED;
        env->errormsg = "SetEnv changed to ";
    }
    LEAVE_ENV_CS(env);
    return ret;
}

#ifndef UNICODE_SUPPORTXX
/*	SQLSet(Param/Scroll/Stmt)Option -> SQLSetStmtAttr */
RETCODE SQL_API SQLSetStmtAttr(HSTMT StatementHandle, SQLINTEGER Attribute,
                               PTR Value, SQLINTEGER StringLength) {
    StatementClass *stmt = (StatementClass *)StatementHandle;
    RETCODE ret;

    MYLOG(ES_TRACE, "entering Handle=%p " FORMAT_INTEGER "," FORMAT_ULEN "\n",
          StatementHandle, Attribute, (SQLULEN)Value);
    ENTER_STMT_CS(stmt);
    SC_clear_error(stmt);
    ret = ESAPI_SetStmtAttr(StatementHandle, Attribute, Value, StringLength);
    LEAVE_STMT_CS(stmt);
    return ret;
}
#endif /* UNICODE_SUPPORTXX */

#define SQL_FUNC_ESET(pfExists, uwAPI) \
    (*(((UWORD *)(pfExists)) + ((uwAPI) >> 4)) |= (1 << ((uwAPI)&0x000F)))
RETCODE SQL_API ESAPI_GetFunctions30(HDBC hdbc, SQLUSMALLINT fFunction,
                                     SQLUSMALLINT FAR *pfExists) {
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    CC_clear_error(conn);
    if (fFunction != SQL_API_ODBC3_ALL_FUNCTIONS)
        return SQL_ERROR;
    memset(pfExists, 0, sizeof(UWORD) * SQL_API_ODBC3_ALL_FUNCTIONS_SIZE);

    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLALLOCCONNECT); 1 deprecated */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLALLOCENV); 2 deprecated */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLALLOCSTMT); 3 deprecated */

    /*
     * for (i = SQL_API_SQLBINDCOL; i <= 23; i++) SQL_FUNC_ESET(pfExists,
     * i);
     */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLBINDCOL);      /* 4 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCANCEL);       /* 5 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCOLATTRIBUTE); /* 6 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCONNECT);      /* 7 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLDESCRIBECOL);  /* 8 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLDISCONNECT);   /* 9 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLERROR);  10 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLEXECDIRECT); /* 11 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLEXECUTE);    /* 12 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLFETCH);      /* 13 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLFREECONNECT); 14 deprecated */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLFREEENV); 15 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLFREESTMT);      /* 16 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETCURSORNAME); /* 17 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLNUMRESULTCOLS); /* 18 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPREPARE);       /* 19 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLROWCOUNT);      /* 20 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETCURSORNAME); /* 21 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLSETPARAM); 22 deprecated */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLTRANSACT); 23 deprecated */

    /*
     * for (i = 40; i < SQL_API_SQLEXTENDEDFETCH; i++)
     * SQL_FUNC_ESET(pfExists, i);
     */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCOLUMNS);       /* 40 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLDRIVERCONNECT); /* 41 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLGETCONNECTOPTION); 42 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETDATA);      /* 43 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETFUNCTIONS); /* 44 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETINFO);      /* 45 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLGETSTMTOPTION); 46 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETTYPEINFO); /* 47 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPARAMDATA);   /* 48 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPUTDATA);     /* 49 */

    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLSETCONNECTIONOPTION); 50 deprecated */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLSETSTMTOPTION); 51 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSPECIALCOLUMNS); /* 52 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSTATISTICS);     /* 53 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLTABLES);         /* 54 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLDATASOURCES);    /* 57 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLDESCRIBEPARAM);  /* 58 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLEXTENDEDFETCH);  /* 59 deprecated ? */

    /*
     * for (++i; i < SQL_API_SQLBINDPARAMETER; i++)
     * SQL_FUNC_ESET(pfExists, i);
     */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLFOREIGNKEYS); /* 60 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLMORERESULTS); /* 61 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLNATIVESQL);   /* 62 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLNUMPARAMS);   /* 63 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLPARAMOPTIONS); 64 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPRIMARYKEYS);      /* 65 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPROCEDURECOLUMNS); /* 66 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLPROCEDURES);       /* 67 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETPOS);           /* 68 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLSETSCROLLOPTIONS); 69 deprecated */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLTABLEPRIVILEGES); /* 70 */
    /* SQL_FUNC_ESET(pfExists, SQL_API_SQLDRIVERS); */   /* 71 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLBINDPARAMETER);   /* 72 */

    SQL_FUNC_ESET(pfExists, SQL_API_SQLALLOCHANDLE);    /* 1001 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLBINDPARAM);      /* 1002 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCLOSECURSOR);    /* 1003 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLCOPYDESC);       /* 1004 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLENDTRAN);        /* 1005 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLFREEHANDLE);     /* 1006 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETCONNECTATTR); /* 1007 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETDESCFIELD);   /* 1008 */
    SQL_FUNC_ESET(pfExists,
                  SQL_API_SQLGETDIAGFIELD); /* 1010 minimal implementation */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETDIAGREC);     /* 1011 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETENVATTR);     /* 1012 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLGETSTMTATTR);    /* 1014 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETCONNECTATTR); /* 1016 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETDESCFIELD);   /* 1017 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETENVATTR);     /* 1019 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLSETSTMTATTR);    /* 1020 */
    SQL_FUNC_ESET(pfExists, SQL_API_SQLFETCHSCROLL);    /* 1021 */
    return SQL_SUCCESS;
}

RETCODE SQL_API SQLBulkOperations(HSTMT hstmt, SQLSMALLINT operation) {
    UNUSED(operation);
    StatementClass *stmt = (StatementClass *)hstmt;
    if (stmt == NULL)
        return SQL_ERROR;
    SC_clear_error(stmt);
    SC_set_error(stmt, STMT_NOT_IMPLEMENTED_ERROR,
                 "Bulk operations are not supported.", "SQLBulkOperations");
    return SQL_ERROR;
}
