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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.multi;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Eliran on 19/8/2016.
 */
public class MultiQueryAction extends QueryAction {
    private MultiQuerySelect multiQuerySelect;

    public MultiQueryAction(Client client, MultiQuerySelect multiSelect) {
        super(client, null);
        this.multiQuerySelect = multiSelect;
    }

    @Override
    public SqlElasticRequestBuilder explain() throws SqlParseException {
        if (!isValidMultiSelectReturnFields()) {
            throw new SqlParseException("on multi query fields/aliases of one table should be subset of other");
        }
        MultiQueryRequestBuilder requestBuilder = new MultiQueryRequestBuilder(this.multiQuerySelect);
        requestBuilder.setFirstSearchRequest(createRequestBuilder(this.multiQuerySelect.getFirstSelect()));
        requestBuilder.setSecondSearchRequest(createRequestBuilder(this.multiQuerySelect.getSecondSelect()));
        requestBuilder.fillTableAliases(this.multiQuerySelect.getFirstSelect().getFields(),
                this.multiQuerySelect.getSecondSelect().getFields());

        return requestBuilder;
    }

    private boolean isValidMultiSelectReturnFields() {
        List<Field> firstQueryFields = multiQuerySelect.getFirstSelect().getFields();
        List<Field> secondQueryFields = multiQuerySelect.getSecondSelect().getFields();
        if (firstQueryFields.size() > secondQueryFields.size()) {
            return isSubsetFields(firstQueryFields, secondQueryFields);
        }
        return isSubsetFields(secondQueryFields, firstQueryFields);
    }

    private boolean isSubsetFields(List<Field> bigGroup, List<Field> smallerGroup) {
        Set<String> biggerGroup = new HashSet<>();
        for (Field field : bigGroup) {
            String fieldName = getNameOrAlias(field);
            biggerGroup.add(fieldName);
        }
        for (Field field : smallerGroup) {
            String fieldName = getNameOrAlias(field);
            if (!biggerGroup.contains(fieldName)) {
                return false;
            }
        }
        return true;
    }

    private String getNameOrAlias(Field field) {
        String fieldName = field.getName();
        if (field.getAlias() != null && !field.getAlias().isEmpty()) {
            fieldName = field.getAlias();
        }
        return fieldName;
    }

    protected SearchRequestBuilder createRequestBuilder(Select select) throws SqlParseException {
        DefaultQueryAction queryAction = new DefaultQueryAction(client, select);
        queryAction.explain();
        return queryAction.getRequestBuilder();
    }
}
