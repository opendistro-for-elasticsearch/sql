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
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.amazon.opendistroforelasticsearch.sql.rewriter.RewriteRule;
import java.util.HashSet;
import java.util.Set;

public class RemoveSensitiveDataRule extends MySqlASTVisitorAdapter implements RewriteRule<SQLQueryExpr> {

    private Set<String> identifierSet = new HashSet<>();

    @Override
    public boolean visit(SQLIdentifierExpr identifierExpr) {
        identifierSet.add(identifierExpr.getName());
        return true;
    }

    @Override
    public boolean match(SQLQueryExpr expr) {
        return true;
    }

    @Override
    public void rewrite(SQLQueryExpr expr) {
        expr.accept(this);
    }

    public Set<String> getIdentifierSet() {
        return this.identifierSet;
    }
}
