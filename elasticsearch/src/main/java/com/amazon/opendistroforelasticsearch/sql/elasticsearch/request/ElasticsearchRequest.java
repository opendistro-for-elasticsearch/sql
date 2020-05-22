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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.request;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.Objects;

/**
 * Elasticsearch search request
 */
@EqualsAndHashCode
@RequiredArgsConstructor
@Getter
@ToString
public class ElasticsearchRequest {

    /**
     * Default scroll context timeout in minutes
     */
    public static final TimeValue DEFAULT_SCROLL_TIMEOUT = TimeValue.timeValueMinutes(1L);

    /**
     * Index name.
     */
    private final String indexName;

    /**
     * Scroll id which is set after first request issued.
     * Because ElasticsearchClient is shared by multi-thread so this state has to be maintained here.
     */
    @Setter
    private String scrollId;

    /**
     * Search request source builder.
     */
    private final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

    /**
     * Generate Elasticsearch search request.
     * @return  search request
     */
    public SearchRequest searchRequest() {
        return new SearchRequest().indices(indexName).
                                   scroll(DEFAULT_SCROLL_TIMEOUT).
                                   source(sourceBuilder);
    }

    /**
     * Is scroll started which means pages after first is being requested
     * @return  true if scroll started
     */
    public boolean isScrollStarted() {
        return (scrollId != null);
    }

    /**
     * Generate Elasticsearch scroll request by scroll id maintained.
     * @return  scroll request
     */
    public SearchScrollRequest scrollRequest() {
        Objects.requireNonNull(scrollId, "Scroll id cannot be null");
        return new SearchScrollRequest().scroll(DEFAULT_SCROLL_TIMEOUT).
                                         scrollId(scrollId);
    }

}
