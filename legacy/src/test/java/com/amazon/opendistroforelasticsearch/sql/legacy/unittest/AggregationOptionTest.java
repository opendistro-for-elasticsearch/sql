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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.SqlParserUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Unit test class for feature of aggregation options: DISTINCT, ALL, UNIQUE, DEDUPLICATION
 */
public class AggregationOptionTest {

    @Test
    public void selectDistinctFieldsShouldHaveAggregationOption() {
        List<Field> fields = getSelectFields("SELECT DISTINCT gender, city FROM accounts");
        for (Field field: fields) {
            Assert.assertEquals(field.getOption(), SQLAggregateOption.DISTINCT);
        }
    }

    @Test
    public void selectWithoutDistinctFieldsShouldNotHaveAggregationOption() {
        List<Field> fields = getSelectFields("SELECT gender, city FROM accounts");
        for (Field field: fields) {
            Assert.assertNull(field.getOption());
        }
    }

    @Test
    public void selectDistinctWithoutGroupByShouldHaveGroupByItems() {
        List<List<Field>> groupBys = getGroupBys("SELECT DISTINCT gender, city FROM accounts");
        Assert.assertFalse(groupBys.isEmpty());
    }

    @Test
    public void selectWithoutDistinctWithoutGroupByShouldNotHaveGroupByItems() {
        List<List<Field>> groupBys = getGroupBys("SELECT gender, city FROM accounts");
        Assert.assertTrue(groupBys.isEmpty());
    }

    private List<List<Field>> getGroupBys(String query) {
        return getSelect(query).getGroupBys();
    }

    private List<Field> getSelectFields(String query) {
        return getSelect(query).getFields();
    }

    private Select getSelect(String query) {
        SQLQueryExpr queryExpr = SqlParserUtils.parse(query);
        Select select = null;
        try {
            select = new SqlParser().parseSelect(queryExpr);
        } catch (SqlParseException e) {
            e.printStackTrace();
        }
        return select;
    }
}
