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

#include "descriptor.h"

#include <ctype.h>
#include <stdio.h>
#include <string.h>

#include "environ.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "misc.h"
#include "qresult.h"
#include "statement.h"

void TI_Destructor(TABLE_INFO **ti, int count) {
    int i;

    MYLOG(ES_TRACE, "entering count=%d\n", count);
    if (ti) {
        for (i = 0; i < count; i++) {
            if (ti[i]) {
                COL_INFO *coli = ti[i]->col_info;
                if (coli) {
                    MYLOG(ES_ALL, "!!!refcnt %p:%d -> %d\n", coli, coli->refcnt,
                          coli->refcnt - 1);
                    coli->refcnt--;
                    if (coli->refcnt <= 0
                        && 0 == coli->acc_time) /* acc_time == 0 means the table
                                                   is dropped */
                        free_col_info_contents(coli);
                }
                NULL_THE_NAME(ti[i]->schema_name);
                NULL_THE_NAME(ti[i]->table_name);
                NULL_THE_NAME(ti[i]->table_alias);
                NULL_THE_NAME(ti[i]->bestitem);
                NULL_THE_NAME(ti[i]->bestqual);
                TI_Destroy_IH(ti[i]);
                free(ti[i]);
                ti[i] = NULL;
            }
        }
    }
}

void FI_Destructor(FIELD_INFO **fi, int count, BOOL freeFI) {
    int i;

    MYLOG(ES_TRACE, "entering count=%d\n", count);
    if (fi) {
        for (i = 0; i < count; i++) {
            if (fi[i]) {
                NULL_THE_NAME(fi[i]->column_name);
                NULL_THE_NAME(fi[i]->column_alias);
                NULL_THE_NAME(fi[i]->schema_name);
                NULL_THE_NAME(fi[i]->before_dot);
                if (freeFI) {
                    free(fi[i]);
                    fi[i] = NULL;
                }
            }
        }
        if (freeFI)
            free(fi);
    }
}

#define INIT_IH 32

void TI_Destroy_IH(TABLE_INFO *ti) {
    InheritanceClass *ih;
    unsigned int i;

    if (NULL == (ih = ti->ih))
        return;
    for (i = 0; i < ih->count; i++) {
        NULL_THE_NAME(ih->inf[i].fullTable);
    }
    free(ih);
    ti->ih = NULL;
}

void DC_Constructor(DescriptorClass *self, BOOL embedded,
                    StatementClass *stmt) {
    UNUSED(stmt);
    memset(self, 0, sizeof(DescriptorClass));
    self->deschd.embedded = (char)embedded;
}

static void ARDFields_free(ARDFields *self) {
    MYLOG(ES_TRACE, "entering %p bookmark=%p\n", self, self->bookmark);
    if (self->bookmark) {
        free(self->bookmark);
        self->bookmark = NULL;
    }
    /*
     * the memory pointed to by the bindings is not deallocated by the
     * driver but by the application that uses that driver, so we don't
     * have to care
     */
    ARD_unbind_cols(self, TRUE);
}

static void APDFields_free(APDFields *self) {
    if (self->bookmark) {
        free(self->bookmark);
        self->bookmark = NULL;
    }
    /* param bindings */
    APD_free_params(self, STMT_FREE_PARAMS_ALL);
}

static void IRDFields_free(IRDFields *self) {
    /* Free the parsed field information */
    if (self->fi) {
        FI_Destructor(self->fi, self->allocated, TRUE);
        self->fi = NULL;
    }
    self->allocated = 0;
    self->nfields = 0;
}

static void IPDFields_free(IPDFields *self) {
    /* param bindings */
    IPD_free_params(self, STMT_FREE_PARAMS_ALL);
}

void DC_Destructor(DescriptorClass *self) {
    DescriptorHeader *deschd = &(self->deschd);
    if (deschd->__error_message) {
        free(deschd->__error_message);
        deschd->__error_message = NULL;
    }
    if (deschd->eserror) {
        ER_Destructor(deschd->eserror);
        deschd->eserror = NULL;
    }
    if (deschd->type_defined) {
        switch (deschd->desc_type) {
            case SQL_ATTR_APP_ROW_DESC:
                ARDFields_free(&(self->ardf));
                break;
            case SQL_ATTR_APP_PARAM_DESC:
                APDFields_free(&(self->apdf));
                break;
            case SQL_ATTR_IMP_ROW_DESC:
                IRDFields_free(&(self->irdf));
                break;
            case SQL_ATTR_IMP_PARAM_DESC:
                IPDFields_free(&(self->ipdf));
                break;
        }
    }
}

