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

package com.amazon.opendistroforelasticsearch.sql.legacy.utils;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.identifier.AnonymizeSensitiveDataRule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util.toSqlExpr;

/**
 * Utility class to mask sensitive information in incoming SQL queries
 */
public class QueryDataAnonymizer {

    private static final Logger LOG = LogManager.getLogger(QueryDataAnonymizer.class);

    /**
     * This method is used to anonymize sensitive data in SQL query.
     * Sensitive data includes index names, column names etc.,
     * which in druid parser are parsed to SQLIdentifierExpr instances
     * @param query entire sql query string
     * @return sql query string with all identifiers replaced with "***"
     */
    public static String anonymizeData(String query) {
        String resultQuery;
        try {
            AnonymizeSensitiveDataRule rule = new AnonymizeSensitiveDataRule();
            SQLQueryExpr sqlExpr = (SQLQueryExpr) toSqlExpr(query);
            rule.rewrite(sqlExpr);
            resultQuery = SQLUtils.toMySqlString(sqlExpr).replaceAll("0", "number")
                    .replaceAll("false", "boolean_literal")
                    .replaceAll("[\\n][\\t]+", " ");
        } catch (Exception e) {
            LOG.warn("Caught an exception when anonymizing sensitive data");
            resultQuery = query;
        }
        return resultQuery;
    }
}
