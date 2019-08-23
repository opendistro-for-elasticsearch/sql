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

package com.amazon.opendistroforelasticsearch.sql.executor.join;

import com.amazon.opendistroforelasticsearch.sql.domain.Field;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Eliran on 2/11/2015.
 */
public class HashJoinComparisonStructure {
    private HashMap<String, List<Map.Entry<Field, Field>>> comparisonIDtoComparisonFields;
    private HashMap<String, HashMap<String, SearchHitsResult>> comparisonIDtoComparisonHash;

    public HashJoinComparisonStructure(List<List<Map.Entry<Field, Field>>> t1ToT2FieldsComparisons) {
        comparisonIDtoComparisonFields = new HashMap<>();
        comparisonIDtoComparisonHash = new HashMap<>();
        if (t1ToT2FieldsComparisons == null || t1ToT2FieldsComparisons.size() == 0) {
            String comparisonId = UUID.randomUUID().toString();
            this.comparisonIDtoComparisonFields.put(comparisonId, new ArrayList<Map.Entry<Field, Field>>());
            this.comparisonIDtoComparisonHash.put(comparisonId, new HashMap<String, SearchHitsResult>());
        }
        for (List<Map.Entry<Field, Field>> comparisonFields : t1ToT2FieldsComparisons) {
            String comparisonId = UUID.randomUUID().toString();
            //maby from field to List<IDS> ?
            this.comparisonIDtoComparisonFields.put(comparisonId, comparisonFields);
            this.comparisonIDtoComparisonHash.put(comparisonId, new HashMap<String, SearchHitsResult>());
        }
    }

    public HashMap<String, List<Map.Entry<Field, Field>>> getComparisons() {
        return comparisonIDtoComparisonFields;
    }

    public void insertIntoComparisonHash(String comparisonID, String comparisonKey, SearchHit hit) {
        HashMap<String, SearchHitsResult> comparisonHash = this.comparisonIDtoComparisonHash.get(comparisonID);
        SearchHitsResult currentSearchHitsResult = comparisonHash.get(comparisonKey);
        if (currentSearchHitsResult == null) {
            currentSearchHitsResult = new SearchHitsResult(new ArrayList<SearchHit>(), false);
            comparisonHash.put(comparisonKey, currentSearchHitsResult);
        }
        currentSearchHitsResult.getSearchHits().add(hit);
    }

    public SearchHitsResult searchForMatchingSearchHits(String comparisonID, String comparisonKey) {
        HashMap<String, SearchHitsResult> comparisonHash = this.comparisonIDtoComparisonHash.get(comparisonID);
        return comparisonHash.get(comparisonKey);
    }

    public List<SearchHitsResult> getAllSearchHits() {
        List<SearchHitsResult> allSearchHits = new ArrayList<>();

        for (HashMap<String, SearchHitsResult> comparisonHash : this.comparisonIDtoComparisonHash.values()) {
            allSearchHits.addAll(comparisonHash.values());
        }
        return allSearchHits;
    }

}
