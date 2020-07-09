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

#include <ctype.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "es_apifunc.h"
#include "es_connection.h"
#include "misc.h"
#include "multibyte.h"
#ifndef WIN32
#include <locale.h>
#endif
#ifndef TRUE
#define TRUE 1
#endif

typedef struct ES_CS {
    char *name;
    int code;
} ES_CS;

static ES_CS CS_Table[] = {
    {"SQL_ASCII", SQL_ASCII},
    {"EUC_JP", EUC_JP},
    {"EUC_CN", EUC_CN},
    {"EUC_KR", EUC_KR},
    {"EUC_TW", EUC_TW},
    {"JOHAB", JOHAB}, /* since 7.3 */
    {"UTF8", UTF8},   /* since 7.2 */
    {"MULE_INTERNAL", MULE_INTERNAL},
    {"LATIN1", LATIN1},
    {"LATIN2", LATIN2},
    {"LATIN3", LATIN3},
    {"LATIN4", LATIN4},
    {"LATIN5", LATIN5},
    {"LATIN6", LATIN6},
    {"LATIN7", LATIN7},
    {"LATIN8", LATIN8},
    {"LATIN9", LATIN9},
    {"LATIN10", LATIN10},
    {"WIN1256", WIN1256}, /* Arabic since 7.3 */
    {"WIN1258", WIN1258}, /* Vietnamese since 8.1 */
    {"WIN866", WIN866},   /* since 8.1 */
    {"WIN874", WIN874},   /* Thai since 7.3 */
    {"KOI8", KOI8R},
    {"WIN1251", WIN1251}, /* Cyrillic */
    {"WIN1252", WIN1252}, /* Western Europe since 8.1 */
    {"ISO_8859_5", ISO_8859_5},
    {"ISO_8859_6", ISO_8859_6},
    {"ISO_8859_7", ISO_8859_7},
    {"ISO_8859_8", ISO_8859_8},
    {"WIN1250", WIN1250}, /* Central Europe */
    {"WIN1253", WIN1253}, /* Greek since 8.2 */
    {"WIN1254", WIN1254}, /* Turkish since 8.2 */
    {"WIN1255", WIN1255}, /* Hebrew since 8.2 */
    {"WIN1257", WIN1257}, /* Baltic(North Europe) since 8.2 */

    {"EUC_JIS_2004",
     EUC_JIS_2004}, /* EUC for SHIFT-JIS-2004 Japanese, since 8.3 */
    {"SJIS", SJIS},
    {"BIG5", BIG5},
    {"GBK", GBK},                       /* since 7.3 */
    {"UHC", UHC},                       /* since 7.3 */
    {"GB18030", GB18030},               /* since 7.3 */
    {"SHIFT_JIS_2004", SHIFT_JIS_2004}, /* SHIFT-JIS-2004 Japanese, standard JIS
                                           X 0213, since 8.3 */
    {"OTHER", OTHER}};

static ES_CS CS_Alias[] = {{"UNICODE", UTF8}, {"TCVN", WIN1258},
                           {"ALT", WIN866},   {"WIN", WIN1251},
                           {"KOI8R", KOI8R},  {"OTHER", OTHER}};

int es_CS_code(const char *characterset_string) {
    int i, c = -1;

    for (i = 0; CS_Table[i].code != OTHER; i++) {
        if (0 == stricmp(characterset_string, CS_Table[i].name)) {
            c = CS_Table[i].code;
            break;
        }
    }
    if (c < 0) {
        for (i = 0; CS_Alias[i].code != OTHER; i++) {
            if (0 == stricmp(characterset_string, CS_Alias[i].name)) {
                c = CS_Alias[i].code;
                break;
            }
        }
    }
    if (c < 0)
        c = OTHER;
    return (c);
}

int es_mb_maxlen(int characterset_code) {
    switch (characterset_code) {
        case UTF8:
            return 4;
        case EUC_TW:
            return 4;
        case EUC_JIS_2004:
        case EUC_JP:
        case GB18030:
            return 3;
        case SHIFT_JIS_2004:
        case SJIS:
        case BIG5:
        case GBK:
        case UHC:
        case EUC_CN:
        case EUC_KR:
        case JOHAB:
            return 2;
        default:
            return 1;
    }
}

