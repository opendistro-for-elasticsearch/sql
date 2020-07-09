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

#ifndef __ENVIRON_H__
#define __ENVIRON_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "es_helper.h"
#include "es_odbc.h"

#define ENV_ALLOC_ERROR 1

/**********		Environment Handle	*************/
struct EnvironmentClass_ {
    char *errormsg;
    int errornumber;
    Int4 flag;
    void *cs;
};

/*	Environment prototypes */
EnvironmentClass *EN_Constructor(void);
char EN_Destructor(EnvironmentClass *self);
char EN_get_error(EnvironmentClass *self, int *number, char **message);
char EN_add_connection(EnvironmentClass *self, ConnectionClass *conn);
char EN_remove_connection(EnvironmentClass *self, ConnectionClass *conn);
void EN_log_error(const char *func, char *desc, EnvironmentClass *self);

#define EN_OV_ODBC2 1L
#define EN_CONN_POOLING (1L << 1)
#define EN_is_odbc2(env) ((env->flag & EN_OV_ODBC2) != 0)
#define EN_is_odbc3(env) (env && (env->flag & EN_OV_ODBC2) == 0)
#define EN_set_odbc2(env) (env->flag |= EN_OV_ODBC2)
#define EN_set_odbc3(env) (env->flag &= ~EN_OV_ODBC2)
#define EN_is_pooling(env) (env && (env->flag & EN_CONN_POOLING) != 0)
#define EN_set_pooling(env) (env->flag |= EN_CONN_POOLING)
#define EN_unset_pooling(env) (env->flag &= ~EN_CONN_POOLING)

/* For Multi-thread */
#define INIT_CONNS_CS XPlatformInitializeCriticalSection(&conns_cs)
#define ENTER_CONNS_CS XPlatformEnterCriticalSection(conns_cs)
#define LEAVE_CONNS_CS XPlatformLeaveCriticalSection(conns_cs)
#define DELETE_CONNS_CS XPlatformDeleteCriticalSection(&conns_cs)
#define INIT_ENV_CS(x) XPlatformInitializeCriticalSection(&((x)->cs))
#define ENTER_ENV_CS(x) XPlatformEnterCriticalSection(((x)->cs))
#define LEAVE_ENV_CS(x) XPlatformLeaveCriticalSection(((x)->cs))
#define DELETE_ENV_CS(x) XPlatformDeleteCriticalSection(&((x)->cs))
#define INIT_COMMON_CS XPlatformInitializeCriticalSection(&common_cs)
#define ENTER_COMMON_CS XPlatformEnterCriticalSection(common_cs)
#define LEAVE_COMMON_CS XPlatformLeaveCriticalSection(common_cs)
#define DELETE_COMMON_CS XPlatformDeleteCriticalSection(&common_cs)

#ifdef __cplusplus
}
#endif
#endif /* __ENVIRON_H_ */
