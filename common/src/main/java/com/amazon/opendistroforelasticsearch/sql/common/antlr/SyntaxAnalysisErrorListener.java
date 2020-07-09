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

package com.amazon.opendistroforelasticsearch.sql.common.antlr;

import java.util.Locale;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Syntax analysis error listener that handles any syntax error by throwing exception with useful
 * information.
 */
public class SyntaxAnalysisErrorListener extends BaseErrorListener {

  @Override
  public void syntaxError(
      Recognizer<?, ?> recognizer,
      Object offendingSymbol,
      int line,
      int charPositionInLine,
      String msg,
      RecognitionException e) {

    CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
    Token offendingToken = (Token) offendingSymbol;
    String query = tokens.getText();

    throw new SyntaxCheckException(
        String.format(
            Locale.ROOT,
            "Failed to parse query due to offending symbol [%s] "
                + "at: '%s' <--- HERE... More details: %s",
            getOffendingText(offendingToken),
            truncateQueryAtOffendingToken(query, offendingToken),
            getDetails(recognizer, msg, e)));
  }

  private String getOffendingText(Token offendingToken) {
    return offendingToken.getText();
  }

  private String truncateQueryAtOffendingToken(String query, Token offendingToken) {
    return query.substring(0, offendingToken.getStopIndex() + 1);
  }

  /**
   * As official JavaDoc says, e=null means parser was able to recover from the error. In other
   * words, "msg" argument includes the information we want.
   */
  private String getDetails(Recognizer<?, ?> recognizer, String msg, RecognitionException e) {
    String details;
    if (e == null) {
      details = msg;
    } else {
      IntervalSet followSet = e.getExpectedTokens();
      details = "Expecting tokens in " + followSet.toString(recognizer.getVocabulary());
    }
    return details;
  }
}
