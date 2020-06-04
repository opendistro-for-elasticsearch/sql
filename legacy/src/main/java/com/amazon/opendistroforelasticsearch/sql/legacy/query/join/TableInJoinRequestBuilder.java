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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import org.elasticsearch.action.search.SearchRequestBuilder;

import java.util.List;

/**
 * Created by Eliran on 28/8/2015.
 */
public class TableInJoinRequestBuilder {
    private SearchRequestBuilder requestBuilder;
    private String alias;
    private List<Field> returnedFields;
    private Select originalSelect;
    private Integer hintLimit;

    public TableInJoinRequestBuilder() {
    }

    public SearchRequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public void setRequestBuilder(SearchRequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<Field> getReturnedFields() {
        return returnedFields;
    }

    public void setReturnedFields(List<Field> returnedFields) {
        this.returnedFields = returnedFields;
    }

    public Select getOriginalSelect() {
        return originalSelect;
    }

    public void setOriginalSelect(Select originalSelect) {
        this.originalSelect = originalSelect;
    }

    public Integer getHintLimit() {
        return hintLimit;
    }

    public void setHintLimit(Integer hintLimit) {
        this.hintLimit = hintLimit;
    }
}
