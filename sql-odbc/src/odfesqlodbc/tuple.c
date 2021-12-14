// clang-format off
#include "tuple.h"
#include "misc.h"

#include <string.h>
#include <stdlib.h>
// clang-format on

void set_tuplefield_null(TupleField *tuple_field) {
    tuple_field->len = 0;
    // Changing value to strdup("") from NULL to fix error 
    // "Object cannot be cast from DBNull to other types" in Excel & Power BI
    tuple_field->value = strdup(""); /* NULL; */
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
