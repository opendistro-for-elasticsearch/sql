package org.nlpcn.es4sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;

public class MathFunctionsTest {

    private static final String FROM = "FROM " + TEST_INDEX_ACCOUNT + "/account";

    @Test
    public void lowerCaseFunctionCall() {
        SearchHit[] hits = query(
                "SELECT abs(age - 100) AS abs"
        );
        for (SearchHit hit : hits) {
            double abs = (double) getField(hit, "abs");
            assertThat(abs, greaterThanOrEqualTo(0.0));
        }
    }

    @Test
    public void upperCaseFunctionCall() {
        SearchHit[] hits = query(
                "SELECT ABS(age - 100) AS abs"
        );
        for (SearchHit hit : hits) {
            double abs = (double) getField(hit, "abs");
            assertThat(abs, greaterThanOrEqualTo(0.0));
        }
    }

    @Test
    public void eulersNumber() {
        SearchHit[] hits = query(
                "SELECT E() AS e"
        );
        double e = (double) getField(hits[0], "e");
        assertThat(e, equalTo(Math.E));
    }

    @Test
    public void pi() {
        SearchHit[] hits = query(
                "SELECT PI() AS pi"
        );
        double pi = (double) getField(hits[0], "pi");
        assertThat(pi, equalTo(Math.PI));
    }

    @Test
    public void expm1Function() {
        SearchHit[] hits = query(
                "SELECT EXPM1(2) AS expm1"
        );
        double expm1 = (double) getField(hits[0], "expm1");
        assertThat(expm1, equalTo(Math.expm1(2)));
    }

    @Test
    public void degreesFunction() {
        SearchHit[] hits = query(
                "SELECT age, DEGREES(age) AS degrees"
        );
        for (SearchHit hit : hits) {
            int age = (int) getFieldFromSource(hit, "age");
            double degrees = (double) getField(hit, "degrees");
            assertThat(degrees, equalTo(Math.toDegrees(age)));
        }
    }

    @Test
    public void radiansFunction() {
        SearchHit[] hits = query(
                "SELECT age, RADIANS(age) as radians"
        );
        for (SearchHit hit : hits) {
            int age = (int) getFieldFromSource(hit, "age");
            double radians = (double) getField(hit, "radians");
            assertThat(radians, equalTo(Math.toRadians(age)));
        }
    }

    @Test
    public void sin() {
        SearchHit[] hits = query(
                "SELECT SIN(PI()) as sin"
        );
        double sin = (double) getField(hits[0], "sin");
        assertThat(sin, equalTo(Math.sin(Math.PI)));
    }

    @Test
    public void asin() {
        SearchHit[] hits = query(
                "SELECT ASIN(PI()) as asin"
        );
        double asin = (double) getField(hits[0], "asin");
        assertThat(asin, equalTo(Math.asin(Math.PI)));
    }

    @Test
    public void sinh() {
        SearchHit[] hits = query(
                "SELECT SINH(PI()) as sinh"
        );
        double sinh = (double) getField(hits[0], "sinh");
        assertThat(sinh, equalTo(Math.sinh(Math.PI)));
    }

    private SearchHit[] query(String select, String... statements) {
        return execute(select + " " + FROM + " " + String.join(" ", statements));
    }

    private SearchHit[] execute(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(sql).explain();
            return ((SearchResponse) select.get()).getHits().getHits();
        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new ParserException("Unable to parse query: " + sql);
        }

    }

    private Object getField(SearchHit hit, String fieldName) {
        return hit.field(fieldName).getValue();
    }

    private Object getFieldFromSource(SearchHit hit, String fieldName) {
        return hit.getSourceAsMap().get(fieldName);
    }
}
