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
import com.alibaba.druid.sql.ast.expr.SQLAggregateOption;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ChildrenType;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.NestedType;

import java.util.Objects;

/**
 * 搜索域
 *
 * @author ansj
 */
public class Field implements Cloneable {

    /**
     * Constant for '*' field in SELECT
     */
    public static final Field STAR = new Field("*", "");

    protected String name;
    protected SQLAggregateOption option;
    private String alias;
    private NestedType nested;
    private ChildrenType children;
    private SQLExpr expression;

    public Field(String name, String alias) {
        this.name = name;
        this.alias = alias;
        this.nested = null;
        this.children = null;
        this.option = null;
    }

    public Field(String name, String alias, NestedType nested, ChildrenType children) {
        this.name = name;
        this.alias = alias;
        this.nested = nested;
        this.children = children;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public boolean isNested() {
        return this.nested != null;
    }

    public boolean isReverseNested() {
        return this.nested != null && this.nested.isReverse();
    }

    public void setNested(NestedType nested) {
        this.nested = nested;
    }

    public String getNestedPath() {
        if (this.nested == null) {
            return null;
        }

        return this.nested.path;
    }

    public boolean isChildren() {
        return this.children != null;
    }

    public void setChildren(ChildrenType children) {
        this.children = children;
    }

    public String getChildType() {
        if (this.children == null) {
            return null;
        }
        return this.children.childType;
    }

    public void setAggregationOption(SQLAggregateOption option) {
        this.option = option;
    }

    public SQLAggregateOption getOption() {
        return option;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        Field other = (Field) obj;
        boolean namesAreEqual = (other.getName() == null && this.name == null)
                || other.getName().equals(this.name);
        if (!namesAreEqual) {
            return false;
        }
        return (other.getAlias() == null && this.alias == null)
                || other.getAlias().equals(this.alias);
    }

    @Override
    public int hashCode() { // Bug: equals() is present but hashCode was missing
        return Objects.hash(name, alias);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new Field(new String(this.name), new String(this.alias));
    }

    /**
     * Returns true if the field is script field.
     */
    public boolean isScriptField() {
        return false;
    }

    public void setExpression(SQLExpr expression) {
        this.expression = expression;
    }

    public SQLExpr getExpression() {
        return expression;
    }
}
