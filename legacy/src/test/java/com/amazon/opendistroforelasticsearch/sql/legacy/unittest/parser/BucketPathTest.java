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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest.parser;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath.BucketPath;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.bucketpath.Path;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class BucketPathTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private final Path agg1 = Path.getAggPath("projects@NESTED");
    private final Path agg2 = Path.getAggPath("projects@FILTERED");
    private final Path metric = Path.getMetricPath("c");

    @Test
    public void bucketPath() {
        BucketPath bucketPath = new BucketPath();
        bucketPath.add(metric);
        bucketPath.add(agg2);
        bucketPath.add(agg1);

        assertEquals("projects@NESTED>projects@FILTERED.c", bucketPath.getBucketPath());
    }

    @Test
    public void bucketPathEmpty() {
        BucketPath bucketPath = new BucketPath();

        assertEquals("", bucketPath.getBucketPath());
    }

    @Test
    public void theLastMustBeMetric() {
        BucketPath bucketPath = new BucketPath();

        exceptionRule.expect(AssertionError.class);
        exceptionRule.expectMessage("The last path in the bucket path must be Metric");
        bucketPath.add(agg1);
    }

    @Test
    public void allTheOtherMustBeAgg() {
        BucketPath bucketPath = new BucketPath();

        exceptionRule.expect(AssertionError.class);
        exceptionRule.expectMessage("All the other path in the bucket path must be Agg");
        bucketPath.add(metric);
        bucketPath.add(metric);
    }
}
