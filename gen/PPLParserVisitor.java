// Generated from /Users/penghuo/opendistro/poc-ppl/src/main/antlr/PPLParser.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link PPLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface PPLParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link PPLParser#pplStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPplStatement(PPLParser.PplStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#searchCommands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchCommands(PPLParser.SearchCommandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#reportsCommands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReportsCommands(PPLParser.ReportsCommandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#resultsCommands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitResultsCommands(PPLParser.ResultsCommandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#filteringCommands}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFilteringCommands(PPLParser.FilteringCommandsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#searchCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSearchCommand(PPLParser.SearchCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#fromClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromClause(PPLParser.FromClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#topCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTopCommand(PPLParser.TopCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#fromCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFromCommand(PPLParser.FromCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#whereCommand}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhereCommand(PPLParser.WhereCommandContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#logicalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalExpression(PPLParser.LogicalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#booleanExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanExpression(PPLParser.BooleanExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#comparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpression(PPLParser.ComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#evalExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalExpression(PPLParser.EvalExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(PPLParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#evalFunctionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalFunctionCall(PPLParser.EvalFunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by the {@code countfieldTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCountfieldTopOption(PPLParser.CountfieldTopOptionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code limitTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLimitTopOption(PPLParser.LimitTopOptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#datasetType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatasetType(PPLParser.DatasetTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#datasetName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDatasetName(PPLParser.DatasetNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#byClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitByClause(PPLParser.ByClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBooleanLiteral(PPLParser.BooleanLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(PPLParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#evalFunctionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalFunctionName(PPLParser.EvalFunctionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#functionArgs}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArgs(PPLParser.FunctionArgsContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#functionArg}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionArg(PPLParser.FunctionArgContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#fieldExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldExpression(PPLParser.FieldExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#fieldList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldList(PPLParser.FieldListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueExpression(PPLParser.ValueExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#valueList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueList(PPLParser.ValueListContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#constant}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConstant(PPLParser.ConstantContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#literalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralValue(PPLParser.LiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#stringLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLiteral(PPLParser.StringLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDecimalLiteral(PPLParser.DecimalLiteralContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#fullColumnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullColumnName(PPLParser.FullColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#simpleId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimpleId(PPLParser.SimpleIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link PPLParser#evalFunctionNameBase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEvalFunctionNameBase(PPLParser.EvalFunctionNameBaseContext ctx);
}