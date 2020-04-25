/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExpressionAnalyzer extends AbstractNodeVisitor<Expression, AnalysisContext> {
    private final DSL dsl;

    public Expression analyze(UnresolvedExpression unresolved, AnalysisContext context) {
        return unresolved.accept(this, context);
    }

    @Override
    public Expression visitUnresolvedAttribute(UnresolvedAttribute node, AnalysisContext context) {
        TypeEnvironment typeEnv = context.peek();
        ReferenceExpression ref = DSL.ref(node.getAttr());
        typeEnv.resolve(ref);
        return ref;
    }

    @Override
    public Expression visitEqualTo(EqualTo node, AnalysisContext context) {
        Expression left = node.getLeft().accept(this, context);
        Expression right = node.getRight().accept(this, context);

        return dsl.equal(context.peek(), left, right);
    }

    @Override
    public Expression visitLiteral(Literal node, AnalysisContext context) {
        return DSL.literal(ExprValueUtils.fromObjectValue(node.getValue()));
    }

    @Override
    public Expression visitAnd(And node, AnalysisContext context) {
        Expression left = node.getLeft().accept(this, context);
        Expression right = node.getRight().accept(this, context);

        return dsl.and(context.peek(), left, right);
    }


}
