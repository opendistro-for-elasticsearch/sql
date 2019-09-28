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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.IndexMappings;

import java.util.Objects;

/**
 * Semantic context responsible for environment chain (stack) management and everything required for analysis.
 * This context should be shared by different stages in future, particularly
 * from semantic analysis to logical planning to physical planning.
 */
public class SemanticContext {

    /** Local cluster state for mapping query */
    private final LocalClusterState clusterState;

    /** Environment stack for symbol scope management */
    private Environment environment = new Environment(null);

    public SemanticContext(LocalClusterState clusterState) {
        this.clusterState = clusterState;
    }

    /**
     * Push a new environment
     */
    public void push() {
        environment = new Environment(environment);
    }

    /**
     * Return current environment
     * @return  current environment
     */
    public Environment peek() {
        return environment;
    }

    /**
     * Pop up current environment from environment chain
     * @return  current environment (before pop)
     */
    public Environment pop() {
        Objects.requireNonNull(environment, "Fail to pop context due to no environment present");

        Environment curEnv = environment;
        environment = curEnv.getParent();
        return curEnv;
    }

    /**
     * Return field mappings for an index
     * @param indexName     index name
     * @return              field mappings
     */
    public FieldMappings getMapping(String indexName) {
        IndexMappings indexMappings = clusterState.getFieldMappings(new String[]{indexName});
        return indexMappings.firstMapping().firstMapping();
    }

}
