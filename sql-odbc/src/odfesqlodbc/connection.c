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

/*	TryEnterCritiaclSection needs the following #define */
#ifndef _WIN32_WINNT
#define _WIN32_WINNT 0x0400
#endif /* _WIN32_WINNT */

#include <ctype.h>
#include <stdio.h>
#include <string.h>

#include "es_connection.h"
#include "misc.h"

/* for htonl */
#ifdef WIN32
#include <Winsock2.h>
#else
#include <arpa/inet.h>
#endif

#include "dlg_specific.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_helper.h"
#include "loadlib.h"
#include "multibyte.h"
#include "qresult.h"
#include "statement.h"
#ifndef WIN32
#include <limits.h>
#endif
#define SAFE_STR(s) (NULL != (s) ? (s) : "(null)")

#define ELASTIC_MAXIMUM_ID_LEN SHRT_MAX  // Max 16-bit signed int
#define ELASTIC_TRANSACTION_SUPPORT 0    // Not supported
#define STMT_INCREMENT                             \
    16 /* how many statement holders to allocate \ \
        * at a time */

#define PROTOCOL3_OPTS_MAX 30

RETCODE SQL_API ESAPI_AllocConnect(HENV henv, HDBC *phdbc) {
    EnvironmentClass *env = (EnvironmentClass *)henv;
    ConnectionClass *conn;
    CSTR func = "ESAPI_AllocConnect";

    MYLOG(ES_TRACE, "entering...\n");

    conn = CC_Constructor();
    MYLOG(ES_DEBUG, "**** henv = %p, conn = %p\n", henv, conn);

    if (!conn) {
        env->errormsg = "Couldn't allocate memory for Connection object.";
        env->errornumber = ENV_ALLOC_ERROR;
        *phdbc = SQL_NULL_HDBC;
        EN_log_error(func, "", env);
        return SQL_ERROR;
    }

    if (!EN_add_connection(env, conn)) {
        env->errormsg = "Maximum number of connections exceeded.";
        env->errornumber = ENV_ALLOC_ERROR;
        CC_Destructor(conn);
        *phdbc = SQL_NULL_HDBC;
        EN_log_error(func, "", env);
        return SQL_ERROR;
    }

    if (phdbc)
        *phdbc = (HDBC)conn;

    return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_Connect(HDBC hdbc, const SQLCHAR *szDSN,
                              SQLSMALLINT cbDSN, const SQLCHAR *szUID,
                              SQLSMALLINT cbUID, const SQLCHAR *szAuthStr,
                              SQLSMALLINT cbAuthStr) {
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    ConnInfo *ci;
    CSTR func = "ESAPI_Connect";
    RETCODE ret = SQL_SUCCESS;
    char fchar, *tmpstr;

    MYLOG(ES_TRACE, "entering..cbDSN=%hi.\n", cbDSN);

    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    ci = &conn->connInfo;
    CC_conninfo_init(ci, INIT_GLOBALS);

    make_string(szDSN, cbDSN, ci->dsn, sizeof(ci->dsn));

    /* get the values for the DSN from the registry */
    getDSNinfo(ci, NULL);

    logs_on_off(1, ci->drivers.loglevel, ci->drivers.loglevel);
    /* initialize es_version from connInfo.protocol    */
    CC_initialize_es_version(conn);

    /*
     * override values from DSN info with UID and authStr(pwd) This only
     * occurs if the values are actually there.
     */
    fchar = ci->username[0]; /* save the first byte */
    make_string(szUID, cbUID, ci->username, sizeof(ci->username));
    if ('\0' == ci->username[0]) /* an empty string is specified */
        ci->username[0] = fchar; /* restore the original username */
    tmpstr = make_string(szAuthStr, cbAuthStr, NULL, 0);
    if (tmpstr) {
        if (tmpstr[0]) /* non-empty string is specified */
            STR_TO_NAME(ci->password, tmpstr);
        free(tmpstr);
    }

    MYLOG(ES_DEBUG, "conn = %p (DSN='%s', UID='%s', PWD='%s')\n", conn, ci->dsn,
          ci->username, NAME_IS_VALID(ci->password) ? "xxxxx" : "");

    if ((fchar = CC_connect(conn)) <= 0) {
        /* Error messages are filled in */
        CC_log_error(func, "Error on CC_connect", conn);
        ret = SQL_ERROR;
    }
    if (SQL_SUCCESS == ret && 2 == fchar)
        ret = SQL_SUCCESS_WITH_INFO;

    MYLOG(ES_TRACE, "leaving..%d.\n", ret);

    return ret;
}

RETCODE SQL_API ESAPI_BrowseConnect(HDBC hdbc, const SQLCHAR *szConnStrIn,
                                    SQLSMALLINT cbConnStrIn,
                                    SQLCHAR *szConnStrOut,
                                    SQLSMALLINT cbConnStrOutMax,
                                    SQLSMALLINT *pcbConnStrOut) {
    UNUSED(szConnStrIn, cbConnStrIn, szConnStrOut, cbConnStrOutMax,
           cbConnStrOutMax, pcbConnStrOut);
    CSTR func = "ESAPI_BrowseConnect";
    ConnectionClass *conn = (ConnectionClass *)hdbc;

    MYLOG(ES_TRACE, "entering...\n");

    CC_set_error(conn, CONN_NOT_IMPLEMENTED_ERROR, "Function not implemented",
                 func);
    return SQL_ERROR;
}

/* Drop any hstmts open on hdbc and disconnect from database */
RETCODE SQL_API ESAPI_Disconnect(HDBC hdbc) {
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    CSTR func = "ESAPI_Disconnect";
    RETCODE ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");

    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    if (conn->status == CONN_EXECUTING) {
        // This should only be possible if transactions are supported, but they
        // are not. Return an error regardless
        CC_set_error(conn, CONN_IN_USE, "Connection is currently in use!",
                     func);
        return SQL_ERROR;
    }

    logs_on_off(-1, conn->connInfo.drivers.loglevel,
                conn->connInfo.drivers.loglevel);
    MYLOG(ES_DEBUG, "about to CC_cleanup\n");

    /* Close the connection and free statements */
    ret = CC_cleanup(conn, FALSE);

    MYLOG(ES_DEBUG, "done CC_cleanup\n");
    MYLOG(ES_TRACE, "leaving...\n");

    return ret;
}

RETCODE SQL_API ESAPI_FreeConnect(HDBC hdbc) {
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    CSTR func = "ESAPI_FreeConnect";
    EnvironmentClass *env;

    MYLOG(ES_TRACE, "entering...hdbc=%p\n", hdbc);

    if (!conn) {
        CC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    /* Remove the connection from the environment */
    if (NULL != (env = CC_get_env(conn)) && !EN_remove_connection(env, conn)) {
        // This should only be possible if transactions are supported, but they
        // are not. Return an error regardless
        CC_set_error(conn, CONN_IN_USE, "Connection is currently in use!",
                     func);
        return SQL_ERROR;
    }

    CC_Destructor(conn);

    MYLOG(ES_TRACE, "leaving...\n");

    return SQL_SUCCESS;
}

/*
 *		IMPLEMENTATION CONNECTION CLASS
 */

static void reset_current_schema(ConnectionClass *self) {
    if (self->current_schema) {
        free(self->current_schema);
        self->current_schema = NULL;
    }
    self->current_schema_valid = FALSE;
}

static ConnectionClass *CC_alloc(void) {
    return (ConnectionClass *)calloc(sizeof(ConnectionClass), 1);
}

static void CC_lockinit(ConnectionClass *self) {
    UNUSED(self);
    INIT_CONNLOCK(self);
    INIT_CONN_CS(self);
}

static ConnectionClass *CC_initialize(ConnectionClass *rv, BOOL lockinit) {
    size_t clear_size;

    clear_size = (char *)&(rv->cs) - (char *)rv;

    memset(rv, 0, clear_size);
    rv->status = CONN_NOT_CONNECTED;
    rv->transact_status = CONN_IN_AUTOCOMMIT; /* autocommit by default */
    rv->unnamed_prepared_stmt = NULL;

    rv->stmts =
        (StatementClass **)malloc(sizeof(StatementClass *) * STMT_INCREMENT);
    if (!rv->stmts)
        goto cleanup;
    memset(rv->stmts, 0, sizeof(StatementClass *) * STMT_INCREMENT);

    rv->num_stmts = STMT_INCREMENT;
    rv->descs =
        (DescriptorClass **)malloc(sizeof(DescriptorClass *) * STMT_INCREMENT);
    if (!rv->descs)
        goto cleanup;
    memset(rv->descs, 0, sizeof(DescriptorClass *) * STMT_INCREMENT);

    rv->num_descs = STMT_INCREMENT;

    rv->lobj_type = ES_TYPE_LO_UNDEFINED;
    if (isMsAccess())
        rv->ms_jet = 1;
    rv->isolation = 0;  // means initially unknown server's default isolation
    rv->mb_maxbyte_per_char = 1;
    rv->max_identifier_length = ELASTIC_MAXIMUM_ID_LEN;
    rv->autocommit_public = SQL_AUTOCOMMIT_ON;

    /* Initialize statement options to defaults */
    /* Statements under this conn will inherit these options */

    InitializeStatementOptions(&rv->stmtOptions);
    InitializeARDFields(&rv->ardOptions);
    InitializeAPDFields(&rv->apdOptions);
#ifdef _HANDLE_ENLIST_IN_DTC_
    rv->asdum = NULL;
    rv->gTranInfo = 0;
#endif /* _HANDLE_ENLIST_IN_DTC_ */
    if (lockinit)
        CC_lockinit(rv);

    return rv;

cleanup:
    CC_Destructor(rv);
    return NULL;
}

ConnectionClass *CC_Constructor() {
    ConnectionClass *rv, *retrv = NULL;

    if (rv = CC_alloc(), NULL != rv)
        retrv = CC_initialize(rv, TRUE);
    return retrv;
}

char CC_Destructor(ConnectionClass *self) {
    MYLOG(ES_TRACE, "entering self=%p\n", self);

    if (self->status == CONN_EXECUTING)
        return 0;

    CC_cleanup(self, FALSE); /* cleanup socket and statements */

    MYLOG(ES_DEBUG, "after CC_Cleanup\n");

    /* Free up statement holders */
    if (self->stmts) {
        free(self->stmts);
        self->stmts = NULL;
    }
    if (self->descs) {
        free(self->descs);
        self->descs = NULL;
    }
    MYLOG(ES_DEBUG, "after free statement holders\n");

    NULL_THE_NAME(self->schemaIns);
    NULL_THE_NAME(self->tableIns);
    CC_conninfo_release(&self->connInfo);
    if (self->__error_message)
        free(self->__error_message);
    DELETE_CONN_CS(self);
    DELETE_CONNLOCK(self);
    free(self);

    MYLOG(ES_TRACE, "leaving\n");

    return 1;
}

void CC_clear_error(ConnectionClass *self) {
    if (!self)
        return;
    CONNLOCK_ACQUIRE(self);
    self->__error_number = 0;
    if (self->__error_message) {
        free(self->__error_message);
        self->__error_message = NULL;
    }
    self->sqlstate[0] = '\0';
    CONNLOCK_RELEASE(self);
}

/* This is called by SQLSetConnectOption etc also */
BOOL CC_set_autocommit(ConnectionClass *self, BOOL on) {
    BOOL currsts = CC_is_in_autocommit(self);

    if ((on && currsts) || (!on && !currsts))
        return on;
    MYLOG(ES_DEBUG, " %d->%d\n", currsts, on);
    if (on)
        self->transact_status |= CONN_IN_AUTOCOMMIT;
    else
        self->transact_status &= ~CONN_IN_AUTOCOMMIT;

    return on;
}

/* Clear cached table info */
static void CC_clear_col_info(ConnectionClass *self, BOOL destroy) {
    if (self->col_info) {
        int i;
        COL_INFO *coli;

        for (i = 0; i < self->ntables; i++) {
            if (coli = self->col_info[i], NULL != coli) {
                if (destroy || coli->refcnt == 0) {
                    free_col_info_contents(coli);
                    free(coli);
                    self->col_info[i] = NULL;
                } else
                    coli->acc_time = 0;
            }
        }
        self->ntables = 0;
        if (destroy) {
            free(self->col_info);
            self->col_info = NULL;
            self->coli_allocated = 0;
        }
    }
}

/* This is called by SQLDisconnect also */
RETCODE
CC_cleanup(ConnectionClass *self, BOOL keepCommunication) {
    int i;
    StatementClass *stmt;
    DescriptorClass *desc;
    RETCODE ret = SQL_SUCCESS;
    CSTR func = "CC_cleanup";

    if (self->status == CONN_EXECUTING)
        return FALSE;

    MYLOG(ES_TRACE, "entering self=%p\n", self);

    ENTER_CONN_CS(self);
    /* Cancel an ongoing transaction */
    /* We are always in the middle of a transaction, */
    /* even if we are in auto commit. */
    if (self->esconn) {
        QLOG(0, "LIBES_disconnect: %p\n", self->esconn);
        LIBES_disconnect(self->esconn);
        self->esconn = NULL;
    } else {
        ret = SQL_ERROR;
        CC_set_error(self, CC_not_connected(self), "Connection not open", func);
    }

    MYLOG(ES_DEBUG, "after LIBES_disconnect\n");

    /* Free all the stmts on this connection */
    for (i = 0; i < self->num_stmts; i++) {
        stmt = self->stmts[i];
        if (stmt) {
            stmt->hdbc = NULL; /* prevent any more dbase interactions */

            SC_Destructor(stmt);

            self->stmts[i] = NULL;
        }
    }
    /* Free all the descs on this connection */
    for (i = 0; i < self->num_descs; i++) {
        desc = self->descs[i];
        if (desc) {
            DC_get_conn(desc) = NULL; /* prevent any more dbase interactions */
            DC_Destructor(desc);
            free(desc);
            self->descs[i] = NULL;
        }
    }

    /* Check for translation dll */
#ifdef WIN32
    if (!keepCommunication && self->translation_handle) {
        FreeLibrary(self->translation_handle);
        self->translation_handle = NULL;
    }
#endif

    if (!keepCommunication) {
        self->status = CONN_NOT_CONNECTED;
        self->transact_status = CONN_IN_AUTOCOMMIT;
        self->unnamed_prepared_stmt = NULL;
    }
    if (!keepCommunication) {
        CC_conninfo_init(&(self->connInfo), CLEANUP_FOR_REUSE);
        if (self->original_client_encoding) {
            free(self->original_client_encoding);
            self->original_client_encoding = NULL;
        }
        if (self->locale_encoding) {
            free(self->locale_encoding);
            self->locale_encoding = NULL;
        }
        if (self->server_encoding) {
            free(self->server_encoding);
            self->server_encoding = NULL;
        }
        reset_current_schema(self);
    }
    /* Free cached table info */
    CC_clear_col_info(self, TRUE);
    if (self->num_discardp > 0 && self->discardp) {
        for (i = 0; i < self->num_discardp; i++)
            free(self->discardp[i]);
        self->num_discardp = 0;
    }
    if (self->discardp) {
        free(self->discardp);
        self->discardp = NULL;
    }

    LEAVE_CONN_CS(self);
    MYLOG(ES_TRACE, "leaving\n");
    return ret;
}

#ifndef ES_DIAG_SEVERITY_NONLOCALIZED
#define ES_DIAG_SEVERITY_NONLOCALIZED 'V'
#endif

#define TRANSACTION_ISOLATION "transaction_isolation"
#define ISOLATION_SHOW_QUERY "show " TRANSACTION_ISOLATION

char CC_add_statement(ConnectionClass *self, StatementClass *stmt) {
    int i;
    char ret = TRUE;

    MYLOG(ES_DEBUG, "self=%p, stmt=%p\n", self, stmt);

    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++) {
        if (!self->stmts[i]) {
            stmt->hdbc = self;
            self->stmts[i] = stmt;
            break;
        }
    }

    if (i >= self->num_stmts) /* no more room -- allocate more memory */
    {
        StatementClass **newstmts;
        Int2 new_num_stmts;

        new_num_stmts = STMT_INCREMENT + self->num_stmts;

        if (new_num_stmts > 0)
            newstmts = (StatementClass **)realloc(
                self->stmts, sizeof(StatementClass *) * new_num_stmts);
        else
            newstmts = NULL; /* num_stmts overflowed */
        if (!newstmts)
            ret = FALSE;
        else {
            self->stmts = newstmts;
            memset(&self->stmts[self->num_stmts], 0,
                   sizeof(StatementClass *) * STMT_INCREMENT);

            stmt->hdbc = self;
            self->stmts[self->num_stmts] = stmt;

            self->num_stmts = new_num_stmts;
        }
    }
    CONNLOCK_RELEASE(self);

    return ret;
}

static void CC_set_error_statements(ConnectionClass *self) {
    int i;

    MYLOG(ES_TRACE, "entering self=%p\n", self);

    for (i = 0; i < self->num_stmts; i++) {
        if (NULL != self->stmts[i])
            SC_ref_CC_error(self->stmts[i]);
    }
}

char CC_remove_statement(ConnectionClass *self, StatementClass *stmt) {
    int i;
    char ret = FALSE;

    CONNLOCK_ACQUIRE(self);
    for (i = 0; i < self->num_stmts; i++) {
        if (self->stmts[i] == stmt && stmt->status != STMT_EXECUTING) {
            self->stmts[i] = NULL;
            ret = TRUE;
            break;
        }
    }
    CONNLOCK_RELEASE(self);

    return ret;
}

char CC_get_escape(const ConnectionClass *self) {
    UNUSED(self);
    return ESCAPE_IN_LITERAL;
}

int CC_get_max_idlen(ConnectionClass *self) {
    UNUSED(self);
    return self->max_identifier_length;
}

SQLUINTEGER CC_get_isolation(ConnectionClass *self) {
    UNUSED(self);
    return ELASTIC_TRANSACTION_SUPPORT;
}

void CC_set_error(ConnectionClass *self, int number, const char *message,
                  const char *func) {
    CONNLOCK_ACQUIRE(self);
    if (self->__error_message)
        free(self->__error_message);
    self->__error_number = number;
    self->__error_message = message ? strdup(message) : NULL;
    if (0 != number)
        CC_set_error_statements(self);
    if (func && number != 0)
        CC_log_error(func, "", self);
    CONNLOCK_RELEASE(self);
}

void CC_set_errormsg(ConnectionClass *self, const char *message) {
    CONNLOCK_ACQUIRE(self);
    if (self->__error_message)
        free(self->__error_message);
    self->__error_message = message ? strdup(message) : NULL;
    CONNLOCK_RELEASE(self);
}

int CC_get_error(ConnectionClass *self, int *number, char **message) {
    int rv;

    MYLOG(ES_TRACE, "entering\n");

    CONNLOCK_ACQUIRE(self);

    if (CC_get_errornumber(self)) {
        *number = CC_get_errornumber(self);
        *message = CC_get_errormsg(self);
    }
    rv = (CC_get_errornumber(self) != 0);

    CONNLOCK_RELEASE(self);

    MYLOG(ES_TRACE, "leaving\n");

    return rv;
}
void CC_log_error(const char *func, const char *desc,
                  const ConnectionClass *self) {
#define NULLCHECK(a) (a ? a : "(NULL)")

    if (self) {
        MYLOG(ES_ERROR, "CONN ERROR: func=%s, desc='%s', errnum=%d, errmsg='%s'\n",
              func, desc, self->__error_number,
              NULLCHECK(self->__error_message));
        MYLOG(ES_ERROR,
              "            "
              "------------------------------------------------------------\n");
        MYLOG(ES_ERROR,
              "            henv=%p, conn=%p, status=%u, num_stmts=%d\n",
              self->henv, self, self->status, self->num_stmts);
        MYLOG(ES_ERROR,
              "            esconn=%p, stmts=%p, lobj_type=%d\n", self->esconn,
              self->stmts, self->lobj_type);
    } else {
        MYLOG(ES_ERROR, "INVALID CONNECTION HANDLE ERROR: func=%s, desc='%s'\n", func,
              desc);
    }
}

const char *CurrCat(const ConnectionClass *conn) {
    UNUSED(conn);
    return NULL;
}

const char *CurrCatString(const ConnectionClass *conn) {
    const char *cat = CurrCat(conn);

    if (!cat)
        cat = NULL_STRING;
    return cat;
}

/*------
 *	Create a null terminated lower-case string if the
 *	original string contains upper-case characters.
 *	The SQL_NTS length is considered.
 *------
 */
SQLCHAR *make_lstring_ifneeded(ConnectionClass *conn, const SQLCHAR *s,
                               ssize_t len, BOOL ifallupper) {
    ssize_t length = len;
    char *str = NULL;
    const char *ccs = (const char *)s;

    if (s && (len > 0 || (len == SQL_NTS && (length = strlen(ccs)) > 0))) {
        int i;
        int tchar;
        encoded_str encstr;

        make_encoded_str(&encstr, conn, ccs);
        for (i = 0; i < length; i++) {
            tchar = encoded_nextchar(&encstr);
            if (MBCS_NON_ASCII(encstr))
                continue;
            if (ifallupper && islower(tchar)) {
                if (str) {
                    free(str);
                    str = NULL;
                }
                break;
            }
            if (tolower(tchar) != tchar) {
                if (!str) {
                    str = malloc(length + 1);
                    if (!str)
                        return NULL;
                    memcpy(str, s, length);
                    str[length] = '\0';
                }
                str[i] = (char)tolower(tchar);
            }
        }
    }

    return (SQLCHAR *)str;
}
