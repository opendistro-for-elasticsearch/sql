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

/**
 * 排序规则
 *
 * @author ansj
 */
public class Order {
    private String nestedPath;
    private String name;
    private String type;
    private Field sortField;

    public boolean isScript() {
        return sortField != null && sortField.isScriptField();
    }

    public Order(String nestedPath, String name, String type, Field sortField) {
        this.nestedPath = nestedPath;
        this.name = name;
        this.type = type;
        this.sortField = sortField;
    }

    public String getNestedPath() {
        return nestedPath;
    }

    public void setNestedPath(String nestedPath) {
        this.nestedPath = nestedPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Field getSortField() {
        return sortField;
    }
}
