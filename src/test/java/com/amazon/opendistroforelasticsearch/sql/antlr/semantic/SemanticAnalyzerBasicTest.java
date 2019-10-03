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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.Namespace;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor.TypeChecker;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.NESTED;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.OBJECT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.BaseType.UNKNOWN;
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

    private TypeChecker analyzer;

    @Before
    public void setUp() {
        context = new SemanticContext(LocalClusterState.state());
        analyzer = new TypeChecker(context);
    }

    @Test
    public void contextShouldIncludeAllFieldsAfterVisitingIndexNameInFromClause() {
        analyzer.visitIndexName("semantics", "");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("semantics.projects", NESTED),
                hasEntry("semantics.projects.active", BOOLEAN),
                hasEntry("semantics.projects.release", DATE),
                hasEntry("semantics.projects.members", NESTED),
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
        analyzer.visitIndexName("semantics", "s");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
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
        analyzer.visitIndexName("semantics", "s"); // This should be required
        analyzer.visitNestedIndexName("s.projects", "");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(41),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
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
        analyzer.visitIndexName("semantics", "s");
        analyzer.visitNestedIndexName("s.projects", "p");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(46),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", NESTED),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", NESTED),
                hasEntry("p.members.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingDeepNestedFieldWithAliasInFromClause() {
        analyzer.visitIndexName("semantics", "s");
        analyzer.visitNestedIndexName("s.projects.members", "m");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(43),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of deep nested field alias specified
                hasEntry("m", NESTED),
                hasEntry("m.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingAllNestedFieldsWithAliasInFromClause() {
        analyzer.visitIndexName("semantics", "s");
        analyzer.visitNestedIndexName("s.projects", "p");
        analyzer.visitNestedIndexName("s.projects.members", "m");

        Map<String, Type> typeByName = context.peek().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(48),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", NESTED),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", NESTED),
                hasEntry("p.members.name", TEXT),
                // Valid because of deep nested field alias specified
                hasEntry("m", NESTED),
                hasEntry("m.name", TEXT)
            )
        );
    }

    @Test
    public void contextShouldIncludeMoreFieldsPrefixedByNestedFieldAliasAfterVisitingNestedFieldWithAliasInSubqueryFromClause() {
        analyzer.visitIndexName("semantics", "s");
        analyzer.visitQuery();
        analyzer.visitNestedIndexName("s.projects", "p");

        assertThat(
            context.peek().resolveAll(Namespace.FIELD_NAME),
            allOf(
                aMapWithSize(46),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
                hasEntry("s.projects.members.name", TEXT),
                hasEntry("s.manager", OBJECT),
                hasEntry("s.manager.name", TEXT),
                hasEntry("s.manager.name.keyword", KEYWORD),
                hasEntry("s.manager.address", KEYWORD),
                hasEntry("s.manager.salary", LONG),
                // Valid because of nested field alias specified
                hasEntry("p", NESTED),
                hasEntry("p.active", BOOLEAN),
                hasEntry("p.release", DATE),
                hasEntry("p.members", NESTED),
                hasEntry("p.members.name", TEXT)
            )
        );

        analyzer.endVisitQuery();
        assertThat(
            context.peek().resolveAll(Namespace.FIELD_NAME),
            allOf(
                aMapWithSize(41),
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
                hasEntry("projects", NESTED),
                hasEntry("projects.active", BOOLEAN),
                hasEntry("projects.release", DATE),
                hasEntry("projects.members", NESTED),
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
                hasEntry("s.projects", NESTED),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.members", NESTED),
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
        analyzer.visitIndexName("semantics", "");
        Assert.assertEquals(UNKNOWN, analyzer.visitFieldName("new_field"));
    }

    @Test
    public void fieldWithSpacesInNameShouldPass() {
        analyzer.visitIndexName("semantics", "");
        Assert.assertEquals(TEXT, analyzer.visitFieldName("field with spaces"));
    }

}