void InitializeEmbeddedDescriptor(DescriptorClass *self, StatementClass *stmt,
                                  UInt4 desc_type) {
    DescriptorHeader *deschd = &(self->deschd);
    DC_Constructor(self, TRUE, stmt);
    DC_get_conn(self) = SC_get_conn(stmt);
    deschd->type_defined = TRUE;
    deschd->desc_type = desc_type;
    switch (desc_type) {
        case SQL_ATTR_APP_ROW_DESC:
            memset(&(self->ardf), 0, sizeof(ARDFields));
            stmt->ard = self;
            break;
        case SQL_ATTR_APP_PARAM_DESC:
            memset(&(self->apdf), 0, sizeof(APDFields));
            stmt->apd = self;
            break;
        case SQL_ATTR_IMP_ROW_DESC:
            memset(&(self->irdf), 0, sizeof(IRDFields));
            stmt->ird = self;
            stmt->ird->irdf.stmt = stmt;
            break;
        case SQL_ATTR_IMP_PARAM_DESC:
            memset(&(self->ipdf), 0, sizeof(IPDFields));
            stmt->ipd = self;
            break;
    }
}

/*
 * ARDFields initialize
 */
void InitializeARDFields(ARDFields *opt) {
    memset(opt, 0, sizeof(ARDFields));
    opt->size_of_rowset = 1;
    opt->bind_size = 0; /* default is to bind by column */
    opt->size_of_rowset_odbc2 = 1;
}
/*
 * APDFields initialize
 */
void InitializeAPDFields(APDFields *opt) {
    memset(opt, 0, sizeof(APDFields));
    opt->paramset_size = 1;
    opt->param_bind_type = 0;     /* default is to bind by column */
    opt->paramset_size_dummy = 1; /* dummy setting */
}

BindInfoClass *ARD_AllocBookmark(ARDFields *ardopts) {
    if (!ardopts->bookmark) {
        ardopts->bookmark = (BindInfoClass *)malloc(sizeof(BindInfoClass));
        memset(ardopts->bookmark, 0, sizeof(BindInfoClass));
    }
    return ardopts->bookmark;
}

#define DESC_INCREMENT 10
char CC_add_descriptor(ConnectionClass *self, DescriptorClass *desc) {
    int i;
    int new_num_descs;
    DescriptorClass **descs;

    MYLOG(ES_TRACE, "entering self=%p, desc=%p\n", self, desc);

    for (i = 0; i < self->num_descs; i++) {
        if (!self->descs[i]) {
            DC_get_conn(desc) = self;
            self->descs[i] = desc;
            return TRUE;
        }
    }
    /* no more room -- allocate more memory */
    new_num_descs = DESC_INCREMENT + self->num_descs;
    descs = (DescriptorClass **)realloc(
        self->descs, sizeof(DescriptorClass *) * new_num_descs);
    if (!descs)
        return FALSE;
    self->descs = descs;

    memset(&self->descs[self->num_descs], 0,
           sizeof(DescriptorClass *) * DESC_INCREMENT);
    DC_get_conn(desc) = self;
    self->descs[self->num_descs] = desc;
    self->num_descs = new_num_descs;

    return TRUE;
}

/*
 *	This API allocates a Application descriptor.
 */
RETCODE SQL_API ESAPI_AllocDesc(HDBC ConnectionHandle,
                                SQLHDESC *DescriptorHandle) {
    CSTR func = "ESAPI_AllocDesc";
    ConnectionClass *conn = (ConnectionClass *)ConnectionHandle;
    RETCODE ret = SQL_SUCCESS;
    DescriptorClass *desc;

    MYLOG(ES_TRACE, "entering...\n");

    desc = (DescriptorClass *)malloc(sizeof(DescriptorClass));
    if (desc) {
        memset(desc, 0, sizeof(DescriptorClass));
        DC_get_conn(desc) = conn;
        if (CC_add_descriptor(conn, desc))
            *DescriptorHandle = desc;
        else {
            free(desc);
            CC_set_error(conn, CONN_STMT_ALLOC_ERROR,
                         "Maximum number of descriptors exceeded", func);
            ret = SQL_ERROR;
        }
    } else {
        CC_set_error(conn, CONN_STMT_ALLOC_ERROR,
                     "No more memory ti allocate a further descriptor", func);
        ret = SQL_ERROR;
    }
    return ret;
}

