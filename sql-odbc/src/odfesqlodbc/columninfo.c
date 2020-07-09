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

#include "columninfo.h"
#include "es_types.h"

#include <stdlib.h>
#include <string.h>
#include "es_apifunc.h"
#include "es_connection.h"

ColumnInfoClass *CI_Constructor(void) {
    ColumnInfoClass *rv;

    rv = (ColumnInfoClass *)malloc(sizeof(ColumnInfoClass));

    if (rv) {
        rv->refcount = 0;
        rv->num_fields = 0;
        rv->coli_array = NULL;
    }

    return rv;
}

void CI_Destructor(ColumnInfoClass *self) {
    CI_free_memory(self);

    free(self);
}

void CI_free_memory(ColumnInfoClass *self) {
    register Int2 lf;
    int num_fields = self->num_fields;

    /* Safe to call even if null */
    self->num_fields = 0;
    if (self->coli_array) {
        for (lf = 0; lf < num_fields; lf++) {
            if (self->coli_array[lf].name) {
                free(self->coli_array[lf].name);
                self->coli_array[lf].name = NULL;
            }
        }
        free(self->coli_array);
        self->coli_array = NULL;
    }
}

void CI_set_num_fields(ColumnInfoClass *self, SQLSMALLINT new_num_fields) {
    CI_free_memory(self); /* always safe to call */

    self->num_fields = new_num_fields;

    self->coli_array =
        (struct srvr_info *)calloc(sizeof(struct srvr_info), self->num_fields);
}

void CI_set_field_info(ColumnInfoClass *self, int field_num,
                       const char *new_name, OID new_adtid, Int2 new_adtsize,
                       Int4 new_atttypmod, OID new_relid, OID new_attid) {
    /* check bounds */
    if ((field_num < 0) || (field_num >= self->num_fields))
        return;

    /* store the info */
    self->coli_array[field_num].name = strdup(new_name);
    self->coli_array[field_num].adtid = new_adtid;
    self->coli_array[field_num].adtsize = new_adtsize;
    self->coli_array[field_num].atttypmod = new_atttypmod;

    self->coli_array[field_num].display_size = ES_ADT_UNSET;
    self->coli_array[field_num].relid = new_relid;
    self->coli_array[field_num].attid = (short)new_attid;
}
