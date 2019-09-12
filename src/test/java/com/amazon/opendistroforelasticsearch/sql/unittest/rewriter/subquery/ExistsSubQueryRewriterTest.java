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

package com.amazon.opendistroforelasticsearch.sql.unittest.rewriter.subquery;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class ExistsSubQueryRewriterTest extends SubQueryRewriterTestBase {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void nonCorrlatedExists() {
        assertEquals(
                sqlString(expr(
                        "SELECT e.name " +
                        "FROM employee e, e.projects p " +
                        "WHERE p IS NOT NULL")),
                sqlString(rewrite(expr(
                        "SELECT e.name " +
                        "FROM employee as e, e.projects as p " +
                        "WHERE EXISTS (SELECT * FROM p)")))
        );
    }

    @Test
    public void nonCorrlatedExistsWhere() {
        assertEquals(
                sqlString(expr(
                        "SELECT e.name " +
                        "FROM employee e, e.projects p " +
                        "WHERE p IS NOT NULL AND p.name LIKE 'security'")),
                sqlString(rewrite(expr(
                        "SELECT e.name " +
                        "FROM employee as e, e.projects as p " +
                        "WHERE EXISTS (SELECT * FROM p WHERE p.name LIKE 'security')")))
        );
    }

    @Test
    public void nonCorrlatedExistsParentWhere() {
        assertEquals(
                sqlString(expr(
                        "SELECT e.name " +
                        "FROM employee e, e.projects p " +
                        "WHERE p IS NOT NULL AND e.name LIKE 'security'")),
                sqlString(rewrite(expr(
                        "SELECT e.name " +
                        "FROM employee as e, e.projects as p " +
                        "WHERE EXISTS (SELECT * FROM p) AND e.name LIKE 'security'")))
        );
    }

    @Test
    public void nonCorrlatedExistsAnd() {
        assertEquals(
                sqlString(expr(
                        "SELECT e.name " +
                        "FROM employee e, e.projects p, e.comments c " +
                        "WHERE p IS NOT NULL AND c IS NOT NULL")),
                sqlString(rewrite(expr(
                        "SELECT e.name " +
                        "FROM employee as e, e.projects as p, e.comments as c " +
                        "WHERE EXISTS (SELECT * FROM p) AND EXISTS (SELECT * FROM c)")))
        );
    }

    @Test
    public void nonCorrlatedNotExistsUnsupported() throws Exception {
        exceptionRule.expect(IllegalStateException.class);
        exceptionRule.expectMessage("Unsupported subquery");
        rewrite(expr(
                "SELECT e.name " +
                "FROM employee as e, e.projects as p " +
                "WHERE NOT EXISTS (SELECT * FROM p)"));
    }

}