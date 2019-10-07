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

package com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm;

import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.esdomain.mapping.IndexMappings;

import java.util.HashMap;
import java.util.Map;

/**
 * Index Mapping information in current query being visited.
 */
public class TermFieldScope {

    // mapper => index, type, field_name, FieldMappingMetaData
    private IndexMappings mapper;
    private FieldMappings finalMapping;
    private Map<String, String> aliases;

    public TermFieldScope() {
        this.mapper = IndexMappings.EMPTY;
        this.aliases = new HashMap<>();
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases;
    }

    public IndexMappings getMapper() {
        return this.mapper;
    }

    public void setMapper(IndexMappings mapper) {
        this.mapper = mapper;
    }

    public FieldMappings getFinalMapping() {
        return this.finalMapping;
    }

    public void setFinalMapping(FieldMappings finalMapping) {
        this.finalMapping = finalMapping;
    }

}
