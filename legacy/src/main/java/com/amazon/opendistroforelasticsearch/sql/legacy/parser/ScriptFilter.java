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
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.Util;
import org.elasticsearch.script.ScriptType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 11/12/2015.
 */
public class ScriptFilter {
    private String script;
    private Map<String, Object> args;
    private ScriptType scriptType;

    public ScriptFilter() {

        args = null;
        scriptType = ScriptType.INLINE;
    }

    public ScriptFilter(String script, Map<String, Object> args, ScriptType scriptType) {
        this.script = script;
        this.args = args;
        this.scriptType = scriptType;
    }

    public boolean tryParseFromMethodExpr(SQLMethodInvokeExpr expr) throws SqlParseException {
        if (!expr.getMethodName().toLowerCase().equals("script")) {
            return false;
        }
        List<SQLExpr> methodParameters = expr.getParameters();
        if (methodParameters.size() == 0) {
            return false;
        }
        script = Util.extendedToString(methodParameters.get(0));

        if (methodParameters.size() == 1) {
            return true;
        }

        args = new HashMap<>();
        for (int i = 1; i < methodParameters.size(); i++) {

            SQLExpr innerExpr = methodParameters.get(i);
            if (!(innerExpr instanceof SQLBinaryOpExpr)) {
                return false;
            }
            SQLBinaryOpExpr binaryOpExpr = (SQLBinaryOpExpr) innerExpr;
            if (!binaryOpExpr.getOperator().getName().equals("=")) {
                return false;
            }

            SQLExpr right = binaryOpExpr.getRight();
            Object value = Util.expr2Object(right);
            String key = Util.extendedToString(binaryOpExpr.getLeft());
            if (key.equals("script_type")) {
                parseAndUpdateScriptType(value.toString());
            } else {
                args.put(key, value);
            }

        }
        return true;
    }

    private void parseAndUpdateScriptType(String scriptType) {
        String scriptTypeUpper = scriptType.toUpperCase();
        switch (scriptTypeUpper) {
            case "INLINE":
                this.scriptType = ScriptType.INLINE;
                break;
            case "INDEXED":
            case "STORED":
                this.scriptType = ScriptType.STORED;
                break;
        }
    }

    public boolean containsParameters() {
        return args != null && args.size() > 0;
    }

    public String getScript() {
        return script;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public void setArgs(Map<String, Object> args) {
        this.args = args;
    }

}
