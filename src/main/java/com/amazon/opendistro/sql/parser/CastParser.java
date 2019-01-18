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

package com.amazon.opendistro.sql.parser;

import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.google.common.base.Joiner;
import com.amazon.opendistro.sql.utils.SQLFunctions;
import com.amazon.opendistro.sql.utils.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leonlu on 2017/9/21.
 */
public class CastParser {

    private enum DataType {
        INT, LONG, FLOAT, DOUBLE, STRING, DATETIME
    }

    private SQLCastExpr castExpr;
    private String alias;
    private String tableAlias;

    public CastParser(SQLCastExpr castExpr, String alias, String tableAlias) {
        this.castExpr = castExpr;
        this.alias = alias;
        this.tableAlias = tableAlias;
    }

    public String parse(boolean isReturn) throws SqlParseException {
        List<String> result = new ArrayList<>();

        String dataType = castExpr.getDataType().getName().toUpperCase();
        String fileName = String.format("doc['%s'].value",Util.expr2Object(castExpr.getExpr()));
        String name = SQLFunctions.randomize("field");

        try {
            if (DataType.valueOf(dataType) == DataType.INT) {
                result.add(String.format("def %s = Double.parseDouble(%s.toString()).intValue()", name, fileName));
            } else if (DataType.valueOf(dataType) == DataType.LONG) {
                result.add(String.format("def %s = Double.parseDouble(%s.toString()).longValue()", name, fileName));
            } else if (DataType.valueOf(dataType) == DataType.FLOAT) {
                result.add(String.format("def %s = Double.parseDouble(%s.toString()).floatValue()", name, fileName));
            } else if (DataType.valueOf(dataType) == DataType.DOUBLE) {
                result.add(String.format("def %s = Double.parseDouble(%s.toString()).doubleValue()", name, fileName));
            } else if (DataType.valueOf(dataType) == DataType.STRING) {
                result.add(String.format("def %s = %s.toString()",name, fileName));
            } else if (DataType.valueOf(dataType) == DataType.DATETIME) {
                result.add(String.format("def %s = new Date(Double.parseDouble(%s.toString()).longValue())", name, fileName));
            } else {
                throw new SqlParseException("not support cast to data type:" + dataType);
            }
            if(isReturn) {
                result.add("return " + name);
            }

            return Joiner.on("; ").join(result);
        } catch (Exception ex) {
            throw new SqlParseException(String.format("field cast to type: %s failed. error:%s",dataType, ex.getMessage()));
        }
    }
}
