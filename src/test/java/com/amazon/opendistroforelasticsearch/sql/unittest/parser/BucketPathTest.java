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

package com.amazon.opendistroforelasticsearch.sql.unittest.parser;

import com.amazon.opendistroforelasticsearch.sql.domain.bucketpath.AggPath;
import com.amazon.opendistroforelasticsearch.sql.domain.bucketpath.BucketPath;
import com.amazon.opendistroforelasticsearch.sql.domain.bucketpath.MetricPath;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class BucketPathTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void bucketPath() {
        BucketPath bucketPath = new BucketPath();
        bucketPath.add(new MetricPath("c"));
        bucketPath.add(new AggPath("projects@FILTERED"));
        bucketPath.add(new AggPath("projects@NESTED"));

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
        bucketPath.add(new AggPath("projects@NESTED"));
    }

    @Test
    public void allTheOtherMustBeAgg() {
        BucketPath bucketPath = new BucketPath();

        exceptionRule.expect(AssertionError.class);
        exceptionRule.expectMessage("All the other path in the bucket path must be Agg");
        bucketPath.add(new MetricPath("c"));
        bucketPath.add(new MetricPath("c"));
    }

    @Test
    public void atLeastIncludeAggAndMetric() {
        BucketPath bucketPath = new BucketPath();

        exceptionRule.expect(AssertionError.class);
        exceptionRule.expectMessage("The bucket path should as least include agg and metric");
        bucketPath.add(new MetricPath("c"));
        bucketPath.getBucketPath();
    }
}
