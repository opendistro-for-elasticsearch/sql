/*
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License").
 *  * You may not use this file except in compliance with the License.
 *  * A copy of the License is located at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * or in the "license" file accompanying this file. This file is distributed
 *  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  * express or implied. See the License for the specific language governing
 *  * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.rewriter.identifier;

import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;

public class RemoveSensitiveInfoRule extends MySqlASTVisitorAdapter implements RewriteRule<SQLQueryExpr> {

    @Override
    public void endVisit(SQLIdentifierExpr identifierExpr) {
        identifierExpr.setName("***");
    }

    @Override
    public void endVisit(SQLExprTableSource tableSource) {
        tableSource.setAlias("***");
    }

    @Override
    public boolean match(SQLQueryExpr expr) {
        return true;
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        expr.accept(new RemoveSensitiveInfoRule());
    }
}
