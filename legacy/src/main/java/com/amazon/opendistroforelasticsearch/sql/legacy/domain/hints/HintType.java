/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints;

/**
 * Created by Eliran on 29/8/2015.
 */
public enum HintType {
    HASH_WITH_TERMS_FILTER,
    JOIN_LIMIT,
    USE_NESTED_LOOPS,
    NL_MULTISEARCH_SIZE,
    USE_SCROLL,
    IGNORE_UNAVAILABLE,
    DOCS_WITH_AGGREGATION,
    ROUTINGS,
    SHARD_SIZE,
    HIGHLIGHT,
    MINUS_FETCH_AND_RESULT_LIMITS,
    MINUS_USE_TERMS_OPTIMIZATION,
    COLLAPSE,
    POST_FILTER,
    JOIN_ALGORITHM_BLOCK_SIZE,
    JOIN_ALGORITHM_USE_LEGACY,
    JOIN_SCROLL_PAGE_SIZE,
    JOIN_CIRCUIT_BREAK_LIMIT,
    JOIN_BACK_OFF_RETRY_INTERVALS,
    JOIN_TIME_OUT
}
