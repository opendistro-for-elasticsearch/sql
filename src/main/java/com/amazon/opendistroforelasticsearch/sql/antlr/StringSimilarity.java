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

package com.amazon.opendistroforelasticsearch.sql.antlr;

import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * String similarity for similar string(s) computation
 */
public class StringSimilarity {

    private final StringDistance algorithm = new LevenshteinDistance();

    private final Collection<String> candidates;

    public StringSimilarity(Collection<String> candidates) {
        this.candidates = Collections.unmodifiableCollection(candidates);
    }

    /**
     * Calculate similarity distance between target and candidate strings.
     * @param target string to match
     * @return       one or more most similar strings
     */
    public List<String> similarTo(String target) {
        float max = -1;
        String result = target; // get only one for now
        for (String name : candidates) {
            float dist = algorithm.getDistance(target, name);
            if (dist > max) {
                result = name;
                max = dist;
            }
        }
        return Arrays.asList(result);
    }

}
