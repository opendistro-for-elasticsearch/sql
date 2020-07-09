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

#include <limits.h>
#include <stdio.h>
#include <string.h>

#include "bind.h"
#include "convert.h"
#include "dlg_specific.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_odbc.h"
#include "es_types.h"
#include "misc.h"
#include "qresult.h"
#include "statement.h"
#include "es_statement.h"

/*	Helper macro */
#define getEffectiveOid(conn, fi) \
    es_true_type((conn), (fi)->columntype, FI_type(fi))
#define NULL_IF_NULL(a) ((a) ? ((const char *)(a)) : "(null)")

RETCODE SQL_API ESAPI_RowCount(HSTMT hstmt, SQLLEN *pcrow) {
    CSTR func = "ESAPI_RowCount";
    StatementClass *stmt = (StatementClass *)hstmt;
    QResultClass *res;

    MYLOG(ES_TRACE, "entering...\n");
    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    res = SC_get_Curres(stmt);
    if (res) {
        if (stmt->status != STMT_FINISHED) {
            SC_set_error(
                stmt, STMT_SEQUENCE_ERROR,
                "Can't get row count while statement is still executing.",
                func);
            return SQL_ERROR;
        }
    }

    // Row count is not supported by this driver, so we will always report -1,
    // as defined by the ODBC API for SQLRowCount.
    *pcrow = -1;

    return SQL_SUCCESS;
}

/*
 *	This returns the number of columns associated with the database
 *	attached to "hstmt".
 */
RETCODE SQL_API ESAPI_NumResultCols(HSTMT hstmt, SQLSMALLINT *pccol) {
    CSTR func = "ESAPI_NumResultCols";
    StatementClass *stmt = (StatementClass *)hstmt;
    QResultClass *result;
    RETCODE ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");
    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    SC_clear_error(stmt);
#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wkeyword-macro"
#endif  // __APPLE__
#define return DONT_CALL_RETURN_FROM_HERE ? ? ?
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    if (stmt->proc_return > 0) {
        *pccol = 0;
        goto cleanup;
    }

    result = SC_get_Curres(stmt);
    *pccol = QR_NumPublicResultCols(result);

cleanup:
#undef return
    return ret;
}

#define USE_FI(fi, unknown) (fi && UNKNOWNS_AS_LONGEST != unknown)

/*
 *	Return information about the database column the user wants
 *	information about.
 */
RETCODE SQL_API ESAPI_DescribeCol(HSTMT hstmt, SQLUSMALLINT icol,
                                  SQLCHAR *szColName, SQLSMALLINT cbColNameMax,
                                  SQLSMALLINT *pcbColName,
                                  SQLSMALLINT *pfSqlType, SQLULEN *pcbColDef,
                                  SQLSMALLINT *pibScale,
                                  SQLSMALLINT *pfNullable) {
    CSTR func = "ESAPI_DescribeCol";

    /* gets all the information about a specific column */
    StatementClass *stmt = (StatementClass *)hstmt;
    ConnectionClass *conn;
    IRDFields *irdflds;
    QResultClass *res = NULL;
    char *col_name = NULL;
    OID fieldtype = 0;
    SQLLEN column_size = 0;
    int unknown_sizes;
    SQLINTEGER decimal_digits = 0;
    ConnInfo *ci;
    FIELD_INFO *fi;
    char buf[255];
    int len = 0;
    RETCODE result = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering.%d..\n", icol);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    conn = SC_get_conn(stmt);
    ci = &(conn->connInfo);
    unknown_sizes = DEFAULT_UNKNOWNSIZES;

    SC_clear_error(stmt);

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wkeyword-macro"
#endif  // __APPLE__
#define return DONT_CALL_RETURN_FROM_HERE ? ? ?
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    irdflds = SC_get_IRDF(stmt);
    if (0 == icol) /* bookmark column */
    {
        SQLSMALLINT fType = stmt->options.use_bookmarks == SQL_UB_VARIABLE
                                ? SQL_BINARY
                                : SQL_INTEGER;

        MYLOG(ES_ALL, "answering bookmark info\n");
        if (szColName && cbColNameMax > 0)
            *szColName = '\0';
        if (pcbColName)
            *pcbColName = 0;
        if (pfSqlType)
            *pfSqlType = fType;
        if (pcbColDef)
            *pcbColDef = 10;
        if (pibScale)
            *pibScale = 0;
        if (pfNullable)
            *pfNullable = SQL_NO_NULLS;
        result = SQL_SUCCESS;
        goto cleanup;
    }

    /*
     * Dont check for bookmark column. This is the responsibility of the
     * driver manager.
     */

    icol--; /* use zero based column numbers */

    fi = NULL;
    if (icol < irdflds->nfields && irdflds->fi)
        fi = irdflds->fi[icol];

    if (!FI_is_applicable(fi)) {
        fi = NULL;

        res = SC_get_Curres(stmt);
        if (icol >= QR_NumPublicResultCols(res)) {
            SC_set_error(stmt, STMT_INVALID_COLUMN_NUMBER_ERROR,
                         "Invalid column number in DescribeCol.", func);
            SPRINTF_FIXED(buf, "Col#=%d, #Cols=%d,%d keys=%d", icol,
                          QR_NumResultCols(res), QR_NumPublicResultCols(res),
                          res->num_key_fields);
            SC_log_error(func, buf, stmt);
            result = SQL_ERROR;
            goto cleanup;
        }
        if (icol < irdflds->nfields && irdflds->fi)
            fi = irdflds->fi[icol];
    }
    res = SC_get_Curres(stmt);
#ifdef SUPPRESS_LONGEST_ON_CURSORS
    if (UNKNOWNS_AS_LONGEST == unknown_sizes) {
        if (QR_once_reached_eof(res))
            unknown_sizes = UNKNOWNS_AS_LONGEST;
        else
            unknown_sizes = UNKNOWNS_AS_MAX;
    }
#endif /* SUPPRESS_LONGEST_ON_CURSORS */
    /* handle constants */
    if (res && -2 == QR_get_fieldsize(res, icol))
        unknown_sizes = UNKNOWNS_AS_LONGEST;

    if (FI_is_applicable(fi)) {
        fieldtype = getEffectiveOid(conn, fi);
        if (NAME_IS_VALID(fi->column_alias))
            col_name = GET_NAME(fi->column_alias);
        else
            col_name = GET_NAME(fi->column_name);
        if (USE_FI(fi, unknown_sizes)) {
            column_size = fi->column_size;
            decimal_digits = fi->decimal_digits;
        } else {
            column_size =
                estype_column_size(stmt, fieldtype, icol, unknown_sizes);
            decimal_digits = estype_decimal_digits(stmt, fieldtype, icol);
        }

        MYLOG(ES_DEBUG,
              "PARSE: fieldtype=%u, col_name='%s', column_size=" FORMAT_LEN
              "\n",
              fieldtype, NULL_IF_NULL(col_name), column_size);
    } else {
        col_name = QR_get_fieldname(res, icol);
        fieldtype = QR_get_field_type(res, icol);

        column_size = estype_column_size(stmt, fieldtype, icol, unknown_sizes);
        decimal_digits = estype_decimal_digits(stmt, fieldtype, icol);
    }

    MYLOG(ES_DEBUG, "col %d fieldname = '%s'\n", icol, NULL_IF_NULL(col_name));
    MYLOG(ES_DEBUG, "col %d fieldtype = %d\n", icol, fieldtype);
    MYLOG(ES_DEBUG, "col %d column_size = " FORMAT_LEN "\n", icol, column_size);

    result = SQL_SUCCESS;

    /*
     * COLUMN NAME
     */
    len = col_name ? (int)strlen(col_name) : 0;

    if (pcbColName)
        *pcbColName = (SQLSMALLINT)len;

    if (szColName && cbColNameMax > 0) {
        if (NULL != col_name)
            strncpy_null((char *)szColName, col_name, cbColNameMax);
        else
            szColName[0] = '\0';

        if (len >= cbColNameMax) {
            result = SQL_SUCCESS_WITH_INFO;
            SC_set_error(stmt, STMT_TRUNCATED,
                         "The buffer was too small for the colName.", func);
        }
    }

    /*
     * CONCISE(SQL) TYPE
     */
    if (pfSqlType) {
        *pfSqlType =
            estype_to_concise_type(stmt, fieldtype, icol, unknown_sizes);

        MYLOG(ES_DEBUG, "col %d *pfSqlType = %d\n", icol, *pfSqlType);
    }

    /*
     * COLUMN SIZE(PRECISION in 2.x)
     */
    if (pcbColDef) {
        if (column_size < 0)
            column_size = 0; /* "I dont know" */

        *pcbColDef = column_size;

        MYLOG(ES_DEBUG, "Col: col %d  *pcbColDef = " FORMAT_ULEN "\n", icol,
              *pcbColDef);
    }

    /*
     * DECIMAL DIGITS(SCALE in 2.x)
     */
    if (pibScale) {
        if (decimal_digits < 0)
            decimal_digits = 0;

        *pibScale = (SQLSMALLINT)decimal_digits;
        MYLOG(ES_DEBUG, "col %d  *pibScale = %d\n", icol, *pibScale);
    }

    /*
     * NULLABILITY
     */
    if (pfNullable) {
        if (SC_has_outer_join(stmt))
            *pfNullable = TRUE;
        else
            *pfNullable = fi ? fi->nullable : estype_nullable(conn, fieldtype);

        MYLOG(ES_DEBUG, "col %d  *pfNullable = %d\n", icol, *pfNullable);
    }

cleanup:
#undef return
    return result;
}

