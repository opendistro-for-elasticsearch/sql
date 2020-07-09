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

#include "bind.h"

#include <ctype.h>
#include <string.h>

#include "descriptor.h"
#include "environ.h"
#include "es_apifunc.h"
#include "es_types.h"
#include "misc.h"
#include "multibyte.h"
#include "qresult.h"
#include "statement.h"

/*	Associate a user-supplied buffer with a database column. */
RETCODE SQL_API ESAPI_BindCol(HSTMT hstmt, SQLUSMALLINT icol,
                              SQLSMALLINT fCType, PTR rgbValue,
                              SQLLEN cbValueMax, SQLLEN *pcbValue) {
    StatementClass *stmt = (StatementClass *)hstmt;
    CSTR func = "ESAPI_BindCol";
    ARDFields *opts;
    GetDataInfo *gdata_info;
    BindInfoClass *bookmark;
    RETCODE ret = SQL_SUCCESS;

    MYLOG(ES_TRACE, "entering...\n");

    MYLOG(ES_DEBUG, "**** : stmt = %p, icol = %d\n", stmt, icol);
    MYLOG(ES_DEBUG, "**** : fCType=%d rgb=%p valusMax=" FORMAT_LEN " pcb=%p\n",
          fCType, rgbValue, cbValueMax, pcbValue);

    if (!stmt) {
        SC_log_error(func, "", NULL);
        return SQL_INVALID_HANDLE;
    }

    opts = SC_get_ARDF(stmt);
    if (stmt->status == STMT_EXECUTING) {
        SC_set_error(stmt, STMT_SEQUENCE_ERROR,
                     "Can't bind columns while statement is still executing.",
                     func);
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
    SC_clear_error(stmt);
    /* If the bookmark column is being bound, then just save it */
    if (icol == 0) {
        bookmark = opts->bookmark;
        if (rgbValue == NULL) {
            if (bookmark) {
                bookmark->buffer = NULL;
                bookmark->used = bookmark->indicator = NULL;
            }
        } else {
            /* Make sure it is the bookmark data type */
            switch (fCType) {
                case SQL_C_BOOKMARK:
                case SQL_C_VARBOOKMARK:
                    break;
                default:
                    SC_set_error(stmt, STMT_PROGRAM_TYPE_OUT_OF_RANGE,
                                 "Bind column 0 is not of type SQL_C_BOOKMARK",
                                 func);
                    MYLOG(
                        ES_ERROR,
                        "Bind column 0 is type %d not of type SQL_C_BOOKMARK\n",
                        fCType);
                    ret = SQL_ERROR;
                    goto cleanup;
            }

            bookmark = ARD_AllocBookmark(opts);
            bookmark->buffer = rgbValue;
            bookmark->used = bookmark->indicator = pcbValue;
            bookmark->buflen = cbValueMax;
            bookmark->returntype = fCType;
        }
        goto cleanup;
    }

    /*
     * Allocate enough bindings if not already done. Most likely,
     * execution of a statement would have setup the necessary bindings.
     * But some apps call BindCol before any statement is executed.
     */
    if (icol > opts->allocated)
        extend_column_bindings(opts, icol);
    gdata_info = SC_get_GDTI(stmt);
    if (icol > gdata_info->allocated)
        extend_getdata_info(gdata_info, icol, FALSE);

    /* check to see if the bindings were allocated */
    if (!opts->bindings || !gdata_info->gdata) {
        SC_set_error(stmt, STMT_NO_MEMORY_ERROR,
                     "Could not allocate memory for bindings.", func);
        ret = SQL_ERROR;
        goto cleanup;
    }

    /* use zero based col numbers from here out */
    icol--;

    /* Reset for SQLGetData */
    GETDATA_RESET(gdata_info->gdata[icol]);

    if (rgbValue == NULL) {
        /* we have to unbind the column */
        opts->bindings[icol].buflen = 0;
        opts->bindings[icol].buffer = NULL;
        opts->bindings[icol].used = opts->bindings[icol].indicator = NULL;
        opts->bindings[icol].returntype = SQL_C_CHAR;
        opts->bindings[icol].precision = 0;
        opts->bindings[icol].scale = 0;
        if (gdata_info->gdata[icol].ttlbuf)
            free(gdata_info->gdata[icol].ttlbuf);
        gdata_info->gdata[icol].ttlbuf = NULL;
        gdata_info->gdata[icol].ttlbuflen = 0;
        gdata_info->gdata[icol].ttlbufused = 0;
    } else {
        /* ok, bind that column */
        opts->bindings[icol].buflen = cbValueMax;
        opts->bindings[icol].buffer = rgbValue;
        opts->bindings[icol].used = opts->bindings[icol].indicator = pcbValue;
        opts->bindings[icol].returntype = fCType;
        opts->bindings[icol].precision = 0;
        switch (fCType) {
            case SQL_C_NUMERIC:
                opts->bindings[icol].precision = 32;
                break;
            case SQL_C_TIMESTAMP:
            case SQL_C_INTERVAL_DAY_TO_SECOND:
            case SQL_C_INTERVAL_HOUR_TO_SECOND:
            case SQL_C_INTERVAL_MINUTE_TO_SECOND:
            case SQL_C_INTERVAL_SECOND:
                opts->bindings[icol].precision = 6;
                break;
        }
        opts->bindings[icol].scale = 0;

        MYLOG(ES_DEBUG, "       bound buffer[%d] = %p\n", icol,
              opts->bindings[icol].buffer);
    }

cleanup:
#undef return
    return ret;
}

RETCODE SQL_API ESAPI_NumParams(HSTMT hstmt, SQLSMALLINT *pcpar) {
    StatementClass *stmt = (StatementClass *)hstmt;
    if (pcpar != NULL) {
        *pcpar = 0;
    } else {
        SC_set_error(stmt, STMT_EXEC_ERROR, "Parameter count address is null",
                     "ESAPI_NumParams");
        return SQL_ERROR;
    }
    return SQL_SUCCESS;
}

/*
 *	 Bindings Implementation
 */
static BindInfoClass *create_empty_bindings(int num_columns) {
    BindInfoClass *new_bindings;
    int i;

    new_bindings = (BindInfoClass *)malloc(num_columns * sizeof(BindInfoClass));
    if (!new_bindings)
        return NULL;

    for (i = 0; i < num_columns; i++) {
        new_bindings[i].buflen = 0;
        new_bindings[i].buffer = NULL;
        new_bindings[i].used = new_bindings[i].indicator = NULL;
    }

    return new_bindings;
}

void extend_parameter_bindings(APDFields *self, SQLSMALLINT num_params) {
    ParameterInfoClass *new_bindings;

    MYLOG(ES_TRACE,
          "entering ... self=%p, parameters_allocated=%d, num_params=%d,%p\n",
          self, self->allocated, num_params, self->parameters);

    /*
     * if we have too few, allocate room for more, and copy the old
     * entries into the new structure
     */
    if (self->allocated < num_params) {
        new_bindings = (ParameterInfoClass *)realloc(
            self->parameters, sizeof(ParameterInfoClass) * num_params);
        if (!new_bindings) {
            MYLOG(ES_DEBUG,
                  "unable to create %d new bindings from %d old bindings\n",
                  num_params, self->allocated);

            if (self->parameters)
                free(self->parameters);
            self->parameters = NULL;
            self->allocated = 0;
            return;
        }
        memset(&new_bindings[self->allocated], 0,
               sizeof(ParameterInfoClass) * (num_params - self->allocated));

        self->parameters = new_bindings;
        self->allocated = num_params;
    }

    MYLOG(ES_TRACE, "leaving %p\n", self->parameters);
}

void extend_iparameter_bindings(IPDFields *self, SQLSMALLINT num_params) {
    ParameterImplClass *new_bindings;

    MYLOG(ES_TRACE,
          "entering ... self=%p, parameters_allocated=%d, num_params=%d\n",
          self, self->allocated, num_params);

    /*
     * if we have too few, allocate room for more, and copy the old
     * entries into the new structure
     */
    if (self->allocated < num_params) {
        new_bindings = (ParameterImplClass *)realloc(
            self->parameters, sizeof(ParameterImplClass) * num_params);
        if (!new_bindings) {
            MYLOG(ES_DEBUG,
                  "unable to create %d new bindings from %d old bindings\n",
                  num_params, self->allocated);

            if (self->parameters)
                free(self->parameters);
            self->parameters = NULL;
            self->allocated = 0;
            return;
        }
        memset(&new_bindings[self->allocated], 0,
               sizeof(ParameterImplClass) * (num_params - self->allocated));

        self->parameters = new_bindings;
        self->allocated = num_params;
    }

    MYLOG(ES_TRACE, "leaving %p\n", self->parameters);
}

void reset_a_parameter_binding(APDFields *self, int ipar) {
    MYLOG(ES_TRACE, "entering ... self=%p, parameters_allocated=%d, ipar=%d\n",
          self, self->allocated, ipar);

    if (ipar < 1 || ipar > self->allocated)
        return;

    ipar--;
    self->parameters[ipar].buflen = 0;
    self->parameters[ipar].buffer = NULL;
    self->parameters[ipar].used = self->parameters[ipar].indicator = NULL;
    self->parameters[ipar].CType = 0;
    self->parameters[ipar].data_at_exec = FALSE;
    self->parameters[ipar].precision = 0;
    self->parameters[ipar].scale = 0;
}

void reset_a_iparameter_binding(IPDFields *self, int ipar) {
    MYLOG(ES_TRACE, "entering ... self=%p, parameters_allocated=%d, ipar=%d\n",
          self, self->allocated, ipar);

    if (ipar < 1 || ipar > self->allocated)
        return;

    ipar--;
    NULL_THE_NAME(self->parameters[ipar].paramName);
    self->parameters[ipar].paramType = 0;
    self->parameters[ipar].SQLType = 0;
    self->parameters[ipar].column_size = 0;
    self->parameters[ipar].decimal_digits = 0;
    self->parameters[ipar].precision = 0;
    self->parameters[ipar].scale = 0;
    PIC_set_estype(self->parameters[ipar], 0);
}

int CountParameters(const StatementClass *self, Int2 *inputCount, Int2 *ioCount,
                    Int2 *outputCount) {
    IPDFields *ipdopts = SC_get_IPDF(self);
    int i, num_params, valid_count;

    if (inputCount)
        *inputCount = 0;
    if (ioCount)
        *ioCount = 0;
    if (outputCount)
        *outputCount = 0;
    if (!ipdopts)
        return -1;
    num_params = self->num_params;
    if (ipdopts->allocated < num_params)
        num_params = ipdopts->allocated;
    for (i = 0, valid_count = 0; i < num_params; i++) {
        if (SQL_PARAM_OUTPUT == ipdopts->parameters[i].paramType) {
            if (outputCount) {
                (*outputCount)++;
                valid_count++;
            }
        } else if (SQL_PARAM_INPUT_OUTPUT == ipdopts->parameters[i].paramType) {
            if (ioCount) {
                (*ioCount)++;
                valid_count++;
            }
        } else if (inputCount) {
            (*inputCount)++;
            valid_count++;
        }
    }
    return valid_count;
}

/*
 *	Free parameters and free the memory.
 */
void APD_free_params(APDFields *apdopts, char option) {
    MYLOG(ES_TRACE, "entering self=%p\n", apdopts);

    if (!apdopts->parameters)
        return;

    if (option == STMT_FREE_PARAMS_ALL) {
        free(apdopts->parameters);
        apdopts->parameters = NULL;
        apdopts->allocated = 0;
    }

    MYLOG(ES_TRACE, "leaving\n");
}

void PDATA_free_params(PutDataInfo *pdata, char option) {
    int i;

    MYLOG(ES_TRACE, "entering self=%p\n", pdata);

    if (!pdata->pdata)
        return;

    for (i = 0; i < pdata->allocated; i++) {
        if (pdata->pdata[i].EXEC_used) {
            free(pdata->pdata[i].EXEC_used);
            pdata->pdata[i].EXEC_used = NULL;
        }
        if (pdata->pdata[i].EXEC_buffer) {
            free(pdata->pdata[i].EXEC_buffer);
            pdata->pdata[i].EXEC_buffer = NULL;
        }
    }

    if (option == STMT_FREE_PARAMS_ALL) {
        free(pdata->pdata);
        pdata->pdata = NULL;
        pdata->allocated = 0;
    }

    MYLOG(ES_TRACE, "leaving\n");
}

/*
 *	Free parameters and free the memory.
 */
void IPD_free_params(IPDFields *ipdopts, char option) {
    MYLOG(ES_TRACE, "entering self=%p\n", ipdopts);

    if (!ipdopts->parameters)
        return;
    if (option == STMT_FREE_PARAMS_ALL) {
        free(ipdopts->parameters);
        ipdopts->parameters = NULL;
        ipdopts->allocated = 0;
    }

    MYLOG(ES_TRACE, "leaving\n");
}

void extend_column_bindings(ARDFields *self, SQLSMALLINT num_columns) {
    BindInfoClass *new_bindings;
    SQLSMALLINT i;

    MYLOG(ES_TRACE,
          "entering ... self=%p, bindings_allocated=%d, num_columns=%d\n", self,
          self->allocated, num_columns);

    /*
     * if we have too few, allocate room for more, and copy the old
     * entries into the new structure
     */
    if (self->allocated < num_columns) {
        new_bindings = create_empty_bindings(num_columns);
        if (!new_bindings) {
            MYLOG(ES_DEBUG,
                  "unable to create %d new bindings from %d old bindings\n",
                  num_columns, self->allocated);

            if (self->bindings) {
                free(self->bindings);
                self->bindings = NULL;
            }
            self->allocated = 0;
            return;
        }

        if (self->bindings) {
            for (i = 0; i < self->allocated; i++)
                new_bindings[i] = self->bindings[i];

            free(self->bindings);
        }

        self->bindings = new_bindings;
        self->allocated = num_columns;
    }

    /*
     * There is no reason to zero out extra bindings if there are more
     * than needed.  If an app has allocated extra bindings, let it worry
     * about it by unbinding those columns.
     */

    /* SQLBindCol(1..) ... SQLBindCol(10...)   # got 10 bindings */
    /* SQLExecDirect(...)  # returns 5 cols */
    /* SQLExecDirect(...)  # returns 10 cols  (now OK) */

    MYLOG(ES_TRACE, "leaving %p\n", self->bindings);
}

void reset_a_column_binding(ARDFields *self, int icol) {
    BindInfoClass *bookmark;

    MYLOG(ES_TRACE, "entering ... self=%p, bindings_allocated=%d, icol=%d\n",
          self, self->allocated, icol);

    if (icol > self->allocated)
        return;

    /* use zero based col numbers from here out */
    if (0 == icol) {
        if (bookmark = self->bookmark, bookmark != NULL) {
            bookmark->buffer = NULL;
            bookmark->used = bookmark->indicator = NULL;
        }
    } else {
        icol--;

        /* we have to unbind the column */
        self->bindings[icol].buflen = 0;
        self->bindings[icol].buffer = NULL;
        self->bindings[icol].used = self->bindings[icol].indicator = NULL;
        self->bindings[icol].returntype = SQL_C_CHAR;
    }
}

void ARD_unbind_cols(ARDFields *self, BOOL freeall) {
    Int2 lf;

    MYLOG(ES_ALL, "freeall=%d allocated=%d bindings=%p\n", freeall,
          self->allocated, self->bindings);
    for (lf = 1; lf <= self->allocated; lf++)
        reset_a_column_binding(self, lf);
    if (freeall) {
        if (self->bindings)
            free(self->bindings);
        self->bindings = NULL;
        self->allocated = 0;
    }
}
void GDATA_unbind_cols(GetDataInfo *self, BOOL freeall) {
    Int2 lf;

    MYLOG(ES_ALL, "freeall=%d allocated=%d gdata=%p\n", freeall,
          self->allocated, self->gdata);
    if (self->fdata.ttlbuf) {
        free(self->fdata.ttlbuf);
        self->fdata.ttlbuf = NULL;
    }
    self->fdata.ttlbuflen = self->fdata.ttlbufused = 0;
    GETDATA_RESET(self->fdata);
    for (lf = 1; lf <= self->allocated; lf++)
        reset_a_getdata_info(self, lf);
    if (freeall) {
        if (self->gdata)
            free(self->gdata);
        self->gdata = NULL;
        self->allocated = 0;
    }
}

void GetDataInfoInitialize(GetDataInfo *gdata_info) {
    GETDATA_RESET(gdata_info->fdata);
    gdata_info->fdata.ttlbuf = NULL;
    gdata_info->fdata.ttlbuflen = gdata_info->fdata.ttlbufused = 0;
    gdata_info->allocated = 0;
    gdata_info->gdata = NULL;
}
static GetDataClass *create_empty_gdata(int num_columns) {
    GetDataClass *new_gdata;
    int i;

    new_gdata = (GetDataClass *)malloc(num_columns * sizeof(GetDataClass));
    if (!new_gdata)
        return NULL;
    for (i = 0; i < num_columns; i++) {
        GETDATA_RESET(new_gdata[i]);
        new_gdata[i].ttlbuf = NULL;
        new_gdata[i].ttlbuflen = 0;
        new_gdata[i].ttlbufused = 0;
    }

    return new_gdata;
}
void extend_getdata_info(GetDataInfo *self, SQLSMALLINT num_columns,
                         BOOL shrink) {
    GetDataClass *new_gdata;

    MYLOG(ES_TRACE,
          "entering ... self=%p, gdata_allocated=%d, num_columns=%d\n", self,
          self->allocated, num_columns);

    /*
     * if we have too few, allocate room for more, and copy the old
     * entries into the new structure
     */
    if (self->allocated < num_columns) {
        new_gdata = create_empty_gdata(num_columns);
        if (!new_gdata) {
            MYLOG(ES_DEBUG, "unable to create %d new gdata from %d old gdata\n",
                  num_columns, self->allocated);

            if (self->gdata) {
                free(self->gdata);
                self->gdata = NULL;
            }
            self->allocated = 0;
            return;
        }
        if (self->gdata) {
            SQLSMALLINT i;

            for (i = 0; i < self->allocated; i++)
                new_gdata[i] = self->gdata[i];
            free(self->gdata);
        }
        self->gdata = new_gdata;
        self->allocated = num_columns;
    } else if (shrink && self->allocated > num_columns) {
        int i;

        for (i = self->allocated; i > num_columns; i--)
            reset_a_getdata_info(self, i);
        self->allocated = num_columns;
        if (0 == num_columns) {
            free(self->gdata);
            self->gdata = NULL;
        }
    }

    /*
     * There is no reason to zero out extra gdata if there are more
     * than needed.  If an app has allocated extra gdata, let it worry
     * about it by unbinding those columns.
     */

    MYLOG(ES_TRACE, "leaving %p\n", self->gdata);
}
void reset_a_getdata_info(GetDataInfo *gdata_info, int icol) {
    if (icol < 1 || icol > gdata_info->allocated)
        return;
    icol--;
    if (gdata_info->gdata[icol].ttlbuf) {
        free(gdata_info->gdata[icol].ttlbuf);
        gdata_info->gdata[icol].ttlbuf = NULL;
    }
    gdata_info->gdata[icol].ttlbuflen = gdata_info->gdata[icol].ttlbufused = 0;
    GETDATA_RESET(gdata_info->gdata[icol]);
}

void PutDataInfoInitialize(PutDataInfo *pdata_info) {
    pdata_info->allocated = 0;
    pdata_info->pdata = NULL;
}
void extend_putdata_info(PutDataInfo *self, SQLSMALLINT num_params,
                         BOOL shrink) {
    PutDataClass *new_pdata;

    MYLOG(ES_TRACE,
          "entering ... self=%p, parameters_allocated=%d, num_params=%d\n",
          self, self->allocated, num_params);

    /*
     * if we have too few, allocate room for more, and copy the old
     * entries into the new structure
     */
    if (self->allocated < num_params) {
        if (self->allocated <= 0 && self->pdata) {
            MYLOG(ES_DEBUG, "??? pdata is not null while allocated == 0\n");
            self->pdata = NULL;
        }
        new_pdata = (PutDataClass *)realloc(self->pdata,
                                            sizeof(PutDataClass) * num_params);
        if (!new_pdata) {
            MYLOG(ES_DEBUG, "unable to create %d new pdata from %d old pdata\n",
                  num_params, self->allocated);

            self->pdata = NULL;
            self->allocated = 0;
            return;
        }
        memset(&new_pdata[self->allocated], 0,
               sizeof(PutDataClass) * (num_params - self->allocated));

        self->pdata = new_pdata;
        self->allocated = num_params;
    } else if (shrink && self->allocated > num_params) {
        int i;

        for (i = self->allocated; i > num_params; i--)
            reset_a_putdata_info(self, i);
        self->allocated = num_params;
        if (0 == num_params) {
            free(self->pdata);
            self->pdata = NULL;
        }
    }

    MYLOG(ES_TRACE, "leaving %p\n", self->pdata);
}
void reset_a_putdata_info(PutDataInfo *pdata_info, int ipar) {
    if (ipar < 1 || ipar > pdata_info->allocated)
        return;
    ipar--;
    if (pdata_info->pdata[ipar].EXEC_used) {
        free(pdata_info->pdata[ipar].EXEC_used);
        pdata_info->pdata[ipar].EXEC_used = NULL;
    }
    if (pdata_info->pdata[ipar].EXEC_buffer) {
        free(pdata_info->pdata[ipar].EXEC_buffer);
        pdata_info->pdata[ipar].EXEC_buffer = NULL;
    }
    pdata_info->pdata[ipar].lobj_oid = 0;
}

void SC_param_next(const StatementClass *stmt, int *param_number,
                   ParameterInfoClass **apara, ParameterImplClass **ipara) {
    int next;
    IPDFields *ipdopts = SC_get_IPDF(stmt);

    if (*param_number < 0)
        next = stmt->proc_return;
    else
        next = *param_number + 1;
    if (stmt->discard_output_params) {
        for (; next < ipdopts->allocated
               && SQL_PARAM_OUTPUT == ipdopts->parameters[next].paramType;
             next++)
            ;
    }
    *param_number = next;
    if (ipara) {
        if (next < ipdopts->allocated)
            *ipara = ipdopts->parameters + next;
        else
            *ipara = NULL;
    }
    if (apara) {
        APDFields *apdopts = SC_get_APDF(stmt);
        if (next < apdopts->allocated)
            *apara = apdopts->parameters + next;
        else
            *apara = NULL;
    }
}
