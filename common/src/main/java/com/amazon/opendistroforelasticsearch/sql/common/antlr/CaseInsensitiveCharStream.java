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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Custom stream to convert character to upper case for case insensitive grammar before sending to
 * lexer.
 */
public class CaseInsensitiveCharStream implements CharStream {

  /** Character stream. */
  private final CharStream charStream;

  public CaseInsensitiveCharStream(String sql) {
    this.charStream = CharStreams.fromString(sql);
  }

  @Override
  public String getText(Interval interval) {
    return charStream.getText(interval);
  }

  @Override
  public void consume() {
    charStream.consume();
  }

  @Override
  public int LA(int i) {
    int c = charStream.LA(i);
    if (c <= 0) {
      return c;
    }
    return Character.toUpperCase(c);
  }

  @Override
  public int mark() {
    return charStream.mark();
  }

  @Override
  public void release(int marker) {
    charStream.release(marker);
  }

  @Override
  public int index() {
    return charStream.index();
  }

  @Override
  public void seek(int index) {
    charStream.seek(index);
  }

  @Override
  public int size() {
    return charStream.size();
  }

  @Override
  public String getSourceName() {
    return charStream.getSourceName();
  }
}
