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

/**
 * Created by omershelef on 18/12/14.
 */
public class TestsConstants {

    public final static String TEST_INDEX = "elasticsearch-sql_test_index";

    public final static String TEST_INDEX_ONLINE = TEST_INDEX + "_online";
    public final static String TEST_INDEX_ACCOUNT = TEST_INDEX + "_account";
    public final static String TEST_INDEX_ACCOUNT_TEMP = TEST_INDEX + "_account_temp";
    public final static String TEST_INDEX_PHRASE = TEST_INDEX + "_phrase";
    public final static String TEST_INDEX_DOG = TEST_INDEX + "_dog";
    public final static String TEST_INDEX_DOG2 = TEST_INDEX + "_dog2";
    public final static String TEST_INDEX_PEOPLE = TEST_INDEX + "_people";
    public final static String TEST_INDEX_PEOPLE2 = TEST_INDEX + "_people2";
    public final static String TEST_INDEX_GAME_OF_THRONES = TEST_INDEX + "_game_of_thrones";
    public final static String TEST_INDEX_SYSTEM = TEST_INDEX + "_system";
    public final static String TEST_INDEX_ODBC = TEST_INDEX + "_odbc";
    public final static String TEST_INDEX_LOCATION = TEST_INDEX + "_location";
    public final static String TEST_INDEX_LOCATION2 = TEST_INDEX + "_location2";
    public final static String TEST_INDEX_NESTED_TYPE = TEST_INDEX + "_nested_type";
    public final static String TEST_INDEX_JOIN_TYPE = TEST_INDEX + "_join_type";
    public final static String TEST_INDEX_BANK = TEST_INDEX + "_bank";
    public final static String TEST_INDEX_BANK_TWO = TEST_INDEX_BANK + "_two";

    public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public final static String TS_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";
}
