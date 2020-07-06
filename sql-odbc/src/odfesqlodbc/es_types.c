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

#include "es_types.h"

#include "dlg_specific.h"
#include "environ.h"
#include "es_connection.h"
#include "qresult.h"
#include "statement.h"
#ifndef WIN32
#include <limits.h>
#endif

#define EXPERIMENTAL_CURRENTLY

SQLSMALLINT ansi_to_wtype(const ConnectionClass *self, SQLSMALLINT ansitype) {
#ifndef UNICODE_SUPPORT
    return ansitype;
#else
    if (!ALLOW_WCHAR(self))
        return ansitype;
    switch (ansitype) {
        case SQL_CHAR:
            return SQL_WCHAR;
        case SQL_VARCHAR:
            return SQL_WVARCHAR;
        case SQL_LONGVARCHAR:
            return SQL_WLONGVARCHAR;
    }
    return ansitype;
#endif /* UNICODE_SUPPORT */
}

/*	These are NOW the SQL Types reported in SQLGetTypeInfo.  */
SQLSMALLINT sqlTypes[] = {
    SQL_BIGINT,
    /* SQL_BINARY, -- Commented out because VarBinary is more correct. */
    SQL_BIT, SQL_CHAR, SQL_TYPE_DATE, SQL_DATE, SQL_DECIMAL, SQL_DOUBLE,
    SQL_FLOAT, SQL_INTEGER, SQL_LONGVARBINARY, SQL_LONGVARCHAR, SQL_NUMERIC,
    SQL_REAL, SQL_SMALLINT, SQL_TYPE_TIME, SQL_TYPE_TIMESTAMP, SQL_TIME,
    SQL_TIMESTAMP, SQL_TINYINT, SQL_VARBINARY, SQL_VARCHAR,
#ifdef UNICODE_SUPPORT
    SQL_WCHAR, SQL_WVARCHAR, SQL_WLONGVARCHAR,
#endif /* UNICODE_SUPPORT */
    SQL_GUID,
/* AFAIK SQL_INTERVAL types cause troubles in some spplications */
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
    SQL_INTERVAL_MONTH, SQL_INTERVAL_YEAR, SQL_INTERVAL_YEAR_TO_MONTH,
    SQL_INTERVAL_DAY, SQL_INTERVAL_HOUR, SQL_INTERVAL_MINUTE,
    SQL_INTERVAL_SECOND, SQL_INTERVAL_DAY_TO_HOUR, SQL_INTERVAL_DAY_TO_MINUTE,
    SQL_INTERVAL_DAY_TO_SECOND, SQL_INTERVAL_HOUR_TO_MINUTE,
    SQL_INTERVAL_HOUR_TO_SECOND, SQL_INTERVAL_MINUTE_TO_SECOND,
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
    0};

#ifdef ODBCINT64
#define ALLOWED_C_BIGINT SQL_C_SBIGINT
/* #define	ALLOWED_C_BIGINT	SQL_C_CHAR */ /* Delphi should be either ? */
#else
#define ALLOWED_C_BIGINT SQL_C_CHAR
#endif

OID es_true_type(const ConnectionClass *conn, OID type, OID basetype) {
    if (0 == basetype)
        return type;
    else if (0 == type)
        return basetype;
    else if (type == (OID)conn->lobj_type)
        return type;
    return basetype;
}

#define MONTH_BIT (1 << 17)
#define YEAR_BIT (1 << 18)
#define DAY_BIT (1 << 19)
#define HOUR_BIT (1 << 26)
#define MINUTE_BIT (1 << 27)
#define SECOND_BIT (1 << 28)

static Int4 getCharColumnSizeX(const ConnectionClass *conn, OID type,
                               int atttypmod, int adtsize_or_longestlen,
                               int handle_unknown_size_as) {
    int p = -1, maxsize;
    MYLOG(ES_TRACE, "entering type=%d, atttypmod=%d, adtsize_or=%d, unknown = %d\n",
          type, atttypmod, adtsize_or_longestlen, handle_unknown_size_as);

    maxsize = MAX_VARCHAR_SIZE;
#ifdef UNICODE_SUPPORT
    if (CC_is_in_unicode_driver(conn) && isSqlServr() && maxsize > 4000)
        maxsize = 4000;
#endif /* UNICODE_SUPPORT */

    if (maxsize == TEXT_FIELD_SIZE + 1) /* magic length for testing */
        maxsize = 0;

    /*
     * Static ColumnSize (i.e., the Maximum ColumnSize of the datatype) This
     * has nothing to do with a result set.
     */
    MYLOG(ES_DEBUG, "!!! atttypmod  < 0 ?\n");
    if (atttypmod < 0 && adtsize_or_longestlen < 0)
        return maxsize;

    MYLOG(ES_DEBUG, "!!! adtsize_or_logngest=%d\n",
          adtsize_or_longestlen);
    p = adtsize_or_longestlen; /* longest */
                               /*
                                * Catalog Result Sets -- use assigned column width (i.e., from
                                * set_tuplefield_string)
                                */
    MYLOG(ES_DEBUG, "!!! catalog_result=%d\n", handle_unknown_size_as);
    if (UNKNOWNS_AS_LONGEST == handle_unknown_size_as) {
        MYLOG(ES_DEBUG, "LONGEST: p = %d\n", p);
        if (p > 0 && (atttypmod < 0 || atttypmod > p))
            return p;
    }
    if (TYPE_MAY_BE_ARRAY(type)) {
        if (p > 0)
            return p;
        return maxsize;
    }

    /* Size is unknown -- handle according to parameter */
    if (atttypmod > 0) /* maybe the length is known */
    {
        return atttypmod;
    }

    /* The type is really unknown */
    switch (handle_unknown_size_as) {
        case UNKNOWNS_AS_DONTKNOW:
            return -1;
        case UNKNOWNS_AS_LONGEST:
        case UNKNOWNS_AS_MAX:
            break;
        default:
            return -1;
    }
    if (maxsize <= 0)
        return maxsize;
    switch (type) {
        case ES_TYPE_BPCHAR:
        case ES_TYPE_VARCHAR:
        case ES_TYPE_TEXT:
            return maxsize;
    }

    if (p > maxsize)
        maxsize = p;
    return maxsize;
}

/*
 *	Specify when handle_unknown_size_as parameter is unused
 */
