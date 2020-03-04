// Generated from /Users/penghuo/opendistro/poc-ppl/src/main/antlr/PPLParser.g4 by ANTLR 4.8
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link PPLParser}.
 */
public interface PPLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link PPLParser#pplStatement}.
	 * @param ctx the parse tree
	 */
	void enterPplStatement(PPLParser.PplStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#pplStatement}.
	 * @param ctx the parse tree
	 */
	void exitPplStatement(PPLParser.PplStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#searchCommands}.
	 * @param ctx the parse tree
	 */
	void enterSearchCommands(PPLParser.SearchCommandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#searchCommands}.
	 * @param ctx the parse tree
	 */
	void exitSearchCommands(PPLParser.SearchCommandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#reportsCommands}.
	 * @param ctx the parse tree
	 */
	void enterReportsCommands(PPLParser.ReportsCommandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#reportsCommands}.
	 * @param ctx the parse tree
	 */
	void exitReportsCommands(PPLParser.ReportsCommandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#resultsCommands}.
	 * @param ctx the parse tree
	 */
	void enterResultsCommands(PPLParser.ResultsCommandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#resultsCommands}.
	 * @param ctx the parse tree
	 */
	void exitResultsCommands(PPLParser.ResultsCommandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#filteringCommands}.
	 * @param ctx the parse tree
	 */
	void enterFilteringCommands(PPLParser.FilteringCommandsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#filteringCommands}.
	 * @param ctx the parse tree
	 */
	void exitFilteringCommands(PPLParser.FilteringCommandsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#searchCommand}.
	 * @param ctx the parse tree
	 */
	void enterSearchCommand(PPLParser.SearchCommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#searchCommand}.
	 * @param ctx the parse tree
	 */
	void exitSearchCommand(PPLParser.SearchCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void enterFromClause(PPLParser.FromClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#fromClause}.
	 * @param ctx the parse tree
	 */
	void exitFromClause(PPLParser.FromClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#topCommand}.
	 * @param ctx the parse tree
	 */
	void enterTopCommand(PPLParser.TopCommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#topCommand}.
	 * @param ctx the parse tree
	 */
	void exitTopCommand(PPLParser.TopCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#fromCommand}.
	 * @param ctx the parse tree
	 */
	void enterFromCommand(PPLParser.FromCommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#fromCommand}.
	 * @param ctx the parse tree
	 */
	void exitFromCommand(PPLParser.FromCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#whereCommand}.
	 * @param ctx the parse tree
	 */
	void enterWhereCommand(PPLParser.WhereCommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#whereCommand}.
	 * @param ctx the parse tree
	 */
	void exitWhereCommand(PPLParser.WhereCommandContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalExpression(PPLParser.LogicalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#logicalExpression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalExpression(PPLParser.LogicalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void enterBooleanExpression(PPLParser.BooleanExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#booleanExpression}.
	 * @param ctx the parse tree
	 */
	void exitBooleanExpression(PPLParser.BooleanExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#comparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpression(PPLParser.ComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#comparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpression(PPLParser.ComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#evalExpression}.
	 * @param ctx the parse tree
	 */
	void enterEvalExpression(PPLParser.EvalExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#evalExpression}.
	 * @param ctx the parse tree
	 */
	void exitEvalExpression(PPLParser.EvalExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(PPLParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(PPLParser.ExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#evalFunctionCall}.
	 * @param ctx the parse tree
	 */
	void enterEvalFunctionCall(PPLParser.EvalFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#evalFunctionCall}.
	 * @param ctx the parse tree
	 */
	void exitEvalFunctionCall(PPLParser.EvalFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code countfieldTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 */
	void enterCountfieldTopOption(PPLParser.CountfieldTopOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code countfieldTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 */
	void exitCountfieldTopOption(PPLParser.CountfieldTopOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code limitTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 */
	void enterLimitTopOption(PPLParser.LimitTopOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code limitTopOption}
	 * labeled alternative in {@link PPLParser#topOptions}.
	 * @param ctx the parse tree
	 */
	void exitLimitTopOption(PPLParser.LimitTopOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#datasetType}.
	 * @param ctx the parse tree
	 */
	void enterDatasetType(PPLParser.DatasetTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#datasetType}.
	 * @param ctx the parse tree
	 */
	void exitDatasetType(PPLParser.DatasetTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#datasetName}.
	 * @param ctx the parse tree
	 */
	void enterDatasetName(PPLParser.DatasetNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#datasetName}.
	 * @param ctx the parse tree
	 */
	void exitDatasetName(PPLParser.DatasetNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#byClause}.
	 * @param ctx the parse tree
	 */
	void enterByClause(PPLParser.ByClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#byClause}.
	 * @param ctx the parse tree
	 */
	void exitByClause(PPLParser.ByClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void enterBooleanLiteral(PPLParser.BooleanLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#booleanLiteral}.
	 * @param ctx the parse tree
	 */
	void exitBooleanLiteral(PPLParser.BooleanLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(PPLParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(PPLParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#evalFunctionName}.
	 * @param ctx the parse tree
	 */
	void enterEvalFunctionName(PPLParser.EvalFunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#evalFunctionName}.
	 * @param ctx the parse tree
	 */
	void exitEvalFunctionName(PPLParser.EvalFunctionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArgs(PPLParser.FunctionArgsContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#functionArgs}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArgs(PPLParser.FunctionArgsContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void enterFunctionArg(PPLParser.FunctionArgContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#functionArg}.
	 * @param ctx the parse tree
	 */
	void exitFunctionArg(PPLParser.FunctionArgContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#fieldExpression}.
	 * @param ctx the parse tree
	 */
	void enterFieldExpression(PPLParser.FieldExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#fieldExpression}.
	 * @param ctx the parse tree
	 */
	void exitFieldExpression(PPLParser.FieldExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void enterFieldList(PPLParser.FieldListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#fieldList}.
	 * @param ctx the parse tree
	 */
	void exitFieldList(PPLParser.FieldListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterValueExpression(PPLParser.ValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitValueExpression(PPLParser.ValueExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#valueList}.
	 * @param ctx the parse tree
	 */
	void enterValueList(PPLParser.ValueListContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#valueList}.
	 * @param ctx the parse tree
	 */
	void exitValueList(PPLParser.ValueListContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#constant}.
	 * @param ctx the parse tree
	 */
	void enterConstant(PPLParser.ConstantContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#constant}.
	 * @param ctx the parse tree
	 */
	void exitConstant(PPLParser.ConstantContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void enterLiteralValue(PPLParser.LiteralValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void exitLiteralValue(PPLParser.LiteralValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void enterStringLiteral(PPLParser.StringLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#stringLiteral}.
	 * @param ctx the parse tree
	 */
	void exitStringLiteral(PPLParser.StringLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void enterDecimalLiteral(PPLParser.DecimalLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#decimalLiteral}.
	 * @param ctx the parse tree
	 */
	void exitDecimalLiteral(PPLParser.DecimalLiteralContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void enterFullColumnName(PPLParser.FullColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#fullColumnName}.
	 * @param ctx the parse tree
	 */
	void exitFullColumnName(PPLParser.FullColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#simpleId}.
	 * @param ctx the parse tree
	 */
	void enterSimpleId(PPLParser.SimpleIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#simpleId}.
	 * @param ctx the parse tree
	 */
	void exitSimpleId(PPLParser.SimpleIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link PPLParser#evalFunctionNameBase}.
	 * @param ctx the parse tree
	 */
	void enterEvalFunctionNameBase(PPLParser.EvalFunctionNameBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link PPLParser#evalFunctionNameBase}.
	 * @param ctx the parse tree
	 */
	void exitEvalFunctionNameBase(PPLParser.EvalFunctionNameBaseContext ctx);
}