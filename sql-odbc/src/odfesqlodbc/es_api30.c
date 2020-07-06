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

#include "descriptor.h"
#include "dlg_specific.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_odbc.h"
#include "loadlib.h"
#include "misc.h"
#include "qresult.h"
#include "statement.h"

/*	SQLError -> SQLDiagRec */
RETCODE SQL_API ESAPI_GetDiagRec(SQLSMALLINT HandleType, SQLHANDLE Handle,
                                 SQLSMALLINT RecNumber, SQLCHAR *Sqlstate,
                                 SQLINTEGER *NativeError, SQLCHAR *MessageText,
                                 SQLSMALLINT BufferLength,
                                 SQLSMALLINT *TextLength) {
    RETCODE ret;

    MYLOG(ES_TRACE, "entering type=%d rec=%d\n", HandleType, RecNumber);
    switch (HandleType) {
        case SQL_HANDLE_ENV:
            ret = ESAPI_EnvError(Handle, RecNumber, Sqlstate, NativeError,
                                 MessageText, BufferLength, TextLength, 0);
            break;
        case SQL_HANDLE_DBC:
            ret = ESAPI_ConnectError(Handle, RecNumber, Sqlstate, NativeError,
                                     MessageText, BufferLength, TextLength, 0);
            break;
        case SQL_HANDLE_STMT:
            ret = ESAPI_StmtError(Handle, RecNumber, Sqlstate, NativeError,
                                  MessageText, BufferLength, TextLength, 0);
            break;
        case SQL_HANDLE_DESC:
            ret = ESAPI_DescError(Handle, RecNumber, Sqlstate, NativeError,
                                  MessageText, BufferLength, TextLength, 0);
            break;
        default:
            ret = SQL_ERROR;
    }
    MYLOG(ES_TRACE, "leaving %d\n", ret);
    return ret;
}

/*
 *	Minimal implementation.
 *
 */
