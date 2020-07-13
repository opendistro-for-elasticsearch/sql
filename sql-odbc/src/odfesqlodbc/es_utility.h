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

#ifndef ES_UTILITY_H
#define ES_UTILITY_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

typedef struct ESExpBufferData {
    char *data;
    size_t len;
    size_t maxlen;
} ESExpBufferData;

typedef ESExpBufferData *ESExpBuffer;

#define ESExpBufferBroken(str) ((str) == NULL || (str)->maxlen == 0)
#define ESExpBufferDataBroken(buf) ((buf).maxlen == 0)
#define INITIAL_EXPBUFFER_SIZE 256

void InitESExpBuffer(ESExpBuffer str);
void AppendESExpBuffer(ESExpBuffer str, const char *fmt, ...);
void TermESExpBuffer(ESExpBuffer str);

#ifdef __cplusplus
}
#endif

#endif /* ES_UTILITY_H */
