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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.join;

import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eliran on 28/8/2015.
 */
public class SearchHitsResult {
    private List<SearchHit> searchHits;
    private boolean matchedWithOtherTable;

    public SearchHitsResult() {
        searchHits = new ArrayList<>();
    }

    public SearchHitsResult(List<SearchHit> searchHits, boolean matchedWithOtherTable) {
        this.searchHits = searchHits;
        this.matchedWithOtherTable = matchedWithOtherTable;
    }

    public List<SearchHit> getSearchHits() {
        return searchHits;
    }

    public void setSearchHits(List<SearchHit> searchHits) {
        this.searchHits = searchHits;
    }

    public boolean isMatchedWithOtherTable() {
        return matchedWithOtherTable;
    }

    public void setMatchedWithOtherTable(boolean matchedWithOtherTable) {
        this.matchedWithOtherTable = matchedWithOtherTable;
    }
}
