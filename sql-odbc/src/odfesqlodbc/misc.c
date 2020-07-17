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
#include "es_odbc.h"
#include "misc.h"

#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
// clang-format on

#ifndef WIN32
#include <pwd.h>
#include <sys/types.h>
#include <unistd.h>
#else
#include <process.h> /* Byron: is this where Windows keeps def.
								 * of getpid ? */
#endif

/*
 *	returns STRCPY_FAIL, STRCPY_TRUNCATED, or #bytes copied
 *	(not including null term)
 */
ssize_t my_strcpy(char *dst, ssize_t dst_len, const char *src,
                  ssize_t src_len) {
    if (dst_len <= 0)
        return STRCPY_FAIL;

    if (src_len == SQL_NULL_DATA) {
        dst[0] = '\0';
        return STRCPY_NULL;
    } else if (src_len == SQL_NTS)
        src_len = strlen(src);

    if (src_len <= 0)
        return STRCPY_FAIL;
    else {
        if (src_len < dst_len) {
            memcpy(dst, src, src_len);
            dst[src_len] = '\0';
        } else {
            memcpy(dst, src, dst_len - 1);
            dst[dst_len - 1] = '\0'; /* truncated */
            return STRCPY_TRUNCATED;
        }
    }

    return strlen(dst);
}

/*
 * strncpy copies up to len characters, and doesn't terminate
 * the destination string if src has len characters or more.
 * instead, I want it to copy up to len-1 characters and always
 * terminate the destination string.
 */
size_t strncpy_null(char *dst, const char *src, ssize_t len) {
    int i;

    if (NULL != dst && len > 0) {
        for (i = 0; src[i] && i < len - 1; i++)
            dst[i] = src[i];

        dst[i] = '\0';
    } else
        return 0;
    if (src[i])
        return strlen(src);
    return i;
}

/*------
 *	Create a null terminated string (handling the SQL_NTS thing):
 *		1. If buf is supplied, place the string in there
 *		   (assumes enough space) and return buf.
 *		2. If buf is not supplied, malloc space and return this string
 *------
 */
char *make_string(const SQLCHAR *s, SQLINTEGER len, char *buf, size_t bufsize) {
    size_t length;
    char *str;

    if (!s || SQL_NULL_DATA == len)
        return NULL;
    if (len >= 0)
        length = len;
    else if (SQL_NTS == len)
        length = strlen((char *)s);
    else {
        MYLOG(ES_DEBUG, "invalid length=" FORMAT_INTEGER "\n", len);
        return NULL;
    }
    if (buf) {
        strncpy_null(buf, (char *)s, bufsize > length ? length + 1 : bufsize);
        return buf;
    }

    MYLOG(ES_DEBUG, "malloc size=" FORMAT_SIZE_T "\n", length);
    str = malloc(length + 1);
    MYLOG(ES_DEBUG, "str=%p\n", str);
    if (!str)
        return NULL;

    strncpy_null(str, (char *)s, length + 1);
    return str;
}

/*
 * snprintfcat is a extension to snprintf
 * It add format to buf at given pos
 */
#ifdef POSIX_SNPRINTF_REQUIRED
static posix_vsnprintf(char *str, size_t size, const char *format, va_list ap);
#define vsnprintf posix_vsnprintf
#endif /* POSIX_SNPRINTF_REQUIRED */

int snprintfcat(char *buf, size_t size, const char *format, ...) {
    int len;
    size_t pos = strlen(buf);
    va_list arglist;

    va_start(arglist, format);
    len = vsnprintf(buf + pos, size - pos, format, arglist);
    va_end(arglist);
    return len + (int)pos;
}

/*
 * Windows doesn't have snprintf(). It has _snprintf() which is similar,
 * but it behaves differently wrt. truncation. This is a compatibility
 * function that uses _snprintf() to provide POSIX snprintf() behavior.
 *
 * Our strategy, if the output doesn't fit, is to create a temporary buffer
 * and call _snprintf() on that. If it still doesn't fit, enlarge the buffer
 * and repeat.
 */
#ifdef POSIX_SNPRINTF_REQUIRED
static int posix_vsnprintf(char *str, size_t size, const char *format,
                           va_list ap) {
    int len;
    char *tmp;
    size_t newsize;

    len = _vsnprintf(str, size, format, ap);
    if (len < 0) {
        if (size == 0)
            newsize = 100;
        else
            newsize = size;
        do {
            newsize *= 2;
            tmp = malloc(newsize);
            if (!tmp)
                return -1;
            len = _vsnprintf(tmp, newsize, format, ap);
            if (len >= 0)
                memcpy(str, tmp, size);
            free(tmp);
        } while (len < 0);
    }
    if (len >= size && size > 0) {
        /* Ensure the buffer is NULL-terminated */
        str[size - 1] = '\0';
    }
    return len;
}

int posix_snprintf(char *buf, size_t size, const char *format, ...) {
    int len;
    va_list arglist;

    va_start(arglist, format);
    len = posix_vsnprintf(buf, size, format, arglist);
    va_end(arglist);
    return len;
}
#endif /* POSIX_SNPRINTF_REQUIRED */

#ifndef HAVE_STRLCAT
size_t strlcat(char *dst, const char *src, size_t size) {
    size_t ttllen;
    char *pd = dst;
    const char *ps = src;

    for (ttllen = 0; ttllen < size; ttllen++, pd++) {
        if (0 == *pd)
            break;
    }
    if (ttllen >= size - 1)
        return ttllen + strlen(src);
    for (; ttllen < size - 1; ttllen++, pd++, ps++) {
        if (0 == (*pd = *ps))
            return ttllen;
    }
    *pd = 0;
    for (; *ps; ttllen++, ps++)
        ;
    return ttllen;
}
#endif /* HAVE_STRLCAT */
