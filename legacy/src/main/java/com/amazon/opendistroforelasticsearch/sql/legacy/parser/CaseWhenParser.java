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

package com.amazon.opendistroforelasticsearch.sql.legacy.parser;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCaseExpr;
import com.alibaba.druid.sql.ast.expr.SQLNullExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Condition;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by allwefantasy on 9/3/16.
 */
public class CaseWhenParser {

    private SQLCaseExpr caseExpr;
    private String alias;
    private String tableAlias;

    public CaseWhenParser(SQLCaseExpr caseExpr, String alias, String tableAlias) {
        this.alias = alias;
        this.tableAlias = tableAlias;
        this.caseExpr = caseExpr;
    }

    public String parse() throws SqlParseException {
        List<String> result = new ArrayList<>();

        if (caseExpr.getValueExpr() != null) {
            for (SQLCaseExpr.Item item : caseExpr.getItems()) {
                SQLExpr left = caseExpr.getValueExpr();
                SQLExpr right = item.getConditionExpr();
                SQLBinaryOpExpr conditionExpr = new SQLBinaryOpExpr(left, SQLBinaryOperator.Equality, right);
                item.setConditionExpr(conditionExpr);
            }
            caseExpr.setValueExpr(null);
        }

        for (SQLCaseExpr.Item item : caseExpr.getItems()) {
            SQLExpr conditionExpr = item.getConditionExpr();

            WhereParser parser = new WhereParser(new SqlParser(), conditionExpr);
            String scriptCode = explain(parser.findWhere());
            if (scriptCode.startsWith(" &&")) {
                scriptCode = scriptCode.substring(3);
            }
            if (result.size() == 0) {
                result.add("if(" + scriptCode + ")" + "{" + Util.getScriptValueWithQuote(item.getValueExpr(),
                        "'") + "}");
            } else {
                result.add("else if(" + scriptCode + ")" + "{" + Util.getScriptValueWithQuote(item.getValueExpr(),
                        "'") + "}");
            }

        }
        SQLExpr elseExpr = caseExpr.getElseExpr();
        if (elseExpr == null) {
            result.add("else { null }");
        } else {
            result.add("else {" + Util.getScriptValueWithQuote(elseExpr, "'") + "}");
        }


        return Joiner.on(" ").join(result);
    }

    public String explain(Where where) throws SqlParseException {
        List<String> codes = new ArrayList<>();
        while (where.getWheres().size() == 1) {
            where = where.getWheres().getFirst();
        }
        explainWhere(codes, where);
        String relation = where.getConn().name().equals("AND") ? " && " : " || ";
        return Joiner.on(relation).join(codes);
    }


    private void explainWhere(List<String> codes, Where where) throws SqlParseException {
        if (where instanceof Condition) {
            Condition condition = (Condition) where;

            if (condition.getValue() instanceof ScriptFilter) {
                codes.add("(" + ((ScriptFilter) condition.getValue()).getScript() + ")");
            } else if (condition.getOPERATOR() == Condition.OPERATOR.BETWEEN) {
                Object[] objs = (Object[]) condition.getValue();
                codes.add("(" + "doc['" + condition.getName() + "'].value >= " + objs[0] + " && doc['"
                        + condition.getName() + "'].value <=" + objs[1] + ")");
            } else {
                SQLExpr nameExpr = condition.getNameExpr();
                SQLExpr valueExpr = condition.getValueExpr();
                if (valueExpr instanceof SQLNullExpr) {
                    codes.add("(" + "doc['" + nameExpr.toString() + "']" + ".empty)");
                } else {
                    codes.add("(" + Util.getScriptValueWithQuote(nameExpr, "'") + condition.getOpertatorSymbol()
                            + Util.getScriptValueWithQuote(valueExpr, "'") + ")");
                }
            }
        } else {
            for (Where subWhere : where.getWheres()) {
                List<String> subCodes = new ArrayList<>();
                explainWhere(subCodes, subWhere);
                String relation = subWhere.getConn().name().equals("AND") ? "&&" : "||";
                codes.add(Joiner.on(relation).join(subCodes));
            }
        }
    }

}