RETCODE SQL_API ESAPI_GetDiagField(SQLSMALLINT HandleType, SQLHANDLE Handle,
                                   SQLSMALLINT RecNumber,
                                   SQLSMALLINT DiagIdentifier, PTR DiagInfoPtr,
                                   SQLSMALLINT BufferLength,
                                   SQLSMALLINT *StringLengthPtr) {
    RETCODE ret = SQL_ERROR, rtn;
    ConnectionClass *conn;
    StatementClass *stmt;
    SQLLEN rc;
    SQLSMALLINT pcbErrm;
    ssize_t rtnlen = -1;
    int rtnctype = SQL_C_CHAR;

    MYLOG(ES_TRACE, "entering rec=%d\n", RecNumber);
    switch (HandleType) {
        case SQL_HANDLE_ENV:
            switch (DiagIdentifier) {
                case SQL_DIAG_CLASS_ORIGIN:
                case SQL_DIAG_SUBCLASS_ORIGIN:
                case SQL_DIAG_CONNECTION_NAME:
                case SQL_DIAG_SERVER_NAME:
                    rtnlen = 0;
                    if (DiagInfoPtr && BufferLength > rtnlen) {
                        ret = SQL_SUCCESS;
                        *((char *)DiagInfoPtr) = '\0';
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_MESSAGE_TEXT:
                    ret = ESAPI_EnvError(Handle, RecNumber, NULL, NULL,
                                         DiagInfoPtr, BufferLength,
                                         StringLengthPtr, 0);
                    break;
                case SQL_DIAG_NATIVE:
                    rtnctype = SQL_C_LONG;
                    ret = ESAPI_EnvError(Handle, RecNumber, NULL,
                                         (SQLINTEGER *)DiagInfoPtr, NULL, 0,
                                         NULL, 0);
                    break;
                case SQL_DIAG_NUMBER:
                    rtnctype = SQL_C_LONG;
                    ret = ESAPI_EnvError(Handle, RecNumber, NULL, NULL, NULL, 0,
                                         NULL, 0);
                    if (SQL_SUCCEEDED(ret)) {
                        *((SQLINTEGER *)DiagInfoPtr) = 1;
                    }
                    break;
                case SQL_DIAG_SQLSTATE:
                    rtnlen = 5;
                    ret = ESAPI_EnvError(Handle, RecNumber, DiagInfoPtr, NULL,
                                         NULL, 0, NULL, 0);
                    if (SQL_SUCCESS_WITH_INFO == ret)
                        ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_RETURNCODE: /* driver manager returns */
                    break;
                case SQL_DIAG_CURSOR_ROW_COUNT:
                case SQL_DIAG_ROW_COUNT:
                case SQL_DIAG_DYNAMIC_FUNCTION:
                case SQL_DIAG_DYNAMIC_FUNCTION_CODE:
                    /* options for statement type only */
                    break;
            }
            break;
        case SQL_HANDLE_DBC:
            conn = (ConnectionClass *)Handle;
            switch (DiagIdentifier) {
                case SQL_DIAG_CLASS_ORIGIN:
                case SQL_DIAG_SUBCLASS_ORIGIN:
                case SQL_DIAG_CONNECTION_NAME:
                    rtnlen = 0;
                    if (DiagInfoPtr && BufferLength > rtnlen) {
                        ret = SQL_SUCCESS;
                        *((char *)DiagInfoPtr) = '\0';
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_SERVER_NAME:
                    rtnlen = strlen(CC_get_DSN(conn));
                    if (DiagInfoPtr) {
                        strncpy_null(DiagInfoPtr, CC_get_DSN(conn),
                                     BufferLength);
                        ret = (BufferLength > rtnlen ? SQL_SUCCESS
                                                     : SQL_SUCCESS_WITH_INFO);
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_MESSAGE_TEXT:
                    ret = ESAPI_ConnectError(Handle, RecNumber, NULL, NULL,
                                             DiagInfoPtr, BufferLength,
                                             StringLengthPtr, 0);
                    break;
                case SQL_DIAG_NATIVE:
                    rtnctype = SQL_C_LONG;
                    ret = ESAPI_ConnectError(Handle, RecNumber, NULL,
                                             (SQLINTEGER *)DiagInfoPtr, NULL, 0,
                                             NULL, 0);
                    break;
                case SQL_DIAG_NUMBER:
                    rtnctype = SQL_C_LONG;
                    ret = ESAPI_ConnectError(Handle, RecNumber, NULL, NULL,
                                             NULL, 0, NULL, 0);
                    if (SQL_SUCCEEDED(ret)) {
                        *((SQLINTEGER *)DiagInfoPtr) = 1;
                    }
                    break;
                case SQL_DIAG_SQLSTATE:
                    rtnlen = 5;
                    ret = ESAPI_ConnectError(Handle, RecNumber, DiagInfoPtr,
                                             NULL, NULL, 0, NULL, 0);
                    if (SQL_SUCCESS_WITH_INFO == ret)
                        ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_RETURNCODE: /* driver manager returns */
                    break;
                case SQL_DIAG_CURSOR_ROW_COUNT:
                case SQL_DIAG_ROW_COUNT:
                case SQL_DIAG_DYNAMIC_FUNCTION:
                case SQL_DIAG_DYNAMIC_FUNCTION_CODE:
                    /* options for statement type only */
                    break;
            }
            break;
        case SQL_HANDLE_STMT:
            conn = (ConnectionClass *)SC_get_conn(((StatementClass *)Handle));
            switch (DiagIdentifier) {
                case SQL_DIAG_CLASS_ORIGIN:
                case SQL_DIAG_SUBCLASS_ORIGIN:
                case SQL_DIAG_CONNECTION_NAME:
                    rtnlen = 0;
                    if (DiagInfoPtr && BufferLength > rtnlen) {
                        ret = SQL_SUCCESS;
                        *((char *)DiagInfoPtr) = '\0';
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_SERVER_NAME:
                    rtnlen = strlen(CC_get_DSN(conn));
                    if (DiagInfoPtr) {
                        strncpy_null(DiagInfoPtr, CC_get_DSN(conn),
                                     BufferLength);
                        ret = (BufferLength > rtnlen ? SQL_SUCCESS
                                                     : SQL_SUCCESS_WITH_INFO);
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_MESSAGE_TEXT:
                    ret = ESAPI_StmtError(Handle, RecNumber, NULL, NULL,
                                          DiagInfoPtr, BufferLength,
                                          StringLengthPtr, 0);
                    break;
                case SQL_DIAG_NATIVE:
                    rtnctype = SQL_C_LONG;
                    ret = ESAPI_StmtError(Handle, RecNumber, NULL,
                                          (SQLINTEGER *)DiagInfoPtr, NULL, 0,
                                          NULL, 0);
                    break;
                case SQL_DIAG_NUMBER:
                    rtnctype = SQL_C_LONG;
                    *((SQLINTEGER *)DiagInfoPtr) = 0;
                    ret = SQL_NO_DATA_FOUND;
                    stmt = (StatementClass *)Handle;
                    rtn = ESAPI_StmtError(Handle, -1, NULL, NULL, NULL, 0,
                                          &pcbErrm, 0);
                    switch (rtn) {
                        case SQL_SUCCESS:
                        case SQL_SUCCESS_WITH_INFO:
                            ret = SQL_SUCCESS;
                            if (pcbErrm > 0 && stmt->eserror)

                                *((SQLINTEGER *)DiagInfoPtr) =
                                    (pcbErrm - 1) / stmt->eserror->recsize + 1;
                            break;
                        default:
                            break;
                    }
                    break;
                case SQL_DIAG_SQLSTATE:
                    rtnlen = 5;
                    ret = ESAPI_StmtError(Handle, RecNumber, DiagInfoPtr, NULL,
                                          NULL, 0, NULL, 0);
                    if (SQL_SUCCESS_WITH_INFO == ret)
                        ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_CURSOR_ROW_COUNT:
                    rtnctype = SQL_C_LONG;
                    stmt = (StatementClass *)Handle;
                    rc = -1;
                    if (stmt->status == STMT_FINISHED) {
                        QResultClass *res = SC_get_Curres(stmt);

                        /*if (!res)
                            return SQL_ERROR;*/
                        if (stmt->proc_return > 0)
                            rc = 0;
                        else if (res && QR_NumResultCols(res) > 0
                                 && !SC_is_fetchcursor(stmt))
                            rc = QR_get_num_total_tuples(res) - res->dl_count;
                    }
                    *((SQLLEN *)DiagInfoPtr) = rc;
                    MYLOG(ES_ALL, "rc=" FORMAT_LEN "\n", rc);
                    ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_ROW_COUNT:
                    rtnctype = SQL_C_LONG;
                    stmt = (StatementClass *)Handle;
                    *((SQLLEN *)DiagInfoPtr) = stmt->diag_row_count;
                    ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_ROW_NUMBER:
                    rtnctype = SQL_C_LONG;
                    *((SQLLEN *)DiagInfoPtr) = SQL_ROW_NUMBER_UNKNOWN;
                    ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_COLUMN_NUMBER:
                    rtnctype = SQL_C_LONG;
                    *((SQLINTEGER *)DiagInfoPtr) = SQL_COLUMN_NUMBER_UNKNOWN;
                    ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_RETURNCODE: /* driver manager returns */
                    break;
            }
            break;
        case SQL_HANDLE_DESC:
            conn = DC_get_conn(((DescriptorClass *)Handle));
            switch (DiagIdentifier) {
                case SQL_DIAG_CLASS_ORIGIN:
                case SQL_DIAG_SUBCLASS_ORIGIN:
                case SQL_DIAG_CONNECTION_NAME:
                    rtnlen = 0;
                    if (DiagInfoPtr && BufferLength > rtnlen) {
                        ret = SQL_SUCCESS;
                        *((char *)DiagInfoPtr) = '\0';
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_SERVER_NAME:
                    rtnlen = strlen(CC_get_DSN(conn));
                    if (DiagInfoPtr) {
                        strncpy_null(DiagInfoPtr, CC_get_DSN(conn),
                                     BufferLength);
                        ret = (BufferLength > rtnlen ? SQL_SUCCESS
                                                     : SQL_SUCCESS_WITH_INFO);
                    } else
                        ret = SQL_SUCCESS_WITH_INFO;
                    break;
                case SQL_DIAG_MESSAGE_TEXT:
                case SQL_DIAG_NATIVE:
                case SQL_DIAG_NUMBER:
                    break;
                case SQL_DIAG_SQLSTATE:
                    rtnlen = 5;
                    ret = ESAPI_DescError(Handle, RecNumber, DiagInfoPtr, NULL,
                                          NULL, 0, NULL, 0);
                    if (SQL_SUCCESS_WITH_INFO == ret)
                        ret = SQL_SUCCESS;
                    break;
                case SQL_DIAG_RETURNCODE: /* driver manager returns */
                    break;
                case SQL_DIAG_CURSOR_ROW_COUNT:
                case SQL_DIAG_ROW_COUNT:
                case SQL_DIAG_DYNAMIC_FUNCTION:
                case SQL_DIAG_DYNAMIC_FUNCTION_CODE:
                    rtnctype = SQL_C_LONG;
                    /* options for statement type only */
                    break;
            }
            break;
        default:
            ret = SQL_ERROR;
    }
    if (SQL_C_LONG == rtnctype) {
        if (SQL_SUCCESS_WITH_INFO == ret)
            ret = SQL_SUCCESS;
        if (StringLengthPtr)
            *StringLengthPtr = sizeof(SQLINTEGER);
    } else if (rtnlen >= 0) {
        if (rtnlen >= BufferLength) {
            if (SQL_SUCCESS == ret)
                ret = SQL_SUCCESS_WITH_INFO;
            if (BufferLength > 0)
                ((char *)DiagInfoPtr)[BufferLength - 1] = '\0';
        }
        if (StringLengthPtr)
            *StringLengthPtr = (SQLSMALLINT)rtnlen;
    }
    MYLOG(ES_TRACE, "leaving %d\n", ret);
    return ret;
}

/*	SQLGetConnectOption -> SQLGetconnectAttr */
RETCODE SQL_API ESAPI_GetConnectAttr(HDBC ConnectionHandle,
                                     SQLINTEGER Attribute, PTR Value,
                                     SQLINTEGER BufferLength,
                                     SQLINTEGER *StringLength) {
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;
    RETCODE ret = SQL_SUCCESS;
    SQLINTEGER len = 4;

    MYLOG(ES_TRACE, "entering " FORMAT_INTEGER "\n", Attribute);
    switch (Attribute) {
        case SQL_ATTR_ASYNC_ENABLE:
            *((SQLINTEGER *)Value) = SQL_ASYNC_ENABLE_OFF;
            break;
        case SQL_ATTR_AUTO_IPD:
            *((SQLINTEGER *)Value) = SQL_FALSE;
            break;
        case SQL_ATTR_CONNECTION_DEAD:
            *((SQLUINTEGER *)Value) = CC_not_connected(conn);
            break;
        case SQL_ATTR_CONNECTION_TIMEOUT:
            *((SQLUINTEGER *)Value) = 0;
            break;
        case SQL_ATTR_METADATA_ID:
            *((SQLUINTEGER *)Value) = conn->stmtOptions.metadata_id;
            break;
        case SQL_ATTR_ESOPT_DEBUG:
            *((SQLINTEGER *)Value) = conn->connInfo.drivers.loglevel;
            break;
        case SQL_ATTR_ESOPT_COMMLOG:
            *((SQLINTEGER *)Value) = conn->connInfo.drivers.loglevel;
            break;
        default:
            ret = ESAPI_GetConnectOption(ConnectionHandle, (UWORD)Attribute,
                                         Value, &len, BufferLength);
    }
    if (StringLength)
        *StringLength = len;
    return ret;
}

static SQLHDESC descHandleFromStatementHandle(HSTMT StatementHandle,
                                              SQLINTEGER descType) {
    StatementClass *stmt = (StatementClass *)StatementHandle;

    switch (descType) {
        case SQL_ATTR_APP_ROW_DESC: /* 10010 */
            return (HSTMT)stmt->ard;
        case SQL_ATTR_APP_PARAM_DESC: /* 10011 */
            return (HSTMT)stmt->apd;
        case SQL_ATTR_IMP_ROW_DESC: /* 10012 */
            return (HSTMT)stmt->ird;
        case SQL_ATTR_IMP_PARAM_DESC: /* 10013 */
            return (HSTMT)stmt->ipd;
    }
    return (HSTMT)0;
}

static void column_bindings_set(ARDFields *opts, SQLSMALLINT cols,
                                BOOL maxset) {
    int i;

    if (cols == opts->allocated)
        return;
    if (cols > opts->allocated) {
        extend_column_bindings(opts, cols);
        return;
    }
    if (maxset)
        return;

    for (i = opts->allocated; i > cols; i--)
        reset_a_column_binding(opts, i);
    opts->allocated = cols;
    if (0 == cols) {
        free(opts->bindings);
        opts->bindings = NULL;
    }
}

static RETCODE SQL_API ARDSetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    ARDFields *opts = &(desc->ardf);
    SQLSMALLINT row_idx;
    BOOL unbind = TRUE;

    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
            opts->size_of_rowset = CAST_UPTR(SQLULEN, Value);
            return ret;
        case SQL_DESC_ARRAY_STATUS_PTR:
            opts->row_operation_ptr = Value;
            return ret;
        case SQL_DESC_BIND_OFFSET_PTR:
            opts->row_offset_ptr = Value;
            return ret;
        case SQL_DESC_BIND_TYPE:
            opts->bind_size = CAST_UPTR(SQLUINTEGER, Value);
            return ret;
        case SQL_DESC_COUNT:
            column_bindings_set(opts, CAST_PTR(SQLSMALLINT, Value), FALSE);
            return ret;

        case SQL_DESC_TYPE:
        case SQL_DESC_DATETIME_INTERVAL_CODE:
        case SQL_DESC_CONCISE_TYPE:
            column_bindings_set(opts, RecNumber, TRUE);
            break;
    }
    if (RecNumber < 0 || RecNumber > opts->allocated) {
        DC_set_error(desc, DESC_INVALID_COLUMN_NUMBER_ERROR,
                     "invalid column number");
        return SQL_ERROR;
    }
    if (0 == RecNumber) /* bookmark column */
    {
        BindInfoClass *bookmark = ARD_AllocBookmark(opts);

        switch (FieldIdentifier) {
            case SQL_DESC_DATA_PTR:
                bookmark->buffer = Value;
                break;
            case SQL_DESC_INDICATOR_PTR:
                bookmark->indicator = Value;
                break;
            case SQL_DESC_OCTET_LENGTH_PTR:
                bookmark->used = Value;
                break;
            default:
                DC_set_error(desc, DESC_INVALID_COLUMN_NUMBER_ERROR,
                             "invalid column number");
                ret = SQL_ERROR;
        }
        return ret;
    }
    row_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_TYPE:
            opts->bindings[row_idx].returntype = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            switch (opts->bindings[row_idx].returntype) {
                case SQL_DATETIME:
                case SQL_C_TYPE_DATE:
                case SQL_C_TYPE_TIME:
                case SQL_C_TYPE_TIMESTAMP:
                    switch ((LONG_PTR)Value) {
                        case SQL_CODE_DATE:
                            opts->bindings[row_idx].returntype =
                                SQL_C_TYPE_DATE;
                            break;
                        case SQL_CODE_TIME:
                            opts->bindings[row_idx].returntype =
                                SQL_C_TYPE_TIME;
                            break;
                        case SQL_CODE_TIMESTAMP:
                            opts->bindings[row_idx].returntype =
                                SQL_C_TYPE_TIMESTAMP;
                            break;
                    }
                    break;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            opts->bindings[row_idx].returntype = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_DATA_PTR:
            unbind = FALSE;
            opts->bindings[row_idx].buffer = Value;
            break;
        case SQL_DESC_INDICATOR_PTR:
            unbind = FALSE;
            opts->bindings[row_idx].indicator = Value;
            break;
        case SQL_DESC_OCTET_LENGTH_PTR:
            unbind = FALSE;
            opts->bindings[row_idx].used = Value;
            break;
        case SQL_DESC_OCTET_LENGTH:
            opts->bindings[row_idx].buflen = CAST_PTR(SQLLEN, Value);
            break;
        case SQL_DESC_PRECISION:
            opts->bindings[row_idx].precision = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_SCALE:
            opts->bindings[row_idx].scale = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_LENGTH:
        case SQL_DESC_NUM_PREC_RADIX:
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    if (unbind)
        opts->bindings[row_idx].buffer = NULL;
    return ret;
}

static void parameter_bindings_set(APDFields *opts, SQLSMALLINT params,
                                   BOOL maxset) {
    int i;

    if (params == opts->allocated)
        return;
    if (params > opts->allocated) {
        extend_parameter_bindings(opts, params);
        return;
    }
    if (maxset)
        return;

    for (i = opts->allocated; i > params; i--)
        reset_a_parameter_binding(opts, i);
    opts->allocated = params;
    if (0 == params) {
        free(opts->parameters);
        opts->parameters = NULL;
    }
}

static void parameter_ibindings_set(IPDFields *opts, SQLSMALLINT params,
                                    BOOL maxset) {
    int i;

    if (params == opts->allocated)
        return;
    if (params > opts->allocated) {
        extend_iparameter_bindings(opts, params);
        return;
    }
    if (maxset)
        return;

    for (i = opts->allocated; i > params; i--)
        reset_a_iparameter_binding(opts, i);
    opts->allocated = params;
    if (0 == params) {
        free(opts->parameters);
        opts->parameters = NULL;
    }
}

static RETCODE SQL_API APDSetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    APDFields *opts = &(desc->apdf);
    SQLSMALLINT para_idx;
    BOOL unbind = TRUE;

    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
            opts->paramset_size = CAST_UPTR(SQLUINTEGER, Value);
            return ret;
        case SQL_DESC_ARRAY_STATUS_PTR:
            opts->param_operation_ptr = Value;
            return ret;
        case SQL_DESC_BIND_OFFSET_PTR:
            opts->param_offset_ptr = Value;
            return ret;
        case SQL_DESC_BIND_TYPE:
            opts->param_bind_type = CAST_UPTR(SQLUINTEGER, Value);
            return ret;
        case SQL_DESC_COUNT:
            parameter_bindings_set(opts, CAST_PTR(SQLSMALLINT, Value), FALSE);
            return ret;

        case SQL_DESC_TYPE:
        case SQL_DESC_DATETIME_INTERVAL_CODE:
        case SQL_DESC_CONCISE_TYPE:
            parameter_bindings_set(opts, RecNumber, TRUE);
            break;
    }
    if (RecNumber <= 0) {
        MYLOG(ES_ALL, "RecN=%d allocated=%d\n", RecNumber, opts->allocated);
        DC_set_error(desc, DESC_BAD_PARAMETER_NUMBER_ERROR,
                     "bad parameter number");
        return SQL_ERROR;
    }
    if (RecNumber > opts->allocated) {
        MYLOG(ES_ALL, "RecN=%d allocated=%d\n", RecNumber, opts->allocated);
        parameter_bindings_set(opts, RecNumber, TRUE);
        /* DC_set_error(desc, DESC_BAD_PARAMETER_NUMBER_ERROR,
                "bad parameter number");
        return SQL_ERROR;*/
    }
    para_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_TYPE:
            opts->parameters[para_idx].CType = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            switch (opts->parameters[para_idx].CType) {
                case SQL_DATETIME:
                case SQL_C_TYPE_DATE:
                case SQL_C_TYPE_TIME:
                case SQL_C_TYPE_TIMESTAMP:
                    switch ((LONG_PTR)Value) {
                        case SQL_CODE_DATE:
                            opts->parameters[para_idx].CType = SQL_C_TYPE_DATE;
                            break;
                        case SQL_CODE_TIME:
                            opts->parameters[para_idx].CType = SQL_C_TYPE_TIME;
                            break;
                        case SQL_CODE_TIMESTAMP:
                            opts->parameters[para_idx].CType =
                                SQL_C_TYPE_TIMESTAMP;
                            break;
                    }
                    break;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            opts->parameters[para_idx].CType = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_DATA_PTR:
            unbind = FALSE;
            opts->parameters[para_idx].buffer = Value;
            break;
        case SQL_DESC_INDICATOR_PTR:
            unbind = FALSE;
            opts->parameters[para_idx].indicator = Value;
            break;
        case SQL_DESC_OCTET_LENGTH:
            opts->parameters[para_idx].buflen = CAST_PTR(Int4, Value);
            break;
        case SQL_DESC_OCTET_LENGTH_PTR:
            unbind = FALSE;
            opts->parameters[para_idx].used = Value;
            break;
        case SQL_DESC_PRECISION:
            opts->parameters[para_idx].precision = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_SCALE:
            opts->parameters[para_idx].scale = CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_LENGTH:
        case SQL_DESC_NUM_PREC_RADIX:
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invaid descriptor identifier");
    }
    if (unbind)
        opts->parameters[para_idx].buffer = NULL;

    return ret;
}

static RETCODE SQL_API IRDSetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength) {
    UNUSED(BufferLength, RecNumber);
    RETCODE ret = SQL_SUCCESS;
    IRDFields *opts = &(desc->irdf);

    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_STATUS_PTR:
            opts->rowStatusArray = (SQLUSMALLINT *)Value;
            break;
        case SQL_DESC_ROWS_PROCESSED_PTR:
            opts->rowsFetched = (SQLULEN *)Value;
            break;
        case SQL_DESC_ALLOC_TYPE:                  /* read-only */
        case SQL_DESC_COUNT:                       /* read-only */
        case SQL_DESC_AUTO_UNIQUE_VALUE:           /* read-only */
        case SQL_DESC_BASE_COLUMN_NAME:            /* read-only */
        case SQL_DESC_BASE_TABLE_NAME:             /* read-only */
        case SQL_DESC_CASE_SENSITIVE:              /* read-only */
        case SQL_DESC_CATALOG_NAME:                /* read-only */
        case SQL_DESC_CONCISE_TYPE:                /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_CODE:      /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION: /* read-only */
        case SQL_DESC_DISPLAY_SIZE:                /* read-only */
        case SQL_DESC_FIXED_PREC_SCALE:            /* read-only */
        case SQL_DESC_LABEL:                       /* read-only */
        case SQL_DESC_LENGTH:                      /* read-only */
        case SQL_DESC_LITERAL_PREFIX:              /* read-only */
        case SQL_DESC_LITERAL_SUFFIX:              /* read-only */
        case SQL_DESC_LOCAL_TYPE_NAME:             /* read-only */
        case SQL_DESC_NAME:                        /* read-only */
        case SQL_DESC_NULLABLE:                    /* read-only */
        case SQL_DESC_NUM_PREC_RADIX:              /* read-only */
        case SQL_DESC_OCTET_LENGTH:                /* read-only */
        case SQL_DESC_PRECISION:                   /* read-only */
        case SQL_DESC_ROWVER:                      /* read-only */
        case SQL_DESC_SCALE:                       /* read-only */
        case SQL_DESC_SCHEMA_NAME:                 /* read-only */
        case SQL_DESC_SEARCHABLE:                  /* read-only */
        case SQL_DESC_TABLE_NAME:                  /* read-only */
        case SQL_DESC_TYPE:                        /* read-only */
        case SQL_DESC_TYPE_NAME:                   /* read-only */
        case SQL_DESC_UNNAMED:                     /* read-only */
        case SQL_DESC_UNSIGNED:                    /* read-only */
        case SQL_DESC_UPDATABLE:                   /* read-only */
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    return ret;
}

static RETCODE SQL_API IPDSetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    IPDFields *ipdopts = &(desc->ipdf);
    SQLSMALLINT para_idx;

    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_STATUS_PTR:
            ipdopts->param_status_ptr = (SQLUSMALLINT *)Value;
            return ret;
        case SQL_DESC_ROWS_PROCESSED_PTR:
            ipdopts->param_processed_ptr = (SQLULEN *)Value;
            return ret;
        case SQL_DESC_COUNT:
            parameter_ibindings_set(ipdopts, CAST_PTR(SQLSMALLINT, Value),
                                    FALSE);
            return ret;
        case SQL_DESC_UNNAMED: /* only SQL_UNNAMED is allowed */
            if (SQL_UNNAMED != CAST_PTR(SQLSMALLINT, Value)) {
                ret = SQL_ERROR;
                DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                             "invalid descriptor identifier");
                return ret;
            }
        case SQL_DESC_NAME:
        case SQL_DESC_TYPE:
        case SQL_DESC_DATETIME_INTERVAL_CODE:
        case SQL_DESC_CONCISE_TYPE:
            parameter_ibindings_set(ipdopts, RecNumber, TRUE);
            break;
    }
    if (RecNumber <= 0 || RecNumber > ipdopts->allocated) {
        MYLOG(ES_ALL, "RecN=%d allocated=%d\n", RecNumber, ipdopts->allocated);
        DC_set_error(desc, DESC_BAD_PARAMETER_NUMBER_ERROR,
                     "bad parameter number");
        return SQL_ERROR;
    }
    para_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_TYPE:
            if (ipdopts->parameters[para_idx].SQLType
                != CAST_PTR(SQLSMALLINT, Value)) {
                reset_a_iparameter_binding(ipdopts, RecNumber);
                ipdopts->parameters[para_idx].SQLType =
                    CAST_PTR(SQLSMALLINT, Value);
            }
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            switch (ipdopts->parameters[para_idx].SQLType) {
                case SQL_DATETIME:
                case SQL_TYPE_DATE:
                case SQL_TYPE_TIME:
                case SQL_TYPE_TIMESTAMP:
                    switch ((LONG_PTR)Value) {
                        case SQL_CODE_DATE:
                            ipdopts->parameters[para_idx].SQLType =
                                SQL_TYPE_DATE;
                            break;
                        case SQL_CODE_TIME:
                            ipdopts->parameters[para_idx].SQLType =
                                SQL_TYPE_TIME;
                            break;
                        case SQL_CODE_TIMESTAMP:
                            ipdopts->parameters[para_idx].SQLType =
                                SQL_TYPE_TIMESTAMP;
                            break;
                    }
                    break;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            ipdopts->parameters[para_idx].SQLType =
                CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_NAME:
            if (Value)
                STR_TO_NAME(ipdopts->parameters[para_idx].paramName, Value);
            else
                NULL_THE_NAME(ipdopts->parameters[para_idx].paramName);
            break;
        case SQL_DESC_PARAMETER_TYPE:
            ipdopts->parameters[para_idx].paramType =
                CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_SCALE:
            ipdopts->parameters[para_idx].decimal_digits =
                CAST_PTR(SQLSMALLINT, Value);
            break;
        case SQL_DESC_UNNAMED: /* only SQL_UNNAMED is allowed */
            if (SQL_UNNAMED != CAST_PTR(SQLSMALLINT, Value)) {
                ret = SQL_ERROR;
                DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                             "invalid descriptor identifier");
            } else
                NULL_THE_NAME(ipdopts->parameters[para_idx].paramName);
            break;
        case SQL_DESC_ALLOC_TYPE:     /* read-only */
        case SQL_DESC_CASE_SENSITIVE: /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_FIXED_PREC_SCALE: /* read-only */
        case SQL_DESC_LENGTH:
        case SQL_DESC_LOCAL_TYPE_NAME: /* read-only */
        case SQL_DESC_NULLABLE:        /* read-only */
        case SQL_DESC_NUM_PREC_RADIX:
        case SQL_DESC_OCTET_LENGTH:
        case SQL_DESC_PRECISION:
        case SQL_DESC_ROWVER:    /* read-only */
        case SQL_DESC_TYPE_NAME: /* read-only */
        case SQL_DESC_UNSIGNED:  /* read-only */
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    return ret;
}

