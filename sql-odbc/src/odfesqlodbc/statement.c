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

// clang-format off
#include "statement.h"
#include "misc.h" // strncpy_null

#include "bind.h"
#include "es_connection.h"
#include "multibyte.h"
#include "qresult.h"
#include "convert.h"
#include "environ.h"
#include "loadlib.h"

#include <stdio.h>
#include <string.h>
#include <ctype.h>

#include "es_apifunc.h"
#include "es_helper.h"
#include "es_statement.h"
// clang-format on

/*	Map sql commands to statement types */
static const struct {
    int type;
    char *s;
} Statement_Type[] =

    {{STMT_TYPE_SELECT, "SELECT"},
     {STMT_TYPE_INSERT, "INSERT"},
     {STMT_TYPE_UPDATE, "UPDATE"},
     {STMT_TYPE_DELETE, "DELETE"},
     {STMT_TYPE_PROCCALL, "{"},
     {STMT_TYPE_SET, "SET"},
     {STMT_TYPE_RESET, "RESET"},
     {STMT_TYPE_CREATE, "CREATE"},
     {STMT_TYPE_DECLARE, "DECLARE"},
     {STMT_TYPE_FETCH, "FETCH"},
     {STMT_TYPE_MOVE, "MOVE"},
     {STMT_TYPE_CLOSE, "CLOSE"},
     {STMT_TYPE_PREPARE, "PREPARE"},
     {STMT_TYPE_EXECUTE, "EXECUTE"},
     {STMT_TYPE_DEALLOCATE, "DEALLOCATE"},
     {STMT_TYPE_DROP, "DROP"},
     {STMT_TYPE_START, "BEGIN"},
     {STMT_TYPE_START, "START"},
     {STMT_TYPE_TRANSACTION, "SAVEPOINT"},
     {STMT_TYPE_TRANSACTION, "RELEASE"},
     {STMT_TYPE_TRANSACTION, "COMMIT"},
     {STMT_TYPE_TRANSACTION, "END"},
     {STMT_TYPE_TRANSACTION, "ROLLBACK"},
     {STMT_TYPE_TRANSACTION, "ABORT"},
     {STMT_TYPE_LOCK, "LOCK"},
     {STMT_TYPE_ALTER, "ALTER"},
     {STMT_TYPE_GRANT, "GRANT"},
     {STMT_TYPE_REVOKE, "REVOKE"},
     {STMT_TYPE_COPY, "COPY"},
     {STMT_TYPE_ANALYZE, "ANALYZE"},
     {STMT_TYPE_NOTIFY, "NOTIFY"},
     {STMT_TYPE_EXPLAIN, "EXPLAIN"}

     /*
      * Special-commands that cannot be run in a transaction block. This isn't
      * as granular as it could be. VACUUM can never be run in a transaction
      * block, but some variants of REINDEX and CLUSTER can be. CHECKPOINT
      * doesn't throw an error if you do, but it cannot be rolled back so
      * there's no point in beginning a new transaction for it.
      */
     ,
     {STMT_TYPE_SPECIAL, "VACUUM"},
     {STMT_TYPE_SPECIAL, "REINDEX"},
     {STMT_TYPE_SPECIAL, "CLUSTER"},
     {STMT_TYPE_SPECIAL, "CHECKPOINT"}

     ,
     {STMT_TYPE_WITH, "WITH"},
     {0, NULL}};

static void SC_set_error_if_not_set(StatementClass *self, int errornumber,
                                    const char *errmsg, const char *func);

RETCODE SQL_API ESAPI_AllocStmt(HDBC hdbc, HSTMT *phstmt, UDWORD flag) {
    CSTR func = "ESAPI_AllocStmt";
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    StatementClass *stmt;
    ARDFields *ardopts;

    MYLOG(ES_TRACE, "entering...\n");

    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    stmt = SC_Constructor(conn);

    MYLOG(ES_DEBUG, "**** : hdbc = %p, stmt = %p\n", hdbc, stmt);

    if (!stmt) {
        CC_set_error(conn, CONN_STMT_ALLOC_ERROR,
                     "No more memory to allocate a further SQL-statement",
                     func);
        *phstmt = SQL_NULL_HSTMT;
        return SQL_ERROR;
    }

    if (!CC_add_statement(conn, stmt)) {
        CC_set_error(conn, CONN_STMT_ALLOC_ERROR,
                     "Maximum number of statements exceeded.", func);
        SC_Destructor(stmt);
        *phstmt = SQL_NULL_HSTMT;
        return SQL_ERROR;
    }

    *phstmt = (HSTMT)stmt;

    stmt->iflag = flag;
    /* Copy default statement options based from Connection options */
    if (0 != (PODBC_INHERIT_CONNECT_OPTIONS & flag)) {
        stmt->options = stmt->options_orig = conn->stmtOptions;
        stmt->ardi.ardf = conn->ardOptions;
    } else {
        InitializeStatementOptions(&stmt->options_orig);
        stmt->options = stmt->options_orig;
        InitializeARDFields(&stmt->ardi.ardf);
    }
    ardopts = SC_get_ARDF(stmt);
    ARD_AllocBookmark(ardopts);

    /* Save the handle for later */
    stmt->phstmt = phstmt;

    return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_FreeStmt(HSTMT hstmt, SQLUSMALLINT fOption) {
    CSTR func = "ESAPI_FreeStmt";
    StatementClass *stmt = (StatementClass *)hstmt;

    MYLOG(ES_TRACE, "entering...hstmt=%p, fOption=%hi\n", hstmt, fOption);

    if (!stmt) {
        SC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }
    SC_clear_error(stmt);

    if (fOption == SQL_DROP) {
        ConnectionClass *conn = stmt->hdbc;

        ESStopRetrieval(conn->esconn);

        /* Remove the statement from the connection's statement list */
        if (conn) {
            QResultClass *res;

            if (STMT_EXECUTING == stmt->status) {
                SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                             "Statement is currently executing a transaction.",
                             func);
                return SQL_ERROR; /* stmt may be executing a transaction */
            }
            if (conn->unnamed_prepared_stmt == stmt)
                conn->unnamed_prepared_stmt = NULL;

            res = SC_get_Result(stmt);
            QR_Destructor(res);
            SC_init_Result(stmt);
            if (!CC_remove_statement(conn, stmt)) {
                SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                             "Statement is currently executing a transaction.",
                             func);
                return SQL_ERROR; /* stmt may be executing a
                                   * transaction */
            }
        }

        if (stmt->execute_delegate) {
            ESAPI_FreeStmt(stmt->execute_delegate, SQL_DROP);
            stmt->execute_delegate = NULL;
        }
        if (stmt->execute_parent)
            stmt->execute_parent->execute_delegate = NULL;
        /* Destroy the statement and free any results, cursors, etc. */
        SC_Destructor(stmt);
    } else if (fOption == SQL_UNBIND)
        SC_unbind_cols(stmt);
    else if (fOption == SQL_CLOSE) {
        ESStopRetrieval(stmt->hdbc->esconn);

        /*
         * this should discard all the results, but leave the statement
         * itself in place (it can be executed again)
         */
        stmt->transition_status = STMT_TRANSITION_ALLOCATED;
        if (stmt->execute_delegate) {
            ESAPI_FreeStmt(stmt->execute_delegate, SQL_DROP);
            stmt->execute_delegate = NULL;
        }
        if (!SC_recycle_statement(stmt)) {
            return SQL_ERROR;
        }
        SC_set_Curres(stmt, NULL);
    } else if (fOption == SQL_RESET_PARAMS)
        ;
    else {
        SC_set_error(stmt, STMT_OPTION_OUT_OF_RANGE_ERROR,
                     "Invalid option passed to ESAPI_FreeStmt.", func);
        return SQL_ERROR;
    }

    return SQL_SUCCESS;
}

