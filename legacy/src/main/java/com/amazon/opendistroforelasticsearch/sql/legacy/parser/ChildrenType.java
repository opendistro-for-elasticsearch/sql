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
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.ast.expr.SQLTextLiteralExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;

import java.util.List;

/**
 * Created by Razma Tazz on 14/04/2016.
 */
public class ChildrenType {
    public String field;
    public String childType;
    public Where where;
    private boolean simple;

    public boolean tryFillFromExpr(SQLExpr expr) throws SqlParseException {
        if (!(expr instanceof SQLMethodInvokeExpr)) {
            return false;
        }
        SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) expr;

        String methodName = method.getMethodName();

        if (!methodName.toLowerCase().equals("children")) {
            return false;
        }

        List<SQLExpr> parameters = method.getParameters();

        if (parameters.size() != 2) {
            throw new SqlParseException(
                    "on children object only allowed 2 parameters (type, field)/(type, conditions...) ");
        }

        String type = Util.extendedToString(parameters.get(0));
        this.childType = type;

        SQLExpr secondParameter = parameters.get(1);
        if (secondParameter instanceof SQLTextLiteralExpr || secondParameter instanceof SQLIdentifierExpr
                || secondParameter instanceof SQLPropertyExpr) {
            this.field = Util.extendedToString(secondParameter);
            this.simple = true;
        } else {
            Where where = Where.newInstance();
            new WhereParser(new SqlParser()).parseWhere(secondParameter, where);
            if (where.getWheres().size() == 0) {
                throw new SqlParseException("Failed to parse filter condition");
            }
            this.where = where;
            simple = false;
        }

        return true;
    }

    public boolean isSimple() {
        return simple;
    }
}
