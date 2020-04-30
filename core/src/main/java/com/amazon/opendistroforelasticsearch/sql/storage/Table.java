/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.storage;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;

/**
 * Table
 */
public interface Table {

    Table NONE = new Table() {
        @Override
        public Metadata getMetaData() {
            throw new UnsupportedOperationException();
        }

        @Override
        public PhysicalPlan find(LogicalPlan plan) {
            throw new UnsupportedOperationException();
        }
    };


    /**********************************
     *          Catalog
     **********************************/

    Metadata getMetaData();

    // void create(Metadata m);
    // void drop();


    /**********************************
     *          Access methods
     **********************************/

    PhysicalPlan find(LogicalPlan plan);

    // Create/drop table
    // Insert/delete/update data
    //  to support other DML, DDL and materialization

}
