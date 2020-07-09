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

#include "qresult.h"

#include <limits.h>
#include <stdio.h>
#include <string.h>

#include "es_statement.h"
#include "misc.h"
#include "statement.h"

/*
 *	Used for building a Manual Result only
 *	All info functions call this function to create the manual result set.
 */
void QR_set_num_fields(QResultClass *self, int new_num_fields) {
    if (!self)
        return;
    MYLOG(ES_TRACE, "entering\n");

    CI_set_num_fields(QR_get_fields(self), (SQLSMALLINT)new_num_fields);

    MYLOG(ES_TRACE, "leaving\n");
}

void QR_set_position(QResultClass *self, SQLLEN pos) {
    self->tupleField =
        self->backend_tuples
        + ((QR_get_rowstart_in_cache(self) + pos) * self->num_fields);
}

void QR_set_reqsize(QResultClass *self, Int4 reqsize) {
    self->rowset_size_include_ommitted = reqsize;
}

void QR_set_cursor(QResultClass *self, const char *name) {
    ConnectionClass *conn = QR_get_conn(self);

    if (self->cursor_name) {
        if (name && 0 == strcmp(name, self->cursor_name))
            return;
        free(self->cursor_name);
        if (conn) {
            CONNLOCK_ACQUIRE(conn);
            conn->ncursors--;
            CONNLOCK_RELEASE(conn);
        }
        self->cursTuple = -1;
        QR_set_no_cursor(self);
    } else if (NULL == name)
        return;
    if (name) {
        self->cursor_name = strdup(name);
        if (conn) {
            CONNLOCK_ACQUIRE(conn);
            conn->ncursors++;
            CONNLOCK_RELEASE(conn);
        }
    } else {
        QResultClass *res;

        self->cursor_name = NULL;
        for (res = self->next; NULL != res; res = res->next) {
            if (NULL != res->cursor_name)
                free(res->cursor_name);
            res->cursor_name = NULL;
        }
    }
}

void QR_set_rowstart_in_cache(QResultClass *self, SQLLEN start) {
    if (QR_synchronize_keys(self))
        self->key_base = start;
    self->base = start;
}

void QR_inc_rowstart_in_cache(QResultClass *self, SQLLEN base_inc) {
    if (!QR_has_valid_base(self))
        MYLOG(ES_DEBUG, " called while the cache is not ready\n");
    self->base += base_inc;
    if (QR_synchronize_keys(self))
        self->key_base = self->base;
}

void QR_set_fields(QResultClass *self, ColumnInfoClass *fields) {
    ColumnInfoClass *curfields = QR_get_fields(self);

    if (curfields == fields)
        return;

    /*
     * Unlink the old columninfo from this result set, freeing it if this
     * was the last reference.
     */
    if (NULL != curfields) {
        if (curfields->refcount > 1)
            curfields->refcount--;
        else
            CI_Destructor(curfields);
    }
    self->fields = fields;
    if (NULL != fields)
        fields->refcount++;
}

/*
 * CLASS QResult
 */
QResultClass *QR_Constructor(void) {
    QResultClass *rv;

    MYLOG(ES_TRACE, "entering\n");
    rv = (QResultClass *)malloc(sizeof(QResultClass));

    if (rv != NULL) {
        ColumnInfoClass *fields;

        rv->rstatus = PORES_EMPTY_QUERY;
        rv->pstatus = 0;

        /* construct the column info */
        rv->fields = NULL;
        if (fields = CI_Constructor(), NULL == fields) {
            free(rv);
            return NULL;
        }
        QR_set_fields(rv, fields);
        rv->backend_tuples = NULL;
        rv->sqlstate[0] = '\0';
        rv->message = NULL;
        rv->messageref = NULL;
        rv->command = NULL;
        rv->notice = NULL;
        rv->conn = NULL;
        rv->next = NULL;
        rv->count_backend_allocated = 0;
        rv->count_keyset_allocated = 0;
        rv->num_total_read = 0;
        rv->num_cached_rows = 0;
        rv->num_cached_keys = 0;
        rv->fetch_number = 0;
        rv->flags =
            0; /* must be cleared before calling QR_set_rowstart_in_cache() */
        QR_set_rowstart_in_cache(rv, -1);
        rv->key_base = -1;
        rv->recent_processed_row_count = -1;
        rv->cursTuple = -1;
        rv->move_offset = 0;
        rv->num_fields = 0;
        rv->num_key_fields = ES_NUM_NORMAL_KEYS; /* CTID + OID */
        rv->tupleField = NULL;
        rv->cursor_name = NULL;
        rv->aborted = FALSE;

        rv->cache_size = 0;
        rv->cmd_fetch_size = 0;
        rv->rowset_size_include_ommitted = 1;
        rv->move_direction = 0;
        rv->keyset = NULL;
        rv->reload_count = 0;
        rv->rb_alloc = 0;
        rv->rb_count = 0;
        rv->dataFilled = FALSE;
        rv->rollback = NULL;
        rv->ad_alloc = 0;
        rv->ad_count = 0;
        rv->added_keyset = NULL;
        rv->added_tuples = NULL;
        rv->up_alloc = 0;
        rv->up_count = 0;
        rv->updated = NULL;
        rv->updated_keyset = NULL;
        rv->updated_tuples = NULL;
        rv->dl_alloc = 0;
        rv->dl_count = 0;
        rv->deleted = NULL;
        rv->deleted_keyset = NULL;
        rv->es_result = NULL;
        rv->server_cursor_id = NULL;
    }

    MYLOG(ES_TRACE, "leaving\n");
    return rv;
}

