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

package com.amazon.opendistroforelasticsearch.sql.util

import com.alibaba.druid.sql.ast.*
import com.alibaba.druid.sql.ast.expr.*
import com.alibaba.druid.sql.ast.statement.*
import com.alibaba.druid.sql.visitor.SQLASTVisitor
import com.amazon.opendistroforelasticsearch.sql.parser.ElasticSqlExprParser

import java.util.HashMap
import java.util.LinkedList

fun main(args: Array<String>) {
    val queries =
            listOf(
                    """SELECT e.name, (SELECT COUNT(p.name) FROM e.projects p) AS project_count
                            FROM employess_with_missing e""",
                    """SELECT e.name, (SELECT COUNT(p.name) FROM e.projects p WHERE p.name = 'War games #1') AS project_count
                            FROM employess_with_missing e""",
                    """SELECT e.name, (SELECT p.name FROM e.projects p WHERE p.name = 'War games #1') AS names
                            FROM employess_with_missing e""",
                    """SELECT e.project.name
                            FROM employess_with_missing e"""

            )

    queries.forEach {

        val render = RenderVisitor(it)
        println(render.renderDigraph())
    }
}


class RenderVisitor(val query:String) : SQLASTVisitor {

    internal class Label(val id: String) {
        private var attributes: MutableMap<String, Any?> = HashMap()

        fun addAttribute(key: String, value: Any?) {
            attributes[key] = value
        }

        fun render() = buildString {
            append(id)

            append(" [shape=\"box\"] [ label=\"")
            append(id)
            append("\\l")

            if (attributes.isNotEmpty()) {
                attributes.entries.stream().forEach { me -> append(me.key + "=" + me.value + "\\l") }
            }

            append("\" ]")
            append("\n")
        }
    }

    // this is just an example

    private val sb = StringBuilder()
    private val stack = LinkedList<String>()

    private var label: Label = Label("")
    private var id: String = ""
    private var nodeId: Int = 0

    private val nodesToLabels = HashMap<SQLObject, Label>()

    public var parser : (String) -> SQLExpr = {
        val parser = ElasticSqlExprParser(it)
        parser.expr()
    }

    fun renderDigraph() : String {
        val expr = parser(query)
        expr.accept(this)

        return """digraph {
            query [shape="box"][label="${query.replace("\n", "\\l").trimMargin()}"]
            
            query -> ${expr.javaClass.simpleName + "_" + 1}
            $sb
        }""".trimMargin()
    }

    override fun preVisit(astNode: SQLObject) {
        id = astNode.javaClass.simpleName + "_" + ++nodeId

        label = Label(id)
        nodesToLabels[astNode] = label

        astNode.attributes.entries.stream().forEach { me -> label.addAttribute(me.key, me.value) }

        if (!stack.isEmpty()) {
            sb.append("${stack.last} -> $id\n")
        }

        stack.addLast(id)
    }

    override fun postVisit(astNode: SQLObject) {
        val label = nodesToLabels.get(astNode)!!
        label.addAttribute("raw_value", astNode.toString())
        sb.append(label.render())
        stack.removeLast()
    }

    override fun visit(x: SQLIdentifierExpr): Boolean {
        label.addAttribute("identifier", x.name)
        return true
    }

    override fun visit(astNode: SQLAggregateExpr): Boolean {
        label.addAttribute("Expression ", astNode.methodName)
        return true
    }

    override fun visit(x: SQLAllColumnExpr) = true
    override fun visit(x: SQLBetweenExpr) = true
    override fun visit(x: SQLBinaryOpExpr): Boolean {
        label.addAttribute("operation", x.operator)
        return true
    }

