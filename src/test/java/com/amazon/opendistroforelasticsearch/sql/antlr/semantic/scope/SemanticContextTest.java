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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope;

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import org.junit.Assert;
import org.junit.Test;

import static org.mockito.Mockito.mock;

/**
 * Test cases for semantic context
 */
public class SemanticContextTest {

    private final SemanticContext context = new SemanticContext(mock(LocalClusterState.class));

    @Test
    public void rootEnvironmentShouldBeThereInitially() {
        Assert.assertNotNull(
            "Didn't find root environment. Context is NOT supposed to be empty initially",
            context.peek()
        );
    }

    @Test
    public void pushAndPopEnvironmentShouldPass() {
        context.push();
        context.pop();
    }

    @Test
    public void popRootEnvironmentShouldPass() {
        context.pop();
    }

    @Test(expected = NullPointerException.class)
    public void popEmptyEnvironmentStackShouldFail() {
        context.pop();
        context.pop();
    }

}
