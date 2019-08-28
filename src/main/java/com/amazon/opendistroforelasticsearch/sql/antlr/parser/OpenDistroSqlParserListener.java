// Generated from OpenDistroSqlParser.g4 by ANTLR 4.7.1
package com.amazon.opendistroforelasticsearch.sql.antlr.parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link OpenDistroSqlParser}.
 */
public interface OpenDistroSqlParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void enterRoot(OpenDistroSqlParser.RootContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#root}.
	 * @param ctx the parse tree
	 */
	void exitRoot(OpenDistroSqlParser.RootContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void enterSqlStatement(OpenDistroSqlParser.SqlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#sqlStatement}.
	 * @param ctx the parse tree
	 */
	void exitSqlStatement(OpenDistroSqlParser.SqlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDmlStatement(OpenDistroSqlParser.DmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDmlStatement(OpenDistroSqlParser.DmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStatement(OpenDistroSqlParser.DeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStatement(OpenDistroSqlParser.DeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterSimpleSelect(OpenDistroSqlParser.SimpleSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitSimpleSelect(OpenDistroSqlParser.SimpleSelectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parenthesisSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterParenthesisSelect(OpenDistroSqlParser.ParenthesisSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parenthesisSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitParenthesisSelect(OpenDistroSqlParser.ParenthesisSelectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unionSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterUnionSelect(OpenDistroSqlParser.UnionSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unionSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitUnionSelect(OpenDistroSqlParser.UnionSelectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code minusSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterMinusSelect(OpenDistroSqlParser.MinusSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code minusSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitMinusSelect(OpenDistroSqlParser.MinusSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#singleDeleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleDeleteStatement(OpenDistroSqlParser.SingleDeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#singleDeleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleDeleteStatement(OpenDistroSqlParser.SingleDeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void enterOrderByClause(OpenDistroSqlParser.OrderByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#orderByClause}.
	 * @param ctx the parse tree
	 */
	void exitOrderByClause(OpenDistroSqlParser.OrderByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#orderByExpression}.
	 * @param ctx the parse tree
	 */
	void enterOrderByExpression(OpenDistroSqlParser.OrderByExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#orderByExpression}.
	 * @param ctx the parse tree
	 */
	void exitOrderByExpression(OpenDistroSqlParser.OrderByExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tableSources}.
	 * @param ctx the parse tree
	 */
	void enterTableSources(OpenDistroSqlParser.TableSourcesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tableSources}.
	 * @param ctx the parse tree
	 */
	void exitTableSources(OpenDistroSqlParser.TableSourcesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableSourceBase}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 */
	void enterTableSourceBase(OpenDistroSqlParser.TableSourceBaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableSourceBase}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 */
	void exitTableSourceBase(OpenDistroSqlParser.TableSourceBaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableSourceNested}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 */
	void enterTableSourceNested(OpenDistroSqlParser.TableSourceNestedContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableSourceNested}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSource}.
	 * @param ctx the parse tree
	 */
	void exitTableSourceNested(OpenDistroSqlParser.TableSourceNestedContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void enterAtomTableItem(OpenDistroSqlParser.AtomTableItemContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void exitAtomTableItem(OpenDistroSqlParser.AtomTableItemContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryTableItem(OpenDistroSqlParser.SubqueryTableItemContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryTableItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryTableItem(OpenDistroSqlParser.SubqueryTableItemContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableSourcesItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void enterTableSourcesItem(OpenDistroSqlParser.TableSourcesItemContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableSourcesItem}
	 * labeled alternative in {@link OpenDistroSqlParser#tableSourceItem}.
	 * @param ctx the parse tree
	 */
	void exitTableSourcesItem(OpenDistroSqlParser.TableSourcesItemContext ctx);
	/**
	 * Enter a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void enterInnerJoin(OpenDistroSqlParser.InnerJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code innerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void exitInnerJoin(OpenDistroSqlParser.InnerJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code outerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void enterOuterJoin(OpenDistroSqlParser.OuterJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code outerJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void exitOuterJoin(OpenDistroSqlParser.OuterJoinContext ctx);
	/**
	 * Enter a parse tree produced by the {@code naturalJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void enterNaturalJoin(OpenDistroSqlParser.NaturalJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code naturalJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void exitNaturalJoin(OpenDistroSqlParser.NaturalJoinContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpression(OpenDistroSqlParser.QueryExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#queryExpression}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpression(OpenDistroSqlParser.QueryExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void enterQuerySpecification(OpenDistroSqlParser.QuerySpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#querySpecification}.
	 * @param ctx the parse tree
	 */
	void exitQuerySpecification(OpenDistroSqlParser.QuerySpecificationContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#unionStatement}.
	 * @param ctx the parse tree
	 */
	void enterUnionStatement(OpenDistroSqlParser.UnionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#unionStatement}.
	 * @param ctx the parse tree
	 */
	void exitUnionStatement(OpenDistroSqlParser.UnionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#minusStatement}.
	 * @param ctx the parse tree
	 */
	void enterMinusStatement(OpenDistroSqlParser.MinusStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#minusStatement}.
	 * @param ctx the parse tree
	 */
	void exitMinusStatement(OpenDistroSqlParser.MinusStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#selectSpec}.
	 * @param ctx the parse tree
	 */
	void enterSelectSpec(OpenDistroSqlParser.SelectSpecContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#selectSpec}.
	 * @param ctx the parse tree
	 */
	void exitSelectSpec(OpenDistroSqlParser.SelectSpecContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void enterSelectElements(OpenDistroSqlParser.SelectElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#selectElements}.
	 * @param ctx the parse tree
	 */
	void exitSelectElements(OpenDistroSqlParser.SelectElementsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectStarElement(OpenDistroSqlParser.SelectStarElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectStarElement(OpenDistroSqlParser.SelectStarElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectColumnElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectColumnElement(OpenDistroSqlParser.SelectColumnElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectColumnElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectColumnElement(OpenDistroSqlParser.SelectColumnElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectFunctionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectFunctionElement(OpenDistroSqlParser.SelectFunctionElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectFunctionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectFunctionElement(OpenDistroSqlParser.SelectFunctionElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectExpressionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectExpressionElement(OpenDistroSqlParser.SelectExpressionElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectExpressionElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectExpressionElement(OpenDistroSqlParser.SelectExpressionElementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectNestedStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void enterSelectNestedStarElement(OpenDistroSqlParser.SelectNestedStarElementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectNestedStarElement}
	 * labeled alternative in {@link OpenDistroSqlParser#selectElement}.
	 * @param ctx the parse tree
	 */
	void exitSelectNestedStarElement(OpenDistroSqlParser.SelectNestedStarElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(OpenDistroSqlParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(OpenDistroSqlParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void enterGroupByItem(OpenDistroSqlParser.GroupByItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#groupByItem}.
	 * @param ctx the parse tree
	 */
	void exitGroupByItem(OpenDistroSqlParser.GroupByItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void enterLimitClause(OpenDistroSqlParser.LimitClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#limitClause}.
	 * @param ctx the parse tree
	 */
	void exitLimitClause(OpenDistroSqlParser.LimitClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#limitClauseAtom}.
	 * @param ctx the parse tree
	 */
	void enterLimitClauseAtom(OpenDistroSqlParser.LimitClauseAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#limitClauseAtom}.
	 * @param ctx the parse tree
	 */
	void exitLimitClauseAtom(OpenDistroSqlParser.LimitClauseAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#administrationStatement}.
	 * @param ctx the parse tree
	 */
	void enterAdministrationStatement(OpenDistroSqlParser.AdministrationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#administrationStatement}.
	 * @param ctx the parse tree
	 */
	void exitAdministrationStatement(OpenDistroSqlParser.AdministrationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowStatement(OpenDistroSqlParser.ShowStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowStatement(OpenDistroSqlParser.ShowStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#utilityStatement}.
	 * @param ctx the parse tree
	 */
	void enterUtilityStatement(OpenDistroSqlParser.UtilityStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#utilityStatement}.
	 * @param ctx the parse tree
	 */
	void exitUtilityStatement(OpenDistroSqlParser.UtilityStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#simpleDescribeStatement}.
	 * @param ctx the parse tree
	 */
	void enterSimpleDescribeStatement(OpenDistroSqlParser.SimpleDescribeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#simpleDescribeStatement}.
	 * @param ctx the parse tree
	 */
	void exitSimpleDescribeStatement(OpenDistroSqlParser.SimpleDescribeStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#helpStatement}.
	 * @param ctx the parse tree
	 */
	void enterHelpStatement(OpenDistroSqlParser.HelpStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#helpStatement}.
	 * @param ctx the parse tree
	 */
	void exitHelpStatement(OpenDistroSqlParser.HelpStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showFilter}.
	 * @param ctx the parse tree
	 */
	void enterShowFilter(OpenDistroSqlParser.ShowFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showFilter}.
	 * @param ctx the parse tree
	 */
	void exitShowFilter(OpenDistroSqlParser.ShowFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showSchemaEntity}.
	 * @param ctx the parse tree
	 */
	void enterShowSchemaEntity(OpenDistroSqlParser.ShowSchemaEntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showSchemaEntity}.
	 * @param ctx the parse tree
	 */
	void exitShowSchemaEntity(OpenDistroSqlParser.ShowSchemaEntityContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#intervalType}.
	 * @param ctx the parse tree
	 */
	void enterIntervalType(OpenDistroSqlParser.IntervalTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#intervalType}.
	 * @param ctx the parse tree
	 */
	void exitIntervalType(OpenDistroSqlParser.IntervalTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#fullId}.
	 * @param ctx the parse tree
	 */
	void enterFullId(OpenDistroSqlParser.FullIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#fullId}.
	 * @param ctx the parse tree
	 */
	void exitFullId(OpenDistroSqlParser.FullIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(OpenDistroSqlParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(OpenDistroSqlParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void enterFullColumnName(OpenDistroSqlParser.FullColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void exitFullColumnName(OpenDistroSqlParser.FullColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#charsetName}.
	 * @param ctx the parse tree
	 */
	void enterCharsetName(OpenDistroSqlParser.CharsetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#charsetName}.
	 * @param ctx the parse tree
	 */
	void exitCharsetName(OpenDistroSqlParser.CharsetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#collationName}.
	 * @param ctx the parse tree
	 */
	void enterCollationName(OpenDistroSqlParser.CollationNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#collationName}.
	 * @param ctx the parse tree
	 */
	void exitCollationName(OpenDistroSqlParser.CollationNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#engineName}.
	 * @param ctx the parse tree
	 */
	void enterEngineName(OpenDistroSqlParser.EngineNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#engineName}.
	 * @param ctx the parse tree
	 */
	void exitEngineName(OpenDistroSqlParser.EngineNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#uid}.
	 * @param ctx the parse tree
	 */
	void enterUid(OpenDistroSqlParser.UidContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#uid}.
	 * @param ctx the parse tree
	 */
	void exitUid(OpenDistroSqlParser.UidContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#simpleId}.
	 * @param ctx the parse tree
	 */
	void enterSimpleId(OpenDistroSqlParser.SimpleIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#simpleId}.
	 * @param ctx the parse tree
	 */
	void exitSimpleId(OpenDistroSqlParser.SimpleIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dottedId}.
	 * @param ctx the parse tree
	 */
	void enterDottedId(OpenDistroSqlParser.DottedIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dottedId}.
	 * @param ctx the parse tree
	 */
	void exitDottedId(OpenDistroSqlParser.DottedIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(OpenDistroSqlParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(OpenDistroSqlParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(OpenDistroSqlParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(OpenDistroSqlParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(OpenDistroSqlParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(OpenDistroSqlParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#hexadecimalLiteral}.
	 * @param ctx the parse tree
	 */
	void enterHexadecimalLiteral(OpenDistroSqlParser.HexadecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#hexadecimalLiteral}.
	 * @param ctx the parse tree
	 */
	void exitHexadecimalLiteral(OpenDistroSqlParser.HexadecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#nullNotnull}.
	 * @param ctx the parse tree
	 */
	void enterNullNotnull(OpenDistroSqlParser.NullNotnullContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#nullNotnull}.
	 * @param ctx the parse tree
	 */
	void exitNullNotnull(OpenDistroSqlParser.NullNotnullContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(OpenDistroSqlParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(OpenDistroSqlParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#uidList}.
	 * @param ctx the parse tree
	 */
	void enterUidList(OpenDistroSqlParser.UidListContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#uidList}.
	 * @param ctx the parse tree
	 */
	void exitUidList(OpenDistroSqlParser.UidListContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#expressions}.
	 * @param ctx the parse tree
	 */
	void enterExpressions(OpenDistroSqlParser.ExpressionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#expressions}.
	 * @param ctx the parse tree
	 */
	void exitExpressions(OpenDistroSqlParser.ExpressionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#constants}.
	 * @param ctx the parse tree
	 */
	void enterConstants(OpenDistroSqlParser.ConstantsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#constants}.
	 * @param ctx the parse tree
	 */
	void exitConstants(OpenDistroSqlParser.ConstantsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#simpleStrings}.
	 * @param ctx the parse tree
	 */
	void enterSimpleStrings(OpenDistroSqlParser.SimpleStringsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#simpleStrings}.
	 * @param ctx the parse tree
	 */
	void exitSimpleStrings(OpenDistroSqlParser.SimpleStringsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code specificFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterSpecificFunctionCall(OpenDistroSqlParser.SpecificFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code specificFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitSpecificFunctionCall(OpenDistroSqlParser.SpecificFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterAggregateFunctionCall(OpenDistroSqlParser.AggregateFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code aggregateFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitAggregateFunctionCall(OpenDistroSqlParser.AggregateFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterScalarFunctionCall(OpenDistroSqlParser.ScalarFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code scalarFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitScalarFunctionCall(OpenDistroSqlParser.ScalarFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterSimpleFunctionCall(OpenDistroSqlParser.SimpleFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitSimpleFunctionCall(OpenDistroSqlParser.SimpleFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code caseFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterCaseFunctionCall(OpenDistroSqlParser.CaseFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code caseFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitCaseFunctionCall(OpenDistroSqlParser.CaseFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#caseFuncAlternative}.
	 * @param ctx the parse tree
	 */
	void enterCaseFuncAlternative(OpenDistroSqlParser.CaseFuncAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#caseFuncAlternative}.
	 * @param ctx the parse tree
	 */
	void exitCaseFuncAlternative(OpenDistroSqlParser.CaseFuncAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#aggregateWindowedFunction}.
	 * @param ctx the parse tree
	 */
	void enterAggregateWindowedFunction(OpenDistroSqlParser.AggregateWindowedFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#aggregateWindowedFunction}.
	 * @param ctx the parse tree
	 */
	void exitAggregateWindowedFunction(OpenDistroSqlParser.AggregateWindowedFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterScalarFunctionName(OpenDistroSqlParser.ScalarFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#scalarFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitScalarFunctionName(OpenDistroSqlParser.ScalarFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(OpenDistroSqlParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(OpenDistroSqlParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArg(OpenDistroSqlParser.FunctionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArg(OpenDistroSqlParser.FunctionArgContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIsExpression(OpenDistroSqlParser.IsExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIsExpression(OpenDistroSqlParser.IsExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(OpenDistroSqlParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(OpenDistroSqlParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpression(OpenDistroSqlParser.LogicalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpression(OpenDistroSqlParser.LogicalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPredicateExpression(OpenDistroSqlParser.PredicateExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code predicateExpression}
	 * labeled alternative in {@link OpenDistroSqlParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPredicateExpression(OpenDistroSqlParser.PredicateExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterExpressionAtomPredicate(OpenDistroSqlParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expressionAtomPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitExpressionAtomPredicate(OpenDistroSqlParser.ExpressionAtomPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code inPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterInPredicate(OpenDistroSqlParser.InPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code inPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitInPredicate(OpenDistroSqlParser.InPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryComparasionPredicate(OpenDistroSqlParser.SubqueryComparasionPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryComparasionPredicate(OpenDistroSqlParser.SubqueryComparasionPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code betweenPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterBetweenPredicate(OpenDistroSqlParser.BetweenPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code betweenPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitBetweenPredicate(OpenDistroSqlParser.BetweenPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterBinaryComparasionPredicate(OpenDistroSqlParser.BinaryComparasionPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryComparasionPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitBinaryComparasionPredicate(OpenDistroSqlParser.BinaryComparasionPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterIsNullPredicate(OpenDistroSqlParser.IsNullPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isNullPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitIsNullPredicate(OpenDistroSqlParser.IsNullPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterLikePredicate(OpenDistroSqlParser.LikePredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code likePredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitLikePredicate(OpenDistroSqlParser.LikePredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterRegexpPredicate(OpenDistroSqlParser.RegexpPredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexpPredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitRegexpPredicate(OpenDistroSqlParser.RegexpPredicateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpressionAtom(OpenDistroSqlParser.UnaryExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpressionAtom(OpenDistroSqlParser.UnaryExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subqueryExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterSubqueryExpessionAtom(OpenDistroSqlParser.SubqueryExpessionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subqueryExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitSubqueryExpessionAtom(OpenDistroSqlParser.SubqueryExpessionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code existsExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterExistsExpessionAtom(OpenDistroSqlParser.ExistsExpessionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code existsExpessionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitExistsExpessionAtom(OpenDistroSqlParser.ExistsExpessionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterConstantExpressionAtom(OpenDistroSqlParser.ConstantExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constantExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitConstantExpressionAtom(OpenDistroSqlParser.ConstantExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCallExpressionAtom(OpenDistroSqlParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionCallExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCallExpressionAtom(OpenDistroSqlParser.FunctionCallExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterFullColumnNameExpressionAtom(OpenDistroSqlParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code fullColumnNameExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitFullColumnNameExpressionAtom(OpenDistroSqlParser.FullColumnNameExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code bitExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterBitExpressionAtom(OpenDistroSqlParser.BitExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code bitExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitBitExpressionAtom(OpenDistroSqlParser.BitExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterNestedExpressionAtom(OpenDistroSqlParser.NestedExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nestedExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitNestedExpressionAtom(OpenDistroSqlParser.NestedExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterMathExpressionAtom(OpenDistroSqlParser.MathExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mathExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitMathExpressionAtom(OpenDistroSqlParser.MathExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intervalExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpressionAtom(OpenDistroSqlParser.IntervalExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intervalExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpressionAtom(OpenDistroSqlParser.IntervalExpressionAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void enterUnaryOperator(OpenDistroSqlParser.UnaryOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#unaryOperator}.
	 * @param ctx the parse tree
	 */
	void exitUnaryOperator(OpenDistroSqlParser.UnaryOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(OpenDistroSqlParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(OpenDistroSqlParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOperator(OpenDistroSqlParser.LogicalOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#logicalOperator}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOperator(OpenDistroSqlParser.LogicalOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#bitOperator}.
	 * @param ctx the parse tree
	 */
	void enterBitOperator(OpenDistroSqlParser.BitOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#bitOperator}.
	 * @param ctx the parse tree
	 */
	void exitBitOperator(OpenDistroSqlParser.BitOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void enterMathOperator(OpenDistroSqlParser.MathOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#mathOperator}.
	 * @param ctx the parse tree
	 */
	void exitMathOperator(OpenDistroSqlParser.MathOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#charsetNameBase}.
	 * @param ctx the parse tree
	 */
	void enterCharsetNameBase(OpenDistroSqlParser.CharsetNameBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#charsetNameBase}.
	 * @param ctx the parse tree
	 */
	void exitCharsetNameBase(OpenDistroSqlParser.CharsetNameBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#intervalTypeBase}.
	 * @param ctx the parse tree
	 */
	void enterIntervalTypeBase(OpenDistroSqlParser.IntervalTypeBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#intervalTypeBase}.
	 * @param ctx the parse tree
	 */
	void exitIntervalTypeBase(OpenDistroSqlParser.IntervalTypeBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dataTypeBase}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeBase(OpenDistroSqlParser.DataTypeBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dataTypeBase}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeBase(OpenDistroSqlParser.DataTypeBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void enterKeywordsCanBeId(OpenDistroSqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#keywordsCanBeId}.
	 * @param ctx the parse tree
	 */
	void exitKeywordsCanBeId(OpenDistroSqlParser.KeywordsCanBeIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#functionNameBase}.
	 * @param ctx the parse tree
	 */
	void enterFunctionNameBase(OpenDistroSqlParser.FunctionNameBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#functionNameBase}.
	 * @param ctx the parse tree
	 */
	void exitFunctionNameBase(OpenDistroSqlParser.FunctionNameBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#esFunctionNameBase}.
	 * @param ctx the parse tree
	 */
	void enterEsFunctionNameBase(OpenDistroSqlParser.EsFunctionNameBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#esFunctionNameBase}.
	 * @param ctx the parse tree
	 */
	void exitEsFunctionNameBase(OpenDistroSqlParser.EsFunctionNameBaseContext ctx);
}