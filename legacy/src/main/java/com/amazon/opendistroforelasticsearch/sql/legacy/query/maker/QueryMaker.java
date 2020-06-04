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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.maker;


import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.join.query.JoinQueryBuilders;

public class QueryMaker extends Maker {

    /**
     * 将where条件构建成query
     *
     * @param where
     * @return
     * @throws SqlParseException
     */
    public static BoolQueryBuilder explain(Where where) throws SqlParseException {
        return explain(where, true);
    }

    public static BoolQueryBuilder explain(Where where, boolean isQuery) throws SqlParseException {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        while (where.getWheres().size() == 1) {
            where = where.getWheres().getFirst();
        }
        new QueryMaker().explanWhere(boolQuery, where);
        if (isQuery) {
            return boolQuery;
        }
        return QueryBuilders.boolQuery().filter(boolQuery);
    }

    private QueryMaker() {
        super(true);
    }

    private void explanWhere(BoolQueryBuilder boolQuery, Where where) throws SqlParseException {
        if (where instanceof Condition) {
            addSubQuery(boolQuery, where, (QueryBuilder) make((Condition) where));
        } else {
            BoolQueryBuilder subQuery = QueryBuilders.boolQuery();
            addSubQuery(boolQuery, where, subQuery);
            for (Where subWhere : where.getWheres()) {
                explanWhere(subQuery, subWhere);
            }
        }
    }

    /**
     * 增加嵌套插
     *
     * @param boolQuery
     * @param where
     * @param subQuery
     */
    private void addSubQuery(BoolQueryBuilder boolQuery, Where where, QueryBuilder subQuery) {
        if (where instanceof Condition) {
            Condition condition = (Condition) where;

            if (condition.isNested()) {
                // bugfix #628
                if ("missing".equalsIgnoreCase(String.valueOf(condition.getValue()))
                        && (condition.getOPERATOR() == Condition.OPERATOR.IS
                        || condition.getOPERATOR() == Condition.OPERATOR.EQ)) {
                    boolQuery.mustNot(QueryBuilders.nestedQuery(condition.getNestedPath(),
                            QueryBuilders.boolQuery().mustNot(subQuery), ScoreMode.None));
                    return;
                }

                subQuery = QueryBuilders.nestedQuery(condition.getNestedPath(), subQuery, ScoreMode.None);
            } else if (condition.isChildren()) {
                subQuery = JoinQueryBuilders.hasChildQuery(condition.getChildType(), subQuery, ScoreMode.None);
            }
        }

        if (where.getConn() == Where.CONN.AND) {
            boolQuery.must(subQuery);
        } else {
            boolQuery.should(subQuery);
        }
    }
}
