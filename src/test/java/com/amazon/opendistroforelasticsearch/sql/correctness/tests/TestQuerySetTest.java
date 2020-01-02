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

package com.amazon.opendistroforelasticsearch.sql.correctness.tests;

import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestQuerySet;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Tests for {@link TestQuerySet}
 */
public class TestQuerySetTest {

    @Test
    public void testQuerySet() {
        TestQuerySet querySet = new TestQuerySet("SELECT * FROM accounts\nSELECT * FROM accounts LIMIT 5");
        assertThat(
            querySet,
            contains(
                "SELECT * FROM accounts",
                "SELECT * FROM accounts LIMIT 5"
            )
        );
    }

}