RETCODE SQL_API ESAPI_FreeDesc(SQLHDESC DescriptorHandle) {
    DescriptorClass *desc = (DescriptorClass *)DescriptorHandle;
    RETCODE ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");
    DC_Destructor(desc);
    if (!desc->deschd.embedded) {
        int i;
        ConnectionClass *conn = DC_get_conn(desc);

        for (i = 0; i < conn->num_descs; i++) {
            if (conn->descs[i] == desc) {
                conn->descs[i] = NULL;
                break;
            }
        }
        free(desc);
    }
    return ret;
}

static void BindInfoClass_copy(const BindInfoClass *src,
                               BindInfoClass *target) {
    memcpy(target, src, sizeof(BindInfoClass));
}
static void ARDFields_copy(const ARDFields *src, ARDFields *target) {
    memcpy(target, src, sizeof(ARDFields));
    target->bookmark = NULL;
    if (src->bookmark) {
        BindInfoClass *bookmark = ARD_AllocBookmark(target);
        if (bookmark)
            BindInfoClass_copy(src->bookmark, bookmark);
    }
    if (src->allocated <= 0) {
        target->allocated = 0;
        target->bindings = NULL;
    } else {
        int i;

        target->bindings = malloc(target->allocated * sizeof(BindInfoClass));
        if (!target->bindings)
            target->allocated = 0;
        for (i = 0; i < target->allocated; i++)
            BindInfoClass_copy(&src->bindings[i], &target->bindings[i]);
    }
}

static void ParameterInfoClass_copy(const ParameterInfoClass *src,
                                    ParameterInfoClass *target) {
    memcpy(target, src, sizeof(ParameterInfoClass));
}
static void APDFields_copy(const APDFields *src, APDFields *target) {
    memcpy(target, src, sizeof(APDFields));
    if (src->bookmark) {
        target->bookmark = malloc(sizeof(ParameterInfoClass));
        if (target->bookmark)
            ParameterInfoClass_copy(src->bookmark, target->bookmark);
    }
    if (src->allocated <= 0) {
        target->allocated = 0;
        target->parameters = NULL;
    } else {
        int i;

        target->parameters =
            malloc(target->allocated * sizeof(ParameterInfoClass));
        if (!target->parameters)
            target->allocated = 0;
        for (i = 0; i < target->allocated; i++)
            ParameterInfoClass_copy(&src->parameters[i],
                                    &target->parameters[i]);
    }
}

static void ParameterImplClass_copy(const ParameterImplClass *src,
                                    ParameterImplClass *target) {
    memcpy(target, src, sizeof(ParameterImplClass));
}
static void IPDFields_copy(const IPDFields *src, IPDFields *target) {
    memcpy(target, src, sizeof(IPDFields));
    if (src->allocated <= 0) {
        target->allocated = 0;
        target->parameters = NULL;
    } else {
        int i;

        target->parameters = (ParameterImplClass *)malloc(
            target->allocated * sizeof(ParameterImplClass));
        if (!target->parameters)
            target->allocated = 0;
        for (i = 0; i < target->allocated; i++)
            ParameterImplClass_copy(&src->parameters[i],
                                    &target->parameters[i]);
    }
}

