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
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath.BucketPath;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath.Path;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;

import java.util.List;

/**
 * Created by Eliran on 12/11/2015.
 */
public class NestedType {
    public String field;
    public String path;
    public Where where;
    private boolean reverse;
    private boolean simple;
    private final BucketPath bucketPath = new BucketPath();

    public boolean tryFillFromExpr(SQLExpr expr) throws SqlParseException {
        if (!(expr instanceof SQLMethodInvokeExpr)) {
            return false;
        }
        SQLMethodInvokeExpr method = (SQLMethodInvokeExpr) expr;
        String methodNameLower = method.getMethodName().toLowerCase();
        if (!(methodNameLower.equals("nested") || methodNameLower.equals("reverse_nested"))) {
            return false;
        }

        reverse = methodNameLower.equals("reverse_nested");

        List<SQLExpr> parameters = method.getParameters();
        if (parameters.size() != 2 && parameters.size() != 1) {
            throw new IllegalArgumentException("on nested object only allowed 2 parameters "
                    + "(field,path)/(path,conditions..) or 1 parameter (field) ");
        }

        String field = Util.extendedToString(parameters.get(0));
        this.field = field;
        if (parameters.size() == 1) {
            //calc path myself..
            if (!field.contains(".")) {
                if (!reverse) {
                    throw new IllegalArgumentException("Illegal nested field name: " + field);
                } else {
                    this.path = null;
                    this.simple = true;
                }
            } else {
                int lastDot = field.lastIndexOf(".");
                this.path = field.substring(0, lastDot);
                this.simple = true;

            }

        } else if (parameters.size() == 2) {
            SQLExpr secondParameter = parameters.get(1);
            if (secondParameter instanceof SQLTextLiteralExpr || secondParameter instanceof SQLIdentifierExpr
                    || secondParameter instanceof SQLPropertyExpr) {

                String pathString = Util.extendedToString(secondParameter);
                if (pathString.equals("")) {
                    this.path = null;
                } else {
                    this.path = pathString;
                }
                this.simple = true;
            } else {
                this.path = field;
                Where where = Where.newInstance();
                new WhereParser(new SqlParser()).parseWhere(secondParameter, where);
                if (where.getWheres().size() == 0) {
                    throw new SqlParseException("Failed to parse filter condition");
                }
                this.where = where;
                simple = false;
            }
        }

        return true;
    }

    public boolean isSimple() {
        return simple;
    }

    public boolean isReverse() {
        return reverse;
    }

    /**
     * Return the name of the Nested Aggregation.
     */
    public String getNestedAggName() {
        return field + "@NESTED";
    }

    /**
     * Return the name of the Filter Aggregation
     */
    public String getFilterAggName() {
        return field + "@FILTER";
    }

    public void addBucketPath(Path path) {
        bucketPath.add(path);
    }

    public String getBucketPath() {
        return bucketPath.getBucketPath();
    }

    /**
     * Return true if the filed is the nested filed.
     * For example, the mapping
     * {
     * "projects":{
     * "type": "nested"
     * "properties": {
     * "name": {
     * "type": "text"
     * }
     * }
     * }
     * }
     * <p>
     * If the filed is projects, return true.
     * If the filed is projects.name, return false.
     */
    public boolean isNestedField() {
        return !field.contains(".") && field.equalsIgnoreCase(path);
    }
}
