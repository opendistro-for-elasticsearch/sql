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

package com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.matchtoterm;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.FieldMappings;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping.IndexMappings;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    public Optional<Map<String, Object>> resolveFieldMapping(String fieldName) {
        Set<FieldMappings> indexMappings = mapper.allMappings().stream().
                flatMap(typeMappings -> typeMappings.allMappings().stream()).
                collect(Collectors.toSet());
        Optional<Map<String, Object>> resolvedMapping =
                indexMappings.stream()
                        .filter(mapping -> mapping.has(fieldName))
                        .map(mapping -> mapping.mapping(fieldName)).reduce((map1, map2) -> {
                    if (!map1.equals(map2)) {
                        // TODO: Merge mappings if they are compatible, for text and text/keyword to text/keyword.
                        String exceptionReason = String.format(Locale.ROOT, "Different mappings are not allowed "
                                        + "for the same field[%s]: found [%s] and [%s] ",
                                fieldName, pretty(map1), pretty(map2));
                        throw new VerificationException(exceptionReason);
                    }
                    return map1;
                });
        return resolvedMapping;
    }

    private static String pretty(Map<String, Object> mapping) {
        return new JSONObject(mapping).toString().replaceAll("\"", "");
    }

}
