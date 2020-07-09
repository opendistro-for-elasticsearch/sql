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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.JoinSelect;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.HintType;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import org.elasticsearch.client.Client;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 22/8/2015.
 */
public class ESHashJoinQueryAction extends ESJoinQueryAction {

    public ESHashJoinQueryAction(Client client, JoinSelect joinSelect) {
        super(client, joinSelect);
    }

    @Override
    protected void fillSpecificRequestBuilder(JoinRequestBuilder requestBuilder) throws SqlParseException {
        String t1Alias = joinSelect.getFirstTable().getAlias();
        String t2Alias = joinSelect.getSecondTable().getAlias();

        List<List<Map.Entry<Field, Field>>> comparisonFields = getComparisonFields(t1Alias, t2Alias,
                joinSelect.getConnectedWhere());

        ((HashJoinElasticRequestBuilder) requestBuilder).setT1ToT2FieldsComparison(comparisonFields);
    }

    @Override
    protected JoinRequestBuilder createSpecificBuilder() {
        if (isLegacy()) {
            return new HashJoinElasticRequestBuilder();
        }
        return new HashJoinQueryPlanRequestBuilder(client, sqlRequest);
    }

    @Override
    protected void updateRequestWithHints(JoinRequestBuilder requestBuilder) {
        super.updateRequestWithHints(requestBuilder);
        for (Hint hint : joinSelect.getHints()) {
            if (hint.getType() == HintType.HASH_WITH_TERMS_FILTER) {
                ((HashJoinElasticRequestBuilder) requestBuilder).setUseTermFiltersOptimization(true);
            }
        }
    }

    /**
     * Keep the option to run legacy hash join algorithm mainly for the comparison
     */
    private boolean isLegacy() {
        for (Hint hint : joinSelect.getHints()) {
            if (hint.getType() == HintType.JOIN_ALGORITHM_USE_LEGACY) {
                return true;
            }
        }
        return false;
    }

    private List<Map.Entry<Field, Field>> getComparisonFields(String t1Alias, String t2Alias,
                                                              List<Condition> connectedConditions)
            throws SqlParseException {
        List<Map.Entry<Field, Field>> comparisonFields = new ArrayList<>();
        for (Condition condition : connectedConditions) {

            if (condition.getOPERATOR() != Condition.OPERATOR.EQ) {
                throw new SqlParseException(
                        String.format("HashJoin should only be with EQ conditions, got:%s on condition:%s",
                                condition.getOPERATOR().name(), condition.toString()));
            }

            String firstField = condition.getName();
            String secondField = condition.getValue().toString();
            Field t1Field, t2Field;
            if (firstField.startsWith(t1Alias)) {
                t1Field = new Field(removeAlias(firstField, t1Alias), null);
                t2Field = new Field(removeAlias(secondField, t2Alias), null);
            } else {
                t1Field = new Field(removeAlias(secondField, t1Alias), null);
                t2Field = new Field(removeAlias(firstField, t2Alias), null);
            }
            comparisonFields.add(new AbstractMap.SimpleEntry<>(t1Field, t2Field));
        }
        return comparisonFields;
    }

    private List<List<Map.Entry<Field, Field>>> getComparisonFields(String t1Alias, String t2Alias,
                                                                    Where connectedWhere) throws SqlParseException {
        List<List<Map.Entry<Field, Field>>> comparisonFields = new ArrayList<>();
        //where is AND with lots of conditions.
        if (connectedWhere == null) {
            return comparisonFields;
        }
        boolean allAnds = true;
        for (Where innerWhere : connectedWhere.getWheres()) {
            if (innerWhere.getConn() == Where.CONN.OR) {
                allAnds = false;
                break;
            }
        }
        if (allAnds) {
            List<Map.Entry<Field, Field>> innerComparisonFields =
                    getComparisonFieldsFromWhere(t1Alias, t2Alias, connectedWhere);
            comparisonFields.add(innerComparisonFields);
        } else {
            for (Where innerWhere : connectedWhere.getWheres()) {
                comparisonFields.add(getComparisonFieldsFromWhere(t1Alias, t2Alias, innerWhere));
            }
        }

        return comparisonFields;
    }

    private List<Map.Entry<Field, Field>> getComparisonFieldsFromWhere(String t1Alias, String t2Alias, Where where)
            throws SqlParseException {
        List<Condition> conditions = new ArrayList<>();
        if (where instanceof Condition) {
            conditions.add((Condition) where);
        } else {
            for (Where innerWhere : where.getWheres()) {
                if (!(innerWhere instanceof Condition)) {
                    throw new SqlParseException(
                            "if connectedCondition is AND then all inner wheres should be Conditions");
                }
                conditions.add((Condition) innerWhere);
            }
        }
        return getComparisonFields(t1Alias, t2Alias, conditions);
    }

    private String removeAlias(String field, String alias) {
        return field.replace(alias + ".", "");
    }

}
