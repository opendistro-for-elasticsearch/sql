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

package com.amazon.opendistroforelasticsearch.sql.rewriter.identifier;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import static com.amazon.opendistroforelasticsearch.sql.utils.StringUtils.unquoteFullColumn;
import static com.amazon.opendistroforelasticsearch.sql.utils.StringUtils.unquoteSingleField;

/**
 * Quoted Identifiers Rewriter Rule
 */
public class UnquoteIdentifierRule extends MySqlASTVisitorAdapter implements RewriteRule<SQLQueryExpr> {

    /**
     *
     * This method is to adjust the AST in the cases where the field is quoted,
     * and the full name in the SELECT field is in the format of indexAlias.fieldName
     * (e.g. SELECT b.`lastname` FROM bank AS b).
     *
     * In this case, the druid parser constructs a SQLSelectItem for the field "b.`lastname`", with SQLIdentifierExpr of
     * "b." and alias of "`lastname`".
     *
     * This method corrects the SQLSelectItem object to have SQLIdentifier of "b.lastname" and alias of null.
     */
    @Override
    public boolean visit(SQLSelectItem selectItem) {
        if (selectItem.getExpr() instanceof SQLIdentifierExpr) {
            String identifier = ((SQLIdentifierExpr) selectItem.getExpr()).getName();
            if (identifier.endsWith(".")) {
                String correctedIdentifier = identifier + unquoteSingleField(selectItem.getAlias(), "`");
                selectItem.setExpr(new SQLIdentifierExpr(correctedIdentifier));
                selectItem.setAlias(null);
            }
        }
        selectItem.setAlias(unquoteSingleField(selectItem.getAlias(), "`"));
        return true;
    }

    /**
     *
     * This method is to adjust the AST in the cases where the alias of index is quoted
     * (e.g. SELECT `b`.lastname FROM bank AS `b`).
     *
     * In this case, the druid parser constructs a SQLPropertyExpr for the field "`b`.lastname", with owner of a
     * SQLIdentifierExpr "`b`" and name of "lastname".
     *
     * This method prevent the visitor from visitin the SQLPropertyExpr in this case,
     * and corrects AST with a SQLSelectItem object to have SQLIdentifier of "b.lastname".
     *
     * Used in the case where alias of index and the field name are both quoted
     * (e.g. SELECT `b`.`lastname` FROM bank AS `b`).
     */
    @Override
    public boolean visit(SQLPropertyExpr propertyExpr) {
        String fieldName = ((SQLIdentifierExpr) propertyExpr.getOwner()).getName();
        if (!StringUtils.isQuoted(fieldName, "`")) {
            return true;
        }
        String correctedIdentifier = unquoteSingleField(fieldName) + "." + unquoteSingleField(propertyExpr.getName());
        SQLSelectItem selectItem = (SQLSelectItem) propertyExpr.getParent();
        selectItem.setExpr(new SQLIdentifierExpr(correctedIdentifier));
        return false;
    }

    @Override
    public void endVisit(SQLIdentifierExpr identifierExpr) {
        identifierExpr.setName(unquoteFullColumn(identifierExpr.getName()));
    }

    @Override
    public void endVisit(SQLExprTableSource tableSource) {
        tableSource.setAlias(unquoteSingleField(tableSource.getAlias()));
    }

    @Override
    public boolean match(SQLQueryExpr root) {
        return true;
    }

    @Override
    public void rewrite(SQLQueryExpr root) {
        root.accept(new UnquoteIdentifierRule());
    }
}