static RETCODE SQL_API ARDGetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength,
                                   SQLINTEGER *StringLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    SQLLEN ival = 0;
    SQLINTEGER len, rettype = 0;
    PTR ptr = NULL;
    const ARDFields *opts = &(desc->ardf);
    SQLSMALLINT row_idx;

    len = sizeof(SQLINTEGER);
    if (0 == RecNumber) /* bookmark */
    {
        BindInfoClass *bookmark = opts->bookmark;
        switch (FieldIdentifier) {
            case SQL_DESC_DATA_PTR:
                rettype = SQL_IS_POINTER;
                ptr = bookmark ? bookmark->buffer : NULL;
                break;
            case SQL_DESC_INDICATOR_PTR:
                rettype = SQL_IS_POINTER;
                ptr = bookmark ? bookmark->indicator : NULL;
                break;
            case SQL_DESC_OCTET_LENGTH_PTR:
                rettype = SQL_IS_POINTER;
                ptr = bookmark ? bookmark->used : NULL;
                break;
        }
        if (ptr) {
            *((void **)Value) = ptr;
            if (StringLength)
                *StringLength = len;
            return ret;
        }
    }
    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
        case SQL_DESC_ARRAY_STATUS_PTR:
        case SQL_DESC_BIND_OFFSET_PTR:
        case SQL_DESC_BIND_TYPE:
        case SQL_DESC_COUNT:
            break;
        default:
            if (RecNumber <= 0 || RecNumber > opts->allocated) {
                DC_set_error(desc, DESC_INVALID_COLUMN_NUMBER_ERROR,
                             "invalid column number");
                return SQL_ERROR;
            }
    }
    row_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
            ival = opts->size_of_rowset;
            break;
        case SQL_DESC_ARRAY_STATUS_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->row_operation_ptr;
            break;
        case SQL_DESC_BIND_OFFSET_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->row_offset_ptr;
            break;
        case SQL_DESC_BIND_TYPE:
            ival = opts->bind_size;
            break;
        case SQL_DESC_TYPE:
            rettype = SQL_IS_SMALLINT;
            switch (opts->bindings[row_idx].returntype) {
                case SQL_C_TYPE_DATE:
                case SQL_C_TYPE_TIME:
                case SQL_C_TYPE_TIMESTAMP:
                    ival = SQL_DATETIME;
                    break;
                default:
                    ival = opts->bindings[row_idx].returntype;
            }
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            rettype = SQL_IS_SMALLINT;
            switch (opts->bindings[row_idx].returntype) {
                case SQL_C_TYPE_DATE:
                    ival = SQL_CODE_DATE;
                    break;
                case SQL_C_TYPE_TIME:
                    ival = SQL_CODE_TIME;
                    break;
                case SQL_C_TYPE_TIMESTAMP:
                    ival = SQL_CODE_TIMESTAMP;
                    break;
                default:
                    ival = 0;
                    break;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            rettype = SQL_IS_SMALLINT;
            ival = opts->bindings[row_idx].returntype;
            break;
        case SQL_DESC_DATA_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->bindings[row_idx].buffer;
            break;
        case SQL_DESC_INDICATOR_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->bindings[row_idx].indicator;
            break;
        case SQL_DESC_OCTET_LENGTH_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->bindings[row_idx].used;
            break;
        case SQL_DESC_COUNT:
            rettype = SQL_IS_SMALLINT;
            ival = opts->allocated;
            break;
        case SQL_DESC_OCTET_LENGTH:
            ival = opts->bindings[row_idx].buflen;
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
            rettype = SQL_IS_SMALLINT;
            if (DC_get_embedded(desc))
                ival = SQL_DESC_ALLOC_AUTO;
            else
                ival = SQL_DESC_ALLOC_USER;
            break;
        case SQL_DESC_PRECISION:
            rettype = SQL_IS_SMALLINT;
            ival = opts->bindings[row_idx].precision;
            break;
        case SQL_DESC_SCALE:
            rettype = SQL_IS_SMALLINT;
            ival = opts->bindings[row_idx].scale;
            break;
        case SQL_DESC_NUM_PREC_RADIX:
            ival = 10;
            break;
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_LENGTH:
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    switch (rettype) {
        case 0:
        case SQL_IS_INTEGER:
            len = sizeof(SQLINTEGER);
            *((SQLINTEGER *)Value) = (SQLINTEGER)ival;
            break;
        case SQL_IS_SMALLINT:
            len = sizeof(SQLSMALLINT);
            *((SQLSMALLINT *)Value) = (SQLSMALLINT)ival;
            break;
        case SQL_IS_POINTER:
            len = sizeof(SQLPOINTER);
            *((void **)Value) = ptr;
            break;
    }

    if (StringLength)
        *StringLength = len;
    return ret;
}

