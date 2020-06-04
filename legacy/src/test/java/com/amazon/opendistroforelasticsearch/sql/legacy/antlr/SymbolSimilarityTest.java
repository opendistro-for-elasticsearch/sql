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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

/**
 * Test cases for symbol similarity
 */
public class SymbolSimilarityTest {

    @Test
    public void noneCandidateShouldReturnTargetStringItself() {
        String target = "test";
        String mostSimilarSymbol = new SimilarSymbols(emptyList()).mostSimilarTo(target);
        Assert.assertEquals(target, mostSimilarSymbol);
    }

    @Test
    public void singleCandidateShouldReturnTheOnlyCandidate() {
        String target = "test";
        String candidate = "hello";
        String mostSimilarSymbol = new SimilarSymbols(singletonList(candidate)).mostSimilarTo(target);
        Assert.assertEquals(candidate, mostSimilarSymbol);
    }

    @Test
    public void twoCandidatesShouldReturnMostSimilarCandidate() {
        String target = "test";
        String mostSimilar = "tests";
        List<String> candidates = Arrays.asList("hello", mostSimilar);
        String mostSimilarSymbol = new SimilarSymbols(candidates).mostSimilarTo(target);
        Assert.assertEquals(mostSimilar, mostSimilarSymbol);
    }

    @Test
    public void manyCandidatesShouldReturnMostSimilarCandidate() {
        String target = "test";
        String mostSimilar = "tests";
        List<String> candidates = Arrays.asList("hello", mostSimilar, "world");
        String mostSimilarSymbol = new SimilarSymbols(candidates).mostSimilarTo(target);
        Assert.assertEquals(mostSimilar, mostSimilarSymbol);
    }

}
