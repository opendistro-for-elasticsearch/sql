package org.nlpcn.es4sql.intgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.valuecount.ValueCount;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;


public class HavingTest {

    private static final String SELECT_FROM_WHERE_GROUP_BY =
            "SELECT state, COUNT(*) cnt " +
            "  FROM " + TEST_INDEX_ACCOUNT + "/account " +
            "    WHERE age = 30 " +
            "      GROUP BY state ";

    private static Set<Matcher<Object[]>> states1;
    private static Set<Matcher<Object[]>> states2;
    private static Set<Matcher<Object[]>> states3;

    @Before
    public void setUp() {
        states1 = rowSet(1, Arrays.asList(
            "AK", "AR", "CT", "DE", "HI", "IA", "IL", "IN", "LA", "MA", "MD", "MN",
            "MO", "MT", "NC", "ND", "NE", "NH", "NJ", "NV", "SD", "VT", "WV", "WY"
        ));
        states2 = rowSet(2, Arrays.asList("AZ", "DC", "KS", "ME"));
        states3 = rowSet(3, Arrays.asList("AL", "ID", "KY", "OR", "TN"));
    }

    @Test
    public void equalsTo() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt = 2"),
            resultSet(
                states2
            )
        );
    }

    @Test
    public void lessThanOrEqual() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt <= 2"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void notEqualsTo() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt <> 2"),
            resultSet(
                states1,
                states3
            )
        );
    }

    @Test
    public void between() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt BETWEEN 1 AND 2"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void notBetween() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt NOT BETWEEN 1 AND 2"),
            resultSet(
                states3
            )
        );
    }

    @Test
    public void in() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt IN (2, 3)"),
            resultSet(
                states2,
                states3
            )
        );
    }

    @Test
    public void notIn() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt NOT IN (2, 3)"),
            resultSet(
                states1
            )
        );
    }

    @Test
    public void and() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt >= 1 AND cnt < 3"),
            resultSet(
                states1,
                states2
            )
        );
    }

    @Test
    public void or() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING cnt = 1 OR cnt = 3"),
            resultSet(
                states1,
                states3
            )
        );
    }

    @Test
    public void not() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING NOT cnt >= 2"),
            resultSet(
                states1
            )
        );
    }

    @Test
    public void notAndOr() {
        assertThat(
            query(SELECT_FROM_WHERE_GROUP_BY + "HAVING NOT (cnt > 0 AND cnt <= 2)"),
            resultSet(
                states3
            )
        );
    }

    private Set<Object[]> query(String query) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(query).explain();
            return getResult((SearchResponse) select.get(), "state", "cnt");
        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Object[]> getResult(SearchResponse resp, String aggName, String aggFunc) {
        Aggregations aggs = resp.getAggregations();
        Terms agg = aggs.get(aggName);
        Set<Object[]> result = new HashSet<>();
        for (Terms.Bucket bucket : agg.getBuckets()) {
            result.add(new Object[]{
                bucket.getKey().toString(),
                ((ValueCount) bucket.getAggregations().get(aggFunc)).getValue()
            });
        }
        return result;
    }

    private Set<Matcher<Object[]>> rowSet(long count, List<String> states) {
        return states.stream().
                      map(state -> row(state, count)).
                      collect(Collectors.toSet());
    }

    @SafeVarargs
    private final Matcher<Iterable<? extends Object[]>> resultSet(Set<Matcher<Object[]>>... rowSets) {
        return containsInAnyOrder(Arrays.stream(rowSets).
                                         flatMap(Collection::stream).
                                         collect(Collectors.toList()));
    }

    private Matcher<Object[]> row(String state, long count) {
        return arrayContaining(is(state.toLowerCase()), is(count));
    }
}
