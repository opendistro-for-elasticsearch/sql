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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.utils;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.unquoteFullColumn;
import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.unquoteSingleField;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * To test the functionality of {@link StringUtils#unquoteSingleField}
 * and {@link StringUtils#unquoteFullColumn(String, String)}
 */
public class BackticksUnquoterTest {

    @Test
    public void assertNotQuotedStringShouldKeepTheSame() {
        assertThat(unquoteSingleField("identifier"), equalTo("identifier"));
        assertThat(unquoteFullColumn("identifier"), equalTo("identifier"));
    }

    @Test
    public void assertStringWithOneBackTickShouldKeepTheSame() {
        assertThat(unquoteSingleField("`identifier"), equalTo("`identifier"));
        assertThat(unquoteFullColumn("`identifier"), equalTo("`identifier"));
    }

    @Test
    public void assertBackticksQuotedStringShouldBeUnquoted() {
        assertThat("identifier", equalTo(unquoteSingleField("`identifier`")));

        assertThat("identifier1.identifier2", equalTo(unquoteFullColumn("`identifier1`.`identifier2`")));
        assertThat("identifier1.identifier2", equalTo(unquoteFullColumn("`identifier1`.identifier2")));
    }
}