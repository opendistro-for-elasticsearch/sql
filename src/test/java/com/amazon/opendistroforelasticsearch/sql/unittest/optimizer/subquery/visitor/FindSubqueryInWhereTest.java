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

package com.amazon.opendistroforelasticsearch.sql.unittest.optimizer.subquery.visitor;

import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.optimizer.subquery.model.SubqueryType;
import com.amazon.opendistroforelasticsearch.sql.optimizer.subquery.visitor.FindSubqueryInWhere;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.util.SqlParserUtils.parse;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class FindSubqueryInWhereTest {

    @Test
    public void findIn() {
        String sql = "SELECT * FROM A WHERE a IN (SELECT b FROM B)";
        FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.IN, subqueryInWhere.subquery().get().getSubqueryType());
        assertThat(subqueryInWhere.subquery().get().getSubQueryExpr(), is(instanceOf(SQLInSubQueryExpr.class)));
    }

    @Test
    public void findNotIn() {
        String sql = "SELECT * FROM A WHERE a NOT IN (SELECT b FROM B)";
        FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subquery().get().getSubqueryType());
        assertNull(subqueryInWhere.subquery().get().getSubQueryExpr());
    }

    @Test
    public void findExist() {
        String sql = "SELECT * FROM A WHERE EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subquery().get().getSubqueryType());
        assertNull(subqueryInWhere.subquery().get().getSubQueryExpr());
    }

    @Test
    public void findNotExist() {
        String sql = "SELECT * FROM A WHERE NOT EXISTS (SELECT 1 FROM B WHERE A.a_v = B.b_v)";
        FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertEquals(SubqueryType.UNSUPPORTED, subqueryInWhere.subquery().get().getSubqueryType());
        assertNull(subqueryInWhere.subquery().get().getSubQueryExpr());
    }

    @Test
    public void findInAtSelect() {
        String sql = "SELECT A.v as v, (SELECT MAX(b) FROM B WHERE A.id = B.id) as max_age FROM A";
        FindSubqueryInWhere subqueryInWhere = new FindSubqueryInWhere();
        parse(sql).accept(subqueryInWhere);

        assertFalse(subqueryInWhere.subquery().isPresent());
    }
}