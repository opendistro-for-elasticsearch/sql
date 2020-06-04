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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintType;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import org.elasticsearch.client.Client;

/**
 * Created by Eliran on 15/9/2015.
 */
public class ESNestedLoopsQueryAction extends ESJoinQueryAction {

    public ESNestedLoopsQueryAction(Client client, JoinSelect joinSelect) {
        super(client, joinSelect);
    }

    @Override
    protected void fillSpecificRequestBuilder(JoinRequestBuilder requestBuilder) throws SqlParseException {
        NestedLoopsElasticRequestBuilder nestedBuilder = (NestedLoopsElasticRequestBuilder) requestBuilder;
        Where where = joinSelect.getConnectedWhere();
        nestedBuilder.setConnectedWhere(where);

    }

    @Override
    protected JoinRequestBuilder createSpecificBuilder() {
        return new NestedLoopsElasticRequestBuilder();
    }

    @Override
    protected void updateRequestWithHints(JoinRequestBuilder requestBuilder) {
        super.updateRequestWithHints(requestBuilder);
        for (Hint hint : this.joinSelect.getHints()) {
            if (hint.getType() == HintType.NL_MULTISEARCH_SIZE) {
                Integer multiSearchMaxSize = (Integer) hint.getParams()[0];
                ((NestedLoopsElasticRequestBuilder) requestBuilder).setMultiSearchMaxSize(multiSearchMaxSize);
            }
        }
    }

    private String removeAlias(String field) {
        String alias = joinSelect.getFirstTable().getAlias();
        if (!field.startsWith(alias + ".")) {
            alias = joinSelect.getSecondTable().getAlias();
        }
        return field.replace(alias + ".", "");
    }

}
