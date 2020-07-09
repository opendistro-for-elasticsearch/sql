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
#ifndef ES_RESULT_QUEUE
#define ES_RESULT_QUEUE

#include <queue>
#include <mutex>
#include "es_semaphore.h"

#define QUEUE_TIMEOUT 20 // milliseconds

struct ESResult;

class ESResultQueue {
    public:
        ESResultQueue(unsigned int capacity);
        ~ESResultQueue();

        void clear();
        bool pop(unsigned int timeout_ms, ESResult*& result);
        bool push(unsigned int timeout_ms, ESResult* result);

    private:
        std::queue<ESResult*> m_queue;
        std::mutex m_queue_mutex;
        es_semaphore m_push_semaphore;
        es_semaphore m_pop_semaphore;
};

#endif
