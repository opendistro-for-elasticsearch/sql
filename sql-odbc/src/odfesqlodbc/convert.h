#ifndef __CONVERT_H__
#define __CONVERT_H__

#include "es_odbc.h"

#ifdef __cplusplus
extern "C" {
#endif
/* copy_and_convert results */
#define COPY_OK 0
#define COPY_UNSUPPORTED_TYPE 1
#define COPY_UNSUPPORTED_CONVERSION 2
#define COPY_RESULT_TRUNCATED 3
#define COPY_GENERAL_ERROR 4
#define COPY_NO_DATA_FOUND 5
#define COPY_INVALID_STRING_CONVERSION 6

int copy_and_convert_field_bindinfo(StatementClass *stmt, OID field_type,
                                    int atttypmod, void *value, int col);
int copy_and_convert_field(StatementClass *stmt, OID field_type, int atttypmod,
                           void *value, SQLSMALLINT fCType, int precision,
                           PTR rgbValue, SQLLEN cbValueMax, SQLLEN *pcbValue,
                           SQLLEN *pIndicator);

SQLLEN es_hex2bin(const char *in, char *out, SQLLEN len);

#ifdef __cplusplus
}
#endif
#endif
