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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;

/**
 * Created by Eliran on 3/10/2015.
 */
public class SubQueryExpression {
    private Object[] values;
    private Select select;
    private String returnField;

    public SubQueryExpression(Select innerSelect) {
        this.select = innerSelect;
        this.returnField = select.getFields().get(0).getName();
        values = null;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public Select getSelect() {
        return select;
    }

    public void setSelect(Select select) {
        this.select = select;
    }

    public String getReturnField() {
        return returnField;
    }
}
