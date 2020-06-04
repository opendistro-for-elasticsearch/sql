/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.domain;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.operator.SetOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Product;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider.COLUMN_DEFAULT_TYPE;
import static org.junit.Assert.assertEquals;

public class ColumnTypeProviderTest {
    @Test
    public void singleESDataTypeShouldReturnCorrectSchemaType() {
        assertEquals(Schema.Type.LONG, new ColumnTypeProvider(ESDataType.LONG).get(0));
    }

    @Test
    public void productTypeShouldReturnCorrectSchemaType() {
        ColumnTypeProvider columnTypeProvider =
                new ColumnTypeProvider(new Product(ImmutableList.of(ESDataType.LONG, ESDataType.SHORT)));
        assertEquals(Schema.Type.LONG, columnTypeProvider.get(0));
        assertEquals(Schema.Type.SHORT, columnTypeProvider.get(1));
    }

    @Test
    public void unSupportedTypeShouldReturnDefaultSchemaType() {
        ColumnTypeProvider columnTypeProvider = new ColumnTypeProvider(SetOperator.UNION);
        assertEquals(COLUMN_DEFAULT_TYPE, columnTypeProvider.get(0));
    }

    @Test
    public void providerWithoutColumnTypeShouldReturnDefaultSchemaType() {
        ColumnTypeProvider columnTypeProvider = new ColumnTypeProvider();
        assertEquals(COLUMN_DEFAULT_TYPE, columnTypeProvider.get(0));
    }
}
