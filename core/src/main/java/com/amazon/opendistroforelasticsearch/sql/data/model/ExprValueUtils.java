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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.annotations.VisibleForTesting;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * The definition of {@link ExprValue} factory.
 */
@UtilityClass
public class ExprValueUtils {
  public static final ExprValue LITERAL_TRUE = ExprBooleanValue.of(true);
  public static final ExprValue LITERAL_FALSE = ExprBooleanValue.of(false);
  public static final ExprValue LITERAL_NULL = ExprNullValue.of();
  public static final ExprValue LITERAL_MISSING = ExprMissingValue.of();

  public static ExprValue booleanValue(Boolean value) {
    return value ? LITERAL_TRUE : LITERAL_FALSE;
  }

  public static ExprValue integerValue(Integer value) {
    return new ExprIntegerValue(value);
  }

  public static ExprValue doubleValue(Double value) {
    return new ExprDoubleValue(value);
  }

  public static ExprValue floatValue(Float value) {
    return new ExprFloatValue(value);
  }

  public static ExprValue longValue(Long value) {
    return new ExprLongValue(value);
  }

  public static ExprValue stringValue(String value) {
    return new ExprStringValue(value);
  }

  /**
   * {@link ExprTupleValue} constructor.
   */
  public static ExprValue tupleValue(Map<String, Object> map) {
    LinkedHashMap<String, ExprValue> valueMap = new LinkedHashMap<>();
    map.forEach((k, v) -> valueMap.put(k, fromObjectValue(v)));
    return new ExprTupleValue(valueMap);
  }

  /**
   * {@link ExprCollectionValue} constructor.
   */
  public static ExprValue collectionValue(List<Object> list) {
    List<ExprValue> valueList = new ArrayList<>();
    list.forEach(o -> valueList.add(fromObjectValue(o)));
    return new ExprCollectionValue(valueList);
  }

  public static ExprValue missingValue() {
    return ExprMissingValue.of();
  }

  public static ExprValue nullValue() {
    return ExprNullValue.of();
  }

  /**
   * Construct ExprValue from Object.
   */
  public static ExprValue fromObjectValue(Object o) {
    if (null == o) {
      return LITERAL_NULL;
    }
    if (o instanceof Map) {
      return tupleValue((Map) o);
    } else if (o instanceof List) {
      return collectionValue(((List) o));
    } else if (o instanceof Integer) {
      return integerValue((Integer) o);
    } else if (o instanceof Long) {
      return longValue(((Long) o));
    } else if (o instanceof Boolean) {
      return booleanValue((Boolean) o);
    } else if (o instanceof Double) {
      return doubleValue((Double) o);
    } else if (o instanceof String) {
      return stringValue((String) o);
    } else if (o instanceof Float) {
      return floatValue((Float) o);
    } else {
      throw new ExpressionEvaluationException("unsupported object " + o.getClass());
    }
  }

  /**
   * Construct ExprValue from Object with ExprCoreType.
   */
  public static ExprValue fromObjectValue(Object o, ExprCoreType type) {
    switch (type) {
      case TIMESTAMP:
        return new ExprTimestampValue((String)o);
      case DATE:
        return new ExprDateValue((String)o);
      case TIME:
        return new ExprTimeValue((String)o);
      default:
        return fromObjectValue(o);
    }
  }

  public static Integer getIntegerValue(ExprValue exprValue) {
    return exprValue.integerValue();
  }

  public static Double getDoubleValue(ExprValue exprValue) {
    return exprValue.doubleValue();
  }

  public static Long getLongValue(ExprValue exprValue) {
    return exprValue.longValue();
  }

  public static Float getFloatValue(ExprValue exprValue) {
    return exprValue.floatValue();
  }

  public static String getStringValue(ExprValue exprValue) {
    return exprValue.stringValue();
  }

  public static List<ExprValue> getCollectionValue(ExprValue exprValue) {
    return exprValue.collectionValue();
  }

  public static Map<String, ExprValue> getTupleValue(ExprValue exprValue) {
    return exprValue.tupleValue();
  }

  public static Boolean getBooleanValue(ExprValue exprValue) {
    return exprValue.booleanValue();
  }

  /**
   * Get {@link ZonedDateTime} from ExprValue of Date type.
   */
  public static ZonedDateTime getDateValue(ExprValue exprValue) {
    return exprValue.dateValue();
  }
}
