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
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Product;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.AntlrSqlParseTreeVisitor;
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
        SemanticAnalyzer analyzer = new SemanticAnalyzer(new SemanticContext(null)) {
            @Override
            public Type visitIndexName(String indexName, String alias) {
                return null; // avoid querying mapping on null LocalClusterState
            }

            @Override
            public Type visitFieldName(String fieldName) {
                return fieldName.equals("age") ? INTEGER : UNKNOWN;
            }
        };

        AntlrSqlParseTreeVisitor<Type> visitor = new AntlrSqlParseTreeVisitor<Type>(analyzer) {
            @Override
            public Type visitQuerySpecification(QuerySpecificationContext ctx) {
                Type result = super.visitQuerySpecification(ctx);
                Assert.assertTrue(result instanceof Product);
                return result;
            }
        };

        ParseTree parseTree = createParseTree("SELECT age FROM test");
        parseTree.accept(visitor);
    }

    private ParseTree createParseTree(String sql) {
        return new OpenDistroSqlAnalyzer(sql).analyzeSyntax();
    }

}
