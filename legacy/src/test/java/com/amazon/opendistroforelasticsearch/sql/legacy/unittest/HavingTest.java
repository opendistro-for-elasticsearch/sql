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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.MethodField;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.AggMaker;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.BucketSelectorPipelineAggregationBuilder;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.HasFieldWithValue.hasFieldWithValue;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;


public class HavingTest {

    private static final String SELECT_CNT = "SELECT COUNT(*) as c ";
    private static final String SELECT_CNT_AVG = "SELECT COUNT(*) as c, AVG(age) as a ";
    private static final String SELECT_CNT_AVG_SUM = "SELECT COUNT(*) as c, AVG(age) as a, SUM(income) as i ";
    private static final String FROM_BANK = "FROM bank ";
    private static final String GROUP_BY_AGE = "GROUP BY age ";
    private static final String SELECT_CNT_FROM_BANK_GROUP_BY_AGE = SELECT_CNT + FROM_BANK + GROUP_BY_AGE;
    private static final String SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE = SELECT_CNT_AVG + FROM_BANK + GROUP_BY_AGE;
    private static final String SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE = SELECT_CNT_AVG_SUM + FROM_BANK + GROUP_BY_AGE;
    private static final String NESTED_SELECT_COUNT = "SELECT COUNT(nested(income, 'income')) as c ";
    private static final String NESTED_SELECT_CNT_FROM_BANK_GROUP_BY_AGE = NESTED_SELECT_COUNT + FROM_BANK + GROUP_BY_AGE;

