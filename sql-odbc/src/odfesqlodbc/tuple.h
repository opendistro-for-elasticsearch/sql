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

#ifndef __TUPLE_H__
#define __TUPLE_H__

#include "es_odbc.h"

// C Interface
#ifdef __cplusplus
extern "C" {
#endif

/*	Used by backend data AND manual result sets */
struct TupleField_ {
    Int4 len;    /* ES length of the current Tuple */
    void *value; /* an array representing the value */
};

/*	keyset(TID + OID) info */
struct KeySet_ {
    UWORD status;
    UInt2 offset;
    UInt4 blocknum;
    OID oid;
};
/*	Rollback(index + original TID) info */
struct Rollback_ {
    SQLLEN index;
    UInt4 blocknum;
    UInt2 offset;
    OID oid;
    UWORD option;
};
#define KEYSET_INFO_PUBLIC 0x07
#define CURS_SELF_ADDING (1L << 3)
#define CURS_SELF_DELETING (1L << 4)
#define CURS_SELF_UPDATING (1L << 5)
#define CURS_SELF_ADDED (1L << 6)
#define CURS_SELF_DELETED (1L << 7)
#define CURS_SELF_UPDATED (1L << 8)
#define CURS_NEEDS_REREAD (1L << 9)
#define CURS_IN_ROWSET (1L << 10)
#define CURS_OTHER_DELETED (1L << 11)

/*	These macros are wrappers for the corresponding set_tuplefield functions
    but these handle automatic NULL determination and call set_tuplefield_null()
    if appropriate for the datatype (used by SQLGetTypeInfo).
*/
#define set_nullfield_string(FLD, VAL) \
    ((VAL) ? set_tuplefield_string(FLD, (VAL)) : set_tuplefield_null(FLD))
#define set_nullfield_int2(FLD, VAL) \
    ((VAL) != -1 ? set_tuplefield_int2(FLD, (VAL)) : set_tuplefield_null(FLD))
#define set_nullfield_int4(FLD, VAL) \
    ((VAL) != -1 ? set_tuplefield_int4(FLD, (VAL)) : set_tuplefield_null(FLD))

void set_tuplefield_null(TupleField *tuple_field);
void set_tuplefield_string(TupleField *tuple_field, const char *string);
void set_tuplefield_int2(TupleField *tuple_field, Int2 value);
void set_tuplefield_int4(TupleField *tuple_field, Int4 value);
SQLLEN ClearCachedRows(TupleField *tuple, int num_fields, SQLLEN num_rows);

typedef struct _ES_BM_ {
    Int4 index;
    KeySet keys;
} ES_BM;

#ifdef __cplusplus
}
#endif

#endif
