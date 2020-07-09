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

import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.hints.Hint;

import java.util.List;

/**
 * Created by Eliran on 20/8/2015.
 */
public class JoinSelect extends Query {


    private TableOnJoinSelect firstTable;
    private TableOnJoinSelect secondTable;
    private Where connectedWhere;
    private List<Hint> hints;
    private List<Condition> connectedConditions;
    private int totalLimit;

    private final int DEAFULT_NUM_OF_RESULTS = 200;

    private SQLJoinTableSource.JoinType joinType;


    public JoinSelect() {
        firstTable = new TableOnJoinSelect();
        secondTable = new TableOnJoinSelect();

        totalLimit = DEAFULT_NUM_OF_RESULTS;
    }


    public Where getConnectedWhere() {
        return connectedWhere;
    }

    public void setConnectedWhere(Where connectedWhere) {
        this.connectedWhere = connectedWhere;
    }

    public TableOnJoinSelect getFirstTable() {
        return firstTable;
    }

    public TableOnJoinSelect getSecondTable() {
        return secondTable;
    }


    public SQLJoinTableSource.JoinType getJoinType() {
        return joinType;
    }

    public void setJoinType(SQLJoinTableSource.JoinType joinType) {
        this.joinType = joinType;
    }

    public List<Hint> getHints() {
        return hints;
    }

    public void setHints(List<Hint> hints) {
        this.hints = hints;
    }

    public int getTotalLimit() {
        return totalLimit;
    }

    public List<Condition> getConnectedConditions() {
        return connectedConditions;
    }

    public void setConnectedConditions(List<Condition> connectedConditions) {
        this.connectedConditions = connectedConditions;
    }

    public void setTotalLimit(int totalLimit) {
        this.totalLimit = totalLimit;
    }
}
