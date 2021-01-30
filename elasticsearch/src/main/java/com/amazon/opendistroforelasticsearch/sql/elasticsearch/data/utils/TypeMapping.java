/*
 *     Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License").
 *     You may not use this file except in compliance with the License.
 *     A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     or in the "license" file accompanying this file. This file is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *     express or implied. See the License for the specific language governing
 *     permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import java.util.Map;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public interface TypeMapping {

  ExprType type(String field);

  TypeMapping subTypeMapping(String field);

  TypeMapping EMPTY = new TypeMapping() {
    @Override
    public ExprType type(String field) {
      throw new RuntimeException(String.format("Field:%s not exist in empty mapping"));
    }

    @Override
    public TypeMapping subTypeMapping(String field) {
      throw new RuntimeException(String.format("Field:%s doesn't have sub type mapping in empty "
          + "mapping"));
    }
  };

  class DefaultTypeMapping implements TypeMapping {

    private NavigableMap<String, ExprType> typeMapping = new TreeMap<>();

    public DefaultTypeMapping(Map<String, ExprType> typeMap) {
      typeMapping.putAll(typeMap);
    }

    @Override
    public ExprType type(String field) {
      if (typeMapping.containsKey(field)) {
        return typeMapping.get(field);
      } else {
        throw new IllegalStateException(String.format("No type found for field: %s.", field));
      }
    }

    @Override
    public TypeMapping subTypeMapping(String field) {
      String prefix = field + ".";
      final SortedMap<String, ExprType> subMap =
          typeMapping.subMap(prefix, prefix + Character.MAX_VALUE);
      if (subMap.isEmpty()) {
        return EMPTY;
      } else {
        return new DefaultTypeMapping(subMap.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().replaceAll(prefix
            , ""), Map.Entry::getValue)));
      }
    }
  }
}
