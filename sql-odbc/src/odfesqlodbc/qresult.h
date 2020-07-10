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

#ifndef __QRESULT_H__
#define __QRESULT_H__

#include "columninfo.h"
#include "es_connection.h"
#include "es_odbc.h"
#include "tuple.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef enum QueryResultCode_ {
    PORES_EMPTY_QUERY = 0,
    PORES_COMMAND_OK, /* a query command that doesn't return
                       * anything was executed properly by the backend */
    PORES_TUPLES_OK,  /* a query command that returns tuples
                       * was executed properly by the backend, ESresult
                       * contains the resulttuples */
    PORES_COPY_OUT,
    PORES_COPY_IN,
    PORES_BAD_RESPONSE, /* an unexpected response was recv'd from
                         * the backend */
    PORES_NONFATAL_ERROR,
    PORES_FATAL_ERROR,
    PORES_NO_MEMORY_ERROR,
    PORES_FIELDS_OK = 100, /* field information from a query was
                            * successful */
    /* PORES_END_TUPLES, */
    PORES_INTERNAL_ERROR
} QueryResultCode;

enum {
    FQR_REACHED_EOF = (1L << 1) /* reached eof */
    ,
    FQR_HAS_VALID_BASE = (1L << 2),
    FQR_NEEDS_SURVIVAL_CHECK = (1L << 3) /* check if the cursor is open */
};

struct QResultClass_ {
    ColumnInfoClass *fields; /* the Column information */
    ConnectionClass *conn;   /* the connection this result is using
                              * (backend) */
    QResultClass *next;      /* the following result class */

    /* Stuff for declare/fetch tuples */
    SQLULEN num_total_read; /* the highest absolute position ever read in + 1 */
    SQLULEN count_backend_allocated; /* m(re)alloced count */
    SQLULEN num_cached_rows; /* count of tuples kept in backend_tuples member */
    SQLLEN fetch_number;     /* 0-based index to the tuple to read next */
    SQLLEN cursTuple; /* absolute current position in the servr's cursor used to
                         retrieve tuples from the DB */
    SQLULEN move_offset;
    SQLLEN base; /* relative position of rowset start in the current data
                    cache(backend_tuples) */

    UInt2 num_fields;                   /* number of fields in the result */
    UInt2 num_key_fields;               /* number of key fields in the result */
    UInt4 rowset_size_include_ommitted; /* ES restriction */
    SQLLEN recent_processed_row_count;
    SQLULEN cache_size;
    SQLULEN cmd_fetch_size;

    QueryResultCode rstatus; /* result status */

    char sqlstate[8];
    char *message;
    const char *messageref;
    char *cursor_name; /* The name of the cursor for select statements */
    char *command;
    char *notice;

    TupleField *backend_tuples; /* data from the backend (the tuple cache) */
    TupleField *tupleField;     /* current backend tuple being retrieved */

    char pstatus;                   /* processing status */
    char aborted;                   /* was aborted ? */
    char flags;                     /* this result contains keyset etc ? */
    po_ind_t move_direction;        /* must move before fetching this
                            result set */
    SQLULEN count_keyset_allocated; /* m(re)alloced count */
    SQLULEN num_cached_keys; /* count of keys kept in backend_keys member */
    KeySet *keyset;
    SQLLEN key_base; /* relative position of rowset start in the current keyset
                        cache */
    UInt2 reload_count;
    UInt2 rb_alloc;  /* count of allocated rollback info */
    UInt2 rb_count;  /* count of rollback info */
    char dataFilled; /* Cache is filled with data ? */
    Rollback *rollback;
    UInt4 ad_alloc;             /* count of allocated added info */
    UInt4 ad_count;             /* count of newly added rows */
    KeySet *added_keyset;       /* added keyset info */
    TupleField *added_tuples;   /* added data by myself */
    UInt2 dl_alloc;             /* count of allocated deleted info */
    UInt2 dl_count;             /* count of deleted info */
    SQLLEN *deleted;            /* deleted index info */
    KeySet *deleted_keyset;     /* deleted keyset info */
    UInt2 up_alloc;             /* count of allocated updated info */
    UInt2 up_count;             /* count of updated info */
    SQLLEN *updated;            /* updated index info */
    KeySet *updated_keyset;     /* uddated keyset info */
    TupleField *updated_tuples; /* uddated data by myself */
    void *es_result;
    char *server_cursor_id;
};

enum {
    FQR_HASKEYSET = 1L,
    FQR_WITHHOLD = (1L << 1),
    FQR_HOLDPERMANENT = (1L << 2) /* the cursor is alive across transactions */
    ,
    FQR_SYNCHRONIZEKEYS =
        (1L
         << 3) /* synchronize the keyset range with that of cthe tuples cache */
};