static RETCODE SQL_API APDGetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength,
                                   SQLINTEGER *StringLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    SQLLEN ival = 0;
    SQLINTEGER len, rettype = 0;
    PTR ptr = NULL;
    const APDFields *opts = (const APDFields *)&(desc->apdf);
    SQLSMALLINT para_idx;

    len = sizeof(SQLINTEGER);
    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
        case SQL_DESC_ARRAY_STATUS_PTR:
        case SQL_DESC_BIND_OFFSET_PTR:
        case SQL_DESC_BIND_TYPE:
        case SQL_DESC_COUNT:
            break;
        default:
            if (RecNumber <= 0 || RecNumber > opts->allocated) {
                MYLOG(ES_ALL, "RecN=%d allocated=%d\n", RecNumber,
                      opts->allocated);
                DC_set_error(desc, DESC_BAD_PARAMETER_NUMBER_ERROR,
                             "bad parameter number");
                return SQL_ERROR;
            }
    }
    para_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_SIZE:
            rettype = SQL_IS_LEN;
            ival = opts->paramset_size;
            break;
        case SQL_DESC_ARRAY_STATUS_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->param_operation_ptr;
            break;
        case SQL_DESC_BIND_OFFSET_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->param_offset_ptr;
            break;
        case SQL_DESC_BIND_TYPE:
            ival = opts->param_bind_type;
            break;

        case SQL_DESC_TYPE:
            rettype = SQL_IS_SMALLINT;
            switch (opts->parameters[para_idx].CType) {
                case SQL_C_TYPE_DATE:
                case SQL_C_TYPE_TIME:
                case SQL_C_TYPE_TIMESTAMP:
                    ival = SQL_DATETIME;
                    break;
                default:
                    ival = opts->parameters[para_idx].CType;
            }
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            rettype = SQL_IS_SMALLINT;
            switch (opts->parameters[para_idx].CType) {
                case SQL_C_TYPE_DATE:
                    ival = SQL_CODE_DATE;
                    break;
                case SQL_C_TYPE_TIME:
                    ival = SQL_CODE_TIME;
                    break;
                case SQL_C_TYPE_TIMESTAMP:
                    ival = SQL_CODE_TIMESTAMP;
                    break;
                default:
                    ival = 0;
                    break;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            rettype = SQL_IS_SMALLINT;
            ival = opts->parameters[para_idx].CType;
            break;
        case SQL_DESC_DATA_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->parameters[para_idx].buffer;
            break;
        case SQL_DESC_INDICATOR_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->parameters[para_idx].indicator;
            break;
        case SQL_DESC_OCTET_LENGTH:
            ival = opts->parameters[para_idx].buflen;
            break;
        case SQL_DESC_OCTET_LENGTH_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->parameters[para_idx].used;
            break;
        case SQL_DESC_COUNT:
            rettype = SQL_IS_SMALLINT;
            ival = opts->allocated;
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
            rettype = SQL_IS_SMALLINT;
            if (DC_get_embedded(desc))
                ival = SQL_DESC_ALLOC_AUTO;
            else
                ival = SQL_DESC_ALLOC_USER;
            break;
        case SQL_DESC_NUM_PREC_RADIX:
            ival = 10;
            break;
        case SQL_DESC_PRECISION:
            rettype = SQL_IS_SMALLINT;
            ival = opts->parameters[para_idx].precision;
            break;
        case SQL_DESC_SCALE:
            rettype = SQL_IS_SMALLINT;
            ival = opts->parameters[para_idx].scale;
            break;
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_LENGTH:
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifer");
    }
    switch (rettype) {
        case SQL_IS_LEN:
            len = sizeof(SQLLEN);
            *((SQLLEN *)Value) = ival;
            break;
        case 0:
        case SQL_IS_INTEGER:
            len = sizeof(SQLINTEGER);
            *((SQLINTEGER *)Value) = (SQLINTEGER)ival;
            break;
        case SQL_IS_SMALLINT:
            len = sizeof(SQLSMALLINT);
            *((SQLSMALLINT *)Value) = (SQLSMALLINT)ival;
            break;
        case SQL_IS_POINTER:
            len = sizeof(SQLPOINTER);
            *((void **)Value) = ptr;
            break;
    }

    if (StringLength)
        *StringLength = len;
    return ret;
}

