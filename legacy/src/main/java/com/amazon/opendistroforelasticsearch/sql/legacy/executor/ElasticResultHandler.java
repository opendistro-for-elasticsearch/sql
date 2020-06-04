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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor;

import org.elasticsearch.search.SearchHit;

import java.util.Map;

/**
 * Created by Eliran on 3/10/2015.
 */
public class ElasticResultHandler {
    public static Object getFieldValue(SearchHit hit, String field) {
        return deepSearchInMap(hit.getSourceAsMap(), field);
    }

    private static Object deepSearchInMap(Map<String, Object> fieldsMap, String name) {
        if (name.contains(".")) {
            String[] path = name.split("\\.");
            Map<String, Object> currentObject = fieldsMap;
            for (int i = 0; i < path.length - 1; i++) {
                Object valueFromCurrentMap = currentObject.get(path[i]);
                if (valueFromCurrentMap == null) {
                    return null;
                }
                if (!Map.class.isAssignableFrom(valueFromCurrentMap.getClass())) {
                    return null;
                }
                currentObject = (Map<String, Object>) valueFromCurrentMap;
            }
            return currentObject.get(path[path.length - 1]);
        }

        return fieldsMap.get(name);
    }

}