/*		Returns result column descriptor information for a result set. */
RETCODE SQL_API ESAPI_ColAttributes(HSTMT hstmt, SQLUSMALLINT icol,
                                    SQLUSMALLINT fDescType, PTR rgbDesc,
                                    SQLSMALLINT cbDescMax, SQLSMALLINT *pcbDesc,
                                    SQLLEN *pfDesc) {
    CSTR func = "ESAPI_ColAttributes";
    StatementClass *stmt = (StatementClass *)hstmt;
    IRDFields *irdflds;
    OID field_type = 0;
    Int2 col_idx;
    ConnectionClass *conn;
    ConnInfo *ci;
    int column_size, unknown_sizes;
    int cols = 0;
    RETCODE result;
    const char *p = NULL;
    SQLLEN value = 0;
    const FIELD_INFO *fi = NULL;
    const TABLE_INFO *ti = NULL;
    QResultClass *res;
    BOOL stmt_updatable;

    MYLOG(ES_TRACE, "entering..col=%d %d len=%d.\n", icol, fDescType,
          cbDescMax);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }
    stmt_updatable = SC_is_updatable(stmt)
        /* The following doesn't seem appropriate for client side cursors
          && stmt->options.scroll_concurrency != SQL_CONCUR_READ_ONLY
         */
        ;

    if (pcbDesc)
        *pcbDesc = 0;
    irdflds = SC_get_IRDF(stmt);
    conn = SC_get_conn(stmt);
    ci = &(conn->connInfo);

    /*
     * Dont check for bookmark column.	This is the responsibility of the
     * driver manager.	For certain types of arguments, the column number
     * is ignored anyway, so it may be 0.
     */

    res = SC_get_Curres(stmt);
    if (0 == icol && SQL_DESC_COUNT != fDescType) /* bookmark column */
    {
        MYLOG(ES_ALL, "answering bookmark info\n");
        switch (fDescType) {
            case SQL_DESC_OCTET_LENGTH:
                if (pfDesc)
                    *pfDesc = 4;
                break;
            case SQL_DESC_TYPE:
                if (pfDesc)
                    *pfDesc = stmt->options.use_bookmarks == SQL_UB_VARIABLE
                                  ? SQL_BINARY
                                  : SQL_INTEGER;
                break;
        }
        return SQL_SUCCESS;
    }

    col_idx = icol - 1;

    unknown_sizes = DEFAULT_UNKNOWNSIZES;

    /* not appropriate for SQLColAttributes() */
    if (stmt->catalog_result)
        unknown_sizes = UNKNOWNS_AS_LONGEST;
    else if (unknown_sizes == UNKNOWNS_AS_DONTKNOW)
        unknown_sizes = UNKNOWNS_AS_MAX;

    if (!stmt->catalog_result && SC_is_parse_forced(stmt)
        && SC_can_parse_statement(stmt)) {
        cols = irdflds->nfields;

        /*
         * Column Count is a special case.	The Column number is ignored
         * in this case.
         */
        if (fDescType == SQL_DESC_COUNT) {
            if (pfDesc)
                *pfDesc = cols;

            return SQL_SUCCESS;
        }

        if (SC_parsed_status(stmt) != STMT_PARSE_FATAL && irdflds->fi) {
            if (col_idx >= cols) {
                SC_set_error(stmt, STMT_INVALID_COLUMN_NUMBER_ERROR,
                             "Invalid column number in ColAttributes.", func);
                return SQL_ERROR;
            }
        }
    }

    if ((unsigned int)col_idx < irdflds->nfields && irdflds->fi)
        fi = irdflds->fi[col_idx];
    if (FI_is_applicable(fi))
        field_type = getEffectiveOid(conn, fi);
    else {
        BOOL build_fi = FALSE;

        fi = NULL;
        switch (fDescType) {
            case SQL_COLUMN_OWNER_NAME:
            case SQL_COLUMN_TABLE_NAME:
            case SQL_COLUMN_TYPE:
            case SQL_COLUMN_TYPE_NAME:
            case SQL_COLUMN_AUTO_INCREMENT:
            case SQL_DESC_NULLABLE:
            case SQL_DESC_BASE_TABLE_NAME:
            case SQL_DESC_BASE_COLUMN_NAME:
            case SQL_COLUMN_UPDATABLE:
            case 1212: /* SQL_CA_SS_COLUMN_KEY ? */
                build_fi = TRUE;
                break;
        }

        res = SC_get_Curres(stmt);
        cols = QR_NumPublicResultCols(res);

        /*
         * Column Count is a special case.	The Column number is ignored
         * in this case.
         */
        if (fDescType == SQL_DESC_COUNT) {
            if (pfDesc)
                *pfDesc = cols;

            return SQL_SUCCESS;
        }

        if (col_idx >= cols) {
            SC_set_error(stmt, STMT_INVALID_COLUMN_NUMBER_ERROR,
                         "Invalid column number in ColAttributes.", func);
            return SQL_ERROR;
        }

        field_type = QR_get_field_type(res, col_idx);
        if ((unsigned int)col_idx < irdflds->nfields && irdflds->fi)
            fi = irdflds->fi[col_idx];
    }
    if (FI_is_applicable(fi)) {
        ti = fi->ti;
        field_type = getEffectiveOid(conn, fi);
    }

    MYLOG(ES_DEBUG, "col %d field_type=%d fi,ti=%p,%p\n", col_idx, field_type,
          fi, ti);

