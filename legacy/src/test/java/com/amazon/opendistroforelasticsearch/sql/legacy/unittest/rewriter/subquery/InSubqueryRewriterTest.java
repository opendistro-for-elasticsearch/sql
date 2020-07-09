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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.subquery;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class InSubqueryRewriterTest extends SubQueryRewriterTestBase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void nonCorrleatedIn() throws Exception {
        assertEquals(
                sqlString(expr(
                        "SELECT TbA_0.* " +
                        "FROM TbA as TbA_0 " +
                        "JOIN TbB as TbB_1 " +
                        "ON TbA_0.a = TbB_1.b " +
                        "WHERE TbB_1.b IS NOT NULL")),
                sqlString(rewrite(expr(
                        "SELECT * FROM TbA " +
                        "WHERE a in (SELECT b FROM TbB)")))
        );
    }

    @Test
    public void nonCorrleatedInWithWhere() throws Exception {
        assertEquals(
                sqlString(expr(
                        "SELECT TbA_0.* " +
                        "FROM TbA as TbA_0 " +
                        "JOIN TbB as TbB_1 " +
                        "ON TbA_0.a = TbB_1.b " +
                        "WHERE TbB_1.b IS NOT NULL AND TbB_1.b > 0")),
                sqlString(rewrite(expr(
                        "SELECT * " +
                        "FROM TbA " +
                        "WHERE a in (SELECT b FROM TbB WHERE b > 0)")))
        );
    }

    @Test
    public void nonCorrleatedInWithOuterWhere() throws Exception {
        assertEquals(
                sqlString(expr(
                        "SELECT TbA_0.* " +
                        "FROM TbA as TbA_0 " +
                        "JOIN TbB as TbB_1 " +
                        "ON TbA_0.a = TbB_1.b " +
                        "WHERE TbB_1.b IS NOT NULL AND TbA_0.a > 10")),
                sqlString(rewrite(expr(
                        "SELECT * " +
                        "FROM TbA " +
                        "WHERE a in (SELECT b FROM TbB) AND a > 10")))
        );
    }


    @Test
    public void notInUnsupported() throws Exception {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Unsupported subquery");
        rewrite(expr(
                "SELECT * FROM TbA " +
                "WHERE a not in (SELECT b FROM TbB)"));
    }

    @Test
    public void testMultipleSelectException() throws Exception {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Unsupported subquery with multiple select [TbB_1.b1, TbB_1.b2]");
        rewrite(expr(
                "SELECT * " +
                "FROM TbA " +
                "WHERE a in (SELECT b1, b2 FROM TbB) AND a > 10"));
    }
}