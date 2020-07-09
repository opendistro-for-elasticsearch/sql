/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The definition of the Scalar Operation.
 */
@Getter
@RequiredArgsConstructor
public enum ScalarOperation {
    ADD("add"),
    SUBTRACT("subtract"),
    MULTIPLY("multiply"),
    DIVIDE("divide"),
    MODULES("modules"),
    ABS("abs"),
    ACOS("acos"),
    ASIN("asin"),
    ATAN("atan"),
    ATAN2("atan2"),
    TAN("tan"),
    CBRT("cbrt"),
    CEIL("ceil"),
    COS("cos"),
    COSH("cosh"),
    EXP("exp"),
    FLOOR("floor"),
    LN("ln"),
    LOG("log"),
    LOG2("log2"),
    LOG10("log10");

    private final String name;
}
