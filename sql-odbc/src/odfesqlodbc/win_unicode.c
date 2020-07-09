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

#ifdef UNICODE_SUPPORT

#include <ctype.h>
#include <stdlib.h>
#include <string.h>
#include "unicode_support.h"

#ifdef WIN32
#define FORMAT_SIZE_T "%Iu"
#else
#define FORMAT_SIZE_T "%zu"
#endif

#if (defined(__STDC_ISO_10646__) && defined(HAVE_MBSTOWCS) \
     && defined(HAVE_WCSTOMBS))                            \
    || defined(WIN32)
#define __WCS_ISO10646__
static BOOL use_wcs = FALSE;
#endif

#if (defined(__STDC_UTF_16__) && defined(HAVE_UCHAR_H) \
     && defined(HAVE_MBRTOC16) && defined(HAVE_C16RTOMB))
#define __CHAR16_UTF_16__
#include <uchar.h>
static BOOL use_c16 = FALSE;
#endif

static int convtype = -1;

int get_convtype(void) {
    const UCHAR *cdt;
    (void)(cdt);
#if defined(__WCS_ISO10646__)
    if (convtype < 0) {
        wchar_t *wdt = L"a";
        int sizeof_w = sizeof(wchar_t);

        cdt = (UCHAR *)wdt;
        switch (sizeof_w) {
            case 2:
                if ('a' == cdt[0] && '\0' == cdt[1] && '\0' == cdt[2]
                    && '\0' == cdt[3]) {
                    MYLOG(ES_DEBUG, " UTF-16LE detected\n");
                    convtype = WCSTYPE_UTF16_LE;
                    use_wcs = TRUE;
                }
                break;
            case 4:
                if ('a' == cdt[0] && '\0' == cdt[1] && '\0' == cdt[2]
                    && '\0' == cdt[3] && '\0' == cdt[4] && '\0' == cdt[5]
                    && '\0' == cdt[6] && '\0' == cdt[7]) {
                    MYLOG(ES_DEBUG, " UTF32-LE detected\n");
                    convtype = WCSTYPE_UTF32_LE;
                    use_wcs = TRUE;
                }
                break;
        }
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (convtype < 0) {
        char16_t *c16dt = u"a";

        cdt = (UCHAR *)c16dt;
        if ('a' == cdt[0] && '\0' == cdt[1] && '\0' == cdt[2]
            && '\0' == cdt[3]) {
            MYLOG(ES_DEBUG, " C16_UTF-16LE detected\n");
            convtype = C16TYPE_UTF16_LE;
            use_c16 = TRUE;
        }
    }
#endif /* __CHAR16_UTF_16__ */
    if (convtype < 0)
        convtype = CONVTYPE_UNKNOWN; /* unknown */
    return convtype;
}

#define byte3check 0xfffff800
#define byte2_base 0x80c0
#define byte2_mask1 0x07c0
#define byte2_mask2 0x003f
#define byte3_base 0x8080e0
#define byte3_mask1 0xf000
#define byte3_mask2 0x0fc0
#define byte3_mask3 0x003f

#define surrog_check 0xfc00
#define surrog1_bits 0xd800
#define surrog2_bits 0xdc00
#define byte4_base 0x808080f0
#define byte4_sr1_mask1 0x0700
#define byte4_sr1_mask2 0x00fc
#define byte4_sr1_mask3 0x0003
#define byte4_sr2_mask1 0x03c0
#define byte4_sr2_mask2 0x003f
#define surrogate_adjust (0x10000 >> 10)

static int little_endian = -1;

SQLULEN ucs2strlen(const SQLWCHAR *ucs2str) {
    SQLULEN len;
    for (len = 0; ucs2str[len]; len++)
        ;
    return len;
}
char *ucs2_to_utf8(const SQLWCHAR *ucs2str, SQLLEN ilen, SQLLEN *olen,
                   BOOL lower_identifier) {
    char *utf8str;
    int len = 0;
    MYLOG(ES_DEBUG, "%p ilen=" FORMAT_LEN " ", ucs2str, ilen);

    if (!ucs2str) {
        if (olen)
            *olen = SQL_NULL_DATA;
        return NULL;
    }
    if (little_endian < 0) {
        int crt = 1;
        little_endian = (0 != ((char *)&crt)[0]);
    }
    if (ilen < 0)
        ilen = ucs2strlen(ucs2str);
    MYPRINTF(0, " newlen=" FORMAT_LEN, ilen);
    utf8str = (char *)malloc(ilen * 4 + 1);
    if (utf8str) {
        int i = 0;
        UInt2 byte2code;
        Int4 byte4code, surrd1, surrd2;
        const SQLWCHAR *wstr;

        for (i = 0, wstr = ucs2str; i < ilen; i++, wstr++) {
            if (!*wstr)
                break;
            else if (0 == (*wstr & 0xffffff80)) /* ASCII */
            {
                if (lower_identifier)
                    utf8str[len++] = (char)tolower(*wstr);
                else
                    utf8str[len++] = (char)*wstr;
            } else if ((*wstr & byte3check) == 0) {
                byte2code = byte2_base | ((byte2_mask1 & *wstr) >> 6)
                            | ((byte2_mask2 & *wstr) << 8);
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte2code,
                           sizeof(byte2code));
                else {
                    utf8str[len] = ((char *)&byte2code)[1];
                    utf8str[len + 1] = ((char *)&byte2code)[0];
                }
                len += sizeof(byte2code);
            }
            /* surrogate pair check for non ucs-2 code */
            else if (surrog1_bits == (*wstr & surrog_check)) {
                surrd1 = (*wstr & ~surrog_check) + surrogate_adjust;
                wstr++;
                i++;
                surrd2 = (*wstr & ~surrog_check);
                byte4code = byte4_base | ((byte4_sr1_mask1 & surrd1) >> 8)
                            | ((byte4_sr1_mask2 & surrd1) << 6)
                            | ((byte4_sr1_mask3 & surrd1) << 20)
                            | ((byte4_sr2_mask1 & surrd2) << 10)
                            | ((byte4_sr2_mask2 & surrd2) << 24);
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte4code,
                           sizeof(byte4code));
                else {
                    utf8str[len] = ((char *)&byte4code)[3];
                    utf8str[len + 1] = ((char *)&byte4code)[2];
                    utf8str[len + 2] = ((char *)&byte4code)[1];
                    utf8str[len + 3] = ((char *)&byte4code)[0];
                }
                len += sizeof(byte4code);
            } else {
                byte4code = byte3_base | ((byte3_mask1 & *wstr) >> 12)
                            | ((byte3_mask2 & *wstr) << 2)
                            | ((byte3_mask3 & *wstr) << 16);
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte4code, 3);
                else {
                    utf8str[len] = ((char *)&byte4code)[3];
                    utf8str[len + 1] = ((char *)&byte4code)[2];
                    utf8str[len + 2] = ((char *)&byte4code)[1];
                }
                len += 3;
            }
        }
        utf8str[len] = '\0';
        if (olen)
            *olen = len;
    }
    MYPRINTF(0, " olen=%d utf8str=%s\n", len, utf8str ? utf8str : "");
    return utf8str;
}

