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

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.And;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Compare;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Not;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Or;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedAttribute;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Xor;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Analyze the {@link UnresolvedExpression} in the {@link AnalysisContext} to construct the {@link
 * Expression}.
 */
public class ExpressionAnalyzer extends AbstractNodeVisitor<Expression, AnalysisContext> {
  private final BuiltinFunctionRepository repository;
  private final DSL dsl;

  public ExpressionAnalyzer(
      BuiltinFunctionRepository repository) {
    this.repository = repository;
    this.dsl = new DSL(repository);
  }

  public Expression analyze(UnresolvedExpression unresolved, AnalysisContext context) {
    return unresolved.accept(this, context);
  }

  @Override
  public Expression visitUnresolvedAttribute(UnresolvedAttribute node, AnalysisContext context) {
    return visitIdentifier(node.getAttr(), context);
  }

  @Override
  public Expression visitEqualTo(EqualTo node, AnalysisContext context) {
    Expression left = node.getLeft().accept(this, context);
    Expression right = node.getRight().accept(this, context);

    return dsl.equal(left, right);
  }

  @Override
  public Expression visitLiteral(Literal node, AnalysisContext context) {
    return DSL
        .literal(ExprValueUtils.fromObjectValue(node.getValue(), node.getType().getCoreType()));
  }

  @Override
  public Expression visitAnd(And node, AnalysisContext context) {
    Expression left = node.getLeft().accept(this, context);
    Expression right = node.getRight().accept(this, context);

    return dsl.and(left, right);
  }

  @Override
  public Expression visitOr(Or node, AnalysisContext context) {
    Expression left = node.getLeft().accept(this, context);
    Expression right = node.getRight().accept(this, context);

    return dsl.or(left, right);
  }

  @Override
  public Expression visitXor(Xor node, AnalysisContext context) {
    Expression left = node.getLeft().accept(this, context);
    Expression right = node.getRight().accept(this, context);

    return dsl.xor(left, right);
  }

  @Override
  public Expression visitNot(Not node, AnalysisContext context) {
    return dsl.not(node.getExpression().accept(this, context));
  }

  @Override
  public Expression visitAggregateFunction(AggregateFunction node, AnalysisContext context) {
    Optional<BuiltinFunctionName> builtinFunctionName = BuiltinFunctionName.of(node.getFuncName());
    if (builtinFunctionName.isPresent()) {
      Expression arg = node.getField().accept(this, context);
      return (Aggregator)
          repository.compile(
              builtinFunctionName.get().getName(), Collections.singletonList(arg));
    } else {
      throw new SemanticCheckException("Unsupported aggregation function " + node.getFuncName());
    }
  }

  @Override
  public Expression visitFunction(Function node, AnalysisContext context) {
    FunctionName functionName = FunctionName.of(node.getFuncName());
    List<Expression> arguments =
        node.getFuncArgs().stream()
            .map(unresolvedExpression -> analyze(unresolvedExpression, context))
            .collect(Collectors.toList());
    return (Expression) repository.compile(functionName, arguments);
  }

  @Override
  public Expression visitCompare(Compare node, AnalysisContext context) {
    FunctionName functionName = FunctionName.of(node.getOperator());
    Expression left = analyze(node.getLeft(), context);
    Expression right = analyze(node.getRight(), context);
    return (Expression)
        repository.compile(functionName, Arrays.asList(left, right));
  }

  @Override
  public Expression visitField(Field node, AnalysisContext context) {
    String attr = node.getField().toString();
    return visitIdentifier(attr, context);
  }

  @Override
  public Expression visitQualifiedName(QualifiedName node, AnalysisContext context) {
    // Name with qualifier (index.field, index_alias.field, object/nested.inner_field
    //  text.keyword) is not supported for now
    if (node.getParts().size() > 1) {
      throw new SyntaxCheckException(String.format(
          "Qualified name [%s] is not supported yet", node));
    }
    return visitIdentifier(node.toString(), context);
  }

  private Expression visitIdentifier(String ident, AnalysisContext context) {
    TypeEnvironment typeEnv = context.peek();
    ReferenceExpression ref = DSL.ref(ident,
        typeEnv.resolve(new Symbol(Namespace.FIELD_NAME, ident)));

    // Fall back to old engine too if type is not supported semantically
    if (isTypeNotSupported(ref.type())) {
      throw new SyntaxCheckException(String.format(
          "Identifier [%s] of type [%s] is not supported yet", ident, ref.type()));
    }
    return ref;
  }

  private boolean isTypeNotSupported(ExprType type) {
    return "struct".equalsIgnoreCase(type.typeName())
        || "array".equalsIgnoreCase(type.typeName());
  }

}
