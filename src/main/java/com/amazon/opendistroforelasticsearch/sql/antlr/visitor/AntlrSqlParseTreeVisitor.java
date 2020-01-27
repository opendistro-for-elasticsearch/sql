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

package com.amazon.opendistroforelasticsearch.sql.antlr.visitor;

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.InnerJoinContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SelectColumnElementContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SubqueryTableItemContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableNamePatternContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParserBaseVisitor;
import com.google.common.base.Strings;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.AggregateWindowedFunctionContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.AtomTableItemContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.BinaryComparisonPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.BooleanLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ComparisonOperatorContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ConstantContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FullColumnNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FunctionNameBaseContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.InPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.IsExpressionContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.MathOperatorContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.MinusSelectContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.OuterJoinContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.PredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.RegexpPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.RootContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ScalarFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SelectElementsContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SelectExpressionElementContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SelectFunctionElementContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.SimpleTableNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableAndTypeNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableSourceBaseContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableSourceItemContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableSourcesContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.UdfFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.UidContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.UnionSelectContext;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * ANTLR parse tree visitor to drive the analysis process.
 */
public class AntlrSqlParseTreeVisitor<T extends Reducible> extends OpenDistroSqlParserBaseVisitor<T> {

    /** Generic visitor to perform the real action on parse tree */
    private final GenericSqlParseTreeVisitor<T> visitor;

    public AntlrSqlParseTreeVisitor(GenericSqlParseTreeVisitor<T> visitor) {
        this.visitor = visitor;
    }

    @Override
    public T visitRoot(RootContext ctx) {
        visitor.visitRoot();
        return super.visitRoot(ctx);
    }

    @Override
    public T visitUnionSelect(UnionSelectContext ctx) {
        T union = visitor.visitOperator("UNION");
        return reduce(union,
            asList(
                ctx.querySpecification(),
                ctx.unionStatement()
            )
        );
    }

    @Override
    public T visitMinusSelect(MinusSelectContext ctx) {
        T minus = visitor.visitOperator("MINUS");
        return reduce(minus, asList(ctx.querySpecification(), ctx.minusStatement()));
    }

    @Override
    public T visitInPredicate(InPredicateContext ctx) {
        T in = visitor.visitOperator("IN");
        PredicateContext field = ctx.predicate();
        ParserRuleContext subquery = (ctx.selectStatement() != null) ? ctx.selectStatement() : ctx.expressions();
        return reduce(in, Arrays.asList(field, subquery));
    }

    @Override
    public T visitTableSources(TableSourcesContext ctx) {
        if (ctx.tableSource().size() < 2) {
            return super.visitTableSources(ctx);
        }
        T commaJoin = visitor.visitOperator("JOIN");
        return reduce(commaJoin, ctx.tableSource());
    }

    @Override
    public T visitTableSourceBase(TableSourceBaseContext ctx) {
        if (ctx.joinPart().isEmpty()) {
            return super.visitTableSourceBase(ctx);
        }
        T join = visitor.visitOperator("JOIN");
        return reduce(join, asList(ctx.tableSourceItem(), ctx.joinPart()));
    }

    @Override
    public T visitInnerJoin(InnerJoinContext ctx) {
        return visitJoin(ctx.children, ctx.tableSourceItem());
    }

    @Override
    public T visitOuterJoin(OuterJoinContext ctx) {
        return visitJoin(ctx.children, ctx.tableSourceItem());
    }

    /**
     * Enforce visit order because ANTLR is generic and unaware.
     *
     * Visiting order is:
     *  FROM
     *  => WHERE
     *   => SELECT
     *    => GROUP BY
     *     => HAVING
     *      => ORDER BY
     *       => LIMIT
     */
     @Override
    public T visitQuerySpecification(QuerySpecificationContext ctx) {
        visitor.visitQuery();

        // Always visit FROM clause first to define symbols
        FromClauseContext fromClause = ctx.fromClause();
        visit(fromClause.tableSources());

        if (fromClause.whereExpr != null) {
            visit(fromClause.whereExpr);
        }

        // Note visit GROUP BY and HAVING later than SELECT for alias definition
        T result = visitSelectElements(ctx.selectElements());
        fromClause.groupByItem().forEach(this::visit);
        if (fromClause.havingExpr != null) {
            visit(fromClause.havingExpr);
        }

        if (ctx.orderByClause() != null) {
            visitOrderByClause(ctx.orderByClause());
        }
        if (ctx.limitClause() != null) {
            visitLimitClause(ctx.limitClause());
        }

        visitor.endVisitQuery();
        return result;
    }

