package org.nlpcn.es4sql.unittest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.bucketselector.BucketSelectorPipelineAggregationBuilder;
import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ElasticSqlExprParser;
import org.nlpcn.es4sql.parse.SqlParser;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.nlpcn.es4sql.unittest.HasFieldWithValue.hasFieldWithValue;


public class HavingTest {

    private static final String SELECT_CNT = "SELECT COUNT(*) as c ";
    private static final String SELECT_CNT_AVG = "SELECT COUNT(*) as c, AVG(age) as a ";
    private static final String SELECT_CNT_AVG_SUM = "SELECT COUNT(*) as c, AVG(age) as a, SUM(income) as i ";
    private static final String FROM_BANK = "FROM bank ";
    private static final String GROUP_BY_AGE = "GROUP BY age ";
    private static final String SELECT_CNT_FROM_BANK_GROUP_BY_AGE = SELECT_CNT + FROM_BANK + GROUP_BY_AGE;
    private static final String SELECT_CNT_AVG_FROM_BANK_GROUP_BY_AGE = SELECT_CNT_AVG + FROM_BANK + GROUP_BY_AGE;
    private static final String SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE = SELECT_CNT_AVG_SUM + FROM_BANK + GROUP_BY_AGE;

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

    @Test(expected = ParserException.class)
    public void aggregationFunctionInHavingRatherThanAlias() {
        query(SELECT_CNT_AVG_SUM_FROM_BANK_GROUP_BY_AGE + "HAVING AVG(age) > 10");
    }

    private List<PipelineAggregationBuilder> query(String sql) {
        return translate(parseSql(sql));
    }

    private List<PipelineAggregationBuilder> translate(SQLQueryExpr expr) {
        try {
            Select select = new SqlParser().parseSelect(expr);
            AggregationBuilder agg = AggregationBuilders.terms("");
            select.getHaving().explain(agg, select.getFields());
            return agg.getPipelineAggregations();
        } catch (SqlParseException e) {
            throw new ParserException("Illegal sql expr: " + expr.toString());
        }
    }

    private SQLQueryExpr parseSql(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
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

/**
 * A matcher for private field value extraction along with matcher to assert its value.
 *
 * @param <T>   Type of target (actual) object
 * @param <U>   Type of field member (feature) extracted from target object by reflection
 */
class HasFieldWithValue<T, U> extends FeatureMatcher<T, U> {

    private final String fieldName;

    /**
     * Construct a matcher. Reordered the argument list.
     *
     * @param name      Identifying text for mismatch message
     * @param desc      Descriptive text to use in describeTo
     * @param matcher   The matcher to apply to the feature
     */
    private HasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        super(matcher, desc, name);
        this.fieldName = name;
    }

    @Factory
    static <T, U> HasFieldWithValue<T, U> hasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        return new HasFieldWithValue<>(name, desc, matcher);
    }

    @Override
    protected U featureValueOf(T targetObj) {
        return getFieldValue(targetObj, fieldName);
    }

    @SuppressWarnings("unchecked")
    private U getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (U) field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}