#define byte3_m1 0x0f
#define byte3_m2 0x3f
#define byte3_m3 0x3f
#define byte2_m1 0x1f
#define byte2_m2 0x3f
#define byte4_m1 0x07
#define byte4_m2 0x3f
#define byte4_m31 0x30
#define byte4_m32 0x0f
#define byte4_m4 0x3f

/*
 * Convert a string from UTF-8 encoding to UCS-2.
 *
 * utf8str		- input string in UTF-8
 * ilen			- length of input string in bytes  (or minus)
 * lfconv		- TRUE if line feeds (LF) should be converted to CR + LF
 * ucs2str		- output buffer
 * bufcount		- size of output buffer
 * errcheck		- if TRUE, check for invalidly encoded input characters
 *
 * Returns the number of SQLWCHARs copied to output buffer. If the output
 * buffer is too small, the output is truncated. The output string is
 * NULL-terminated, except when the output is truncated.
 */
SQLULEN
utf8_to_ucs2_lf(const char *utf8str, SQLLEN ilen, BOOL lfconv,
                SQLWCHAR *ucs2str, SQLULEN bufcount, BOOL errcheck) {
    int i;
    SQLULEN rtn, ocount, wcode;
    const UCHAR *str;

    MYLOG(ES_DEBUG, "ilen=" FORMAT_LEN " bufcount=" FORMAT_ULEN, ilen,
          bufcount);
    if (!utf8str)
        return 0;
    MYPRINTF(ES_DEBUG, " string=%s", utf8str);

    if (!bufcount)
        ucs2str = NULL;
    else if (!ucs2str)
        bufcount = 0;
    if (ilen < 0)
        ilen = strlen(utf8str);
    for (i = 0, ocount = 0, str = (SQLCHAR *)utf8str; i < ilen && *str;) {
        if ((*str & 0x80) == 0) {
            if (lfconv && ES_LINEFEED == *str
                && (i == 0 || ES_CARRIAGE_RETURN != str[-1])) {
                if (ocount < bufcount)
                    ucs2str[ocount] = ES_CARRIAGE_RETURN;
                ocount++;
            }
            if (ocount < bufcount)
                ucs2str[ocount] = *str;
            ocount++;
            i++;
            str++;
        } else if (0xf8 == (*str & 0xf8)) /* more than 5 byte code */
        {
            ocount = (SQLULEN)-1;
            goto cleanup;
        } else if (0xf0 == (*str & 0xf8)) /* 4 byte code */
        {
            if (errcheck) {
                if (i + 4 > ilen || 0 == (str[1] & 0x80) || 0 == (str[2] & 0x80)
                    || 0 == (str[3] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = (surrog1_bits | ((((UInt4)*str) & byte4_m1) << 8)
                         | ((((UInt4)str[1]) & byte4_m2) << 2)
                         | ((((UInt4)str[2]) & byte4_m31) >> 4))
                        - surrogate_adjust;
                ucs2str[ocount] = (SQLWCHAR)wcode;
            }
            ocount++;
            if (ocount < bufcount) {
                wcode = surrog2_bits | ((((UInt4)str[2]) & byte4_m32) << 6)
                        | (((UInt4)str[3]) & byte4_m4);
                ucs2str[ocount] = (SQLWCHAR)wcode;
            }
            ocount++;
            i += 4;
            str += 4;
        } else if (0xe0 == (*str & 0xf0)) /* 3 byte code */
        {
            if (errcheck) {
                if (i + 3 > ilen || 0 == (str[1] & 0x80)
                    || 0 == (str[2] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = ((((UInt4)*str) & byte3_m1) << 12)
                        | ((((UInt4)str[1]) & byte3_m2) << 6)
                        | (((UInt4)str[2]) & byte3_m3);
                ucs2str[ocount] = (SQLWCHAR)wcode;
            }
            ocount++;
            i += 3;
            str += 3;
        } else if (0xc0 == (*str & 0xe0)) /* 2 byte code */
        {
            if (errcheck) {
                if (i + 2 > ilen || 0 == (str[1] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = ((((UInt4)*str) & byte2_m1) << 6)
                        | (((UInt4)str[1]) & byte2_m2);
                ucs2str[ocount] = (SQLWCHAR)wcode;
            }
            ocount++;
            i += 2;
            str += 2;
        } else {
            ocount = (SQLULEN)-1;
            goto cleanup;
        }
    }
cleanup:
    rtn = ocount;
    if (ocount == (SQLULEN)-1) {
        if (!errcheck)
            rtn = 0;
        ocount = 0;
    }
    if (ocount < bufcount && ucs2str)
        ucs2str[ocount] = 0;
    MYPRINTF(ES_ALL, " ocount=" FORMAT_ULEN "\n", ocount);
    return rtn;
}

#ifdef __WCS_ISO10646__

/* UCS4 => utf8 */
#define byte4check 0xffff0000
#define byte4_check 0x10000
#define byte4_mask1 0x1c0000
#define byte4_mask2 0x3f000
#define byte4_mask3 0x0fc0
#define byte4_mask4 0x003f

#define byte4_m3 0x3f

static SQLULEN ucs4strlen(const UInt4 *ucs4str) {
    SQLULEN len;
    for (len = 0; ucs4str[len]; len++)
        ;
    return len;
}

static char *ucs4_to_utf8(const UInt4 *ucs4str, SQLLEN ilen, SQLLEN *olen,
                          BOOL lower_identifier) {
    char *utf8str;
    int len = 0;
    MYLOG(ES_DEBUG, " %p ilen=" FORMAT_LEN "\n", ucs4str, ilen);

    if (!ucs4str) {
        if (olen)
            *olen = SQL_NULL_DATA;
        return NULL;
    }
    if (little_endian < 0) {
        int crt = 1;
        little_endian = (0 != ((char *)&crt)[0]);
    }
    if (ilen < 0)
        ilen = ucs4strlen(ucs4str);
    MYLOG(ES_DEBUG, " newlen=" FORMAT_LEN "\n", ilen);
    utf8str = (char *)malloc(ilen * 4 + 1);
    if (utf8str) {
        int i;
        UInt2 byte2code;
        Int4 byte4code;
        const UInt4 *wstr;

        for (i = 0, wstr = ucs4str; i < ilen; i++, wstr++) {
            if (!*wstr)
                break;
            else if (0 == (*wstr & 0xffffff80)) /* ASCII */
            {
                if (lower_identifier)
                    utf8str[len++] = (char)tolower(*wstr);
                else
                    utf8str[len++] = (char)*wstr;
            } else if ((*wstr & byte3check) == 0) {
                byte2code = byte2_base | ((byte2_mask1 & *wstr) >> 6)
                            | ((byte2_mask2 & *wstr) << 8);
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte2code,
                           sizeof(byte2code));
                else {
                    utf8str[len] = ((char *)&byte2code)[1];
                    utf8str[len + 1] = ((char *)&byte2code)[0];
                }
                len += sizeof(byte2code);
            } else if ((*wstr & byte4check) == 0) {
                byte4code = byte3_base | ((byte3_mask1 & *wstr) >> 12)
                            | ((byte3_mask2 & *wstr) << 2)
                            | ((byte3_mask3 & *wstr) << 16);
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte4code, 3);
                else {
                    utf8str[len] = ((char *)&byte4code)[3];
                    utf8str[len + 1] = ((char *)&byte4code)[2];
                    utf8str[len + 2] = ((char *)&byte4code)[1];
                }
                len += 3;
            } else {
                byte4code = byte4_base | ((byte4_mask1 & *wstr) >> 18)
                            | ((byte4_mask2 & *wstr) >> 4)
                            | ((byte4_mask3 & *wstr) << 10)
                            | ((byte4_mask4 & *wstr) << 24);
                /* MYLOG(ES_DEBUG, " %08x->%08x\n", *wstr, byte4code); */
                if (little_endian)
                    memcpy(utf8str + len, (char *)&byte4code,
                           sizeof(byte4code));
                else {
                    utf8str[len] = ((char *)&byte4code)[3];
                    utf8str[len + 1] = ((char *)&byte4code)[2];
                    utf8str[len + 2] = ((char *)&byte4code)[1];
                    utf8str[len + 3] = ((char *)&byte4code)[0];
                }
                len += sizeof(byte4code);
            }
        }
        utf8str[len] = '\0';
        if (olen)
            *olen = len;
    }
    MYLOG(ES_DEBUG, " olen=%d %s\n", len, utf8str ? utf8str : "");
    return utf8str;
}

/*
 * Convert a string from UTF-8 encoding to UTF-32.
 *
 * utf8str		- input string in UTF-8
 * ilen			- length of input string in bytes  (or minus)
 * lfconv		- TRUE if line feeds (LF) should be converted to CR + LF
 * ucs4str		- output buffer
 * bufcount		- size of output buffer
 * errcheck		- if TRUE, check for invalidly encoded input characters
 *
 * Returns the number of UInt4s copied to output buffer. If the output
 * buffer is too small, the output is truncated. The output string is
 * NULL-terminated, except when the output is truncated.
 */
static SQLULEN utf8_to_ucs4_lf(const char *utf8str, SQLLEN ilen, BOOL lfconv,
                               UInt4 *ucs4str, SQLULEN bufcount,
                               BOOL errcheck) {
    int i;
    SQLULEN rtn, ocount, wcode;
    const UCHAR *str;

    MYLOG(ES_DEBUG, " ilen=" FORMAT_LEN " bufcount=" FORMAT_ULEN "\n", ilen, bufcount);
    if (!utf8str)
        return 0;
    MYLOG(99, " string=%s\n", utf8str);

    if (!bufcount)
        ucs4str = NULL;
    else if (!ucs4str)
        bufcount = 0;
    if (ilen < 0)
        ilen = strlen(utf8str);
    for (i = 0, ocount = 0, str = (SQLCHAR *)utf8str; i < ilen && *str;) {
        if ((*str & 0x80) == 0) {
            if (lfconv && ES_LINEFEED == *str
                && (i == 0 || ES_CARRIAGE_RETURN != str[-1])) {
                if (ocount < bufcount)
                    ucs4str[ocount] = ES_CARRIAGE_RETURN;
                ocount++;
            }
            if (ocount < bufcount)
                ucs4str[ocount] = *str;
            ocount++;
            i++;
            str++;
        } else if (0xf8 == (*str & 0xf8)) /* more than 5 byte code */
        {
            ocount = (SQLULEN)-1;
            goto cleanup;
        } else if (0xf0 == (*str & 0xf8)) /* 4 byte code */
        {
            if (errcheck) {
                if (i + 4 > ilen || 0 == (str[1] & 0x80) || 0 == (str[2] & 0x80)
                    || 0 == (str[3] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = (((((UInt4)*str) & byte4_m1) << 18)
                         | ((((UInt4)str[1]) & byte4_m2) << 12)
                         | ((((UInt4)str[2]) & byte4_m3) << 6))
                        | (((UInt4)str[3]) & byte4_m4);
                ucs4str[ocount] = (unsigned int)wcode;
            }
            ocount++;
            i += 4;
            str += 4;
        } else if (0xe0 == (*str & 0xf0)) /* 3 byte code */
        {
            if (errcheck) {
                if (i + 3 > ilen || 0 == (str[1] & 0x80)
                    || 0 == (str[2] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = ((((UInt4)*str) & byte3_m1) << 12)
                        | ((((UInt4)str[1]) & byte3_m2) << 6)
                        | (((UInt4)str[2]) & byte3_m3);
                ucs4str[ocount] = (unsigned int)wcode;
            }
            ocount++;
            i += 3;
            str += 3;
        } else if (0xc0 == (*str & 0xe0)) /* 2 byte code */
        {
            if (errcheck) {
                if (i + 2 > ilen || 0 == (str[1] & 0x80)) {
                    ocount = (SQLULEN)-1;
                    goto cleanup;
                }
            }
            if (ocount < bufcount) {
                wcode = ((((UInt4)*str) & byte2_m1) << 6)
                        | (((UInt4)str[1]) & byte2_m2);
                ucs4str[ocount] = (SQLWCHAR)wcode;
            }
            ocount++;
            i += 2;
            str += 2;
        } else {
            ocount = (SQLULEN)-1;
            goto cleanup;
        }
    }
cleanup:
    rtn = ocount;
    if (ocount == (SQLULEN)-1) {
        if (!errcheck)
            rtn = 0;
        ocount = 0;
    }
    if (ocount < bufcount && ucs4str)
        ucs4str[ocount] = 0;
    MYLOG(ES_DEBUG, " ocount=" FORMAT_ULEN "\n", ocount);
    return rtn;
}

#define SURROGATE_CHECK 0xfc
#define SURROG1_BYTE 0xd8
#define SURROG2_BYTE 0xdc

static int ucs4_to_ucs2_lf(const unsigned int *ucs4str, SQLLEN ilen,
                           SQLWCHAR *ucs2str, int bufcount, BOOL lfconv) {
    int outlen = 0, i;
    UCHAR *ucdt;
    SQLWCHAR *sqlwdt, dmy_wchar;
    UCHAR *const udt = (UCHAR *)&dmy_wchar;
    unsigned int uintdt;

    MYLOG(ES_DEBUG, " ilen=" FORMAT_LEN " bufcount=%d\n", ilen, bufcount);
    if (ilen < 0)
        ilen = ucs4strlen(ucs4str);
    for (i = 0; i < ilen && (uintdt = ucs4str[i], uintdt); i++) {
        sqlwdt = (SQLWCHAR *)&uintdt;
        ucdt = (UCHAR *)&uintdt;
        if (0 == sqlwdt[1]) {
            if (lfconv && ES_LINEFEED == ucdt[0]
                && (i == 0
                    || ES_CARRIAGE_RETURN != *((UCHAR *)&ucs4str[i - 1]))) {
                if (outlen < bufcount) {
                    udt[0] = ES_CARRIAGE_RETURN;
                    udt[1] = 0;
                    ucs2str[outlen] = *((SQLWCHAR *)udt);
                }
                outlen++;
            }
            if (outlen < bufcount)
                ucs2str[outlen] = sqlwdt[0];
            outlen++;
            continue;
        }
        sqlwdt[1]--;
        udt[0] = ((0xfc & ucdt[1]) >> 2) | ((0x3 & ucdt[2]) << 6);
        //	printf("%02x", udt[0]);
        udt[1] = SURROG1_BYTE | ((0xc & ucdt[2]) >> 2);
        //	printf("%02x", udt[1]);
        if (outlen < bufcount)
            ucs2str[outlen] = *((SQLWCHAR *)udt);
        outlen++;
        udt[0] = ucdt[0];
        //	printf("%02x", udt[0]);
        udt[1] = SURROG2_BYTE | (0x3 & ucdt[1]);
        //	printf("%02x\n", udt[1]);
        if (outlen < bufcount)
            ucs2str[outlen] = *((SQLWCHAR *)udt);
        outlen++;
    }
    if (outlen < bufcount)
        ucs2str[outlen] = 0;

    return outlen;
}
static int ucs2_to_ucs4(const SQLWCHAR *ucs2str, SQLLEN ilen,
                        unsigned int *ucs4str, int bufcount) {
    int outlen = 0, i;
    UCHAR *ucdt;
    SQLWCHAR sqlwdt;
    unsigned int dmy_uint;
    UCHAR *const udt = (UCHAR *)&dmy_uint;

    MYLOG(ES_DEBUG, " ilen=" FORMAT_LEN " bufcount=%d\n", ilen, bufcount);
    if (ilen < 0)
        ilen = ucs2strlen(ucs2str);
    udt[3] = 0; /* always */
    for (i = 0; i < ilen && (sqlwdt = ucs2str[i], sqlwdt); i++) {
        ucdt = (UCHAR *)(ucs2str + i);
        //	printf("IN=%x\n", sqlwdt);
        if ((ucdt[1] & SURROGATE_CHECK) != SURROG1_BYTE) {
            //	printf("SURROG1=%2x\n", ucdt[1] & SURROG1_BYTE);
            if (outlen < bufcount) {
                udt[0] = ucdt[0];
                udt[1] = ucdt[1];
                udt[2] = 0;
                ucs4str[outlen] = *((unsigned int *)udt);
            }
            outlen++;
            continue;
        }
        /* surrogate pair */
        udt[0] = ucdt[2];
        udt[1] = (ucdt[3] & 0x3) | ((ucdt[0] & 0x3f) << 2);
        udt[2] = (((ucdt[0] & 0xc0) >> 6) | ((ucdt[1] & 0x3) << 2)) + 1;
        // udt[3] = 0; needless
        if (outlen < bufcount)
            ucs4str[outlen] = *((unsigned int *)udt);
        outlen++;
        i++;
    }
    if (outlen < bufcount)
        ucs4str[outlen] = 0;

    return outlen;
}
#endif /* __WCS_ISO10646__ */

#if defined(__WCS_ISO10646__)

static SQLULEN utf8_to_wcs_lf(const char *utf8str, SQLLEN ilen, BOOL lfconv,
                              wchar_t *wcsstr, SQLULEN bufcount,
                              BOOL errcheck) {
    switch (get_convtype()) {
        case WCSTYPE_UTF16_LE:
            return utf8_to_ucs2_lf(utf8str, ilen, lfconv, (SQLWCHAR *)wcsstr,
                                   bufcount, errcheck);
        case WCSTYPE_UTF32_LE:
            return utf8_to_ucs4_lf(utf8str, ilen, lfconv, (UInt4 *)wcsstr,
                                   bufcount, errcheck);
    }
    return (SQLULEN)~0;
}

static char *wcs_to_utf8(const wchar_t *wcsstr, SQLLEN ilen, SQLLEN *olen,
                         BOOL lower_identifier) {
    switch (get_convtype()) {
        case WCSTYPE_UTF16_LE:
            return ucs2_to_utf8((const SQLWCHAR *)wcsstr, ilen, olen,
                                lower_identifier);
        case WCSTYPE_UTF32_LE:
            return ucs4_to_utf8((const UInt4 *)wcsstr, ilen, olen,
                                lower_identifier);
    }

    return NULL;
}

/*
 *	Input strings must be NULL terminated.
 *	Output wide character strings would be NULL terminated. The result
 *	outmsg would be truncated when the buflen is small.
 *
 *	The output NULL terminator is counted as buflen.
 *	if outmsg is NULL or buflen is 0, only output length is returned.
 *	As for return values, NULL terminators aren't counted.
 */
static int msgtowstr(const char *inmsg, wchar_t *outmsg, int buflen) {
    int outlen = -1;

    MYLOG(ES_DEBUG, " inmsg=%p buflen=%d\n", inmsg, buflen);
#ifdef WIN32
    if (NULL == outmsg)
        buflen = 0;
    if ((outlen =
             MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED | MB_ERR_INVALID_CHARS,
                                 inmsg, -1, outmsg, buflen))
        > 0)
        outlen--;
    else if (ERROR_INSUFFICIENT_BUFFER == GetLastError())
        outlen =
            MultiByteToWideChar(CP_ACP, MB_PRECOMPOSED | MB_ERR_INVALID_CHARS,
                                inmsg, -1, NULL, 0)
            - 1;
    else
        outlen = -1;
#else
    if (0 == buflen)
        outmsg = NULL;
    outlen = mbstowcs((wchar_t *)outmsg, inmsg, buflen);
#endif /* WIN32 */
    if (outmsg && outlen >= buflen) {
        outmsg[buflen - 1] = 0;
        MYLOG(ES_DEBUG, " out=%dchars truncated to %d\n", outlen, buflen - 1);
    }
    MYLOG(ES_DEBUG, " buf=%dchars out=%dchars\n", buflen, outlen);

    return outlen;
}

/*
 *	Input wide character strings must be NULL terminated.
 *	Output strings would be NULL terminated. The result outmsg would be
 *	truncated when the buflen is small.
 *
 *	The output NULL terminator is counted as buflen.
 *	if outmsg is NULL or buflen is 0, only output length is returned.
 *	As for return values, NULL terminators aren't counted.
 */
static int wstrtomsg(const wchar_t *wstr, char *outmsg, int buflen) {
    int outlen = -1;

    MYLOG(ES_DEBUG, " wstr=%p buflen=%d\n", wstr, buflen);
#ifdef WIN32
    if (NULL == outmsg)
        buflen = 0;
    if ((outlen = WideCharToMultiByte(CP_ACP, 0, wstr, -1, outmsg, buflen, NULL,
                                      NULL))
        > 0)
        outlen--;
    else if (ERROR_INSUFFICIENT_BUFFER == GetLastError())
        outlen =
            WideCharToMultiByte(CP_ACP, 0, wstr, -1, NULL, 0, NULL, NULL) - 1;
    else
        outlen = -1;
#else
    if (0 == buflen)
        outmsg = NULL;
    outlen = wcstombs(outmsg, wstr, buflen);
#endif /* WIN32 */
    if (outmsg && outlen >= buflen) {
        outmsg[buflen - 1] = 0;
        MYLOG(ES_DEBUG, " out=%dbytes truncated to %d\n", outlen, buflen - 1);
    }
    MYLOG(ES_DEBUG, " buf=%dbytes outlen=%dbytes\n", buflen, outlen);

    return outlen;
}
#endif /* __WCS_ISO10646__ */

#if defined(__CHAR16_UTF_16__)

static mbstate_t initial_state;

static SQLLEN mbstoc16_lf(char16_t *c16dt, const char *c8dt, size_t n,
                          BOOL lf_conv) {
    int i;
    size_t brtn;
    const char *cdt;
    mbstate_t mbst = initial_state;

    MYLOG(ES_DEBUG, " c16dt=%p size=" FORMAT_SIZE_T "\n", c16dt, n);
    for (i = 0, cdt = c8dt; i < n || (!c16dt); i++) {
        if (lf_conv && ES_LINEFEED == *cdt && i > 0
            && ES_CARRIAGE_RETURN != cdt[-1]) {
            if (c16dt)
                c16dt[i] = ES_CARRIAGE_RETURN;
            i++;
        }
        brtn = mbrtoc16(c16dt ? c16dt + i : NULL, cdt, 4, &mbst);
        if (0 == brtn)
            break;
        if (brtn == (size_t)-1 || brtn == (size_t)-2)
            return -1;
        if (brtn == (size_t)-3)
            continue;
        cdt += brtn;
    }
    if (c16dt && i >= n)
        c16dt[n - 1] = 0;

    return i;
}

static SQLLEN c16tombs(char *c8dt, const char16_t *c16dt, size_t n) {
    int i;
    SQLLEN result = 0;
    size_t brtn;
    char *cdt, c4byte[4];
    mbstate_t mbst = initial_state;

    MYLOG(ES_DEBUG, " c8dt=%p size=" FORMAT_SIZE_T "u\n", c8dt, n);
    if (!c8dt)
        n = 0;
    for (i = 0, cdt = c8dt; c16dt[i] && (result < n || (!cdt)); i++) {
        if (NULL != cdt && result + 4 < n)
            brtn = c16rtomb(cdt, c16dt[i], &mbst);
        else {
            brtn = c16rtomb(c4byte, c16dt[i], &mbst);
            if (brtn < 5) {
                SQLLEN result_n = result + brtn;

                if (result_n < n)
                    memcpy(cdt, c4byte, brtn);
                else {
                    if (cdt && n > 0) {
                        c8dt[result] = '\0'; /* truncate */
                        return result_n;
                    }
                }
            }
        }
        /*
        printf("c16dt=%04X brtn=%lu result=%ld cdt=%02X%02X%02X%02X\n",
            c16dt[i], brtn, result, (UCHAR) cdt[0], (UCHAR) cdt[1], (UCHAR)
        cdt[2], (UCHAR) cdt[3]);
        */
        if (brtn == (size_t)-1) {
            if (n > 0)
                c8dt[n - 1] = '\0';
            return -1;
        }
        if (cdt)
            cdt += brtn;
        result += brtn;
    }
    if (cdt)
        *cdt = '\0';

    return result;
}
#endif /* __CHAR16_UTF_16__ */

//
//	SQLBindParameter	SQL_C_CHAR to UTF-8 case
//		the current locale => UTF-8
//
SQLLEN bindpara_msg_to_utf8(const char *ldt, char **wcsbuf, SQLLEN used) {
    SQLLEN l = (-2);
    char *utf8 = NULL, *ldt_nts, *alloc_nts = NULL, ntsbuf[128];
    int count;

    if (SQL_NTS == used) {
        count = (int)strlen(ldt);
        ldt_nts = (char *)ldt;
    } else if (used < 0) {
        return -1;
    } else {
        count = (int)used;
        if (used < (SQLLEN)sizeof(ntsbuf))
            ldt_nts = ntsbuf;
        else {
            if (NULL == (alloc_nts = malloc(used + 1)))
                return l;
            ldt_nts = alloc_nts;
        }
        memcpy(ldt_nts, ldt, used);
        ldt_nts[used] = '\0';
    }

    get_convtype();
    MYLOG(ES_DEBUG, " \n");
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
        wchar_t *wcsdt = (wchar_t *)malloc((count + 1) * sizeof(wchar_t));

        if ((l = msgtowstr(ldt_nts, (wchar_t *)wcsdt, count + 1)) >= 0)
            utf8 = wcs_to_utf8(wcsdt, -1, &l, FALSE);
        free(wcsdt);
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16) {
        SQLWCHAR *utf16 = (SQLWCHAR *)malloc((count + 1) * sizeof(SQLWCHAR));

        if ((l = mbstoc16_lf((char16_t *)utf16, ldt_nts, count + 1, FALSE))
            >= 0)
            utf8 = ucs2_to_utf8(utf16, -1, &l, FALSE);
        free(utf16);
    }
#endif /* __CHAR16_UTF_16__ */
    if (l < 0 && NULL != utf8)
        free(utf8);
    else
        *wcsbuf = (char *)utf8;

    if (NULL != alloc_nts)
        free(alloc_nts);
    return l;
}

//
//	SQLBindParameter	hybrid case
//		SQLWCHAR(UTF-16) => the current locale
//
SQLLEN bindpara_wchar_to_msg(const SQLWCHAR *utf16, char **wcsbuf,
                             SQLLEN used) {
    SQLLEN l = (-2);
    char *ldt = NULL;
    SQLWCHAR *utf16_nts, *alloc_nts = NULL, ntsbuf[128];
    int count;

    if (SQL_NTS == used) {
        count = (int)ucs2strlen(utf16);
        utf16_nts = (SQLWCHAR *)utf16;
    } else if (used < 0)
        return -1;
    else {
        count = (int)(used / WCLEN);
        if (used + WCLEN <= sizeof(ntsbuf))
            utf16_nts = ntsbuf;
        else {
            if (NULL == (alloc_nts = (SQLWCHAR *)malloc(used + WCLEN)))
                return l;
            utf16_nts = alloc_nts;
        }
        memcpy(utf16_nts, utf16, used);
        utf16_nts[count] = 0;
    }

    get_convtype();
    MYLOG(ES_DEBUG, "\n");
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
#pragma warning(push)
#pragma warning(disable : 4127)
        if (sizeof(SQLWCHAR) == sizeof(wchar_t))
#pragma warning(pop)
        {
            ldt = (char *)malloc(2 * count + 1);
            l = wstrtomsg((wchar_t *)utf16_nts, ldt, 2 * count + 1);
        } else {
            unsigned int *utf32 =
                (unsigned int *)malloc((count + 1) * sizeof(unsigned int));

            l = ucs2_to_ucs4(utf16_nts, -1, utf32, count + 1);
            if ((l = wstrtomsg((wchar_t *)utf32, NULL, 0)) >= 0) {
                ldt = (char *)malloc(l + 1);
                l = wstrtomsg((wchar_t *)utf32, ldt, (int)l + 1);
            }
            free(utf32);
        }
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16) {
        ldt = (char *)malloc(4 * count + 1);
        l = c16tombs(ldt, (const char16_t *)utf16_nts, 4 * count + 1);
    }
#endif /* __CHAR16_UTF_16__ */
    if (l < 0 && NULL != ldt)
        free(ldt);
    else
        *wcsbuf = ldt;

    if (NULL != alloc_nts)
        free(alloc_nts);
    return l;
}

size_t convert_linefeeds(const char *s, char *dst, size_t max, BOOL convlf,
                         BOOL *changed);
//
//	SQLBindCol	hybrid case
//		the current locale => SQLWCHAR(UTF-16)
//
SQLLEN bindcol_hybrid_estimate(const char *ldt, BOOL lf_conv, char **wcsbuf) {
    UNUSED(ldt, wcsbuf);
    SQLLEN l = (-2);

    get_convtype();
    MYLOG(ES_DEBUG, " lf_conv=%d\n", lf_conv);
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
        unsigned int *utf32 = NULL;

#pragma warning(push)
#pragma warning(disable : 4127)
        if (sizeof(SQLWCHAR) == sizeof(wchar_t))
#pragma warning(pop)
        {
            l = msgtowstr(ldt, (wchar_t *)NULL, 0);
            if (l >= 0 && lf_conv) {
                BOOL changed;
                size_t len;

                len = convert_linefeeds(ldt, NULL, 0, TRUE, &changed);
                if (changed) {
                    l += (len - strlen(ldt));
                    *wcsbuf = (char *)malloc(len + 1);
                    convert_linefeeds(ldt, *wcsbuf, len + 1, TRUE, NULL);
                }
            }
        } else {
            int count = (int)strlen(ldt);

            utf32 = (unsigned int *)malloc((count + 1) * sizeof(unsigned int));
            if ((l = msgtowstr(ldt, (wchar_t *)utf32, count + 1)) >= 0) {
                l = ucs4_to_ucs2_lf(utf32, -1, NULL, 0, lf_conv);
                *wcsbuf = (char *)utf32;
            }
        }
        if (l < 0 && NULL != utf32)
            free(utf32);
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16)
        l = mbstoc16_lf((char16_t *)NULL, ldt, 0, lf_conv);
#endif /* __CHAR16_UTF_16__ */

    return l;
}

SQLLEN bindcol_hybrid_exec(SQLWCHAR *utf16, const char *ldt, size_t n,
                           BOOL lf_conv, char **wcsbuf) {
    UNUSED(ldt, utf16, wcsbuf);
    SQLLEN l = (-2);

    get_convtype();
    MYLOG(ES_DEBUG, " size=" FORMAT_SIZE_T " lf_conv=%d\n", n, lf_conv);
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
        unsigned int *utf32 = NULL;
        BOOL midbuf = (wcsbuf && *wcsbuf);

#pragma warning(push)
#pragma warning(disable : 4127)
        if (sizeof(SQLWCHAR) == sizeof(wchar_t))
#pragma warning(pop)
        {
            if (midbuf)
                l = msgtowstr(*wcsbuf, (wchar_t *)utf16, (int)n);
            else
                l = msgtowstr(ldt, (wchar_t *)utf16, (int)n);
        } else if (midbuf) {
            utf32 = (unsigned int *)*wcsbuf;
            l = ucs4_to_ucs2_lf(utf32, -1, utf16, (int)n, lf_conv);
        } else {
            int count = (int)strlen(ldt);

            utf32 = (unsigned int *)malloc((count + 1) * sizeof(unsigned int));
            if ((l = msgtowstr(ldt, (wchar_t *)utf32, count + 1)) >= 0) {
                l = ucs4_to_ucs2_lf(utf32, -1, utf16, (int)n, lf_conv);
            }
            free(utf32);
        }
        if (midbuf) {
            free(*wcsbuf);
            *wcsbuf = NULL;
        }
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16) {
        l = mbstoc16_lf((char16_t *)utf16, ldt, n, lf_conv);
    }
#endif /* __CHAR16_UTF_16__ */

    return l;
}

SQLLEN locale_to_sqlwchar(SQLWCHAR *utf16, const char *ldt, size_t n,
                          BOOL lf_conv) {
    return bindcol_hybrid_exec(utf16, ldt, n, lf_conv, NULL);
}

//
//	SQLBindCol	localize case
//		UTF-8 => the current locale
//
SQLLEN bindcol_localize_estimate(const char *utf8dt, BOOL lf_conv,
                                 char **wcsbuf) {
    UNUSED(utf8dt);
    SQLLEN l = (-2);
    char *convalc = NULL;

    get_convtype();
    MYLOG(ES_DEBUG, " lf_conv=%d\n", lf_conv);
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
        wchar_t *wcsalc = NULL;

        l = utf8_to_wcs_lf(utf8dt, -1, lf_conv, NULL, 0, FALSE);
        wcsalc = (wchar_t *)malloc(sizeof(wchar_t) * (l + 1));
        convalc = (char *)wcsalc;
        l = utf8_to_wcs_lf(utf8dt, -1, lf_conv, wcsalc, l + 1, FALSE);
        l = wstrtomsg(wcsalc, NULL, 0);
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16) {
        SQLWCHAR *wcsalc = NULL;

        l = utf8_to_ucs2_lf(utf8dt, -1, lf_conv, (SQLWCHAR *)NULL, 0, FALSE);
        wcsalc = (SQLWCHAR *)malloc(sizeof(SQLWCHAR) * (l + 1));
        convalc = (char *)wcsalc;
        l = utf8_to_ucs2_lf(utf8dt, -1, lf_conv, wcsalc, l + 1, FALSE);
        l = c16tombs(NULL, (char16_t *)wcsalc, 0);
    }
#endif /* __CHAR16_UTF_16__ */
    if (l < 0 && NULL != convalc)
        free(convalc);
    else if (NULL != convalc)
        *wcsbuf = (char *)convalc;

    MYLOG(ES_DEBUG, " return=" FORMAT_LEN "\n", l);
    return l;
}

SQLLEN bindcol_localize_exec(char *ldt, size_t n, BOOL lf_conv, char **wcsbuf) {
    UNUSED(ldt, lf_conv);
    SQLLEN l = (-2);

    get_convtype();
    MYLOG(ES_DEBUG, " size=" FORMAT_SIZE_T "\n", n);
#if defined(__WCS_ISO10646__)
    if (use_wcs) {
        wchar_t *wcsalc = (wchar_t *)*wcsbuf;

        l = wstrtomsg(wcsalc, ldt, (int)n);
    }
#endif /* __WCS_ISO10646__ */
#ifdef __CHAR16_UTF_16__
    if (use_c16) {
        char16_t *wcsalc = (char16_t *)*wcsbuf;

        l = c16tombs(ldt, (char16_t *)wcsalc, n);
    }
#endif /* __CHAR16_UTF_16__ */
    free(*wcsbuf);
    *wcsbuf = NULL;

    MYLOG(ES_DEBUG, " return=" FORMAT_LEN "\n", l);
    return l;
}

#endif /* UNICODE_SUPPORT */