/*
 * StatementClass implementation
 */
void InitializeStatementOptions(StatementOptions *opt) {
    memset(opt, 0, sizeof(StatementOptions));
    opt->scroll_concurrency = SQL_CONCUR_READ_ONLY;
    opt->cursor_type = SQL_CURSOR_FORWARD_ONLY;
    opt->retrieve_data = SQL_RD_ON;
    opt->use_bookmarks = SQL_UB_OFF;
    opt->metadata_id = SQL_FALSE;
}

static void SC_clear_parse_status(StatementClass *self, ConnectionClass *conn) {
    UNUSED(self, conn);
    self->parse_status = STMT_PARSE_NONE;
}

static void SC_init_discard_output_params(StatementClass *self) {
    ConnectionClass *conn = SC_get_conn(self);

    if (!conn)
        return;
    self->discard_output_params = 0;
}

static void SC_init_parse_method(StatementClass *self) {
    ConnectionClass *conn = SC_get_conn(self);

    self->parse_method = 0;
    if (!conn)
        return;
    if (0 == (PODBC_EXTERNAL_STATEMENT & self->iflag))
        return;
    if (self->catalog_result)
        return;
}

StatementClass *SC_Constructor(ConnectionClass *conn) {
    StatementClass *rv;

    rv = (StatementClass *)malloc(sizeof(StatementClass));
    if (rv) {
        rv->hdbc = conn;
        rv->phstmt = NULL;
        rv->result = NULL;
        rv->curres = NULL;
        rv->catalog_result = FALSE;
        rv->prepare = NON_PREPARE_STATEMENT;
        rv->prepared = NOT_PREPARED;
        rv->status = STMT_ALLOCATED;
        rv->external = FALSE;
        rv->iflag = 0;
        rv->plan_name = NULL;
        rv->transition_status = STMT_TRANSITION_UNALLOCATED;
        rv->multi_statement = -1; /* unknown */
        rv->num_params = -1;      /* unknown */
        rv->processed_statements = NULL;

        rv->__error_message = NULL;
        rv->__error_number = 0;
        rv->eserror = NULL;

        rv->statement = NULL;
        rv->load_statement = NULL;
        rv->statement_type = STMT_TYPE_UNKNOWN;

        rv->currTuple = -1;
        rv->rowset_start = 0;
        SC_set_rowset_start(rv, -1, FALSE);
        rv->current_col = -1;
        rv->bind_row = 0;
        rv->from_pos = rv->load_from_pos = rv->where_pos = -1;
        rv->last_fetch_count = rv->last_fetch_count_include_ommitted = 0;
        rv->save_rowset_size = -1;

        rv->data_at_exec = -1;
        rv->put_data = FALSE;
        rv->ref_CC_error = FALSE;
        rv->join_info = 0;
        SC_init_parse_method(rv);

        rv->lobj_fd = -1;
        INIT_NAME(rv->cursor_name);

        /* Parse Stuff */
        rv->ti = NULL;
        rv->ntab = 0;
        rv->num_key_fields = -1; /* unknown */
        SC_clear_parse_status(rv, conn);
        rv->proc_return = -1;
        SC_init_discard_output_params(rv);
        rv->cancel_info = 0;

        /* Clear Statement Options -- defaults will be set in AllocStmt */
        memset(&rv->options, 0, sizeof(StatementOptions));
        InitializeEmbeddedDescriptor((DescriptorClass *)&(rv->ardi), rv,
                                     SQL_ATTR_APP_ROW_DESC);
        InitializeEmbeddedDescriptor((DescriptorClass *)&(rv->apdi), rv,
                                     SQL_ATTR_APP_PARAM_DESC);
        InitializeEmbeddedDescriptor((DescriptorClass *)&(rv->irdi), rv,
                                     SQL_ATTR_IMP_ROW_DESC);
        InitializeEmbeddedDescriptor((DescriptorClass *)&(rv->ipdi), rv,
                                     SQL_ATTR_IMP_PARAM_DESC);

        rv->miscinfo = 0;
        rv->execinfo = 0;
        rv->rb_or_tc = 0;
        SC_reset_updatable(rv);
        rv->diag_row_count = 0;
        rv->stmt_time = 0;
        rv->execute_delegate = NULL;
        rv->execute_parent = NULL;
        rv->allocated_callbacks = 0;
        rv->num_callbacks = 0;
        rv->callbacks = NULL;
        GetDataInfoInitialize(SC_get_GDTI(rv));
        PutDataInfoInitialize(SC_get_PDTI(rv));
        rv->lock_CC_for_rb = FALSE;
        INIT_STMT_CS(rv);
    }
    return rv;
}

char SC_Destructor(StatementClass *self) {
    CSTR func = "SC_Destructor";
    QResultClass *res = SC_get_Result(self);

    MYLOG(ES_TRACE, "entering self=%p, self->result=%p, self->hdbc=%p\n", self,
          res, self->hdbc);
    SC_clear_error(self);
    if (STMT_EXECUTING == self->status) {
        SC_set_error(self, STMT_SEQUENCE_ERROR,
                     "Statement is currently executing a transaction.", func);
        return FALSE;
    }

    if (res) {
        if (!self->hdbc)
            res->conn = NULL; /* prevent any dbase activity */

        QR_Destructor(res);
    }

    SC_initialize_stmts(self, TRUE);

    /* Free the parsed table information */
    SC_initialize_cols_info(self, FALSE, TRUE);

    NULL_THE_NAME(self->cursor_name);
    /* Free the parsed field information */
    DC_Destructor((DescriptorClass *)SC_get_ARDi(self));
    DC_Destructor((DescriptorClass *)SC_get_APDi(self));
    DC_Destructor((DescriptorClass *)SC_get_IRDi(self));
    DC_Destructor((DescriptorClass *)SC_get_IPDi(self));
    GDATA_unbind_cols(SC_get_GDTI(self), TRUE);
    PDATA_free_params(SC_get_PDTI(self), STMT_FREE_PARAMS_ALL);

    if (self->__error_message)
        free(self->__error_message);
    if (self->eserror)
        ER_Destructor(self->eserror);
    cancelNeedDataState(self);
    if (self->callbacks)
        free(self->callbacks);

    DELETE_STMT_CS(self);
    free(self);

    MYLOG(ES_TRACE, "leaving\n");

    return TRUE;
}