#define QR_haskeyset(self) (0 != (self->flags & FQR_HASKEYSET))
#define QR_is_withhold(self) (0 != (self->flags & FQR_WITHHOLD))
#define QR_is_permanent(self) (0 != (self->flags & FQR_HOLDPERMANENT))
#define QR_synchronize_keys(self) (0 != (self->flags & FQR_SYNCHRONIZEKEYS))
#define QR_get_fields(self) (self->fields)

/*	These functions are for retrieving data from the qresult */
#define QR_get_value_backend(self, fieldno) (self->tupleField[fieldno].value)
#define QR_get_value_backend_row(self, tupleno, fieldno) \
    ((self->backend_tuples + (tupleno * self->num_fields))[fieldno].value)
#define QR_get_value_backend_text(self, tupleno, fieldno) \
    QR_get_value_backend_row(self, tupleno, fieldno)
#define QR_get_value_backend_int(self, tupleno, fieldno, isNull) \
    atoi(QR_get_value_backend_row(self, tupleno, fieldno))

/*	These functions are used by both manual and backend results */
#define QR_NumResultCols(self) (CI_get_num_fields(self->fields))
#define QR_NumPublicResultCols(self)                                \
    (QR_haskeyset(self)                                             \
         ? (CI_get_num_fields(self->fields) - self->num_key_fields) \
         : CI_get_num_fields(self->fields))
#define QR_get_fieldname(self, fieldno_) \
    (CI_get_fieldname(self->fields, fieldno_))
#define QR_get_fieldsize(self, fieldno_) \
    (CI_get_fieldsize(self->fields, fieldno_))
#define QR_get_display_size(self, fieldno_) \
    (CI_get_display_size(self->fields, fieldno_))
#define QR_get_atttypmod(self, fieldno_) \
    (CI_get_atttypmod(self->fields, fieldno_))
#define QR_get_field_type(self, fieldno_) (CI_get_oid(self->fields, fieldno_))
#define QR_get_relid(self, fieldno_) (CI_get_relid(self->fields, fieldno_))
#define QR_get_attid(self, fieldno_) (CI_get_attid(self->fields, fieldno_))

/*	These functions are used only for manual result sets */
#define QR_get_num_total_tuples(self)                                    \
    (QR_once_reached_eof(self) ? (self->num_total_read + self->ad_count) \
                               : self->num_total_read)
#define QR_get_num_total_read(self) (self->num_total_read)
#define QR_get_num_cached_tuples(self) (self->num_cached_rows)
#define QR_set_field_info(self, field_num, name, adtid, adtsize, relid, attid) \
    (CI_set_field_info(self->fields, field_num, name, adtid, adtsize, -1,      \
                       relid, attid))
#define QR_set_field_info_v(self, field_num, name, adtid, adtsize) \
    (CI_set_field_info(self->fields, field_num, name, adtid, adtsize, -1, 0, 0))

/* status macros */
#define QR_command_successful(self)                \
    (self                                          \
     && !(self->rstatus == PORES_BAD_RESPONSE      \
          || self->rstatus == PORES_NONFATAL_ERROR \
          || self->rstatus == PORES_FATAL_ERROR    \
          || self->rstatus == PORES_NO_MEMORY_ERROR))
#define QR_command_maybe_successful(self)       \
    (self                                       \
     && !(self->rstatus == PORES_BAD_RESPONSE   \
          || self->rstatus == PORES_FATAL_ERROR \
          || self->rstatus == PORES_NO_MEMORY_ERROR))
#define QR_command_nonfatal(self) (self->rstatus == PORES_NONFATAL_ERROR)
#define QR_set_conn(self, conn_) (self->conn = conn_)
#define QR_set_rstatus(self, condition) (self->rstatus = condition)
#define QR_set_sqlstatus(self, status) strcpy(self->sqlstatus, status)
#define QR_set_messageref(self, m) ((self)->messageref = m)
#define QR_set_aborted(self, aborted_) (self->aborted = aborted_)
#define QR_set_haskeyset(self) (self->flags |= FQR_HASKEYSET)
#define QR_set_synchronize_keys(self) (self->flags |= FQR_SYNCHRONIZEKEYS)
#define QR_set_no_cursor(self)                             \
    ((self)->flags &= ~(FQR_WITHHOLD | FQR_HOLDPERMANENT), \
     (self)->pstatus &= ~FQR_NEEDS_SURVIVAL_CHECK)
#define QR_set_withhold(self) (self->flags |= FQR_WITHHOLD)
#define QR_set_permanent(self) (self->flags |= FQR_HOLDPERMANENT)
#define QR_set_reached_eof(self) (self->pstatus |= FQR_REACHED_EOF)
#define QR_set_has_valid_base(self) (self->pstatus |= FQR_HAS_VALID_BASE)
#define QR_set_no_valid_base(self) (self->pstatus &= ~FQR_HAS_VALID_BASE)
#define QR_set_survival_check(self) (self->pstatus |= FQR_NEEDS_SURVIVAL_CHECK)
#define QR_set_no_survival_check(self) \
    (self->pstatus &= ~FQR_NEEDS_SURVIVAL_CHECK)