static RETCODE SQL_API IRDGetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength,
                                   SQLINTEGER *StringLength) {
    RETCODE ret = SQL_SUCCESS;
    SQLLEN ival = 0;
    SQLINTEGER len = 0, rettype = 0;
    PTR ptr = NULL;
    BOOL bCallColAtt = FALSE;
    const IRDFields *opts = &(desc->irdf);

    switch (FieldIdentifier) {
        case SQL_DESC_ROWVER: /* read-only */
            // Database is read-only, and does not support transactions
            rettype = SQL_IS_SMALLINT;
            ival = SQL_FALSE;
            break;
        case SQL_DESC_ARRAY_STATUS_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->rowStatusArray;
            break;
        case SQL_DESC_ROWS_PROCESSED_PTR:
            rettype = SQL_IS_POINTER;
            ptr = opts->rowsFetched;
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
            rettype = SQL_IS_SMALLINT;
            ival = SQL_DESC_ALLOC_AUTO;
            break;
        case SQL_DESC_AUTO_UNIQUE_VALUE:           /* read-only */
        case SQL_DESC_CASE_SENSITIVE:              /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION: /* read-only */
        case SQL_DESC_NUM_PREC_RADIX:              /* read-only */
            rettype = SQL_IS_INTEGER;
            bCallColAtt = TRUE;
            break;
        case SQL_DESC_DISPLAY_SIZE:                /* read-only */
        case SQL_DESC_LENGTH:                      /* read-only */
        case SQL_DESC_OCTET_LENGTH:                /* read-only */
            rettype = SQL_IS_LEN;
            bCallColAtt = TRUE;
            break;
        case SQL_DESC_NULLABLE:                    /* read-only */
        case SQL_DESC_FIXED_PREC_SCALE:            /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_CODE:      /* read-only */
        case SQL_DESC_CONCISE_TYPE:                /* read-only */
        case SQL_DESC_COUNT:                       /* read-only */
        case SQL_DESC_PRECISION:                   /* read-only */
        case SQL_DESC_SCALE:                       /* read-only */
        case SQL_DESC_SEARCHABLE:                  /* read-only */
        case SQL_DESC_TYPE:                        /* read-only */
        case SQL_DESC_UNNAMED:                     /* read-only */
        case SQL_DESC_UNSIGNED:                    /* read-only */
        case SQL_DESC_UPDATABLE:                   /* read-only */
            rettype = SQL_IS_SMALLINT;
            bCallColAtt = TRUE;
            break;
        case SQL_DESC_BASE_COLUMN_NAME: /* read-only */
        case SQL_DESC_BASE_TABLE_NAME:  /* read-only */
        case SQL_DESC_CATALOG_NAME:     /* read-only */
        case SQL_DESC_LABEL:            /* read-only */
        case SQL_DESC_LITERAL_PREFIX:   /* read-only */
        case SQL_DESC_LITERAL_SUFFIX:   /* read-only */
        case SQL_DESC_LOCAL_TYPE_NAME:  /* read-only */
        case SQL_DESC_NAME:             /* read-only */
        case SQL_DESC_SCHEMA_NAME:      /* read-only */
        case SQL_DESC_TABLE_NAME:       /* read-only */
        case SQL_DESC_TYPE_NAME:        /* read-only */
            rettype = SQL_NTS;
            bCallColAtt = TRUE;
            break;
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    if (bCallColAtt) {
        SQLSMALLINT pcbL;
        StatementClass *stmt;

        stmt = opts->stmt;
        ret = ESAPI_ColAttributes(stmt, RecNumber, FieldIdentifier, Value,
                                  (SQLSMALLINT)BufferLength, &pcbL, &ival);
        len = pcbL;
    }
    switch (rettype) {
        case 0:
        case SQL_IS_INTEGER:
            len = sizeof(SQLINTEGER);
            *((SQLINTEGER *)Value) = (SQLINTEGER)ival;
            break;
        case SQL_IS_UINTEGER:
            len = sizeof(SQLUINTEGER);
            *((SQLUINTEGER *)Value) = (SQLUINTEGER)ival;
            break;
        case SQL_IS_SMALLINT:
            len = sizeof(SQLSMALLINT);
            *((SQLSMALLINT *)Value) = (SQLSMALLINT)ival;
            break;
        case SQL_IS_POINTER:
            len = sizeof(SQLPOINTER);
            *((void **)Value) = ptr;
            break;
        case SQL_NTS:
            break;
    }

    if (StringLength)
        *StringLength = len;
    return ret;
}

static RETCODE SQL_API IPDGetField(DescriptorClass *desc, SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength,
                                   SQLINTEGER *StringLength) {
    UNUSED(BufferLength);
    RETCODE ret = SQL_SUCCESS;
    SQLINTEGER ival = 0, len = 0, rettype = 0;
    PTR ptr = NULL;
    const IPDFields *ipdopts = (const IPDFields *)&(desc->ipdf);
    SQLSMALLINT para_idx;

    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_STATUS_PTR:
        case SQL_DESC_ROWS_PROCESSED_PTR:
        case SQL_DESC_COUNT:
            break;
        default:
            if (RecNumber <= 0 || RecNumber > ipdopts->allocated) {
                MYLOG(ES_ALL, "RecN=%d allocated=%d\n", RecNumber,
                      ipdopts->allocated);
                DC_set_error(desc, DESC_BAD_PARAMETER_NUMBER_ERROR,
                             "bad parameter number");
                return SQL_ERROR;
            }
    }
    para_idx = RecNumber - 1;
    switch (FieldIdentifier) {
        case SQL_DESC_ARRAY_STATUS_PTR:
            rettype = SQL_IS_POINTER;
            ptr = ipdopts->param_status_ptr;
            break;
        case SQL_DESC_ROWS_PROCESSED_PTR:
            rettype = SQL_IS_POINTER;
            ptr = ipdopts->param_processed_ptr;
            break;
        case SQL_DESC_UNNAMED:
            rettype = SQL_IS_SMALLINT;
            ival = NAME_IS_NULL(ipdopts->parameters[para_idx].paramName)
                       ? SQL_UNNAMED
                       : SQL_NAMED;
            break;
        case SQL_DESC_TYPE:
            rettype = SQL_IS_SMALLINT;
            switch (ipdopts->parameters[para_idx].SQLType) {
                case SQL_TYPE_DATE:
                case SQL_TYPE_TIME:
                case SQL_TYPE_TIMESTAMP:
                    ival = SQL_DATETIME;
                    break;
                default:
                    ival = ipdopts->parameters[para_idx].SQLType;
            }
            break;
        case SQL_DESC_DATETIME_INTERVAL_CODE:
            rettype = SQL_IS_SMALLINT;
            switch (ipdopts->parameters[para_idx].SQLType) {
                case SQL_TYPE_DATE:
                    ival = SQL_CODE_DATE;
                    break;
                case SQL_TYPE_TIME:
                    ival = SQL_CODE_TIME;
                    break;
                case SQL_TYPE_TIMESTAMP:
                    ival = SQL_CODE_TIMESTAMP;
                    break;
                default:
                    ival = 0;
            }
            break;
        case SQL_DESC_CONCISE_TYPE:
            rettype = SQL_IS_SMALLINT;
            ival = ipdopts->parameters[para_idx].SQLType;
            break;
        case SQL_DESC_COUNT:
            rettype = SQL_IS_SMALLINT;
            ival = ipdopts->allocated;
            break;
        case SQL_DESC_PARAMETER_TYPE:
            rettype = SQL_IS_SMALLINT;
            ival = ipdopts->parameters[para_idx].paramType;
            break;
        case SQL_DESC_PRECISION:
            rettype = SQL_IS_SMALLINT;
            switch (ipdopts->parameters[para_idx].SQLType) {
                case SQL_TYPE_DATE:
                case SQL_TYPE_TIME:
                case SQL_TYPE_TIMESTAMP:
                case SQL_DATETIME:
                    ival = ipdopts->parameters[para_idx].decimal_digits;
                    break;
            }
            break;
        case SQL_DESC_SCALE:
            rettype = SQL_IS_SMALLINT;
            switch (ipdopts->parameters[para_idx].SQLType) {
                case SQL_NUMERIC:
                    ival = ipdopts->parameters[para_idx].decimal_digits;
                    break;
            }
            break;
        case SQL_DESC_ALLOC_TYPE: /* read-only */
            rettype = SQL_IS_SMALLINT;
            ival = SQL_DESC_ALLOC_AUTO;
            break;
        case SQL_DESC_CASE_SENSITIVE: /* read-only */
        case SQL_DESC_DATETIME_INTERVAL_PRECISION:
        case SQL_DESC_FIXED_PREC_SCALE: /* read-only */
        case SQL_DESC_LENGTH:
        case SQL_DESC_LOCAL_TYPE_NAME: /* read-only */
        case SQL_DESC_NAME:
        case SQL_DESC_NULLABLE: /* read-only */
        case SQL_DESC_NUM_PREC_RADIX:
        case SQL_DESC_OCTET_LENGTH:
        case SQL_DESC_ROWVER:    /* read-only */
        case SQL_DESC_TYPE_NAME: /* read-only */
        case SQL_DESC_UNSIGNED:  /* read-only */
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INVALID_DESCRIPTOR_IDENTIFIER,
                         "invalid descriptor identifier");
    }
    switch (rettype) {
        case 0:
        case SQL_IS_INTEGER:
            len = sizeof(SQLINTEGER);
            *((SQLINTEGER *)Value) = ival;
            break;
        case SQL_IS_SMALLINT:
            len = sizeof(SQLSMALLINT);
            *((SQLSMALLINT *)Value) = (SQLSMALLINT)ival;
            break;
        case SQL_IS_POINTER:
            len = sizeof(SQLPOINTER);
            *((void **)Value) = ptr;
            break;
    }

    if (StringLength)
        *StringLength = len;
    return ret;
}