RETCODE SQL_API ESAPI_CopyDesc(SQLHDESC SourceDescHandle,
                               SQLHDESC TargetDescHandle) {
    RETCODE ret = SQL_ERROR;
    DescriptorClass *src, *target;
    DescriptorHeader *srchd, *targethd;
    ARDFields *ard_src, *ard_tgt;
    APDFields *apd_src, *apd_tgt;
    IPDFields *ipd_src, *ipd_tgt;

    MYLOG(ES_TRACE, "entering...\n");
    src = (DescriptorClass *)SourceDescHandle;
    target = (DescriptorClass *)TargetDescHandle;
    srchd = &(src->deschd);
    targethd = &(target->deschd);
    if (!srchd->type_defined) {
        MYLOG(ES_ERROR, "source type undefined\n");
        DC_set_error(target, DESC_EXEC_ERROR, "source handle type undefined");
        return ret;
    }
    if (targethd->type_defined) {
        MYLOG(ES_DEBUG, "source type=%d -> target type=%d\n", srchd->desc_type,
              targethd->desc_type);
        if (SQL_ATTR_IMP_ROW_DESC == targethd->desc_type) {
            MYLOG(ES_DEBUG, "can't modify IRD\n");
            DC_set_error(target, DESC_EXEC_ERROR, "can't copy to IRD");
            return ret;
        } else if (targethd->desc_type != srchd->desc_type) {
            if (targethd->embedded) {
                MYLOG(ES_DEBUG, "src type != target type\n");
                DC_set_error(
                    target, DESC_EXEC_ERROR,
                    "copying different type descriptor to embedded one");
                return ret;
            }
        }
        DC_Destructor(target);
    }
    ret = SQL_SUCCESS;
    switch (srchd->desc_type) {
        case SQL_ATTR_APP_ROW_DESC:
            MYLOG(ES_DEBUG, "src=%p target=%p type=%d", src, target,
                  srchd->desc_type);
            if (!targethd->type_defined) {
                targethd->desc_type = srchd->desc_type;
            }
            ard_src = &(src->ardf);
            MYPRINTF(ES_DEBUG,
                     " rowset_size=" FORMAT_LEN " bind_size=" FORMAT_UINTEGER
                     " ope_ptr=%p off_ptr=%p\n",
                     ard_src->size_of_rowset, ard_src->bind_size,
                     ard_src->row_operation_ptr, ard_src->row_offset_ptr);
            ard_tgt = &(target->ardf);
            MYPRINTF(ES_DEBUG, " target=%p", ard_tgt);
            ARDFields_copy(ard_src, ard_tgt);
            MYPRINTF(ES_DEBUG, " offset_ptr=%p\n", ard_tgt->row_offset_ptr);
            break;
        case SQL_ATTR_APP_PARAM_DESC:
            if (!targethd->type_defined) {
                targethd->desc_type = srchd->desc_type;
            }
            apd_src = &(src->apdf);
            apd_tgt = &(target->apdf);
            APDFields_copy(apd_src, apd_tgt);
            break;
        case SQL_ATTR_IMP_PARAM_DESC:
            if (!targethd->type_defined) {
                targethd->desc_type = srchd->desc_type;
            }
            ipd_src = &(src->ipdf);
            ipd_tgt = &(target->ipdf);
            IPDFields_copy(ipd_src, ipd_tgt);
            break;
        default:
            MYLOG(ES_DEBUG, "invalid descriptor handle type=%d\n",
                  srchd->desc_type);
            DC_set_error(target, DESC_EXEC_ERROR, "invalid descriptor type");
            ret = SQL_ERROR;
    }

    if (SQL_SUCCESS == ret)
        targethd->type_defined = TRUE;
    return ret;
}

void DC_set_error(DescriptorClass *self, int errornumber,
                  const char *errormsg) {
    DescriptorHeader *deschd = &(self->deschd);
    if (deschd->__error_message)
        free(deschd->__error_message);
    deschd->__error_number = errornumber;
    deschd->__error_message = errormsg ? strdup(errormsg) : NULL;
}
void DC_set_errormsg(DescriptorClass *self, const char *errormsg) {
    DescriptorHeader *deschd = &(self->deschd);
    if (deschd->__error_message)
        free(deschd->__error_message);
    deschd->__error_message = errormsg ? strdup(errormsg) : NULL;
}
const char *DC_get_errormsg(const DescriptorClass *desc) {
    return desc->deschd.__error_message;
}
int DC_get_errornumber(const DescriptorClass *desc) {
    return desc->deschd.__error_number;
}