void QR_close_result(QResultClass *self, BOOL destroy) {
    UNUSED(self);
    QResultClass *next;
    BOOL top = TRUE;

    if (!self)
        return;
    MYLOG(ES_TRACE, "entering\n");

    while (self) {
        QR_free_memory(self); /* safe to call anyway */

        /*
         * Should have been freed in the close() but just in case...
         * QR_set_cursor clears the cursor name of all the chained results too,
         * so we only need to do this for the first result in the chain.
         */
        if (top)
            QR_set_cursor(self, NULL);

        /* Free up column info */
        if (destroy)
            QR_set_fields(self, NULL);

        /* Free command info (this is from strdup()) */
        if (self->command) {
            free(self->command);
            self->command = NULL;
        }

        /* Free message info (this is from strdup()) */
        if (self->message) {
            free(self->message);
            self->message = NULL;
        }

        /* Free notice info (this is from strdup()) */
        if (self->notice) {
            free(self->notice);
            self->notice = NULL;
        }

        /* Free server_cursor_id (this is from strdup()) */
        if (self->server_cursor_id) {
            free(self->server_cursor_id);
            self->server_cursor_id = NULL;
        }

        /* Destruct the result object in the chain */
        next = self->next;
        self->next = NULL;
        if (destroy)
            free(self);

        /* Repeat for the next result in the chain */
        self = next;
        destroy = TRUE; /* always destroy chained results */
        top = FALSE;
    }

    MYLOG(ES_TRACE, "leaving\n");
}

void QR_reset_for_re_execute(QResultClass *self) {
    MYLOG(ES_TRACE, "entering for %p\n", self);
    if (!self)
        return;
    QR_close_result(self, FALSE);
    /* reset flags etc */
    self->flags = 0;
    QR_set_rowstart_in_cache(self, -1);
    self->recent_processed_row_count = -1;
    /* clear error info etc */
    self->rstatus = PORES_EMPTY_QUERY;
    self->aborted = FALSE;
    self->sqlstate[0] = '\0';
    self->messageref = NULL;

    MYLOG(ES_TRACE, "leaving\n");
}

void QR_Destructor(QResultClass *self) {
    MYLOG(ES_TRACE, "entering\n");
    if (!self)
        return;
    QR_close_result(self, TRUE);

    MYLOG(ES_TRACE, "leaving\n");
}

void QR_set_command(QResultClass *self, const char *msg) {
    if (self->command)
        free(self->command);

    self->command = msg ? strdup(msg) : NULL;
}

void QR_set_message(QResultClass *self, const char *msg) {
    if (self->message)
        free(self->message);
    self->messageref = NULL;

    self->message = msg ? strdup(msg) : NULL;
}

void QR_set_server_cursor_id(QResultClass *self, const char *server_cursor_id) {
    if (self->server_cursor_id) {
        free(self->server_cursor_id);
    }

    self->server_cursor_id = server_cursor_id ? strdup(server_cursor_id) : NULL;
}

void QR_add_message(QResultClass *self, const char *msg) {
    char *message = self->message;
    size_t alsize, pos, addlen;

    if (!msg || !msg[0])
        return;
    addlen = strlen(msg);
    if (message) {
        pos = strlen(message) + 1;
        alsize = pos + addlen + 1;
    } else {
        pos = 0;
        alsize = addlen + 1;
    }
    char *message_tmp = realloc(message, alsize);
    if (message_tmp) {
        message = message_tmp;
        if (pos > 0)
            message[pos - 1] = ';';
        strncpy_null(message + pos, msg, addlen + 1);
        self->message = message;
    }
}

void QR_set_notice(QResultClass *self, const char *msg) {
    if (self->notice)
        free(self->notice);

    self->notice = msg ? strdup(msg) : NULL;
}

