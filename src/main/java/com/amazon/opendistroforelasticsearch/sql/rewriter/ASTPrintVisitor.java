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

package com.amazon.opendistroforelasticsearch.sql.rewriter;

import com.alibaba.druid.sql.ast.SQLCommentHint;
import com.alibaba.druid.sql.ast.SQLDataType;
import com.alibaba.druid.sql.ast.SQLObject;
import com.alibaba.druid.sql.ast.SQLOrderBy;
import com.alibaba.druid.sql.ast.SQLOver;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllColumnExpr;
import com.alibaba.druid.sql.ast.expr.SQLAllExpr;
import com.alibaba.druid.sql.ast.expr.SQLAnyExpr;
import com.alibaba.druid.sql.ast.expr.SQLBetweenExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBooleanExpr;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLCurrentOfCursorExpr;
import com.alibaba.druid.sql.ast.expr.SQLDefaultExpr;
import com.alibaba.druid.sql.ast.expr.SQLExistsExpr;
import com.alibaba.druid.sql.ast.expr.SQLHexExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLIntegerExpr;
import com.alibaba.druid.sql.ast.expr.SQLListExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLNCharExpr;
import com.alibaba.druid.sql.ast.expr.SQLNotExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.alibaba.druid.sql.ast.expr.SQLNumberExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.expr.SQLSomeExpr;
import com.alibaba.druid.sql.ast.expr.SQLTimestampExpr;
import com.alibaba.druid.sql.ast.expr.SQLUnaryExpr;
import com.alibaba.druid.sql.ast.expr.SQLVariantRefExpr;
import com.alibaba.druid.sql.ast.statement.NotNullConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAddIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableAlterColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDisableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropColumnItem;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropForeignKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropIndex;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableDropPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableConstraint;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableEnableKeys;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRename;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableRenameColumn;
import com.alibaba.druid.sql.ast.statement.SQLAlterTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLAssignItem;
import com.alibaba.druid.sql.ast.statement.SQLCallStatement;
import com.alibaba.druid.sql.ast.statement.SQLCharacterDataType;
import com.alibaba.druid.sql.ast.statement.SQLCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnCheck;
import com.alibaba.druid.sql.ast.statement.SQLColumnDefinition;
import com.alibaba.druid.sql.ast.statement.SQLColumnPrimaryKey;
import com.alibaba.druid.sql.ast.statement.SQLColumnReference;
import com.alibaba.druid.sql.ast.statement.SQLColumnUniqueKey;
import com.alibaba.druid.sql.ast.statement.SQLCommentStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLCreateViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLDeleteStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropDatabaseStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropFunctionStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropIndexStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropProcedureStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropSequenceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableSpaceStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTableStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropTriggerStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropUserStatement;
import com.alibaba.druid.sql.ast.statement.SQLDropViewStatement;
import com.alibaba.druid.sql.ast.statement.SQLExplainStatement;
import com.alibaba.druid.sql.ast.statement.SQLExprHint;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLForeignKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLGrantStatement;
import com.alibaba.druid.sql.ast.statement.SQLInsertStatement;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLPrimaryKeyImpl;
import com.alibaba.druid.sql.ast.statement.SQLReleaseSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLRevokeStatement;
import com.alibaba.druid.sql.ast.statement.SQLRollbackStatement;
import com.alibaba.druid.sql.ast.statement.SQLSavePointStatement;
import com.alibaba.druid.sql.ast.statement.SQLSelect;
import com.alibaba.druid.sql.ast.statement.SQLSelectGroupByClause;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.ast.statement.SQLSelectStatement;
import com.alibaba.druid.sql.ast.statement.SQLSetStatement;
import com.alibaba.druid.sql.ast.statement.SQLSubqueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLTruncateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUnionQuery;
import com.alibaba.druid.sql.ast.statement.SQLUnionQueryTableSource;
import com.alibaba.druid.sql.ast.statement.SQLUnique;
import com.alibaba.druid.sql.ast.statement.SQLUpdateSetItem;
import com.alibaba.druid.sql.ast.statement.SQLUpdateStatement;
import com.alibaba.druid.sql.ast.statement.SQLUseStatement;
import com.alibaba.druid.sql.ast.statement.SQLWithSubqueryClause;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlForceIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlIgnoreIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUseIndexHint;
import com.alibaba.druid.sql.dialect.mysql.ast.MysqlForeignKey;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlCharExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlExtractExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlIntervalExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlMatchAgainstExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlOutFileExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlUserName;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.CobarShowStatus;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableAddColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableChangeColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableCharacter;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableDiscardTablespace;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableImportTablespace;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableModifyColumn;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableOption;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAlterUserStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlAnalyzeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlBinlogStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlBlockStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCommitStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateIndexStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateUserStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDeleteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlDescribeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlExecuteStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHelpStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlHintStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlKillStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadDataInFileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLoadXmlStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlLockTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlOptimizeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByHash;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByKey;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByList;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitionByRange;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPartitioningDef;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlPrepareStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRenameTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlReplaceStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlResetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlRollbackStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectGroupBy;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetCharSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetNamesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetPasswordStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSetTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowAuthorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowBinaryLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCharacterSetStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCollationStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowColumnsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowContributorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateDatabaseStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateEventStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateFunctionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateProcedureStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateTriggerStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowCreateViewStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowDatabasesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEngineStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEnginesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowErrorsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowFunctionStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowGrantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowIndexesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowKeysStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterLogsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowMasterStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowOpenTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPluginsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowPrivilegesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureCodeStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcedureStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProcessListStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfileStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowProfilesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowRelayLogEventsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveHostsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowSlaveStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTableStatusStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowTriggersStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowVariantsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlShowWarningsStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlStartTransactionStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlTableIndex;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnionQuery;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUnlockTablesStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ASTPrintVisitor extends MySqlASTVisitorAdapter {

    private static final Logger logger = LogManager.getLogger();
    private static final String TAB = "    ";
    private static final String NEW_LINE = "\n";

    private StringBuilder stringBuilder = new StringBuilder("\n");

    private int level = 0;

    public void printAST() {
        logger.info(stringBuilder.toString());
    }

    private void addPrintableNode(int l, String simpleText) {
        for (int i = 0; i < l; i++) {
            stringBuilder.append(TAB);
        }

        stringBuilder.append(simpleText);
        stringBuilder.append(NEW_LINE);
    }

    private void addPrintableNodeName(int l, Object o) {
        addPrintableNode(l, o.getClass().getSimpleName());
    }

    public void endVisit(SQLAllColumnExpr x) {
        level--;
    }

    public void endVisit(SQLBetweenExpr x) {
        level--;
    }

    public void endVisit(SQLBinaryOpExpr x) {
        level--;
    }

    public void endVisit(SQLCaseExpr x) {
        level--;
    }

    public void endVisit(SQLCaseExpr.Item x) {
        level--;
    }

    public void endVisit(SQLCharExpr x) {
        level--;
    }

    public void endVisit(SQLIdentifierExpr x) {
        level--;
    }

    public void endVisit(SQLInListExpr x) {
        level--;
    }

    public void endVisit(SQLIntegerExpr x) {
        level--;
    }

    public void endVisit(SQLExistsExpr x) {
        level--;
    }

    public void endVisit(SQLNCharExpr x) {
        level--;
    }

    public void endVisit(SQLNotExpr x) {
        level--;
    }

    public void endVisit(SQLNullExpr x) {
        level--;
    }

    public void endVisit(SQLNumberExpr x) {
        level--;
    }

    public void endVisit(SQLPropertyExpr x) {
        level--;
    }

    public void endVisit(SQLSelectGroupByClause x) {
        level--;
    }

    public void endVisit(SQLSelectItem x) {
        level--;
    }

    public void endVisit(SQLSelectStatement selectStatement) {
        level--;
    }

    public void postVisit(SQLObject astNode) {
    }

    public void preVisit(SQLObject astNode) {
    }

    public boolean visit(SQLAllColumnExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLBetweenExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLBinaryOpExpr x) {
        addPrintableNode(level,
            String.format(
                "%s op: %s",
                x.getClass().getSimpleName(),
                x.getOperator()));
        level++;
        return true;
    }

    public boolean visit(SQLCaseExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLCaseExpr.Item x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLCastExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLCharExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLExistsExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLIdentifierExpr x) {
        addPrintableNode(level, String.format("%s  name: %s", x.getClass().getSimpleName(), x.getName()));
        level++;
        return true;
    }

    public boolean visit(SQLInListExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLIntegerExpr x) {
        addPrintableNode(level,
            String.format(
                "%s (n: %s, v: %s)",
                x.getClass().getSimpleName(),
                x.getNumber(),
                x.getValue())
        );
        level++;
        return true;
    }

    public boolean visit(SQLNCharExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLNotExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLNullExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLNumberExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLPropertyExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLSelectGroupByClause x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLSelectItem x) {
        addPrintableNode(level,
            String.format(
                "%s (expr:%s,  alias:%s)",
                x.getClass().getSimpleName(),
                x.getExpr().toString(),
                x.getAlias())
        );
        level++;
        return true;
    }

    public void endVisit(SQLCastExpr x) {
        level--;
    }

    public boolean visit(SQLSelectStatement astNode) {
        addPrintableNodeName(level, astNode);
        level++;
        return true;
    }

    public void endVisit(SQLAggregateExpr x) {
        level--;
    }

    public boolean visit(SQLAggregateExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public boolean visit(SQLVariantRefExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLVariantRefExpr x) {
        level--;
    }

    public boolean visit(SQLQueryExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLQueryExpr x) {
        level--;
    }

    public boolean visit(SQLSelect x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLSelect select) {
        level--;
    }

    public boolean visit(SQLSelectQueryBlock x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLSelectQueryBlock x) {
        level--;
    }

    public boolean visit(SQLExprTableSource x) {
        addPrintableNode(level,
            String.format(
                "%s alias: %s",
                x.getClass().getSimpleName(),
                x.getAlias()
            )
        );
        level++;
        return true;
    }

    public void endVisit(SQLExprTableSource x) {
        level--;
    }

    public boolean visit(SQLOrderBy x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLOrderBy x) {
        level--;
    }

    public boolean visit(SQLSelectOrderByItem x) {

        String typeString = "NULL";
        if (x.getType()!=null) {
            typeString = x.getType().toString();
        }

        String nullOrderTypeString = "NULL";
        if (x.getNullsOrderType()!=null) {
            nullOrderTypeString = x.getNullsOrderType().toFormalString();
        }

        addPrintableNode(level,
            String.format(
                "%s type: %s nullOrderType: %s",
                x.getClass().getSimpleName(),
                typeString,
                nullOrderTypeString));
        level++;
        return true;
    }

    public void endVisit(SQLSelectOrderByItem x) {
        level--;
    }

    public boolean visit(SQLDropTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLDropTableStatement x) {
        level--;
    }

    public boolean visit(SQLCreateTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLCreateTableStatement x) {
        level--;
    }

    public boolean visit(SQLColumnDefinition x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLColumnDefinition x) {
        level--;
    }

    public boolean visit(SQLDataType x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLDataType x) {
        level--;
    }

    public boolean visit(SQLDeleteStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLDeleteStatement x) {
        level--;
    }

    public boolean visit(SQLCurrentOfCursorExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLCurrentOfCursorExpr x) {
        level--;
    }

    public boolean visit(SQLInsertStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLInsertStatement x) {
        level--;
    }

    public boolean visit(SQLUpdateSetItem x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLUpdateSetItem x) {
        level--;
    }

    public boolean visit(SQLUpdateStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLUpdateStatement x) {
        level--;
    }

    public boolean visit(SQLCreateViewStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    public void endVisit(SQLCreateViewStatement x) {
        level--;
    }

    public boolean visit(NotNullConstraint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;

    }

    public void endVisit(NotNullConstraint x) {
        level--;
    }

    @Override
    public void endVisit(SQLMethodInvokeExpr x) {
        level--;

    }

    @Override
    public boolean visit(SQLMethodInvokeExpr x) {
        addPrintableNode(level,
            String.format(
                "%s ( %s )",
                x.getClass().getSimpleName(),
                x.getMethodName()));
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLUnionQuery x) {
        level--;
    }

    @Override
    public boolean visit(SQLUnionQuery x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLUnaryExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLUnaryExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLHexExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLHexExpr x) {
        level--;
    }

    @Override
    public void endVisit(SQLSetStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLSetStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAssignItem x) {
        level--;
    }

    @Override
    public boolean visit(SQLAssignItem x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCallStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLCallStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLJoinTableSource x) {
        level--;
    }

    @Override
    public boolean visit(SQLJoinTableSource x) {
        addPrintableNode(level,
            String.format(
                "%s %s",
                x.getClass().getSimpleName(),
                x.getJoinType()
            )
        );
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLInsertStatement.ValuesClause x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLInsertStatement.ValuesClause x) {
        level--;
    }

    @Override
    public void endVisit(SQLSomeExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLSomeExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAnyExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLAnyExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAllExpr x) {
        level--;

    }

    @Override
    public boolean visit(SQLAllExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLInSubQueryExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLInSubQueryExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLListExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLListExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLSubqueryTableSource x) {
        level--;
    }

    @Override
    public boolean visit(SQLSubqueryTableSource x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLTruncateStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLTruncateStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDefaultExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLDefaultExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCommentStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLCommentStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLUseStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLUseStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLAlterTableAddColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddColumn x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDropColumnItem x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropColumnItem x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropIndexStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropIndexStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropViewStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropViewStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLSavePointStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLSavePointStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLRollbackStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLRollbackStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLReleaseSavePointStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLReleaseSavePointStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLCommentHint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCommentHint x) {
        level--;
    }

    @Override
    public void endVisit(SQLCreateDatabaseStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLCreateDatabaseStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLAlterTableDropIndex x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropIndex x) {
        level--;
    }

    @Override
    public void endVisit(SQLOver x) {
        level--;
    }

    @Override
    public boolean visit(SQLOver x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLColumnPrimaryKey x) {
        level--;
    }

    @Override
    public boolean visit(SQLColumnPrimaryKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLColumnUniqueKey x) {
        level--;
    }

    @Override
    public boolean visit(SQLColumnUniqueKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLWithSubqueryClause x) {
        level--;
    }

    @Override
    public boolean visit(SQLWithSubqueryClause x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLWithSubqueryClause.Entry x) {
        level--;
    }

    @Override
    public boolean visit(SQLWithSubqueryClause.Entry x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLCharacterDataType x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCharacterDataType x) {
        level--;
    }

    @Override
    public void endVisit(SQLAlterTableAlterColumn x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableAlterColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(SQLCheck x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCheck x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDropForeignKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropForeignKey x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDropPrimaryKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropPrimaryKey x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDisableKeys x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDisableKeys x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableEnableKeys x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableEnableKeys x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDisableConstraint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDisableConstraint x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableEnableConstraint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableEnableConstraint x) {
        level--;
    }

    @Override
    public boolean visit(SQLColumnCheck x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLColumnCheck x) {
        level--;
    }

    @Override
    public boolean visit(SQLExprHint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLExprHint x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableDropConstraint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableDropConstraint x) {
        level--;
    }

    @Override
    public boolean visit(SQLUnique x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLUnique x) {
        level--;
    }

    @Override
    public boolean visit(SQLCreateIndexStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCreateIndexStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLPrimaryKeyImpl x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLPrimaryKeyImpl x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableRenameColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRenameColumn x) {
        level--;
    }

    @Override
    public boolean visit(SQLColumnReference x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLColumnReference x) {
        level--;
    }

    @Override
    public boolean visit(SQLForeignKeyImpl x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLForeignKeyImpl x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropSequenceStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropSequenceStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropTriggerStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropTriggerStatement x) {
        level--;
    }

    @Override
    public void endVisit(SQLDropUserStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropUserStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLExplainStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLExplainStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLGrantStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLGrantStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropDatabaseStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropDatabaseStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddIndex x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableAddIndex x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableAddConstraint x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableAddConstraint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLCreateTriggerStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLCreateTriggerStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropFunctionStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropFunctionStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropTableSpaceStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropTableSpaceStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLDropProcedureStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLDropProcedureStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLBooleanExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLBooleanExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLUnionQueryTableSource x) {
        level--;
    }

    @Override
    public boolean visit(SQLUnionQueryTableSource x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLTimestampExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLTimestampExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLRevokeStatement x) {
        level--;
    }

    @Override
    public boolean visit(SQLRevokeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLBinaryExpr x) {
        level--;
    }

    @Override
    public boolean visit(SQLBinaryExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(SQLAlterTableRename x) {
        level--;
    }

    @Override
    public boolean visit(SQLAlterTableRename x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock.Limit x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock.Limit x) {
        level--;
    }

    @Override
    public boolean visit(MySqlTableIndex x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlTableIndex x) {
        level--;
    }

    @Override
    public boolean visit(MySqlKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlKey x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPrimaryKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPrimaryKey x) {
        level--;
    }

    @Override
    public void endVisit(MySqlIntervalExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlIntervalExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlExtractExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlExtractExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlMatchAgainstExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlMatchAgainstExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPrepareStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPrepareStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlExecuteStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlExecuteStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlDeleteStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlDeleteStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlInsertStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlInsertStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlLoadDataInFileStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlLoadDataInFileStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlLoadXmlStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlLoadXmlStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlReplaceStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlReplaceStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSelectGroupBy x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSelectGroupBy x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlStartTransactionStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlStartTransactionStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCommitStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCommitStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlRollbackStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlRollbackStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowColumnsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowColumnsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowTablesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowTablesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowDatabasesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowDatabasesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowWarningsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowWarningsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(CobarShowStatus x) {
        level--;
    }

    @Override
    public boolean visit(CobarShowStatus x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlKillStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlKillStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlBinlogStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlBinlogStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlResetStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlResetStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCreateUserStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCreateUserStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCreateUserStatement.UserSpecification x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCreateUserStatement.UserSpecification x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitionByKey x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitionByKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock x) {
        addPrintableNode(level,
            String.format(
                "%s attr: %s ",
                x.getClass().getSimpleName(),
                x.getAttributes()
            )
        );
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock x) {
        level--;
    }

    @Override
    public boolean visit(MySqlOutFileExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlOutFileExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlDescribeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlDescribeStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUpdateStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUpdateStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSetTransactionStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSetTransactionStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSetNamesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSetNamesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSetCharSetStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSetCharSetStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowAuthorsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowAuthorsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowBinaryLogsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowBinaryLogsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowMasterLogsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowMasterLogsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCollationStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCollationStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowBinLogEventsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowBinLogEventsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCharacterSetStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCharacterSetStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowContributorsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowContributorsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateDatabaseStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateDatabaseStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateEventStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateEventStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateFunctionStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateFunctionStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateProcedureStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateProcedureStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateTriggerStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateTriggerStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowCreateViewStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowCreateViewStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowEngineStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowEngineStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowEnginesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowEnginesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowErrorsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowErrorsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowEventsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowEventsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowFunctionCodeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowFunctionCodeStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowFunctionStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowFunctionStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowGrantsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowGrantsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUserName x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUserName x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowIndexesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowIndexesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowKeysStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowKeysStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowMasterStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowMasterStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowOpenTablesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowOpenTablesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowPluginsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowPluginsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowPrivilegesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowPrivilegesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowProcedureCodeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcedureCodeStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowProcedureStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcedureStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowProcessListStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowProcessListStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowProfileStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowProfileStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowProfilesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowProfilesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowRelayLogEventsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowRelayLogEventsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowSlaveHostsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowSlaveHostsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowSlaveStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowSlaveStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowTableStatusStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowTableStatusStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowTriggersStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowTriggersStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlShowVariantsStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlShowVariantsStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableAddColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableAddColumn x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCreateIndexStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCreateIndexStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlRenameTableStatement.Item x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlRenameTableStatement.Item x) {
        level--;
    }

    @Override
    public boolean visit(MySqlRenameTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlRenameTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUnionQuery x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUnionQuery x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUseIndexHint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUseIndexHint x) {
        level--;
    }

    @Override
    public boolean visit(MySqlIgnoreIndexHint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlIgnoreIndexHint x) {
        level--;
    }

    @Override
    public boolean visit(MySqlLockTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlLockTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUnlockTablesStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUnlockTablesStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlForceIndexHint x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlForceIndexHint x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableChangeColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableChangeColumn x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableCharacter x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableCharacter x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableOption x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableOption x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCreateTableStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCreateTableStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlHelpStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlHelpStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCharExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCharExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlUnique x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlUnique x) {
        level--;
    }

    @Override
    public boolean visit(MysqlForeignKey x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MysqlForeignKey x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableModifyColumn x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableModifyColumn x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableDiscardTablespace x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableDiscardTablespace x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterTableImportTablespace x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterTableImportTablespace x) {
        level--;
    }

    @Override
    public boolean visit(MySqlCreateTableStatement.TableSpaceOption x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlCreateTableStatement.TableSpaceOption x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitionByHash x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitionByHash x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitionByRange x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitionByRange x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitioningDef x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitioningDef x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitioningDef.LessThanValues x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitioningDef.LessThanValues x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitioningDef.InValues x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitioningDef.InValues x) {
        level--;
    }

    @Override
    public boolean visit(MySqlPartitionByList x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlPartitionByList x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAnalyzeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAnalyzeStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlAlterUserStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlAlterUserStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlOptimizeStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlOptimizeStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSetPasswordStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSetPasswordStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlHintStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlHintStatement x) {
        level--;
    }

    @Override
    public boolean visit(MySqlSelectGroupByExpr x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlSelectGroupByExpr x) {
        level--;
    }

    @Override
    public boolean visit(MySqlBlockStatement x) {
        addPrintableNodeName(level, x);
        level++;
        return true;
    }

    @Override
    public void endVisit(MySqlBlockStatement x) {
        level--;
    }

}