    @Override
    public T visitSubqueryTableItem(SubqueryTableItemContext ctx) {
        throw new EarlyExitAnalysisException("Exit when meeting subquery in from");
    }

    /** Visit here instead of tableName because we need alias */
    @Override
    public T visitAtomTableItem(AtomTableItemContext ctx) {
        String alias = (ctx.alias == null) ? "" : ctx.alias.getText();
        T result = visit(ctx.tableName());
        visitor.visitAs(alias, result);
        return result;
    }

    @Override
    public T visitSimpleTableName(SimpleTableNameContext ctx) {
        return visitor.visitIndexName(ctx.getText());
    }

    @Override
    public T visitTableNamePattern(TableNamePatternContext ctx) {
        throw new EarlyExitAnalysisException("Exit when meeting index pattern");
    }

    @Override
    public T visitTableAndTypeName(TableAndTypeNameContext ctx) {
        return visitor.visitIndexName(ctx.uid(0).getText());
    }

    @Override
    public T visitFullColumnName(FullColumnNameContext ctx) {
        return visitor.visitFieldName(ctx.getText());
    }

    @Override
    public T visitUdfFunctionCall(UdfFunctionCallContext ctx) {
        String funcName = ctx.fullId().getText();
        T func = visitor.visitFunctionName(funcName);
        return reduce(func, ctx.functionArgs());
    }

    @Override
    public T visitScalarFunctionCall(ScalarFunctionCallContext ctx) {
        UnsupportedSemanticVerifier.verify(ctx);
        T func = visit(ctx.scalarFunctionName());
        return reduce(func, ctx.functionArgs());
    }

    @Override
    public T visitMathOperator(MathOperatorContext ctx) {
        UnsupportedSemanticVerifier.verify(ctx);
        return super.visitMathOperator(ctx);
    }

    @Override
    public T visitRegexpPredicate(RegexpPredicateContext ctx) {
        UnsupportedSemanticVerifier.verify(ctx);
        return super.visitRegexpPredicate(ctx);
    }

    @Override
    public T visitSelectElements(SelectElementsContext ctx) {
        return visitor.visitSelect(ctx.selectElement().
                                       stream().
                                       map(this::visit).
                                       collect(Collectors.toList()));
    }

    @Override
    public T visitSelectColumnElement(SelectColumnElementContext ctx) {
        return visitSelectItem(ctx.fullColumnName(), ctx.uid());
    }

    @Override
    public T visitSelectFunctionElement(SelectFunctionElementContext ctx) {
        return visitSelectItem(ctx.functionCall(), ctx.uid());
    }

    @Override
    public T visitSelectExpressionElement(SelectExpressionElementContext ctx) {
        return visitSelectItem(ctx.expression(), ctx.uid());
    }

    @Override
    public T visitAggregateWindowedFunction(AggregateWindowedFunctionContext ctx) {
        String funcName = ctx.getChild(0).getText();
        T func = visitor.visitFunctionName(funcName);
        return reduce(func, ctx.functionArg());
    }

    @Override
    public T visitFunctionNameBase(FunctionNameBaseContext ctx) {
        return visitor.visitFunctionName(ctx.getText());
    }

    @Override
    public T visitBinaryComparisonPredicate(BinaryComparisonPredicateContext ctx) {
        if (isNamedArgument(ctx)) { // Essentially named argument is assign instead of comparison
            return defaultResult();
        }

        T op = visit(ctx.comparisonOperator());
        return reduce(op, Arrays.asList(ctx.left, ctx.right));
    }