#ifdef SUPPRESS_LONGEST_ON_CURSORS
    if (UNKNOWNS_AS_LONGEST == unknown_sizes) {
        if (QR_once_reached_eof(res))
            unknown_sizes = UNKNOWNS_AS_LONGEST;
        else
            unknown_sizes = UNKNOWNS_AS_MAX;
    }
#endif /* SUPPRESS_LONGEST_ON_CURSORS */
    /* handle constants */
    if (res && -2 == QR_get_fieldsize(res, col_idx))
        unknown_sizes = UNKNOWNS_AS_LONGEST;

    column_size =
        (USE_FI(fi, unknown_sizes) && fi->column_size > 0)
            ? fi->column_size
            : estype_column_size(stmt, field_type, col_idx, unknown_sizes);
    switch (fDescType) {
        case SQL_COLUMN_AUTO_INCREMENT: /* == SQL_DESC_AUTO_UNIQUE_VALUE */
            if (fi && fi->auto_increment)
                value = TRUE;
            else
                value = estype_auto_increment(conn, field_type);
            if (value == -1) /* non-numeric becomes FALSE (ODBC Doc) */
                value = FALSE;
            MYLOG(ES_DEBUG, "AUTO_INCREMENT=" FORMAT_LEN "\n", value);

            break;

        case SQL_COLUMN_CASE_SENSITIVE: /* == SQL_DESC_CASE_SENSITIVE */
            value = estype_case_sensitive(conn, field_type);
            break;

            /*
             * This special case is handled above.
             *
             * case SQL_COLUMN_COUNT:
             */
        case SQL_COLUMN_DISPLAY_SIZE: /* == SQL_DESC_DISPLAY_SIZE */
            value = (USE_FI(fi, unknown_sizes) && 0 != fi->display_size)
                        ? fi->display_size
                        : estype_display_size(stmt, field_type, col_idx,
                                              unknown_sizes);

            MYLOG(ES_DEBUG, "col %d, display_size= " FORMAT_LEN "\n", col_idx,
                  value);

            break;

        case SQL_COLUMN_LABEL: /* == SQL_DESC_LABEL */
            if (fi && (NAME_IS_VALID(fi->column_alias))) {
                p = GET_NAME(fi->column_alias);

                MYLOG(ES_DEBUG, "COLUMN_LABEL = '%s'\n", p);
                break;
            }
            /* otherwise same as column name -- FALL THROUGH!!! */

        case SQL_DESC_NAME:
            MYLOG(ES_ALL, "fi=%p (alias, name)=", fi);
            if (fi)
                MYPRINTF(ES_DEBUG, "(%s,%s)\n", PRINT_NAME(fi->column_alias),
                         PRINT_NAME(fi->column_name));
            else
                MYPRINTF(ES_DEBUG, "NULL\n");
            p = fi ? (NAME_IS_NULL(fi->column_alias)
                          ? SAFE_NAME(fi->column_name)
                          : GET_NAME(fi->column_alias))
                   : QR_get_fieldname(res, col_idx);

            MYLOG(ES_DEBUG, "COLUMN_NAME = '%s'\n", p);
            break;

        case SQL_COLUMN_LENGTH:
            value = (USE_FI(fi, unknown_sizes) && fi->length > 0)
                        ? fi->length
                        : estype_buffer_length(stmt, field_type, col_idx,
                                               unknown_sizes);
            if (0 > value)
                /* if (-1 == value)  I'm not sure which is right */
                value = 0;

            MYLOG(ES_DEBUG, "col %d, column_length = " FORMAT_LEN "\n", col_idx,
                  value);
            break;

        case SQL_COLUMN_MONEY: /* == SQL_DESC_FIXED_PREC_SCALE */
            value = estype_money(conn, field_type);
            MYLOG(ES_ALL, "COLUMN_MONEY=" FORMAT_LEN "\n", value);
            break;

        case SQL_DESC_NULLABLE:
            if (SC_has_outer_join(stmt))
                value = TRUE;
            else
                value = fi ? fi->nullable : estype_nullable(conn, field_type);
            MYLOG(ES_ALL, "COLUMN_NULLABLE=" FORMAT_LEN "\n", value);
            break;

        case SQL_COLUMN_OWNER_NAME: /* == SQL_DESC_SCHEMA_NAME */
            p = ti ? SAFE_NAME(ti->schema_name) : NULL_STRING;
            MYLOG(ES_DEBUG, "SCHEMA_NAME = '%s'\n", p);
            break;

        case SQL_COLUMN_PRECISION: /* in 2.x */
            value = column_size;
            if (value < 0)
                value = 0;

            MYLOG(ES_DEBUG, "col %d, column_size = " FORMAT_LEN "\n", col_idx,
                  value);
            break;

        case SQL_COLUMN_QUALIFIER_NAME: /* == SQL_DESC_CATALOG_NAME */
            p = ti ? CurrCatString(conn)
                   : NULL_STRING; /* empty string means *not supported* */
            break;

        case SQL_COLUMN_SCALE: /* in 2.x */
            value = estype_decimal_digits(stmt, field_type, col_idx);
            MYLOG(ES_ALL, "COLUMN_SCALE=" FORMAT_LEN "\n", value);
            if (value < 0)
                value = 0;
            break;

        case SQL_COLUMN_SEARCHABLE: /* == SQL_DESC_SEARCHABLE */
            value = estype_searchable(conn, field_type);
            break;

        case SQL_COLUMN_TABLE_NAME: /* == SQL_DESC_TABLE_NAME */
            p = ti ? SAFE_NAME(ti->table_name) : NULL_STRING;

            MYLOG(ES_DEBUG, "TABLE_NAME = '%s'\n", p);
            break;

        case SQL_COLUMN_TYPE: /* == SQL_DESC_CONCISE_TYPE */
            value = estype_to_concise_type(stmt, field_type, col_idx,
                                           unknown_sizes);
            MYLOG(ES_DEBUG, "COLUMN_TYPE=" FORMAT_LEN "\n", value);
            break;

        case SQL_COLUMN_TYPE_NAME: /* == SQL_DESC_TYPE_NAME */
            p = estype_to_name(stmt, field_type, col_idx,
                               fi && fi->auto_increment);
            break;

        case SQL_COLUMN_UNSIGNED: /* == SQL_DESC_UNSINGED */
            value = estype_unsigned(conn, field_type);
            if (value == -1) /* non-numeric becomes TRUE (ODBC Doc) */
                value = SQL_TRUE;

            break;

        case SQL_COLUMN_UPDATABLE: /* == SQL_DESC_UPDATABLE */

            /*
             * Neither Access or Borland care about this.
             *
             * if (field_type == ES_TYPE_OID) pfDesc = SQL_ATTR_READONLY;
             * else
             */
            if (!stmt_updatable)
                value = SQL_ATTR_READONLY;
            else
                value =
                    fi ? (fi->updatable ? SQL_ATTR_WRITE : SQL_ATTR_READONLY)
                       : (QR_get_attid(res, col_idx) > 0 ? SQL_ATTR_WRITE
                                                         : SQL_ATTR_READONLY);
            if (SQL_ATTR_READONLY != value) {
                const char *name = fi ? SAFE_NAME(fi->column_name)
                                      : QR_get_fieldname(res, col_idx);
                if (stricmp(name, OID_NAME) == 0 || stricmp(name, "ctid") == 0
                    || stricmp(name, XMIN_NAME) == 0)
                    value = SQL_ATTR_READONLY;
                else if (conn->ms_jet && fi && fi->auto_increment)
                    value = SQL_ATTR_READONLY;
            }

            MYLOG(ES_DEBUG, "%s: UPDATEABLE = " FORMAT_LEN "\n", func, value);
            break;
        case SQL_DESC_BASE_COLUMN_NAME:

            p = fi ? SAFE_NAME(fi->column_name)
                   : QR_get_fieldname(res, col_idx);

            MYLOG(ES_DEBUG, "BASE_COLUMN_NAME = '%s'\n", p);
            break;
        case SQL_DESC_BASE_TABLE_NAME: /* the same as TABLE_NAME ok ? */
            p = ti ? SAFE_NAME(ti->table_name) : NULL_STRING;

            MYLOG(ES_DEBUG, "BASE_TABLE_NAME = '%s'\n", p);
            break;
        case SQL_DESC_LENGTH: /* different from SQL_COLUMN_LENGTH */
            value = (fi && column_size > 0)
                        ? column_size
                        : estype_desclength(stmt, field_type, col_idx,
                                            unknown_sizes);
            if (-1 == value)
                value = 0;

            MYLOG(ES_DEBUG, "col %d, desc_length = " FORMAT_LEN "\n", col_idx,
                  value);
            break;
        case SQL_DESC_OCTET_LENGTH:
            value = (USE_FI(fi, unknown_sizes) && fi->length > 0)
                        ? fi->length
                        : estype_attr_transfer_octet_length(
                            conn, field_type, column_size, unknown_sizes);
            if (-1 == value)
                value = 0;
            MYLOG(ES_DEBUG, "col %d, octet_length = " FORMAT_LEN "\n", col_idx,
                  value);
            break;
        case SQL_DESC_PRECISION: /* different from SQL_COLUMN_PRECISION */
            if (value = FI_precision(fi), value <= 0)
                value =
                    estype_precision(stmt, field_type, col_idx, unknown_sizes);
            if (value < 0)
                value = 0;

            MYLOG(ES_DEBUG, "col %d, desc_precision = " FORMAT_LEN "\n",
                  col_idx, value);
            break;
        case SQL_DESC_SCALE: /* different from SQL_COLUMN_SCALE */
            value = estype_scale(stmt, field_type, col_idx);
            if (value < 0)
                value = 0;
            break;
        case SQL_DESC_LOCAL_TYPE_NAME:
            p = estype_to_name(stmt, field_type, col_idx,
                               fi && fi->auto_increment);
            break;
        case SQL_DESC_TYPE:
            value =
                estype_to_sqldesctype(stmt, field_type, col_idx, unknown_sizes);
            break;
        case SQL_DESC_NUM_PREC_RADIX:
            value = estype_radix(conn, field_type);
            break;
        case SQL_DESC_LITERAL_PREFIX:
            p = estype_literal_prefix(conn, field_type);
            break;
        case SQL_DESC_LITERAL_SUFFIX:
            p = estype_literal_suffix(conn, field_type);
            break;
        case SQL_DESC_UNNAMED:
            value = (fi && NAME_IS_NULL(fi->column_name)
                     && NAME_IS_NULL(fi->column_alias))
                        ? SQL_UNNAMED
                        : SQL_NAMED;
            break;
        case 1211: /* SQL_CA_SS_COLUMN_HIDDEN ? */
            value = 0;
            break;
        case 1212: /* SQL_CA_SS_COLUMN_KEY ? */
            SC_set_error(stmt, STMT_OPTION_NOT_FOR_THE_DRIVER,
                         "this request may be for MS SQL Server", func);
            return SQL_ERROR;
        default:
            SC_set_error(stmt, STMT_INVALID_OPTION_IDENTIFIER,
                         "ColAttribute for this type not implemented yet",
                         func);
            return SQL_ERROR;
    }

    result = SQL_SUCCESS;

    if (p) { /* char/binary data */
        size_t len = strlen(p);

        if (rgbDesc) {
            strncpy_null((char *)rgbDesc, p, (size_t)cbDescMax);

            if (len >= (size_t)cbDescMax) {
                result = SQL_SUCCESS_WITH_INFO;
                SC_set_error(stmt, STMT_TRUNCATED,
                             "The buffer was too small for the rgbDesc.", func);
            }
        }

        if (pcbDesc)
            *pcbDesc = (SQLSMALLINT)len;
    } else {
        /* numeric data */
        if (pfDesc)
            *pfDesc = value;
    }

    return result;
}

