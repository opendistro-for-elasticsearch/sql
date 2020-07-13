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

#include "unit_test_helper.h"

#include <fstream>
#include <iostream>

void WriteFileIfSpecified(char** begin, char** end, const std::string& option,
                          std::string& output) {
    char** itr = std::find(begin, end, option);
    if (itr != end && ++itr != end) {
        std::ofstream out_file(*itr);
        if (out_file.good())
            out_file << output;
    }
    return;
}
