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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor;

import com.amazon.opendistroforelasticsearch.sql.antlr.OpenDistroSqlAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.AntlrSqlParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.GenericSqlParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.UNKNOWN;

/**
 * Test cases for AntlrSqlParseTreeVisitor
 */
public class AntlrSqlParseTreeVisitorTest {

    @Test
    public void selectClauseVisitingResultShouldReturnAsQueryVisitingResult() {
        AntlrSqlParseTreeVisitor<Type> visitor = new AntlrSqlParseTreeVisitor<>(
            new GenericSqlParseTreeVisitor<Type>() {
                @Override
                public Type visitSelectItem(Type type, String alias) {
                    return type;
                }

                @Override
                public Type visitFieldName(String fieldName) {
                    return fieldName.equals("age") ? INTEGER : UNKNOWN;
                }
            }
        );
        ParseTree parseTree = createParseTree("SELECT age FROM test");
        Type result = parseTree.accept(visitor);
        Assert.assertEquals(INTEGER, result);
    }

    private ParseTree createParseTree(String sql) {
        return new OpenDistroSqlAnalyzer(sql).analyzeSyntax();
    }

}