/*	Returns result data for a single column in the current row. */
RETCODE SQL_API ESAPI_GetData(HSTMT hstmt, SQLUSMALLINT icol,
                              SQLSMALLINT fCType, PTR rgbValue,
                              SQLLEN cbValueMax, SQLLEN *pcbValue) {
    CSTR func = "ESAPI_GetData";
    QResultClass *res;
    StatementClass *stmt = (StatementClass *)hstmt;
    UInt2 num_cols;
    SQLLEN num_rows;
    OID field_type;
    int atttypmod;
    void *value = NULL;
    RETCODE result = SQL_SUCCESS;
    char get_bookmark = FALSE;
    SQLSMALLINT target_type;
    int precision = -1;
#ifdef WITH_UNIXODBC
    SQLCHAR dum_rgb[2] = "\0\0";
#endif /* WITH_UNIXODBC */

    MYLOG(ES_TRACE, "entering stmt=%p icol=%d\n", stmt, icol);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }
    res = SC_get_Curres(stmt);

    if (STMT_EXECUTING == stmt->status) {
        SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                     "Can't get data while statement is still executing.",
                     func);
        return SQL_ERROR;
    }

    if (stmt->status != STMT_FINISHED) {
        SC_set_error(stmt, STMT_STATUS_ERROR,
                     "GetData can only be called after the successful "
                     "execution on a SQL statement",
                     func);
        return SQL_ERROR;
    }

#ifdef WITH_UNIXODBC
    if (NULL == rgbValue) /* unixODBC allows rgbValue is NULL? */
    {
        cbValueMax = 0;
        rgbValue = dum_rgb; /* to avoid a crash */
    }
