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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Field;

import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 22/8/2015.
 */
public class HashJoinElasticRequestBuilder extends JoinRequestBuilder {

    private List<List<Map.Entry<Field, Field>>> t1ToT2FieldsComparison;
    private boolean useTermFiltersOptimization;

    public HashJoinElasticRequestBuilder() {
    }

    @Override
    public String explain() {
        return "HashJoin " + super.explain();
    }

    public List<List<Map.Entry<Field, Field>>> getT1ToT2FieldsComparison() {
        return t1ToT2FieldsComparison;
    }

    public void setT1ToT2FieldsComparison(List<List<Map.Entry<Field, Field>>> t1ToT2FieldsComparison) {
        this.t1ToT2FieldsComparison = t1ToT2FieldsComparison;
    }

    public boolean isUseTermFiltersOptimization() {
        return useTermFiltersOptimization;
    }

    public void setUseTermFiltersOptimization(boolean useTermFiltersOptimization) {
        this.useTermFiltersOptimization = useTermFiltersOptimization;
    }
}
