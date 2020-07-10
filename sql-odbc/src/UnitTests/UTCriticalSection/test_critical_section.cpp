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
#include <stdio.h>

#include <iostream>
#include <string>
#include <thread>

#include "es_helper.h"
#include "pch.h"
#include "unit_test_helper.h"

const size_t loop_count = 10;
const size_t thread_count = 1000;

#define INIT_CS(x) XPlatformInitializeCriticalSection(&(x))
#define ENTER_CS(x) XPlatformEnterCriticalSection((x))
#define LEAVE_CS(x) XPlatformLeaveCriticalSection((x))
#define DELETE_CS(x) XPlatformDeleteCriticalSection(&(x))

class TestCriticalSection : public testing::Test {
   public:
    TestCriticalSection() : m_lock(NULL) {
    }

    void SetUp() {
        INIT_CS(m_lock);
    }

    void TearDown() {
        DELETE_CS(m_lock);
    }

    ~TestCriticalSection() {
    }
    void* m_lock;

    typedef struct CriticalInfo {
        volatile size_t* shared_mem;
        void* lock;
    } CriticalInfo;
};

TEST_F(TestCriticalSection, SingleEnterExit) {
    ENTER_CS(m_lock);
    LEAVE_CS(m_lock);
}

TEST_F(TestCriticalSection, MultipleEntersMultipleExits) {
    for (size_t i = 0; i < loop_count; i++)
        ENTER_CS(m_lock);
    for (size_t i = 0; i < loop_count; i++)
        LEAVE_CS(m_lock);
}

TEST_F(TestCriticalSection, MultipleEnterExit) {
    for (size_t i = 0; i < loop_count; i++) {
        ENTER_CS(m_lock);
        LEAVE_CS(m_lock);
    }
}

TEST_F(TestCriticalSection, MultiThreadSingleLock) {
    auto f = [](CriticalInfo* info) {
        *info->shared_mem = static_cast< size_t >(1);
        ENTER_CS(info->lock);
        *info->shared_mem = static_cast< size_t >(2);
        LEAVE_CS(info->lock);
    };

    volatile size_t shared_mem = 0;
    CriticalInfo crit_info;
    crit_info.shared_mem = &shared_mem;
    crit_info.lock = m_lock;

    ENTER_CS(m_lock);
    std::thread thread_object(f, &crit_info);
#ifdef WIN32
    Sleep(1000);
#else
    usleep(1000 * 1000);
#endif
    EXPECT_EQ(shared_mem, static_cast< size_t >(1));
    LEAVE_CS(m_lock);
#ifdef WIN32
    Sleep(1000);
#else
    usleep(1000 * 1000);
#endif
    EXPECT_EQ(shared_mem, static_cast< size_t >(2));
    thread_object.join();
}

// Make many threads to see if multiple simultaneous attempts at locking cause
// any issues
TEST_F(TestCriticalSection, RaceConditions) {
    auto f = [](CriticalInfo* info) {
        std::stringstream ss_thread_id;
        ss_thread_id << std::this_thread::get_id();
        size_t thread_id = static_cast< size_t >(
            strtoull(ss_thread_id.str().c_str(), NULL, 10));
        ENTER_CS(info->lock);
        // Update shared memory, release thread priority, then check if memory
        // is still the same
        *info->shared_mem = static_cast< size_t >(thread_id);
#ifdef WIN32
        Sleep(0);
#else
        usleep(0);
#endif
        EXPECT_EQ(thread_id, *info->shared_mem);
        LEAVE_CS(info->lock);
    };

    volatile size_t shared_mem = 0;
    CriticalInfo crit_info;
    crit_info.shared_mem = &shared_mem;
    crit_info.lock = m_lock;
    std::vector< std::thread > threads;
    threads.reserve(thread_count);

    for (size_t i = 0; i < thread_count; i++)
        threads.emplace_back(std::thread(f, &crit_info));

    for (auto& it : threads)
        it.join();
}

int main(int argc, char** argv) {
    testing::internal::CaptureStdout();
    ::testing::InitGoogleTest(&argc, argv);
    int failures = RUN_ALL_TESTS();
    std::string output = testing::internal::GetCapturedStdout();
    std::cout << output << std::endl;
    std::cout << (failures ? "Not all tests passed." : "All tests passed")
              << std::endl;
    WriteFileIfSpecified(argv, argv + argc, "-fout", output);
}