    @Test
    public void singleCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a > 30"),
            contains(
                bucketSelector(
                    hasBucketPath("c: c", "a: a"),
                    hasScript("params.a > 30")
                )
            ));
    }

    @Ignore
    @Test
    public void singleConditionWithTwoAggExpr() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a > c"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: c", "a: a"),
                                hasScript("params.a > params.c")
                        )
                ));
    }

    @Test
    public void singleConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(age) > 30"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: c", "a: a", "avg_0: avg_0"),
                                hasScript("params.avg_0 > 30")
                        )
                ));
    }

    @Ignore
    @Test
    public void singleConditionWithHavingTwoAggExpr() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(age) > COUNT(*)"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: c", "a: a", "avg_0: avg_0", "count_0: count_0"),
                                hasScript("params.avg_0 > count_0")
                        )
                ));
    }

    @Test
    public void nestedSingleCondition() {
        assertThat(
                query(NESTED_SELECT_CNT_FROM_BANK_GROUP_BY_AGE + "HAVING c > 30"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: income@NESTED.c"),
                                hasScript("params.c > 30")
                        )
                ));
    }

    @Test
    public void singleConditionWithOneFieldInSelect() {
        assertThat(
            query(SELECT_CNT_FROM_BANK_GROUP_BY_AGE + "HAVING a > 30"),
            contains(
                bucketSelector(
                    hasBucketPath("c: c")
                )
            ));
    }

    @Test
    public void singleConditionWithOneFieldInSelectWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) > 30"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: c", "avg_0: avg_0"),
                                hasScript("params.avg_0 > 30")
                        )
                ));
    }

    @Test
    public void singleConditionWithThreeFieldsInSelect() {
        assertThat(
            query(SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE + "HAVING a > 30"),
            contains(
                bucketSelector(
                    hasBucketPath("c: c", "a: a", "i: i")
                )
            ));
    }

    @Test
    public void singleConditionWithThreeFieldsInSelectWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) > 30"),
                contains(
                        bucketSelector(
                                hasBucketPath("c: c", "a: a", "i: i", "avg_0: avg_0"),
                                hasScript("params.avg_0 > 30")
                        )
                ));
    }

    @Test
    public void notEqualCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a <> 30"),
            contains(
                bucketSelector(
                    hasScript("params.a != 30")
                )
            ));
    }

    @Test
    public void notEqualConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) <> 30"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 != 30")
                        )
                ));
    }

    @Test
    public void notCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING NOT (a > 30)"),
            contains(
                bucketSelector(
                    hasScript("params.a <= 30")
                )
            ));
    }

    @Test
    public void notConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING NOT (AVG(a) > 30)"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 <= 30")
                        )
                ));
    }

    @Test
    public void andConditions() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a > 30 AND c <= 10"),
            contains(
                bucketSelector(
                    hasScript("params.a > 30 && params.c <= 10")
                )
            ));
    }

    @Test
    public void andConditionsWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) > 30 AND SUM(c) <= 10"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 > 30 && params.sum_1 <= 10")
                        )
                ));
    }

    @Test
    public void orConditions() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a > 30 OR c <= 10"),
            contains(
                bucketSelector(
                    hasScript("params.a > 30 || params.c <= 10")
                )
            ));
    }

    @Test
    public void orConditionsWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) > 30 OR SUM(c) <= 10"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 > 30 || params.sum_1 <= 10")
                        )
                ));
    }

    @Test
    public void betweenCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a BETWEEN 30 AND 50"),
            contains(
                bucketSelector(
                    hasScript("params.a >= 30 && params.a <= 50")
                )
            ));
    }

    @Test
    public void betweenConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) BETWEEN 30 AND 50"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 >= 30 && params.avg_0 <= 50")
                        )
                ));
    }

    @Test
    public void notBetweenCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a NOT BETWEEN 30 AND 50"),
            contains(
                bucketSelector(
                    hasScript("params.a < 30 || params.a > 50")
                )
            ));
    }

    @Test
    public void notBetweenConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) NOT BETWEEN 30 AND 50"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 < 30 || params.avg_0 > 50")
                        )
                ));
    }

    @Test
    public void inCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a IN (30, 40, 50)"),
            contains(
                bucketSelector(
                    hasScript("params.a == 30 || params.a == 40 || params.a == 50")
                )
            ));
    }

    @Test
    public void inConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) IN (30, 40, 50)"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 == 30 || params.avg_0 == 40 || params.avg_0 == 50")
                        )
                ));
    }

    @Test
    public void notInCondition() {
        assertThat(
            query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING a NOT IN (30, 40, 50)"),
            contains(
                bucketSelector(
                    hasScript("params.a != 30 && params.a != 40 && params.a != 50")
                )
            ));
    }

    @Test
    public void notInConditionWithHavingAgg() {
        assertThat(
                query(SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(a) NOT IN (30, 40, 50)"),
                contains(
                        bucketSelector(
                                hasScript("params.avg_0 != 30 && params.avg_0 != 40 && params.avg_0 != 50")
                        )
                ));
    }

    @Test
    public void nestedConditions() {
        assertThat(
            query(SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE + "HAVING i <= 10000 OR NOT (a < 10 OR a > 30) AND c <= 10"),
            contains(
                bucketSelector(
                    hasScript("params.i <= 10000 || ((params.a >= 10 && params.a <= 30) && params.c <= 10)")
                )
            ));
    }

    @Test(expected = ParserException.class)
    public void aggregationFunctionOnTheRight() {
        query(SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE + "HAVING 10 < a");
    }

    private Collection<PipelineAggregationBuilder> query(String sql) {
        return translate(SqlParserUtils.parse(sql));
    }

    private Collection<PipelineAggregationBuilder> translate(SQLQueryExpr expr) {
        try {
            Select select = new SqlParser().parseSelect(expr);
            select.getFields().forEach(field -> {
                try {
                    new AggMaker()
                            .withWhere(select.getWhere())
                            .makeFieldAgg((MethodField) field, AggregationBuilders.terms(""));
                } catch (SqlParseException e) {
                    throw new RuntimeException(e);
                }
            });
            AggregationBuilder agg = AggregationBuilders.terms("");
            select.getHaving().explain(agg, select.getFields());
            return agg.getPipelineAggregations();
        } catch (SqlParseException e) {
            throw new ParserException("Illegal sql expr: " + expr.toString());
        }
    }

    @SafeVarargs
    private final Matcher<PipelineAggregationBuilder> bucketSelector(Matcher<PipelineAggregationBuilder>... matchers) {
        return both(Matchers.<PipelineAggregationBuilder>   // instanceOf() has type inference problem
                    instanceOf(BucketSelectorPipelineAggregationBuilder.class)
               ).
               and(allOf(matchers));
    }

    private Matcher<PipelineAggregationBuilder> hasBucketPath(String... expectedBucketPath) {
        Map<String, String> expectedMap = Arrays.stream(expectedBucketPath).
                                                 map(e -> e.split(":")).
                                                 collect(toMap(e -> e[0].trim(), e -> e[1].trim()));
        return hasFieldWithValue("bucketsPathsMap", "has bucket path", is(expectedMap));
    }

    private Matcher<PipelineAggregationBuilder> hasScript(String expectedCode) {
        return hasFieldWithValue("script", "has script", is(new Script(expectedCode)));
    }
}

