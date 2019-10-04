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
import com.amazon.opendistroforelasticsearch.sql.antlr.SqlAnalysisConfig;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.special.Product;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.AntlrSqlParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.UNKNOWN;
import static java.util.Collections.emptyList;

/**
 * Test cases for AntlrSqlParseTreeVisitor
 */
public class AntlrSqlParseTreeVisitorTest {

    private TypeChecker analyzer = new TypeChecker(new SemanticContext()) {
        @Override
        public Type visitIndexName(String indexName) {
            return null; // avoid querying mapping on null LocalClusterState
        }

        @Override
        public Type visitFieldName(String fieldName) {
            switch (fieldName) {
                case "age": return INTEGER;
                case "birthday": return DATE;
                default: return UNKNOWN;
            }
        }
    };

    @Test
    public void selectNumberShouldReturnNumberAsQueryVisitingResult() {
        Type result = visit("SELECT age FROM test");
        Assert.assertSame(result, INTEGER);
    }

    @Test
    public void selectNumberAndDateShouldReturnProductOfThemAsQueryVisitingResult() {
        Type result = visit("SELECT age, birthday FROM test");
        Assert.assertTrue(result instanceof Product );
        Assert.assertTrue(result.isCompatible(new Product(Arrays.asList(INTEGER, DATE))));
    }

    @Test
    public void selectStarShouldReturnEmptyProductAsQueryVisitingResult() {
        Type result = visit("SELECT * FROM test");
        Assert.assertTrue(result instanceof Product);
        Assert.assertTrue(result.isCompatible(new Product(emptyList())));
    }

    private ParseTree createParseTree(String sql) {
        return new OpenDistroSqlAnalyzer(new SqlAnalysisConfig(true, true, 1000)).analyzeSyntax(sql);
    }

    private Type visit(String sql) {
        ParseTree parseTree = createParseTree(sql);
        return parseTree.accept(new AntlrSqlParseTreeVisitor<>(analyzer));
    }

}