#define UNUSED_HANDLE_UNKNOWN_SIZE_AS (-2)

static SQLSMALLINT getNumericDecimalDigitsX(const ConnectionClass *conn,
                                            OID type, int atttypmod,
                                            int adtsize_or_longest,
                                            int handle_unknown_size_as) {
    UNUSED(conn, handle_unknown_size_as);
    SQLSMALLINT default_decimal_digits = 6;

    MYLOG(ES_TRACE, "entering type=%d, atttypmod=%d\n", type, atttypmod);

    if (atttypmod < 0 && adtsize_or_longest < 0)
        return default_decimal_digits;

    if (atttypmod > -1)
        return (SQLSMALLINT)(atttypmod & 0xffff);
    if (adtsize_or_longest <= 0)
        return default_decimal_digits;
    adtsize_or_longest >>= 16; /* extract the scale part */
    return (SQLSMALLINT)adtsize_or_longest;
}

static Int4 /* Elasticsearch restritiction */
getNumericColumnSizeX(const ConnectionClass *conn, OID type, int atttypmod,
                      int adtsize_or_longest, int handle_unknown_size_as) {
    UNUSED(conn);
    Int4 default_column_size = 28;
    MYLOG(ES_TRACE, "entering type=%d, typmod=%d\n", type, atttypmod);

    if (atttypmod > -1)
        return (atttypmod >> 16) & 0xffff;
    switch (handle_unknown_size_as) {
        case UNKNOWNS_AS_DONTKNOW:
            return SQL_NO_TOTAL;
    }
    if (adtsize_or_longest <= 0)
        return default_column_size;
    adtsize_or_longest %= (1 << 16); /* extract the precision part */
    switch (handle_unknown_size_as) {
        case UNKNOWNS_AS_MAX:
            return adtsize_or_longest > default_column_size
                       ? adtsize_or_longest
                       : default_column_size;
        default:
            if (adtsize_or_longest < 10)
                adtsize_or_longest = 10;
    }
    return adtsize_or_longest;
}

static SQLSMALLINT getTimestampDecimalDigitsX(const ConnectionClass *conn,
                                              OID type, int atttypmod) {
    UNUSED(conn);
    MYLOG(ES_DEBUG, "type=%d, atttypmod=%d\n", type, atttypmod);
    return (SQLSMALLINT)(atttypmod > -1 ? atttypmod : 6);
}

#ifdef ES_INTERVAL_AS_SQL_INTERVAL
static SQLSMALLINT getIntervalDecimalDigits(OID type, int atttypmod) {
    Int4 prec;

    MYLOG(ES_TRACE, "entering type=%d, atttypmod=%d\n", type, atttypmod);

    if ((atttypmod & SECOND_BIT) == 0)
        return 0;
    return (SQLSMALLINT)((prec = atttypmod & 0xffff) == 0xffff ? 6 : prec);
}
#endif  // ES_INTERVAL_AS_SQL_INTERVAL

SQLSMALLINT
estype_attr_to_concise_type(const ConnectionClass *conn, OID type,
                            int atttypmod, int adtsize_or_longestlen,
                            int handle_unknown_size_as) {
    EnvironmentClass *env = (EnvironmentClass *)CC_get_env(conn);
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
    SQLSMALLINT sqltype;
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
    BOOL bLongVarchar, bFixed = FALSE;

    switch (type) {
        case ES_TYPE_CHAR:
            return ansi_to_wtype(conn, SQL_CHAR);
        case ES_TYPE_NAME:
        case ES_TYPE_REFCURSOR:
            return ansi_to_wtype(conn, SQL_VARCHAR);

        case ES_TYPE_BPCHAR:
            bFixed = TRUE;
        case ES_TYPE_VARCHAR:
            if (getCharColumnSizeX(conn, type, atttypmod, adtsize_or_longestlen,
                                   handle_unknown_size_as)
                > MAX_VARCHAR_SIZE)
                bLongVarchar = TRUE;
            else
                bLongVarchar = FALSE;
            return ansi_to_wtype(conn, bLongVarchar
                                           ? SQL_LONGVARCHAR
                                           : (bFixed ? SQL_CHAR : SQL_VARCHAR));
        case ES_TYPE_TEXT:
            bLongVarchar = DEFAULT_TEXTASLONGVARCHAR;
            if (bLongVarchar) {
                int column_size = getCharColumnSizeX(conn, type, atttypmod,
                                                     adtsize_or_longestlen,
                                                     handle_unknown_size_as);
                if (column_size > 0 && column_size <= MAX_VARCHAR_SIZE)
                    bLongVarchar = FALSE;
            }
            return ansi_to_wtype(conn,
                                 bLongVarchar ? SQL_LONGVARCHAR : SQL_VARCHAR);

        case ES_TYPE_BYTEA:
            return SQL_VARBINARY;
        case ES_TYPE_LO_UNDEFINED:
            return SQL_LONGVARBINARY;

        case ES_TYPE_INT2:
            return SQL_SMALLINT;

        case ES_TYPE_OID:
        case ES_TYPE_XID:
        case ES_TYPE_INT4:
            return SQL_INTEGER;

            /* Change this to SQL_BIGINT for ODBC v3 bjm 2001-01-23 */
        case ES_TYPE_INT8:
            if (conn->ms_jet)
                return SQL_NUMERIC; /* maybe a little better than SQL_VARCHAR */
            return SQL_BIGINT;

        case ES_TYPE_NUMERIC:
            return SQL_NUMERIC;

        case ES_TYPE_FLOAT4:
            return SQL_REAL;
        case ES_TYPE_FLOAT8:
            return SQL_FLOAT;
        case ES_TYPE_DATE:
            if (EN_is_odbc3(env))
                return SQL_TYPE_DATE;
            return SQL_DATE;
        case ES_TYPE_TIME:
            if (EN_is_odbc3(env))
                return SQL_TYPE_TIME;
            return SQL_TIME;
        case ES_TYPE_ABSTIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
        case ES_TYPE_TIMESTAMP:
            if (EN_is_odbc3(env))
                return SQL_TYPE_TIMESTAMP;
            return SQL_TIMESTAMP;
        case ES_TYPE_MONEY:
            return SQL_FLOAT;
        case ES_TYPE_BOOL:
            return SQL_BIT;
        case ES_TYPE_XML:
            return ansi_to_wtype(conn, SQL_LONGVARCHAR);
        case ES_TYPE_INET:
        case ES_TYPE_CIDR:
        case ES_TYPE_MACADDR:
            return ansi_to_wtype(conn, SQL_VARCHAR);
        case ES_TYPE_UUID:
            return SQL_GUID;

        case ES_TYPE_INTERVAL:
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
            if (sqltype = get_interval_type(atttypmod, NULL), 0 != sqltype)
                return sqltype;
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
            return ansi_to_wtype(conn, SQL_VARCHAR);

        default:

            /*
             * first, check to see if 'type' is in list.  If not, look up
             * with query. Add oid, name to list.  If it's already in
             * list, just return.
             */
            /* hack until permanent type is available */
            if (type == (OID)conn->lobj_type)
                return SQL_LONGVARBINARY;

            bLongVarchar = DEFAULT_UNKNOWNSASLONGVARCHAR;
            if (bLongVarchar) {
                int column_size = getCharColumnSizeX(conn, type, atttypmod,
                                                     adtsize_or_longestlen,
                                                     handle_unknown_size_as);
                if (column_size > 0
                    && column_size <= MAX_VARCHAR_SIZE)
                    bLongVarchar = FALSE;
            }
#ifdef EXPERIMENTAL_CURRENTLY
            return ansi_to_wtype(conn,
                                 bLongVarchar ? SQL_LONGVARCHAR : SQL_VARCHAR);
#else
            return bLongVarchar ? SQL_LONGVARCHAR : SQL_VARCHAR;
#endif /* EXPERIMENTAL_CURRENTLY */
    }
}

