/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.util.Arrays;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

/**
 * Analyzer that analyzes qualifier(s) in a full field name.
 */
@RequiredArgsConstructor
public class QualifierAnalyzer {

  private final AnalysisContext context;

  public String unqualified(String... parts) {
    return unqualified(QualifiedName.of(Arrays.asList(parts)));
  }

  /**
   * Get unqualified name if its qualifier symbol found is in index namespace
   * on type environment. Unqualified name means name with qualifier removed.
   * For example, unqualified name of "accounts.age" or "acc.age" is "age".
   *
   * @return  unqualified name if criteria met above, otherwise original name
   */
  public String unqualified(QualifiedName fullName) {
    return isQualifierIndexOrAlias(fullName) ? fullName.rest().toString() : fullName.toString();
  }

  private boolean isQualifierIndexOrAlias(QualifiedName fullName) {
    Optional<String> qualifier = fullName.first();
    if (qualifier.isPresent()) {
      resolveQualifierSymbol(fullName, qualifier.get());
      return true;
    }
    return false;
  }

  private void resolveQualifierSymbol(QualifiedName fullName, String qualifier) {
    try {
      context.peek().resolve(new Symbol(Namespace.INDEX_NAME, qualifier));
    } catch (SemanticCheckException e) {
      // Throw syntax check intentionally to indicate fall back to old engine.
      // Need change to semantic check exception in future.
      throw new SyntaxCheckException(String.format(
          "The qualifier [%s] of qualified name [%s] must be an index name or its alias",
              qualifier, fullName));
    }
  }

}
