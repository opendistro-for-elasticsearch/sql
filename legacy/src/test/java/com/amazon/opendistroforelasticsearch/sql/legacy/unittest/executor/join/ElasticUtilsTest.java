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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.executor.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.ElasticUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.MetaSearchResult;
import org.apache.lucene.search.TotalHits;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class ElasticUtilsTest {

    @Mock
    MetaSearchResult metaSearchResult;

    /**
     * test handling {@link TotalHits} correctly.
     */
    @Test
    public void hitsAsStringResult() throws IOException {
        final SearchHits searchHits = new SearchHits(new SearchHit[]{}, new TotalHits(1, Relation.EQUAL_TO), 0);
        final String result = ElasticUtils.hitsAsStringResult(searchHits, metaSearchResult);

        Assert.assertEquals(1, new JSONObject(result).query("/hits/total/value"));
        Assert.assertEquals(Relation.EQUAL_TO.toString(), new JSONObject(result).query("/hits/total/relation"));
    }

    /**
     * test handling {@link TotalHits} with null value correctly.
     */
    @Test
    public void test_hitsAsStringResult_withNullTotalHits() throws IOException {
        final SearchHits searchHits = new SearchHits(new SearchHit[]{}, null, 0);
        final String result = ElasticUtils.hitsAsStringResult(searchHits, metaSearchResult);

        Assert.assertEquals(0, new JSONObject(result).query("/hits/total/value"));
        Assert.assertEquals(Relation.EQUAL_TO.toString(), new JSONObject(result).query("/hits/total/relation"));
    }
}