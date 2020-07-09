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

#include "es_result_queue.h"

#include "es_types.h"

ESResultQueue::ESResultQueue(unsigned int capacity)
    : m_push_semaphore(capacity, capacity),
      m_pop_semaphore(0, capacity) {
}

ESResultQueue::~ESResultQueue() {
    while (!m_queue.empty()) {
        delete m_queue.front();
        m_queue.pop();
    }
}

void ESResultQueue::clear() {
    std::scoped_lock lock(m_queue_mutex);
    while (!m_queue.empty()) {
        delete m_queue.front();
        m_queue.pop();
        m_push_semaphore.release();
        m_pop_semaphore.lock();
    }
}

bool ESResultQueue::pop(unsigned int timeout_ms, ESResult*& result) {
    if (m_pop_semaphore.try_lock_for(timeout_ms)) {
        std::scoped_lock lock(m_queue_mutex);
        result = m_queue.front();
        m_queue.pop();
        m_push_semaphore.release();
        return true;
    }

    return false;
}

bool ESResultQueue::push(unsigned int timeout_ms, ESResult* result) {
    if (m_push_semaphore.try_lock_for(timeout_ms)) {
        std::scoped_lock lock(m_queue_mutex);
        m_queue.push(result);
        m_pop_semaphore.release();
        return true;
    }

    return false;
}
