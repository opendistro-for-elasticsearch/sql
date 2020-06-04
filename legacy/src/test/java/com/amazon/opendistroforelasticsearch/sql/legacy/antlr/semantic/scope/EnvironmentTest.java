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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.OBJECT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.TEXT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESIndex.IndexType.NESTED_FIELD;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

/**
 * Test cases for environment
 */
public class EnvironmentTest {

    /** Use context class for push/pop */
    private SemanticContext context = new SemanticContext();

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldBeAbleToResolve() {
        // Root environment
        Symbol birthday = new Symbol(Namespace.FIELD_NAME, "s.birthday");
        environment().define(birthday, DATE);
        Assert.assertTrue(environment().resolve(birthday).isPresent());

        // New environment 1
        context.push();
        Symbol city = new Symbol(Namespace.FIELD_NAME, "s.city");
        environment().define(city, KEYWORD);
        Assert.assertTrue(environment().resolve(birthday).isPresent());
        Assert.assertTrue(environment().resolve(city).isPresent());

        // New environment 2
        context.push();
        Symbol manager = new Symbol(Namespace.FIELD_NAME, "s.manager");
        environment().define(manager, OBJECT);
        Assert.assertTrue(environment().resolve(birthday).isPresent());
        Assert.assertTrue(environment().resolve(city).isPresent());
        Assert.assertTrue(environment().resolve(manager).isPresent());
    }

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldNotAbleToResolveOncePopped() {
        // Root environment
        Symbol birthday = new Symbol(Namespace.FIELD_NAME, "s.birthday");
        environment().define(birthday, DATE);

        // New environment
        context.push();
        Symbol city = new Symbol(Namespace.FIELD_NAME, "s.city");
        Symbol manager = new Symbol(Namespace.FIELD_NAME, "s.manager");
        environment().define(city, OBJECT);
        environment().define(manager, OBJECT);
        Assert.assertTrue(environment().resolve(birthday).isPresent());
        Assert.assertTrue(environment().resolve(city).isPresent());
        Assert.assertTrue(environment().resolve(manager).isPresent());

        context.pop();
        Assert.assertFalse(environment().resolve(city).isPresent());
        Assert.assertFalse(environment().resolve(manager).isPresent());
        Assert.assertTrue(environment().resolve(birthday).isPresent());
    }

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldBeAbleToResolveByPrefix() {
        // Root environment
        Symbol birthday = new Symbol(Namespace.FIELD_NAME, "s.birthday");
        environment().define(birthday, DATE);

        // New environment 1
        context.push();
        Symbol city = new Symbol(Namespace.FIELD_NAME, "s.city");
        environment().define(city, KEYWORD);

        // New environment 2
        context.push();
        Symbol manager = new Symbol(Namespace.FIELD_NAME, "s.manager");
        environment().define(manager, OBJECT);

        Map<String, Type> typeByName = environment().resolveByPrefix(new Symbol(Namespace.FIELD_NAME, "s"));
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(3),
                hasEntry("s.birthday", DATE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.manager", OBJECT)
            )
        );
    }

    @Test
    public void defineFieldSymbolShouldBeAbleToResolveAll() {
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.projects"), new ESIndex("s.projects", NESTED_FIELD));
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.projects.release"), DATE);
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.projects.active"), BOOLEAN);
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.address"), TEXT);
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.city"), KEYWORD);
        environment().define(new Symbol(Namespace.FIELD_NAME, "s.manager.name"), TEXT);

        Map<String, Type> typeByName = environment().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(6),
                hasEntry("s.projects", (Type) new ESIndex("s.projects", NESTED_FIELD)),
                hasEntry("s.projects.release", DATE),
                hasEntry("s.projects.active", BOOLEAN),
                hasEntry("s.address", TEXT),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.manager.name", TEXT)
            )
        );
    }

    @Test
    public void defineFieldSymbolInDifferentEnvironmentsShouldBeAbleToResolveAll() {
        // Root environment
        Symbol birthday = new Symbol(Namespace.FIELD_NAME, "s.birthday");
        environment().define(birthday, DATE);

        // New environment 1
        context.push();
        Symbol city = new Symbol(Namespace.FIELD_NAME, "s.city");
        environment().define(city, KEYWORD);

        // New environment 2
        context.push();
        Symbol manager = new Symbol(Namespace.FIELD_NAME, "s.manager");
        environment().define(manager, OBJECT);

        Map<String, Type> typeByName = environment().resolveAll(Namespace.FIELD_NAME);
        assertThat(
            typeByName,
            allOf(
                aMapWithSize(3),
                hasEntry("s.birthday", DATE),
                hasEntry("s.city", KEYWORD),
                hasEntry("s.manager", OBJECT)
            )
        );
    }

    private Environment environment() {
        return context.peek();
    }

}
