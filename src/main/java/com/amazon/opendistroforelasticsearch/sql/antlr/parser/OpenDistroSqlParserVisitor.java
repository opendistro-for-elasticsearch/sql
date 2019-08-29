// Generated from OpenDistroSqlParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.antlr.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OpenDistroSqlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OpenDistroSqlParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#root}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRoot(OpenDistroSqlParser.RootContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSqlStatement(OpenDistroSqlParser.SqlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDmlStatement(OpenDistroSqlParser.DmlStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(OpenDistroSqlParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleSelect(OpenDistroSqlParser.SimpleSelectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesisSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesisSelect(OpenDistroSqlParser.ParenthesisSelectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unionSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionSelect(OpenDistroSqlParser.UnionSelectContext ctx);
	/**
	 * Visit a parse tree produced by the {@code minusSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinusSelect(OpenDistroSqlParser.MinusSelectContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#singleDeleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSingleDeleteStatement(OpenDistroSqlParser.SingleDeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#orderByClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByClause(OpenDistroSqlParser.OrderByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#orderByExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderByExpression(OpenDistroSqlParser.OrderByExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#tableSources}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSources(OpenDistroSqlParser.TableSourcesContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableSourceBase}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSourceBase(OpenDistroSqlParser.TableSourceBaseContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableSourceNested}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSourceNested(OpenDistroSqlParser.TableSourceNestedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomTableItem(OpenDistroSqlParser.AtomTableItemContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryTableItem(OpenDistroSqlParser.SubqueryTableItemContext ctx);
	/**
	 * Visit a parse tree produced by the {@code tableSourcesItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableSourcesItem(OpenDistroSqlParser.TableSourcesItemContext ctx);
	/**
	 * Visit a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInnerJoin(OpenDistroSqlParser.InnerJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code outerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOuterJoin(OpenDistroSqlParser.OuterJoinContext ctx);
	/**
	 * Visit a parse tree produced by the {@code naturalJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNaturalJoin(OpenDistroSqlParser.NaturalJoinContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#queryExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQueryExpression(OpenDistroSqlParser.QueryExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#querySpecification}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQuerySpecification(OpenDistroSqlParser.QuerySpecificationContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#unionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnionStatement(OpenDistroSqlParser.UnionStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#minusStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinusStatement(OpenDistroSqlParser.MinusStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#selectSpec}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectSpec(OpenDistroSqlParser.SelectSpecContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#selectElements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectElements(OpenDistroSqlParser.SelectElementsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code selectStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectStarElement(OpenDistroSqlParser.SelectStarElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code selectColumnElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectColumnElement(OpenDistroSqlParser.SelectColumnElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code selectFunctionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectFunctionElement(OpenDistroSqlParser.SelectFunctionElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code selectExpressionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectExpressionElement(OpenDistroSqlParser.SelectExpressionElementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code selectNestedStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectNestedStarElement(OpenDistroSqlParser.SelectNestedStarElementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(OpenDistroSqlParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#groupByItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupByItem(OpenDistroSqlParser.GroupByItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#limitClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClause(OpenDistroSqlParser.LimitClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#limitClauseAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitClauseAtom(OpenDistroSqlParser.LimitClauseAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#administrationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdministrationStatement(OpenDistroSqlParser.AdministrationStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowStatement(OpenDistroSqlParser.ShowStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#utilityStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUtilityStatement(OpenDistroSqlParser.UtilityStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#simpleDescribeStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleDescribeStatement(OpenDistroSqlParser.SimpleDescribeStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#helpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHelpStatement(OpenDistroSqlParser.HelpStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#showFilter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowFilter(OpenDistroSqlParser.ShowFilterContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#showSchemaEntity}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitShowSchemaEntity(OpenDistroSqlParser.ShowSchemaEntityContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#intervalType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalType(OpenDistroSqlParser.IntervalTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#fullId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullId(OpenDistroSqlParser.FullIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(OpenDistroSqlParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#fullColumnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullColumnName(OpenDistroSqlParser.FullColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#uid}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUid(OpenDistroSqlParser.UidContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#simpleId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleId(OpenDistroSqlParser.SimpleIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#dottedId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDottedId(OpenDistroSqlParser.DottedIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#decimalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(OpenDistroSqlParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(OpenDistroSqlParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(OpenDistroSqlParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#hexadecimalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHexadecimalLiteral(OpenDistroSqlParser.HexadecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#nullNotnull}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNullNotnull(OpenDistroSqlParser.NullNotnullContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(OpenDistroSqlParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#uidList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUidList(OpenDistroSqlParser.UidListContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#expressions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressions(OpenDistroSqlParser.ExpressionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#constants}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstants(OpenDistroSqlParser.ConstantsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#simpleStrings}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleStrings(OpenDistroSqlParser.SimpleStringsContext ctx);
	/**
	 * Visit a parse tree produced by the {@code specificFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSpecificFunctionCall(OpenDistroSqlParser.SpecificFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateFunctionCall(OpenDistroSqlParser.AggregateFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarFunctionCall(OpenDistroSqlParser.ScalarFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code caseFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseFunctionCall(OpenDistroSqlParser.CaseFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#caseFuncAlternative}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCaseFuncAlternative(OpenDistroSqlParser.CaseFuncAlternativeContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#aggregateWindowedFunction}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAggregateWindowedFunction(OpenDistroSqlParser.AggregateWindowedFunctionContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitScalarFunctionName(OpenDistroSqlParser.ScalarFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(OpenDistroSqlParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#functionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArg(OpenDistroSqlParser.FunctionArgContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsExpression(OpenDistroSqlParser.IsExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotExpression(OpenDistroSqlParser.NotExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logicalExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpression(OpenDistroSqlParser.LogicalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicateExpression(OpenDistroSqlParser.PredicateExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionAtomPredicate(OpenDistroSqlParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code inPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInPredicate(OpenDistroSqlParser.InPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryComparasionPredicate(OpenDistroSqlParser.SubqueryComparasionPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code betweenPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBetweenPredicate(OpenDistroSqlParser.BetweenPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code binaryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinaryComparasionPredicate(OpenDistroSqlParser.BinaryComparasionPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsNullPredicate(OpenDistroSqlParser.IsNullPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLikePredicate(OpenDistroSqlParser.LikePredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRegexpPredicate(OpenDistroSqlParser.RegexpPredicateContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpressionAtom(OpenDistroSqlParser.UnaryExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code subqueryExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubqueryExpessionAtom(OpenDistroSqlParser.SubqueryExpessionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code existsExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExistsExpessionAtom(OpenDistroSqlParser.ExistsExpessionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstantExpressionAtom(OpenDistroSqlParser.ConstantExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCallExpressionAtom(OpenDistroSqlParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullColumnNameExpressionAtom(OpenDistroSqlParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code bitExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitExpressionAtom(OpenDistroSqlParser.BitExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNestedExpressionAtom(OpenDistroSqlParser.NestedExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathExpressionAtom(OpenDistroSqlParser.MathExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intervalExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalExpressionAtom(OpenDistroSqlParser.IntervalExpressionAtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#unaryOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOperator(OpenDistroSqlParser.UnaryOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(OpenDistroSqlParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#logicalOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOperator(OpenDistroSqlParser.LogicalOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#bitOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBitOperator(OpenDistroSqlParser.BitOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#mathOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMathOperator(OpenDistroSqlParser.MathOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#charsetNameBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharsetNameBase(OpenDistroSqlParser.CharsetNameBaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#intervalTypeBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntervalTypeBase(OpenDistroSqlParser.IntervalTypeBaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#dataTypeBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDataTypeBase(OpenDistroSqlParser.DataTypeBaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitKeywordsCanBeId(OpenDistroSqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#functionNameBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionNameBase(OpenDistroSqlParser.FunctionNameBaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link OpenDistroSqlParser#esFunctionNameBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEsFunctionNameBase(OpenDistroSqlParser.EsFunctionNameBaseContext ctx);
}