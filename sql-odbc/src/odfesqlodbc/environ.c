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

#include "environ.h"

#include <stdlib.h>
#include <string.h>

#include "dlg_specific.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "misc.h"
#include "statement.h"
#ifdef WIN32
#include <winsock2.h>
#endif /* WIN32 */
#include "loadlib.h"

/* The one instance of the handles */
static int conns_count = 0;
static ConnectionClass **conns = NULL;

void *conns_cs = NULL;
void *common_cs = NULL;
void *common_lcs = NULL;

RETCODE SQL_API ESAPI_AllocEnv(HENV *phenv) {
    CSTR func = "ESAPI_AllocEnv";
    SQLRETURN ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering\n");

    /*
     * For systems on which none of the constructor-making
     * techniques in elasticodbc.c work:
     * It's ok to call initialize_global_cs() twice.
     */
    { initialize_global_cs(); }

    *phenv = (HENV)EN_Constructor();
    if (!*phenv) {
        *phenv = SQL_NULL_HENV;
        EN_log_error(func, "Error allocating environment", NULL);
        ret = SQL_ERROR;
    }

    MYLOG(ES_TRACE, "leaving phenv=%p\n", *phenv);
    return ret;
}

RETCODE SQL_API ESAPI_FreeEnv(HENV henv) {
    CSTR func = "ESAPI_FreeEnv";
    SQLRETURN ret = SQL_SUCCESS;
    EnvironmentClass *env = (EnvironmentClass *)henv;

    MYLOG(ES_TRACE, "entering env=%p\n", env);

    if (env && EN_Destructor(env)) {
        MYLOG(ES_DEBUG, "   ok\n");
        goto cleanup;
    }

    ret = SQL_ERROR;
    EN_log_error(func, "Error freeing environment", NULL);
cleanup:
    return ret;
}

#define SIZEOF_SQLSTATE 6

static void es_sqlstate_set(const EnvironmentClass *env, UCHAR *szSqlState,
                            const char *ver3str, const char *ver2str) {
    strncpy_null((char *)szSqlState, EN_is_odbc3(env) ? ver3str : ver2str,
                 SIZEOF_SQLSTATE);
}

ES_ErrorInfo *ER_Constructor(SDWORD errnumber, const char *msg) {
    ES_ErrorInfo *error;
    ssize_t aladd, errsize;

    if (DESC_OK == errnumber)
        return NULL;
    if (msg) {
        errsize = strlen(msg);
        aladd = errsize - sizeof(error->__error_message) + 1;
        if (aladd < 0)
            aladd = 0;
    } else {
        errsize = -1;
        aladd = 0;
    }
    error = (ES_ErrorInfo *)malloc(sizeof(ES_ErrorInfo) + aladd);
    if (error) {
        memset(error, 0, sizeof(ES_ErrorInfo));
        error->status = errnumber;
        error->errorsize = (Int2)errsize;
        if (errsize > 0)
            memcpy(error->__error_message, msg, errsize);
        error->__error_message[errsize] = '\0';
        error->recsize = -1;
    }
    return error;
}

void ER_Destructor(ES_ErrorInfo *self) {
    free(self);
}

ES_ErrorInfo *ER_Dup(const ES_ErrorInfo *self) {
    ES_ErrorInfo *new;
    Int4 alsize;

    if (!self)
        return NULL;
    alsize = sizeof(ES_ErrorInfo);
    if (self->errorsize > 0)
        alsize += self->errorsize;
    new = (ES_ErrorInfo *)malloc(alsize);
    if (new)
        memcpy(new, self, alsize);

    return new;
}