SQLSMALLINT
estype_attr_to_sqldesctype(const ConnectionClass *conn, OID type, int atttypmod,
                           int adtsize_or_longestlen,
                           int handle_unknown_size_as) {
    SQLSMALLINT rettype;

#ifdef ES_INTERVAL_AS_SQL_INTERVAL
    if (ES_TYPE_INTERVAL == type)
        return SQL_INTERVAL;
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
    switch (rettype = estype_attr_to_concise_type(conn, type, atttypmod,
                                                  adtsize_or_longestlen,
                                                  handle_unknown_size_as)) {
        case SQL_TYPE_DATE:
        case SQL_TYPE_TIME:
        case SQL_TYPE_TIMESTAMP:
            return SQL_DATETIME;
    }
    return rettype;
}

SQLSMALLINT
estype_attr_to_datetime_sub(const ConnectionClass *conn, OID type,
                            int atttypmod) {
    UNUSED(conn, type, atttypmod);
    return -1;
}

SQLSMALLINT
estype_attr_to_ctype(const ConnectionClass *conn, OID type, int atttypmod) {
    UNUSED(atttypmod);
    EnvironmentClass *env = (EnvironmentClass *)CC_get_env(conn);
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
    SQLSMALLINT ctype;
#endif /* ES_INTERVAL_A_SQL_INTERVAL */

    switch (type) {
        case ES_TYPE_INT8:
            if (!conn->ms_jet)
                return ALLOWED_C_BIGINT;
            return SQL_C_CHAR;
        case ES_TYPE_NUMERIC:
            return SQL_C_CHAR;
        case ES_TYPE_INT2:
            return SQL_C_SSHORT;
        case ES_TYPE_OID:
        case ES_TYPE_XID:
            return SQL_C_ULONG;
        case ES_TYPE_INT4:
            return SQL_C_SLONG;
        case ES_TYPE_FLOAT4:
            return SQL_C_FLOAT;
        case ES_TYPE_FLOAT8:
            return SQL_C_DOUBLE;
        case ES_TYPE_DATE:
            if (EN_is_odbc3(env))
                return SQL_C_TYPE_DATE;
            return SQL_C_DATE;
        case ES_TYPE_TIME:
            if (EN_is_odbc3(env))
                return SQL_C_TYPE_TIME;
            return SQL_C_TIME;
        case ES_TYPE_ABSTIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
        case ES_TYPE_TIMESTAMP:
            if (EN_is_odbc3(env))
                return SQL_C_TYPE_TIMESTAMP;
            return SQL_C_TIMESTAMP;
        case ES_TYPE_MONEY:
            return SQL_C_FLOAT;
        case ES_TYPE_BOOL:
            return SQL_C_BIT;

        case ES_TYPE_BYTEA:
            return SQL_C_BINARY;
        case ES_TYPE_LO_UNDEFINED:
            return SQL_C_BINARY;
        case ES_TYPE_BPCHAR:
        case ES_TYPE_VARCHAR:
        case ES_TYPE_TEXT:
            return ansi_to_wtype(conn, SQL_C_CHAR);
        case ES_TYPE_UUID:
            if (!conn->ms_jet)
                return SQL_C_GUID;
            return ansi_to_wtype(conn, SQL_C_CHAR);

        case ES_TYPE_INTERVAL:
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
            if (ctype = get_interval_type(atttypmod, NULL), 0 != ctype)
                return ctype;
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
            return ansi_to_wtype(conn, SQL_CHAR);

        default:
            /* hack until permanent type is available */
            if (type == (OID)conn->lobj_type)
                return SQL_C_BINARY;

                /* Experimental, Does this work ? */
#ifdef EXPERIMENTAL_CURRENTLY
            return ansi_to_wtype(conn, SQL_C_CHAR);
#else
            return SQL_C_CHAR;
#endif /* EXPERIMENTAL_CURRENTLY */
    }
}

const char *estype_attr_to_name(const ConnectionClass *conn, OID type,
                                int typmod, BOOL auto_increment) {
    UNUSED(conn, typmod, conn, auto_increment);
    switch (type) {
        case ES_TYPE_BOOL:
            return "boolean";
        case ES_TYPE_INT1:
            return "byte";
        case ES_TYPE_INT2:
            return "short";
        case ES_TYPE_INT4:
            return "integer";
        case ES_TYPE_INT8:
            return "long";
        case ES_TYPE_HALF_FLOAT:
            return "half_float";
        case ES_TYPE_FLOAT4:
            return "float";
        case ES_TYPE_FLOAT8:
            return "double";
        case ES_TYPE_SCALED_FLOAT:
            return "scaled_float";
        case ES_TYPE_KEYWORD:
            return "keyword";
        case ES_TYPE_TEXT:
            return "text";
        case ES_TYPE_NESTED:
            return "nested";
        case ES_TYPE_DATETIME:
            return "date";
        case ES_TYPE_OBJECT:
            return "object";
        case ES_TYPE_VARCHAR:
            return "varchar";
        default:
            return "unsupported";
    }
}