void SC_init_Result(StatementClass *self) {
    self->result = self->curres = NULL;
    MYLOG(ES_TRACE, "leaving(%p)\n", self);
}

void SC_set_Result(StatementClass *self, QResultClass *res) {
    if (res != self->result) {
        MYLOG(ES_DEBUG, "(%p, %p)\n", self, res);
        QR_Destructor(self->result);
        self->result = self->curres = res;
    }
}

int statement_type(const char *statement) {
    int i;

    /* ignore leading whitespace in query string */
    while (*statement && (isspace((UCHAR)*statement) || *statement == '('))
        statement++;

    for (i = 0; Statement_Type[i].s; i++)
        if (!strnicmp(statement, Statement_Type[i].s,
                      strlen(Statement_Type[i].s)))
            return Statement_Type[i].type;

    return STMT_TYPE_OTHER;
}

void SC_set_planname(StatementClass *stmt, const char *plan_name) {
    if (stmt->plan_name)
        free(stmt->plan_name);
    if (plan_name && plan_name[0])
        stmt->plan_name = strdup(plan_name);
    else
        stmt->plan_name = NULL;
}

void SC_set_rowset_start(StatementClass *stmt, SQLLEN start, BOOL valid_base) {
    QResultClass *res = SC_get_Curres(stmt);
    SQLLEN incr = start - stmt->rowset_start;

    MYLOG(ES_DEBUG, "%p->SC_set_rowstart " FORMAT_LEN "->" FORMAT_LEN "(%s) ",
          stmt, stmt->rowset_start, start, valid_base ? "valid" : "unknown");
    if (res != NULL) {
        BOOL valid = QR_has_valid_base(res);
        MYPRINTF(ES_DEBUG, ":(%p)QR is %s", res,
                 QR_has_valid_base(res) ? "valid" : "unknown");

        if (valid) {
            if (valid_base)
                QR_inc_rowstart_in_cache(res, incr);
            else
                QR_set_no_valid_base(res);
        } else if (valid_base) {
            QR_set_has_valid_base(res);
            if (start < 0)
                QR_set_rowstart_in_cache(res, -1);
            else
                QR_set_rowstart_in_cache(res, start);
        }
        if (!QR_get_cursor(res))
            res->key_base = start;
        MYPRINTF(ES_DEBUG, ":(%p)QR result=" FORMAT_LEN "(%s)", res,
                 QR_get_rowstart_in_cache(res),
                 QR_has_valid_base(res) ? "valid" : "unknown");
    }
    stmt->rowset_start = start;
    MYPRINTF(ES_DEBUG, ":stmt result=" FORMAT_LEN "\n", stmt->rowset_start);
}
void SC_inc_rowset_start(StatementClass *stmt, SQLLEN inc) {
    SQLLEN start = stmt->rowset_start + inc;

    SC_set_rowset_start(stmt, start, TRUE);
}
int SC_set_current_col(StatementClass *stmt, int col) {
    if (col == stmt->current_col)
        return col;
    if (col >= 0)
        reset_a_getdata_info(SC_get_GDTI(stmt), col + 1);
    stmt->current_col = (short)col;

    return stmt->current_col;
}

void SC_set_prepared(StatementClass *stmt, int prepared) {
    if (NOT_PREPARED == prepared)
        SC_set_planname(stmt, NULL);

    // po_ind_t -> char
    stmt->prepared = (po_ind_t)prepared;
}

/*
 * Initialize stmt_with_params and load_statement member pointer
 * deallocating corresponding prepared plan. Also initialize
 * statement member pointer if specified.
 */
RETCODE
SC_initialize_stmts(StatementClass *self, BOOL initializeOriginal) {
    ProcessedStmt *pstmt;
    ProcessedStmt *next_pstmt;

    if (self->lock_CC_for_rb) {
        LEAVE_CONN_CS(SC_get_conn(self));
        self->lock_CC_for_rb = FALSE;
    }
    if (initializeOriginal) {
        if (self->statement) {
            free(self->statement);
            self->statement = NULL;
        }

        pstmt = self->processed_statements;
        while (pstmt) {
            if (pstmt->query)
                free(pstmt->query);
            next_pstmt = pstmt->next;
            free(pstmt);
            pstmt = next_pstmt;
        }
        self->processed_statements = NULL;

        self->prepare = NON_PREPARE_STATEMENT;
        SC_set_prepared(self, NOT_PREPARED);
        self->statement_type = STMT_TYPE_UNKNOWN; /* unknown */
        self->multi_statement = -1;               /* unknown */
        self->num_params = -1;                    /* unknown */
        self->proc_return = -1;                   /* unknown */
        self->join_info = 0;
        SC_init_parse_method(self);
        SC_init_discard_output_params(self);
    }
    if (self->load_statement) {
        free(self->load_statement);
        self->load_statement = NULL;
    }

    return 0;
}

BOOL SC_opencheck(StatementClass *self, const char *func) {
    QResultClass *res;

    if (!self)
        return FALSE;
    if (self->status == STMT_EXECUTING) {
        SC_set_error(self, STMT_SEQUENCE_ERROR,
                     "Statement is currently executing a transaction.", func);
        return TRUE;
    }
    /*
     * We can dispose the result of Describe-only any time.
     */
    if (self->prepare && self->status == STMT_DESCRIBED) {
        MYLOG(ES_DEBUG, "self->prepare && self->status == STMT_DESCRIBED\n");
        return FALSE;
    }
    if (res = SC_get_Curres(self), NULL != res) {
        if (QR_command_maybe_successful(res) && res->backend_tuples) {
            SC_set_error(self, STMT_SEQUENCE_ERROR, "The cursor is open.",
                         func);
            return TRUE;
        }
    }

    return FALSE;
}

RETCODE
SC_initialize_and_recycle(StatementClass *self) {
    SC_initialize_stmts(self, TRUE);
    if (!SC_recycle_statement(self))
        return SQL_ERROR;

    return SQL_SUCCESS;
}

void SC_reset_result_for_rerun(StatementClass *self) {
    QResultClass *res;
    ColumnInfoClass *flds;

    if (!self)
        return;
    if (res = SC_get_Result(self), NULL == res)
        return;
    flds = QR_get_fields(res);
    if (NULL == flds || 0 == CI_get_num_fields(flds))
        SC_set_Result(self, NULL);
    else {
        QR_reset_for_re_execute(res);
        SC_set_Curres(self, NULL);
    }
}

