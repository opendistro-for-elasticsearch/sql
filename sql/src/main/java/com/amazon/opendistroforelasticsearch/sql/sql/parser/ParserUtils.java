/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

/**
 * Parser Utils Class.
 */
@UtilityClass
public class ParserUtils {

  /**
   * Get original text in query.
   */
  public static String getTextInQuery(ParserRuleContext ctx, String queryString) {
    Token start = ctx.getStart();
    Token stop = ctx.getStop();
    return queryString.substring(start.getStartIndex(), stop.getStopIndex() + 1);
  }
}