#endif /* WITH_UNIXODBC */
    if (SQL_ARD_TYPE == fCType) {
        ARDFields *opts;
        BindInfoClass *binfo = NULL;

        opts = SC_get_ARDF(stmt);
        if (0 == icol)
            binfo = opts->bookmark;
        else if (icol <= opts->allocated && opts->bindings)
            binfo = &opts->bindings[icol - 1];
        if (binfo) {
            target_type = binfo->returntype;
            MYLOG(ES_DEBUG, "SQL_ARD_TYPE=%d\n", target_type);
            precision = binfo->precision;
        } else {
            SC_set_error(stmt, STMT_STATUS_ERROR,
                         "GetData can't determine the type via ARD", func);
            return SQL_ERROR;
        }
    } else
        target_type = fCType;
    if (icol == 0) {
        if (stmt->options.use_bookmarks == SQL_UB_OFF) {
            SC_set_error(
                stmt, STMT_COLNUM_ERROR,
                "Attempt to retrieve bookmark with bookmark usage disabled",
                func);
            return SQL_ERROR;
        }

        /* Make sure it is the bookmark data type */
        switch (target_type) {
            case SQL_C_BOOKMARK:
            case SQL_C_VARBOOKMARK:
                break;
            default:
                MYLOG(
                    ES_ALL,
                    "GetData Column 0 is type %d not of type SQL_C_BOOKMARK\n",
                    target_type);
                SC_set_error(stmt, STMT_PROGRAM_TYPE_OUT_OF_RANGE,
                             "Column 0 is not of type SQL_C_BOOKMARK", func);
                return SQL_ERROR;
        }

        get_bookmark = TRUE;
    } else {
        /* use zero-based column numbers */
        icol--;

        /* make sure the column number is valid */
        num_cols = QR_NumPublicResultCols(res);
        if (icol >= num_cols) {
            SC_set_error(stmt, STMT_INVALID_COLUMN_NUMBER_ERROR,
                         "Invalid column number.", func);
            return SQL_ERROR;
        }
    }

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wkeyword-macro"
#endif  // __APPLE__
#define return DONT_CALL_RETURN_FROM_HERE ? ? ?
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    if (!SC_is_fetchcursor(stmt)) {
        /* make sure we're positioned on a valid row */
        num_rows = QR_get_num_total_tuples(res);
        if ((stmt->currTuple < 0) || (stmt->currTuple >= num_rows)) {
            SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                         "Not positioned on a valid row for GetData.", func);
            result = SQL_ERROR;
            goto cleanup;
        }
        MYLOG(ES_DEBUG, "     num_rows = " FORMAT_LEN "\n", num_rows);

        if (!get_bookmark) {
            SQLLEN curt = GIdx2CacheIdx(stmt->currTuple, stmt, res);
            value = QR_get_value_backend_row(res, curt, icol);
            MYLOG(ES_DEBUG,
                  "currT=" FORMAT_LEN " base=" FORMAT_LEN " rowset=" FORMAT_LEN
                  "\n",
                  stmt->currTuple, QR_get_rowstart_in_cache(res),
                  SC_get_rowset_start(stmt));
            MYLOG(ES_DEBUG, "     value = '%s'\n", NULL_IF_NULL(value));
        }
    } else {
        /* it's a SOCKET result (backend data) */
        if (stmt->currTuple == -1 || !res || !res->tupleField) {
            SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                         "Not positioned on a valid row for GetData.", func);
            result = SQL_ERROR;
            goto cleanup;
        }

        if (!get_bookmark) {
            /** value = QR_get_value_backend(res, icol); maybe thiw doesn't work
             */
            SQLLEN curt = GIdx2CacheIdx(stmt->currTuple, stmt, res);
            value = QR_get_value_backend_row(res, curt, icol);
        }
        MYLOG(ES_DEBUG, "  socket: value = '%s'\n", NULL_IF_NULL(value));
    }

    if (get_bookmark) {
        BOOL contents_get = FALSE;

        if (rgbValue) {
            if (SQL_C_BOOKMARK == target_type
                || (SQLLEN)sizeof(UInt4) <= cbValueMax) {
                Int4 bookmark = (int)SC_make_int4_bookmark(stmt->currTuple);
                contents_get = TRUE;
                memcpy(rgbValue, &bookmark, sizeof(bookmark));
            }
        }
        if (pcbValue)
            *pcbValue = sizeof(Int4);

        if (contents_get)
            result = SQL_SUCCESS;
        else {
            SC_set_error(stmt, STMT_TRUNCATED,
                         "The buffer was too small for the GetData.", func);
            result = SQL_SUCCESS_WITH_INFO;
        }
        goto cleanup;
    }

    field_type = QR_get_field_type(res, icol);
    atttypmod = QR_get_atttypmod(res, icol);

    MYLOG(ES_DEBUG,
          "**** icol = %d, target_type = %d, field_type = %d, value = '%s'\n",
          icol, target_type, field_type, NULL_IF_NULL(value));

    SC_set_current_col(stmt, icol);

    result = (RETCODE)copy_and_convert_field(stmt, field_type, atttypmod, value,
                                             target_type, precision, rgbValue,
                                             cbValueMax, pcbValue, pcbValue);

    switch (result) {
        case COPY_OK:
            result = SQL_SUCCESS;
            break;

        case COPY_UNSUPPORTED_TYPE:
            SC_set_error(stmt, STMT_RESTRICTED_DATA_TYPE_ERROR,
                         "Received an unsupported type from Elasticsearch.",
                         func);
            result = SQL_ERROR;
            break;

        case COPY_UNSUPPORTED_CONVERSION:
            SC_set_error(stmt, STMT_RESTRICTED_DATA_TYPE_ERROR,
                         "Couldn't handle the necessary data type conversion.",
                         func);
            result = SQL_ERROR;
            break;

        case COPY_RESULT_TRUNCATED:
            SC_set_error(stmt, STMT_TRUNCATED,
                         "The buffer was too small for the GetData.", func);
            result = SQL_SUCCESS_WITH_INFO;
            break;

        case COPY_INVALID_STRING_CONVERSION: /* invalid string */
            SC_set_error(stmt, STMT_STRING_CONVERSION_ERROR,
                         "invalid string conversion occured.", func);
            result = SQL_ERROR;
            break;

        case COPY_GENERAL_ERROR: /* error msg already filled in */
            result = SQL_ERROR;
            break;

        case COPY_NO_DATA_FOUND:
            /* SC_log_error(func, "no data found", stmt); */
            result = SQL_NO_DATA_FOUND;
            break;

        default:
            SC_set_error(
                stmt, STMT_INTERNAL_ERROR,
                "Unrecognized return value from copy_and_convert_field.", func);
            result = SQL_ERROR;
            break;
    }

cleanup:
#undef return
    MYLOG(ES_TRACE, "leaving %d\n", result);
    return result;
}

/*
 *		Returns data for bound columns in the current row ("hstmt->iCursor"),
 *		advances the cursor.
 */
RETCODE SQL_API ESAPI_Fetch(HSTMT hstmt) {
    CSTR func = "ESAPI_Fetch";
    StatementClass *stmt = (StatementClass *)hstmt;
    ARDFields *opts;
    QResultClass *res;
    BindInfoClass *bookmark;
    RETCODE retval = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering stmt = %p, stmt->result= %p\n", stmt,
          stmt ? SC_get_Curres(stmt) : NULL);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    SC_clear_error(stmt);

    if (!(res = SC_get_Curres(stmt), res)) {
        SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                     "Null statement result in ESAPI_Fetch.", func);
        return SQL_ERROR;
    }

    /* Not allowed to bind a bookmark column when using SQLFetch. */
    opts = SC_get_ARDF(stmt);
    if ((bookmark = opts->bookmark, bookmark) && bookmark->buffer) {
        SC_set_error(
            stmt, STMT_COLNUM_ERROR,
            "Not allowed to bind a bookmark column when using ESAPI_Fetch",
            func);
        return SQL_ERROR;
    }

    if (stmt->status == STMT_EXECUTING) {
        SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                     "Can't fetch while statement is still executing.", func);
        return SQL_ERROR;
    }

    if (stmt->status != STMT_FINISHED) {
        SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                     "Fetch can only be called after the successful execution "
                     "on a SQL statement",
                     func);
        return SQL_ERROR;
    }

    if (opts->bindings == NULL) {
        if (!SC_may_fetch_rows(stmt))
            return SQL_NO_DATA_FOUND;
        /* just to avoid a crash if the user insists on calling this */
        /* function even if SQL_ExecDirect has reported an Error */
        SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                     "Bindings were not allocated properly.", func);
        return SQL_ERROR;
    }

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wkeyword-macro"
#endif  // __APPLE__
#define return DONT_CALL_RETURN_FROM_HERE ? ? ?
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    if (stmt->rowset_start < 0)
        SC_set_rowset_start(stmt, 0, TRUE);
    QR_set_reqsize(res, 1);
    /* QR_inc_rowstart_in_cache(res, stmt->last_fetch_count_include_ommitted);
     */
    SC_inc_rowset_start(stmt, stmt->last_fetch_count_include_ommitted);

    retval = SC_fetch(stmt);