Int4 /* Elasticsearch restriction */
estype_attr_column_size(const ConnectionClass *conn, OID type, int atttypmod,
                        int adtsize_or_longest, int handle_unknown_size_as) {
    UNUSED(handle_unknown_size_as, adtsize_or_longest, atttypmod, conn);
    switch (type) {
        case ES_TYPE_BOOL:
            return 1;
        case ES_TYPE_INT1:
            return 3;
        case ES_TYPE_INT2:
            return 5;
        case ES_TYPE_INT4:
            return 10;
        case ES_TYPE_INT8:
            return 19;
        case ES_TYPE_HALF_FLOAT:
            return 7;
        case ES_TYPE_FLOAT4:
            return 7;
        case ES_TYPE_FLOAT8:
            return 15;
        case ES_TYPE_SCALED_FLOAT:
            return 15;
        case ES_TYPE_KEYWORD:
            return 256;
        case ES_TYPE_TEXT:
            return INT_MAX;
        case ES_TYPE_NESTED:
            return 0;
        case ES_TYPE_DATETIME:
            return 24;
        case ES_TYPE_OBJECT:
            return 0;
        case ES_TYPE_UNSUPPORTED:
            return 0;
        default:
            return adtsize_or_longest;
    }
}

SQLSMALLINT
estype_attr_precision(const ConnectionClass *conn, OID type, int atttypmod,
                      int adtsize_or_longest, int handle_unknown_size_as) {
    switch (type) {
        case ES_TYPE_NUMERIC:
            return (SQLSMALLINT)getNumericColumnSizeX(conn, type, atttypmod,
                                                      adtsize_or_longest,
                                                      handle_unknown_size_as);
        case ES_TYPE_TIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
            return getTimestampDecimalDigitsX(conn, type, atttypmod);
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
        case ES_TYPE_INTERVAL:
            return getIntervalDecimalDigits(type, atttypmod);
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */
    }
    return -1;
}

Int4 estype_attr_display_size(const ConnectionClass *conn, OID type,
                              int atttypmod, int adtsize_or_longestlen,
                              int handle_unknown_size_as) {
    int dsize;

    switch (type) {
        case ES_TYPE_INT2:
            return 6;

        case ES_TYPE_OID:
        case ES_TYPE_XID:
            return 10;

        case ES_TYPE_INT4:
            return 11;

        case ES_TYPE_INT8:
            return 20; /* signed: 19 digits + sign */

        case ES_TYPE_NUMERIC:
            dsize = getNumericColumnSizeX(conn, type, atttypmod,
                                          adtsize_or_longestlen,
                                          handle_unknown_size_as);
            return dsize <= 0 ? dsize : dsize + 2;

        case ES_TYPE_MONEY:
            return 15; /* ($9,999,999.99) */

        case ES_TYPE_FLOAT4: /* a sign, ES_REAL_DIGITS digits, a decimal point,
                                the letter E, a sign, and 2 digits */
            return (1 + ES_REAL_DIGITS + 1 + 1 + 3);

        case ES_TYPE_FLOAT8: /* a sign, ES_DOUBLE_DIGITS digits, a decimal
                                point, the letter E, a sign, and 3 digits */
            return (1 + ES_DOUBLE_DIGITS + 1 + 1 + 1 + 3);

        case ES_TYPE_MACADDR:
            return 17;
        case ES_TYPE_INET:
        case ES_TYPE_CIDR:
            return sizeof("xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:255.255.255.255/128");
        case ES_TYPE_UUID:
            return 36;
        case ES_TYPE_INTERVAL:
            return 30;

            /* Character types use regular precision */
        default:
            return estype_attr_column_size(conn, type, atttypmod,
                                           adtsize_or_longestlen,
                                           handle_unknown_size_as);
    }
}

Int4 estype_attr_buffer_length(const ConnectionClass *conn, OID type,
                               int atttypmod, int adtsize_or_longestlen,
                               int handle_unknown_size_as) {
    int dsize;

    switch (type) {
        case ES_TYPE_INT2:
            return 2; /* sizeof(SQLSMALLINT) */

        case ES_TYPE_OID:
        case ES_TYPE_XID:
        case ES_TYPE_INT4:
            return 4; /* sizeof(SQLINTEGER) */

        case ES_TYPE_INT8:
            if (SQL_C_CHAR == estype_attr_to_ctype(conn, type, atttypmod))
                return 20; /* signed: 19 digits + sign */
            return 8;      /* sizeof(SQLSBININT) */

        case ES_TYPE_NUMERIC:
            dsize = getNumericColumnSizeX(conn, type, atttypmod,
                                          adtsize_or_longestlen,
                                          handle_unknown_size_as);
            return dsize <= 0 ? dsize : dsize + 2;

        case ES_TYPE_FLOAT4:
        case ES_TYPE_MONEY:
            return 4; /* sizeof(SQLREAL) */

        case ES_TYPE_FLOAT8:
            return 8; /* sizeof(SQLFLOAT) */

        case ES_TYPE_DATE:
        case ES_TYPE_TIME:
            return 6; /* sizeof(DATE(TIME)_STRUCT) */

        case ES_TYPE_ABSTIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
            return 16; /* sizeof(TIMESTAMP_STRUCT) */

        case ES_TYPE_MACADDR:
            return 17;
        case ES_TYPE_INET:
        case ES_TYPE_CIDR:
            return sizeof("xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:255.255.255.255/128");
        case ES_TYPE_UUID:
            return 16; /* sizeof(SQLGUID) */

            /* Character types use the default precision */
        case ES_TYPE_VARCHAR:
        case ES_TYPE_BPCHAR: {
            int coef = 1;
            Int4 prec = estype_attr_column_size(conn, type, atttypmod,
                                                adtsize_or_longestlen,
                                                handle_unknown_size_as),
                 maxvarc;
            if (SQL_NO_TOTAL == prec)
                return prec;
#ifdef UNICODE_SUPPORT
            if (CC_is_in_unicode_driver(conn))
                return prec * WCLEN;
#endif /* UNICODE_SUPPORT */
            coef = conn->mb_maxbyte_per_char;
            if (coef < 2)
                /* CR -> CR/LF */
                coef = 2;
            if (coef == 1)
                return prec;
            maxvarc = MAX_VARCHAR_SIZE;
            if (prec <= maxvarc && prec * coef > maxvarc)
                return maxvarc;
            return coef * prec;
        }
#ifdef ES_INTERVAL_AS_SQL_INTERVAL
        case ES_TYPE_INTERVAL:
            return sizeof(SQL_INTERVAL_STRUCT);
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */

        default:
            return estype_attr_column_size(conn, type, atttypmod,
                                           adtsize_or_longestlen,
                                           handle_unknown_size_as);
    }
}

