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

#ifndef __STATEMENT_H__
#define __STATEMENT_H__

#include <time.h>

#include "bind.h"
#include "descriptor.h"
#include "es_helper.h"
#include "es_odbc.h"
#include "es_types.h"
#include "tuple.h"

// C Interface
#ifdef __cplusplus
extern "C" {
#endif

enum {
    CancelRequestSet = 1L,
    CancelRequestAccepted = (1L << 1),
    CancelCompleted = (1L << 2)
};

typedef enum {
    STMT_ALLOCATED, /* The statement handle is allocated, but
                     * not used so far */
    STMT_READY,     /* the statement is waiting to be executed */
    STMT_DESCRIBED, /* ODBC states that it is legal to call
                     * e.g. SQLDescribeCol before a call to
                     * SQLExecute, but after SQLPrepare. To
                     * get all the necessary information in
                     * such a case, we parse the query _before_
                     * the actual call to SQLExecute, and the
                     * result set contains only column information,
                     * but no actual data. */
    STMT_FINISHED,  /* statement execution has finished */
    STMT_EXECUTING  /* statement execution is still going on */
} STMT_Status;
/*
 *		ERROR status code
 *
 *		The code for warnings must be minus
 *		and  LOWEST_STMT_ERROR must be set to
 *		the least code number.
 *		The code for STMT_OK is 0 and error
 *		codes follow after it.
 */
enum {
    LOWEST_STMT_ERROR = (-6)
    /* minus values mean warning returns */
    ,
    STMT_ERROR_IN_ROW = (-6),
    STMT_OPTION_VALUE_CHANGED = (-5),
    STMT_ROW_VERSION_CHANGED = (-4),
    STMT_POS_BEFORE_RECORDSET = (-3),
    STMT_TRUNCATED = (-2),
    STMT_INFO_ONLY = (-1)
    /* not an error message,
     * just a notification
     * to be returned by
     * SQLError
     */
    ,
    STMT_OK = 0,
    STMT_EXEC_ERROR,
    STMT_STATUS_ERROR,
    STMT_SEQUENCE_ERROR,
    STMT_NO_MEMORY_ERROR,
    STMT_COLNUM_ERROR,
    STMT_NO_STMTSTRING,
    STMT_ERROR_TAKEN_FROM_BACKEND,
    STMT_INTERNAL_ERROR,
    STMT_STILL_EXECUTING,
    STMT_NOT_IMPLEMENTED_ERROR,
    STMT_BAD_PARAMETER_NUMBER_ERROR,
    STMT_OPTION_OUT_OF_RANGE_ERROR,
    STMT_INVALID_COLUMN_NUMBER_ERROR,
    STMT_RESTRICTED_DATA_TYPE_ERROR,
    STMT_INVALID_CURSOR_STATE_ERROR,
    STMT_CREATE_TABLE_ERROR,
    STMT_NO_CURSOR_NAME,
    STMT_INVALID_CURSOR_NAME,
    STMT_INVALID_ARGUMENT_NO,
    STMT_ROW_OUT_OF_RANGE,
    STMT_OPERATION_CANCELLED,
    STMT_INVALID_CURSOR_POSITION,
    STMT_VALUE_OUT_OF_RANGE,
    STMT_OPERATION_INVALID,
    STMT_PROGRAM_TYPE_OUT_OF_RANGE,
    STMT_BAD_ERROR,
    STMT_INVALID_OPTION_IDENTIFIER,
    STMT_RETURN_NULL_WITHOUT_INDICATOR,
    STMT_INVALID_DESCRIPTOR_IDENTIFIER,
    STMT_OPTION_NOT_FOR_THE_DRIVER,
    STMT_FETCH_OUT_OF_RANGE,
    STMT_COUNT_FIELD_INCORRECT,
    STMT_INVALID_NULL_ARG,
    STMT_NO_RESPONSE,
    STMT_COMMUNICATION_ERROR,
    STMT_STRING_CONVERSION_ERROR
};

/* statement types */
enum {
    STMT_TYPE_UNKNOWN = -2,
    STMT_TYPE_OTHER = -1,
    STMT_TYPE_SELECT = 0,
    STMT_TYPE_WITH,
    STMT_TYPE_PROCCALL,
    STMT_TYPE_TRANSACTION,
    STMT_TYPE_DECLARE,
    STMT_TYPE_FETCH,
    STMT_TYPE_CLOSE,
    STMT_TYPE_INSERT,
    STMT_TYPE_UPDATE,
    STMT_TYPE_DELETE,
    STMT_TYPE_CREATE,
    STMT_TYPE_ALTER,
    STMT_TYPE_DROP,
    STMT_TYPE_GRANT,
    STMT_TYPE_REVOKE,
    STMT_TYPE_LOCK,
    STMT_TYPE_PREPARE,
    STMT_TYPE_EXECUTE,
    STMT_TYPE_DEALLOCATE,
    STMT_TYPE_ANALYZE,
    STMT_TYPE_NOTIFY,
    STMT_TYPE_EXPLAIN,
    STMT_TYPE_SET,
    STMT_TYPE_RESET,
    STMT_TYPE_MOVE,
    STMT_TYPE_COPY,
    STMT_TYPE_START,
    STMT_TYPE_SPECIAL
};

#define STMT_UPDATE(stmt) ((stmt)->statement_type > STMT_TYPE_PROCCALL)

/*	Parsing status */
enum {
    STMT_PARSE_NONE = 0,
    STMT_PARSE_COMPLETE /* the driver parsed the statement */
    ,
    STMT_PARSE_INCOMPLETE,
    STMT_PARSE_FATAL,
    STMT_PARSE_MASK = 3L,
    STMT_PARSED_OIDS = (1L << 2),
    STMT_FOUND_KEY = (1L << 3),
    STMT_HAS_ROW_DESCRIPTION = (1L << 4) /* already got the col info */
    ,
    STMT_REFLECTED_ROW_DESCRIPTION = (1L << 5)
};

/*	transition status */
enum {
    STMT_TRANSITION_UNALLOCATED = 0,
    STMT_TRANSITION_ALLOCATED = 1,
    STMT_TRANSITION_FETCH_SCROLL = 6,
    STMT_TRANSITION_EXTENDED_FETCH = 7
};

/*	Result style */
enum { STMT_FETCH_NONE = 0, STMT_FETCH_NORMAL, STMT_FETCH_EXTENDED };

#define ES_NUM_NORMAL_KEYS 2

typedef RETCODE (*NeedDataCallfunc)(RETCODE, void *);
typedef struct {
    NeedDataCallfunc func;
    void *data;
} NeedDataCallback;

/*
 * ProcessedStmt represents a fragment of the original SQL query, after
 * converting ? markers to $n style, processing ODBC escapes, and splitting
 * a multi-statement into individual statements. Each individual statement
 * is represented by one ProcessedStmt struct.
 */
struct ProcessedStmt {
    struct ProcessedStmt *next;
    char *query;
    int num_params; /* number of parameter markers in this,
                     * fragment or -1 if not known */
};
typedef struct ProcessedStmt ProcessedStmt;

/********	Statement Handle	***********/
struct StatementClass_ {
    ConnectionClass *hdbc; /* pointer to ConnectionClass this
                            * statement belongs to */
    QResultClass *result;  /* result of the current statement */
    QResultClass *curres;  /* the current result in the chain */
    HSTMT *phstmt;
    StatementOptions options;
    StatementOptions options_orig;
    /* attached descriptor handles */
    DescriptorClass *ard;
    DescriptorClass *apd;
    DescriptorClass *ird;
    DescriptorClass *ipd;
    /* implicit descriptor handles */
    DescriptorClass ardi;
    DescriptorClass irdi;
    DescriptorClass apdi;
    DescriptorClass ipdi;

    STMT_Status status;
    char *__error_message;
    int __error_number;
    ES_ErrorInfo *eserror;

    SQLLEN currTuple; /* current absolute row number (GetData,
                       * SetPos, SQLFetch) */
    GetDataInfo gdata_info;
    SQLLEN save_rowset_size; /* saved rowset size in case of
                              * change/FETCH_NEXT */
    SQLLEN rowset_start;     /* start of rowset (an absolute row
                              * number) */
    SQLSETPOSIROW bind_row;  /* current offset for Multiple row/column
                              * binding */
    Int2 current_col;        /* current column for GetData -- used to
                              * handle multiple calls */
    SQLLEN last_fetch_count; /* number of rows retrieved in
                              * last fetch/extended fetch */
    int lobj_fd;             /* fd of the current large object */

    char *statement; /* if non--null pointer to the SQL
                      * statement that has been executed */
    /*
     * processed_statements is the SQL after splitting multi-statement into
     * parts, and replacing ? markers with $n style markers, or injecting the
     * values in UseServerSidePrepare=0 mode.
     */
    ProcessedStmt *processed_statements;

    TABLE_INFO **ti;
    Int2 ntab;
    Int2 num_key_fields;
    Int2 statement_type; /* According to the defines above */
    Int2 num_params;
    Int2 data_at_exec;       /* Number of params needing SQLPutData */
    UDWORD iflag;            /* ESAPI_AllocStmt parameter */
    PutDataInfo pdata_info;
    po_ind_t parse_status;
    po_ind_t proc_return;
    po_ind_t put_data;          /* Has SQLPutData been called ? */
    po_ind_t catalog_result;    /* Is this a result of catalog function ? */
    po_ind_t prepare;           /* is this a prepared statement ? */
    po_ind_t prepared;          /* is this statement already
                                 * prepared at the server ? */
    po_ind_t external;          /* Allocated via SQLAllocHandle() */
    po_ind_t transition_status; /* Transition status */
    po_ind_t multi_statement;   /* -1:unknown 0:single 1:multi */
    po_ind_t rb_or_tc;          /* rollback on error */
    po_ind_t
        discard_output_params;  /* discard output parameters on parse stage */
    po_ind_t cancel_info;       /* cancel information */
    po_ind_t ref_CC_error;      /* refer to CC_error ? */
    po_ind_t lock_CC_for_rb;    /* lock CC for statement rollback ? */
    po_ind_t join_info;         /* have joins ? */
    po_ind_t parse_method;      /* parse_statement is forced or ? */
    esNAME cursor_name;
    char *plan_name;
    unsigned char miscinfo;
    unsigned char execinfo;
    po_ind_t updatable;
    SQLLEN diag_row_count;
    char *load_statement; /* to (re)load updatable individual rows */
    ssize_t from_pos;
    ssize_t load_from_pos;
    ssize_t where_pos;
    SQLLEN last_fetch_count_include_ommitted;
    time_t stmt_time;
    struct tm localtime;
    /* SQL_NEED_DATA Callback list */
    StatementClass *execute_delegate;
    StatementClass *execute_parent;
    UInt2 allocated_callbacks;
    UInt2 num_callbacks;
    NeedDataCallback *callbacks;
    void *cs;
};

#define SC_get_conn(a) ((a)->hdbc)
void SC_init_Result(StatementClass *self);
void SC_set_Result(StatementClass *self, QResultClass *res);
#define SC_get_Result(a) ((a)->result)
#define SC_set_Curres(a, b) ((a)->curres = b)
#define SC_get_Curres(a) ((a)->curres)
#define SC_get_ARD(a) ((a)->ard)
#define SC_get_APD(a) ((a)->apd)
#define SC_get_IRD(a) ((a)->ird)
#define SC_get_IPD(a) ((a)->ipd)
#define SC_get_ARDF(a) (&(SC_get_ARD(a)->ardf))
#define SC_get_APDF(a) (&(SC_get_APD(a)->apdf))
#define SC_get_IRDF(a) (&(SC_get_IRD(a)->irdf))
#define SC_get_IPDF(a) (&(SC_get_IPD(a)->ipdf))
#define SC_get_ARDi(a) (&((a)->ardi))
#define SC_get_APDi(a) (&((a)->apdi))
#define SC_get_IRDi(a) (&((a)->irdi))
#define SC_get_IPDi(a) (&((a)->ipdi))
#define SC_get_GDTI(a) (&((a)->gdata_info))
#define SC_get_PDTI(a) (&((a)->pdata_info))

#define SC_get_errornumber(a) ((a)->__error_number)
#define SC_set_errornumber(a, n) ((a)->__error_number = n)
#define SC_get_errormsg(a) ((a)->__error_message)
#define SC_is_prepare_statement(a) (0 != ((a)->prepare & PREPARE_STATEMENT))
#define SC_get_prepare_method(a) ((a)->prepare & (~PREPARE_STATEMENT))

#define SC_parsed_status(a) ((a)->parse_status & STMT_PARSE_MASK)
#define SC_set_parse_status(a, s) ((a)->parse_status |= s)
#define SC_update_not_ready(a)              \
    (SC_parsed_status(a) == STMT_PARSE_NONE \
     || 0 == ((a)->parse_status & STMT_PARSED_OIDS))
#define SC_update_ready(a)                      \
    (SC_parsed_status(a) == STMT_PARSE_COMPLETE \
     && 0 != ((a)->parse_status & STMT_FOUND_KEY) && (a)->updatable)
#define SC_set_checked_hasoids(a, b) \
    ((a)->parse_status |= (STMT_PARSED_OIDS | (b ? STMT_FOUND_KEY : 0)))
#define SC_checked_hasoids(a) (0 != ((a)->parse_status & STMT_PARSED_OIDS))
#define SC_set_delegate(p, c) \
    ((p)->execute_delegate = c, (c)->execute_parent = p)

#define SC_is_updatable(s) (0 < ((s)->updatable))
#define SC_reset_updatable(s) ((s)->updatable = -1)
#define SC_set_updatable(s, b) ((s)->updatable = (b))
#define SC_clear_parse_method(s) ((s)->parse_method = 0)
#define SC_is_parse_forced(s) (0 != ((s)->parse_method & 1L))
#define SC_set_parse_forced(s) ((s)->parse_method |= 1L)

#define SC_cursor_is_valid(s) (NAME_IS_VALID((s)->cursor_name))
#define SC_cursor_name(s) (SAFE_NAME((s)->cursor_name))

void SC_reset_delegate(RETCODE, StatementClass *);

#define SC_is_lower_case(a, b) \
    ((a)->options.metadata_id || (b)->connInfo.lower_case_identifier)

#define SC_MALLOC_return_with_error(t, tp, s, a, m, r)             \
    do {                                                           \
        if (t = (tp *)malloc(s), NULL == t) {                      \
            SC_set_error(a, STMT_NO_MEMORY_ERROR, m, "SC_MALLOC"); \
            return r;                                              \
        }                                                          \
    } while (0)
#define SC_MALLOC_gexit_with_error(t, tp, s, a, m, r)              \
    do {                                                           \
        if (t = (tp *)malloc(s), NULL == t) {                      \
            SC_set_error(a, STMT_NO_MEMORY_ERROR, m, "SC_MALLOC"); \
            r;                                                     \
            goto cleanup;                                          \
        }                                                          \
    } while (0)
#define SC_REALLOC_return_with_error(t, tp, s, a, m, r)             \
    do {                                                            \
        tp *tmp;                                                    \
        if (tmp = (tp *)realloc(t, s), NULL == tmp) {               \
            SC_set_error(a, STMT_NO_MEMORY_ERROR, m, "SC_REALLOC"); \
            return r;                                               \
        }                                                           \
        t = tmp;                                                    \
    } while (0)
#define SC_REALLOC_gexit_with_error(t, tp, s, a, m, r)              \
    do {                                                            \
        tp *tmp;                                                    \
        if (tmp = (tp *)realloc(t, s), NULL == tmp) {               \
            SC_set_error(a, STMT_NO_MEMORY_ERROR, m, __FUNCTION__); \
            r;                                                      \
            goto cleanup;                                           \
        }                                                           \
        t = tmp;                                                    \
    } while (0)

/*	options for SC_free_params() */
#define STMT_FREE_PARAMS_ALL 0
#define STMT_FREE_PARAMS_DATA_AT_EXEC_ONLY 1

/*	prepare state */
enum {
    NON_PREPARE_STATEMENT = 0,
    PREPARE_STATEMENT = 1,
    PREPARE_BY_THE_DRIVER = (1L << 1),
    NAMED_PARSE_REQUEST = (3L << 1),
    PARSE_TO_EXEC_ONCE = (4L << 1),
    PARSE_REQ_FOR_INFO = (5L << 1)
};

/*	prepared state */
enum {
    NOT_PREPARED = 0,
    PREPARED,
    EXECUTED
};

/*	misc info */
#define SC_set_fetchcursor(a) ((a)->miscinfo |= (1L << 1))
#define SC_no_fetchcursor(a) ((a)->miscinfo &= ~(1L << 1))
#define SC_is_fetchcursor(a) (((a)->miscinfo & (1L << 1)) != 0)
#define SC_miscinfo_clear(a) ((a)->miscinfo = 0)
#define SC_set_with_hold(a) ((a)->execinfo |= 1L)
#define SC_set_without_hold(a) ((a)->execinfo &= (~1L))
#define SC_is_with_hold(a) (((a)->execinfo & 1L) != 0)
#define SC_set_readonly(a) ((a)->execinfo |= (1L << 1))
#define SC_set_no_readonly(a) ((a)->execinfo &= ~(1L << 1))
#define SC_is_readonly(a) (((a)->execinfo & (1L << 1)) != 0)
#define SC_execinfo_clear(a)	(((a)->execinfo = 0)
#define STMT_HAS_OUTER_JOIN 1L
#define STMT_HAS_INNER_JOIN (1L << 1)
#define SC_has_join(a) (0 != (a)->join_info)
#define SC_has_outer_join(a) (0 != (STMT_HAS_OUTER_JOIN & (a)->join_info))
#define SC_has_inner_join(a) (0 != (STMT_HAS_INNER_JOIN & (a)->join_info))
#define SC_set_outer_join(a) ((a)->join_info |= STMT_HAS_OUTER_JOIN)
#define SC_set_inner_join(a) ((a)->join_info |= STMT_HAS_INNER_JOIN)

#define SC_start_tc_stmt(a) ((a)->rb_or_tc = (1L << 1))
#define SC_is_tc_stmt(a) (((a)->rb_or_tc & (1L << 1)) != 0)
#define SC_start_rb_stmt(a) ((a)->rb_or_tc = (1L << 2))
#define SC_is_rb_stmt(a) (((a)->rb_or_tc & (1L << 2)) != 0)
#define SC_unref_CC_error(a) (((a)->ref_CC_error) = FALSE)
#define SC_ref_CC_error(a) (((a)->ref_CC_error) = TRUE)
#define SC_can_parse_statement(a) (STMT_TYPE_SELECT == (a)->statement_type)
/*
 * DECLARE CURSOR + FETCH can only be used with SELECT-type queries. And
 * it's not currently supported with array-bound parameters.
 */
#define SC_may_use_cursor(a)                     \
    (SC_get_APDF(a)->paramset_size <= 1          \
     && (STMT_TYPE_SELECT == (a)->statement_type \
         || STMT_TYPE_WITH == (a)->statement_type))
#define SC_may_fetch_rows(a)                 \
    (STMT_TYPE_SELECT == (a)->statement_type \
     || STMT_TYPE_WITH == (a)->statement_type)

/* For Multi-thread */
#define INIT_STMT_CS(x) XPlatformInitializeCriticalSection(&((x)->cs))
#define ENTER_STMT_CS(x) XPlatformEnterCriticalSection(((x)->cs))
#define TRY_ENTER_STMT_CS(x) XPlatformTryEnterCriticalSection(&((x)->cs))
#define LEAVE_STMT_CS(x) XPlatformLeaveCriticalSection(((x)->cs))
#define DELETE_STMT_CS(x) XPlatformDeleteCriticalSection(&((x)->cs))

/*	Statement prototypes */
StatementClass *SC_Constructor(ConnectionClass *);
void InitializeStatementOptions(StatementOptions *opt);
char SC_Destructor(StatementClass *self);
BOOL SC_opencheck(StatementClass *self, const char *func);
RETCODE SC_initialize_and_recycle(StatementClass *self);
void SC_initialize_cols_info(StatementClass *self, BOOL DCdestroy,
                             BOOL parseReset);
void SC_reset_result_for_rerun(StatementClass *self);
int statement_type(const char *statement);
char SC_unbind_cols(StatementClass *self);
char SC_recycle_statement(StatementClass *self);
void SC_clear_error(StatementClass *self);
void SC_set_error(StatementClass *self, int errnum, const char *msg,
                  const char *func);
void SC_set_errormsg(StatementClass *self, const char *msg);
void SC_error_copy(StatementClass *self, const StatementClass *from, BOOL);
void SC_full_error_copy(StatementClass *self, const StatementClass *from, BOOL);
void SC_set_prepared(StatementClass *self, int);
void SC_set_planname(StatementClass *self, const char *plan_name);
void SC_set_rowset_start(StatementClass *self, SQLLEN, BOOL);
void SC_inc_rowset_start(StatementClass *self, SQLLEN);
RETCODE SC_initialize_stmts(StatementClass *self, BOOL);
RETCODE SC_fetch(StatementClass *self);
void SC_log_error(const char *func, const char *desc,
                  const StatementClass *self);
time_t SC_get_time(StatementClass *self);
struct tm *SC_get_localtime(StatementClass *self);
int SC_Create_bookmark(StatementClass *stmt, BindInfoClass *bookmark,
                       Int4 row_pos, Int4 currTuple, const KeySet *keyset);
int SC_set_current_col(StatementClass *self, int col);
BOOL SC_SetExecuting(StatementClass *self, BOOL on);
BOOL SC_SetCancelRequest(StatementClass *self);

BOOL SC_connection_lost_check(StatementClass *stmt, const char *funcname);
void SC_set_errorinfo(StatementClass *self, QResultClass *res, int errkind);
RETCODE dequeueNeedDataCallback(RETCODE, StatementClass *self);
void cancelNeedDataState(StatementClass *self);

/*
 *	Macros to convert global index <-> relative index in resultset/rowset
 */
/* a global index to the relative index in a rowset */
#define SC_get_rowset_start(stmt) ((stmt)->rowset_start)
#define GIdx2RowIdx(gidx, stmt) (gidx - (stmt)->rowset_start)
/* a global index to the relative index in a resultset(not a rowset) */
#define GIdx2CacheIdx(gidx, s, r) \
    (gidx - (QR_has_valid_base(r) ? ((s)->rowset_start - (r)->base) : 0))
#define GIdx2KResIdx(gidx, s, r) \
    (gidx - (QR_has_valid_base(r) ? ((s)->rowset_start - (r)->key_base) : 0))
/* a relative index in a rowset to the global index */
#define RowIdx2GIdx(ridx, stmt) (ridx + (stmt)->rowset_start)
/* a relative index in a resultset to the global index */
#define CacheIdx2GIdx(ridx, stmt, res) \
    (ridx - (res)->base + (stmt)->rowset_start)
#define KResIdx2GIdx(ridx, stmt, res) \
    (ridx - (res)->key_base + (stmt)->rowset_start)

#define BOOKMARK_SHIFT 1
#define SC_make_int4_bookmark(b) ((b < 0) ? (b) : (b + BOOKMARK_SHIFT))
#define SC_resolve_int4_bookmark(b) ((b < 0) ? (b) : (b - BOOKMARK_SHIFT))

#ifdef __cplusplus
}
#endif
#endif /* __STATEMENT_H__ */
