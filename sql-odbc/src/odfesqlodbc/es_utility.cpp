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

#include "es_utility.h"

#include <ctype.h>
#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <climits>

// Used in the event that we run out of memory. This way we have a way of
// settings the buffer to point at an empty char array (because the buffer
// itself isn't const, we can't set this to const without having to later cast
// it away)
static char oom_buffer[1] = "";
static char *oom_buffer_ptr = oom_buffer;

static void MarkESExpBufferBroken(ESExpBuffer str) {
    if (str->data != oom_buffer)
        free(str->data);
    str->data = oom_buffer_ptr;
    str->len = 0;
    str->maxlen = 0;
}

static bool EnlargeESExpBuffer(ESExpBuffer str, size_t needed) {
    if (ESExpBufferBroken(str))
        return 0;

    if (needed >= ((size_t)INT_MAX - str->len)) {
        MarkESExpBufferBroken(str);
        return false;
    }

    needed += str->len + 1;
    if (needed <= str->maxlen)
        return true;

    size_t newlen = (str->maxlen > 0) ? (2 * str->maxlen) : 64;
    while (needed > newlen)
        newlen = 2 * newlen;

    if (newlen > (size_t)INT_MAX)
        newlen = (size_t)INT_MAX;

    char *newdata = (char *)realloc(str->data, newlen);
    if (newdata != NULL) {
        str->data = newdata;
        str->maxlen = newlen;
        return true;
    }

    MarkESExpBufferBroken(str);
    return false;
}

static bool AppendESExpBufferVA(ESExpBuffer str, const char *fmt,
                                va_list args) {
    size_t needed = 32;
    if (str->maxlen > (str->len + 16)) {
        size_t avail = str->maxlen - str->len;

        int nprinted = vsnprintf(str->data + str->len, avail, fmt, args);
        if ((nprinted < 0) || (nprinted > (INT_MAX - 1))) {
            MarkESExpBufferBroken(str);
            return true;
        } else if ((size_t)nprinted < avail) {
            str->len += nprinted;
            return true;
        }
        needed = nprinted + 1;
    }
    return !EnlargeESExpBuffer(str, needed);
}

void InitESExpBuffer(ESExpBuffer str) {
    str->data = (char *)malloc(INITIAL_EXPBUFFER_SIZE);
    if (str->data == NULL) {
        str->data = oom_buffer_ptr;
        str->maxlen = 0;
    } else {
        str->maxlen = INITIAL_EXPBUFFER_SIZE;
        str->data[0] = '\0';
    }
    str->len = 0;
}

void AppendESExpBuffer(ESExpBuffer str, const char *fmt, ...) {
    if (ESExpBufferBroken(str))
        return;

    va_list args;
    bool done = false;
    int save_errno = errno;
    do {
        errno = save_errno;
        va_start(args, fmt);
        done = AppendESExpBufferVA(str, fmt, args);
        va_end(args);
    } while (!done);
}

void TermESExpBuffer(ESExpBuffer str) {
    if (str->data != oom_buffer)
        free(str->data);
    str->data = oom_buffer_ptr;
    str->maxlen = 0;
    str->len = 0;
}
