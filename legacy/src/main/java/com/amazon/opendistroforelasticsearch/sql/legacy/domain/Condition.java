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

package com.amazon.opendistroforelasticsearch.sql.legacy.domain;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ChildrenType;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.NestedType;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 过滤条件
 *
 * @author ansj
 */
public class Condition extends Where {

    public enum OPERATOR {

        EQ,
        GT,
        LT,
        GTE,
        LTE,
        N,
        LIKE,
        NLIKE,
        REGEXP,
        IS,
        ISN,
        IN,
        NIN,
        BETWEEN,
        NBETWEEN,
        GEO_INTERSECTS,
        GEO_BOUNDING_BOX,
        GEO_DISTANCE,
        GEO_POLYGON,
        IN_TERMS,
        TERM,
        IDS_QUERY,
        NESTED_COMPLEX,
        NOT_EXISTS_NESTED_COMPLEX,
        CHILDREN_COMPLEX,
        SCRIPT,
        NIN_TERMS,
        NTERM,
        NREGEXP;

        public static Map<String, OPERATOR> methodNameToOpear;

        public static Map<String, OPERATOR> operStringToOpear;

        public static Map<String, OPERATOR> simpleOperStringToOpear;

        private static BiMap<OPERATOR, OPERATOR> negatives;

        private static BiMap<OPERATOR, OPERATOR> simpleReverses;

        static {
            methodNameToOpear = new HashMap<>();
            methodNameToOpear.put("term", TERM);
            methodNameToOpear.put("matchterm", TERM);
            methodNameToOpear.put("match_term", TERM);
            methodNameToOpear.put("terms", IN_TERMS);
            methodNameToOpear.put("in_terms", IN_TERMS);
            methodNameToOpear.put("ids", IDS_QUERY);
            methodNameToOpear.put("ids_query", IDS_QUERY);
            methodNameToOpear.put("regexp", REGEXP);
            methodNameToOpear.put("regexp_query", REGEXP);
        }

        static {
            operStringToOpear = new HashMap<>();
            operStringToOpear.put("=", EQ);
            operStringToOpear.put(">", GT);
            operStringToOpear.put("<", LT);
            operStringToOpear.put(">=", GTE);
            operStringToOpear.put("<=", LTE);
            operStringToOpear.put("<>", N);
            operStringToOpear.put("LIKE", LIKE);
            operStringToOpear.put("NOT", N);
            operStringToOpear.put("NOT LIKE", NLIKE);
            operStringToOpear.put("IS", IS);
            operStringToOpear.put("IS NOT", ISN);
            operStringToOpear.put("IN", IN);
            operStringToOpear.put("NOT IN", NIN);
            operStringToOpear.put("BETWEEN", BETWEEN);
            operStringToOpear.put("NOT BETWEEN", NBETWEEN);
            operStringToOpear.put("GEO_INTERSECTS", GEO_INTERSECTS);
            operStringToOpear.put("GEO_BOUNDING_BOX", GEO_BOUNDING_BOX);
            operStringToOpear.put("GEO_DISTANCE", GEO_DISTANCE);
            operStringToOpear.put("GEO_POLYGON", GEO_POLYGON);
            operStringToOpear.put("NESTED", NESTED_COMPLEX);
            operStringToOpear.put("CHILDREN", CHILDREN_COMPLEX);
            operStringToOpear.put("SCRIPT", SCRIPT);
        }

        static {
            simpleOperStringToOpear = new HashMap<>();
            simpleOperStringToOpear.put("=", EQ);
            simpleOperStringToOpear.put(">", GT);
            simpleOperStringToOpear.put("<", LT);
            simpleOperStringToOpear.put(">=", GTE);
            simpleOperStringToOpear.put("<=", LTE);
            simpleOperStringToOpear.put("<>", N);
        }

        static {
            negatives = HashBiMap.create(7);
            negatives.put(EQ, N);
            negatives.put(IN_TERMS, NIN_TERMS);
            negatives.put(TERM, NTERM);
            negatives.put(GT, LTE);
            negatives.put(LT, GTE);
            negatives.put(LIKE, NLIKE);
            negatives.put(IS, ISN);
            negatives.put(IN, NIN);
            negatives.put(BETWEEN, NBETWEEN);
            negatives.put(NESTED_COMPLEX, NOT_EXISTS_NESTED_COMPLEX);
            negatives.put(REGEXP, NREGEXP);
        }

        static {
            simpleReverses = HashBiMap.create(4);
            simpleReverses.put(EQ, EQ);
            simpleReverses.put(GT, LT);
            simpleReverses.put(GTE, LTE);
            simpleReverses.put(N, N);
        }

        public OPERATOR negative() throws SqlParseException {
            OPERATOR negative = negatives.get(this);
            negative = negative != null ? negative : negatives.inverse().get(this);
            if (negative == null) {
                throw new SqlParseException(StringUtils.format("Negative operator [%s] is not supported.",
                        this.name()));
            }
            return negative;
        }

        public OPERATOR simpleReverse() throws SqlParseException {
            OPERATOR reverse = simpleReverses.get(this);
            reverse = reverse != null ? reverse : simpleReverses.inverse().get(this);
            if (reverse == null) {
                throw new SqlParseException(StringUtils.format("Simple reverse operator [%s] is not supported.",
                        this.name()));
            }
            return reverse;
        }

        public Boolean isSimpleOperator() {
            return simpleOperStringToOpear.containsValue(this);
        }
    }

    private String name;

    private SQLExpr nameExpr;

    private Object value;

    public SQLExpr getNameExpr() {
        return nameExpr;
    }

    public SQLExpr getValueExpr() {
        return valueExpr;
    }