/*	SQLGetStmtOption -> SQLGetStmtAttr */
RETCODE SQL_API ESAPI_GetStmtAttr(HSTMT StatementHandle, SQLINTEGER Attribute,
                                  PTR Value, SQLINTEGER BufferLength,
                                  SQLINTEGER *StringLength) {
    CSTR func = "ESAPI_GetStmtAttr";
    StatementClass *stmt = (StatementClass *)StatementHandle;
    RETCODE ret = SQL_SUCCESS;
    SQLINTEGER len = 0;

    MYLOG(ES_TRACE, "entering Handle=%p " FORMAT_INTEGER "\n", StatementHandle,
          Attribute);
    switch (Attribute) {
        case SQL_ATTR_FETCH_BOOKMARK_PTR: /* 16 */
            *((void **)Value) = stmt->options.bookmark_ptr;
            len = sizeof(SQLPOINTER);
            break;
        case SQL_ATTR_PARAM_BIND_OFFSET_PTR: /* 17 */
            *((SQLULEN **)Value) = SC_get_APDF(stmt)->param_offset_ptr;
            len = sizeof(SQLPOINTER);
            break;
        case SQL_ATTR_PARAM_BIND_TYPE: /* 18 */
            *((SQLUINTEGER *)Value) = SC_get_APDF(stmt)->param_bind_type;
            len = sizeof(SQLUINTEGER);
            break;
        case SQL_ATTR_PARAM_OPERATION_PTR: /* 19 */
            *((SQLUSMALLINT **)Value) = SC_get_APDF(stmt)->param_operation_ptr;
            len = sizeof(SQLPOINTER);
            break;
        case SQL_ATTR_PARAM_STATUS_PTR: /* 20 */
            *((SQLUSMALLINT **)Value) = SC_get_IPDF(stmt)->param_status_ptr;
            len = sizeof(SQLPOINTER);
            break;
        case SQL_ATTR_PARAMS_PROCESSED_PTR: /* 21 */
            *((SQLULEN **)Value) = SC_get_IPDF(stmt)->param_processed_ptr;
            len = sizeof(SQLPOINTER);
            break;
        case SQL_ATTR_PARAMSET_SIZE: /* 22 */
            *((SQLULEN *)Value) = SC_get_APDF(stmt)->paramset_size;
            len = sizeof(SQLUINTEGER);
            break;
        case SQL_ATTR_ROW_BIND_OFFSET_PTR: /* 23 */
            *((SQLULEN **)Value) = SC_get_ARDF(stmt)->row_offset_ptr;
            len = 4;
            break;
        case SQL_ATTR_ROW_OPERATION_PTR: /* 24 */
            *((SQLUSMALLINT **)Value) = SC_get_ARDF(stmt)->row_operation_ptr;
            len = 4;
            break;
        case SQL_ATTR_ROW_STATUS_PTR: /* 25 */
            *((SQLUSMALLINT **)Value) = SC_get_IRDF(stmt)->rowStatusArray;
            len = 4;
            break;
        case SQL_ATTR_ROWS_FETCHED_PTR: /* 26 */
            *((SQLULEN **)Value) = SC_get_IRDF(stmt)->rowsFetched;
            len = 4;
            break;
        case SQL_ATTR_ROW_ARRAY_SIZE: /* 27 */
            *((SQLULEN *)Value) = SC_get_ARDF(stmt)->size_of_rowset;
            len = 4;
            break;
        case SQL_ATTR_APP_ROW_DESC:   /* 10010 */
        case SQL_ATTR_APP_PARAM_DESC: /* 10011 */
        case SQL_ATTR_IMP_ROW_DESC:   /* 10012 */
        case SQL_ATTR_IMP_PARAM_DESC: /* 10013 */
            len = 4;
            *((HSTMT *)Value) =
                descHandleFromStatementHandle(StatementHandle, Attribute);
            break;

        case SQL_ATTR_CURSOR_SCROLLABLE: /* -1 */
            len = 4;
            if (SQL_CURSOR_FORWARD_ONLY == stmt->options.cursor_type)
                *((SQLUINTEGER *)Value) = SQL_NONSCROLLABLE;
            else
                *((SQLUINTEGER *)Value) = SQL_SCROLLABLE;
            break;
        case SQL_ATTR_CURSOR_SENSITIVITY: /* -2 */
            len = 4;
            if (SQL_CONCUR_READ_ONLY == stmt->options.scroll_concurrency)
                *((SQLUINTEGER *)Value) = SQL_INSENSITIVE;
            else
                *((SQLUINTEGER *)Value) = SQL_UNSPECIFIED;
            break;
        case SQL_ATTR_METADATA_ID: /* 10014 */
            *((SQLUINTEGER *)Value) = stmt->options.metadata_id;
            break;
        case SQL_ATTR_ENABLE_AUTO_IPD: /* 15 */
            *((SQLUINTEGER *)Value) = SQL_FALSE;
            break;
        case SQL_ATTR_AUTO_IPD: /* 10001 */
            /* case SQL_ATTR_ROW_BIND_TYPE: ** == SQL_BIND_TYPE(ODBC2.0) */
            SC_set_error(stmt, DESC_INVALID_OPTION_IDENTIFIER,
                         "Unsupported statement option (Get)", func);
            return SQL_ERROR;
        default:
            ret = ESAPI_GetStmtOption(StatementHandle, (SQLSMALLINT)Attribute,
                                      Value, &len, BufferLength);
    }
    if (ret == SQL_SUCCESS && StringLength)
        *StringLength = len;
    return ret;
}

