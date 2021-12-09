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
