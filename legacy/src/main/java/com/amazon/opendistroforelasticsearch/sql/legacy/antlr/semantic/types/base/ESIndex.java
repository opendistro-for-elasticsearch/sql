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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;

import java.util.Objects;

/**
 * Index type is not Enum because essentially each index is a brand new type.
 */
public class ESIndex implements BaseType {

    public enum IndexType {
        INDEX, NESTED_FIELD, INDEX_PATTERN
    }

    private final String indexName;
    private final IndexType indexType;

    public ESIndex(String indexName, IndexType indexType) {
        this.indexName = indexName;
        this.indexType = indexType;
    }

    public IndexType type() {
        return indexType;
    }

    @Override
    public String getName() {
        return indexName;
    }

    @Override
    public boolean isCompatible(Type other) {
        return equals(other);
    }

    @Override
    public String usage() {
        return indexType.name();
    }

    @Override
    public String toString() {
        return indexType + " [" + indexName + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ESIndex index = (ESIndex) o;
        return Objects.equals(indexName, index.indexName)
            && indexType == index.indexType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexName, indexType);
    }
}
