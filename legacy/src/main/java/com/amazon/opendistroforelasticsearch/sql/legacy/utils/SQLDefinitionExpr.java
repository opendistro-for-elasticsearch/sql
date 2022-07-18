package com.amazon.opendistroforelasticsearch.sql.legacy.utils;

import com.alibaba.druid.sql.ast.SQLExprImpl;
import com.alibaba.druid.sql.ast.expr.SQLValuableExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;

public class SQLDefinitionExpr extends SQLExprImpl implements SQLValuableExpr {

    private final String name;

    private final String definition;

    public SQLDefinitionExpr(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    public String getName() {
        return name;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public Object getValue() {
        return this.getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SQLDefinitionExpr other = (SQLDefinitionExpr) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (definition == null) {
            if (other.definition != null) {
                return false;
            }
        } else if (!definition.equals(other.definition)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((definition == null) ? 0 : definition.hashCode());
        return result;
    }

    @Override
    protected void accept0(SQLASTVisitor visitor) {

    }
}
