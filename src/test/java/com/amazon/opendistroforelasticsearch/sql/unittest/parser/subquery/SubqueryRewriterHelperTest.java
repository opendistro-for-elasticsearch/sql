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

package com.amazon.opendistroforelasticsearch.sql.unittest.parser.subquery;

import com.amazon.opendistroforelasticsearch.sql.parser.subquery.SubqueryRewriterHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.SQLFeatureNotSupportedException;

import static com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils.parse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubqueryRewriterHelperTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void isSubquery() throws Exception {
        assertTrue(SubqueryRewriterHelper.isSubquery(parse(
                "SELECT * " +
                        "FROM A " +
                        "WHERE a IN (SELECT b FROM B)")));
    }

    @Test
    public void notSubquery() throws Exception {
        assertFalse(SubqueryRewriterHelper.isSubquery(parse(
                "SELECT * FROM A")));
    }

    @Test
    public void notSupportedSubquery() throws Exception {
        exceptionRule.expect(SQLFeatureNotSupportedException.class);
        exceptionRule.expectMessage("Unsupported subquery");
        assertFalse(SubqueryRewriterHelper.isSubquery(parse(
                "SELECT * " +
                        "FROM A " +
                        "WHERE a NOT IN (SELECT b FROM B)")));
    }
}