    override fun visit(x: SQLCaseExpr) = true
    override fun visit(x: SQLCaseExpr.Item) = true
    override fun visit(x: SQLCastExpr) = true
    override fun visit(x: SQLCharExpr) = true
    override fun visit(x: SQLExistsExpr) = true
    override fun visit(x: SQLInListExpr) = true
    override fun visit(x: SQLIntegerExpr) = true
    override fun visit(x: SQLNCharExpr) = true
    override fun visit(x: SQLNotExpr) = true
    override fun visit(x: SQLNullExpr) = true
    override fun visit(x: SQLNumberExpr) = true
    override fun visit(x: SQLPropertyExpr) = true
    override fun visit(x: SQLSelectGroupByClause) = true
    override fun visit(x: SQLSelectItem) : Boolean {
        label.addAttribute("alias", x.alias)
        return true
    }
    override fun endVisit(x: SQLCastExpr) {}
    override fun visit(astNode: SQLSelectStatement) = true
    override fun endVisit(astNode: SQLAggregateExpr) {}
    override fun visit(x: SQLVariantRefExpr) = true
    override fun endVisit(x: SQLVariantRefExpr) {}
    override fun visit(x: SQLQueryExpr) = true
    override fun endVisit(x: SQLQueryExpr) {}
    override fun visit(x: SQLUnaryExpr) = true
    override fun endVisit(x: SQLUnaryExpr) {}
    override fun visit(x: SQLHexExpr) = true
    override fun endVisit(x: SQLHexExpr) {}
    override fun visit(x: SQLSelect) = true
    override fun endVisit(select: SQLSelect) {}
    override fun visit(x: SQLSelectQueryBlock) = true
    override fun endVisit(x: SQLSelectQueryBlock) {}
    override fun visit(x: SQLExprTableSource) : Boolean {
        label.addAttribute("alias", x.alias)
        return true
    }
    override fun endVisit(x: SQLExprTableSource) {}
    override fun visit(x: SQLOrderBy) = true
    override fun endVisit(x: SQLOrderBy) {}
    override fun visit(x: SQLSelectOrderByItem) = true
    override fun endVisit(x: SQLSelectOrderByItem) {}
    override fun visit(x: SQLDropTableStatement) = true
    override fun endVisit(x: SQLDropTableStatement) {}
    override fun visit(x: SQLCreateTableStatement) = true
    override fun endVisit(x: SQLCreateTableStatement) {}
    override fun visit(x: SQLColumnDefinition) = true
    override fun endVisit(x: SQLColumnDefinition) {}
    override fun visit(x: SQLDataType) = true
    override fun endVisit(x: SQLDataType) {}
    override fun visit(x: SQLCharacterDataType) = true
    override fun endVisit(x: SQLCharacterDataType) {}
    override fun visit(x: SQLDeleteStatement) = true
    override fun endVisit(x: SQLDeleteStatement) {}
    override fun visit(x: SQLCurrentOfCursorExpr) = true
    override fun endVisit(x: SQLCurrentOfCursorExpr) {}
    override fun visit(x: SQLInsertStatement) = true
    override fun endVisit(x: SQLInsertStatement) {}
    override fun visit(x: SQLInsertStatement.ValuesClause) = true
    override fun endVisit(x: SQLInsertStatement.ValuesClause) {}
    override fun visit(x: SQLUpdateSetItem) = true
    override fun endVisit(x: SQLUpdateSetItem) {}
    override fun visit(x: SQLUpdateStatement) = true
    override fun endVisit(x: SQLUpdateStatement) {}
    override fun visit(x: SQLCreateViewStatement) = true
    override fun endVisit(x: SQLCreateViewStatement) {}
    override fun visit(x: NotNullConstraint) = true
    override fun endVisit(x: NotNullConstraint) {}
    override fun endVisit(x: SQLMethodInvokeExpr) {}
    override fun visit(x: SQLMethodInvokeExpr) = true
    override fun endVisit(x: SQLUnionQuery) {}
    override fun visit(x: SQLUnionQuery) = true
    override fun endVisit(x: SQLSetStatement) {}
    override fun visit(x: SQLSetStatement) = true
    override fun endVisit(x: SQLAssignItem) {}
    override fun visit(x: SQLAssignItem) = true
    override fun endVisit(x: SQLCallStatement) {}
    override fun visit(x: SQLCallStatement) = true
    override fun endVisit(x: SQLJoinTableSource) {}
    override fun visit(x: SQLJoinTableSource) = true
    override fun endVisit(x: SQLSomeExpr) {}
    override fun visit(x: SQLSomeExpr) = true
    override fun endVisit(x: SQLAnyExpr) {}
    override fun visit(x: SQLAnyExpr) = true
    override fun endVisit(x: SQLAllExpr) {}
    override fun visit(x: SQLAllExpr) = true
    override fun endVisit(x: SQLInSubQueryExpr) {}
    override fun visit(x: SQLInSubQueryExpr) = true
    override fun endVisit(x: SQLListExpr) {}
    override fun visit(x: SQLListExpr) = true
    override fun endVisit(x: SQLSubqueryTableSource) {}
    override fun visit(x: SQLSubqueryTableSource) = true
    override fun endVisit(x: SQLTruncateStatement) {}
    override fun visit(x: SQLTruncateStatement) = true
    override fun endVisit(x: SQLDefaultExpr) {}
    override fun visit(x: SQLDefaultExpr) = true
    override fun endVisit(x: SQLCommentStatement) {}
    override fun visit(x: SQLCommentStatement) = true
    override fun endVisit(x: SQLUseStatement) {}
    override fun visit(x: SQLUseStatement) = true
    override fun visit(x: SQLAlterTableAddColumn) = true
    override fun endVisit(x: SQLAlterTableAddColumn) {}
    override fun visit(x: SQLAlterTableDropColumnItem) = true
    override fun endVisit(x: SQLAlterTableDropColumnItem) {}
    override fun visit(x: SQLAlterTableDropIndex) = true
    override fun endVisit(x: SQLAlterTableDropIndex) {}
    override fun visit(x: SQLDropIndexStatement) = true
    override fun endVisit(x: SQLDropIndexStatement) {}
    override fun visit(x: SQLDropViewStatement) = true
    override fun endVisit(x: SQLDropViewStatement) {}
    override fun visit(x: SQLSavePointStatement) = true
    override fun endVisit(x: SQLSavePointStatement) {}
    override fun visit(x: SQLRollbackStatement) = true
    override fun endVisit(x: SQLRollbackStatement) {}
    override fun visit(x: SQLReleaseSavePointStatement) = true
    override fun endVisit(x: SQLReleaseSavePointStatement) {}
    override fun endVisit(x: SQLCommentHint) {}
    override fun visit(x: SQLCommentHint) = true
    override fun endVisit(x: SQLCreateDatabaseStatement) {}
    override fun visit(x: SQLCreateDatabaseStatement) = true
    override fun endVisit(x: SQLOver) {}
    override fun visit(x: SQLOver) = true
    override fun endVisit(x: SQLColumnPrimaryKey) {}
    override fun visit(x: SQLColumnPrimaryKey) = true
    override fun visit(x: SQLColumnUniqueKey) = true
    override fun endVisit(x: SQLColumnUniqueKey) {}
    override fun endVisit(x: SQLWithSubqueryClause) {}
    override fun visit(x: SQLWithSubqueryClause) = true
    override fun endVisit(x: SQLWithSubqueryClause.Entry) {}
    override fun visit(x: SQLWithSubqueryClause.Entry) = true
    override fun endVisit(x: SQLAlterTableAlterColumn) {}
    override fun visit(x: SQLAlterTableAlterColumn) = true
    override fun visit(x: SQLCheck) = true
    override fun endVisit(x: SQLCheck) {}
    override fun visit(x: SQLAlterTableDropForeignKey) = true
    override fun endVisit(x: SQLAlterTableDropForeignKey) {}
    override fun visit(x: SQLAlterTableDropPrimaryKey) = true
    override fun endVisit(x: SQLAlterTableDropPrimaryKey) {}
    override fun visit(x: SQLAlterTableDisableKeys) = true
    override fun endVisit(x: SQLAlterTableDisableKeys) {}
    override fun visit(x: SQLAlterTableEnableKeys) = true
    override fun endVisit(x: SQLAlterTableEnableKeys) {}
    override fun visit(x: SQLAlterTableStatement) = true
    override fun endVisit(x: SQLAlterTableStatement) {}
    override fun visit(x: SQLAlterTableDisableConstraint) = true
    override fun endVisit(x: SQLAlterTableDisableConstraint) {}
    override fun visit(x: SQLAlterTableEnableConstraint) = true
    override fun endVisit(x: SQLAlterTableEnableConstraint) {}
    override fun visit(x: SQLColumnCheck) = true
    override fun endVisit(x: SQLColumnCheck) {}
    override fun visit(x: SQLExprHint) = true
    override fun endVisit(x: SQLExprHint) {}
    override fun visit(x: SQLAlterTableDropConstraint) = true
    override fun endVisit(x: SQLAlterTableDropConstraint) {}
    override fun visit(x: SQLUnique) = true
    override fun endVisit(x: SQLUnique) {}
    override fun visit(x: SQLPrimaryKeyImpl) = true
    override fun endVisit(x: SQLPrimaryKeyImpl) {}
    override fun visit(x: SQLCreateIndexStatement) = true
    override fun endVisit(x: SQLCreateIndexStatement) {}
    override fun visit(x: SQLAlterTableRenameColumn) = true
    override fun endVisit(x: SQLAlterTableRenameColumn) {}
    override fun visit(x: SQLColumnReference) = true
    override fun endVisit(x: SQLColumnReference) {}
    override fun visit(x: SQLForeignKeyImpl) = true
    override fun endVisit(x: SQLForeignKeyImpl) {}
    override fun visit(x: SQLDropSequenceStatement) = true
    override fun endVisit(x: SQLDropSequenceStatement) {}
    override fun visit(x: SQLDropTriggerStatement) = true
    override fun endVisit(x: SQLDropTriggerStatement) {}
    override fun endVisit(x: SQLDropUserStatement) {}
    override fun visit(x: SQLDropUserStatement) = true
    override fun endVisit(x: SQLExplainStatement) {}
    override fun visit(x: SQLExplainStatement) = true
    override fun endVisit(x: SQLGrantStatement) {}
    override fun visit(x: SQLGrantStatement) = true
    override fun endVisit(x: SQLDropDatabaseStatement) {}
    override fun visit(x: SQLDropDatabaseStatement) = true
    override fun endVisit(x: SQLAlterTableAddIndex) {}
    override fun visit(x: SQLAlterTableAddIndex) = true
    override fun endVisit(x: SQLAlterTableAddConstraint) {}
    override fun visit(x: SQLAlterTableAddConstraint) = true
    override fun endVisit(x: SQLCreateTriggerStatement) {}
    override fun visit(x: SQLCreateTriggerStatement) = true
    override fun endVisit(x: SQLDropFunctionStatement) {}
    override fun visit(x: SQLDropFunctionStatement) = true
    override fun endVisit(x: SQLDropTableSpaceStatement) {}
    override fun visit(x: SQLDropTableSpaceStatement) = true
    override fun endVisit(x: SQLDropProcedureStatement) {}
    override fun visit(x: SQLDropProcedureStatement) = true
    override fun endVisit(x: SQLBooleanExpr) {}
    override fun visit(x: SQLBooleanExpr) = true
    override fun endVisit(x: SQLUnionQueryTableSource) {}
    override fun visit(x: SQLUnionQueryTableSource) = true
    override fun endVisit(x: SQLTimestampExpr) {}
    override fun visit(x: SQLTimestampExpr) = true
    override fun endVisit(x: SQLRevokeStatement) {}
    override fun visit(x: SQLRevokeStatement) = true
    override fun endVisit(x: SQLBinaryExpr) {}
    override fun visit(x: SQLBinaryExpr) = true
    override fun endVisit(x: SQLAlterTableRename) {}
    override fun visit(x: SQLAlterTableRename) = true
    override fun endVisit(x: SQLAllColumnExpr) {}
    override fun endVisit(x: SQLBetweenExpr) {}
    override fun endVisit(x: SQLBinaryOpExpr) {}
    override fun endVisit(x: SQLCaseExpr) {}
    override fun endVisit(x: SQLCaseExpr.Item) {}
    override fun endVisit(x: SQLCharExpr) {}
    override fun endVisit(x: SQLIdentifierExpr) {}
    override fun endVisit(x: SQLInListExpr) {}
    override fun endVisit(x: SQLIntegerExpr) {}
    override fun endVisit(x: SQLExistsExpr) {}
    override fun endVisit(x: SQLNCharExpr) {}
    override fun endVisit(x: SQLNotExpr) {}
    override fun endVisit(x: SQLNullExpr) {}
    override fun endVisit(x: SQLNumberExpr) {}
    override fun endVisit(x: SQLPropertyExpr) {}
    override fun endVisit(x: SQLSelectGroupByClause) {}
    override fun endVisit(x: SQLSelectItem) {}
    override fun endVisit(selectStatement: SQLSelectStatement) {}
}