/*	Map sql commands to statement types */
static const struct {
    int number;
    const char ver3str[6];
    const char ver2str[6];
} Descriptor_sqlstate[] =

    {
        {DESC_ERROR_IN_ROW, "01S01", "01S01"},
        {DESC_OPTION_VALUE_CHANGED, "01S02", "01S02"},
        {DESC_OK, "00000", "00000"},         /* OK */
        {DESC_EXEC_ERROR, "HY000", "S1000"}, /* also a general error */
        {DESC_STATUS_ERROR, "HY010", "S1010"},
        {DESC_SEQUENCE_ERROR, "HY010", "S1010"}, /* Function sequence error */
        {DESC_NO_MEMORY_ERROR, "HY001",
         "S1001"},                             /* memory allocation failure */
        {DESC_COLNUM_ERROR, "07009", "S1002"}, /* invalid column number */
        {DESC_NO_STMTSTRING, "HY001",
         "S1001"}, /* having no stmtstring is also a malloc problem */
        {DESC_ERROR_TAKEN_FROM_BACKEND, "HY000", "S1000"}, /* general error */
        {DESC_INTERNAL_ERROR, "HY000", "S1000"},           /* general error */
        {DESC_STILL_EXECUTING, "HY010", "S1010"},
        {DESC_NOT_IMPLEMENTED_ERROR, "HYC00", "S1C00"}, /* == 'driver not
                                                         * capable' */
        {DESC_BAD_PARAMETER_NUMBER_ERROR, "07009", "S1093"},
        {DESC_OPTION_OUT_OF_RANGE_ERROR, "HY092", "S1092"},
        {DESC_INVALID_COLUMN_NUMBER_ERROR, "07009", "S1002"},
        {DESC_RESTRICTED_DATA_TYPE_ERROR, "07006", "07006"},
        {DESC_INVALID_CURSOR_STATE_ERROR, "07005", "24000"},
        {DESC_CREATE_TABLE_ERROR, "42S01", "S0001"}, /* table already exists */
        {DESC_NO_CURSOR_NAME, "S1015", "S1015"},
        {DESC_INVALID_CURSOR_NAME, "34000", "34000"},
        {DESC_INVALID_ARGUMENT_NO, "HY024",
         "S1009"}, /* invalid argument value */
        {DESC_ROW_OUT_OF_RANGE, "HY107", "S1107"},
        {DESC_OPERATION_CANCELLED, "HY008", "S1008"},
        {DESC_INVALID_CURSOR_POSITION, "HY109", "S1109"},
        {DESC_VALUE_OUT_OF_RANGE, "HY019", "22003"},
        {DESC_OPERATION_INVALID, "HY011", "S1011"},
        {DESC_PROGRAM_TYPE_OUT_OF_RANGE, "?????", "?????"},
        {DESC_BAD_ERROR, "08S01", "08S01"}, /* communication link failure */
        {DESC_INVALID_OPTION_IDENTIFIER, "HY092", "HY092"},
        {DESC_RETURN_NULL_WITHOUT_INDICATOR, "22002", "22002"},
        {DESC_INVALID_DESCRIPTOR_IDENTIFIER, "HY091", "HY091"},
        {DESC_OPTION_NOT_FOR_THE_DRIVER, "HYC00", "HYC00"},
        {DESC_FETCH_OUT_OF_RANGE, "HY106", "S1106"},
        {DESC_COUNT_FIELD_INCORRECT, "07002", "07002"},
};

static ES_ErrorInfo *DC_create_errorinfo(const DescriptorClass *self) {
    const DescriptorHeader *deschd = &(self->deschd);
    ES_ErrorInfo *error;
    ConnectionClass *conn;
    EnvironmentClass *env;
    Int4 errornum;
    BOOL env_is_odbc3 = TRUE;

    if (deschd->eserror)
        return deschd->eserror;
    errornum = deschd->__error_number;
    error = ER_Constructor(errornum, deschd->__error_message);
    if (!error)
        return error;
    conn = DC_get_conn(self);
    if (conn && (env = (EnvironmentClass *)conn->henv, env))
        env_is_odbc3 = EN_is_odbc3(env);
    errornum -= LOWEST_DESC_ERROR;
    if (errornum < 0
        || errornum >= (int)(sizeof(Descriptor_sqlstate)
                             / sizeof(Descriptor_sqlstate[0])))
        errornum = 1 - LOWEST_DESC_ERROR;
    STRCPY_FIXED(error->sqlstate, env_is_odbc3
                                      ? Descriptor_sqlstate[errornum].ver3str
                                      : Descriptor_sqlstate[errornum].ver2str);
    return error;
}
void DC_log_error(const char *func, const char *desc,
                  const DescriptorClass *self) {
#define nullcheck(a) (a ? a : "(NULL)")
    if (self) {
        MYLOG(ES_DEBUG,
              "DESCRIPTOR ERROR: func=%s, desc='%s', errnum=%d, errmsg='%s'\n",
              func, desc, self->deschd.__error_number,
              nullcheck(self->deschd.__error_message));
    }
}

/*		Returns the next SQL error information. */
RETCODE SQL_API ESAPI_DescError(SQLHDESC hdesc, SQLSMALLINT RecNumber,
                                SQLCHAR *szSqlState, SQLINTEGER *pfNativeError,
                                SQLCHAR *szErrorMsg, SQLSMALLINT cbErrorMsgMax,
                                SQLSMALLINT *pcbErrorMsg, UWORD flag) {
    /* CC: return an error of a hdesc  */
    DescriptorClass *desc = (DescriptorClass *)hdesc;
    DescriptorHeader *deschd = &(desc->deschd);

    MYLOG(ES_TRACE, "entering RecN=%hd\n", RecNumber);
    deschd->eserror = DC_create_errorinfo(desc);
    return ER_ReturnError(deschd->eserror, RecNumber, szSqlState, pfNativeError,
                          szErrorMsg, cbErrorMsgMax, pcbErrorMsg, flag);
}