/*
 *	Called from SQLPrepare if STMT_PREMATURE, or
 *	from SQLExecute if STMT_FINISHED, or
 *	from SQLFreeStmt(SQL_CLOSE)
 */
char SC_recycle_statement(StatementClass *self) {
    CSTR func = "SC_recycle_statement";
    ConnectionClass *conn;

    MYLOG(ES_TRACE, "entering self=%p\n", self);

    SC_clear_error(self);
    /* This would not happen */
    if (self->status == STMT_EXECUTING) {
        SC_set_error(self, STMT_SEQUENCE_ERROR,
                     "Statement is currently executing a transaction.", func);
        return FALSE;
    }

    if (SC_get_conn(self)->unnamed_prepared_stmt == self)
        SC_get_conn(self)->unnamed_prepared_stmt = NULL;

    conn = SC_get_conn(self);
    switch (self->status) {
        case STMT_ALLOCATED:
            /* this statement does not need to be recycled */
            return TRUE;

        case STMT_READY:
            break;

        case STMT_DESCRIBED:
            break;

        case STMT_FINISHED:
            break;

        default:
            SC_set_error(self, STMT_INTERNAL_ERROR,
                         "An internal error occured while recycling statements",
                         func);
            return FALSE;
    }

    switch (self->prepared) {
        case NOT_PREPARED:
            /* Free the parsed table/field information */
            SC_initialize_cols_info(self, TRUE, TRUE);

            MYLOG(ES_DEBUG, "SC_clear_parse_status\n");
            SC_clear_parse_status(self, conn);
            break;
    }

    /* Free any cursors */
    if (SC_get_Result(self))
        SC_set_Result(self, NULL);
    self->miscinfo = 0;
    self->execinfo = 0;
    /* self->rbonerr = 0; Never clear the bits here */

    /*
     * Reset only parameters that have anything to do with results
     */
    self->status = STMT_READY;
    self->catalog_result = FALSE; /* not very important */

    self->currTuple = -1;
    SC_set_rowset_start(self, -1, FALSE);
    SC_set_current_col(self, -1);
    self->bind_row = 0;
    MYLOG(ES_DEBUG, "statement=%p ommitted=0\n", self);
    self->last_fetch_count = self->last_fetch_count_include_ommitted = 0;

    self->__error_message = NULL;
    self->__error_number = 0;

    self->lobj_fd = -1;

    SC_initialize_stmts(self, FALSE);
    cancelNeedDataState(self);
    self->cancel_info = 0;
    /*
     *	reset the current attr setting to the original one.
     */
    self->options.scroll_concurrency = self->options_orig.scroll_concurrency;
    self->options.cursor_type = self->options_orig.cursor_type;
    self->options.keyset_size = self->options_orig.keyset_size;
    self->options.maxLength = self->options_orig.maxLength;
    self->options.maxRows = self->options_orig.maxRows;

    return TRUE;
}

/* This is only called from SQLFreeStmt(SQL_UNBIND) */
char SC_unbind_cols(StatementClass *self) {
    ARDFields *opts = SC_get_ARDF(self);
    GetDataInfo *gdata = SC_get_GDTI(self);
    BindInfoClass *bookmark;

    ARD_unbind_cols(opts, FALSE);
    GDATA_unbind_cols(gdata, FALSE);
    if (bookmark = opts->bookmark, bookmark != NULL) {
        bookmark->buffer = NULL;
        bookmark->used = NULL;
    }

    return 1;
}

void SC_clear_error(StatementClass *self) {
    QResultClass *res;

    self->__error_number = 0;
    if (self->__error_message) {
        free(self->__error_message);
        self->__error_message = NULL;
    }
    if (self->eserror) {
        ER_Destructor(self->eserror);
        self->eserror = NULL;
    }
    self->diag_row_count = 0;
    if (res = SC_get_Curres(self), res) {
        QR_set_message(res, NULL);
        QR_set_notice(res, NULL);
        res->sqlstate[0] = '\0';
    }
    self->stmt_time = 0;
    memset(&self->localtime, 0, sizeof(self->localtime));
    self->localtime.tm_sec = -1;
    SC_unref_CC_error(self);
}

/*
 *	This function creates an error info which is the concatenation
 *	of the result, statement, connection, and socket messages.
 */

/*	Map sql commands to statement types */
static const struct {
    int number;
    const char ver3str[6];
    const char ver2str[6];
} Statement_sqlstate[] =

    {{STMT_ERROR_IN_ROW, "01S01", "01S01"},
     {STMT_OPTION_VALUE_CHANGED, "01S02", "01S02"},
     {STMT_ROW_VERSION_CHANGED, "01001", "01001"}, /* data changed */
     {STMT_POS_BEFORE_RECORDSET, "01S06", "01S06"},
     {STMT_TRUNCATED, "01004", "01004"}, /* data truncated */
     {STMT_INFO_ONLY, "00000",
      "00000"}, /* just an information that is returned, no error */

     {STMT_OK, "00000", "00000"},         /* OK */
     {STMT_EXEC_ERROR, "HY000", "S1000"}, /* also a general error */
     {STMT_STATUS_ERROR, "HY010", "S1010"},
     {STMT_SEQUENCE_ERROR, "HY010", "S1010"},  /* Function sequence error */
     {STMT_NO_MEMORY_ERROR, "HY001", "S1001"}, /* memory allocation failure */
     {STMT_COLNUM_ERROR, "07009", "S1002"},    /* invalid column number */
     {STMT_NO_STMTSTRING, "HY001",
      "S1001"}, /* having no stmtstring is also a malloc problem */
     {STMT_ERROR_TAKEN_FROM_BACKEND, "HY000", "S1000"}, /* general error */
     {STMT_INTERNAL_ERROR, "HY000", "S1000"},           /* general error */
     {STMT_STILL_EXECUTING, "HY010", "S1010"},
     {STMT_NOT_IMPLEMENTED_ERROR, "HYC00", "S1C00"}, /* == 'driver not
                                                      * capable' */
     {STMT_BAD_PARAMETER_NUMBER_ERROR, "07009", "S1093"},
     {STMT_OPTION_OUT_OF_RANGE_ERROR, "HY092", "S1092"},
     {STMT_INVALID_COLUMN_NUMBER_ERROR, "07009", "S1002"},
     {STMT_RESTRICTED_DATA_TYPE_ERROR, "07006", "07006"},
     {STMT_INVALID_CURSOR_STATE_ERROR, "07005", "24000"},
     {STMT_CREATE_TABLE_ERROR, "42S01", "S0001"}, /* table already exists */
     {STMT_NO_CURSOR_NAME, "S1015", "S1015"},
     {STMT_INVALID_CURSOR_NAME, "34000", "34000"},
     {STMT_INVALID_ARGUMENT_NO, "HY024", "S1009"}, /* invalid argument value */
     {STMT_ROW_OUT_OF_RANGE, "HY107", "S1107"},
     {STMT_OPERATION_CANCELLED, "HY008", "S1008"},
     {STMT_INVALID_CURSOR_POSITION, "HY109", "S1109"},
     {STMT_VALUE_OUT_OF_RANGE, "HY019", "22003"},
     {STMT_OPERATION_INVALID, "HY011", "S1011"},
     {STMT_PROGRAM_TYPE_OUT_OF_RANGE, "?????", "?????"},
     {STMT_BAD_ERROR, "08S01", "08S01"}, /* communication link failure */
     {STMT_INVALID_OPTION_IDENTIFIER, "HY092", "HY092"},
     {STMT_RETURN_NULL_WITHOUT_INDICATOR, "22002", "22002"},
     {STMT_INVALID_DESCRIPTOR_IDENTIFIER, "HY091", "HY091"},
     {STMT_OPTION_NOT_FOR_THE_DRIVER, "HYC00", "HYC00"},
     {STMT_FETCH_OUT_OF_RANGE, "HY106", "S1106"},
     {STMT_COUNT_FIELD_INCORRECT, "07002", "07002"},
     {STMT_INVALID_NULL_ARG, "HY009", "S1009"},
     {STMT_NO_RESPONSE, "08S01", "08S01"},
     {STMT_COMMUNICATION_ERROR, "08S01", "08S01"}};

