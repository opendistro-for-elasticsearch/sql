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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#sqlStatements}.
	 * @param ctx the parse tree
	 */
	void enterSqlStatements(OpenDistroSqlParser.SqlStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#sqlStatements}.
	 * @param ctx the parse tree
	 */
	void exitSqlStatements(OpenDistroSqlParser.SqlStatementsContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void enterEmptyStatement(OpenDistroSqlParser.EmptyStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#emptyStatement}.
	 * @param ctx the parse tree
	 */
	void exitEmptyStatement(OpenDistroSqlParser.EmptyStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void enterDdlStatement(OpenDistroSqlParser.DdlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#ddlStatement}.
	 * @param ctx the parse tree
	 */
	void exitDdlStatement(OpenDistroSqlParser.DdlStatementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void enterTransactionStatement(OpenDistroSqlParser.TransactionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#transactionStatement}.
	 * @param ctx the parse tree
	 */
	void exitTransactionStatement(OpenDistroSqlParser.TransactionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#replicationStatement}.
	 * @param ctx the parse tree
	 */
	void enterReplicationStatement(OpenDistroSqlParser.ReplicationStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#replicationStatement}.
	 * @param ctx the parse tree
	 */
	void exitReplicationStatement(OpenDistroSqlParser.ReplicationStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#preparedStatement}.
	 * @param ctx the parse tree
	 */
	void enterPreparedStatement(OpenDistroSqlParser.PreparedStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#preparedStatement}.
	 * @param ctx the parse tree
	 */
	void exitPreparedStatement(OpenDistroSqlParser.PreparedStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void enterCompoundStatement(OpenDistroSqlParser.CompoundStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#compoundStatement}.
	 * @param ctx the parse tree
	 */
	void exitCompoundStatement(OpenDistroSqlParser.CompoundStatementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabase(OpenDistroSqlParser.CreateDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createDatabase}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabase(OpenDistroSqlParser.CreateDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createEvent}.
	 * @param ctx the parse tree
	 */
	void enterCreateEvent(OpenDistroSqlParser.CreateEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createEvent}.
	 * @param ctx the parse tree
	 */
	void exitCreateEvent(OpenDistroSqlParser.CreateEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createIndex}.
	 * @param ctx the parse tree
	 */
	void enterCreateIndex(OpenDistroSqlParser.CreateIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createIndex}.
	 * @param ctx the parse tree
	 */
	void exitCreateIndex(OpenDistroSqlParser.CreateIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void enterCreateLogfileGroup(OpenDistroSqlParser.CreateLogfileGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void exitCreateLogfileGroup(OpenDistroSqlParser.CreateLogfileGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void enterCreateProcedure(OpenDistroSqlParser.CreateProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createProcedure}.
	 * @param ctx the parse tree
	 */
	void exitCreateProcedure(OpenDistroSqlParser.CreateProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createFunction}.
	 * @param ctx the parse tree
	 */
	void enterCreateFunction(OpenDistroSqlParser.CreateFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createFunction}.
	 * @param ctx the parse tree
	 */
	void exitCreateFunction(OpenDistroSqlParser.CreateFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createServer}.
	 * @param ctx the parse tree
	 */
	void enterCreateServer(OpenDistroSqlParser.CreateServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createServer}.
	 * @param ctx the parse tree
	 */
	void exitCreateServer(OpenDistroSqlParser.CreateServerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code copyCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterCopyCreateTable(OpenDistroSqlParser.CopyCreateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code copyCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitCopyCreateTable(OpenDistroSqlParser.CopyCreateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code queryCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterQueryCreateTable(OpenDistroSqlParser.QueryCreateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code queryCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitQueryCreateTable(OpenDistroSqlParser.QueryCreateTableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code columnCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void enterColumnCreateTable(OpenDistroSqlParser.ColumnCreateTableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code columnCreateTable}
	 * labeled alternative in {@link OpenDistroSqlParser#createTable}.
	 * @param ctx the parse tree
	 */
	void exitColumnCreateTable(OpenDistroSqlParser.ColumnCreateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createTablespaceInnodb}.
	 * @param ctx the parse tree
	 */
	void enterCreateTablespaceInnodb(OpenDistroSqlParser.CreateTablespaceInnodbContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createTablespaceInnodb}.
	 * @param ctx the parse tree
	 */
	void exitCreateTablespaceInnodb(OpenDistroSqlParser.CreateTablespaceInnodbContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createTablespaceNdb}.
	 * @param ctx the parse tree
	 */
	void enterCreateTablespaceNdb(OpenDistroSqlParser.CreateTablespaceNdbContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createTablespaceNdb}.
	 * @param ctx the parse tree
	 */
	void exitCreateTablespaceNdb(OpenDistroSqlParser.CreateTablespaceNdbContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void enterCreateTrigger(OpenDistroSqlParser.CreateTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createTrigger}.
	 * @param ctx the parse tree
	 */
	void exitCreateTrigger(OpenDistroSqlParser.CreateTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createView}.
	 * @param ctx the parse tree
	 */
	void enterCreateView(OpenDistroSqlParser.CreateViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createView}.
	 * @param ctx the parse tree
	 */
	void exitCreateView(OpenDistroSqlParser.CreateViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createDatabaseOption}.
	 * @param ctx the parse tree
	 */
	void enterCreateDatabaseOption(OpenDistroSqlParser.CreateDatabaseOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createDatabaseOption}.
	 * @param ctx the parse tree
	 */
	void exitCreateDatabaseOption(OpenDistroSqlParser.CreateDatabaseOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#ownerStatement}.
	 * @param ctx the parse tree
	 */
	void enterOwnerStatement(OpenDistroSqlParser.OwnerStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#ownerStatement}.
	 * @param ctx the parse tree
	 */
	void exitOwnerStatement(OpenDistroSqlParser.OwnerStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code preciseSchedule}
	 * labeled alternative in {@link OpenDistroSqlParser#scheduleExpression}.
	 * @param ctx the parse tree
	 */
	void enterPreciseSchedule(OpenDistroSqlParser.PreciseScheduleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code preciseSchedule}
	 * labeled alternative in {@link OpenDistroSqlParser#scheduleExpression}.
	 * @param ctx the parse tree
	 */
	void exitPreciseSchedule(OpenDistroSqlParser.PreciseScheduleContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intervalSchedule}
	 * labeled alternative in {@link OpenDistroSqlParser#scheduleExpression}.
	 * @param ctx the parse tree
	 */
	void enterIntervalSchedule(OpenDistroSqlParser.IntervalScheduleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intervalSchedule}
	 * labeled alternative in {@link OpenDistroSqlParser#scheduleExpression}.
	 * @param ctx the parse tree
	 */
	void exitIntervalSchedule(OpenDistroSqlParser.IntervalScheduleContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#timestampValue}.
	 * @param ctx the parse tree
	 */
	void enterTimestampValue(OpenDistroSqlParser.TimestampValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#timestampValue}.
	 * @param ctx the parse tree
	 */
	void exitTimestampValue(OpenDistroSqlParser.TimestampValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#intervalExpr}.
	 * @param ctx the parse tree
	 */
	void enterIntervalExpr(OpenDistroSqlParser.IntervalExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#intervalExpr}.
	 * @param ctx the parse tree
	 */
	void exitIntervalExpr(OpenDistroSqlParser.IntervalExprContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#enableType}.
	 * @param ctx the parse tree
	 */
	void enterEnableType(OpenDistroSqlParser.EnableTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#enableType}.
	 * @param ctx the parse tree
	 */
	void exitEnableType(OpenDistroSqlParser.EnableTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexType}.
	 * @param ctx the parse tree
	 */
	void enterIndexType(OpenDistroSqlParser.IndexTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexType}.
	 * @param ctx the parse tree
	 */
	void exitIndexType(OpenDistroSqlParser.IndexTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexOption}.
	 * @param ctx the parse tree
	 */
	void enterIndexOption(OpenDistroSqlParser.IndexOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexOption}.
	 * @param ctx the parse tree
	 */
	void exitIndexOption(OpenDistroSqlParser.IndexOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#procedureParameter}.
	 * @param ctx the parse tree
	 */
	void enterProcedureParameter(OpenDistroSqlParser.ProcedureParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#procedureParameter}.
	 * @param ctx the parse tree
	 */
	void exitProcedureParameter(OpenDistroSqlParser.ProcedureParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#functionParameter}.
	 * @param ctx the parse tree
	 */
	void enterFunctionParameter(OpenDistroSqlParser.FunctionParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#functionParameter}.
	 * @param ctx the parse tree
	 */
	void exitFunctionParameter(OpenDistroSqlParser.FunctionParameterContext ctx);
	/**
	 * Enter a parse tree produced by the {@code routineComment}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void enterRoutineComment(OpenDistroSqlParser.RoutineCommentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code routineComment}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void exitRoutineComment(OpenDistroSqlParser.RoutineCommentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code routineLanguage}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void enterRoutineLanguage(OpenDistroSqlParser.RoutineLanguageContext ctx);
	/**
	 * Exit a parse tree produced by the {@code routineLanguage}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void exitRoutineLanguage(OpenDistroSqlParser.RoutineLanguageContext ctx);
	/**
	 * Enter a parse tree produced by the {@code routineBehavior}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void enterRoutineBehavior(OpenDistroSqlParser.RoutineBehaviorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code routineBehavior}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void exitRoutineBehavior(OpenDistroSqlParser.RoutineBehaviorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code routineData}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void enterRoutineData(OpenDistroSqlParser.RoutineDataContext ctx);
	/**
	 * Exit a parse tree produced by the {@code routineData}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void exitRoutineData(OpenDistroSqlParser.RoutineDataContext ctx);
	/**
	 * Enter a parse tree produced by the {@code routineSecurity}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void enterRoutineSecurity(OpenDistroSqlParser.RoutineSecurityContext ctx);
	/**
	 * Exit a parse tree produced by the {@code routineSecurity}
	 * labeled alternative in {@link OpenDistroSqlParser#routineOption}.
	 * @param ctx the parse tree
	 */
	void exitRoutineSecurity(OpenDistroSqlParser.RoutineSecurityContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#serverOption}.
	 * @param ctx the parse tree
	 */
	void enterServerOption(OpenDistroSqlParser.ServerOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#serverOption}.
	 * @param ctx the parse tree
	 */
	void exitServerOption(OpenDistroSqlParser.ServerOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createDefinitions}.
	 * @param ctx the parse tree
	 */
	void enterCreateDefinitions(OpenDistroSqlParser.CreateDefinitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createDefinitions}.
	 * @param ctx the parse tree
	 */
	void exitCreateDefinitions(OpenDistroSqlParser.CreateDefinitionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code columnDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDeclaration(OpenDistroSqlParser.ColumnDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code columnDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDeclaration(OpenDistroSqlParser.ColumnDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code constraintDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void enterConstraintDeclaration(OpenDistroSqlParser.ConstraintDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code constraintDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void exitConstraintDeclaration(OpenDistroSqlParser.ConstraintDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code indexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void enterIndexDeclaration(OpenDistroSqlParser.IndexDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code indexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#createDefinition}.
	 * @param ctx the parse tree
	 */
	void exitIndexDeclaration(OpenDistroSqlParser.IndexDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterColumnDefinition(OpenDistroSqlParser.ColumnDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#columnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitColumnDefinition(OpenDistroSqlParser.ColumnDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterNullColumnConstraint(OpenDistroSqlParser.NullColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitNullColumnConstraint(OpenDistroSqlParser.NullColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code defaultColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterDefaultColumnConstraint(OpenDistroSqlParser.DefaultColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code defaultColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitDefaultColumnConstraint(OpenDistroSqlParser.DefaultColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code autoIncrementColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterAutoIncrementColumnConstraint(OpenDistroSqlParser.AutoIncrementColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code autoIncrementColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitAutoIncrementColumnConstraint(OpenDistroSqlParser.AutoIncrementColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryKeyColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKeyColumnConstraint(OpenDistroSqlParser.PrimaryKeyColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryKeyColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKeyColumnConstraint(OpenDistroSqlParser.PrimaryKeyColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uniqueKeyColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterUniqueKeyColumnConstraint(OpenDistroSqlParser.UniqueKeyColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uniqueKeyColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitUniqueKeyColumnConstraint(OpenDistroSqlParser.UniqueKeyColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code commentColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterCommentColumnConstraint(OpenDistroSqlParser.CommentColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code commentColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitCommentColumnConstraint(OpenDistroSqlParser.CommentColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code formatColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterFormatColumnConstraint(OpenDistroSqlParser.FormatColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code formatColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitFormatColumnConstraint(OpenDistroSqlParser.FormatColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code storageColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterStorageColumnConstraint(OpenDistroSqlParser.StorageColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code storageColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitStorageColumnConstraint(OpenDistroSqlParser.StorageColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code referenceColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterReferenceColumnConstraint(OpenDistroSqlParser.ReferenceColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code referenceColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitReferenceColumnConstraint(OpenDistroSqlParser.ReferenceColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code collateColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterCollateColumnConstraint(OpenDistroSqlParser.CollateColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code collateColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitCollateColumnConstraint(OpenDistroSqlParser.CollateColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code generatedColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterGeneratedColumnConstraint(OpenDistroSqlParser.GeneratedColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code generatedColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitGeneratedColumnConstraint(OpenDistroSqlParser.GeneratedColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code serialDefaultColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void enterSerialDefaultColumnConstraint(OpenDistroSqlParser.SerialDefaultColumnConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code serialDefaultColumnConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#columnConstraint}.
	 * @param ctx the parse tree
	 */
	void exitSerialDefaultColumnConstraint(OpenDistroSqlParser.SerialDefaultColumnConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryKeyTableConstraint(OpenDistroSqlParser.PrimaryKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryKeyTableConstraint(OpenDistroSqlParser.PrimaryKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code uniqueKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterUniqueKeyTableConstraint(OpenDistroSqlParser.UniqueKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code uniqueKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitUniqueKeyTableConstraint(OpenDistroSqlParser.UniqueKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code foreignKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterForeignKeyTableConstraint(OpenDistroSqlParser.ForeignKeyTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code foreignKeyTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitForeignKeyTableConstraint(OpenDistroSqlParser.ForeignKeyTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code checkTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void enterCheckTableConstraint(OpenDistroSqlParser.CheckTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code checkTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#tableConstraint}.
	 * @param ctx the parse tree
	 */
	void exitCheckTableConstraint(OpenDistroSqlParser.CheckTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#referenceDefinition}.
	 * @param ctx the parse tree
	 */
	void enterReferenceDefinition(OpenDistroSqlParser.ReferenceDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#referenceDefinition}.
	 * @param ctx the parse tree
	 */
	void exitReferenceDefinition(OpenDistroSqlParser.ReferenceDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#referenceAction}.
	 * @param ctx the parse tree
	 */
	void enterReferenceAction(OpenDistroSqlParser.ReferenceActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#referenceAction}.
	 * @param ctx the parse tree
	 */
	void exitReferenceAction(OpenDistroSqlParser.ReferenceActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#referenceControlType}.
	 * @param ctx the parse tree
	 */
	void enterReferenceControlType(OpenDistroSqlParser.ReferenceControlTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#referenceControlType}.
	 * @param ctx the parse tree
	 */
	void exitReferenceControlType(OpenDistroSqlParser.ReferenceControlTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleIndexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#indexColumnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterSimpleIndexDeclaration(OpenDistroSqlParser.SimpleIndexDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleIndexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#indexColumnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitSimpleIndexDeclaration(OpenDistroSqlParser.SimpleIndexDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code specialIndexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#indexColumnDefinition}.
	 * @param ctx the parse tree
	 */
	void enterSpecialIndexDeclaration(OpenDistroSqlParser.SpecialIndexDeclarationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code specialIndexDeclaration}
	 * labeled alternative in {@link OpenDistroSqlParser#indexColumnDefinition}.
	 * @param ctx the parse tree
	 */
	void exitSpecialIndexDeclaration(OpenDistroSqlParser.SpecialIndexDeclarationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionEngine(OpenDistroSqlParser.TableOptionEngineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionEngine(OpenDistroSqlParser.TableOptionEngineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionAutoIncrement}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionAutoIncrement(OpenDistroSqlParser.TableOptionAutoIncrementContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionAutoIncrement}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionAutoIncrement(OpenDistroSqlParser.TableOptionAutoIncrementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionAverage}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionAverage(OpenDistroSqlParser.TableOptionAverageContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionAverage}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionAverage(OpenDistroSqlParser.TableOptionAverageContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionCharset(OpenDistroSqlParser.TableOptionCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionCharset(OpenDistroSqlParser.TableOptionCharsetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionChecksum}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionChecksum(OpenDistroSqlParser.TableOptionChecksumContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionChecksum}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionChecksum(OpenDistroSqlParser.TableOptionChecksumContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionCollate}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionCollate(OpenDistroSqlParser.TableOptionCollateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionCollate}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionCollate(OpenDistroSqlParser.TableOptionCollateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionComment}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionComment(OpenDistroSqlParser.TableOptionCommentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionComment}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionComment(OpenDistroSqlParser.TableOptionCommentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionCompression}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionCompression(OpenDistroSqlParser.TableOptionCompressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionCompression}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionCompression(OpenDistroSqlParser.TableOptionCompressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionConnection}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionConnection(OpenDistroSqlParser.TableOptionConnectionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionConnection}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionConnection(OpenDistroSqlParser.TableOptionConnectionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionDataDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionDataDirectory(OpenDistroSqlParser.TableOptionDataDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionDataDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionDataDirectory(OpenDistroSqlParser.TableOptionDataDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionDelay}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionDelay(OpenDistroSqlParser.TableOptionDelayContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionDelay}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionDelay(OpenDistroSqlParser.TableOptionDelayContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionEncryption}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionEncryption(OpenDistroSqlParser.TableOptionEncryptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionEncryption}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionEncryption(OpenDistroSqlParser.TableOptionEncryptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionIndexDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionIndexDirectory(OpenDistroSqlParser.TableOptionIndexDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionIndexDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionIndexDirectory(OpenDistroSqlParser.TableOptionIndexDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionInsertMethod}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionInsertMethod(OpenDistroSqlParser.TableOptionInsertMethodContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionInsertMethod}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionInsertMethod(OpenDistroSqlParser.TableOptionInsertMethodContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionKeyBlockSize}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionKeyBlockSize(OpenDistroSqlParser.TableOptionKeyBlockSizeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionKeyBlockSize}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionKeyBlockSize(OpenDistroSqlParser.TableOptionKeyBlockSizeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionMaxRows}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionMaxRows(OpenDistroSqlParser.TableOptionMaxRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionMaxRows}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionMaxRows(OpenDistroSqlParser.TableOptionMaxRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionMinRows}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionMinRows(OpenDistroSqlParser.TableOptionMinRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionMinRows}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionMinRows(OpenDistroSqlParser.TableOptionMinRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionPackKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionPackKeys(OpenDistroSqlParser.TableOptionPackKeysContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionPackKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionPackKeys(OpenDistroSqlParser.TableOptionPackKeysContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionPassword}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionPassword(OpenDistroSqlParser.TableOptionPasswordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionPassword}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionPassword(OpenDistroSqlParser.TableOptionPasswordContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionRowFormat}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionRowFormat(OpenDistroSqlParser.TableOptionRowFormatContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionRowFormat}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionRowFormat(OpenDistroSqlParser.TableOptionRowFormatContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionRecalculation}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionRecalculation(OpenDistroSqlParser.TableOptionRecalculationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionRecalculation}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionRecalculation(OpenDistroSqlParser.TableOptionRecalculationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionPersistent}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionPersistent(OpenDistroSqlParser.TableOptionPersistentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionPersistent}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionPersistent(OpenDistroSqlParser.TableOptionPersistentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionSamplePage}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionSamplePage(OpenDistroSqlParser.TableOptionSamplePageContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionSamplePage}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionSamplePage(OpenDistroSqlParser.TableOptionSamplePageContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionTablespace(OpenDistroSqlParser.TableOptionTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionTablespace(OpenDistroSqlParser.TableOptionTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableOptionUnion}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void enterTableOptionUnion(OpenDistroSqlParser.TableOptionUnionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableOptionUnion}
	 * labeled alternative in {@link OpenDistroSqlParser#tableOption}.
	 * @param ctx the parse tree
	 */
	void exitTableOptionUnion(OpenDistroSqlParser.TableOptionUnionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tablespaceStorage}.
	 * @param ctx the parse tree
	 */
	void enterTablespaceStorage(OpenDistroSqlParser.TablespaceStorageContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tablespaceStorage}.
	 * @param ctx the parse tree
	 */
	void exitTablespaceStorage(OpenDistroSqlParser.TablespaceStorageContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#partitionDefinitions}.
	 * @param ctx the parse tree
	 */
	void enterPartitionDefinitions(OpenDistroSqlParser.PartitionDefinitionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#partitionDefinitions}.
	 * @param ctx the parse tree
	 */
	void exitPartitionDefinitions(OpenDistroSqlParser.PartitionDefinitionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionFunctionHash}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFunctionHash(OpenDistroSqlParser.PartitionFunctionHashContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionFunctionHash}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFunctionHash(OpenDistroSqlParser.PartitionFunctionHashContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionFunctionKey}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFunctionKey(OpenDistroSqlParser.PartitionFunctionKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionFunctionKey}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFunctionKey(OpenDistroSqlParser.PartitionFunctionKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionFunctionRange}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFunctionRange(OpenDistroSqlParser.PartitionFunctionRangeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionFunctionRange}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFunctionRange(OpenDistroSqlParser.PartitionFunctionRangeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionFunctionList}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionFunctionList(OpenDistroSqlParser.PartitionFunctionListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionFunctionList}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionFunctionList(OpenDistroSqlParser.PartitionFunctionListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subPartitionFunctionHash}
	 * labeled alternative in {@link OpenDistroSqlParser#subpartitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterSubPartitionFunctionHash(OpenDistroSqlParser.SubPartitionFunctionHashContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subPartitionFunctionHash}
	 * labeled alternative in {@link OpenDistroSqlParser#subpartitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitSubPartitionFunctionHash(OpenDistroSqlParser.SubPartitionFunctionHashContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subPartitionFunctionKey}
	 * labeled alternative in {@link OpenDistroSqlParser#subpartitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterSubPartitionFunctionKey(OpenDistroSqlParser.SubPartitionFunctionKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subPartitionFunctionKey}
	 * labeled alternative in {@link OpenDistroSqlParser#subpartitionFunctionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitSubPartitionFunctionKey(OpenDistroSqlParser.SubPartitionFunctionKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionComparision}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionComparision(OpenDistroSqlParser.PartitionComparisionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionComparision}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionComparision(OpenDistroSqlParser.PartitionComparisionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionListAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionListAtom(OpenDistroSqlParser.PartitionListAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionListAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionListAtom(OpenDistroSqlParser.PartitionListAtomContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionListVector}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionListVector(OpenDistroSqlParser.PartitionListVectorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionListVector}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionListVector(OpenDistroSqlParser.PartitionListVectorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionSimple}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterPartitionSimple(OpenDistroSqlParser.PartitionSimpleContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionSimple}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitPartitionSimple(OpenDistroSqlParser.PartitionSimpleContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#partitionDefinerAtom}.
	 * @param ctx the parse tree
	 */
	void enterPartitionDefinerAtom(OpenDistroSqlParser.PartitionDefinerAtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#partitionDefinerAtom}.
	 * @param ctx the parse tree
	 */
	void exitPartitionDefinerAtom(OpenDistroSqlParser.PartitionDefinerAtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#partitionDefinerVector}.
	 * @param ctx the parse tree
	 */
	void enterPartitionDefinerVector(OpenDistroSqlParser.PartitionDefinerVectorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#partitionDefinerVector}.
	 * @param ctx the parse tree
	 */
	void exitPartitionDefinerVector(OpenDistroSqlParser.PartitionDefinerVectorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#subpartitionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterSubpartitionDefinition(OpenDistroSqlParser.SubpartitionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#subpartitionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitSubpartitionDefinition(OpenDistroSqlParser.SubpartitionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionEngine(OpenDistroSqlParser.PartitionOptionEngineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionEngine(OpenDistroSqlParser.PartitionOptionEngineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionComment}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionComment(OpenDistroSqlParser.PartitionOptionCommentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionComment}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionComment(OpenDistroSqlParser.PartitionOptionCommentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionDataDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionDataDirectory(OpenDistroSqlParser.PartitionOptionDataDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionDataDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionDataDirectory(OpenDistroSqlParser.PartitionOptionDataDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionIndexDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionIndexDirectory(OpenDistroSqlParser.PartitionOptionIndexDirectoryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionIndexDirectory}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionIndexDirectory(OpenDistroSqlParser.PartitionOptionIndexDirectoryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionMaxRows}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionMaxRows(OpenDistroSqlParser.PartitionOptionMaxRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionMaxRows}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionMaxRows(OpenDistroSqlParser.PartitionOptionMaxRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionMinRows}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionMinRows(OpenDistroSqlParser.PartitionOptionMinRowsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionMinRows}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionMinRows(OpenDistroSqlParser.PartitionOptionMinRowsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionTablespace(OpenDistroSqlParser.PartitionOptionTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionTablespace(OpenDistroSqlParser.PartitionOptionTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code partitionOptionNodeGroup}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void enterPartitionOptionNodeGroup(OpenDistroSqlParser.PartitionOptionNodeGroupContext ctx);
	/**
	 * Exit a parse tree produced by the {@code partitionOptionNodeGroup}
	 * labeled alternative in {@link OpenDistroSqlParser#partitionOption}.
	 * @param ctx the parse tree
	 */
	void exitPartitionOptionNodeGroup(OpenDistroSqlParser.PartitionOptionNodeGroupContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterSimpleDatabase}
	 * labeled alternative in {@link OpenDistroSqlParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void enterAlterSimpleDatabase(OpenDistroSqlParser.AlterSimpleDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterSimpleDatabase}
	 * labeled alternative in {@link OpenDistroSqlParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void exitAlterSimpleDatabase(OpenDistroSqlParser.AlterSimpleDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterUpgradeName}
	 * labeled alternative in {@link OpenDistroSqlParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void enterAlterUpgradeName(OpenDistroSqlParser.AlterUpgradeNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterUpgradeName}
	 * labeled alternative in {@link OpenDistroSqlParser#alterDatabase}.
	 * @param ctx the parse tree
	 */
	void exitAlterUpgradeName(OpenDistroSqlParser.AlterUpgradeNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterEvent}.
	 * @param ctx the parse tree
	 */
	void enterAlterEvent(OpenDistroSqlParser.AlterEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterEvent}.
	 * @param ctx the parse tree
	 */
	void exitAlterEvent(OpenDistroSqlParser.AlterEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void enterAlterFunction(OpenDistroSqlParser.AlterFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterFunction}.
	 * @param ctx the parse tree
	 */
	void exitAlterFunction(OpenDistroSqlParser.AlterFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterInstance}.
	 * @param ctx the parse tree
	 */
	void enterAlterInstance(OpenDistroSqlParser.AlterInstanceContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterInstance}.
	 * @param ctx the parse tree
	 */
	void exitAlterInstance(OpenDistroSqlParser.AlterInstanceContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void enterAlterLogfileGroup(OpenDistroSqlParser.AlterLogfileGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void exitAlterLogfileGroup(OpenDistroSqlParser.AlterLogfileGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void enterAlterProcedure(OpenDistroSqlParser.AlterProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterProcedure}.
	 * @param ctx the parse tree
	 */
	void exitAlterProcedure(OpenDistroSqlParser.AlterProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterServer}.
	 * @param ctx the parse tree
	 */
	void enterAlterServer(OpenDistroSqlParser.AlterServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterServer}.
	 * @param ctx the parse tree
	 */
	void exitAlterServer(OpenDistroSqlParser.AlterServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterTable}.
	 * @param ctx the parse tree
	 */
	void enterAlterTable(OpenDistroSqlParser.AlterTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterTable}.
	 * @param ctx the parse tree
	 */
	void exitAlterTable(OpenDistroSqlParser.AlterTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void enterAlterTablespace(OpenDistroSqlParser.AlterTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterTablespace}.
	 * @param ctx the parse tree
	 */
	void exitAlterTablespace(OpenDistroSqlParser.AlterTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#alterView}.
	 * @param ctx the parse tree
	 */
	void enterAlterView(OpenDistroSqlParser.AlterViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#alterView}.
	 * @param ctx the parse tree
	 */
	void exitAlterView(OpenDistroSqlParser.AlterViewContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByTableOption}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByTableOption(OpenDistroSqlParser.AlterByTableOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByTableOption}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByTableOption(OpenDistroSqlParser.AlterByTableOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddColumn(OpenDistroSqlParser.AlterByAddColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddColumn(OpenDistroSqlParser.AlterByAddColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddColumns}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddColumns(OpenDistroSqlParser.AlterByAddColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddColumns}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddColumns(OpenDistroSqlParser.AlterByAddColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddIndex(OpenDistroSqlParser.AlterByAddIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddIndex(OpenDistroSqlParser.AlterByAddIndexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddPrimaryKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddPrimaryKey(OpenDistroSqlParser.AlterByAddPrimaryKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddPrimaryKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddPrimaryKey(OpenDistroSqlParser.AlterByAddPrimaryKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddUniqueKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddUniqueKey(OpenDistroSqlParser.AlterByAddUniqueKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddUniqueKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddUniqueKey(OpenDistroSqlParser.AlterByAddUniqueKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddSpecialIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddSpecialIndex(OpenDistroSqlParser.AlterByAddSpecialIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddSpecialIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddSpecialIndex(OpenDistroSqlParser.AlterByAddSpecialIndexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddForeignKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddForeignKey(OpenDistroSqlParser.AlterByAddForeignKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddForeignKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddForeignKey(OpenDistroSqlParser.AlterByAddForeignKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddCheckTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddCheckTableConstraint(OpenDistroSqlParser.AlterByAddCheckTableConstraintContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddCheckTableConstraint}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddCheckTableConstraint(OpenDistroSqlParser.AlterByAddCheckTableConstraintContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterBySetAlgorithm}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterBySetAlgorithm(OpenDistroSqlParser.AlterBySetAlgorithmContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterBySetAlgorithm}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterBySetAlgorithm(OpenDistroSqlParser.AlterBySetAlgorithmContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByChangeDefault}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByChangeDefault(OpenDistroSqlParser.AlterByChangeDefaultContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByChangeDefault}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByChangeDefault(OpenDistroSqlParser.AlterByChangeDefaultContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByChangeColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByChangeColumn(OpenDistroSqlParser.AlterByChangeColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByChangeColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByChangeColumn(OpenDistroSqlParser.AlterByChangeColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRenameColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRenameColumn(OpenDistroSqlParser.AlterByRenameColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRenameColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRenameColumn(OpenDistroSqlParser.AlterByRenameColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByLock}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByLock(OpenDistroSqlParser.AlterByLockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByLock}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByLock(OpenDistroSqlParser.AlterByLockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByModifyColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByModifyColumn(OpenDistroSqlParser.AlterByModifyColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByModifyColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByModifyColumn(OpenDistroSqlParser.AlterByModifyColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDropColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDropColumn(OpenDistroSqlParser.AlterByDropColumnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDropColumn}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDropColumn(OpenDistroSqlParser.AlterByDropColumnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDropPrimaryKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDropPrimaryKey(OpenDistroSqlParser.AlterByDropPrimaryKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDropPrimaryKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDropPrimaryKey(OpenDistroSqlParser.AlterByDropPrimaryKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRenameIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRenameIndex(OpenDistroSqlParser.AlterByRenameIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRenameIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRenameIndex(OpenDistroSqlParser.AlterByRenameIndexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDropIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDropIndex(OpenDistroSqlParser.AlterByDropIndexContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDropIndex}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDropIndex(OpenDistroSqlParser.AlterByDropIndexContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDropForeignKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDropForeignKey(OpenDistroSqlParser.AlterByDropForeignKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDropForeignKey}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDropForeignKey(OpenDistroSqlParser.AlterByDropForeignKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDisableKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDisableKeys(OpenDistroSqlParser.AlterByDisableKeysContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDisableKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDisableKeys(OpenDistroSqlParser.AlterByDisableKeysContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByEnableKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByEnableKeys(OpenDistroSqlParser.AlterByEnableKeysContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByEnableKeys}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByEnableKeys(OpenDistroSqlParser.AlterByEnableKeysContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRename}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRename(OpenDistroSqlParser.AlterByRenameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRename}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRename(OpenDistroSqlParser.AlterByRenameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByOrder}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByOrder(OpenDistroSqlParser.AlterByOrderContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByOrder}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByOrder(OpenDistroSqlParser.AlterByOrderContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByConvertCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByConvertCharset(OpenDistroSqlParser.AlterByConvertCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByConvertCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByConvertCharset(OpenDistroSqlParser.AlterByConvertCharsetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDefaultCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDefaultCharset(OpenDistroSqlParser.AlterByDefaultCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDefaultCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDefaultCharset(OpenDistroSqlParser.AlterByDefaultCharsetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDiscardTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDiscardTablespace(OpenDistroSqlParser.AlterByDiscardTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDiscardTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDiscardTablespace(OpenDistroSqlParser.AlterByDiscardTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByImportTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByImportTablespace(OpenDistroSqlParser.AlterByImportTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByImportTablespace}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByImportTablespace(OpenDistroSqlParser.AlterByImportTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByForce}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByForce(OpenDistroSqlParser.AlterByForceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByForce}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByForce(OpenDistroSqlParser.AlterByForceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByValidate}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByValidate(OpenDistroSqlParser.AlterByValidateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByValidate}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByValidate(OpenDistroSqlParser.AlterByValidateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAddPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAddPartition(OpenDistroSqlParser.AlterByAddPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAddPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAddPartition(OpenDistroSqlParser.AlterByAddPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDropPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDropPartition(OpenDistroSqlParser.AlterByDropPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDropPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDropPartition(OpenDistroSqlParser.AlterByDropPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByDiscardPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByDiscardPartition(OpenDistroSqlParser.AlterByDiscardPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByDiscardPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByDiscardPartition(OpenDistroSqlParser.AlterByDiscardPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByImportPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByImportPartition(OpenDistroSqlParser.AlterByImportPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByImportPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByImportPartition(OpenDistroSqlParser.AlterByImportPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByTruncatePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByTruncatePartition(OpenDistroSqlParser.AlterByTruncatePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByTruncatePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByTruncatePartition(OpenDistroSqlParser.AlterByTruncatePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByCoalescePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByCoalescePartition(OpenDistroSqlParser.AlterByCoalescePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByCoalescePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByCoalescePartition(OpenDistroSqlParser.AlterByCoalescePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByReorganizePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByReorganizePartition(OpenDistroSqlParser.AlterByReorganizePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByReorganizePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByReorganizePartition(OpenDistroSqlParser.AlterByReorganizePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByExchangePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByExchangePartition(OpenDistroSqlParser.AlterByExchangePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByExchangePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByExchangePartition(OpenDistroSqlParser.AlterByExchangePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByAnalyzePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByAnalyzePartition(OpenDistroSqlParser.AlterByAnalyzePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByAnalyzePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByAnalyzePartition(OpenDistroSqlParser.AlterByAnalyzePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByCheckPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByCheckPartition(OpenDistroSqlParser.AlterByCheckPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByCheckPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByCheckPartition(OpenDistroSqlParser.AlterByCheckPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByOptimizePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByOptimizePartition(OpenDistroSqlParser.AlterByOptimizePartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByOptimizePartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByOptimizePartition(OpenDistroSqlParser.AlterByOptimizePartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRebuildPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRebuildPartition(OpenDistroSqlParser.AlterByRebuildPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRebuildPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRebuildPartition(OpenDistroSqlParser.AlterByRebuildPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRepairPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRepairPartition(OpenDistroSqlParser.AlterByRepairPartitionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRepairPartition}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRepairPartition(OpenDistroSqlParser.AlterByRepairPartitionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByRemovePartitioning}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByRemovePartitioning(OpenDistroSqlParser.AlterByRemovePartitioningContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByRemovePartitioning}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByRemovePartitioning(OpenDistroSqlParser.AlterByRemovePartitioningContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterByUpgradePartitioning}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void enterAlterByUpgradePartitioning(OpenDistroSqlParser.AlterByUpgradePartitioningContext ctx);
	/**
	 * Exit a parse tree produced by the {@code alterByUpgradePartitioning}
	 * labeled alternative in {@link OpenDistroSqlParser#alterSpecification}.
	 * @param ctx the parse tree
	 */
	void exitAlterByUpgradePartitioning(OpenDistroSqlParser.AlterByUpgradePartitioningContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropDatabase}.
	 * @param ctx the parse tree
	 */
	void enterDropDatabase(OpenDistroSqlParser.DropDatabaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropDatabase}.
	 * @param ctx the parse tree
	 */
	void exitDropDatabase(OpenDistroSqlParser.DropDatabaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropEvent}.
	 * @param ctx the parse tree
	 */
	void enterDropEvent(OpenDistroSqlParser.DropEventContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropEvent}.
	 * @param ctx the parse tree
	 */
	void exitDropEvent(OpenDistroSqlParser.DropEventContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void enterDropIndex(OpenDistroSqlParser.DropIndexContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropIndex}.
	 * @param ctx the parse tree
	 */
	void exitDropIndex(OpenDistroSqlParser.DropIndexContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void enterDropLogfileGroup(OpenDistroSqlParser.DropLogfileGroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropLogfileGroup}.
	 * @param ctx the parse tree
	 */
	void exitDropLogfileGroup(OpenDistroSqlParser.DropLogfileGroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropProcedure}.
	 * @param ctx the parse tree
	 */
	void enterDropProcedure(OpenDistroSqlParser.DropProcedureContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropProcedure}.
	 * @param ctx the parse tree
	 */
	void exitDropProcedure(OpenDistroSqlParser.DropProcedureContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropFunction}.
	 * @param ctx the parse tree
	 */
	void enterDropFunction(OpenDistroSqlParser.DropFunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropFunction}.
	 * @param ctx the parse tree
	 */
	void exitDropFunction(OpenDistroSqlParser.DropFunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropServer}.
	 * @param ctx the parse tree
	 */
	void enterDropServer(OpenDistroSqlParser.DropServerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropServer}.
	 * @param ctx the parse tree
	 */
	void exitDropServer(OpenDistroSqlParser.DropServerContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropTable}.
	 * @param ctx the parse tree
	 */
	void enterDropTable(OpenDistroSqlParser.DropTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropTable}.
	 * @param ctx the parse tree
	 */
	void exitDropTable(OpenDistroSqlParser.DropTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropTablespace}.
	 * @param ctx the parse tree
	 */
	void enterDropTablespace(OpenDistroSqlParser.DropTablespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropTablespace}.
	 * @param ctx the parse tree
	 */
	void exitDropTablespace(OpenDistroSqlParser.DropTablespaceContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropTrigger}.
	 * @param ctx the parse tree
	 */
	void enterDropTrigger(OpenDistroSqlParser.DropTriggerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropTrigger}.
	 * @param ctx the parse tree
	 */
	void exitDropTrigger(OpenDistroSqlParser.DropTriggerContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropView}.
	 * @param ctx the parse tree
	 */
	void enterDropView(OpenDistroSqlParser.DropViewContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropView}.
	 * @param ctx the parse tree
	 */
	void exitDropView(OpenDistroSqlParser.DropViewContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#renameTable}.
	 * @param ctx the parse tree
	 */
	void enterRenameTable(OpenDistroSqlParser.RenameTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#renameTable}.
	 * @param ctx the parse tree
	 */
	void exitRenameTable(OpenDistroSqlParser.RenameTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#renameTableClause}.
	 * @param ctx the parse tree
	 */
	void enterRenameTableClause(OpenDistroSqlParser.RenameTableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#renameTableClause}.
	 * @param ctx the parse tree
	 */
	void exitRenameTableClause(OpenDistroSqlParser.RenameTableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#truncateTable}.
	 * @param ctx the parse tree
	 */
	void enterTruncateTable(OpenDistroSqlParser.TruncateTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#truncateTable}.
	 * @param ctx the parse tree
	 */
	void exitTruncateTable(OpenDistroSqlParser.TruncateTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#callStatement}.
	 * @param ctx the parse tree
	 */
	void enterCallStatement(OpenDistroSqlParser.CallStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#callStatement}.
	 * @param ctx the parse tree
	 */
	void exitCallStatement(OpenDistroSqlParser.CallStatementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void enterDoStatement(OpenDistroSqlParser.DoStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#doStatement}.
	 * @param ctx the parse tree
	 */
	void exitDoStatement(OpenDistroSqlParser.DoStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerStatement(OpenDistroSqlParser.HandlerStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#handlerStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerStatement(OpenDistroSqlParser.HandlerStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatement(OpenDistroSqlParser.InsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatement(OpenDistroSqlParser.InsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadDataStatement(OpenDistroSqlParser.LoadDataStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#loadDataStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadDataStatement(OpenDistroSqlParser.LoadDataStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoadXmlStatement(OpenDistroSqlParser.LoadXmlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#loadXmlStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoadXmlStatement(OpenDistroSqlParser.LoadXmlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#replaceStatement}.
	 * @param ctx the parse tree
	 */
	void enterReplaceStatement(OpenDistroSqlParser.ReplaceStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#replaceStatement}.
	 * @param ctx the parse tree
	 */
	void exitReplaceStatement(OpenDistroSqlParser.ReplaceStatementContext ctx);
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
	 * Enter a parse tree produced by the {@code unionParenthesisSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterUnionParenthesisSelect(OpenDistroSqlParser.UnionParenthesisSelectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unionParenthesisSelect}
	 * labeled alternative in {@link OpenDistroSqlParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitUnionParenthesisSelect(OpenDistroSqlParser.UnionParenthesisSelectContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdateStatement(OpenDistroSqlParser.UpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdateStatement(OpenDistroSqlParser.UpdateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#insertStatementValue}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatementValue(OpenDistroSqlParser.InsertStatementValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#insertStatementValue}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatementValue(OpenDistroSqlParser.InsertStatementValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#updatedElement}.
	 * @param ctx the parse tree
	 */
	void enterUpdatedElement(OpenDistroSqlParser.UpdatedElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#updatedElement}.
	 * @param ctx the parse tree
	 */
	void exitUpdatedElement(OpenDistroSqlParser.UpdatedElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#assignmentField}.
	 * @param ctx the parse tree
	 */
	void enterAssignmentField(OpenDistroSqlParser.AssignmentFieldContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#assignmentField}.
	 * @param ctx the parse tree
	 */
	void exitAssignmentField(OpenDistroSqlParser.AssignmentFieldContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void enterLockClause(OpenDistroSqlParser.LockClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lockClause}.
	 * @param ctx the parse tree
	 */
	void exitLockClause(OpenDistroSqlParser.LockClauseContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#multipleDeleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterMultipleDeleteStatement(OpenDistroSqlParser.MultipleDeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#multipleDeleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitMultipleDeleteStatement(OpenDistroSqlParser.MultipleDeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerOpenStatement(OpenDistroSqlParser.HandlerOpenStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#handlerOpenStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerOpenStatement(OpenDistroSqlParser.HandlerOpenStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadIndexStatement(OpenDistroSqlParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#handlerReadIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadIndexStatement(OpenDistroSqlParser.HandlerReadIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerReadStatement(OpenDistroSqlParser.HandlerReadStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#handlerReadStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerReadStatement(OpenDistroSqlParser.HandlerReadStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void enterHandlerCloseStatement(OpenDistroSqlParser.HandlerCloseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#handlerCloseStatement}.
	 * @param ctx the parse tree
	 */
	void exitHandlerCloseStatement(OpenDistroSqlParser.HandlerCloseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#singleUpdateStatement}.
	 * @param ctx the parse tree
	 */
	void enterSingleUpdateStatement(OpenDistroSqlParser.SingleUpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#singleUpdateStatement}.
	 * @param ctx the parse tree
	 */
	void exitSingleUpdateStatement(OpenDistroSqlParser.SingleUpdateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#multipleUpdateStatement}.
	 * @param ctx the parse tree
	 */
	void enterMultipleUpdateStatement(OpenDistroSqlParser.MultipleUpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#multipleUpdateStatement}.
	 * @param ctx the parse tree
	 */
	void exitMultipleUpdateStatement(OpenDistroSqlParser.MultipleUpdateStatementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexHint}.
	 * @param ctx the parse tree
	 */
	void enterIndexHint(OpenDistroSqlParser.IndexHintContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexHint}.
	 * @param ctx the parse tree
	 */
	void exitIndexHint(OpenDistroSqlParser.IndexHintContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexHintType}.
	 * @param ctx the parse tree
	 */
	void enterIndexHintType(OpenDistroSqlParser.IndexHintTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexHintType}.
	 * @param ctx the parse tree
	 */
	void exitIndexHintType(OpenDistroSqlParser.IndexHintTypeContext ctx);
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
	 * Enter a parse tree produced by the {@code straightJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void enterStraightJoin(OpenDistroSqlParser.StraightJoinContext ctx);
	/**
	 * Exit a parse tree produced by the {@code straightJoin}
	 * labeled alternative in {@link OpenDistroSqlParser#joinPart}.
	 * @param ctx the parse tree
	 */
	void exitStraightJoin(OpenDistroSqlParser.StraightJoinContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#queryExpressionNointo}.
	 * @param ctx the parse tree
	 */
	void enterQueryExpressionNointo(OpenDistroSqlParser.QueryExpressionNointoContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#queryExpressionNointo}.
	 * @param ctx the parse tree
	 */
	void exitQueryExpressionNointo(OpenDistroSqlParser.QueryExpressionNointoContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#querySpecificationNointo}.
	 * @param ctx the parse tree
	 */
	void enterQuerySpecificationNointo(OpenDistroSqlParser.QuerySpecificationNointoContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#querySpecificationNointo}.
	 * @param ctx the parse tree
	 */
	void exitQuerySpecificationNointo(OpenDistroSqlParser.QuerySpecificationNointoContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#unionParenthesis}.
	 * @param ctx the parse tree
	 */
	void enterUnionParenthesis(OpenDistroSqlParser.UnionParenthesisContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#unionParenthesis}.
	 * @param ctx the parse tree
	 */
	void exitUnionParenthesis(OpenDistroSqlParser.UnionParenthesisContext ctx);
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
	 * Enter a parse tree produced by the {@code selectIntoVariables}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectIntoVariables(OpenDistroSqlParser.SelectIntoVariablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectIntoVariables}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectIntoVariables(OpenDistroSqlParser.SelectIntoVariablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectIntoDumpFile}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectIntoDumpFile(OpenDistroSqlParser.SelectIntoDumpFileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectIntoDumpFile}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectIntoDumpFile(OpenDistroSqlParser.SelectIntoDumpFileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code selectIntoTextFile}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void enterSelectIntoTextFile(OpenDistroSqlParser.SelectIntoTextFileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code selectIntoTextFile}
	 * labeled alternative in {@link OpenDistroSqlParser#selectIntoExpression}.
	 * @param ctx the parse tree
	 */
	void exitSelectIntoTextFile(OpenDistroSqlParser.SelectIntoTextFileContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#selectFieldsInto}.
	 * @param ctx the parse tree
	 */
	void enterSelectFieldsInto(OpenDistroSqlParser.SelectFieldsIntoContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#selectFieldsInto}.
	 * @param ctx the parse tree
	 */
	void exitSelectFieldsInto(OpenDistroSqlParser.SelectFieldsIntoContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#selectLinesInto}.
	 * @param ctx the parse tree
	 */
	void enterSelectLinesInto(OpenDistroSqlParser.SelectLinesIntoContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#selectLinesInto}.
	 * @param ctx the parse tree
	 */
	void exitSelectLinesInto(OpenDistroSqlParser.SelectLinesIntoContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#startTransaction}.
	 * @param ctx the parse tree
	 */
	void enterStartTransaction(OpenDistroSqlParser.StartTransactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#startTransaction}.
	 * @param ctx the parse tree
	 */
	void exitStartTransaction(OpenDistroSqlParser.StartTransactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#beginWork}.
	 * @param ctx the parse tree
	 */
	void enterBeginWork(OpenDistroSqlParser.BeginWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#beginWork}.
	 * @param ctx the parse tree
	 */
	void exitBeginWork(OpenDistroSqlParser.BeginWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#commitWork}.
	 * @param ctx the parse tree
	 */
	void enterCommitWork(OpenDistroSqlParser.CommitWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#commitWork}.
	 * @param ctx the parse tree
	 */
	void exitCommitWork(OpenDistroSqlParser.CommitWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#rollbackWork}.
	 * @param ctx the parse tree
	 */
	void enterRollbackWork(OpenDistroSqlParser.RollbackWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#rollbackWork}.
	 * @param ctx the parse tree
	 */
	void exitRollbackWork(OpenDistroSqlParser.RollbackWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#savepointStatement}.
	 * @param ctx the parse tree
	 */
	void enterSavepointStatement(OpenDistroSqlParser.SavepointStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#savepointStatement}.
	 * @param ctx the parse tree
	 */
	void exitSavepointStatement(OpenDistroSqlParser.SavepointStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#rollbackStatement}.
	 * @param ctx the parse tree
	 */
	void enterRollbackStatement(OpenDistroSqlParser.RollbackStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#rollbackStatement}.
	 * @param ctx the parse tree
	 */
	void exitRollbackStatement(OpenDistroSqlParser.RollbackStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#releaseStatement}.
	 * @param ctx the parse tree
	 */
	void enterReleaseStatement(OpenDistroSqlParser.ReleaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#releaseStatement}.
	 * @param ctx the parse tree
	 */
	void exitReleaseStatement(OpenDistroSqlParser.ReleaseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lockTables}.
	 * @param ctx the parse tree
	 */
	void enterLockTables(OpenDistroSqlParser.LockTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lockTables}.
	 * @param ctx the parse tree
	 */
	void exitLockTables(OpenDistroSqlParser.LockTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#unlockTables}.
	 * @param ctx the parse tree
	 */
	void enterUnlockTables(OpenDistroSqlParser.UnlockTablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#unlockTables}.
	 * @param ctx the parse tree
	 */
	void exitUnlockTables(OpenDistroSqlParser.UnlockTablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#setAutocommitStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetAutocommitStatement(OpenDistroSqlParser.SetAutocommitStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#setAutocommitStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetAutocommitStatement(OpenDistroSqlParser.SetAutocommitStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#setTransactionStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetTransactionStatement(OpenDistroSqlParser.SetTransactionStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#setTransactionStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetTransactionStatement(OpenDistroSqlParser.SetTransactionStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#transactionMode}.
	 * @param ctx the parse tree
	 */
	void enterTransactionMode(OpenDistroSqlParser.TransactionModeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#transactionMode}.
	 * @param ctx the parse tree
	 */
	void exitTransactionMode(OpenDistroSqlParser.TransactionModeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lockTableElement}.
	 * @param ctx the parse tree
	 */
	void enterLockTableElement(OpenDistroSqlParser.LockTableElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lockTableElement}.
	 * @param ctx the parse tree
	 */
	void exitLockTableElement(OpenDistroSqlParser.LockTableElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lockAction}.
	 * @param ctx the parse tree
	 */
	void enterLockAction(OpenDistroSqlParser.LockActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lockAction}.
	 * @param ctx the parse tree
	 */
	void exitLockAction(OpenDistroSqlParser.LockActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#transactionOption}.
	 * @param ctx the parse tree
	 */
	void enterTransactionOption(OpenDistroSqlParser.TransactionOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#transactionOption}.
	 * @param ctx the parse tree
	 */
	void exitTransactionOption(OpenDistroSqlParser.TransactionOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#transactionLevel}.
	 * @param ctx the parse tree
	 */
	void enterTransactionLevel(OpenDistroSqlParser.TransactionLevelContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#transactionLevel}.
	 * @param ctx the parse tree
	 */
	void exitTransactionLevel(OpenDistroSqlParser.TransactionLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#changeMaster}.
	 * @param ctx the parse tree
	 */
	void enterChangeMaster(OpenDistroSqlParser.ChangeMasterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#changeMaster}.
	 * @param ctx the parse tree
	 */
	void exitChangeMaster(OpenDistroSqlParser.ChangeMasterContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#changeReplicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterChangeReplicationFilter(OpenDistroSqlParser.ChangeReplicationFilterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#changeReplicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitChangeReplicationFilter(OpenDistroSqlParser.ChangeReplicationFilterContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#purgeBinaryLogs}.
	 * @param ctx the parse tree
	 */
	void enterPurgeBinaryLogs(OpenDistroSqlParser.PurgeBinaryLogsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#purgeBinaryLogs}.
	 * @param ctx the parse tree
	 */
	void exitPurgeBinaryLogs(OpenDistroSqlParser.PurgeBinaryLogsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#resetMaster}.
	 * @param ctx the parse tree
	 */
	void enterResetMaster(OpenDistroSqlParser.ResetMasterContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#resetMaster}.
	 * @param ctx the parse tree
	 */
	void exitResetMaster(OpenDistroSqlParser.ResetMasterContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#resetSlave}.
	 * @param ctx the parse tree
	 */
	void enterResetSlave(OpenDistroSqlParser.ResetSlaveContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#resetSlave}.
	 * @param ctx the parse tree
	 */
	void exitResetSlave(OpenDistroSqlParser.ResetSlaveContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#startSlave}.
	 * @param ctx the parse tree
	 */
	void enterStartSlave(OpenDistroSqlParser.StartSlaveContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#startSlave}.
	 * @param ctx the parse tree
	 */
	void exitStartSlave(OpenDistroSqlParser.StartSlaveContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#stopSlave}.
	 * @param ctx the parse tree
	 */
	void enterStopSlave(OpenDistroSqlParser.StopSlaveContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#stopSlave}.
	 * @param ctx the parse tree
	 */
	void exitStopSlave(OpenDistroSqlParser.StopSlaveContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#startGroupReplication}.
	 * @param ctx the parse tree
	 */
	void enterStartGroupReplication(OpenDistroSqlParser.StartGroupReplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#startGroupReplication}.
	 * @param ctx the parse tree
	 */
	void exitStartGroupReplication(OpenDistroSqlParser.StartGroupReplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#stopGroupReplication}.
	 * @param ctx the parse tree
	 */
	void enterStopGroupReplication(OpenDistroSqlParser.StopGroupReplicationContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#stopGroupReplication}.
	 * @param ctx the parse tree
	 */
	void exitStopGroupReplication(OpenDistroSqlParser.StopGroupReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterStringOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterStringOption(OpenDistroSqlParser.MasterStringOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterStringOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterStringOption(OpenDistroSqlParser.MasterStringOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterDecimalOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterDecimalOption(OpenDistroSqlParser.MasterDecimalOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterDecimalOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterDecimalOption(OpenDistroSqlParser.MasterDecimalOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterBoolOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterBoolOption(OpenDistroSqlParser.MasterBoolOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterBoolOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterBoolOption(OpenDistroSqlParser.MasterBoolOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterRealOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterRealOption(OpenDistroSqlParser.MasterRealOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterRealOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterRealOption(OpenDistroSqlParser.MasterRealOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterUidListOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterUidListOption(OpenDistroSqlParser.MasterUidListOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterUidListOption}
	 * labeled alternative in {@link OpenDistroSqlParser#masterOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterUidListOption(OpenDistroSqlParser.MasterUidListOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#stringMasterOption}.
	 * @param ctx the parse tree
	 */
	void enterStringMasterOption(OpenDistroSqlParser.StringMasterOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#stringMasterOption}.
	 * @param ctx the parse tree
	 */
	void exitStringMasterOption(OpenDistroSqlParser.StringMasterOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#decimalMasterOption}.
	 * @param ctx the parse tree
	 */
	void enterDecimalMasterOption(OpenDistroSqlParser.DecimalMasterOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#decimalMasterOption}.
	 * @param ctx the parse tree
	 */
	void exitDecimalMasterOption(OpenDistroSqlParser.DecimalMasterOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#boolMasterOption}.
	 * @param ctx the parse tree
	 */
	void enterBoolMasterOption(OpenDistroSqlParser.BoolMasterOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#boolMasterOption}.
	 * @param ctx the parse tree
	 */
	void exitBoolMasterOption(OpenDistroSqlParser.BoolMasterOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#channelOption}.
	 * @param ctx the parse tree
	 */
	void enterChannelOption(OpenDistroSqlParser.ChannelOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#channelOption}.
	 * @param ctx the parse tree
	 */
	void exitChannelOption(OpenDistroSqlParser.ChannelOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterDoDbReplication(OpenDistroSqlParser.DoDbReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitDoDbReplication(OpenDistroSqlParser.DoDbReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ignoreDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterIgnoreDbReplication(OpenDistroSqlParser.IgnoreDbReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ignoreDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitIgnoreDbReplication(OpenDistroSqlParser.IgnoreDbReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code doTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterDoTableReplication(OpenDistroSqlParser.DoTableReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code doTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitDoTableReplication(OpenDistroSqlParser.DoTableReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ignoreTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterIgnoreTableReplication(OpenDistroSqlParser.IgnoreTableReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ignoreTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitIgnoreTableReplication(OpenDistroSqlParser.IgnoreTableReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wildDoTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterWildDoTableReplication(OpenDistroSqlParser.WildDoTableReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wildDoTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitWildDoTableReplication(OpenDistroSqlParser.WildDoTableReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code wildIgnoreTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterWildIgnoreTableReplication(OpenDistroSqlParser.WildIgnoreTableReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code wildIgnoreTableReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitWildIgnoreTableReplication(OpenDistroSqlParser.WildIgnoreTableReplicationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code rewriteDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void enterRewriteDbReplication(OpenDistroSqlParser.RewriteDbReplicationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code rewriteDbReplication}
	 * labeled alternative in {@link OpenDistroSqlParser#replicationFilter}.
	 * @param ctx the parse tree
	 */
	void exitRewriteDbReplication(OpenDistroSqlParser.RewriteDbReplicationContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tablePair}.
	 * @param ctx the parse tree
	 */
	void enterTablePair(OpenDistroSqlParser.TablePairContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tablePair}.
	 * @param ctx the parse tree
	 */
	void exitTablePair(OpenDistroSqlParser.TablePairContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#threadType}.
	 * @param ctx the parse tree
	 */
	void enterThreadType(OpenDistroSqlParser.ThreadTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#threadType}.
	 * @param ctx the parse tree
	 */
	void exitThreadType(OpenDistroSqlParser.ThreadTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code gtidsUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void enterGtidsUntilOption(OpenDistroSqlParser.GtidsUntilOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code gtidsUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void exitGtidsUntilOption(OpenDistroSqlParser.GtidsUntilOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code masterLogUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void enterMasterLogUntilOption(OpenDistroSqlParser.MasterLogUntilOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code masterLogUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void exitMasterLogUntilOption(OpenDistroSqlParser.MasterLogUntilOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code relayLogUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void enterRelayLogUntilOption(OpenDistroSqlParser.RelayLogUntilOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code relayLogUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void exitRelayLogUntilOption(OpenDistroSqlParser.RelayLogUntilOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code sqlGapsUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void enterSqlGapsUntilOption(OpenDistroSqlParser.SqlGapsUntilOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code sqlGapsUntilOption}
	 * labeled alternative in {@link OpenDistroSqlParser#untilOption}.
	 * @param ctx the parse tree
	 */
	void exitSqlGapsUntilOption(OpenDistroSqlParser.SqlGapsUntilOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code userConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void enterUserConnectionOption(OpenDistroSqlParser.UserConnectionOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code userConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void exitUserConnectionOption(OpenDistroSqlParser.UserConnectionOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code passwordConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void enterPasswordConnectionOption(OpenDistroSqlParser.PasswordConnectionOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code passwordConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void exitPasswordConnectionOption(OpenDistroSqlParser.PasswordConnectionOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code defaultAuthConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void enterDefaultAuthConnectionOption(OpenDistroSqlParser.DefaultAuthConnectionOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code defaultAuthConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void exitDefaultAuthConnectionOption(OpenDistroSqlParser.DefaultAuthConnectionOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code pluginDirConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void enterPluginDirConnectionOption(OpenDistroSqlParser.PluginDirConnectionOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code pluginDirConnectionOption}
	 * labeled alternative in {@link OpenDistroSqlParser#connectionOption}.
	 * @param ctx the parse tree
	 */
	void exitPluginDirConnectionOption(OpenDistroSqlParser.PluginDirConnectionOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#gtuidSet}.
	 * @param ctx the parse tree
	 */
	void enterGtuidSet(OpenDistroSqlParser.GtuidSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#gtuidSet}.
	 * @param ctx the parse tree
	 */
	void exitGtuidSet(OpenDistroSqlParser.GtuidSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaStartTransaction}.
	 * @param ctx the parse tree
	 */
	void enterXaStartTransaction(OpenDistroSqlParser.XaStartTransactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaStartTransaction}.
	 * @param ctx the parse tree
	 */
	void exitXaStartTransaction(OpenDistroSqlParser.XaStartTransactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaEndTransaction}.
	 * @param ctx the parse tree
	 */
	void enterXaEndTransaction(OpenDistroSqlParser.XaEndTransactionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaEndTransaction}.
	 * @param ctx the parse tree
	 */
	void exitXaEndTransaction(OpenDistroSqlParser.XaEndTransactionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaPrepareStatement}.
	 * @param ctx the parse tree
	 */
	void enterXaPrepareStatement(OpenDistroSqlParser.XaPrepareStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaPrepareStatement}.
	 * @param ctx the parse tree
	 */
	void exitXaPrepareStatement(OpenDistroSqlParser.XaPrepareStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaCommitWork}.
	 * @param ctx the parse tree
	 */
	void enterXaCommitWork(OpenDistroSqlParser.XaCommitWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaCommitWork}.
	 * @param ctx the parse tree
	 */
	void exitXaCommitWork(OpenDistroSqlParser.XaCommitWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaRollbackWork}.
	 * @param ctx the parse tree
	 */
	void enterXaRollbackWork(OpenDistroSqlParser.XaRollbackWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaRollbackWork}.
	 * @param ctx the parse tree
	 */
	void exitXaRollbackWork(OpenDistroSqlParser.XaRollbackWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xaRecoverWork}.
	 * @param ctx the parse tree
	 */
	void enterXaRecoverWork(OpenDistroSqlParser.XaRecoverWorkContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xaRecoverWork}.
	 * @param ctx the parse tree
	 */
	void exitXaRecoverWork(OpenDistroSqlParser.XaRecoverWorkContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#prepareStatement}.
	 * @param ctx the parse tree
	 */
	void enterPrepareStatement(OpenDistroSqlParser.PrepareStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#prepareStatement}.
	 * @param ctx the parse tree
	 */
	void exitPrepareStatement(OpenDistroSqlParser.PrepareStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#executeStatement}.
	 * @param ctx the parse tree
	 */
	void enterExecuteStatement(OpenDistroSqlParser.ExecuteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#executeStatement}.
	 * @param ctx the parse tree
	 */
	void exitExecuteStatement(OpenDistroSqlParser.ExecuteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#deallocatePrepare}.
	 * @param ctx the parse tree
	 */
	void enterDeallocatePrepare(OpenDistroSqlParser.DeallocatePrepareContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#deallocatePrepare}.
	 * @param ctx the parse tree
	 */
	void exitDeallocatePrepare(OpenDistroSqlParser.DeallocatePrepareContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#routineBody}.
	 * @param ctx the parse tree
	 */
	void enterRoutineBody(OpenDistroSqlParser.RoutineBodyContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#routineBody}.
	 * @param ctx the parse tree
	 */
	void exitRoutineBody(OpenDistroSqlParser.RoutineBodyContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(OpenDistroSqlParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(OpenDistroSqlParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void enterCaseStatement(OpenDistroSqlParser.CaseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#caseStatement}.
	 * @param ctx the parse tree
	 */
	void exitCaseStatement(OpenDistroSqlParser.CaseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(OpenDistroSqlParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(OpenDistroSqlParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#iterateStatement}.
	 * @param ctx the parse tree
	 */
	void enterIterateStatement(OpenDistroSqlParser.IterateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#iterateStatement}.
	 * @param ctx the parse tree
	 */
	void exitIterateStatement(OpenDistroSqlParser.IterateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#leaveStatement}.
	 * @param ctx the parse tree
	 */
	void enterLeaveStatement(OpenDistroSqlParser.LeaveStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#leaveStatement}.
	 * @param ctx the parse tree
	 */
	void exitLeaveStatement(OpenDistroSqlParser.LeaveStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoopStatement(OpenDistroSqlParser.LoopStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoopStatement(OpenDistroSqlParser.LoopStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#repeatStatement}.
	 * @param ctx the parse tree
	 */
	void enterRepeatStatement(OpenDistroSqlParser.RepeatStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#repeatStatement}.
	 * @param ctx the parse tree
	 */
	void exitRepeatStatement(OpenDistroSqlParser.RepeatStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStatement(OpenDistroSqlParser.ReturnStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#returnStatement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStatement(OpenDistroSqlParser.ReturnStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStatement(OpenDistroSqlParser.WhileStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#whileStatement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStatement(OpenDistroSqlParser.WhileStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code CloseCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void enterCloseCursor(OpenDistroSqlParser.CloseCursorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code CloseCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void exitCloseCursor(OpenDistroSqlParser.CloseCursorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code FetchCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void enterFetchCursor(OpenDistroSqlParser.FetchCursorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code FetchCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void exitFetchCursor(OpenDistroSqlParser.FetchCursorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code OpenCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void enterOpenCursor(OpenDistroSqlParser.OpenCursorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code OpenCursor}
	 * labeled alternative in {@link OpenDistroSqlParser#cursorStatement}.
	 * @param ctx the parse tree
	 */
	void exitOpenCursor(OpenDistroSqlParser.OpenCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#declareVariable}.
	 * @param ctx the parse tree
	 */
	void enterDeclareVariable(OpenDistroSqlParser.DeclareVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#declareVariable}.
	 * @param ctx the parse tree
	 */
	void exitDeclareVariable(OpenDistroSqlParser.DeclareVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#declareCondition}.
	 * @param ctx the parse tree
	 */
	void enterDeclareCondition(OpenDistroSqlParser.DeclareConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#declareCondition}.
	 * @param ctx the parse tree
	 */
	void exitDeclareCondition(OpenDistroSqlParser.DeclareConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#declareCursor}.
	 * @param ctx the parse tree
	 */
	void enterDeclareCursor(OpenDistroSqlParser.DeclareCursorContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#declareCursor}.
	 * @param ctx the parse tree
	 */
	void exitDeclareCursor(OpenDistroSqlParser.DeclareCursorContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#declareHandler}.
	 * @param ctx the parse tree
	 */
	void enterDeclareHandler(OpenDistroSqlParser.DeclareHandlerContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#declareHandler}.
	 * @param ctx the parse tree
	 */
	void exitDeclareHandler(OpenDistroSqlParser.DeclareHandlerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionCode}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionCode(OpenDistroSqlParser.HandlerConditionCodeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionCode}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionCode(OpenDistroSqlParser.HandlerConditionCodeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionState}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionState(OpenDistroSqlParser.HandlerConditionStateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionState}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionState(OpenDistroSqlParser.HandlerConditionStateContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionName}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionName(OpenDistroSqlParser.HandlerConditionNameContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionName}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionName(OpenDistroSqlParser.HandlerConditionNameContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionWarning}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionWarning(OpenDistroSqlParser.HandlerConditionWarningContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionWarning}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionWarning(OpenDistroSqlParser.HandlerConditionWarningContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionNotfound}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionNotfound(OpenDistroSqlParser.HandlerConditionNotfoundContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionNotfound}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionNotfound(OpenDistroSqlParser.HandlerConditionNotfoundContext ctx);
	/**
	 * Enter a parse tree produced by the {@code handlerConditionException}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void enterHandlerConditionException(OpenDistroSqlParser.HandlerConditionExceptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code handlerConditionException}
	 * labeled alternative in {@link OpenDistroSqlParser#handlerConditionValue}.
	 * @param ctx the parse tree
	 */
	void exitHandlerConditionException(OpenDistroSqlParser.HandlerConditionExceptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#procedureSqlStatement}.
	 * @param ctx the parse tree
	 */
	void enterProcedureSqlStatement(OpenDistroSqlParser.ProcedureSqlStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#procedureSqlStatement}.
	 * @param ctx the parse tree
	 */
	void exitProcedureSqlStatement(OpenDistroSqlParser.ProcedureSqlStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#caseAlternative}.
	 * @param ctx the parse tree
	 */
	void enterCaseAlternative(OpenDistroSqlParser.CaseAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#caseAlternative}.
	 * @param ctx the parse tree
	 */
	void exitCaseAlternative(OpenDistroSqlParser.CaseAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#elifAlternative}.
	 * @param ctx the parse tree
	 */
	void enterElifAlternative(OpenDistroSqlParser.ElifAlternativeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#elifAlternative}.
	 * @param ctx the parse tree
	 */
	void exitElifAlternative(OpenDistroSqlParser.ElifAlternativeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code alterUserMysqlV56}
	 * labeled alternative in {@link OpenDistroSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void enterAlterUserMysqlV56(OpenDistroSqlParser.AlterUserMysqlV56Context ctx);
	/**
	 * Exit a parse tree produced by the {@code alterUserMysqlV56}
	 * labeled alternative in {@link OpenDistroSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void exitAlterUserMysqlV56(OpenDistroSqlParser.AlterUserMysqlV56Context ctx);
	/**
	 * Enter a parse tree produced by the {@code alterUserMysqlV57}
	 * labeled alternative in {@link OpenDistroSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void enterAlterUserMysqlV57(OpenDistroSqlParser.AlterUserMysqlV57Context ctx);
	/**
	 * Exit a parse tree produced by the {@code alterUserMysqlV57}
	 * labeled alternative in {@link OpenDistroSqlParser#alterUser}.
	 * @param ctx the parse tree
	 */
	void exitAlterUserMysqlV57(OpenDistroSqlParser.AlterUserMysqlV57Context ctx);
	/**
	 * Enter a parse tree produced by the {@code createUserMysqlV56}
	 * labeled alternative in {@link OpenDistroSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void enterCreateUserMysqlV56(OpenDistroSqlParser.CreateUserMysqlV56Context ctx);
	/**
	 * Exit a parse tree produced by the {@code createUserMysqlV56}
	 * labeled alternative in {@link OpenDistroSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void exitCreateUserMysqlV56(OpenDistroSqlParser.CreateUserMysqlV56Context ctx);
	/**
	 * Enter a parse tree produced by the {@code createUserMysqlV57}
	 * labeled alternative in {@link OpenDistroSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void enterCreateUserMysqlV57(OpenDistroSqlParser.CreateUserMysqlV57Context ctx);
	/**
	 * Exit a parse tree produced by the {@code createUserMysqlV57}
	 * labeled alternative in {@link OpenDistroSqlParser#createUser}.
	 * @param ctx the parse tree
	 */
	void exitCreateUserMysqlV57(OpenDistroSqlParser.CreateUserMysqlV57Context ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#dropUser}.
	 * @param ctx the parse tree
	 */
	void enterDropUser(OpenDistroSqlParser.DropUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#dropUser}.
	 * @param ctx the parse tree
	 */
	void exitDropUser(OpenDistroSqlParser.DropUserContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#grantStatement}.
	 * @param ctx the parse tree
	 */
	void enterGrantStatement(OpenDistroSqlParser.GrantStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#grantStatement}.
	 * @param ctx the parse tree
	 */
	void exitGrantStatement(OpenDistroSqlParser.GrantStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#grantProxy}.
	 * @param ctx the parse tree
	 */
	void enterGrantProxy(OpenDistroSqlParser.GrantProxyContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#grantProxy}.
	 * @param ctx the parse tree
	 */
	void exitGrantProxy(OpenDistroSqlParser.GrantProxyContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#renameUser}.
	 * @param ctx the parse tree
	 */
	void enterRenameUser(OpenDistroSqlParser.RenameUserContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#renameUser}.
	 * @param ctx the parse tree
	 */
	void exitRenameUser(OpenDistroSqlParser.RenameUserContext ctx);
	/**
	 * Enter a parse tree produced by the {@code detailRevoke}
	 * labeled alternative in {@link OpenDistroSqlParser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void enterDetailRevoke(OpenDistroSqlParser.DetailRevokeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code detailRevoke}
	 * labeled alternative in {@link OpenDistroSqlParser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void exitDetailRevoke(OpenDistroSqlParser.DetailRevokeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code shortRevoke}
	 * labeled alternative in {@link OpenDistroSqlParser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void enterShortRevoke(OpenDistroSqlParser.ShortRevokeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code shortRevoke}
	 * labeled alternative in {@link OpenDistroSqlParser#revokeStatement}.
	 * @param ctx the parse tree
	 */
	void exitShortRevoke(OpenDistroSqlParser.ShortRevokeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#revokeProxy}.
	 * @param ctx the parse tree
	 */
	void enterRevokeProxy(OpenDistroSqlParser.RevokeProxyContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#revokeProxy}.
	 * @param ctx the parse tree
	 */
	void exitRevokeProxy(OpenDistroSqlParser.RevokeProxyContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#setPasswordStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetPasswordStatement(OpenDistroSqlParser.SetPasswordStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#setPasswordStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetPasswordStatement(OpenDistroSqlParser.SetPasswordStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userSpecification}.
	 * @param ctx the parse tree
	 */
	void enterUserSpecification(OpenDistroSqlParser.UserSpecificationContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userSpecification}.
	 * @param ctx the parse tree
	 */
	void exitUserSpecification(OpenDistroSqlParser.UserSpecificationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code passwordAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void enterPasswordAuthOption(OpenDistroSqlParser.PasswordAuthOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code passwordAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void exitPasswordAuthOption(OpenDistroSqlParser.PasswordAuthOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stringAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void enterStringAuthOption(OpenDistroSqlParser.StringAuthOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void exitStringAuthOption(OpenDistroSqlParser.StringAuthOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code hashAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void enterHashAuthOption(OpenDistroSqlParser.HashAuthOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code hashAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void exitHashAuthOption(OpenDistroSqlParser.HashAuthOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void enterSimpleAuthOption(OpenDistroSqlParser.SimpleAuthOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleAuthOption}
	 * labeled alternative in {@link OpenDistroSqlParser#userAuthOption}.
	 * @param ctx the parse tree
	 */
	void exitSimpleAuthOption(OpenDistroSqlParser.SimpleAuthOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tlsOption}.
	 * @param ctx the parse tree
	 */
	void enterTlsOption(OpenDistroSqlParser.TlsOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tlsOption}.
	 * @param ctx the parse tree
	 */
	void exitTlsOption(OpenDistroSqlParser.TlsOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userResourceOption}.
	 * @param ctx the parse tree
	 */
	void enterUserResourceOption(OpenDistroSqlParser.UserResourceOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userResourceOption}.
	 * @param ctx the parse tree
	 */
	void exitUserResourceOption(OpenDistroSqlParser.UserResourceOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userPasswordOption}.
	 * @param ctx the parse tree
	 */
	void enterUserPasswordOption(OpenDistroSqlParser.UserPasswordOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userPasswordOption}.
	 * @param ctx the parse tree
	 */
	void exitUserPasswordOption(OpenDistroSqlParser.UserPasswordOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userLockOption}.
	 * @param ctx the parse tree
	 */
	void enterUserLockOption(OpenDistroSqlParser.UserLockOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userLockOption}.
	 * @param ctx the parse tree
	 */
	void exitUserLockOption(OpenDistroSqlParser.UserLockOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#privelegeClause}.
	 * @param ctx the parse tree
	 */
	void enterPrivelegeClause(OpenDistroSqlParser.PrivelegeClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#privelegeClause}.
	 * @param ctx the parse tree
	 */
	void exitPrivelegeClause(OpenDistroSqlParser.PrivelegeClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#privilege}.
	 * @param ctx the parse tree
	 */
	void enterPrivilege(OpenDistroSqlParser.PrivilegeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#privilege}.
	 * @param ctx the parse tree
	 */
	void exitPrivilege(OpenDistroSqlParser.PrivilegeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code currentSchemaPriviLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterCurrentSchemaPriviLevel(OpenDistroSqlParser.CurrentSchemaPriviLevelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code currentSchemaPriviLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitCurrentSchemaPriviLevel(OpenDistroSqlParser.CurrentSchemaPriviLevelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code globalPrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterGlobalPrivLevel(OpenDistroSqlParser.GlobalPrivLevelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code globalPrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitGlobalPrivLevel(OpenDistroSqlParser.GlobalPrivLevelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code definiteSchemaPrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterDefiniteSchemaPrivLevel(OpenDistroSqlParser.DefiniteSchemaPrivLevelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code definiteSchemaPrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitDefiniteSchemaPrivLevel(OpenDistroSqlParser.DefiniteSchemaPrivLevelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code definiteFullTablePrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterDefiniteFullTablePrivLevel(OpenDistroSqlParser.DefiniteFullTablePrivLevelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code definiteFullTablePrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitDefiniteFullTablePrivLevel(OpenDistroSqlParser.DefiniteFullTablePrivLevelContext ctx);
	/**
	 * Enter a parse tree produced by the {@code definiteFullTablePrivLevel2}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterDefiniteFullTablePrivLevel2(OpenDistroSqlParser.DefiniteFullTablePrivLevel2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code definiteFullTablePrivLevel2}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitDefiniteFullTablePrivLevel2(OpenDistroSqlParser.DefiniteFullTablePrivLevel2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code definiteTablePrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void enterDefiniteTablePrivLevel(OpenDistroSqlParser.DefiniteTablePrivLevelContext ctx);
	/**
	 * Exit a parse tree produced by the {@code definiteTablePrivLevel}
	 * labeled alternative in {@link OpenDistroSqlParser#privilegeLevel}.
	 * @param ctx the parse tree
	 */
	void exitDefiniteTablePrivLevel(OpenDistroSqlParser.DefiniteTablePrivLevelContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#renameUserClause}.
	 * @param ctx the parse tree
	 */
	void enterRenameUserClause(OpenDistroSqlParser.RenameUserClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#renameUserClause}.
	 * @param ctx the parse tree
	 */
	void exitRenameUserClause(OpenDistroSqlParser.RenameUserClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#analyzeTable}.
	 * @param ctx the parse tree
	 */
	void enterAnalyzeTable(OpenDistroSqlParser.AnalyzeTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#analyzeTable}.
	 * @param ctx the parse tree
	 */
	void exitAnalyzeTable(OpenDistroSqlParser.AnalyzeTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#checkTable}.
	 * @param ctx the parse tree
	 */
	void enterCheckTable(OpenDistroSqlParser.CheckTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#checkTable}.
	 * @param ctx the parse tree
	 */
	void exitCheckTable(OpenDistroSqlParser.CheckTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#checksumTable}.
	 * @param ctx the parse tree
	 */
	void enterChecksumTable(OpenDistroSqlParser.ChecksumTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#checksumTable}.
	 * @param ctx the parse tree
	 */
	void exitChecksumTable(OpenDistroSqlParser.ChecksumTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#optimizeTable}.
	 * @param ctx the parse tree
	 */
	void enterOptimizeTable(OpenDistroSqlParser.OptimizeTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#optimizeTable}.
	 * @param ctx the parse tree
	 */
	void exitOptimizeTable(OpenDistroSqlParser.OptimizeTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#repairTable}.
	 * @param ctx the parse tree
	 */
	void enterRepairTable(OpenDistroSqlParser.RepairTableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#repairTable}.
	 * @param ctx the parse tree
	 */
	void exitRepairTable(OpenDistroSqlParser.RepairTableContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#checkTableOption}.
	 * @param ctx the parse tree
	 */
	void enterCheckTableOption(OpenDistroSqlParser.CheckTableOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#checkTableOption}.
	 * @param ctx the parse tree
	 */
	void exitCheckTableOption(OpenDistroSqlParser.CheckTableOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#createUdfunction}.
	 * @param ctx the parse tree
	 */
	void enterCreateUdfunction(OpenDistroSqlParser.CreateUdfunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#createUdfunction}.
	 * @param ctx the parse tree
	 */
	void exitCreateUdfunction(OpenDistroSqlParser.CreateUdfunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#installPlugin}.
	 * @param ctx the parse tree
	 */
	void enterInstallPlugin(OpenDistroSqlParser.InstallPluginContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#installPlugin}.
	 * @param ctx the parse tree
	 */
	void exitInstallPlugin(OpenDistroSqlParser.InstallPluginContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#uninstallPlugin}.
	 * @param ctx the parse tree
	 */
	void enterUninstallPlugin(OpenDistroSqlParser.UninstallPluginContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#uninstallPlugin}.
	 * @param ctx the parse tree
	 */
	void exitUninstallPlugin(OpenDistroSqlParser.UninstallPluginContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setVariable}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetVariable(OpenDistroSqlParser.SetVariableContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setVariable}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetVariable(OpenDistroSqlParser.SetVariableContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetCharset(OpenDistroSqlParser.SetCharsetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setCharset}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetCharset(OpenDistroSqlParser.SetCharsetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setNames}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetNames(OpenDistroSqlParser.SetNamesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setNames}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetNames(OpenDistroSqlParser.SetNamesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setPassword}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetPassword(OpenDistroSqlParser.SetPasswordContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setPassword}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetPassword(OpenDistroSqlParser.SetPasswordContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setTransaction}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetTransaction(OpenDistroSqlParser.SetTransactionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setTransaction}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetTransaction(OpenDistroSqlParser.SetTransactionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setAutocommit}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetAutocommit(OpenDistroSqlParser.SetAutocommitContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setAutocommit}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetAutocommit(OpenDistroSqlParser.SetAutocommitContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setNewValueInsideTrigger}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void enterSetNewValueInsideTrigger(OpenDistroSqlParser.SetNewValueInsideTriggerContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setNewValueInsideTrigger}
	 * labeled alternative in {@link OpenDistroSqlParser#setStatement}.
	 * @param ctx the parse tree
	 */
	void exitSetNewValueInsideTrigger(OpenDistroSqlParser.SetNewValueInsideTriggerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showMasterLogs}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowMasterLogs(OpenDistroSqlParser.ShowMasterLogsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showMasterLogs}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowMasterLogs(OpenDistroSqlParser.ShowMasterLogsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showLogEvents}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowLogEvents(OpenDistroSqlParser.ShowLogEventsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showLogEvents}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowLogEvents(OpenDistroSqlParser.ShowLogEventsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showObjectFilter}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowObjectFilter(OpenDistroSqlParser.ShowObjectFilterContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showObjectFilter}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowObjectFilter(OpenDistroSqlParser.ShowObjectFilterContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showColumns}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowColumns(OpenDistroSqlParser.ShowColumnsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showColumns}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowColumns(OpenDistroSqlParser.ShowColumnsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCreateDb}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateDb(OpenDistroSqlParser.ShowCreateDbContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCreateDb}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateDb(OpenDistroSqlParser.ShowCreateDbContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCreateFullIdObject}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateFullIdObject(OpenDistroSqlParser.ShowCreateFullIdObjectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCreateFullIdObject}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateFullIdObject(OpenDistroSqlParser.ShowCreateFullIdObjectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCreateUser}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowCreateUser(OpenDistroSqlParser.ShowCreateUserContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCreateUser}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowCreateUser(OpenDistroSqlParser.ShowCreateUserContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowEngine(OpenDistroSqlParser.ShowEngineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showEngine}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowEngine(OpenDistroSqlParser.ShowEngineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showGlobalInfo}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowGlobalInfo(OpenDistroSqlParser.ShowGlobalInfoContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showGlobalInfo}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowGlobalInfo(OpenDistroSqlParser.ShowGlobalInfoContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showErrors}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowErrors(OpenDistroSqlParser.ShowErrorsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showErrors}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowErrors(OpenDistroSqlParser.ShowErrorsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showCountErrors}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowCountErrors(OpenDistroSqlParser.ShowCountErrorsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showCountErrors}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowCountErrors(OpenDistroSqlParser.ShowCountErrorsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showSchemaFilter}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowSchemaFilter(OpenDistroSqlParser.ShowSchemaFilterContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showSchemaFilter}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowSchemaFilter(OpenDistroSqlParser.ShowSchemaFilterContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showRoutine}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowRoutine(OpenDistroSqlParser.ShowRoutineContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showRoutine}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowRoutine(OpenDistroSqlParser.ShowRoutineContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showGrants}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowGrants(OpenDistroSqlParser.ShowGrantsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showGrants}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowGrants(OpenDistroSqlParser.ShowGrantsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showIndexes}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowIndexes(OpenDistroSqlParser.ShowIndexesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showIndexes}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowIndexes(OpenDistroSqlParser.ShowIndexesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showOpenTables}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowOpenTables(OpenDistroSqlParser.ShowOpenTablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showOpenTables}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowOpenTables(OpenDistroSqlParser.ShowOpenTablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showProfile}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowProfile(OpenDistroSqlParser.ShowProfileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showProfile}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowProfile(OpenDistroSqlParser.ShowProfileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code showSlaveStatus}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void enterShowSlaveStatus(OpenDistroSqlParser.ShowSlaveStatusContext ctx);
	/**
	 * Exit a parse tree produced by the {@code showSlaveStatus}
	 * labeled alternative in {@link OpenDistroSqlParser#showStatement}.
	 * @param ctx the parse tree
	 */
	void exitShowSlaveStatus(OpenDistroSqlParser.ShowSlaveStatusContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#variableClause}.
	 * @param ctx the parse tree
	 */
	void enterVariableClause(OpenDistroSqlParser.VariableClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#variableClause}.
	 * @param ctx the parse tree
	 */
	void exitVariableClause(OpenDistroSqlParser.VariableClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showCommonEntity}.
	 * @param ctx the parse tree
	 */
	void enterShowCommonEntity(OpenDistroSqlParser.ShowCommonEntityContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showCommonEntity}.
	 * @param ctx the parse tree
	 */
	void exitShowCommonEntity(OpenDistroSqlParser.ShowCommonEntityContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showGlobalInfoClause}.
	 * @param ctx the parse tree
	 */
	void enterShowGlobalInfoClause(OpenDistroSqlParser.ShowGlobalInfoClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showGlobalInfoClause}.
	 * @param ctx the parse tree
	 */
	void exitShowGlobalInfoClause(OpenDistroSqlParser.ShowGlobalInfoClauseContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#showProfileType}.
	 * @param ctx the parse tree
	 */
	void enterShowProfileType(OpenDistroSqlParser.ShowProfileTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#showProfileType}.
	 * @param ctx the parse tree
	 */
	void exitShowProfileType(OpenDistroSqlParser.ShowProfileTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#binlogStatement}.
	 * @param ctx the parse tree
	 */
	void enterBinlogStatement(OpenDistroSqlParser.BinlogStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#binlogStatement}.
	 * @param ctx the parse tree
	 */
	void exitBinlogStatement(OpenDistroSqlParser.BinlogStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#cacheIndexStatement}.
	 * @param ctx the parse tree
	 */
	void enterCacheIndexStatement(OpenDistroSqlParser.CacheIndexStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#cacheIndexStatement}.
	 * @param ctx the parse tree
	 */
	void exitCacheIndexStatement(OpenDistroSqlParser.CacheIndexStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#flushStatement}.
	 * @param ctx the parse tree
	 */
	void enterFlushStatement(OpenDistroSqlParser.FlushStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#flushStatement}.
	 * @param ctx the parse tree
	 */
	void exitFlushStatement(OpenDistroSqlParser.FlushStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#killStatement}.
	 * @param ctx the parse tree
	 */
	void enterKillStatement(OpenDistroSqlParser.KillStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#killStatement}.
	 * @param ctx the parse tree
	 */
	void exitKillStatement(OpenDistroSqlParser.KillStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#loadIndexIntoCache}.
	 * @param ctx the parse tree
	 */
	void enterLoadIndexIntoCache(OpenDistroSqlParser.LoadIndexIntoCacheContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#loadIndexIntoCache}.
	 * @param ctx the parse tree
	 */
	void exitLoadIndexIntoCache(OpenDistroSqlParser.LoadIndexIntoCacheContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#resetStatement}.
	 * @param ctx the parse tree
	 */
	void enterResetStatement(OpenDistroSqlParser.ResetStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#resetStatement}.
	 * @param ctx the parse tree
	 */
	void exitResetStatement(OpenDistroSqlParser.ResetStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#shutdownStatement}.
	 * @param ctx the parse tree
	 */
	void enterShutdownStatement(OpenDistroSqlParser.ShutdownStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#shutdownStatement}.
	 * @param ctx the parse tree
	 */
	void exitShutdownStatement(OpenDistroSqlParser.ShutdownStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tableIndexes}.
	 * @param ctx the parse tree
	 */
	void enterTableIndexes(OpenDistroSqlParser.TableIndexesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tableIndexes}.
	 * @param ctx the parse tree
	 */
	void exitTableIndexes(OpenDistroSqlParser.TableIndexesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void enterSimpleFlushOption(OpenDistroSqlParser.SimpleFlushOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void exitSimpleFlushOption(OpenDistroSqlParser.SimpleFlushOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code channelFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void enterChannelFlushOption(OpenDistroSqlParser.ChannelFlushOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code channelFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void exitChannelFlushOption(OpenDistroSqlParser.ChannelFlushOptionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tableFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void enterTableFlushOption(OpenDistroSqlParser.TableFlushOptionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tableFlushOption}
	 * labeled alternative in {@link OpenDistroSqlParser#flushOption}.
	 * @param ctx the parse tree
	 */
	void exitTableFlushOption(OpenDistroSqlParser.TableFlushOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#flushTableOption}.
	 * @param ctx the parse tree
	 */
	void enterFlushTableOption(OpenDistroSqlParser.FlushTableOptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#flushTableOption}.
	 * @param ctx the parse tree
	 */
	void exitFlushTableOption(OpenDistroSqlParser.FlushTableOptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#loadedTableIndexes}.
	 * @param ctx the parse tree
	 */
	void enterLoadedTableIndexes(OpenDistroSqlParser.LoadedTableIndexesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#loadedTableIndexes}.
	 * @param ctx the parse tree
	 */
	void exitLoadedTableIndexes(OpenDistroSqlParser.LoadedTableIndexesContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#fullDescribeStatement}.
	 * @param ctx the parse tree
	 */
	void enterFullDescribeStatement(OpenDistroSqlParser.FullDescribeStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#fullDescribeStatement}.
	 * @param ctx the parse tree
	 */
	void exitFullDescribeStatement(OpenDistroSqlParser.FullDescribeStatementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#useStatement}.
	 * @param ctx the parse tree
	 */
	void enterUseStatement(OpenDistroSqlParser.UseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#useStatement}.
	 * @param ctx the parse tree
	 */
	void exitUseStatement(OpenDistroSqlParser.UseStatementContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeStatements}
	 * labeled alternative in {@link OpenDistroSqlParser#describeObjectClause}.
	 * @param ctx the parse tree
	 */
	void enterDescribeStatements(OpenDistroSqlParser.DescribeStatementsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeStatements}
	 * labeled alternative in {@link OpenDistroSqlParser#describeObjectClause}.
	 * @param ctx the parse tree
	 */
	void exitDescribeStatements(OpenDistroSqlParser.DescribeStatementsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code describeConnection}
	 * labeled alternative in {@link OpenDistroSqlParser#describeObjectClause}.
	 * @param ctx the parse tree
	 */
	void enterDescribeConnection(OpenDistroSqlParser.DescribeConnectionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code describeConnection}
	 * labeled alternative in {@link OpenDistroSqlParser#describeObjectClause}.
	 * @param ctx the parse tree
	 */
	void exitDescribeConnection(OpenDistroSqlParser.DescribeConnectionContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexColumnName}.
	 * @param ctx the parse tree
	 */
	void enterIndexColumnName(OpenDistroSqlParser.IndexColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexColumnName}.
	 * @param ctx the parse tree
	 */
	void exitIndexColumnName(OpenDistroSqlParser.IndexColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userName}.
	 * @param ctx the parse tree
	 */
	void enterUserName(OpenDistroSqlParser.UserNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userName}.
	 * @param ctx the parse tree
	 */
	void exitUserName(OpenDistroSqlParser.UserNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#mysqlVariable}.
	 * @param ctx the parse tree
	 */
	void enterMysqlVariable(OpenDistroSqlParser.MysqlVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#mysqlVariable}.
	 * @param ctx the parse tree
	 */
	void exitMysqlVariable(OpenDistroSqlParser.MysqlVariableContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#uuidSet}.
	 * @param ctx the parse tree
	 */
	void enterUuidSet(OpenDistroSqlParser.UuidSetContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#uuidSet}.
	 * @param ctx the parse tree
	 */
	void exitUuidSet(OpenDistroSqlParser.UuidSetContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xid}.
	 * @param ctx the parse tree
	 */
	void enterXid(OpenDistroSqlParser.XidContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xid}.
	 * @param ctx the parse tree
	 */
	void exitXid(OpenDistroSqlParser.XidContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#xuidStringId}.
	 * @param ctx the parse tree
	 */
	void enterXuidStringId(OpenDistroSqlParser.XuidStringIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#xuidStringId}.
	 * @param ctx the parse tree
	 */
	void exitXuidStringId(OpenDistroSqlParser.XuidStringIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#authPlugin}.
	 * @param ctx the parse tree
	 */
	void enterAuthPlugin(OpenDistroSqlParser.AuthPluginContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#authPlugin}.
	 * @param ctx the parse tree
	 */
	void exitAuthPlugin(OpenDistroSqlParser.AuthPluginContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#fileSizeLiteral}.
	 * @param ctx the parse tree
	 */
	void enterFileSizeLiteral(OpenDistroSqlParser.FileSizeLiteralContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#fileSizeLiteral}.
	 * @param ctx the parse tree
	 */
	void exitFileSizeLiteral(OpenDistroSqlParser.FileSizeLiteralContext ctx);
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
	 * Enter a parse tree produced by the {@code stringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterStringDataType(OpenDistroSqlParser.StringDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitStringDataType(OpenDistroSqlParser.StringDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nationalStringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterNationalStringDataType(OpenDistroSqlParser.NationalStringDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nationalStringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitNationalStringDataType(OpenDistroSqlParser.NationalStringDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nationalVaryingStringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterNationalVaryingStringDataType(OpenDistroSqlParser.NationalVaryingStringDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nationalVaryingStringDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitNationalVaryingStringDataType(OpenDistroSqlParser.NationalVaryingStringDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dimensionDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterDimensionDataType(OpenDistroSqlParser.DimensionDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dimensionDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitDimensionDataType(OpenDistroSqlParser.DimensionDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code simpleDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterSimpleDataType(OpenDistroSqlParser.SimpleDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code simpleDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitSimpleDataType(OpenDistroSqlParser.SimpleDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code collectionDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterCollectionDataType(OpenDistroSqlParser.CollectionDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code collectionDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitCollectionDataType(OpenDistroSqlParser.CollectionDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by the {@code spatialDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void enterSpatialDataType(OpenDistroSqlParser.SpatialDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code spatialDataType}
	 * labeled alternative in {@link OpenDistroSqlParser#dataType}.
	 * @param ctx the parse tree
	 */
	void exitSpatialDataType(OpenDistroSqlParser.SpatialDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#collectionOptions}.
	 * @param ctx the parse tree
	 */
	void enterCollectionOptions(OpenDistroSqlParser.CollectionOptionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#collectionOptions}.
	 * @param ctx the parse tree
	 */
	void exitCollectionOptions(OpenDistroSqlParser.CollectionOptionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#convertedDataType}.
	 * @param ctx the parse tree
	 */
	void enterConvertedDataType(OpenDistroSqlParser.ConvertedDataTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#convertedDataType}.
	 * @param ctx the parse tree
	 */
	void exitConvertedDataType(OpenDistroSqlParser.ConvertedDataTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lengthOneDimension}.
	 * @param ctx the parse tree
	 */
	void enterLengthOneDimension(OpenDistroSqlParser.LengthOneDimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lengthOneDimension}.
	 * @param ctx the parse tree
	 */
	void exitLengthOneDimension(OpenDistroSqlParser.LengthOneDimensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lengthTwoDimension}.
	 * @param ctx the parse tree
	 */
	void enterLengthTwoDimension(OpenDistroSqlParser.LengthTwoDimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lengthTwoDimension}.
	 * @param ctx the parse tree
	 */
	void exitLengthTwoDimension(OpenDistroSqlParser.LengthTwoDimensionContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#lengthTwoOptionalDimension}.
	 * @param ctx the parse tree
	 */
	void enterLengthTwoOptionalDimension(OpenDistroSqlParser.LengthTwoOptionalDimensionContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#lengthTwoOptionalDimension}.
	 * @param ctx the parse tree
	 */
	void exitLengthTwoOptionalDimension(OpenDistroSqlParser.LengthTwoOptionalDimensionContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#tables}.
	 * @param ctx the parse tree
	 */
	void enterTables(OpenDistroSqlParser.TablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#tables}.
	 * @param ctx the parse tree
	 */
	void exitTables(OpenDistroSqlParser.TablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#indexColumnNames}.
	 * @param ctx the parse tree
	 */
	void enterIndexColumnNames(OpenDistroSqlParser.IndexColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#indexColumnNames}.
	 * @param ctx the parse tree
	 */
	void exitIndexColumnNames(OpenDistroSqlParser.IndexColumnNamesContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#expressionsWithDefaults}.
	 * @param ctx the parse tree
	 */
	void enterExpressionsWithDefaults(OpenDistroSqlParser.ExpressionsWithDefaultsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#expressionsWithDefaults}.
	 * @param ctx the parse tree
	 */
	void exitExpressionsWithDefaults(OpenDistroSqlParser.ExpressionsWithDefaultsContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#userVariables}.
	 * @param ctx the parse tree
	 */
	void enterUserVariables(OpenDistroSqlParser.UserVariablesContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#userVariables}.
	 * @param ctx the parse tree
	 */
	void exitUserVariables(OpenDistroSqlParser.UserVariablesContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void enterDefaultValue(OpenDistroSqlParser.DefaultValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#defaultValue}.
	 * @param ctx the parse tree
	 */
	void exitDefaultValue(OpenDistroSqlParser.DefaultValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#currentTimestamp}.
	 * @param ctx the parse tree
	 */
	void enterCurrentTimestamp(OpenDistroSqlParser.CurrentTimestampContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#currentTimestamp}.
	 * @param ctx the parse tree
	 */
	void exitCurrentTimestamp(OpenDistroSqlParser.CurrentTimestampContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#expressionOrDefault}.
	 * @param ctx the parse tree
	 */
	void enterExpressionOrDefault(OpenDistroSqlParser.ExpressionOrDefaultContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#expressionOrDefault}.
	 * @param ctx the parse tree
	 */
	void exitExpressionOrDefault(OpenDistroSqlParser.ExpressionOrDefaultContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void enterIfExists(OpenDistroSqlParser.IfExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#ifExists}.
	 * @param ctx the parse tree
	 */
	void exitIfExists(OpenDistroSqlParser.IfExistsContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void enterIfNotExists(OpenDistroSqlParser.IfNotExistsContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#ifNotExists}.
	 * @param ctx the parse tree
	 */
	void exitIfNotExists(OpenDistroSqlParser.IfNotExistsContext ctx);
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
	 * Enter a parse tree produced by the {@code udfFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterUdfFunctionCall(OpenDistroSqlParser.UdfFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code udfFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitUdfFunctionCall(OpenDistroSqlParser.UdfFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code passwordFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterPasswordFunctionCall(OpenDistroSqlParser.PasswordFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code passwordFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitPasswordFunctionCall(OpenDistroSqlParser.PasswordFunctionCallContext ctx);
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
	 * Enter a parse tree produced by the {@code dataTypeFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterDataTypeFunctionCall(OpenDistroSqlParser.DataTypeFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dataTypeFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitDataTypeFunctionCall(OpenDistroSqlParser.DataTypeFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code valuesFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterValuesFunctionCall(OpenDistroSqlParser.ValuesFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code valuesFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitValuesFunctionCall(OpenDistroSqlParser.ValuesFunctionCallContext ctx);
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
	 * Enter a parse tree produced by the {@code charFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterCharFunctionCall(OpenDistroSqlParser.CharFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code charFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitCharFunctionCall(OpenDistroSqlParser.CharFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code positionFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterPositionFunctionCall(OpenDistroSqlParser.PositionFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code positionFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitPositionFunctionCall(OpenDistroSqlParser.PositionFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code substrFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterSubstrFunctionCall(OpenDistroSqlParser.SubstrFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code substrFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitSubstrFunctionCall(OpenDistroSqlParser.SubstrFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trimFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterTrimFunctionCall(OpenDistroSqlParser.TrimFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trimFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitTrimFunctionCall(OpenDistroSqlParser.TrimFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code weightFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterWeightFunctionCall(OpenDistroSqlParser.WeightFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code weightFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitWeightFunctionCall(OpenDistroSqlParser.WeightFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code extractFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterExtractFunctionCall(OpenDistroSqlParser.ExtractFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code extractFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitExtractFunctionCall(OpenDistroSqlParser.ExtractFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code getFormatFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void enterGetFormatFunctionCall(OpenDistroSqlParser.GetFormatFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code getFormatFunctionCall}
	 * labeled alternative in {@link OpenDistroSqlParser#specificFunction}.
	 * @param ctx the parse tree
	 */
	void exitGetFormatFunctionCall(OpenDistroSqlParser.GetFormatFunctionCallContext ctx);
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
	 * Enter a parse tree produced by the {@code levelWeightList}
	 * labeled alternative in {@link OpenDistroSqlParser#levelsInWeightString}.
	 * @param ctx the parse tree
	 */
	void enterLevelWeightList(OpenDistroSqlParser.LevelWeightListContext ctx);
	/**
	 * Exit a parse tree produced by the {@code levelWeightList}
	 * labeled alternative in {@link OpenDistroSqlParser#levelsInWeightString}.
	 * @param ctx the parse tree
	 */
	void exitLevelWeightList(OpenDistroSqlParser.LevelWeightListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code levelWeightRange}
	 * labeled alternative in {@link OpenDistroSqlParser#levelsInWeightString}.
	 * @param ctx the parse tree
	 */
	void enterLevelWeightRange(OpenDistroSqlParser.LevelWeightRangeContext ctx);
	/**
	 * Exit a parse tree produced by the {@code levelWeightRange}
	 * labeled alternative in {@link OpenDistroSqlParser#levelsInWeightString}.
	 * @param ctx the parse tree
	 */
	void exitLevelWeightRange(OpenDistroSqlParser.LevelWeightRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#levelInWeightListElement}.
	 * @param ctx the parse tree
	 */
	void enterLevelInWeightListElement(OpenDistroSqlParser.LevelInWeightListElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#levelInWeightListElement}.
	 * @param ctx the parse tree
	 */
	void exitLevelInWeightListElement(OpenDistroSqlParser.LevelInWeightListElementContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#passwordFunctionClause}.
	 * @param ctx the parse tree
	 */
	void enterPasswordFunctionClause(OpenDistroSqlParser.PasswordFunctionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#passwordFunctionClause}.
	 * @param ctx the parse tree
	 */
	void exitPasswordFunctionClause(OpenDistroSqlParser.PasswordFunctionClauseContext ctx);
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
	 * Enter a parse tree produced by the {@code soundsLikePredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterSoundsLikePredicate(OpenDistroSqlParser.SoundsLikePredicateContext ctx);
	/**
	 * Exit a parse tree produced by the {@code soundsLikePredicate}
	 * labeled alternative in {@link OpenDistroSqlParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitSoundsLikePredicate(OpenDistroSqlParser.SoundsLikePredicateContext ctx);
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
	 * Enter a parse tree produced by the {@code collateExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterCollateExpressionAtom(OpenDistroSqlParser.CollateExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code collateExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitCollateExpressionAtom(OpenDistroSqlParser.CollateExpressionAtomContext ctx);
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
	 * Enter a parse tree produced by the {@code mysqlVariableExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterMysqlVariableExpressionAtom(OpenDistroSqlParser.MysqlVariableExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code mysqlVariableExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitMysqlVariableExpressionAtom(OpenDistroSqlParser.MysqlVariableExpressionAtomContext ctx);
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
	 * Enter a parse tree produced by the {@code nestedRowExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterNestedRowExpressionAtom(OpenDistroSqlParser.NestedRowExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nestedRowExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitNestedRowExpressionAtom(OpenDistroSqlParser.NestedRowExpressionAtomContext ctx);
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
	 * Enter a parse tree produced by the {@code binaryExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpressionAtom(OpenDistroSqlParser.BinaryExpressionAtomContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpressionAtom}
	 * labeled alternative in {@link OpenDistroSqlParser#expressionAtom}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpressionAtom(OpenDistroSqlParser.BinaryExpressionAtomContext ctx);
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
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#transactionLevelBase}.
	 * @param ctx the parse tree
	 */
	void enterTransactionLevelBase(OpenDistroSqlParser.TransactionLevelBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#transactionLevelBase}.
	 * @param ctx the parse tree
	 */
	void exitTransactionLevelBase(OpenDistroSqlParser.TransactionLevelBaseContext ctx);
	/**
	 * Enter a parse tree produced by {@link OpenDistroSqlParser#privilegesBase}.
	 * @param ctx the parse tree
	 */
	void enterPrivilegesBase(OpenDistroSqlParser.PrivilegesBaseContext ctx);
	/**
	 * Exit a parse tree produced by {@link OpenDistroSqlParser#privilegesBase}.
	 * @param ctx the parse tree
	 */
	void exitPrivilegesBase(OpenDistroSqlParser.PrivilegesBaseContext ctx);
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
}