/*
 */
Int4 estype_attr_desclength(const ConnectionClass *conn, OID type,
                            int atttypmod, int adtsize_or_longestlen,
                            int handle_unknown_size_as) {
    int dsize;

    switch (type) {
        case ES_TYPE_INT2:
            return 2;

        case ES_TYPE_OID:
        case ES_TYPE_XID:
        case ES_TYPE_INT4:
            return 4;

        case ES_TYPE_INT8:
            return 20; /* signed: 19 digits + sign */

        case ES_TYPE_NUMERIC:
            dsize = getNumericColumnSizeX(conn, type, atttypmod,
                                          adtsize_or_longestlen,
                                          handle_unknown_size_as);
            return dsize <= 0 ? dsize : dsize + 2;

        case ES_TYPE_FLOAT4:
        case ES_TYPE_MONEY:
            return 4;

        case ES_TYPE_FLOAT8:
            return 8;

        case ES_TYPE_DATE:
        case ES_TYPE_TIME:
        case ES_TYPE_ABSTIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
        case ES_TYPE_TIMESTAMP:
        case ES_TYPE_VARCHAR:
        case ES_TYPE_BPCHAR:
            return estype_attr_column_size(conn, type, atttypmod,
                                           adtsize_or_longestlen,
                                           handle_unknown_size_as);
        default:
            return estype_attr_column_size(conn, type, atttypmod,
                                           adtsize_or_longestlen,
                                           handle_unknown_size_as);
    }
}

Int2 estype_attr_decimal_digits(const ConnectionClass *conn, OID type,
                                int atttypmod, int adtsize_or_longestlen,
                                int UNUSED_handle_unknown_size_as) {
    switch (type) {
        case ES_TYPE_INT2:
        case ES_TYPE_OID:
        case ES_TYPE_XID:
        case ES_TYPE_INT4:
        case ES_TYPE_INT8:
        case ES_TYPE_FLOAT4:
        case ES_TYPE_FLOAT8:
        case ES_TYPE_MONEY:
        case ES_TYPE_BOOL:

            /*
             * Number of digits to the right of the decimal point in
             * "yyyy-mm=dd hh:mm:ss[.f...]"
             */
        case ES_TYPE_ABSTIME:
        case ES_TYPE_TIMESTAMP:
            return 0;
        case ES_TYPE_TIME:
        case ES_TYPE_DATETIME:
        case ES_TYPE_TIMESTAMP_NO_TMZONE:
            /* return 0; */
            return getTimestampDecimalDigitsX(conn, type, atttypmod);

        case ES_TYPE_NUMERIC:
            return getNumericDecimalDigitsX(conn, type, atttypmod,
                                            adtsize_or_longestlen,
                                            UNUSED_handle_unknown_size_as);

#ifdef ES_INTERVAL_AS_SQL_INTERVAL
        case ES_TYPE_INTERVAL:
            return getIntervalDecimalDigits(type, atttypmod);
#endif /* ES_INTERVAL_AS_SQL_INTERVAL */

        default:
            return -1;
    }
}

Int2 estype_attr_scale(const ConnectionClass *conn, OID type, int atttypmod,
                       int adtsize_or_longestlen,
                       int UNUSED_handle_unknown_size_as) {
    switch (type) {
        case ES_TYPE_NUMERIC:
            return getNumericDecimalDigitsX(conn, type, atttypmod,
                                            adtsize_or_longestlen,
                                            UNUSED_handle_unknown_size_as);
    }
    return -1;
}

Int4 estype_attr_transfer_octet_length(const ConnectionClass *conn, OID type,
                                       int atttypmod,
                                       int handle_unknown_size_as) {
    int coef = 1;
    Int4 maxvarc, column_size;

    switch (type) {
        case ES_TYPE_VARCHAR:
        case ES_TYPE_BPCHAR:
        case ES_TYPE_TEXT:
        case ES_TYPE_UNKNOWN:
            column_size = estype_attr_column_size(
                conn, type, atttypmod, ES_ADT_UNSET, handle_unknown_size_as);
            if (SQL_NO_TOTAL == column_size)
                return column_size;
#ifdef UNICODE_SUPPORT
            if (CC_is_in_unicode_driver(conn))
                return column_size * WCLEN;
#endif /* UNICODE_SUPPORT */
            coef = conn->mb_maxbyte_per_char;
            if (coef < 2)
                /* CR -> CR/LF */
                coef = 2;
            if (coef == 1)
                return column_size;
            maxvarc = MAX_VARCHAR_SIZE;
            if (column_size <= maxvarc && column_size * coef > maxvarc)
                return maxvarc;
            return coef * column_size;
        case ES_TYPE_BYTEA:
            return estype_attr_column_size(conn, type, atttypmod, ES_ADT_UNSET,
                                           handle_unknown_size_as);
        default:
            if (type == (OID)conn->lobj_type)
                return estype_attr_column_size(conn, type, atttypmod,
                                               ES_ADT_UNSET,
                                               handle_unknown_size_as);
    }
    return -1;
}

/*
 * Casting parameters e.g. ?::timestamp is much more flexible
 * than specifying parameter datatype oids determined by
 * sqltype_to_bind_estype() via parse message.
 */