void QR_add_notice(QResultClass *self, const char *msg) {
    char *message = self->notice;
    size_t alsize, pos, addlen;

    if (!msg || !msg[0])
        return;
    addlen = strlen(msg);
    if (message) {
        pos = strlen(message) + 1;
        alsize = pos + addlen + 1;
    } else {
        pos = 0;
        alsize = addlen + 1;
    }
    char *message_tmp = realloc(message, alsize);
    if (message_tmp) {
        message = message_tmp;
        if (pos > 0)
            message[pos - 1] = ';';
        strncpy_null(message + pos, msg, addlen + 1);
        self->notice = message;
    }
}

TupleField *QR_AddNew(QResultClass *self) {
    size_t alloc;
    UInt4 num_fields;

    if (!self)
        return NULL;
    MYLOG(ES_ALL, FORMAT_ULEN "th row(%d fields) alloc=" FORMAT_LEN "\n",
          self->num_cached_rows, QR_NumResultCols(self),
          self->count_backend_allocated);
    if (num_fields = QR_NumResultCols(self), !num_fields)
        return NULL;
    if (self->num_fields <= 0) {
        self->num_fields = (unsigned short)num_fields;
        QR_set_reached_eof(self);
    }
    alloc = self->count_backend_allocated;
    if (!self->backend_tuples) {
        self->num_cached_rows = 0;
        alloc = TUPLE_MALLOC_INC;
        QR_MALLOC_return_with_error(self->backend_tuples, TupleField,
                                    alloc * sizeof(TupleField) * num_fields,
                                    self, "Out of memory in QR_AddNew.", NULL);
    } else if (self->num_cached_rows >= self->count_backend_allocated) {
        alloc = self->count_backend_allocated * 2;
        QR_REALLOC_return_with_error(self->backend_tuples, TupleField,
                                     alloc * sizeof(TupleField) * num_fields,
                                     self, "Out of memory in QR_AddNew.", NULL);
    }
    self->count_backend_allocated = alloc;

    if (self->backend_tuples) {
        memset(self->backend_tuples + num_fields * self->num_cached_rows, 0,
               num_fields * sizeof(TupleField));
        self->num_cached_rows++;
        self->ad_count++;
    }
    return self->backend_tuples + num_fields * (self->num_cached_rows - 1);
}

void QR_free_memory(QResultClass *self) {
    SQLLEN num_backend_rows = self->num_cached_rows;
    int num_fields = self->num_fields;

    MYLOG(ES_TRACE, "entering fcount=" FORMAT_LEN "\n", num_backend_rows);

    if (self->backend_tuples) {
        ClearCachedRows(self->backend_tuples, num_fields, num_backend_rows);
        free(self->backend_tuples);
        self->count_backend_allocated = 0;
        self->backend_tuples = NULL;
        self->dataFilled = FALSE;
        self->tupleField = NULL;
    }
    if (self->keyset) {
        free(self->keyset);
        self->keyset = NULL;
        self->count_keyset_allocated = 0;
        self->reload_count = 0;
    }
    if (self->rollback) {
        free(self->rollback);
        self->rb_alloc = 0;
        self->rb_count = 0;
        self->rollback = NULL;
    }
    if (self->deleted) {
        free(self->deleted);
        self->deleted = NULL;
    }
    if (self->deleted_keyset) {
        free(self->deleted_keyset);
        self->deleted_keyset = NULL;
    }
    self->dl_alloc = 0;
    self->dl_count = 0;
    /* clear added info */
    if (self->added_keyset) {
        free(self->added_keyset);
        self->added_keyset = NULL;
    }
    if (self->added_tuples) {
        ClearCachedRows(self->added_tuples, num_fields, self->ad_count);
        free(self->added_tuples);
        self->added_tuples = NULL;
    }
    self->ad_alloc = 0;
    self->ad_count = 0;
    /* clear updated info */
    if (self->updated) {
        free(self->updated);
        self->updated = NULL;
    }
    if (self->updated_keyset) {
        free(self->updated_keyset);
        self->updated_keyset = NULL;
    }
    if (self->updated_tuples) {
        ClearCachedRows(self->updated_tuples, num_fields, self->up_count);
        free(self->updated_tuples);
        self->updated_tuples = NULL;
    }
    if (self->es_result) {
        ClearESResult(self->es_result);
        self->es_result = NULL;
    }

    self->up_alloc = 0;
    self->up_count = 0;

    self->num_total_read = 0;
    self->num_cached_rows = 0;
    self->num_cached_keys = 0;
    self->cursTuple = -1;
    self->pstatus = 0;

    MYLOG(ES_TRACE, "leaving\n");
}