static int es_CS_stat(int stat, unsigned int character, int characterset_code) {
    if (character == 0)
        stat = 0;
    switch (characterset_code) {
        case UTF8: {
            if (stat < 2 && character >= 0x80) {
                if (character >= 0xfc)
                    stat = 6;
                else if (character >= 0xf8)
                    stat = 5;
                else if (character >= 0xf0)
                    stat = 4;
                else if (character >= 0xe0)
                    stat = 3;
                else if (character >= 0xc0)
                    stat = 2;
            } else if (stat >= 2 && character > 0x7f)
                stat--;
            else
                stat = 0;
        } break;
            /* SHIFT_JIS_2004 Support. */
        case SHIFT_JIS_2004: {
            if (stat < 2 && character >= 0x81 && character <= 0x9f)
                stat = 2;
            else if (stat < 2 && character >= 0xe0 && character <= 0xef)
                stat = 2;
            else if (stat < 2 && character >= 0xf0 && character <= 0xfc)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;
            /* Shift-JIS Support. */
        case SJIS: {
            if (stat < 2 && character > 0x80
                && !(character > 0x9f && character < 0xe0))
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;
            /* Chinese Big5 Support. */
        case BIG5: {
            if (stat < 2 && character > 0xA0)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;
            /* Chinese GBK Support. */
        case GBK: {
            if (stat < 2 && character > 0x7F)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;

            /* Korian UHC Support. */
        case UHC: {
            if (stat < 2 && character > 0x7F)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;

        case EUC_JIS_2004:
            /* 0x8f is JIS X 0212 + JIS X 0213(2) 3 byte */
            /* 0x8e is JIS X 0201 2 byte */
            /* 0xa0-0xff is JIS X 0213(1) 2 byte */
        case EUC_JP:
            /* 0x8f is JIS X 0212 3 byte */
            /* 0x8e is JIS X 0201 2 byte */
            /* 0xa0-0xff is JIS X 0208 2 byte */
            {
                if (stat < 3 && character == 0x8f) /* JIS X 0212 */
                    stat = 3;
                else if (stat != 2
                         && (character == 0x8e
                             || character > 0xa0)) /* Half Katakana HighByte &
                                                      Kanji HighByte */
                    stat = 2;
                else if (stat == 2)
                    stat = 1;
                else
                    stat = 0;
            }
            break;

            /* EUC_CN, EUC_KR, JOHAB Support */
        case EUC_CN:
        case EUC_KR:
        case JOHAB: {
            if (stat < 2 && character > 0xa0)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;
        case EUC_TW: {
            if (stat < 4 && character == 0x8e)
                stat = 4;
            else if (stat == 4 && character > 0xa0)
                stat = 3;
            else if ((stat == 3 || stat < 2) && character > 0xa0)
                stat = 2;
            else if (stat == 2)
                stat = 1;
            else
                stat = 0;
        } break;
            /*Chinese GB18030 support.Added by Bill Huang <bhuang@redhat.com>
             * <bill_huanghb@ybb.ne.jp>*/
        case GB18030: {
            if (stat < 2 && character > 0x80)
                stat = 2;
            else if (stat == 2) {
                if (character >= 0x30 && character <= 0x39)
                    stat = 3;
                else
                    stat = 1;
            } else if (stat == 3) {
                if (character >= 0x30 && character <= 0x39)
                    stat = 1;
                else
                    stat = 3;
            } else
                stat = 0;
        } break;
        default: {
            stat = 0;
        } break;
    }
    return stat;
}

/*
 *	This function is used to know the encoding corresponding to
 *	the current locale.
 */
const char *derive_locale_encoding(const char *dbencoding) {
    UNUSED(dbencoding);
    const char *wenc = NULL;
#ifdef WIN32
    int acp;
#endif /* WIN32 */

    if (wenc = getenv("ESCLIENTENCODING"),
        NULL != wenc) /* environmnt variable */
        return wenc;
#ifdef WIN32
    acp = GetACP();
    if (acp >= 1251 && acp <= 1258) {
        if (stricmp(dbencoding, "SQL_ASCII") == 0)
            return wenc;
    }
    switch (acp) {
        case 932:
            wenc = "SJIS";
            break;
        case 936:
            wenc = "GBK";
            break;
        case 949:
            wenc = "UHC";
            break;
        case 950:
            wenc = "BIG5";
            break;
        case 1250:
            wenc = "WIN1250";
            break;
        case 1251:
            wenc = "WIN1251";
            break;
        case 1256:
            wenc = "WIN1256";
            break;
        case 1252:
            if (strnicmp(dbencoding, "LATIN", 5) == 0)
                break;
            wenc = "WIN1252";
            break;
        case 1258:
            wenc = "WIN1258";
            break;
        case 1253:
            wenc = "WIN1253";
            break;
        case 1254:
            wenc = "WIN1254";
            break;
        case 1255:
            wenc = "WIN1255";
            break;
        case 1257:
            wenc = "WIN1257";
            break;
    }
#else
        // TODO #34 - Investigate locale handling on Mac 
#endif /* WIN32 */
    return wenc;
}

void encoded_str_constr(encoded_str *encstr, int ccsc, const char *str) {
    encstr->ccsc = ccsc;
    encstr->encstr = (const UCHAR *)str;
    encstr->pos = -1;
    encstr->ccst = 0;
}
int encoded_nextchar(encoded_str *encstr) {
    int chr;

    if (encstr->pos >= 0 && !encstr->encstr[encstr->pos])
        return 0;
    chr = encstr->encstr[++encstr->pos];
    encstr->ccst = es_CS_stat(encstr->ccst, (unsigned int)chr, encstr->ccsc);
    return chr;
}

int encoded_byte_check(encoded_str *encstr, size_t abspos) {
    int chr;

    chr = encstr->encstr[encstr->pos = abspos];
    encstr->ccst = es_CS_stat(encstr->ccst, (unsigned int)chr, encstr->ccsc);
    return chr;
}
