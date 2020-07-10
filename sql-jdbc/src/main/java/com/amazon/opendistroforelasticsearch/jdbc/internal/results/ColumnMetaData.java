/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.internal.results;

import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.ColumnDescriptor;

public class ColumnMetaData {
    private String name;
    private String label;
    private String tableSchemaName;
    private int precision = -1;
    private int scale = -1;
    private String tableName;
    private String catalogName;
    private String esTypeName;
    private ElasticsearchType esType;

    public ColumnMetaData(ColumnDescriptor descriptor) {
        this.name = descriptor.getName();

        // if a label isn't specified, the name is the label
        this.label = descriptor.getLabel() == null ? this.name : descriptor.getLabel();

        this.esTypeName = descriptor.getType();
        this.esType = ElasticsearchType.fromTypeName(esTypeName);

        // use canned values until server can return this
        this.precision = this.esType.getPrecision();
        this.scale = 0;

        // JDBC has these, but our protocol does not yet convey these
        this.tableName = "";
        this.catalogName = "";
        this.tableSchemaName = "";
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public String getTableSchemaName() {
        return tableSchemaName;
    }

    public int getPrecision() {
       return  precision;
    }

    public int getScale() {
        return scale;
    }

    public String getTableName() {
        return tableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public ElasticsearchType getEsType() {
        return esType;
    }

    public String getEsTypeName() {
        return esTypeName;
    }
}
