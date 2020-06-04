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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope.Namespace;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope.Symbol;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.visitor.ESMappingLoader;
import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.OBJECT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.UNKNOWN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex.IndexType.INDEX;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex.IndexType.NESTED_FIELD;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Semantic analysis test cases focused on basic scope building logic which is the cornerstone of analysis followed.
 * The low abstraction here enumerating all present field names in each test case is intentional for better demonstration.
 */
public class SemanticAnalyzerBasicTest extends SemanticAnalyzerTestBase {

    private SemanticContext context;

    private ESMappingLoader analyzer;

    @Before
    public void setUp() {
        context = new SemanticContext();
        analyzer = new ESMappingLoader(context, LocalClusterState.state(), 1000);
    }

    @Test
    public void contextShouldIncludeAllFieldsAfterVisitingIndexNameInFromClause() {
        analyzer.visitIndexName("semantics");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(21),
                hasEntry("semantics", (Type) new ESIndex("semantics", INDEX)),
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG)
            )
        );

        analyzer.visitAs("", new ESIndex("semantics", INDEX));
        typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
                hasEntry("semantics", (Type) new ESIndex("semantics", INDEX)),
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are also valid identifier in SQL
                hasEntry("semantics.address", TEXT),
                hasEntry("semantics.age", INTEGER),
                hasEntry("semantics.balance", DOUBLE),
                hasEntry("semantics.city", KEYWORD),
                hasEntry("semantics.birthday", DATE),
                hasEntry("semantics.location", GEO_POINT),
                hasEntry("semantics.new_field", UNKNOWN),
                hasEntry("semantics.field with spaces", TEXT),
                hasEntry("semantics.employer", TEXT),
                hasEntry("semantics.employer.keyword", KEYWORD),
                hasEntry("semantics.projects", (Type) new ESIndex("semantics.projects", NESTED_FIELD)),
                hasEntry("semantics.projects.active", BOOLEAN),
                hasEntry("semantics.projects.release", DATE),
                hasEntry("semantics.projects.members", (Type) new ESIndex("semantics.projects.members", NESTED_FIELD)),
                hasEntry("semantics.projects.members.name", TEXT),
                hasEntry("semantics.manager", OBJECT),
                hasEntry("semantics.manager.name", TEXT),
                hasEntry("semantics.manager.name.keyword", KEYWORD),
                hasEntry("semantics.manager.address", KEYWORD),
                hasEntry("semantics.manager.salary", LONG)
            )
        );
    }

    @Test
    public void contextShouldIncludeAllFieldsPrefixedByIndexAliasAfterVisitingIndexNameWithAliasInFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG)
            )
        );
    }

    @Test
    public void contextShouldIncludeSameFieldsAfterVisitingNestedFieldWithoutAliasInFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);
        analyzer.visitIndexName("s.projects");
        analyzer.visitAs("", new ESIndex("s.projects", NESTED_FIELD));

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingNestedFieldWithAliasInFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);
        analyzer.visitIndexName("s.projects");
        analyzer.visitAs("p", new ESIndex("s.projects", NESTED_FIELD));

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(46),
                // These are also valid because alias is optional in SQL
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("p.members.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingDeepNestedFieldWithAliasInFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);
        analyzer.visitIndexName("s.projects.members");
        analyzer.visitAs("m", new ESIndex("s.projects.members", NESTED_FIELD));

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);

        assertThat(
            typeByName,
            allOf(
                aMapWithSize(43),
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of deep nested field alias specified
                hasEntry("m", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("m.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingAllNestedFieldsWithAliasInFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);
        analyzer.visitIndexName("s.projects");
        analyzer.visitAs("p", new ESIndex("s.projects", NESTED_FIELD));
        analyzer.visitIndexName("s.projects.members");
        analyzer.visitAs("m", new ESIndex("s.projects.members", NESTED_FIELD));

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(48),
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("p.members.name", TEXT),
                // Valid because of deep nested field alias specified
                hasEntry("m", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("m.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingNestedFieldWithAliasInSubqueryFromClause() {
        ESIndex indexType = new ESIndex("semantics", INDEX);
        analyzer.visitIndexName("semantics");
        analyzer.visitAs("s", indexType);

        context.push();
        analyzer.visitIndexName("s.projects");
        analyzer.visitAs("p", new ESIndex("s.projects", NESTED_FIELD));

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(46),
                // These are also valid because alias is optional in SQL
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("p.members.name", TEXT)
            )
        );

        context.pop();
        typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
                hasEntry("semantics", (Type) indexType),
                // These are also valid because alias is optional in SQL
                hasEntry("address", TEXT),
                hasEntry("age", INTEGER),
                hasEntry("balance", DOUBLE),
                hasEntry("city", KEYWORD),
                hasEntry("birthday", DATE),
                hasEntry("location", GEO_POINT),
                hasEntry("new_field", UNKNOWN),
                hasEntry("field with spaces", TEXT),
                hasEntry("employer", TEXT),
                hasEntry("employer.keyword", KEYWORD),
                hasEntry("projects", (Type) new ESIndex("projects", NESTED_FIELD)),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", (Type) new ESIndex("projects.members", NESTED_FIELD)),
                hasEntry("projects.members.name", TEXT),
                hasEntry("manager", OBJECT),
                hasEntry("manager.name", TEXT),
                hasEntry("manager.name.keyword", KEYWORD),
                hasEntry("manager.address", KEYWORD),
                hasEntry("manager.salary", LONG),
                // These are valid because of alias specified
                hasEntry("s.address", TEXT),
                hasEntry("s.age", INTEGER),
                hasEntry("s.balance", DOUBLE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.birthday", DATE),
                hasEntry("s.location", GEO_POINT),
                hasEntry("s.new_field", UNKNOWN),
                hasEntry("s.field with spaces", TEXT),
                hasEntry("s.employer", TEXT),
                hasEntry("s.employer.keyword", KEYWORD),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", (Type) new ESIndex("s.projects.members", NESTED_FIELD)),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG)
            )
        );
    }

    @Test
    public void fieldWithUnknownEsTypeShouldPass() {
        analyzer.visitIndexName("semantics");
        Optional<Type> type = context.peek().resolve(new Symbol(Namespace.FIELD_NAME, "new_field"));
        Assert.assertTrue(type.isPresent());
        Assert.assertSame(UNKNOWN, type.get());
    }

    @Test
    public void fieldWithSpacesInNameShouldPass() {
        analyzer.visitIndexName("semantics");
        Optional<Type> type = context.peek().resolve(new Symbol(Namespace.FIELD_NAME, "field with spaces"));
        Assert.assertTrue(type.isPresent());
        Assert.assertSame(TEXT, type.get());
    }

}
