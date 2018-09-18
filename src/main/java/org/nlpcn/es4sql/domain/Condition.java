package org.nlpcn.es4sql.domain;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.parse.ChildrenType;
import org.nlpcn.es4sql.parse.NestedType;

/**
 * 过滤条件
 *
 * @author ansj
 */
public class Condition extends Where {

    public enum OPEAR {
        EQ, GT, LT, GTE, LTE, N, LIKE, NLIKE, REGEXP, IS, ISN, IN, NIN, BETWEEN, NBETWEEN, GEO_INTERSECTS, GEO_BOUNDING_BOX, GEO_DISTANCE, GEO_POLYGON, IN_TERMS, TERM, IDS_QUERY, NESTED_COMPLEX, CHILDREN_COMPLEX, SCRIPT,NIN_TERMS,NTERM;

        public static Map<String, OPEAR> methodNameToOpear;

        public static Map<String, OPEAR> operStringToOpear;

        private static BiMap<OPEAR, OPEAR> negatives;

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
        }

        public OPEAR negative() throws SqlParseException {
            OPEAR negative = negatives.get(this);
            negative = negative != null ? negative : negatives.inverse().get(this);
            if (negative == null) {
                throw new SqlParseException("OPEAR negative not supported: " + this);
            }
            return negative;
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

    private OPEAR opear;

    private Object relationshipType;

    private boolean isNested;
    private String nestedPath;

    private boolean isChildren;
    private String childType;

    public Condition(CONN conn, String field, SQLExpr nameExpr, String condition, Object obj, SQLExpr valueExpr) throws SqlParseException {
        this(conn, field, nameExpr, condition, obj, valueExpr, null);
    }

    public Condition(CONN conn, String field, SQLExpr nameExpr, OPEAR condition, Object obj, SQLExpr valueExpr) throws SqlParseException {
        this(conn, field, nameExpr, condition, obj, valueExpr, null);
    }

    public Condition(CONN conn, String name, SQLExpr nameExpr, String oper, Object value, SQLExpr valueExpr, Object relationshipType) throws
            SqlParseException {
        super(conn);

        this.opear = null;
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

        if (OPEAR.operStringToOpear.containsKey(oper))
            this.opear = OPEAR.operStringToOpear.get(oper);
        else
            throw new SqlParseException(oper + " is not a supported operation");
    }


    public Condition(CONN conn,
                     String name,
                     SQLExpr nameExpr,
                     OPEAR oper,
                     Object value,
                     SQLExpr valueExpr,
                     Object relationshipType
    ) throws SqlParseException {
        super(conn);

        this.opear = null;
        this.nameExpr = nameExpr;
        this.valueExpr = valueExpr;
        this.name = name;
        this.value = value;
        this.opear = oper;
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
        switch (opear) {
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
                throw new SqlParseException(opear + " is err!");
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

    public OPEAR getOpear() {
        return opear;
    }

    public void setOpear(OPEAR opear) {
        this.opear = opear;
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
            result += this.conn + " " + this.name + " " + this.opear + " " + Arrays.toString((Object[]) value);
        } else {
            result += this.conn + " " + this.name + " " + this.opear + " " + this.value;
        }

        return result;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Condition clonedCondition = new Condition(this.getConn(), this.getName(),this.getNameExpr(), this.getOpear(), this.getValue(),this.getValueExpr(), this.getRelationshipType());
            return clonedCondition;
        } catch (SqlParseException e) {

        }
        return null;
    }
}