#define QR_inc_num_cache(self)       \
    do {                             \
        self->num_cached_rows++;     \
        if (QR_haskeyset(self))      \
            self->num_cached_keys++; \
    } while (0)
#define QR_set_next_in_cache(self, number)                               \
    do {                                                                 \
        MYLOG(ES_ALL, "set the number to " FORMAT_LEN " to read next\n", \
              number);                                                   \
        self->fetch_number = number;                                     \
    } while (0)
#define QR_inc_next_in_cache(self)                                             \
    do {                                                                       \
        MYLOG(ES_ALL, "increased the number " FORMAT_LEN, self->fetch_number); \
        self->fetch_number++;                                                  \
        MYLOG(ES_ALL, "to " FORMAT_LEN " to next read\n", self->fetch_number); \
    } while (0)

#define QR_get_message(self) \
    ((self)->message ? (self)->message : (self)->messageref)
#define QR_get_command(self) (self->command)
#define QR_get_notice(self) (self->notice)
#define QR_get_rstatus(self) (self->rstatus)
#define QR_get_aborted(self) (self->aborted)
#define QR_get_conn(self) (self->conn)
#define QR_get_cursor(self) (self->cursor_name)
#define QR_get_rowstart_in_cache(self) (self->base)
#define QR_once_reached_eof(self) ((self->pstatus & FQR_REACHED_EOF) != 0)
#define QR_has_valid_base(self) (0 != (self->pstatus & FQR_HAS_VALID_BASE))
#define QR_needs_survival_check(self) \
    (0 != (self->pstatus & FQR_NEEDS_SURVIVAL_CHECK))

#define QR_aborted(self) (!self || self->aborted)
#define QR_get_reqsize(self) (self->rowset_size_include_ommitted)

#define QR_stop_movement(self) (self->move_direction = 0)
#define QR_is_moving(self) (0 != self->move_direction)
#define QR_is_not_moving(self) (0 == self->move_direction)
#define QR_set_move_forward(self) (self->move_direction = 1)
#define QR_is_moving_forward(self) (1 == self->move_direction)
#define QR_set_move_backward(self) (self->move_direction = -1)
#define QR_is_moving_backward(self) (-1 == self->move_direction)
#define QR_set_move_from_the_last(self) (self->move_direction = 2)
#define QR_is_moving_from_the_last(self) (2 == self->move_direction)
#define QR_is_moving_not_backward(self) (0 < self->move_direction)

/*	Core Functions */
QResultClass *QR_Constructor(void);
void QR_Destructor(QResultClass *self);
TupleField *QR_AddNew(QResultClass *self);
void QR_close_result(QResultClass *self, BOOL destroy);
void QR_reset_for_re_execute(QResultClass *self);
void QR_free_memory(QResultClass *self);
void QR_set_command(QResultClass *self, const char *msg);
void QR_set_message(QResultClass *self, const char *msg);
void QR_add_message(QResultClass *self, const char *msg);
void QR_set_notice(QResultClass *self, const char *msg);
void QR_add_notice(QResultClass *self, const char *msg);

void QR_set_num_fields(QResultClass *self,
                       int new_num_fields); /* catalog functions' result only */
void QR_set_fields(QResultClass *self, ColumnInfoClass *);

void QR_set_rowstart_in_cache(QResultClass *, SQLLEN);
void QR_inc_rowstart_in_cache(QResultClass *self, SQLLEN base_inc);
void QR_set_reqsize(QResultClass *self, Int4 reqsize);
void QR_set_position(QResultClass *self, SQLLEN pos);
void QR_set_cursor(QResultClass *self, const char *name);
SQLLEN getNthValid(const QResultClass *self, SQLLEN sta, UWORD orientation,
                   SQLULEN nth, SQLLEN *nearest);
void QR_set_server_cursor_id(QResultClass *self, const char *server_cursor_id);
#define QR_MALLOC_return_with_error(t, tp, s, a, m, r) \
    do {                                               \
        if (t = (tp *)malloc(s), NULL == t) {          \
            QR_set_rstatus(a, PORES_NO_MEMORY_ERROR);  \
            qlog("QR_MALLOC_error\n");                 \
            QR_free_memory(a);                         \
            QR_set_messageref(a, m);                   \
            return r;                                  \
        }                                              \
    } while (0)
#define QR_REALLOC_return_with_error(t, tp, s, a, m, r) \
    do {                                                \
        tp *tmp;                                        \
        if (tmp = (tp *)realloc(t, s), NULL == tmp) {   \
            QR_set_rstatus(a, PORES_NO_MEMORY_ERROR);   \
            qlog("QR_REALLOC_error\n");                 \
            QR_free_memory(a);                          \
            QR_set_messageref(a, m);                    \
            return r;                                   \
        }                                               \
        t = tmp;                                        \
    } while (0)

#ifdef __cplusplus
}
#endif
#endif /* __QRESULT_H__ */