/*	SQLSetConnectOption -> SQLSetConnectAttr */
RETCODE SQL_API ESAPI_SetConnectAttr(HDBC ConnectionHandle,
                                     SQLINTEGER Attribute, PTR Value,
                                     SQLINTEGER StringLength) {
    UNUSED(StringLength);
    CSTR func = "ESAPI_SetConnectAttr";
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;
    RETCODE ret = SQL_SUCCESS;
    BOOL unsupported = FALSE;
    int newValue;

    MYLOG(ES_TRACE, "entering for %p: " FORMAT_INTEGER " %p\n",
          ConnectionHandle, Attribute, Value);
    switch (Attribute) {
        case SQL_ATTR_METADATA_ID:
            conn->stmtOptions.metadata_id = CAST_UPTR(SQLUINTEGER, Value);
            break;
        case SQL_ATTR_ANSI_APP:
            if (SQL_AA_FALSE != CAST_PTR(SQLINTEGER, Value)) {
                MYLOG(ES_DEBUG, "the application is ansi\n");
                if (CC_is_in_unicode_driver(conn)) /* the driver is unicode */
                    CC_set_in_ansi_app(conn);      /* but the app is ansi */
            } else {
                MYLOG(ES_DEBUG, "the application is unicode\n");
            }
            /*return SQL_ERROR;*/
            return SQL_SUCCESS;
        case SQL_ATTR_ENLIST_IN_DTC:
            unsupported = TRUE;
            break;
        case SQL_ATTR_AUTO_IPD:
            if (SQL_FALSE != Value)
                unsupported = TRUE;
            break;
        case SQL_ATTR_ASYNC_ENABLE:
        case SQL_ATTR_CONNECTION_DEAD:
        case SQL_ATTR_CONNECTION_TIMEOUT:
            unsupported = TRUE;
            break;
        case SQL_ATTR_ESOPT_DEBUG:
            newValue = CAST_UPTR(SQLCHAR, Value);
            if (newValue > 0) {
                logs_on_off(-1, conn->connInfo.drivers.loglevel, 0);
                conn->connInfo.drivers.loglevel = (char)newValue;
                logs_on_off(1, conn->connInfo.drivers.loglevel, 0);
                MYLOG(ES_DEBUG, "debug => %d\n",
                      conn->connInfo.drivers.loglevel);
            } else if (newValue == 0 && conn->connInfo.drivers.loglevel > 0) {
                MYLOG(ES_DEBUG, "debug => %d\n", newValue);
                logs_on_off(-1, conn->connInfo.drivers.loglevel, 0);
                conn->connInfo.drivers.loglevel = (char)newValue;
                logs_on_off(1, 0, 0);
            }
            break;
        case SQL_ATTR_ESOPT_COMMLOG:
            newValue = CAST_UPTR(SQLCHAR, Value);
            if (newValue > 0) {
                logs_on_off(-1, 0, conn->connInfo.drivers.loglevel);
                conn->connInfo.drivers.loglevel = (char)newValue;
                logs_on_off(1, 0, conn->connInfo.drivers.loglevel);
                MYLOG(ES_DEBUG, "commlog => %d\n",
                      conn->connInfo.drivers.loglevel);
            } else if (newValue == 0 && conn->connInfo.drivers.loglevel > 0) {
                MYLOG(ES_DEBUG, "commlog => %d\n", newValue);
                logs_on_off(-1, 0, conn->connInfo.drivers.loglevel);
                conn->connInfo.drivers.loglevel = (char)newValue;
                logs_on_off(1, 0, 0);
            }
            break;
        default:
            if (Attribute < 65536)
                ret = ESAPI_SetConnectOption(
                    ConnectionHandle, (SQLUSMALLINT)Attribute, (SQLLEN)Value);
            else
                unsupported = TRUE;
    }
    if (unsupported) {
        char msg[64];
        SPRINTF_FIXED(
            msg, "Couldn't set unsupported connect attribute " FORMAT_INTEGER,
            Attribute);
        CC_set_error(conn, CONN_OPTION_NOT_FOR_THE_DRIVER, msg, func);
        return SQL_ERROR;
    }
    return ret;
}

