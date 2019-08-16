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
        float max = 0;
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