#undef return
    return retval;
}

SQLLEN
getNthValid(const QResultClass *res, SQLLEN sta, UWORD orientation, SQLULEN nth,
            SQLLEN *nearest) {
    SQLLEN i, num_tuples = QR_get_num_total_tuples(res), nearp;
    SQLULEN count;
    KeySet *keyset;

    if (!QR_once_reached_eof(res))
        num_tuples = INT_MAX;
    /* Note that the parameter nth is 1-based */
    MYLOG(ES_DEBUG,
          "get " FORMAT_ULEN "th Valid data from " FORMAT_LEN " to %s [dlt=%d]",
          nth, sta, orientation == SQL_FETCH_PRIOR ? "backward" : "forward",
          res->dl_count);
    if (0 == res->dl_count) {
        MYPRINTF(ES_DEBUG, "\n");
        if (SQL_FETCH_PRIOR == orientation) {
            if (sta + 1 >= (SQLLEN)nth) {
                *nearest = sta + 1 - nth;
                return nth;
            }
            *nearest = -1;
            return -(SQLLEN)(sta + 1);
        } else {
            nearp = sta - 1 + nth;
            if (nearp < num_tuples) {
                *nearest = nearp;
                return nth;
            }
            *nearest = num_tuples;
            return -(SQLLEN)(num_tuples - sta);
        }
    }
    count = 0;
    if (QR_get_cursor(res)) {
        SQLLEN *deleted = res->deleted;
        SQLLEN delsta;

        if (SQL_FETCH_PRIOR == orientation) {
            *nearest = sta + 1 - nth;
            delsta = (-1);
            MYPRINTF(ES_DEBUG, "deleted ");
            for (i = res->dl_count - 1; i >= 0 && *nearest <= deleted[i]; i--) {
                MYPRINTF(ES_DEBUG, "[" FORMAT_LEN "]=" FORMAT_LEN " ", i,
                         deleted[i]);
                if (sta >= deleted[i]) {
                    (*nearest)--;
                    if (i > delsta)
                        delsta = i;
                }
            }
            MYPRINTF(ES_DEBUG, "nearest=" FORMAT_LEN "\n", *nearest);
            if (*nearest < 0) {
                *nearest = -1;
                count = sta - delsta;
            } else
                return nth;
        } else {
            MYPRINTF(ES_DEBUG, "\n");
            *nearest = sta - 1 + nth;
            delsta = res->dl_count;
            if (!QR_once_reached_eof(res))
                num_tuples = INT_MAX;
            for (i = 0; i < res->dl_count && *nearest >= deleted[i]; i++) {
                if (sta <= deleted[i]) {
                    (*nearest)++;
                    if (i < delsta)
                        delsta = i;
                }
            }
            if (*nearest >= num_tuples) {
                *nearest = num_tuples;
                count = *nearest - sta + delsta - res->dl_count;
            } else
                return nth;
        }
    } else if (SQL_FETCH_PRIOR == orientation) {
        for (i = sta, keyset = res->keyset + sta; i >= 0; i--, keyset--) {
            if (0
                == (keyset->status
                    & (CURS_SELF_DELETING | CURS_SELF_DELETED
                       | CURS_OTHER_DELETED))) {
                *nearest = i;
                MYPRINTF(ES_DEBUG, " nearest=" FORMAT_LEN "\n", *nearest);
                if (++count == nth)
                    return count;
            }
        }
        *nearest = -1;
    } else {
        for (i = sta, keyset = res->keyset + sta; i < num_tuples;
             i++, keyset++) {
            if (0
                == (keyset->status
                    & (CURS_SELF_DELETING | CURS_SELF_DELETED
                       | CURS_OTHER_DELETED))) {
                *nearest = i;
                MYPRINTF(ES_DEBUG, " nearest=" FORMAT_LEN "\n", *nearest);
                if (++count == nth)
                    return count;
            }
        }
        *nearest = num_tuples;
    }
    MYPRINTF(ES_DEBUG, " nearest not found\n");
    return -(SQLLEN)count;
}

/*
 *	return NO_DATA_FOUND macros
 *	  save_rowset_start or num_tuples must be defined
 */
#define EXTFETCH_RETURN_BOF(stmt, res)                   \
    {                                                    \
        MYLOG(ES_ALL, "RETURN_BOF\n");                   \
        SC_set_rowset_start(stmt, -1, TRUE);             \
        stmt->currTuple = -1;                            \
        /* move_cursor_position_if_needed(stmt, res); */ \
        return SQL_NO_DATA_FOUND;                        \
    }
#define EXTFETCH_RETURN_EOF(stmt, res)                   \
    {                                                    \
        MYLOG(ES_ALL, "RETURN_EOF\n");                   \
        SC_set_rowset_start(stmt, num_tuples, TRUE);     \
        stmt->currTuple = -1;                            \
        /* move_cursor_position_if_needed(stmt, res); */ \
        return SQL_NO_DATA_FOUND;                        \
    }