    private SQLExpr valueExpr;

    private OPERATOR OPERATOR;

    private Object relationshipType;

    private boolean isNested;
    private String nestedPath;

    private boolean isChildren;
    private String childType;

    public Condition(CONN conn, String field, SQLExpr nameExpr, String condition, Object obj, SQLExpr valueExpr)
            throws SqlParseException {
        this(conn, field, nameExpr, condition, obj, valueExpr, null);
    }

    public Condition(CONN conn, String field, SQLExpr nameExpr, OPERATOR condition, Object obj, SQLExpr valueExpr)
            throws SqlParseException {
        this(conn, field, nameExpr, condition, obj, valueExpr, null);
    }

    public Condition(CONN conn, String name, SQLExpr nameExpr, String oper,
                     Object value, SQLExpr valueExpr, Object relationshipType) throws SqlParseException {
        super(conn);

        this.OPERATOR = null;
        this.name = name;
        this.value = value;
        this.nameExpr = nameExpr;
        this.valueExpr = valueExpr;

        this.relationshipType = relationshipType;

        if (this.relationshipType != null) {
            if (this.relationshipType instanceof NestedType) {
                NestedType nestedType = (NestedType) relationshipType;

                this.isNested = true;
                this.nestedPath = nestedType.path;
                this.isChildren = false;
                this.childType = "";
            } else if (relationshipType instanceof ChildrenType) {
                ChildrenType childrenType = (ChildrenType) relationshipType;

                this.isNested = false;
                this.nestedPath = "";
                this.isChildren = true;
                this.childType = childrenType.childType;
            }
        } else {
            this.isNested = false;
            this.nestedPath = "";
            this.isChildren = false;
            this.childType = "";
        }

        if (OPERATOR.operStringToOpear.containsKey(oper)) {
            this.OPERATOR = OPERATOR.operStringToOpear.get(oper);
        } else {
            throw new SqlParseException("Unsupported operation: " + oper);
        }
    }


    public Condition(CONN conn,
                     String name,
                     SQLExpr nameExpr,
                     OPERATOR oper,
                     Object value,
                     SQLExpr valueExpr,
                     Object relationshipType
    ) throws SqlParseException {
        super(conn);

        this.OPERATOR = null;
        this.nameExpr = nameExpr;
        this.valueExpr = valueExpr;
        this.name = name;
        this.value = value;
        this.OPERATOR = oper;
        this.relationshipType = relationshipType;

        if (this.relationshipType != null) {
            if (this.relationshipType instanceof NestedType) {
                NestedType nestedType = (NestedType) relationshipType;

                this.isNested = true;
                this.nestedPath = nestedType.path;
                this.isChildren = false;
                this.childType = "";
            } else if (relationshipType instanceof ChildrenType) {
                ChildrenType childrenType = (ChildrenType) relationshipType;

                this.isNested = false;
                this.nestedPath = "";
                this.isChildren = true;
                this.childType = childrenType.childType;
            }
        } else {
            this.isNested = false;
            this.nestedPath = "";
            this.isChildren = false;
            this.childType = "";
        }
    }

    public String getOpertatorSymbol() throws SqlParseException {
        switch (OPERATOR) {
            case EQ:
                return "==";
            case GT:
                return ">";
            case LT:
                return "<";
            case GTE:
                return ">=";
            case LTE:
                return "<=";
            case N:
                return "<>";
            case IS:
                return "==";

            case ISN:
                return "!=";
            default:
                throw new SqlParseException(StringUtils.format("Failed to parse operator [%s]", OPERATOR));
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public OPERATOR getOPERATOR() {
        return OPERATOR;
    }

    public void setOPERATOR(OPERATOR OPERATOR) {
        this.OPERATOR = OPERATOR;
    }

    public Object getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(Object relationshipType) {
        this.relationshipType = relationshipType;
    }

    public boolean isNested() {
        return isNested;
    }

    public void setNested(boolean isNested) {
        this.isNested = isNested;
    }

    public String getNestedPath() {
        return nestedPath;
    }

    public void setNestedPath(String nestedPath) {
        this.nestedPath = nestedPath;
    }

    public boolean isChildren() {
        return isChildren;
    }

    public void setChildren(boolean isChildren) {
        this.isChildren = isChildren;
    }

    public String getChildType() {
        return childType;
    }

    public void setChildType(String childType) {
        this.childType = childType;
    }

    /**
     * Return true if the opear is {@link OPERATOR#NESTED_COMPLEX}
     * For example, the opear is {@link OPERATOR#NESTED_COMPLEX} when condition is
     * nested('projects', projects.started_year > 2000 OR projects.name LIKE '%security%')
     */
    public boolean isNestedComplex() {
        return OPERATOR.NESTED_COMPLEX == OPERATOR;
    }

    @Override
    public String toString() {
        String result = "";

        if (this.isNested()) {
            result = "nested condition ";
            if (this.getNestedPath() != null) {
                result += "on path:" + this.getNestedPath() + " ";
            }
        } else if (this.isChildren()) {
            result = "children condition ";

            if (this.getChildType() != null) {
                result += "on child: " + this.getChildType() + " ";
            }
        }

        if (value instanceof Object[]) {
            result += this.conn + " " + this.name + " " + this.OPERATOR + " " + Arrays.toString((Object[]) value);
        } else {
            result += this.conn + " " + this.name + " " + this.OPERATOR + " " + this.value;
        }

        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return new Condition(this.getConn(), this.getName(), this.getNameExpr(),
                    this.getOPERATOR(), this.getValue(), this.getValueExpr(), this.getRelationshipType());
        } catch (SqlParseException e) {

        }
        return null;
    }
}