static ES_ErrorInfo *SC_create_errorinfo(const StatementClass *self,
                                         ES_ErrorInfo *eserror_fail_safe) {
    QResultClass *res = SC_get_Curres(self);
    ConnectionClass *conn = SC_get_conn(self);
    Int4 errornum;
    size_t pos;
    BOOL resmsg = FALSE, detailmsg = FALSE, msgend = FALSE;
    BOOL looponce, loopend;
    char msg[4096], *wmsg;
    char *ermsg = NULL, *sqlstate = NULL;
    ES_ErrorInfo *eserror;

    if (self->eserror)
        return self->eserror;
    errornum = self->__error_number;
    if (errornum == 0)
        return NULL;

    looponce = (SC_get_Result(self) != res);
    msg[0] = '\0';
    for (loopend = FALSE; (NULL != res) && !loopend; res = res->next) {
        if (looponce)
            loopend = TRUE;
        if ('\0' != res->sqlstate[0]) {
            if (NULL != sqlstate && strnicmp(res->sqlstate, "00", 2) == 0)
                continue;
            sqlstate = res->sqlstate;
            if ('0' != sqlstate[0] || '1' < sqlstate[1])
                loopend = TRUE;
        }
        if (NULL != res->message) {
            STRCPY_FIXED(msg, res->message);
            detailmsg = resmsg = TRUE;
        } else if (NULL != res->messageref) {
            STRCPY_FIXED(msg, res->messageref);
            detailmsg = resmsg = TRUE;
        }
        if (msg[0])
            ermsg = msg;
        else if (QR_get_notice(res)) {
            char *notice = QR_get_notice(res);
            size_t len = strlen(notice);
            if (len < sizeof(msg)) {
                memcpy(msg, notice, len);
                msg[len] = '\0';
                ermsg = msg;
            } else {
                ermsg = notice;
                msgend = TRUE;
            }
        }
    }
    if (!msgend && (wmsg = SC_get_errormsg(self), wmsg) && wmsg[0]) {
        pos = strlen(msg);

        snprintf(&msg[pos], sizeof(msg) - pos, "%s%s", detailmsg ? ";\n" : "",
                 wmsg);
        ermsg = msg;
        detailmsg = TRUE;
    }
    if (!self->ref_CC_error)
        msgend = TRUE;

    if (conn && !msgend) {
        if (!resmsg && (wmsg = CC_get_errormsg(conn), wmsg)
            && wmsg[0] != '\0') {
            pos = strlen(msg);
            snprintf(&msg[pos], sizeof(msg) - pos, ";\n%s",
                     CC_get_errormsg(conn));
        }

        ermsg = msg;
    }
    eserror = ER_Constructor(self->__error_number, ermsg);
    if (!eserror) {
        if (eserror_fail_safe) {
            memset(eserror_fail_safe, 0, sizeof(*eserror_fail_safe));
            eserror = eserror_fail_safe;
            eserror->status = self->__error_number;
            eserror->errorsize = sizeof(eserror->__error_message);
            STRCPY_FIXED(eserror->__error_message, ermsg);
            eserror->recsize = -1;
        } else
            return NULL;
    }
    if (sqlstate)
        STRCPY_FIXED(eserror->sqlstate, sqlstate);
    else if (conn) {
        if (!msgend && conn->sqlstate[0])
            STRCPY_FIXED(eserror->sqlstate, conn->sqlstate);
        else {
            EnvironmentClass *env = (EnvironmentClass *)CC_get_env(conn);

            errornum -= LOWEST_STMT_ERROR;
            if (errornum < 0
                || (unsigned long long)errornum
                       >= sizeof(Statement_sqlstate)
                              / sizeof(Statement_sqlstate[0])) {
                errornum = 1 - LOWEST_STMT_ERROR;
            }
            STRCPY_FIXED(eserror->sqlstate,
                         EN_is_odbc3(env)
                             ? Statement_sqlstate[errornum].ver3str
                             : Statement_sqlstate[errornum].ver2str);
        }
    }

    return eserror;
}

void SC_reset_delegate(RETCODE retcode, StatementClass *stmt) {
    UNUSED(retcode);
    StatementClass *delegate = stmt->execute_delegate;

    if (!delegate)
        return;
    ESAPI_FreeStmt(delegate, SQL_DROP);
}

void SC_set_error(StatementClass *self, int number, const char *message,
                  const char *func) {
    if (self->__error_message)
        free(self->__error_message);
    self->__error_number = number;
    self->__error_message = message ? strdup(message) : NULL;
    if (func && number != STMT_OK && number != STMT_INFO_ONLY)
        SC_log_error(func, "", self);
}

void SC_set_errormsg(StatementClass *self, const char *message) {
    if (self->__error_message)
        free(self->__error_message);
    self->__error_message = message ? strdup(message) : NULL;
}