const char *sqltype_to_escast(const ConnectionClass *conn,
                              SQLSMALLINT fSqlType) {
    const char *esCast = NULL_STRING;

    switch (fSqlType) {
        case SQL_BINARY:
        case SQL_VARBINARY:
            esCast = "::bytea";
            break;
        case SQL_TYPE_DATE:
        case SQL_DATE:
            esCast = "::date";
            break;
        case SQL_DECIMAL:
        case SQL_NUMERIC:
            esCast = "::numeric";
            break;
        case SQL_BIGINT:
            esCast = "::int8";
            break;
        case SQL_INTEGER:
            esCast = "::int4";
            break;
        case SQL_REAL:
            esCast = "::float4";
            break;
        case SQL_SMALLINT:
        case SQL_TINYINT:
            esCast = "::int2";
            break;
        case SQL_TIME:
        case SQL_TYPE_TIME:
            esCast = "::time";
            break;
        case SQL_TIMESTAMP:
        case SQL_TYPE_TIMESTAMP:
            esCast = "::timestamp";
            break;
        case SQL_GUID:
            if (ES_VERSION_GE(conn, 8.3))
                esCast = "::uuid";
            break;
        case SQL_INTERVAL_MONTH:
        case SQL_INTERVAL_YEAR:
        case SQL_INTERVAL_YEAR_TO_MONTH:
        case SQL_INTERVAL_DAY:
        case SQL_INTERVAL_HOUR:
        case SQL_INTERVAL_MINUTE:
        case SQL_INTERVAL_SECOND:
        case SQL_INTERVAL_DAY_TO_HOUR:
        case SQL_INTERVAL_DAY_TO_MINUTE:
        case SQL_INTERVAL_DAY_TO_SECOND:
        case SQL_INTERVAL_HOUR_TO_MINUTE:
        case SQL_INTERVAL_HOUR_TO_SECOND:
        case SQL_INTERVAL_MINUTE_TO_SECOND:
            esCast = "::interval";
            break;
    }

    return esCast;
}

OID sqltype_to_estype(const ConnectionClass *conn, SQLSMALLINT fSqlType) {
    OID esType = 0; 
    switch (fSqlType) {
        case SQL_BINARY:
            esType = ES_TYPE_BYTEA;
            break;

        case SQL_CHAR:
            esType = ES_TYPE_BPCHAR;
            break;

#ifdef UNICODE_SUPPORT
        case SQL_WCHAR:
            esType = ES_TYPE_BPCHAR;
            break;
#endif /* UNICODE_SUPPORT */

        case SQL_BIT:
            esType = ES_TYPE_BOOL;
            break;

        case SQL_TYPE_DATE:
        case SQL_DATE:
            esType = ES_TYPE_DATE;
            break;

        case SQL_DOUBLE:
        case SQL_FLOAT:
            esType = ES_TYPE_FLOAT8;
            break;

        case SQL_DECIMAL:
        case SQL_NUMERIC:
            esType = ES_TYPE_NUMERIC;
            break;

        case SQL_BIGINT:
            esType = ES_TYPE_INT8;
            break;

        case SQL_INTEGER:
            esType = ES_TYPE_INT4;
            break;

        case SQL_LONGVARBINARY:
            esType = conn->lobj_type;
            break;

        case SQL_LONGVARCHAR:
            esType = ES_TYPE_VARCHAR;
            break;

#ifdef UNICODE_SUPPORT
        case SQL_WLONGVARCHAR:
            esType = ES_TYPE_VARCHAR;
            break;
#endif /* UNICODE_SUPPORT */

        case SQL_REAL:
            esType = ES_TYPE_FLOAT4;
            break;

        case SQL_SMALLINT:
        case SQL_TINYINT:
            esType = ES_TYPE_INT2;
            break;

        case SQL_TIME:
        case SQL_TYPE_TIME:
            esType = ES_TYPE_TIME;
            break;

        case SQL_TIMESTAMP:
        case SQL_TYPE_TIMESTAMP:
            esType = ES_TYPE_DATETIME;
            break;

        case SQL_VARBINARY:
            esType = ES_TYPE_BYTEA;
            break;

        case SQL_VARCHAR:
            esType = ES_TYPE_VARCHAR;
            break;

#ifdef UNICODE_SUPPORT
        case SQL_WVARCHAR:
            esType = ES_TYPE_VARCHAR;
            break;
#endif /* UNICODE_SUPPORT */

        case SQL_GUID:
            if (ES_VERSION_GE(conn, 8.3))
                esType = ES_TYPE_UUID;
            break;

        case SQL_INTERVAL_MONTH:
        case SQL_INTERVAL_YEAR:
        case SQL_INTERVAL_YEAR_TO_MONTH:
        case SQL_INTERVAL_DAY:
        case SQL_INTERVAL_HOUR:
        case SQL_INTERVAL_MINUTE:
        case SQL_INTERVAL_SECOND:
        case SQL_INTERVAL_DAY_TO_HOUR:
        case SQL_INTERVAL_DAY_TO_MINUTE:
        case SQL_INTERVAL_DAY_TO_SECOND:
        case SQL_INTERVAL_HOUR_TO_MINUTE:
        case SQL_INTERVAL_HOUR_TO_SECOND:
        case SQL_INTERVAL_MINUTE_TO_SECOND:
            esType = ES_TYPE_INTERVAL;
            break;
    }

    return esType;
}

static int getAtttypmodEtc(const StatementClass *stmt, int col,
                           int *adtsize_or_longestlen) {
    int atttypmod = -1;

    if (NULL != adtsize_or_longestlen)
        *adtsize_or_longestlen = ES_ADT_UNSET;
    if (col >= 0) {
        const QResultClass *res;

        if (res = SC_get_Curres(stmt), NULL != res) {
            atttypmod = QR_get_atttypmod(res, col);
            if (NULL != adtsize_or_longestlen) {
                if (stmt->catalog_result)
                    *adtsize_or_longestlen = QR_get_fieldsize(res, col);
                else {
                    *adtsize_or_longestlen = QR_get_display_size(res, col);
                    if (ES_TYPE_NUMERIC == QR_get_field_type(res, col)
                        && atttypmod < 0 && *adtsize_or_longestlen > 0) {
                        SQLULEN i;
                        size_t sval, maxscale = 0;
                        const char *tval, *sptr;

                        for (i = 0; i < res->num_cached_rows; i++) {
                            tval = QR_get_value_backend_text(res, i, col);
                            if (NULL != tval) {
                                sptr = strchr(tval, '.');
                                if (NULL != sptr) {
                                    sval = strlen(tval) - (sptr + 1 - tval);
                                    if (sval > maxscale)
                                        maxscale = sval;
                                }
                            }
                        }
                        *adtsize_or_longestlen += (int)(maxscale << 16);
                    }
                }
            }
        }
    }
    return atttypmod;
}

