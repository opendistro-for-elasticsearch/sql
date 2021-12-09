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