void SC_error_copy(StatementClass *self, const StatementClass *from,
                   BOOL check) {
    QResultClass *self_res, *from_res;
    BOOL repstate;

    MYLOG(ES_TRACE, "entering %p->%p check=%i\n", from, self, check);
    if (!from)
        return; /* for safety */
    if (self == from)
        return; /* for safety */
    if (check) {
        if (0 == from->__error_number) /* SQL_SUCCESS */
            return;
        if (0 > from->__error_number && /* SQL_SUCCESS_WITH_INFO */
            0 < self->__error_number)
            return;
    }
    self->__error_number = from->__error_number;
    if (!check || from->__error_message) {
        if (self->__error_message)
            free(self->__error_message);
        self->__error_message =
            from->__error_message ? strdup(from->__error_message) : NULL;
    }
    if (self->eserror) {
        ER_Destructor(self->eserror);
        self->eserror = NULL;
    }
    self_res = SC_get_Curres(self);
    from_res = SC_get_Curres(from);
    if (!self_res || !from_res)
        return;
    QR_add_message(self_res, QR_get_message(from_res));
    QR_add_notice(self_res, QR_get_notice(from_res));
    repstate = FALSE;
    if (!check)
        repstate = TRUE;
    else if (from_res->sqlstate[0]) {
        if (!self_res->sqlstate[0] || strncmp(self_res->sqlstate, "00", 2) == 0)
            repstate = TRUE;
        else if (strncmp(from_res->sqlstate, "01", 2) >= 0)
            repstate = TRUE;
    }
    if (repstate)
        STRCPY_FIXED(self_res->sqlstate, from_res->sqlstate);
}

void SC_full_error_copy(StatementClass *self, const StatementClass *from,
                        BOOL allres) {
    ES_ErrorInfo *eserror;

    MYLOG(ES_TRACE, "entering %p->%p\n", from, self);
    if (!from)
        return; /* for safety */
    if (self == from)
        return; /* for safety */
    if (self->__error_message) {
        free(self->__error_message);
        self->__error_message = NULL;
    }
    if (from->__error_message)
        self->__error_message = strdup(from->__error_message);
    self->__error_number = from->__error_number;
    if (from->eserror) {
        if (self->eserror)
            ER_Destructor(self->eserror);
        self->eserror = ER_Dup(from->eserror);
        return;
    } else if (!allres)
        return;
    eserror = SC_create_errorinfo(from, NULL);
    if (!eserror || !eserror->__error_message[0]) {
        ER_Destructor(eserror);
        return;
    }
    if (self->eserror)
        ER_Destructor(self->eserror);
    self->eserror = eserror;
}

/* Returns the next SQL error information. */
RETCODE SQL_API ESAPI_StmtError(SQLHSTMT hstmt, SQLSMALLINT RecNumber,
                                SQLCHAR *szSqlState, SQLINTEGER *pfNativeError,
                                SQLCHAR *szErrorMsg, SQLSMALLINT cbErrorMsgMax,
                                SQLSMALLINT *pcbErrorMsg, UWORD flag) {
    /* CC: return an error of a hdesc  */
    ES_ErrorInfo *eserror, error;
    StatementClass *stmt = (StatementClass *)hstmt;
    int errnum = SC_get_errornumber(stmt);

    if (eserror = SC_create_errorinfo(stmt, &error), NULL == eserror)
        return SQL_NO_DATA_FOUND;
    if (eserror != &error)
        stmt->eserror = eserror;
    if (STMT_NO_MEMORY_ERROR == errnum && !eserror->__error_message[0])
        STRCPY_FIXED(eserror->__error_message, "Memory Allocation Error??");
    return ER_ReturnError(eserror, RecNumber, szSqlState, pfNativeError,
                          szErrorMsg, cbErrorMsgMax, pcbErrorMsg, flag);
}

time_t SC_get_time(StatementClass *stmt) {
    if (!stmt)
        return time(NULL);
    if (0 == stmt->stmt_time)
        stmt->stmt_time = time(NULL);
    return stmt->stmt_time;
}

struct tm *SC_get_localtime(StatementClass *stmt) {
#ifndef HAVE_LOCALTIME_R
    struct tm *tim;
#endif /* HAVE_LOCALTIME_R */

    if (stmt->localtime.tm_sec < 0) {
        SC_get_time(stmt);
#ifdef HAVE_LOCALTIME_R
        localtime_r(&stmt->stmt_time, &(stmt->localtime));
#else
        tim = localtime(&stmt->stmt_time);
        stmt->localtime = *tim;
#endif /* HAVE_LOCALTIME_R */
    }

    return &(stmt->localtime);
}

