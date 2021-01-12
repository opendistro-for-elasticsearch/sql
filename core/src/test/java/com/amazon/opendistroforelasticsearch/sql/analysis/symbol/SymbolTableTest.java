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

package com.amazon.opendistroforelasticsearch.sql.analysis.symbol;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


public class SymbolTableTest {

  private SymbolTable symbolTable;

  @BeforeEach
  public void setup() {
    symbolTable = new SymbolTable();
  }

  @Test
  public void defineFieldSymbolShouldBeAbleToResolve() {
    defineSymbolShouldBeAbleToResolve(new Symbol(Namespace.FIELD_NAME, "age"), INTEGER);
  }

  @Test
  public void removeSymbolCannotBeResolve() {
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "age"), INTEGER);

    Optional<ExprType> age = symbolTable.lookup(new Symbol(Namespace.FIELD_NAME, "age"));
    assertTrue(age.isPresent());

    symbolTable.remove(new Symbol(Namespace.FIELD_NAME, "age"));
    age = symbolTable.lookup(new Symbol(Namespace.FIELD_NAME, "age"));
    assertFalse(age.isPresent());
  }

  @Test
  public void defineFieldSymbolShouldBeAbleToResolveByPrefix() {
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "s.projects.active"), BOOLEAN);
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "s.address"), STRING);
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "s.manager.name"), STRING);

    Map<String, ExprType> typeByName =
        symbolTable.lookupByPrefix(new Symbol(Namespace.FIELD_NAME, "s.projects"));

    assertThat(
        typeByName,
        allOf(
            aMapWithSize(1),
            hasEntry("s.projects.active", BOOLEAN)
        )
    );
  }

  @Test
  public void lookupAllFieldsReturnUnnestedFields() {
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "active"), BOOLEAN);
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "active.manager"), STRING);
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "active.manager.name"), STRING);
    symbolTable.store(new Symbol(Namespace.FIELD_NAME, "s.address"), BOOLEAN);

    Map<String, ExprType> typeByName =
        symbolTable.lookupAllFields(Namespace.FIELD_NAME);

    assertThat(
        typeByName,
        allOf(
            aMapWithSize(2),
            hasEntry("active", BOOLEAN),
            hasEntry("s.address", BOOLEAN)
        )
    );
  }

  @Test
  public void failedToResolveSymbolNoNamespaceMatched() {
    symbolTable.store(new Symbol(Namespace.FUNCTION_NAME, "customFunction"), BOOLEAN);
    assertFalse(symbolTable.lookup(new Symbol(Namespace.FIELD_NAME, "s.projects")).isPresent());

    assertThat(symbolTable.lookupByPrefix(new Symbol(Namespace.FIELD_NAME, "s.projects")),
        anEmptyMap());
  }

  @Test
  public void isEmpty() {
    symbolTable.store(new Symbol(Namespace.FUNCTION_NAME, "customFunction"), BOOLEAN);
    assertTrue(symbolTable.isEmpty(Namespace.FIELD_NAME));
  }

  private void defineSymbolShouldBeAbleToResolve(Symbol symbol, ExprType expectedType) {
    symbolTable.store(symbol, expectedType);

    Optional<ExprType> actualType = symbolTable.lookup(symbol);
    assertTrue(actualType.isPresent());
    assertEquals(expectedType, actualType.get());
  }

}