#define DRVMNGRDIV 511
/*		Returns the next SQL error information. */
RETCODE SQL_API ER_ReturnError(ES_ErrorInfo *eserror, SQLSMALLINT RecNumber,
                               SQLCHAR *szSqlState, SQLINTEGER *pfNativeError,
                               SQLCHAR *szErrorMsg, SQLSMALLINT cbErrorMsgMax,
                               SQLSMALLINT *pcbErrorMsg, UWORD flag) {
    /* CC: return an error of a hstmt  */
    ES_ErrorInfo *error;
    BOOL partial_ok = ((flag & PODBC_ALLOW_PARTIAL_EXTRACT) != 0);
    const char *msg;
    SWORD msglen, stapos, wrtlen, pcblen;

    if (!eserror)
        return SQL_NO_DATA_FOUND;
    error = eserror;
    msg = error->__error_message;
    MYLOG(ES_TRACE, "entering status = %d, msg = #%s#\n", error->status, msg);
    msglen = (SQLSMALLINT)strlen(msg);
    /*
     *	Even though an application specifies a larger error message
     *	buffer, the driver manager changes it silently.
     *	Therefore we divide the error message into ...
     */
    if (error->recsize < 0) {
        if (cbErrorMsgMax > 0)
            error->recsize = cbErrorMsgMax - 1; /* apply the first request */
        else
            error->recsize = DRVMNGRDIV;
    } else if (1 == RecNumber && cbErrorMsgMax > 0)
        error->recsize = cbErrorMsgMax - 1;
    if (RecNumber < 0) {
        if (0 == error->errorpos)
            RecNumber = 1;
        else
            RecNumber = 2 + (error->errorpos - 1) / error->recsize;
    }
    stapos = (RecNumber - 1) * error->recsize;
    if (stapos > msglen)
        return SQL_NO_DATA_FOUND;
    pcblen = wrtlen = msglen - stapos;
    if (pcblen > error->recsize)
        pcblen = error->recsize;
    if (0 == cbErrorMsgMax)
        wrtlen = 0;
    else if (wrtlen >= cbErrorMsgMax) {
        if (partial_ok)
            wrtlen = cbErrorMsgMax - 1;
        else if (cbErrorMsgMax <= error->recsize)
            wrtlen = cbErrorMsgMax - 1;
        else
            wrtlen = error->recsize;
    }
    if (wrtlen > pcblen)
        wrtlen = pcblen;
    if (NULL != pcbErrorMsg)
        *pcbErrorMsg = pcblen;

    if ((NULL != szErrorMsg) && (cbErrorMsgMax > 0)) {
        memcpy(szErrorMsg, msg + stapos, wrtlen);
        szErrorMsg[wrtlen] = '\0';
    }

    if (NULL != pfNativeError)
        *pfNativeError = error->status;

    if (NULL != szSqlState)
        strncpy_null((char *)szSqlState, error->sqlstate, 6);

    MYLOG(ES_DEBUG, "	     szSqlState = '%s',len=%d, szError='%s'\n",
          szSqlState, pcblen, szErrorMsg);
    if (wrtlen < pcblen)
        return SQL_SUCCESS_WITH_INFO;
    else
        return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_ConnectError(HDBC hdbc, SQLSMALLINT RecNumber,
                                   SQLCHAR *szSqlState,
                                   SQLINTEGER *pfNativeError,
                                   SQLCHAR *szErrorMsg,
                                   SQLSMALLINT cbErrorMsgMax,
                                   SQLSMALLINT *pcbErrorMsg, UWORD flag) {
    UNUSED(flag);
    ConnectionClass *conn = (ConnectionClass *)hdbc;
    EnvironmentClass *env = (EnvironmentClass *)conn->henv;
    char *msg;
    int status;
    BOOL once_again = FALSE;
    ssize_t msglen;

    MYLOG(ES_ERROR, "entering hdbc=%p <%d>\n", hdbc, cbErrorMsgMax);
    if (RecNumber != 1 && RecNumber != -1)
        return SQL_NO_DATA_FOUND;
    if (cbErrorMsgMax < 0)
        return SQL_ERROR;
    if (CONN_EXECUTING == conn->status || !CC_get_error(conn, &status, &msg)
        || NULL == msg) {
        MYLOG(ES_ERROR, "CC_Get_error returned nothing.\n");
        if (NULL != szSqlState)
            strncpy_null((char *)szSqlState, "00000", SIZEOF_SQLSTATE);
        if (NULL != pcbErrorMsg)
            *pcbErrorMsg = 0;
        if ((NULL != szErrorMsg) && (cbErrorMsgMax > 0))
            szErrorMsg[0] = '\0';

        return SQL_NO_DATA_FOUND;
    }
    MYLOG(ES_ERROR, "CC_get_error: status = %d, msg = #%s#\n", status, msg);

    msglen = strlen(msg);
    if (NULL != pcbErrorMsg) {
        *pcbErrorMsg = (SQLSMALLINT)msglen;
        if (cbErrorMsgMax == 0)
            once_again = TRUE;
        else if (msglen >= cbErrorMsgMax)
            *pcbErrorMsg = cbErrorMsgMax - 1;
    }
    if ((NULL != szErrorMsg) && (cbErrorMsgMax > 0))
        strncpy_null((char *)szErrorMsg, msg, cbErrorMsgMax);
    if (NULL != pfNativeError)
        *pfNativeError = status;

    if (NULL != szSqlState) {
        if (conn->sqlstate[0])
            strncpy_null((char *)szSqlState, conn->sqlstate, SIZEOF_SQLSTATE);
        else
            switch (status) {
                case CONN_OPTION_VALUE_CHANGED:
                    es_sqlstate_set(env, szSqlState, "01S02", "01S02");
                    break;
                case CONN_TRUNCATED:
                    es_sqlstate_set(env, szSqlState, "01004", "01004");
                    /* data truncated */
                    break;
                case CONN_INIREAD_ERROR:
                    es_sqlstate_set(env, szSqlState, "IM002", "IM002");
                    /* data source not found */
                    break;
                case CONNECTION_SERVER_NOT_REACHED:
                case CONN_OPENDB_ERROR:
                    es_sqlstate_set(env, szSqlState, "08001", "08001");
                    /* unable to connect to data source */
                    break;
                case CONN_INVALID_AUTHENTICATION:
                case CONN_AUTH_TYPE_UNSUPPORTED:
                    es_sqlstate_set(env, szSqlState, "28000", "28000");
                    break;
                case CONN_STMT_ALLOC_ERROR:
                    es_sqlstate_set(env, szSqlState, "HY001", "S1001");
                    /* memory allocation failure */
                    break;
                case CONN_IN_USE:
                    es_sqlstate_set(env, szSqlState, "HY000", "S1000");
                    /* general error */
                    break;
                case CONN_UNSUPPORTED_OPTION:
                    es_sqlstate_set(env, szSqlState, "HYC00", "IM001");
                    /* driver does not support this function */
                    break;
                case CONN_INVALID_ARGUMENT_NO:
                    es_sqlstate_set(env, szSqlState, "HY009", "S1009");
                    /* invalid argument value */
                    break;
                case CONN_TRANSACT_IN_PROGRES:
                    es_sqlstate_set(env, szSqlState, "HY011", "S1011");
                    break;
                case CONN_NO_MEMORY_ERROR:
                    es_sqlstate_set(env, szSqlState, "HY001", "S1001");
                    break;
                case CONN_NOT_IMPLEMENTED_ERROR:
                    es_sqlstate_set(env, szSqlState, "HYC00", "S1C00");
                    break;
                case CONN_ILLEGAL_TRANSACT_STATE:
                    es_sqlstate_set(env, szSqlState, "25000", "S1010");
                    break;
                case CONN_VALUE_OUT_OF_RANGE:
                    es_sqlstate_set(env, szSqlState, "HY019", "22003");
                    break;
                case CONNECTION_COULD_NOT_SEND:
                case CONNECTION_COULD_NOT_RECEIVE:
                case CONNECTION_COMMUNICATION_ERROR:
                case CONNECTION_NO_RESPONSE:
                    es_sqlstate_set(env, szSqlState, "08S01", "08S01");
                    break;
                default:
                    es_sqlstate_set(env, szSqlState, "HY000", "S1000");
                    /* general error */
                    break;
            }
    }

    MYLOG(ES_DEBUG,
          "	     szSqlState = '%s',len=" FORMAT_SSIZE_T ", szError='%s'\n",
          szSqlState ? (char *)szSqlState : PRINT_NULL, msglen,
          szErrorMsg ? (char *)szErrorMsg : PRINT_NULL);
    if (once_again) {
        CC_set_errornumber(conn, status);
        return SQL_SUCCESS_WITH_INFO;
    } else
        return SQL_SUCCESS;
}

RETCODE SQL_API ESAPI_EnvError(HENV henv, SQLSMALLINT RecNumber,
                               SQLCHAR *szSqlState, SQLINTEGER *pfNativeError,
                               SQLCHAR *szErrorMsg, SQLSMALLINT cbErrorMsgMax,
                               SQLSMALLINT *pcbErrorMsg, UWORD flag) {
    UNUSED(flag);
    EnvironmentClass *env = (EnvironmentClass *)henv;
    char *msg = NULL;
    int status;

    MYLOG(ES_ERROR, "entering henv=%p <%d>\n", henv, cbErrorMsgMax);
    if (RecNumber != 1 && RecNumber != -1)
        return SQL_NO_DATA_FOUND;
    if (cbErrorMsgMax < 0)
        return SQL_ERROR;
    if (!EN_get_error(env, &status, &msg) || NULL == msg) {
        MYLOG(ES_ERROR, "EN_get_error: msg = #%s#\n", msg);

        if (NULL != szSqlState)
            es_sqlstate_set(env, szSqlState, "00000", "00000");
        if (NULL != pcbErrorMsg)
            *pcbErrorMsg = 0;
        if ((NULL != szErrorMsg) && (cbErrorMsgMax > 0))
            szErrorMsg[0] = '\0';

        return SQL_NO_DATA_FOUND;
    }
    MYLOG(ES_ERROR, "EN_get_error: status = %d, msg = #%s#\n", status, msg);

    if (NULL != pcbErrorMsg)
        *pcbErrorMsg = (SQLSMALLINT)strlen(msg);
    if ((NULL != szErrorMsg) && (cbErrorMsgMax > 0))
        strncpy_null((char *)szErrorMsg, msg, cbErrorMsgMax);
    if (NULL != pfNativeError)
        *pfNativeError = status;

    if (szSqlState) {
        switch (status) {
            case ENV_ALLOC_ERROR:
                /* memory allocation failure */
                es_sqlstate_set(env, szSqlState, "HY001", "S1001");
                break;
            default:
                es_sqlstate_set(env, szSqlState, "HY000", "S1000");
                /* general error */
                break;
        }
    }

    return SQL_SUCCESS;
}

/*
 * EnvironmentClass implementation
 */
EnvironmentClass *EN_Constructor(void) {
    EnvironmentClass *rv = NULL;
#ifdef WIN32
    WORD wVersionRequested;
    WSADATA wsaData;
    const int major = 2, minor = 2;

    /* Load the WinSock Library */
    wVersionRequested = MAKEWORD(major, minor);

    if (WSAStartup(wVersionRequested, &wsaData)) {
        MYLOG(ES_ERROR, " WSAStartup error\n");
        return rv;
    }
    /* Verify that this is the minimum version of WinSock */
    if (LOBYTE(wsaData.wVersion) >= 1
        && (LOBYTE(wsaData.wVersion) >= 2 || HIBYTE(wsaData.wVersion) >= 1))
        ;
    else {
        MYLOG(ES_DEBUG, " WSAStartup version=(%d,%d)\n",
              LOBYTE(wsaData.wVersion), HIBYTE(wsaData.wVersion));
        goto cleanup;
    }
#endif /* WIN32 */

    rv = (EnvironmentClass *)malloc(sizeof(EnvironmentClass));
    if (NULL == rv) {
        MYLOG(ES_ERROR, " malloc error\n");
        goto cleanup;
    }
    rv->errormsg = 0;
    rv->errornumber = 0;
    rv->flag = 0;
    INIT_ENV_CS(rv);
cleanup:
#ifdef WIN32
    if (NULL == rv) {
        WSACleanup();
    }
#endif /* WIN32 */

    return rv;
}

char EN_Destructor(EnvironmentClass *self) {
    int lf, nullcnt;
    char rv = 1;

    MYLOG(ES_TRACE, "entering self=%p\n", self);
    if (!self)
        return 0;

    /*
     * the error messages are static strings distributed throughout the
     * source--they should not be freed
     */

    /* Free any connections belonging to this environment */
    ENTER_CONNS_CS;
    for (lf = 0, nullcnt = 0; lf < conns_count; lf++) {
        if (NULL == conns[lf])
            nullcnt++;
        else if (conns[lf]->henv == self) {
            if (CC_Destructor(conns[lf]))
                conns[lf] = NULL;
            else
                rv = 0;
            nullcnt++;
        }
    }
    if (conns && nullcnt >= conns_count) {
        MYLOG(ES_DEBUG, "clearing conns count=%d\n", conns_count);
        free(conns);
        conns = NULL;
        conns_count = 0;
    }
    LEAVE_CONNS_CS;
    DELETE_ENV_CS(self);
    free(self);

#ifdef WIN32
    WSACleanup();
#endif
    MYLOG(ES_TRACE, "leaving rv=%d\n", rv);
#ifdef _MEMORY_DEBUG_
    debug_memory_check();
#endif /* _MEMORY_DEBUG_ */
    return rv;
}

char EN_get_error(EnvironmentClass *self, int *number, char **message) {
    if (self && self->errormsg && self->errornumber) {
        *message = self->errormsg;
        *number = self->errornumber;
        self->errormsg = 0;
        self->errornumber = 0;
        return 1;
    } else
        return 0;
}

#define INIT_CONN_COUNT 128

char EN_add_connection(EnvironmentClass *self, ConnectionClass *conn) {
    int i, alloc;
    ConnectionClass **newa;
    char ret = FALSE;

    MYLOG(ES_TRACE, "entering self = %p, conn = %p\n", self, conn);

    ENTER_CONNS_CS;
    for (i = 0; i < conns_count; i++) {
        if (!conns[i]) {
            conn->henv = self;
            conns[i] = conn;
            ret = TRUE;
            MYLOG(
                0,
                "       added at i=%d, conn->henv = %p, conns[i]->henv = %p\n",
                i, conn->henv, conns[i]->henv);
            goto cleanup;
        }
    }
    if (conns_count > 0)
        alloc = 2 * conns_count;
    else
        alloc = INIT_CONN_COUNT;
    if (newa = (ConnectionClass **)realloc(conns,
                                           alloc * sizeof(ConnectionClass *)),
        NULL == newa)
        goto cleanup;
    conn->henv = self;
    newa[conns_count] = conn;
    conns = newa;
    ret = TRUE;
    MYLOG(ES_DEBUG,
          "       added at %d, conn->henv = %p, conns[%d]->henv = %p\n",
          conns_count, conn->henv, conns_count, conns[conns_count]->henv);
    for (i = conns_count + 1; i < alloc; i++)
        conns[i] = NULL;
    conns_count = alloc;
cleanup:
    LEAVE_CONNS_CS;
    return ret;
}

char EN_remove_connection(EnvironmentClass *self, ConnectionClass *conn) {
    UNUSED(self);
    int i;

    for (i = 0; i < conns_count; i++)
        if (conns[i] == conn && conns[i]->status != CONN_EXECUTING) {
            ENTER_CONNS_CS;
            conns[i] = NULL;
            LEAVE_CONNS_CS;
            return TRUE;
        }

    return FALSE;
}

void EN_log_error(const char *func, char *desc, EnvironmentClass *self) {
    if (self)
        MYLOG(ES_ERROR,
              "ENVIRON ERROR: func=%s, desc='%s', errnum=%d, errmsg='%s'\n",
              func, desc, self->errornumber, self->errormsg);
    else
        MYLOG(ES_ERROR, "INVALID ENVIRON HANDLE ERROR: func=%s, desc='%s'\n",
              func, desc);
}
