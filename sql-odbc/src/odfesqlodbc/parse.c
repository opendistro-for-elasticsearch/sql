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

#include "es_odbc.h"

#include <ctype.h>
#include <stdio.h>
#include <string.h>

#include "catfunc.h"
#include "es_apifunc.h"
#include "es_connection.h"
#include "es_types.h"
#include "qresult.h"
#include "statement.h"

#include "es_info.h"
#include "misc.h"
#include "multibyte.h"

Int4 FI_precision(const FIELD_INFO *fi) {
    OID ftype;

    if (!fi)
        return -1;
    ftype = FI_type(fi);
    switch (ftype) {
        case ES_TYPE_NUMERIC:
            return fi->column_size;
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
            return fi->decimal_digits;
    }
    return 0;
}

static void setNumFields(IRDFields *irdflds, size_t numFields) {
    FIELD_INFO **fi = irdflds->fi;
    size_t nfields = irdflds->nfields;

    if (numFields < nfields) {
        int i;

        for (i = (int)numFields; i < (int)nfields; i++) {
            if (fi[i])
                fi[i]->flag = 0;
        }
    }
    irdflds->nfields = (UInt4)numFields;
}

void SC_initialize_cols_info(StatementClass *stmt, BOOL DCdestroy,
                             BOOL parseReset) {
    IRDFields *irdflds = SC_get_IRDF(stmt);

    /* Free the parsed table information */
    if (stmt->ti) {
        TI_Destructor(stmt->ti, stmt->ntab);
        free(stmt->ti);
        stmt->ti = NULL;
    }
    stmt->ntab = 0;
    if (DCdestroy) /* Free the parsed field information */
        DC_Destructor((DescriptorClass *)SC_get_IRD(stmt));
    else
        setNumFields(irdflds, 0);
    if (parseReset) {
        stmt->parse_status = STMT_PARSE_NONE;
        SC_reset_updatable(stmt);
    }
}