/*	This fetchs a block of data (rowset). */
RETCODE SQL_API ESAPI_ExtendedFetch(HSTMT hstmt, SQLUSMALLINT fFetchType,
                                    SQLLEN irow, SQLULEN *pcrow,
                                    SQLUSMALLINT *rgfRowStatus,
                                    SQLLEN bookmark_offset, SQLLEN rowsetSize) {
    UNUSED(bookmark_offset, irow);
    CSTR func = "ESAPI_ExtendedFetch";
    StatementClass *stmt = (StatementClass *)hstmt;
    ARDFields *opts;
    QResultClass *res;
    BindInfoClass *bookmark;
    SQLLEN num_tuples, i, fc_io;
    SQLLEN save_rowset_size, progress_size;
    SQLLEN rowset_start, rowset_end = (-1);
    RETCODE result = SQL_SUCCESS;
    char truncated, error, should_set_rowset_start = FALSE;
    SQLLEN currp;
    UWORD pstatus;
    BOOL currp_is_valid, reached_eof, useCursor;
    SQLLEN reqsize = rowsetSize;

    MYLOG(ES_TRACE, "entering stmt=%p rowsetSize=" FORMAT_LEN "\n", stmt,
          rowsetSize);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    /* if (SC_is_fetchcursor(stmt) && !stmt->manual_result) */
    if ((SQL_CURSOR_FORWARD_ONLY != stmt->options.cursor_type)
        || (fFetchType != SQL_FETCH_NEXT)) {
        SC_set_error(stmt, STMT_FETCH_OUT_OF_RANGE,
                     "Only SQL_CURSOR_FORWARD_ONLY with SQL_FETCH_NEXT "
                     "cursor's are supported.",
                     func);
        return SQL_ERROR;
    }

    SC_clear_error(stmt);

    if (!(res = SC_get_Curres(stmt), res)) {
        SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                     "Null statement result in ESAPI_ExtendedFetch.", func);
        return SQL_ERROR;
    }

    opts = SC_get_ARDF(stmt);
    /*
     * If a bookmark column is bound but bookmark usage is off, then error.
     */
    if ((bookmark = opts->bookmark, bookmark) && bookmark->buffer
        && stmt->options.use_bookmarks == SQL_UB_OFF) {
        SC_set_error(
            stmt, STMT_COLNUM_ERROR,
            "Attempt to retrieve bookmark with bookmark usage disabled", func);
        return SQL_ERROR;
    }

    if (stmt->status == STMT_EXECUTING) {
        SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                     "Can't fetch while statement is still executing.", func);
        return SQL_ERROR;
    }

    if (stmt->status != STMT_FINISHED) {
        SC_set_error(stmt, STMT_STATUS_ERROR,
                     "ExtendedFetch can only be called after the successful "
                     "execution on a SQL statement",
                     func);
        return SQL_ERROR;
    }

    if (opts->bindings == NULL) {
        if (!SC_may_fetch_rows(stmt))
            return SQL_NO_DATA_FOUND;
        /* just to avoid a crash if the user insists on calling this */
        /* function even if SQL_ExecDirect has reported an Error */
        SC_set_error(stmt, STMT_INVALID_CURSOR_STATE_ERROR,
                     "Bindings were not allocated properly.", func);
        return SQL_ERROR;
    }

    /* Initialize to no rows fetched */
    if (rgfRowStatus)
        for (i = 0; i < rowsetSize; i++)
            *(rgfRowStatus + i) = SQL_ROW_NOROW;

    if (pcrow)
        *pcrow = 0;

    useCursor = (SC_is_fetchcursor(stmt) && NULL != QR_get_cursor(res));
    num_tuples = QR_get_num_total_tuples(res);
    reached_eof = QR_once_reached_eof(res) && QR_get_cursor(res);
    if (useCursor && !reached_eof)
        num_tuples = INT_MAX;

    MYLOG(ES_ALL, "num_tuples=" FORMAT_LEN "\n", num_tuples);
    /* Save and discard the saved rowset size */
    save_rowset_size = stmt->save_rowset_size;
    stmt->save_rowset_size = -1;
    rowset_start = SC_get_rowset_start(stmt);

    QR_stop_movement(res);
    res->move_offset = 0;
    switch (fFetchType) {
        case SQL_FETCH_NEXT:
            progress_size =
                (save_rowset_size > 0 ? save_rowset_size : rowsetSize);
            if (rowset_start < 0)
                SC_set_rowset_start(stmt, 0, TRUE);
            else if (res->keyset) {
                if (stmt->last_fetch_count <= progress_size) {
                    SC_inc_rowset_start(
                        stmt, stmt->last_fetch_count_include_ommitted);
                    progress_size -= stmt->last_fetch_count;
                }
                if (progress_size > 0) {
                    if (getNthValid(res, SC_get_rowset_start(stmt),
                                    SQL_FETCH_NEXT, progress_size + 1,
                                    &rowset_start)
                        <= 0) {
                        EXTFETCH_RETURN_EOF(stmt, res)
                    } else
                        should_set_rowset_start = TRUE;
                }
            } else
                SC_inc_rowset_start(stmt, progress_size);
            MYLOG(ES_DEBUG,
                  "SQL_FETCH_NEXT: num_tuples=" FORMAT_LEN
                  ", currtuple=" FORMAT_LEN ", rowst=" FORMAT_LEN "\n",
                  num_tuples, stmt->currTuple, rowset_start);
            break;
        default:
            SC_set_error(stmt, STMT_FETCH_OUT_OF_RANGE,
                         "Unsupported ESAPI_ExtendedFetch Direction", func);
            return SQL_ERROR;
    }

    /*
     * CHECK FOR PROPER CURSOR STATE
     */

    /*
     * Handle Declare Fetch style specially because the end is not really
     * the end...
     */
    if (!should_set_rowset_start)
        rowset_start = SC_get_rowset_start(stmt);

    // Get more results when cursor reaches end 
    { 
        ConnectionClass *conn = SC_get_conn(stmt);
        if (conn != NULL) {
            const SQLLEN end_rowset_size = rowset_start + rowsetSize;
            while ((end_rowset_size >= num_tuples)
                   && (NULL != res->server_cursor_id)) {
                GetNextResultSet(stmt);
                num_tuples = QR_get_num_total_tuples(res);
            }
        }
    }

    if (useCursor) {
        if (reached_eof && rowset_start >= num_tuples) {
            EXTFETCH_RETURN_EOF(stmt, res)
        }
    } else {
        /* If *new* rowset is after the result_set, return no data found */
        if (rowset_start >= num_tuples) {
            EXTFETCH_RETURN_EOF(stmt, res)
        }
    }
    /* If *new* rowset is prior to result_set, return no data found */
    if (rowset_start < 0) {
        if (rowset_start + rowsetSize <= 0) {
            EXTFETCH_RETURN_BOF(stmt, res)
        } else { /* overlap with beginning of result set,
                  * so get first rowset */
            SC_set_rowset_start(stmt, 0, TRUE);
        }
        should_set_rowset_start = FALSE;
    }

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wkeyword-macro"
#endif  // __APPLE__
#define return DONT_CALL_RETURN_FROM_HERE ? ? ?
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    /* set the rowset_start if needed */
    if (should_set_rowset_start)
        SC_set_rowset_start(stmt, rowset_start, TRUE);
    if (rowset_end < 0 && QR_haskeyset(res)) {
        getNthValid(res, rowset_start, SQL_FETCH_NEXT, rowsetSize, &rowset_end);
        reqsize = rowset_end - rowset_start + 1;
    }
    QR_set_reqsize(res, (Int4)reqsize);
    /* currTuple is always 1 row prior to the rowset start */
    stmt->currTuple = RowIdx2GIdx(-1, stmt);
    QR_set_rowstart_in_cache(res, SC_get_rowset_start(stmt));

    /* Physical Row advancement occurs for each row fetched below */

    MYLOG(ES_DEBUG, "new currTuple = " FORMAT_LEN "\n", stmt->currTuple);

    truncated = error = FALSE;

    currp = -1;
    stmt->bind_row = 0; /* set the binding location */
    result = SC_fetch(stmt);
    if (SQL_ERROR == result)
        goto cleanup;
    if (SQL_NO_DATA_FOUND != result && res->keyset) {
        currp = GIdx2KResIdx(SC_get_rowset_start(stmt), stmt, res);
        MYLOG(ES_ALL, "currp=" FORMAT_LEN "\n", currp);
        if (currp < 0) {
            result = SQL_ERROR;
            MYLOG(ES_DEBUG,
                  "rowset_start=" FORMAT_LEN " but currp=" FORMAT_LEN "\n",
                  SC_get_rowset_start(stmt), currp);
            SC_set_error(stmt, STMT_INTERNAL_ERROR,
                         "rowset_start not in the keyset", func);
            goto cleanup;
        }
    }
    for (i = 0, fc_io = 0; SQL_NO_DATA_FOUND != result && SQL_ERROR != result;
         currp++) {
        fc_io++;
        currp_is_valid = FALSE;
        if (res->keyset) {
            if ((SQLULEN)currp < res->num_cached_keys) {
                currp_is_valid = TRUE;
                res->keyset[currp].status &=
                    ~CURS_IN_ROWSET; /* Off the flag first */
            } else {
                MYLOG(ES_DEBUG, "Umm current row is out of keyset\n");
                break;
            }
        }
        MYLOG(ES_ALL, "ExtFetch result=%d\n", result);
        if (currp_is_valid && SQL_SUCCESS_WITH_INFO == result
            && 0 == stmt->last_fetch_count) {
            MYLOG(ES_ALL, "just skipping deleted row " FORMAT_LEN "\n", currp);
            if (rowsetSize - i + fc_io > reqsize)
                QR_set_reqsize(res, (Int4)(rowsetSize - i + fc_io));
            result = SC_fetch(stmt);
            if (SQL_ERROR == result)
                break;
            continue;
        }

        /* Determine Function status */
        if (result == SQL_SUCCESS_WITH_INFO)
            truncated = TRUE;
        else if (result == SQL_ERROR)
            error = TRUE;

        /* Determine Row Status */
        if (rgfRowStatus) {
            if (result == SQL_ERROR)
                *(rgfRowStatus + i) = SQL_ROW_ERROR;
            else if (currp_is_valid) {
                pstatus = (res->keyset[currp].status & KEYSET_INFO_PUBLIC);
                if (pstatus != 0 && pstatus != SQL_ROW_ADDED) {
                    rgfRowStatus[i] = pstatus;
                } else
                    rgfRowStatus[i] = SQL_ROW_SUCCESS;
                /* refresh the status */
                /* if (SQL_ROW_DELETED != pstatus) */
                res->keyset[currp].status &= (~KEYSET_INFO_PUBLIC);
            } else
                *(rgfRowStatus + i) = SQL_ROW_SUCCESS;
        }
        if (SQL_ERROR != result && currp_is_valid)
            res->keyset[currp].status |=
                CURS_IN_ROWSET; /* This is the unique place where the
                                   CURS_IN_ROWSET bit is turned on */
        i++;
        if (i >= rowsetSize)
            break;
        stmt->bind_row = (SQLSETPOSIROW)i; /* set the binding location */
        result = SC_fetch(stmt);
    }
    if (SQL_ERROR == result)
        goto cleanup;

    /* Save the fetch count for SQLSetPos */
    stmt->last_fetch_count = i;
    stmt->save_rowset_size = rowsetSize;
    /*
    currp = KResIdx2GIdx(currp, stmt, res);
    stmt->last_fetch_count_include_ommitted = GIdx2RowIdx(currp, stmt);
    */
    stmt->last_fetch_count_include_ommitted = fc_io;

    /* Reset next binding row */
    stmt->bind_row = 0;

    /* Move the cursor position to the first row in the result set. */
    stmt->currTuple = RowIdx2GIdx(0, stmt);

    /* For declare/fetch, need to reset cursor to beginning of rowset */
    if (useCursor)
        QR_set_position(res, 0);

    /* Set the number of rows retrieved */
    if (pcrow)
        *pcrow = i;
    MYLOG(ES_ALL, "pcrow=" FORMAT_LEN "\n", i);

    if (i == 0)
        /* Only DeclareFetch should wind up here */
        result = SQL_NO_DATA_FOUND;
    else if (error)
        result = SQL_ERROR;
    else if (truncated)
        result = SQL_SUCCESS_WITH_INFO;
    else if (SC_get_errornumber(stmt) == STMT_POS_BEFORE_RECORDSET)
        result = SQL_SUCCESS_WITH_INFO;
    else
        result = SQL_SUCCESS;

