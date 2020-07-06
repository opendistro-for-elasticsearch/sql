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

#ifndef UNIT_TEST_HELPER
#define UNIT_TEST_HELPER

#if defined(WIN32) || defined (WIN64)
#ifdef _DEBUG
#define VLD_FORCE_ENABLE 1
#include <vld.h>
#endif
#endif

#include <string>
#ifdef USE_SSL
const bool use_ssl = true;
#else
const bool use_ssl = false;
#endif

void WriteFileIfSpecified(char** begin, char** end, const std::string& option,
                          std::string& output);

#endif