/*
 *	There are two ways of calling this function:
 *
 *	1.	When going through the supported ES types (SQLGetTypeInfo)
 *
 *	2.	When taking any type id (SQLColumns, SQLGetData)
 *
 *	The first type will always work because all the types defined are returned
 *here. The second type will return a default based on global parameter when it
 *does not know.	This allows for supporting types that are unknown.  All
 *other es routines in here return a suitable default.
 */
SQLSMALLINT
estype_to_concise_type(const StatementClass *stmt, OID type, int col,
                       int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_to_concise_type(SC_get_conn(stmt), type, atttypmod,
                                       adtsize_or_longestlen,
                                       handle_unknown_size_as);
}

SQLSMALLINT
estype_to_sqldesctype(const StatementClass *stmt, OID type, int col,
                      int handle_unknown_size_as) {
    int adtsize_or_longestlen;
    int atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);

    return estype_attr_to_sqldesctype(SC_get_conn(stmt), type, atttypmod,
                                      adtsize_or_longestlen,
                                      handle_unknown_size_as);
}

const char *estype_to_name(const StatementClass *stmt, OID type, int col,
                           BOOL auto_increment) {
    int atttypmod = getAtttypmodEtc(stmt, col, NULL);

    return estype_attr_to_name(SC_get_conn(stmt), type, atttypmod,
                               auto_increment);
}

/*
 *	This corresponds to "precision" in ODBC 2.x.
 *
 *	For ES_TYPE_VARCHAR, ES_TYPE_BPCHAR, ES_TYPE_NUMERIC, SQLColumns will
 *	override this length with the atttypmod length from es_attribute .
 *
 *	If col >= 0, then will attempt to get the info from the result set.
 *	This is used for functions SQLDescribeCol and SQLColAttributes.
 */
Int4 /* Elasticsearch restriction */
estype_column_size(const StatementClass *stmt, OID type, int col,
                   int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_column_size(
        SC_get_conn(stmt), type, atttypmod, adtsize_or_longestlen,
        stmt->catalog_result ? UNKNOWNS_AS_LONGEST : handle_unknown_size_as);
}

/*
 *	precision in ODBC 3.x.
 */
SQLSMALLINT
estype_precision(const StatementClass *stmt, OID type, int col,
                 int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_precision(
        SC_get_conn(stmt), type, atttypmod, adtsize_or_longestlen,
        stmt->catalog_result ? UNKNOWNS_AS_LONGEST : handle_unknown_size_as);
}

Int4 estype_display_size(const StatementClass *stmt, OID type, int col,
                         int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_display_size(
        SC_get_conn(stmt), type, atttypmod, adtsize_or_longestlen,
        stmt->catalog_result ? UNKNOWNS_AS_LONGEST : handle_unknown_size_as);
}

/*
 *	The length in bytes of data transferred on an SQLGetData, SQLFetch,
 *	or SQLFetchScroll operation if SQL_C_DEFAULT is specified.
 */
Int4 estype_buffer_length(const StatementClass *stmt, OID type, int col,
                          int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_buffer_length(
        SC_get_conn(stmt), type, atttypmod, adtsize_or_longestlen,
        stmt->catalog_result ? UNKNOWNS_AS_LONGEST : handle_unknown_size_as);
}

/*
 */
Int4 estype_desclength(const StatementClass *stmt, OID type, int col,
                       int handle_unknown_size_as) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_desclength(
        SC_get_conn(stmt), type, atttypmod, adtsize_or_longestlen,
        stmt->catalog_result ? UNKNOWNS_AS_LONGEST : handle_unknown_size_as);
}

#ifdef NOT_USED
/*
 *	Transfer octet length.
 */
Int4 estype_transfer_octet_length(const StatementClass *stmt, OID type,
                                  int column_size) {
    ConnectionClass *conn = SC_get_conn(stmt);

    int coef = 1;
    Int4 maxvarc;
    switch (type) {
        case ES_TYPE_VARCHAR:
        case ES_TYPE_BPCHAR:
        case ES_TYPE_TEXT:
            if (SQL_NO_TOTAL == column_size)
                return column_size;
#ifdef UNICODE_SUPPORT
            if (CC_is_in_unicode_driver(conn))
                return column_size * WCLEN;
#endif /* UNICODE_SUPPORT */
            coef = conn->mb_maxbyte_per_char;
            if (coef < 2 && (conn->connInfo).lf_conversion)
                /* CR -> CR/LF */
                coef = 2;
            if (coef == 1)
                return column_size;
            maxvarc = conn->connInfo.drivers.max_varchar_size;
            if (column_size <= maxvarc && column_size * coef > maxvarc)
                return maxvarc;
            return coef * column_size;
        case ES_TYPE_BYTEA:
            return column_size;
        default:
            if (type == conn->lobj_type)
                return column_size;
    }
    return -1;
}
#endif /* NOT_USED */

/*
 *	corrsponds to "min_scale" in ODBC 2.x.
 */
Int2 estype_min_decimal_digits(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return -1;
}

/*
 *	corrsponds to "max_scale" in ODBC 2.x.
 */
Int2 estype_max_decimal_digits(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return -1;
}

/*
 *	corrsponds to "scale" in ODBC 2.x.
 */
Int2 estype_decimal_digits(const StatementClass *stmt, OID type, int col) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_decimal_digits(SC_get_conn(stmt), type, atttypmod,
                                      adtsize_or_longestlen,
                                      UNUSED_HANDLE_UNKNOWN_SIZE_AS);
}

/*
 *	"scale" in ODBC 3.x.
 */
Int2 estype_scale(const StatementClass *stmt, OID type, int col) {
    int atttypmod, adtsize_or_longestlen;

    atttypmod = getAtttypmodEtc(stmt, col, &adtsize_or_longestlen);
    return estype_attr_scale(SC_get_conn(stmt), type, atttypmod,
                             adtsize_or_longestlen,
                             UNUSED_HANDLE_UNKNOWN_SIZE_AS);
}

Int2 estype_radix(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return 10;
}

Int2 estype_nullable(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return SQL_NULLABLE_UNKNOWN; /* everything should be nullable unknown */
}

