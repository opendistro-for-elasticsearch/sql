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


import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.hitAny;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvDouble;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.kvString;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;


/**
 * Created by allwefantasy on 8/25/16.
 */
public class SQLFunctionsIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void functionFieldAliasAndGroupByAlias() throws Exception {
        String query = "SELECT " +
                "floor(substring(address,0,3)*20) as key," +
                "sum(age) cvalue FROM " + TEST_INDEX_ACCOUNT + "/account where address is not null " +
                "group by key order by cvalue desc limit 10  ";
        final JSONObject result = executeQuery(query);


        IntStream.rangeClosed(0, 9).forEach(i -> {
                    Assert.assertNotNull(result.query(String.format("/aggregations/key/buckets/%d/key", i)));
                    Assert.assertNotNull(result.query(String.format("/aggregations/key/buckets/%d/cvalue/value", i)));
                }
        );
    }

    /**
     * Commented out the following test as alias for normal fields is no longer stored in SearchHit's fields object
     */
//    @Test
//    public void normalFieldAlias() throws Exception {
//
//        //here is a bug,csv field with spa
//        String query = "SELECT " +
//                "address as key,age from " +
//                TEST_INDEX_ACCOUNT + "/account where address is not null " +
//                "limit 10  ";
//
//        CSVResult csvResult = getCsvResult(false, query);
//        List<String> headers = csvResult.getHeaders();
//        Assert.assertTrue(headers.contains("key"));
//    }


//        CSVResult csvResult = getCsvResult(false, query);
//        List<String> headers = csvResult.getHeaders();
//        List<String> contents = csvResult.getLines();
//        String[] splits = contents.get(0).split(",");
//        Assert.assertTrue(splits[0].endsWith("--") || splits[1].endsWith("--"));

    @Test
    public void functionAlias() throws Exception {
        //here is a bug,if only script fields are included,then all fields will return; fix later
        String query = "SELECT " +
                "substring(address,0,3) as key,address from " +
                TEST_INDEX_ACCOUNT + "/account where address is not null " +
                "order by address desc limit 10  ";

        assertThat(
                executeQuery(query),
                hitAny(both(kvString("/_source/address", equalTo("863 Wythe Place"))).and(kvString("/fields/key/0",
                        equalTo("863"))))
        );
    }

    @Test
    public void concat_ws_field_and_string() throws Exception {
        //here is a bug,csv field with spa
        String query = "SELECT " +
                " concat_ws('-',age,'-') as age,address from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " limit 10  ";

        assertThat(
                executeQuery(query),
                hitAny(kvString("/fields/age/0", endsWith("--")))
        );
    }

// todo: add the assert phase
//    @Test
//    public void test() throws Exception {
//
//        String query = "select sum(case \n" +
//                "             when traffic=0 then 100 \n" +
//                "             when traffic=1 then 1000 \n" +
//                "             else 10000 \n" +
//                "       end) as tf,date_format(5minute,'yyyyMMddHHmm') as nt from %s where business_line='2'   group by nt order by tf asc limit 10";
//        query = String.format(query, TEST_INDEX_PHRASE);
//
//        SearchDao searchDao = MainTestSuite.getSearchDao() != null ? MainTestSuite.getSearchDao() : getSearchDao();
//        System.out.println(searchDao.explain(query).explain().explain());
//    }

// todo: change when split is back on language
//    @Test
//    public void whereConditionLeftFunctionRightVariableEqualTest() throws Exception {
//
//        String query = "SELECT " +
//                " * from " +
//                TestsConstants.TEST_INDEX + "/account " +
//                " where split(address,' ')[0]='806' limit 1000  ";
//
//        CSVResult csvResult = getCsvResult(false, query);
//        List<String> contents = csvResult.getLines();
//        Assert.assertTrue(contents.size() == 4);
//    }
//
//    @Test
//    public void whereConditionLeftFunctionRightVariableGreatTest() throws Exception {
//
//        String query = "SELECT " +
//                " * from " +
//                TestsConstants.TEST_INDEX + "/account " +
//                " where floor(split(address,' ')[0]+0) > 805 limit 1000  ";
//
//        SearchDao searchDao = MainTestSuite.getSearchDao() != null ? MainTestSuite.getSearchDao() : getSearchDao();
//        System.out.println(searchDao.explain(query).explain().explain());
//
//        CSVResult csvResult = getCsvResult(false, query);
//        List<String> contents = csvResult.getLines();
//        Assert.assertTrue(contents.size() == 223);
//    }

    @Test
    public void concat_ws_fields() throws Exception {

        //here is a bug,csv field with spa
        String query = "SELECT " +
                " concat_ws('-',age,address) as combine,address from " +
                TEST_INDEX_ACCOUNT + "/account " +
                " limit 10  ";
        assertThat(
                executeQuery(query),
                hitAny(kvString("/fields/combine/0", containsString("-")))
        );
    }

    @Test
    public void functionLogs() throws Exception {
        String query = "SELECT log10(100) as a, log(1) as b, log(2, 4) as c, log2(8) as d from "
                + TEST_INDEX_ACCOUNT + "/account limit 1";

        assertThat(
                executeQuery(query),
                hitAny(both(kvDouble("/fields/a/0", equalTo(2.0)))
                        .and(kvDouble("/fields/b/0", equalTo(0.0)))
                        .and(kvDouble("/fields/c/0", equalTo(1.0)))
                        .and(kvDouble("/fields/d/0", equalTo(3.0))))
        );
    }

    @Test
    public void functionPow() throws Exception {
        String query = "SELECT pow(account_number, 2) as key,"+
                "abs(age - 60) as new_age from " + TEST_INDEX_ACCOUNT + "/account WHERE firstname = 'Virginia' and lastname='Ayala' limit 1";

        assertThat(
                executeQuery(query),
                hitAny(both(kvDouble("/fields/new_age/0", equalTo(21.0))).and(kvDouble("/fields/key/0", equalTo(625.0))))
        );
    }

//    // todo: change when split is back on language
//    @Test
//    public void split_field() throws Exception {
//
//        //here is a bug,csv field with spa
//        String query = "SELECT " +
//                " split(address,' ')[0],age from " +
//                TestsConstants.TEST_INDEX + "/account where address is not null " +
//                " limit 10  ";
//        SearchDao searchDao = MainTestSuite.getSearchDao() != null ? MainTestSuite.getSearchDao() : getSearchDao();
//        System.out.println(searchDao.explain(query).explain().explain());
//
//        CSVResult csvResult = getCsvResult(false, query);
//        List<String> headers = csvResult.getHeaders();
//        List<String> contents = csvResult.getLines();
//        String[] splits = contents.get(0).split(",");
//        Assert.assertTrue(headers.size() == 2);
//        Assert.assertTrue(Integer.parseInt(splits[0]) > 0);
//    }
}
