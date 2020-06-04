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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.multi;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import com.google.common.base.Joiner;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 9/9/2016.
 */
public class ComperableHitResult {
    private SearchHit hit;
    private String comperator;
    private boolean isAllNull;
    private Map<String, Object> flattenMap;

    public ComperableHitResult(SearchHit hit, String[] fieldsOrder, String seperator) {
        this.hit = hit;
        Map<String, Object> hitAsMap = hit.getSourceAsMap();
        this.flattenMap = new HashMap<>();
        List<String> results = new ArrayList<>();
        this.isAllNull = true;

        for (int i = 0; i < fieldsOrder.length; i++) {
            String field = fieldsOrder[i];
            Object result = Util.deepSearchInMap(hitAsMap, field);
            if (result == null) {
                results.add("");
            } else {
                this.isAllNull = false;
                results.add(result.toString());
                this.flattenMap.put(field, result);
            }
        }
        this.comperator = Joiner.on(seperator).join(results);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComperableHitResult that = (ComperableHitResult) o;

        if (!comperator.equals(that.comperator)) {
            return false;
        }

        return true;
    }

    public boolean isAllNull() {
        return isAllNull;
    }

    @Override
    public int hashCode() {
        return comperator.hashCode();
    }

    public String getComperator() {
        return comperator;
    }

    public Map<String, Object> getFlattenMap() {
        return flattenMap;
    }

    public SearchHit getOriginalHit() {
        return hit;
    }
}
