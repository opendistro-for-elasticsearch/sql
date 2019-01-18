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

package com.amazon.opendistro.sql.rewriter.matchtoterm;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;

/**
 * Index Mapping information in current query being visited.
 */
public class TermFieldScope {

    // mapper => index, type, field_name, FieldMappingMetaData
    private Map<String, Map<String, Map<String, FieldMappingMetaData>>> mapper;
    private Map<String, FieldMappingMetaData> finalMapping;
    private String[] finalMappingAsArray;
    private Map<String, String> aliases;

    public TermFieldScope() {
        this.mapper = new HashMap<>();
        this.aliases = new HashMap<>();
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases;
    }

    public Map<String, Map<String, Map<String, FieldMappingMetaData>>> getMapper() {
        return this.mapper;
    }

    public void setMapper(Map<String, Map<String, Map<String, FieldMappingMetaData>>> mapper) {
        this.mapper = mapper;
    }

    public Map<String, FieldMappingMetaData> getFinalMapping() {
        return this.finalMapping;
    }

    public void setFinalMapping(Map<String, FieldMappingMetaData> finalMapping) {
        this.finalMapping = finalMapping;

    }

    public String[] getFinalMappingAsArray() {
        return finalMappingAsArray;
    }

    public void setFinalMappingAsArray(String[] finalMappingAsArray) {
        this.finalMappingAsArray = finalMappingAsArray;
    }
}