RETCODE
SC_fetch(StatementClass *self) {
    CSTR func = "SC_fetch";
    QResultClass *res = SC_get_Curres(self);
    ARDFields *opts;
    GetDataInfo *gdata;
    int retval;
    RETCODE result;

    Int2 num_cols, lf;
    OID type;
    int atttypmod;
    char *value;
    ColumnInfoClass *coli;
    BindInfoClass *bookmark;
    BOOL useCursor = FALSE;
    KeySet *keyset = NULL;

    /* TupleField *tupleField; */

    MYLOG(ES_TRACE, "entering statement=%p res=%p ommitted=0\n", self, res);
    self->last_fetch_count = self->last_fetch_count_include_ommitted = 0;
    if (!res)
        return SQL_ERROR;
    coli = QR_get_fields(res); /* the column info */

    MYLOG(ES_DEBUG, "fetch_cursor=%d, %p->total_read=" FORMAT_LEN "\n",
          SC_is_fetchcursor(self), res, res->num_total_read);

    if (self->currTuple >= (Int4)QR_get_num_total_tuples(res) - 1
        || (self->options.maxRows > 0
            && self->currTuple == self->options.maxRows - 1)) {
        /*
         * if at the end of the tuples, return "no data found" and set
         * the cursor past the end of the result set
         */
        self->currTuple = QR_get_num_total_tuples(res);
        return SQL_NO_DATA_FOUND;
    }

    MYLOG(ES_DEBUG, "**** : non-cursor_result\n");
    (self->currTuple)++;

    num_cols = QR_NumPublicResultCols(res);

    result = SQL_SUCCESS;
    self->last_fetch_count++;
    MYLOG(ES_DEBUG, "stmt=%p ommitted++\n", self);
    self->last_fetch_count_include_ommitted++;

    opts = SC_get_ARDF(self);
    /*
     * If the bookmark column was bound then return a bookmark. Since this
     * is used with SQLExtendedFetch, and the rowset size may be greater
     * than 1, and an application can use row or column wise binding, use
     * the code in copy_and_convert_field() to handle that.
     */
    if ((bookmark = opts->bookmark, bookmark) && bookmark->buffer) {
        SC_set_current_col(self, -1);
        SC_Create_bookmark(self, bookmark, (int)self->bind_row,
                           (int)self->currTuple, keyset);
    }

    if (self->options.retrieve_data == SQL_RD_OFF) /* data isn't required */
        return SQL_SUCCESS;
    /* The following adjustment would be needed after SQLMoreResults() */
    if (opts->allocated < num_cols)
        extend_column_bindings(opts, num_cols);
    gdata = SC_get_GDTI(self);
    if (gdata->allocated != opts->allocated)
        extend_getdata_info(gdata, opts->allocated, TRUE);
    for (lf = 0; lf < num_cols; lf++) {
        MYLOG(ES_DEBUG,
              "fetch: cols=%d, lf=%d, opts = %p, opts->bindings = %p, buffer[] "
              "= %p\n",
              num_cols, lf, opts, opts->bindings, opts->bindings[lf].buffer);

        /* reset for SQLGetData */
        GETDATA_RESET(gdata->gdata[lf]);

        if (NULL == opts->bindings)
            continue;
        if (opts->bindings[lf].buffer != NULL) {
            /* this column has a binding */

            /* type = QR_get_field_type(res, lf); */
            type = CI_get_oid(coli, lf);            /* speed things up */
            atttypmod = CI_get_atttypmod(coli, lf); /* speed things up */

            MYLOG(ES_DEBUG, "type = %d, atttypmod = %d\n", type, atttypmod);

            if (useCursor)
                value = QR_get_value_backend(res, lf);
            else {
                SQLLEN curt = GIdx2CacheIdx(self->currTuple, self, res);
                MYLOG(ES_DEBUG,
                      "%p->base=" FORMAT_LEN " curr=" FORMAT_LEN
                      " st=" FORMAT_LEN " valid=%d\n",
                      res, QR_get_rowstart_in_cache(res), self->currTuple,
                      SC_get_rowset_start(self), QR_has_valid_base(res));
                MYLOG(ES_DEBUG, "curt=" FORMAT_LEN "\n", curt);
                value = QR_get_value_backend_row(res, curt, lf);
            }

            MYLOG(ES_DEBUG, "value = '%s'\n",
                  (value == NULL) ? "<NULL>" : value);

            retval = copy_and_convert_field_bindinfo(self, type, atttypmod,
                                                     value, lf);

            MYLOG(ES_DEBUG, "copy_and_convert: retval = %d\n", retval);

            switch (retval) {
                case COPY_OK:
                    break; /* OK, do next bound column */

                case COPY_UNSUPPORTED_TYPE:
                    SC_set_error(
                        self, STMT_RESTRICTED_DATA_TYPE_ERROR,
                        "Received an unsupported type from Elasticsearch.",
                        func);
                    result = SQL_ERROR;
                    break;

                case COPY_UNSUPPORTED_CONVERSION:
                    SC_set_error(
                        self, STMT_RESTRICTED_DATA_TYPE_ERROR,
                        "Couldn't handle the necessary data type conversion.",
                        func);
                    result = SQL_ERROR;
                    break;

                case COPY_RESULT_TRUNCATED:
                    SC_set_error(self, STMT_TRUNCATED,
                                 "Fetched item was truncated.", func);
                    MYLOG(ES_DEBUG, "The %dth item was truncated\n", lf + 1);
                    MYLOG(ES_DEBUG, "The buffer size = " FORMAT_LEN,
                          opts->bindings[lf].buflen);
                    MYLOG(ES_DEBUG, " and the value is '%s'\n", value);
                    result = SQL_SUCCESS_WITH_INFO;
                    break;

                case COPY_INVALID_STRING_CONVERSION: /* invalid string */
                    SC_set_error(self, STMT_STRING_CONVERSION_ERROR,
                                 "invalid string conversion occured.", func);
                    result = SQL_ERROR;
                    break;

                    /* error msg already filled in */
                case COPY_GENERAL_ERROR:
                    result = SQL_ERROR;
                    break;

                    /* This would not be meaningful in SQLFetch. */
                case COPY_NO_DATA_FOUND:
                    break;

                default:
                    SC_set_error(self, STMT_INTERNAL_ERROR,
                                 "Unrecognized return value from "
                                 "copy_and_convert_field.",
                                 func);
                    result = SQL_ERROR;
                    break;
            }
        }
    }

    return result;
}

#include "dlg_specific.h"

#define CALLBACK_ALLOC_ONCE 4

RETCODE dequeueNeedDataCallback(RETCODE retcode, StatementClass *stmt) {
    RETCODE ret;
    NeedDataCallfunc func;
    void *data;
    int i, cnt;

    MYLOG(ES_TRACE, "entering ret=%d count=%d\n", retcode, stmt->num_callbacks);
    if (SQL_NEED_DATA == retcode)
        return retcode;
    if (stmt->num_callbacks <= 0)
        return retcode;
    func = stmt->callbacks[0].func;
    data = stmt->callbacks[0].data;
    for (i = 1; i < stmt->num_callbacks; i++)
        stmt->callbacks[i - 1] = stmt->callbacks[i];
    cnt = --stmt->num_callbacks;
    ret = (*func)(retcode, data);
    free(data);
    if (SQL_NEED_DATA != ret && cnt > 0)
        ret = dequeueNeedDataCallback(ret, stmt);
    return ret;
}

void cancelNeedDataState(StatementClass *stmt) {
    int cnt = stmt->num_callbacks, i;

    stmt->num_callbacks = 0;
    for (i = 0; i < cnt; i++) {
        if (stmt->callbacks[i].data)
            free(stmt->callbacks[i].data);
    }
    SC_reset_delegate(SQL_ERROR, stmt);
}

void SC_log_error(const char *func, const char *desc,
                  const StatementClass *self) {
    const char *head;
#define NULLCHECK(a) (a ? a : "(NULL)")
    if (self) {
        QResultClass *res = SC_get_Result(self);
        const ARDFields *opts = SC_get_ARDF(self);
        const APDFields *apdopts = SC_get_APDF(self);
        SQLLEN rowsetSize;
        const int level = 9;

        rowsetSize = (STMT_TRANSITION_EXTENDED_FETCH == self->transition_status
                          ? opts->size_of_rowset_odbc2
                          : opts->size_of_rowset);
        if (SC_get_errornumber(self) <= 0)
            head = "STATEMENT WARNING";
        else {
            head = "STATEMENT ERROR";
            QLOG(level, "%s: func=%s, desc='%s', errnum=%d, errmsg='%s'\n",
                 head, func, desc, self->__error_number,
                 NULLCHECK(self->__error_message));
        }
        MYLOG(ES_DEBUG, "%s: func=%s, desc='%s', errnum=%d, errmsg='%s'\n",
              head, func, desc, self->__error_number,
              NULLCHECK(self->__error_message));
        if (SC_get_errornumber(self) > 0) {
            QLOG(level,
                 "                 "
                 "------------------------------------------------------------"
                 "\n");
            QLOG(level, "                 hdbc=%p, stmt=%p, result=%p\n",
                 self->hdbc, self, res);
            QLOG(level, "                 prepare=%d, external=%d\n",
                 self->prepare, self->external);
            QLOG(level, "                 bindings=%p, bindings_allocated=%d\n",
                 opts->bindings, opts->allocated);
            QLOG(level,
                 "                 parameters=%p, parameters_allocated=%d\n",
                 apdopts->parameters, apdopts->allocated);
            QLOG(level, "                 statement_type=%d, statement='%s'\n",
                 self->statement_type, NULLCHECK(self->statement));
            QLOG(level,
                 "                 currTuple=" FORMAT_LEN
                 ", current_col=%d, lobj_fd=%d\n",
                 self->currTuple, self->current_col, self->lobj_fd);
            QLOG(level,
                 "                 maxRows=" FORMAT_LEN
                 ", rowset_size=" FORMAT_LEN ", keyset_size=" FORMAT_LEN
                 ", cursor_type=" FORMAT_UINTEGER
                 ", scroll_concurrency=" FORMAT_UINTEGER "\n",
                 self->options.maxRows, rowsetSize, self->options.keyset_size,
                 self->options.cursor_type, self->options.scroll_concurrency);
            QLOG(level, "                 cursor_name='%s'\n",
                 SC_cursor_name(self));

            QLOG(level,
                 "                 ----------------QResult Info "
                 "-------------------------------\n");

            if (res) {
                QLOG(level,
                     "                 fields=%p, backend_tuples=%p, "
                     "tupleField=%p, conn=%p\n",
                     QR_get_fields(res), res->backend_tuples, res->tupleField,
                     res->conn);
                QLOG(level,
                     "                 fetch_count=" FORMAT_LEN
                     ", num_total_rows=" FORMAT_ULEN
                     ", num_fields=%d, cursor='%s'\n",
                     res->fetch_number, QR_get_num_total_tuples(res),
                     res->num_fields, NULLCHECK(QR_get_cursor(res)));
                QLOG(level,
                     "                 message='%s', command='%s', "
                     "notice='%s'\n",
                     NULLCHECK(QR_get_message(res)), NULLCHECK(res->command),
                     NULLCHECK(res->notice));
                QLOG(level, "                 status=%d\n",
                     QR_get_rstatus(res));
            }

            /* Log the connection error if there is one */
            CC_log_error(func, desc, self->hdbc);
        }
    } else {
        MYLOG(ES_DEBUG, "INVALID STATEMENT HANDLE ERROR: func=%s, desc='%s'\n",
              func, desc);
    }
}