    @Override
    public T visitIsExpression(IsExpressionContext ctx) {
        T op = visitor.visitOperator("IS");
        return op.reduce(Arrays.asList(
            visit(ctx.predicate()),
            visitor.visitBoolean(ctx.testValue.getText()))
        );
    }

    @Override
    public T visitConvertedDataType(OpenDistroSqlParser.ConvertedDataTypeContext ctx) {
        if (ctx.getChild(0) != null && !Strings.isNullOrEmpty(ctx.getChild(0).getText())) {
            return visitor.visitConvertedType(ctx.getChild(0).getText());
        } else {
            return super.visitConvertedDataType(ctx);
        }
    }

    @Override
    public T visitComparisonOperator(ComparisonOperatorContext ctx) {
        return visitor.visitOperator(ctx.getText());
    }

    @Override
    public T visitConstant(ConstantContext ctx) {
        if (ctx.REAL_LITERAL() != null) {
            return visitor.visitFloat(ctx.getText());
        }
        if (ctx.dateType != null) {
            return visitor.visitDate(ctx.getText());
        }
        if (ctx.nullLiteral != null) {
            return visitor.visitNull();
        }
        return super.visitConstant(ctx);
    }

    @Override
    public T visitStringLiteral(StringLiteralContext ctx) {
        return visitor.visitString(ctx.getText());
    }

    @Override
    public T visitDecimalLiteral(DecimalLiteralContext ctx) {
        return visitor.visitInteger(ctx.getText());
    }

    @Override
    public T visitBooleanLiteral(BooleanLiteralContext ctx) {
        return visitor.visitBoolean(ctx.getText());
    }

    @Override
    protected T defaultResult() {
        return visitor.defaultValue();
    }

    @Override
    protected T aggregateResult(T aggregate, T nextResult) {
        if (nextResult != defaultResult()) { // Simply return non-default value for now
            return nextResult;
        }
        return aggregate;
    }

    /**
     * Named argument, ex. TOPHITS('size'=3), is under FunctionArgs -> Predicate
     * And the function name should be contained in esFunctionNameBase
     */
    private boolean isNamedArgument(BinaryComparisonPredicateContext ctx) {
        if (ctx.getParent() != null && ctx.getParent().getParent() != null
                && ctx.getParent().getParent().getParent() != null
                && ctx.getParent().getParent().getParent() instanceof ScalarFunctionCallContext) {

            ScalarFunctionCallContext parent = (ScalarFunctionCallContext) ctx.getParent().getParent().getParent();
            return parent.scalarFunctionName().functionNameBase().esFunctionNameBase() != null;
        }
        return false;
    }

    /** Enforce visiting result of table instead of ON clause as result */
    private T visitJoin(List<ParseTree> children, TableSourceItemContext tableCtx) {
        T result = defaultResult();
        for (ParseTree child : children) {
            if (child == tableCtx) {
                result = visit(tableCtx);
            } else {
                visit(child);
            }
        }
        return result;
    }

    /** Visit select items for type check and alias definition */
    private T visitSelectItem(ParserRuleContext item, UidContext uid) {
        T result = visit(item);
        if (uid != null) {
            visitor.visitAs(uid.getText(), result);
        }
        return result;
    }

    private T reduce(T reducer, ParserRuleContext ctx) {
        return reduce(reducer, (ctx == null) ? emptyList() : ctx.children);
    }

    /** Make constructor apply arguments and return result type */
    private <Node extends ParseTree> T reduce(T reducer, List<Node> nodes) {
        List<T> args;
        if (nodes == null) {
            args = emptyList();
        } else {
            args = nodes.stream().
                         map(this::visit).
                         filter(type -> type != defaultResult()).
                         collect(Collectors.toList());
        }
        return reducer.reduce(args);
    }

    /** Combine an item and a list of items to a single list */
    private <Node1 extends ParseTree,
             Node2 extends ParseTree>
             List<ParseTree> asList(Node1 first, List<Node2> rest) {

        List<ParseTree> result = new ArrayList<>(singleton(first));
        result.addAll(rest);
        return result;
    }

}
