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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.visitor;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.OpenDistroSqlAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.SqlAnalysisConfig;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Product;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.visitor.TypeChecker;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlFeatureNotImplementedException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.UNKNOWN;
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

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

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

    @Test
    public void visitSelectNestedFunctionShouldThrowException() {
        exceptionRule.expect(SqlFeatureNotImplementedException.class);
        exceptionRule.expectMessage("Nested function calls like [abs(log(age))] are not supported yet");
        visit("SELECT abs(log(age)) FROM test");
    }

    @Test
    public void visitWhereNestedFunctionShouldThrowException() {
        exceptionRule.expect(SqlFeatureNotImplementedException.class);
        exceptionRule.expectMessage("Nested function calls like [abs(log(age))] are not supported yet");
        visit("SELECT age FROM test WHERE abs(log(age)) = 1");
    }

    @Test
    public void visitMathConstantAsNestedFunctionShouldPass() {
        visit("SELECT abs(pi()) FROM test");
    }

    @Test
    public void visitSupportedNestedFunctionShouldPass() {
        visit("SELECT sum(nested(name.balance)) FROM test");
    }

    @Test
    public void visitFunctionAsAggregatorShouldThrowException() {
        exceptionRule.expect(SqlFeatureNotImplementedException.class);
        exceptionRule.expectMessage("Aggregation calls with function aggregator like [max(abs(age))] are not supported yet");
        visit("SELECT max(abs(age)) FROM test");
    }

    @Test
    public void visitUnsupportedOperatorShouldThrowException() {
        exceptionRule.expect(SqlFeatureNotImplementedException.class);
        exceptionRule.expectMessage("Operator [DIV] is not supported yet");
        visit("SELECT balance DIV age FROM test");
    }

    private ParseTree createParseTree(String sql) {
        return new OpenDistroSqlAnalyzer(new SqlAnalysisConfig(true, true, 1000)).analyzeSyntax(sql);
    }

    private Type visit(String sql) {
        ParseTree parseTree = createParseTree(sql);
        return parseTree.accept(new AntlrSqlParseTreeVisitor<>(analyzer));
    }

}
