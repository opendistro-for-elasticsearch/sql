/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.jdbc.types;

import java.sql.SQLException;
import java.util.Map;

public class BinaryType implements TypeHelper<String> {

  public static final BinaryType INSTANCE = new BinaryType();

  private BinaryType() {

  }

  @Override
  public String fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
    if (value == null)
      return null;
    else
      return String.valueOf(value);
  }

  @Override
  public String getTypeName() {
    return "Binary";
  }
}
