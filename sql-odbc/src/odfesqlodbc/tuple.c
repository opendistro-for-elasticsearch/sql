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

// clang-format off
#include "tuple.h"
#include "misc.h"

#include <string.h>
#include <stdlib.h>
// clang-format on

void set_tuplefield_null(TupleField *tuple_field) {
    tuple_field->len = 0;
    tuple_field->value = NULL; /* strdup(""); */
}

void set_tuplefield_string(TupleField *tuple_field, const char *string) {
    if (string) {
        tuple_field->len = (Int4)strlen(string); /* ES restriction */
        tuple_field->value = strdup(string);
    }
    if (!tuple_field->value)
        set_tuplefield_null(tuple_field);
}

void set_tuplefield_int2(TupleField *tuple_field, Int2 value) {
    char buffer[10];

    ITOA_FIXED(buffer, value);

    tuple_field->len = (Int4)(strlen(buffer) + 1);
    /* +1 ... is this correct (better be on the save side-...) */
    tuple_field->value = strdup(buffer);
}

void set_tuplefield_int4(TupleField *tuple_field, Int4 value) {
    char buffer[15];

    ITOA_FIXED(buffer, value);

    tuple_field->len = (Int4)(strlen(buffer) + 1);
    /* +1 ... is this correct (better be on the save side-...) */
    tuple_field->value = strdup(buffer);
}
