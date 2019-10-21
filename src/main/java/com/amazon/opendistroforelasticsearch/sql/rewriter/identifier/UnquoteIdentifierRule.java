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
import com.amazon.opendistroforelasticsearch.sql.utils.BackticksUnquoter;

public class UnquoteIdentifierRule extends MySqlASTVisitorAdapter implements RewriteRule<SQLQueryExpr> {

    String identifier = null;

    @Override
    public boolean visit(SQLPropertyExpr propertyExpr) {
        String identifier = ((SQLIdentifierExpr) propertyExpr.getOwner()).getName();
        if (identifier.startsWith("`") && identifier.endsWith("`")) {
            this.identifier = new BackticksUnquoter().unquoteSingleField(identifier) + "."
                    + new BackticksUnquoter().unquoteSingleField(propertyExpr.getName());
            SQLSelectItem selectItem = (SQLSelectItem) propertyExpr.getParent();
            selectItem.setExpr(new SQLIdentifierExpr(this.identifier));
            this.identifier = null;
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(SQLSelectItem selectItem) {
        try {
            String identifier = ((SQLIdentifierExpr) selectItem.getExpr()).getName();
            if (identifier.endsWith(".")) {
                this.identifier = identifier + new BackticksUnquoter().unquoteSingleField(selectItem.getAlias());
                selectItem.setExpr(new SQLIdentifierExpr(this.identifier));
                selectItem.setAlias(null);
            }
        } finally {
            return true;
        }
    }

    @Override
    public boolean visit(SQLIdentifierExpr identifierExpr) {
        if (identifier != null) {
            return false;
        }
        return true;
    }

    @Override
    public void endVisit(SQLIdentifierExpr identifierExpr) {
        if (identifier != null) {
            identifierExpr.setName(identifier);
            identifier = null;
        } else {
            identifierExpr.setName(new BackticksUnquoter().unquoteFullColumn(identifierExpr.getName()));
        }
    }

    @Override
    public void endVisit(SQLExprTableSource tableSource) {
        tableSource.setAlias(new BackticksUnquoter().unquoteSingleField(tableSource.getAlias()));
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