cleanup:
#undef return
    return result;
}

/*
 *		This determines whether there are more results sets available for
 *		the "hstmt".
 */
/* CC: return SQL_NO_DATA_FOUND since we do not support multiple result sets */
RETCODE SQL_API ESAPI_MoreResults(HSTMT hstmt) {
    StatementClass *stmt = (StatementClass *)hstmt;
    QResultClass *res;
    RETCODE ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");
    res = SC_get_Curres(stmt);
    if (res) {
        res = res->next;
        SC_set_Curres(stmt, res);
    }
    if (res) {
        SQLSMALLINT num_p;

        if (stmt->multi_statement < 0)
            ESAPI_NumParams(stmt, &num_p);
        if (stmt->multi_statement > 0) {
            const char *cmdstr;

            SC_initialize_cols_info(stmt, FALSE, TRUE);
            stmt->statement_type = STMT_TYPE_UNKNOWN;
            if (cmdstr = QR_get_command(res), NULL != cmdstr)
                stmt->statement_type = (short)statement_type(cmdstr);
            stmt->join_info = 0;
            SC_clear_parse_method(stmt);
        }
        stmt->diag_row_count = res->recent_processed_row_count;
        SC_set_rowset_start(stmt, -1, FALSE);
        stmt->currTuple = -1;
    } else {
        ESAPI_FreeStmt(hstmt, SQL_CLOSE);
        ret = SQL_NO_DATA_FOUND;
    }
    MYLOG(ES_DEBUG, "leaving %d\n", ret);
    return ret;
}

SQLLEN ClearCachedRows(TupleField *tuple, int num_fields, SQLLEN num_rows) {
    SQLLEN i;

    for (i = 0; i < num_fields * num_rows; i++, tuple++) {
        if (tuple->value) {
            MYLOG(ES_ALL,
                  "freeing tuple[" FORMAT_LEN "][" FORMAT_LEN "].value=%p\n",
                  i / num_fields, i % num_fields, tuple->value);
            free(tuple->value);
            tuple->value = NULL;
        }
        tuple->len = -1;
    }
    return i;
}

/*	Set the cursor name on a statement handle */
RETCODE SQL_API ESAPI_SetCursorName(HSTMT hstmt, const SQLCHAR *szCursor,
                                    SQLSMALLINT cbCursor) {
    CSTR func = "ESAPI_SetCursorName";
    StatementClass *stmt = (StatementClass *)hstmt;

    MYLOG(ES_TRACE, "entering hstmt=%p, szCursor=%p, cbCursorMax=%d\n", hstmt,
          szCursor, cbCursor);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }

    SET_NAME_DIRECTLY(stmt->cursor_name,
                      make_string(szCursor, cbCursor, NULL, 0));
    return SQL_SUCCESS;
}

/*	Return the cursor name for a statement handle */
RETCODE SQL_API ESAPI_GetCursorName(HSTMT hstmt, SQLCHAR *szCursor,
                                    SQLSMALLINT cbCursorMax,
                                    SQLSMALLINT *pcbCursor) {
    CSTR func = "ESAPI_GetCursorName";
    StatementClass *stmt = (StatementClass *)hstmt;
    size_t len = 0;
    RETCODE result;

    MYLOG(ES_DEBUG,
          "entering hstmt=%p, szCursor=%p, cbCursorMax=%d, pcbCursor=%p\n",
          hstmt, szCursor, cbCursorMax, pcbCursor);

    if (!stmt) {
        SC_log_error(func, NULL_STRING, NULL);
        return SQL_INVALID_HANDLE;
    }
    result = SQL_SUCCESS;
    len = strlen(SC_cursor_name(stmt));

    if (szCursor) {
        strncpy_null((char *)szCursor, SC_cursor_name(stmt), cbCursorMax);

        if (len >= (size_t)cbCursorMax) {
            result = SQL_SUCCESS_WITH_INFO;
            SC_set_error(stmt, STMT_TRUNCATED,
                         "The buffer was too small for the GetCursorName.",
                         func);
        }
    }

    if (pcbCursor)
        *pcbCursor = (SQLSMALLINT)len;

    return result;
}
