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
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.TableOnJoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Config;
import org.elasticsearch.client.Client;

import java.util.List;

/**
 * Created by Eliran on 15/9/2015.
 */
public abstract class ESJoinQueryAction extends QueryAction {

    protected JoinSelect joinSelect;

    public ESJoinQueryAction(Client client, JoinSelect joinSelect) {
        super(client, joinSelect);
        this.joinSelect = joinSelect;
    }

    @Override
    public SqlElasticRequestBuilder explain() throws SqlParseException {
        JoinRequestBuilder requestBuilder = createSpecificBuilder();
        fillBasicJoinRequestBuilder(requestBuilder);
        fillSpecificRequestBuilder(requestBuilder);
        return requestBuilder;
    }

    protected abstract void fillSpecificRequestBuilder(JoinRequestBuilder requestBuilder) throws SqlParseException;

    protected abstract JoinRequestBuilder createSpecificBuilder();


    private void fillBasicJoinRequestBuilder(JoinRequestBuilder requestBuilder) throws SqlParseException {

        fillTableInJoinRequestBuilder(requestBuilder.getFirstTable(), joinSelect.getFirstTable());
        fillTableInJoinRequestBuilder(requestBuilder.getSecondTable(), joinSelect.getSecondTable());

        requestBuilder.setJoinType(joinSelect.getJoinType());

        requestBuilder.setTotalLimit(joinSelect.getTotalLimit());

        updateRequestWithHints(requestBuilder);


    }

    protected void updateRequestWithHints(JoinRequestBuilder requestBuilder) {
        for (Hint hint : joinSelect.getHints()) {
            Object[] params = hint.getParams();
            switch (hint.getType()) {
                case JOIN_LIMIT:
                    requestBuilder.getFirstTable().setHintLimit((Integer) params[0]);
                    requestBuilder.getSecondTable().setHintLimit((Integer) params[1]);
                    break;
                case JOIN_ALGORITHM_BLOCK_SIZE:
                    if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
                        queryPlannerConfig(requestBuilder).configureBlockSize(hint.getParams());
                    }
                    break;
                case JOIN_SCROLL_PAGE_SIZE:
                    if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
                        queryPlannerConfig(requestBuilder).configureScrollPageSize(hint.getParams());
                    }
                    break;
                case JOIN_CIRCUIT_BREAK_LIMIT:
                    if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
                        queryPlannerConfig(requestBuilder).configureCircuitBreakLimit(hint.getParams());
                    }
                    break;
                case JOIN_BACK_OFF_RETRY_INTERVALS:
                    if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
                        queryPlannerConfig(requestBuilder).configureBackOffRetryIntervals(hint.getParams());
                    }
                    break;
                case JOIN_TIME_OUT:
                    if (requestBuilder instanceof HashJoinQueryPlanRequestBuilder) {
                        queryPlannerConfig(requestBuilder).configureTimeOut(hint.getParams());
                    }
                    break;
            }
        }
    }

    private Config queryPlannerConfig(JoinRequestBuilder requestBuilder) {
        return ((HashJoinQueryPlanRequestBuilder) requestBuilder).getConfig();
    }

    private void fillTableInJoinRequestBuilder(TableInJoinRequestBuilder requestBuilder,
                                               TableOnJoinSelect tableOnJoinSelect) throws SqlParseException {
        List<Field> connectedFields = tableOnJoinSelect.getConnectedFields();
        addFieldsToSelectIfMissing(tableOnJoinSelect, connectedFields);
        requestBuilder.setOriginalSelect(tableOnJoinSelect);
        DefaultQueryAction queryAction = new DefaultQueryAction(client, tableOnJoinSelect);
        queryAction.explain();
        requestBuilder.setRequestBuilder(queryAction.getRequestBuilder());
        requestBuilder.setReturnedFields(tableOnJoinSelect.getSelectedFields());
        requestBuilder.setAlias(tableOnJoinSelect.getAlias());
    }

    private void addFieldsToSelectIfMissing(Select select, List<Field> fields) {
        //this means all fields
        if (select.getFields() == null || select.getFields().size() == 0) {
            return;
        }

        List<Field> selectedFields = select.getFields();
        for (Field field : fields) {
            if (!selectedFields.contains(field)) {
                selectedFields.add(field);
            }
        }

    }

}
