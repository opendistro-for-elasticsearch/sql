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

package com.amazon.opendistroforelasticsearch.sql.utils;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.rewriter.identifier.RemoveSensitiveDataRule;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.utils.Util.toSqlExpr;

/**
 * Utility class for mask sensitive info in incoming sql queries
 */
public class QueryDataMask {

    /**
     * This method is used to mask sensitive data in sql query.
     * Sensitive data includes index names, column names etc.,
     * which in druid parser are parsed to SQLIdentifierExpr instances
     * @param query entire sql query string
     * @return sql query string with all identifiers replaced with "***"
     */
    public static String maskData(String query) {
        return rebuildQuery(getQueryComponents(query), getIdentifiers(query));
    }

    private static Set<String> getIdentifiers(String query) {
        RemoveSensitiveDataRule rule = new RemoveSensitiveDataRule();
        SQLQueryExpr sqlExpr = (SQLQueryExpr) toSqlExpr(query);
        rule.rewrite(sqlExpr);
        return rule.getIdentifierSet();
    }

    private static String[] getQueryComponents(String query) {
        return replaceQuotedIdentifiers(query).trim().split("\\s");
    }

    private static String rebuildQuery(String[] components, Set<String> identifiers) {

        for (int i = 0; i < components.length; i ++) {
            if (identifiers.contains(components[i])| components[i].equals("identifier")) {
                components[i] = "***";
            }
        }
        return String.join(" ", components);
    }

    /**
     * This method is applied to replace quoted identifiers
     * since quoted identifiers with spaces are tricky to deal with
     * @param query sql query string
     * @return all quoted identifiers are replaced by word "identifier"
     */
    private static String replaceQuotedIdentifiers(String query) {
        return query.replaceAll("'[^']*'|`[^`]*`", "identifier");
    }
}
