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

import java.util.List;

/**
 * Created by Eliran on 28/8/2015.
 */
public class TableOnJoinSelect extends Select {

    private List<Field> connectedFields;
    private List<Field> selectedFields;
    private String alias;

    public TableOnJoinSelect() {
    }


    public List<Field> getConnectedFields() {
        return connectedFields;
    }

    public void setConnectedFields(List<Field> connectedFields) {
        this.connectedFields = connectedFields;
    }

    public List<Field> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<Field> selectedFields) {
        this.selectedFields = selectedFields;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
