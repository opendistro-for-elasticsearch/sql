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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.multi;

import com.alibaba.druid.sql.ast.statement.SQLUnionOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;

/**
 * Created by Eliran on 19/8/2016.
 */
public class MultiQuerySelect {
    private SQLUnionOperator operation;
    private Select firstSelect;
    private Select secondSelect;

    public MultiQuerySelect(SQLUnionOperator operation, Select firstSelect, Select secondSelect) {
        this.operation = operation;
        this.firstSelect = firstSelect;
        this.secondSelect = secondSelect;
    }

    public SQLUnionOperator getOperation() {
        return operation;
    }

    public Select getFirstSelect() {
        return firstSelect;
    }

    public Select getSecondSelect() {
        return secondSelect;
    }
}
