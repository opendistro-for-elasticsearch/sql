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

#include "es_semaphore.h"

#include <string>

#ifdef WIN32
namespace {
HANDLE createSemaphore(unsigned int initial, unsigned int capacity) {
    HANDLE semaphore = NULL;
    std::string semName;
    while (NULL == semaphore) {
        semName = "es_sem_" + std::to_string(rand() * 1000);
        semaphore = CreateSemaphore(NULL, initial, capacity, semName.c_str());
    }

    return semaphore;
}
}  // namespace
#else
#include <time.h>
#endif

#ifdef __APPLE__
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wunused-parameter"
#endif  // __APPLE__
es_semaphore::es_semaphore(unsigned int initial, unsigned int capacity)
#ifdef __APPLE__
#pragma clang diagnostic pop
#endif  // __APPLE__
    :
#ifdef WIN32
      m_semaphore(createSemaphore(initial, capacity))
#elif defined(__APPLE__)
      m_semaphore(dispatch_semaphore_create(initial))
#endif
{
#if !defined(WIN32) && !defined(__APPLE__)
    sem_init(&m_semaphore, 0, capacity);
#endif
}

es_semaphore::~es_semaphore() {
#ifdef WIN32
    CloseHandle(m_semaphore);
#elif defined(__APPLE__)
#else
    sem_destroy(&m_semaphore);
#endif
}

void es_semaphore::lock() {
#ifdef WIN32
    WaitForSingleObject(m_semaphore, INFINITE);
#elif defined(__APPLE__)
    dispatch_semaphore_wait(m_semaphore, DISPATCH_TIME_FOREVER);
#else
    sem_wait(&m_semaphore);
#endif
}

void es_semaphore::release() {
#ifdef WIN32
    ReleaseSemaphore(m_semaphore, 1, NULL);
#elif defined(__APPLE__)
    dispatch_semaphore_signal(m_semaphore);
#else
    sem_post(&m_semaphore);
#endif
}

bool es_semaphore::try_lock_for(unsigned int timeout_ms) {
#ifdef WIN32
    return WaitForSingleObject(m_semaphore, timeout_ms) == WAIT_OBJECT_0;
#elif defined(__APPLE__)
    return 0
           == dispatch_semaphore_wait(
               m_semaphore, dispatch_time(DISPATCH_TIME_NOW,
                                          static_cast< int64_t >(
                                              timeout_ms * NSEC_PER_MSEC)));
#else
    struct timespec ts;
    if (-1 == clock_gettime(CLOCK_REALTIME, &ts)) {
        return false;
    }

    ts.tv_nsec += timeout_ms * 1000000;
    return 0 == sem_timedwait(&m_semaphore & ts);
#endif
}
