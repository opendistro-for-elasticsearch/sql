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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.rewriter.subquery.utils;

import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.subquery.utils.FindSubQuery;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils.parse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FindSubQueryTest {

    @Test
    public void hasInSubQuery() {
        FindSubQuery findSubQuery = new FindSubQuery();

        parse("SELECT * FROM TbA " +
              "WHERE a in (SELECT b FROM TbB)").accept(findSubQuery);
        assertTrue(findSubQuery.hasSubQuery());
        assertFalse(findSubQuery.getSqlInSubQueryExprs().isEmpty());
        assertEquals(1, findSubQuery.getSqlInSubQueryExprs().size());
    }

    @Test
    public void hasExistSubQuery() {
        FindSubQuery findSubQuery = new FindSubQuery();

        parse("SELECT * FROM TbA " +
              "WHERE EXISTS (SELECT * FROM TbB)").accept(findSubQuery);
        assertTrue(findSubQuery.hasSubQuery());
        assertFalse(findSubQuery.getSqlExistsExprs().isEmpty());
        assertEquals(1, findSubQuery.getSqlExistsExprs().size());
    }

    @Test
    public void stopVisitWhenFound() {
        FindSubQuery findSubQuery = new FindSubQuery().continueVisitWhenFound(false);

        parse("SELECT * FROM TbA " +
              "WHERE a in (SELECT b FROM TbB WHERE b2 in (SELECT c FROM Tbc))").accept(findSubQuery);
        assertTrue(findSubQuery.hasSubQuery());
        assertFalse(findSubQuery.getSqlInSubQueryExprs().isEmpty());
        assertEquals(1, findSubQuery.getSqlInSubQueryExprs().size());
    }
}
