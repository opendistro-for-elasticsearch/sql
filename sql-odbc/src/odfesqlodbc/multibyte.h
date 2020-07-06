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

#ifndef __MULTIBUYTE_H__
#define __MULTIBUYTE_H__

/*
 *
 * Multibyte library header
 *
 */
#include "es_odbc.h"
#include "qresult.h"

/* Elastic client encoding */
enum {
    SQL_ASCII = 0 /* SQL/ASCII */
    ,
    EUC_JP /* EUC for Japanese */
    ,
    EUC_CN /* EUC for Chinese */
    ,
    EUC_KR /* EUC for Korean */
    ,
    EUC_TW /* EUC for Taiwan */
    ,
    JOHAB,
    UTF8 /* Unicode UTF-8 */
    ,
    MULE_INTERNAL /* Mule internal code */
    ,
    LATIN1 /* ISO-8859 Latin 1 */
    ,
    LATIN2 /* ISO-8859 Latin 2 */
    ,
    LATIN3 /* ISO-8859 Latin 3 */
    ,
    LATIN4 /* ISO-8859 Latin 4 */
    ,
    LATIN5 /* ISO-8859 Latin 5 */
    ,
    LATIN6 /* ISO-8859 Latin 6 */
    ,
    LATIN7 /* ISO-8859 Latin 7 */
    ,
    LATIN8 /* ISO-8859 Latin 8 */
    ,
    LATIN9 /* ISO-8859 Latin 9 */
    ,
    LATIN10 /* ISO-8859 Latin 10 */
    ,
    WIN1256 /* Arabic Windows */
    ,
    WIN1258 /* Vietnamese Windows */
    ,
    WIN866 /* Alternativny Variant (MS-DOS CP866) */
    ,
    WIN874 /* Thai Windows */
    ,
    KOI8R /* KOI8-R/U */
    ,
    WIN1251 /* Cyrillic Windows */
    ,
    WIN1252 /* Western Europe Windows */
    ,
    ISO_8859_5 /* ISO-8859-5 */
    ,
    ISO_8859_6 /* ISO-8859-6 */
    ,
    ISO_8859_7 /* ISO-8859-7 */
    ,
    ISO_8859_8 /* ISO-8859-8 */
    ,
    WIN1250 /* Central Europe Windows */
    ,
    WIN1253 /* Greek Windows */
    ,
    WIN1254 /* Turkish Windows */
    ,
    WIN1255 /* Hebrew Windows */
    ,
    WIN1257 /* Baltic(North Europe) Windows */
    ,
    EUC_JIS_2004 /* EUC for SHIFT-JIS-2004 Japanese */
    ,
    SJIS /* Shift JIS */
    ,
    BIG5 /* Big5 */
    ,
    GBK /* GBK */
    ,
    UHC /* UHC */
    ,
    GB18030 /* GB18030 */
    ,
    SHIFT_JIS_2004 /* SHIFT-JIS-2004 Japanese, JIS X 0213 */
    ,
    OTHER = -1
};

/* Old Type Compatible */
typedef struct {
    int ccsc;
    const UCHAR *encstr;
    ssize_t pos;
    int ccst;
} encoded_str;

#ifdef __cplusplus
extern "C" {
#endif
int es_CS_code(const char *stat_string);
int encoded_nextchar(encoded_str *encstr);
int encoded_byte_check(encoded_str *encstr, size_t abspos);
const char *derive_locale_encoding(const char *dbencoding);
int es_mb_maxlen(int characterset_code);
#ifdef __cplusplus
}
#endif
#define ENCODE_STATUS(enc) ((enc).ccst)
#define ENCODE_PTR(enc) ((enc).encstr + (enc).pos)
#define MBCS_NON_ASCII(enc) (0 != (enc).ccst || (enc).encstr[(enc).pos] >= 0x80)

void encoded_str_constr(encoded_str *encstr, int ccsc, const char *str);
#define make_encoded_str(encstr, conn, str) \
    encoded_str_constr(encstr, conn->ccsc, str)
#endif /* __MULTIBUYTE_H__ */
