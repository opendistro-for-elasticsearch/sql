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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

public class MathFunctionsIT extends SQLIntegTestCase {

    private static final String FROM = "FROM " + TestsConstants.TEST_INDEX_ACCOUNT + "/account";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void lowerCaseFunctionCall() throws IOException {
        SearchHit[] hits = query(
                "SELECT abs(age - 100) AS abs"
        );
        for (SearchHit hit : hits) {
            double abs = (double) getField(hit, "abs");
            assertThat(abs, greaterThanOrEqualTo(0.0));
        }
    }

    @Test
    public void upperCaseFunctionCall() throws IOException {
        SearchHit[] hits = query(
                "SELECT ABS(age - 100) AS abs"
        );
        for (SearchHit hit : hits) {
            double abs = (double) getField(hit, "abs");
            assertThat(abs, greaterThanOrEqualTo(0.0));
        }
    }

    @Test
    public void eulersNumber() throws IOException {
        SearchHit[] hits = query(
                "SELECT E() AS e"
        );
        double e = (double) getField(hits[0], "e");
        assertThat(e, equalTo(Math.E));
    }

    @Test
    public void pi() throws IOException {
        SearchHit[] hits = query(
                "SELECT PI() AS pi"
        );
        double pi = (double) getField(hits[0], "pi");
        assertThat(pi, equalTo(Math.PI));
    }

    @Test
    public void expm1Function() throws IOException {
        SearchHit[] hits = query(
                "SELECT EXPM1(2) AS expm1"
        );
        double expm1 = (double) getField(hits[0], "expm1");
        assertThat(expm1, equalTo(Math.expm1(2)));
    }

    @Test
    public void degreesFunction() throws IOException {
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
    public void radiansFunction() throws IOException {
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
    public void sin() throws IOException {
        SearchHit[] hits = query(
                "SELECT SIN(PI()) as sin"
        );
        double sin = (double) getField(hits[0], "sin");
        assertThat(sin, equalTo(Math.sin(Math.PI)));
    }

    @Test
    public void asin() throws IOException {
        SearchHit[] hits = query(
                "SELECT ASIN(PI()) as asin"
        );
        double asin = Double.valueOf((String) getField(hits[0], "asin"));
        assertThat(asin, equalTo(Math.asin(Math.PI)));
    }

    @Test
    public void sinh() throws IOException {
        SearchHit[] hits = query(
                "SELECT SINH(PI()) as sinh"
        );
        double sinh = (double) getField(hits[0], "sinh");
        assertThat(sinh, equalTo(Math.sinh(Math.PI)));
    }

    @Test
    public void power() throws IOException {
        SearchHit[] hits = query(
                "SELECT POWER(age, 2) AS power",
                "WHERE (age IS NOT NULL) AND (balance IS NOT NULL) and (POWER(balance, 3) > 0)"
        );
        double power = (double) getField(hits[0], "power");
        assertTrue(power >= 0);
    }

    @Test
    public void atan2() throws IOException {
        SearchHit[] hits = query(
                "SELECT ATAN2(age, age) AS atan2",
                "WHERE (age IS NOT NULL) AND (ATAN2(age, age) > 0)"
        );
        double atan2 = (double) getField(hits[0], "atan2");
        assertThat(atan2, equalTo(Math.atan2(1, 1)));
    }

    @Test
    public void cot() throws IOException {
        SearchHit[] hits = query(
                "SELECT COT(PI()) AS cot"
        );
        double cot = (double) getField(hits[0], "cot");
        assertThat(cot, closeTo(1 / Math.tan(Math.PI), 0.001));
    }

    @Test
    public void sign() throws IOException {
        SearchHit[] hits = query(
                "SELECT SIGN(E()) AS sign"
        );
        double sign = (double) getField(hits[0], "sign");
        assertThat(sign, equalTo(Math.signum(Math.E)));
    }

    private SearchHit[] query(String select, String... statements) throws IOException {
        final String response = executeQueryWithStringOutput(select + " " + FROM + " " + String.join(" ", statements));

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
                NamedXContentRegistry.EMPTY,
                LoggingDeprecationHandler.INSTANCE,
                response);
        return SearchResponse.fromXContent(parser).getHits().getHits();
    }

    private Object getField(SearchHit hit, String fieldName) {
        return hit.field(fieldName).getValue();
    }

    private Object getFieldFromSource(SearchHit hit, String fieldName) {
        return hit.getSourceAsMap().get(fieldName);
    }
}
