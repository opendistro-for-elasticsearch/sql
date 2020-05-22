/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.response;

import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchResponseTest {

    @Mock
    private SearchResponse esResponse;

    @BeforeEach
    void setUp() {
        when(esResponse.getHits()).thenReturn(
            new SearchHits(
                new SearchHit[]{ new SearchHit(1), new SearchHit(2) },
                new TotalHits(2L, TotalHits.Relation.EQUAL_TO),
                1.0F
            )
        );
    }

    @Test
    void isEmpty() {
        ElasticsearchResponse response1 = new ElasticsearchResponse(esResponse);
        assertFalse(response1.isEmpty());

        when(esResponse.getHits()).thenReturn(SearchHits.empty());
        ElasticsearchResponse response2 = new ElasticsearchResponse(esResponse);
        assertTrue(response2.isEmpty());
    }

    @Test
    void iterator() {
        int i = 0;
        for (SearchHit hit : new ElasticsearchResponse(esResponse)) {
            if (i == 0) {
                assertEquals(new SearchHit(1), hit);
            } else if (i == 1) {
                assertEquals(new SearchHit(2), hit);
            } else {
                fail("More search hits returned than expected");
            }
            i++;
        }
    }

}