extern void *common_cs;

BOOL SC_SetExecuting(StatementClass *self, BOOL on) {
    BOOL exeSet = FALSE;
    ENTER_COMMON_CS; /* short time blocking */
    if (on) {
        if (0 == (self->cancel_info & CancelRequestSet)) {
            self->status = STMT_EXECUTING;
            exeSet = TRUE;
        }
    } else {
        self->cancel_info = 0;
        self->status = STMT_FINISHED;
        exeSet = TRUE;
    }
    LEAVE_COMMON_CS;
    return exeSet;
}

#ifdef NOT_USED
BOOL SC_SetCancelRequest(StatementClass *self) {
    BOOL enteredCS = FALSE;

    ENTER_COMMON_CS;
    if (0 != (self->cancel_info & CancelCompleted))
        ;
    else if (STMT_EXECUTING == self->status) {
        self->cancel_info |= CancelRequestSet;
    } else {
        /* try to acquire */
        if (TRY_ENTER_STMT_CS(self))
            enteredCS = TRUE;
        else
            self->cancel_info |= CancelRequestSet;
    }
    LEAVE_COMMON_CS;
    return enteredCS;
}
#endif /* NOT_USED */

static void SC_set_error_if_not_set(StatementClass *self, int errornumber,
                                    const char *errmsg, const char *func) {
    int errnum = SC_get_errornumber(self);

    if (errnum <= 0) {
        const char *emsg = SC_get_errormsg(self);

        if (emsg && 0 == errnum)
            SC_set_errornumber(self, errornumber);
        else
            SC_set_error(self, errornumber, errmsg, func);
    }
}

void SC_set_errorinfo(StatementClass *self, QResultClass *res, int errkind) {
    ConnectionClass *conn = SC_get_conn(self);

    if (CC_not_connected(conn)) {
        SC_set_error_if_not_set(self, STMT_COMMUNICATION_ERROR,
                                "The connection has been lost", __FUNCTION__);
        return;
    }

    switch (QR_get_rstatus(res)) {
        case PORES_NO_MEMORY_ERROR:
            SC_set_error_if_not_set(self, STMT_NO_MEMORY_ERROR,
                                    "memory allocation error???", __FUNCTION__);
            break;
        case PORES_BAD_RESPONSE:
            SC_set_error_if_not_set(self, STMT_COMMUNICATION_ERROR,
                                    "communication error occured",
                                    __FUNCTION__);
            break;
        case PORES_INTERNAL_ERROR:
            SC_set_error_if_not_set(self, STMT_INTERNAL_ERROR,
                                    "Internal error fetching next row",
                                    __FUNCTION__);
            break;
        default:
            switch (errkind) {
                case 1:
                    SC_set_error_if_not_set(
                        self, STMT_EXEC_ERROR,
                        "Error while fetching the next result", __FUNCTION__);
                    break;
                default:
                    SC_set_error_if_not_set(self, STMT_EXEC_ERROR,
                                            "Error while executing the query",
                                            __FUNCTION__);
                    break;
            }
            break;
    }
}

int SC_Create_bookmark(StatementClass *self, BindInfoClass *bookmark,
                       Int4 bind_row, Int4 currTuple, const KeySet *keyset) {
    ARDFields *opts = SC_get_ARDF(self);
    SQLUINTEGER bind_size = opts->bind_size;
    SQLULEN offset = opts->row_offset_ptr ? *opts->row_offset_ptr : 0;
    size_t cvtlen = sizeof(Int4);
    ES_BM ES_bm;

    MYLOG(ES_TRACE, "entering type=%d buflen=" FORMAT_LEN " buf=%p\n",
          bookmark->returntype, bookmark->buflen, bookmark->buffer);
    memset(&ES_bm, 0, sizeof(ES_bm));
    if (SQL_C_BOOKMARK == bookmark->returntype)
        ;
    else if (bookmark->buflen >= (SQLLEN)sizeof(ES_bm))
        cvtlen = sizeof(ES_bm);
    else if (bookmark->buflen >= 12)
        cvtlen = 12;
    ES_bm.index = SC_make_int4_bookmark(currTuple);
    if (keyset)
        ES_bm.keys = *keyset;
    memcpy(CALC_BOOKMARK_ADDR(bookmark, offset, bind_size, bind_row), &ES_bm,
           cvtlen);
    if (bookmark->used) {
        SQLLEN *used = LENADDR_SHIFT(bookmark->used, offset);

        if (bind_size > 0)
            used = (SQLLEN *)((char *)used + (bind_row * bind_size));
        else
            used = (SQLLEN *)((char *)used + (bind_row * sizeof(SQLLEN)));
        *used = cvtlen;
    }
    MYLOG(ES_TRACE, "leaving cvtlen=" FORMAT_SIZE_T " ix(bl,of)=%d(%d,%d)\n",
          cvtlen, ES_bm.index, ES_bm.keys.blocknum, ES_bm.keys.offset);

    return COPY_OK;
}