Int2 estype_auto_increment(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return SQL_FALSE;
}

Int2 estype_case_sensitive(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    switch (type) {
        case ES_TYPE_KEYWORD:
        case ES_TYPE_TEXT:
            return SQL_TRUE;

        default:
            return SQL_FALSE;
    }
}

Int2 estype_money(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return SQL_FALSE;
}

Int2 estype_searchable(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return SQL_SEARCHABLE;
}

Int2 estype_unsigned(const ConnectionClass *conn, OID type) {
    UNUSED(conn);
    switch (type) {
        case ES_TYPE_BOOL:
        case ES_TYPE_KEYWORD:
        case ES_TYPE_TEXT:
        case ES_TYPE_NESTED:
        case ES_TYPE_DATETIME:
        case ES_TYPE_OBJECT:
        case ES_TYPE_UNSUPPORTED:
            return SQL_TRUE;

        case ES_TYPE_INT1:
        case ES_TYPE_INT2:
        case ES_TYPE_INT4:
        case ES_TYPE_INT8:
        case ES_TYPE_HALF_FLOAT:
        case ES_TYPE_FLOAT4:
        case ES_TYPE_FLOAT8:
        case ES_TYPE_SCALED_FLOAT:
            return SQL_FALSE;

        default:
            return -1;
    }
}

const char *estype_literal_prefix(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return "`";
}

const char *estype_literal_suffix(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return "`";
}

const char *estype_create_params(const ConnectionClass *conn, OID type) {
    UNUSED(conn, type);
    return NULL;
}

SQLSMALLINT
sqltype_to_default_ctype(const ConnectionClass *conn, SQLSMALLINT sqltype) {
    /*
     * from the table on page 623 of ODBC 2.0 Programmer's Reference
     * (Appendix D)
     */
    switch (sqltype) {
        case SQL_CHAR:
        case SQL_VARCHAR:
        case SQL_LONGVARCHAR:
        case SQL_DECIMAL:
        case SQL_NUMERIC:
            return SQL_C_CHAR;
        case SQL_BIGINT:
            return ALLOWED_C_BIGINT;

#ifdef UNICODE_SUPPORT
        case SQL_WCHAR:
        case SQL_WVARCHAR:
        case SQL_WLONGVARCHAR:
            return ansi_to_wtype(conn, SQL_C_CHAR);
#endif /* UNICODE_SUPPORT */

        case SQL_BIT:
            return SQL_C_BIT;

        case SQL_TINYINT:
            return SQL_C_STINYINT;

        case SQL_SMALLINT:
            return SQL_C_SSHORT;

        case SQL_INTEGER:
            return SQL_C_SLONG;

        case SQL_REAL:
            return SQL_C_FLOAT;

        case SQL_FLOAT:
        case SQL_DOUBLE:
            return SQL_C_DOUBLE;

        case SQL_BINARY:
        case SQL_VARBINARY:
        case SQL_LONGVARBINARY:
            return SQL_C_BINARY;

        case SQL_DATE:
            return SQL_C_DATE;

        case SQL_TIME:
            return SQL_C_TIME;

        case SQL_TIMESTAMP:
            return SQL_C_TIMESTAMP;

        case SQL_TYPE_DATE:
            return SQL_C_TYPE_DATE;

        case SQL_TYPE_TIME:
            return SQL_C_TYPE_TIME;

        case SQL_TYPE_TIMESTAMP:
            return SQL_C_TYPE_TIMESTAMP;

        case SQL_GUID:
            if (conn->ms_jet)
                return SQL_C_CHAR;
            else
                return SQL_C_GUID;

        default:
            /* should never happen */
            return SQL_C_CHAR;
    }
}

Int4 ctype_length(SQLSMALLINT ctype) {
    switch (ctype) {
        case SQL_C_SSHORT:
        case SQL_C_SHORT:
            return sizeof(SWORD);

        case SQL_C_USHORT:
            return sizeof(UWORD);

        case SQL_C_SLONG:
        case SQL_C_LONG:
            return sizeof(SDWORD);

        case SQL_C_ULONG:
            return sizeof(UDWORD);

        case SQL_C_FLOAT:
            return sizeof(SFLOAT);

        case SQL_C_DOUBLE:
            return sizeof(SDOUBLE);

        case SQL_C_BIT:
            return sizeof(UCHAR);

        case SQL_C_STINYINT:
        case SQL_C_TINYINT:
            return sizeof(SCHAR);

        case SQL_C_UTINYINT:
            return sizeof(UCHAR);

        case SQL_C_DATE:
        case SQL_C_TYPE_DATE:
            return sizeof(DATE_STRUCT);

        case SQL_C_TIME:
        case SQL_C_TYPE_TIME:
            return sizeof(TIME_STRUCT);

        case SQL_C_TIMESTAMP:
        case SQL_C_TYPE_TIMESTAMP:
            return sizeof(TIMESTAMP_STRUCT);

        case SQL_C_GUID:
            return sizeof(SQLGUID);
        case SQL_C_INTERVAL_YEAR:
        case SQL_C_INTERVAL_MONTH:
        case SQL_C_INTERVAL_YEAR_TO_MONTH:
        case SQL_C_INTERVAL_DAY:
        case SQL_C_INTERVAL_HOUR:
        case SQL_C_INTERVAL_DAY_TO_HOUR:
        case SQL_C_INTERVAL_MINUTE:
        case SQL_C_INTERVAL_DAY_TO_MINUTE:
        case SQL_C_INTERVAL_HOUR_TO_MINUTE:
        case SQL_C_INTERVAL_SECOND:
        case SQL_C_INTERVAL_DAY_TO_SECOND:
        case SQL_C_INTERVAL_HOUR_TO_SECOND:
        case SQL_C_INTERVAL_MINUTE_TO_SECOND:
            return sizeof(SQL_INTERVAL_STRUCT);
        case SQL_C_NUMERIC:
            return sizeof(SQL_NUMERIC_STRUCT);
        case SQL_C_SBIGINT:
        case SQL_C_UBIGINT:
            return sizeof(SQLBIGINT);

        case SQL_C_BINARY:
        case SQL_C_CHAR:
#ifdef UNICODE_SUPPORT
        case SQL_C_WCHAR:
#endif /* UNICODE_SUPPORT */
            return 0;

        default: /* should never happen */
            return 0;
    }
}
