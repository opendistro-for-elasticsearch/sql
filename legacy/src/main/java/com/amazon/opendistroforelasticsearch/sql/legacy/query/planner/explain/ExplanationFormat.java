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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.explain;

import java.util.Map;

/**
 * Explanation format
 */
public interface ExplanationFormat {

    /**
     * Initialize internal data structure
     *
     * @param kvs key-value pairs
     */
    void prepare(Map<String, String> kvs);

    /**
     * Start a new section in explanation.
     *
     * @param name section name
     */
    void start(String name);


    /**
     * Explain and add to current section.
     *
     * @param object object to be added to explanation
     */
    void explain(Object object);


    /**
     * End current section.
     */
    void end();

}
