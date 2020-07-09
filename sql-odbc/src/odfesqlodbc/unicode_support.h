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

#ifndef __UNICODE_SUPPORT_H__
#define __UNICODE_SUPPORT_H__

#include "es_odbc.h"

#ifdef UNICODE_SUPPORT
#define WCLEN sizeof(SQLWCHAR)
enum { CONVTYPE_UNKNOWN, WCSTYPE_UTF16_LE, WCSTYPE_UTF32_LE, C16TYPE_UTF16_LE };
char *ucs2_to_utf8(const SQLWCHAR *ucs2str, SQLLEN ilen, SQLLEN *olen,
                   BOOL tolower);
SQLULEN utf8_to_ucs2_lf(const char *utf8str, SQLLEN ilen, BOOL lfconv,
                        SQLWCHAR *ucs2str, SQLULEN buflen, BOOL errcheck);
int get_convtype(void);
#define utf8_to_ucs2(utf8str, ilen, ucs2str, buflen) \
    utf8_to_ucs2_lf(utf8str, ilen, FALSE, ucs2str, buflen, FALSE)

SQLLEN bindcol_hybrid_estimate(const char *ldt, BOOL lf_conv, char **wcsbuf);
SQLLEN bindcol_hybrid_exec(SQLWCHAR *utf16, const char *ldt, size_t n,
                           BOOL lf_conv, char **wcsbuf);
SQLLEN bindcol_localize_estimate(const char *utf8dt, BOOL lf_conv,
                                 char **wcsbuf);
SQLLEN bindcol_localize_exec(char *ldt, size_t n, BOOL lf_conv, char **wcsbuf);
SQLLEN bindpara_msg_to_utf8(const char *ldt, char **wcsbuf, SQLLEN used);
SQLLEN bindpara_wchar_to_msg(const SQLWCHAR *utf16, char **wcsbuf, SQLLEN used);

SQLLEN locale_to_sqlwchar(SQLWCHAR *utf16, const char *ldt, size_t n,
                          BOOL lf_conv);
#endif /* UNICODE_SUPPORT */

#endif /* __UNICODE_SUPPORT_H__ */
