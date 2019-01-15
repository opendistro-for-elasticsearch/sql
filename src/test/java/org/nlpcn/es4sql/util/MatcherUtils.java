package org.nlpcn.es4sql.util;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.hasEntry;

public class MatcherUtils {

    /**
     * Assert field value in object by a custom matcher and getter to access the field.
     *
     * @param name          description
     * @param subMatcher    sub-matcher for field
     * @param getter        getter function to access the field
     * @param <T>           type of outer object
     * @param <U>           type of inner field
     * @return              matcher
     */
    public static <T, U> FeatureMatcher<T, U> featureValueOf(String name,
                                                             Matcher<U> subMatcher,
                                                             Function<T, U> getter) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return getter.apply(actual);
            }
        };
    }

    @SafeVarargs
    public static Matcher<SearchHits> hits(Matcher<SearchHit>... hitMatchers) {
        if (hitMatchers.length == 0) {
            return featureValueOf("SearchHits", emptyArray(), SearchHits::getHits);
        }
        return featureValueOf("SearchHits", arrayContainingInAnyOrder(hitMatchers), SearchHits::getHits);
    }

    @SafeVarargs
    public static Matcher<SearchHits> hitsInOrder(Matcher<SearchHit>... hitMatchers) {
        if (hitMatchers.length == 0) {
            return featureValueOf("SearchHits", emptyArray(), SearchHits::getHits);
        }
        return featureValueOf("SearchHits", arrayContaining(hitMatchers), SearchHits::getHits);
    }

    @SuppressWarnings("unchecked")
    public static Matcher<SearchHit> hit(Matcher<Map<String, Object>>... entryMatchers) {
        return featureValueOf("SearchHit", allOf(entryMatchers), SearchHit::getSourceAsMap);
    }

    @SuppressWarnings("unchecked")
    public static Matcher<Map<String, Object>> kv(String key, Object value) {
        // Use raw type to avoid generic type problem from Matcher<Map<K,V>> to Matcher<String,Object>
        return (Matcher) hasEntry(key, value);
    }

}
