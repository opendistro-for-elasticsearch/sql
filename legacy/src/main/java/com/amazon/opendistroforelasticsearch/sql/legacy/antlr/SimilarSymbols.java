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

import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

/**
 * String similarity for finding most similar string.
 */
public class SimilarSymbols {

    /** LevenshteinDistance instance is basically a math util which is supposed to be thread safe */
    private static final StringDistance ALGORITHM = new LevenshteinDistance();

    /** Symbol candidate list from which to pick one as most similar symbol to a target */
    private final Collection<String> candidates;

    public SimilarSymbols(Collection<String> candidates) {
        this.candidates = Collections.unmodifiableCollection(candidates);
    }

    /**
     * Find most similar string in candidates by calculating similarity distance
     * among target and candidate strings.
     *
     * @param target    string to match
     * @return          most similar string to the target
     */
    public String mostSimilarTo(String target) {
        Optional<SymbolDistance> closest = candidates.stream().
                                                      map(candidate -> new SymbolDistance(candidate, target)).
                                                      max(Comparator.comparing(SymbolDistance::similarity));
        if (closest.isPresent()) {
            return closest.get().candidate;
        }
        return target;
    }

    /** Distance (similarity) between 2 symbols. This class is mainly for Java 8 stream comparator API */
    private static class SymbolDistance {
        private final String candidate;
        private final String target;

        private SymbolDistance(String candidate, String target) {
            this.candidate = candidate;
            this.target = target;
        }

        public float similarity() {
            return ALGORITHM.getDistance(candidate, target);
        }
    }
}