/*	new function */
RETCODE SQL_API ESAPI_GetDescField(SQLHDESC DescriptorHandle,
                                   SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength,
                                   SQLINTEGER *StringLength) {
    CSTR func = "ESAPI_GetDescField";
    RETCODE ret = SQL_SUCCESS;
    DescriptorClass *desc = (DescriptorClass *)DescriptorHandle;

    MYLOG(ES_TRACE,
          "entering h=%p rec=" FORMAT_SMALLI " field=" FORMAT_SMALLI
          " blen=" FORMAT_INTEGER "\n",
          DescriptorHandle, RecNumber, FieldIdentifier, BufferLength);
    switch (DC_get_desc_type(desc)) {
        case SQL_ATTR_APP_ROW_DESC:
            ret = ARDGetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength, StringLength);
            break;
        case SQL_ATTR_APP_PARAM_DESC:
            ret = APDGetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength, StringLength);
            break;
        case SQL_ATTR_IMP_ROW_DESC:
            ret = IRDGetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength, StringLength);
            break;
        case SQL_ATTR_IMP_PARAM_DESC:
            ret = IPDGetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength, StringLength);
            break;
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INTERNAL_ERROR, "Error not implemented");
    }
    if (ret == SQL_ERROR) {
        if (!DC_get_errormsg(desc)) {
            switch (DC_get_errornumber(desc)) {
                case DESC_INVALID_DESCRIPTOR_IDENTIFIER:
                    DC_set_errormsg(
                        desc,
                        "can't SQLGetDescField for this descriptor identifier");
                    break;
                case DESC_INVALID_COLUMN_NUMBER_ERROR:
                    DC_set_errormsg(
                        desc, "can't SQLGetDescField for this column number");
                    break;
                case DESC_BAD_PARAMETER_NUMBER_ERROR:
                    DC_set_errormsg(
                        desc,
                        "can't SQLGetDescField for this parameter number");
                    break;
            }
        }
        DC_log_error(func, "", desc);
    }
    return ret;
}

/*	new function */
RETCODE SQL_API ESAPI_SetDescField(SQLHDESC DescriptorHandle,
                                   SQLSMALLINT RecNumber,
                                   SQLSMALLINT FieldIdentifier, PTR Value,
                                   SQLINTEGER BufferLength) {
    CSTR func = "ESAPI_SetDescField";
    RETCODE ret = SQL_SUCCESS;
    DescriptorClass *desc = (DescriptorClass *)DescriptorHandle;

    MYLOG(ES_TRACE,
          "entering h=%p(%d) rec=" FORMAT_SMALLI " field=" FORMAT_SMALLI
          " val=%p," FORMAT_INTEGER "\n",
          DescriptorHandle, DC_get_desc_type(desc), RecNumber, FieldIdentifier,
          Value, BufferLength);
    switch (DC_get_desc_type(desc)) {
        case SQL_ATTR_APP_ROW_DESC:
            ret = ARDSetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength);
            break;
        case SQL_ATTR_APP_PARAM_DESC:
            ret = APDSetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength);
            break;
        case SQL_ATTR_IMP_ROW_DESC:
            ret = IRDSetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength);
            break;
        case SQL_ATTR_IMP_PARAM_DESC:
            ret = IPDSetField(desc, RecNumber, FieldIdentifier, Value,
                              BufferLength);
            break;
        default:
            ret = SQL_ERROR;
            DC_set_error(desc, DESC_INTERNAL_ERROR, "Error not implemented");
    }
    if (ret == SQL_ERROR) {
        if (!DC_get_errormsg(desc)) {
            switch (DC_get_errornumber(desc)) {
                case DESC_INVALID_DESCRIPTOR_IDENTIFIER:
                    DC_set_errormsg(
                        desc,
                        "can't SQLSetDescField for this descriptor identifier");
                    break;
                case DESC_INVALID_COLUMN_NUMBER_ERROR:
                    DC_set_errormsg(
                        desc, "can't SQLSetDescField for this column number");
                    break;
                case DESC_BAD_PARAMETER_NUMBER_ERROR:
                    DC_set_errormsg(
                        desc,
                        "can't SQLSetDescField for this parameter number");
                    break;
                    break;
            }
        }
        DC_log_error(func, "", desc);
    }
    return ret;
}

/*	SQLSet(Param/Scroll/Stmt)Option -> SQLSetStmtAttr */
RETCODE SQL_API ESAPI_SetStmtAttr(HSTMT StatementHandle, SQLINTEGER Attribute,
                                  PTR Value, SQLINTEGER StringLength) {
    UNUSED(StringLength);
    RETCODE ret = SQL_SUCCESS;
    CSTR func = "ESAPI_SetStmtAttr";
    StatementClass *stmt = (StatementClass *)StatementHandle;

    MYLOG(ES_TRACE,
          "entering Handle=%p " FORMAT_INTEGER "," FORMAT_ULEN "(%p)\n",
          StatementHandle, Attribute, (SQLULEN)Value, Value);
    switch (Attribute) {
        case SQL_ATTR_ENABLE_AUTO_IPD: /* 15 */
            if (SQL_FALSE == Value)
                break;
        case SQL_ATTR_CURSOR_SCROLLABLE:  /* -1 */
        case SQL_ATTR_CURSOR_SENSITIVITY: /* -2 */
        case SQL_ATTR_AUTO_IPD:           /* 10001 */
            SC_set_error(stmt, DESC_OPTION_NOT_FOR_THE_DRIVER,
                         "Unsupported statement option (Set)", func);
            return SQL_ERROR;
        /* case SQL_ATTR_ROW_BIND_TYPE: ** == SQL_BIND_TYPE(ODBC2.0) */
        case SQL_ATTR_IMP_ROW_DESC:   /* 10012 (read-only) */
        case SQL_ATTR_IMP_PARAM_DESC: /* 10013 (read-only) */

            /*
             * case SQL_ATTR_PREDICATE_PTR: case
             * SQL_ATTR_PREDICATE_OCTET_LENGTH_PTR:
             */
            SC_set_error(stmt, DESC_INVALID_OPTION_IDENTIFIER,
                         "Unsupported statement option (Set)", func);
            return SQL_ERROR;

        case SQL_ATTR_METADATA_ID: /* 10014 */
            stmt->options.metadata_id = CAST_UPTR(SQLUINTEGER, Value);
            break;
        case SQL_ATTR_APP_ROW_DESC: /* 10010 */
            if (SQL_NULL_HDESC == Value) {
                stmt->ard = &(stmt->ardi);
            } else {
                stmt->ard = (DescriptorClass *)Value;
                MYLOG(ES_ALL, "set ard=%p\n", stmt->ard);
            }
            break;
        case SQL_ATTR_APP_PARAM_DESC: /* 10011 */
            if (SQL_NULL_HDESC == Value) {
                stmt->apd = &(stmt->apdi);
            } else {
                stmt->apd = (DescriptorClass *)Value;
            }
            break;
        case SQL_ATTR_FETCH_BOOKMARK_PTR: /* 16 */
            stmt->options.bookmark_ptr = Value;
            break;
        case SQL_ATTR_PARAM_BIND_OFFSET_PTR: /* 17 */
            SC_get_APDF(stmt)->param_offset_ptr = (SQLULEN *)Value;
            break;
        case SQL_ATTR_PARAM_BIND_TYPE: /* 18 */
            SC_get_APDF(stmt)->param_bind_type = CAST_UPTR(SQLUINTEGER, Value);
            break;
        case SQL_ATTR_PARAM_OPERATION_PTR: /* 19 */
            SC_get_APDF(stmt)->param_operation_ptr = Value;
            break;
        case SQL_ATTR_PARAM_STATUS_PTR: /* 20 */
            SC_get_IPDF(stmt)->param_status_ptr = (SQLUSMALLINT *)Value;
            break;
        case SQL_ATTR_PARAMS_PROCESSED_PTR: /* 21 */
            SC_get_IPDF(stmt)->param_processed_ptr = (SQLULEN *)Value;
            break;
        case SQL_ATTR_PARAMSET_SIZE: /* 22 */
            SC_get_APDF(stmt)->paramset_size = CAST_UPTR(SQLULEN, Value);
            break;
        case SQL_ATTR_ROW_BIND_OFFSET_PTR: /* 23 */
            SC_get_ARDF(stmt)->row_offset_ptr = (SQLULEN *)Value;
            break;
        case SQL_ATTR_ROW_OPERATION_PTR: /* 24 */
            SC_get_ARDF(stmt)->row_operation_ptr = Value;
            break;
        case SQL_ATTR_ROW_STATUS_PTR: /* 25 */
            SC_get_IRDF(stmt)->rowStatusArray = (SQLUSMALLINT *)Value;
            break;
        case SQL_ATTR_ROWS_FETCHED_PTR: /* 26 */
            SC_get_IRDF(stmt)->rowsFetched = (SQLULEN *)Value;
            break;
        case SQL_ATTR_ROW_ARRAY_SIZE: /* 27 */
            SC_get_ARDF(stmt)->size_of_rowset = CAST_UPTR(SQLULEN, Value);
            break;
        default:
            return ESAPI_SetStmtOption(StatementHandle, (SQLUSMALLINT)Attribute,
                                       (SQLULEN)Value);
    }
    return ret;
}
