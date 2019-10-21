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

package com.amazon.opendistroforelasticsearch.sql.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.utils.BackticksUnquoter;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class BackticksUnquoterTest {

    @Test
    public void assertNotQuotedStringShouldKeepTheSame() {
        final String originalText = "identifier";
        final String resultForUnquotingSingleField = new BackticksUnquoter().unquoteSingleField(originalText);
        final String resultForUnquotingFullColumn = new BackticksUnquoter().unquoteFullColumn(originalText);

        assertThat(resultForUnquotingSingleField, equalTo(originalText));
        assertThat(resultForUnquotingFullColumn, equalTo(originalText));
    }

    @Test
    public void assertStringWithOneBackTickShouldKeepTheSame() {
        final String originalText = "`identifier";
        final String result1 = new BackticksUnquoter().unquoteSingleField(originalText);
        final String result2 = new BackticksUnquoter().unquoteFullColumn(originalText);

        assertThat(result1, equalTo(originalText));
        assertThat(result2, equalTo(originalText));
    }

    @Test
    public void assertBackticksQuotedStringShouldBeUnquoted() {
        final String originalText1 = "`identifier`";
        final String originalText2 = "`identifier1`.`identifier2`";

        final String expectedResult1 = "identifier";
        final String expectedResult2 = "identifier1.identifier2";

        final String result1 = new BackticksUnquoter().unquoteSingleField(originalText1);
        final String result2 = new BackticksUnquoter().unquoteFullColumn(originalText2);

        assertThat(expectedResult1, equalTo(result1));
        assertThat(expectedResult2, equalTo(result2));
    }
}