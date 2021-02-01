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

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Content represent data.
 */
public interface Content {

  boolean isNull();

  boolean isNumber();

  boolean isString();

  Integer intValue();

  Long longValue();

  Short shortValue();

  Byte byteValue();

  Float floatValue();

  Double doubleValue();

  String stringValue();

  Boolean booleanValue();

  Map<String, Content> map();

  Iterator<? extends Content> array();

  Pair<Double, Double> geoValue();

  // Todo
  Object objectValue();
}
