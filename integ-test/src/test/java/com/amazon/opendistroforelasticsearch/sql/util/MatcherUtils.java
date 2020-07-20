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

package com.amazon.opendistroforelasticsearch.sql.util;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONArray;
import org.json.JSONObject;

public class MatcherUtils {

  /**
   * Assert field value in object by a custom matcher and getter to access the field.
   *
   * @param name       description
   * @param subMatcher sub-matcher for field
   * @param getter     getter function to access the field
   * @param <T>        type of outer object
   * @param <U>        type of inner field
   * @return matcher
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
    return featureValueOf("SearchHits", arrayContainingInAnyOrder(hitMatchers),
        SearchHits::getHits);
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

  public static Matcher<JSONObject> hitAny(String query, Matcher<JSONObject>... matcher) {
    return featureValueOf("SearchHits", hasItems(matcher), actual -> {
      JSONArray array = (JSONArray) (actual.query(query));
      List<JSONObject> results = new ArrayList<>(array.length());
      for (Object element : array) {
        results.add((JSONObject) element);
      }
      return results;
    });
  }

  public static Matcher<JSONObject> hitAny(Matcher<JSONObject>... matcher) {
    return hitAny("/hits/hits", matcher);
  }

  public static Matcher<JSONObject> hitAll(Matcher<JSONObject>... matcher) {
    return featureValueOf("SearchHits", containsInAnyOrder(matcher), actual -> {
      JSONArray array = (JSONArray) (actual.query("/hits/hits"));
      List<JSONObject> results = new ArrayList<>(array.length());
      for (Object element : array) {
        results.add((JSONObject) element);
      }
      return results;
    });
  }

  public static Matcher<JSONObject> kvString(String key, Matcher<String> matcher) {
    return featureValueOf("Json Match", matcher, actual -> (String) actual.query(key));
  }

  public static Matcher<JSONObject> kvDouble(String key, Matcher<Double> matcher) {
    return featureValueOf("Json Match", matcher, actual -> (Double) actual.query(key));
  }

  public static Matcher<JSONObject> kvInt(String key, Matcher<Integer> matcher) {
    return featureValueOf("Json Match", matcher, actual -> (Integer) actual.query(key));
  }

  @SafeVarargs
  public static void verifySchema(JSONObject response, Matcher<JSONObject>... matchers) {
    verify(response.getJSONArray("schema"), matchers);
  }

  @SafeVarargs
  public static void verifyDataRows(JSONObject response, Matcher<JSONArray>... matchers) {
    verify(response.getJSONArray("datarows"), matchers);
  }

  @SafeVarargs
  public static void verifyColumn(JSONObject response, Matcher<JSONObject>... matchers) {
    verify(response.getJSONArray("schema"), matchers);
  }

  @SafeVarargs
  public static void verifyOrder(JSONObject response, Matcher<JSONArray>... matchers) {
    verifyOrder(response.getJSONArray("datarows"), matchers);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static void verifyDataRowsInOrder(JSONObject response, Matcher<JSONArray>... matchers) {
    verifyInOrder(response.getJSONArray("datarows"), matchers);
  }

  @SuppressWarnings("unchecked")
  public static <T> void verify(JSONArray array, Matcher<T>... matchers) {
    List<T> objects = new ArrayList<>();
    array.iterator().forEachRemaining(o -> objects.add((T) o));
    assertEquals(matchers.length, objects.size());
    assertThat(objects, containsInAnyOrder(matchers));
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  public static <T> void verifyInOrder(JSONArray array, Matcher<T>... matchers) {
    List<T> objects = new ArrayList<>();
    array.iterator().forEachRemaining(o -> objects.add((T) o));
    assertEquals(matchers.length, objects.size());
    assertThat(objects, contains(matchers));
  }

  @SuppressWarnings("unchecked")
  public static <T> void verifySome(JSONArray array, Matcher<T>... matchers) {
    List<T> objects = new ArrayList<>();
    array.iterator().forEachRemaining(o -> objects.add((T) o));

    assertThat(matchers.length, greaterThan(0));
    for (Matcher<T> matcher : matchers) {
      assertThat(objects, hasItems(matcher));
    }
  }

  @SafeVarargs
  public static <T> void verifyOrder(JSONArray array, Matcher<T>... matchers) {
    List<T> objects = new ArrayList<>();
    array.iterator().forEachRemaining(o -> objects.add((T) o));
    assertEquals(matchers.length, objects.size());
    assertThat(objects, containsInRelativeOrder(matchers));
  }

  public static TypeSafeMatcher<JSONObject> schema(String expectedName, String expectedAlias,
                                                   String expectedType) {
    return new TypeSafeMatcher<JSONObject>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(
            String
                .format("(name=%s, alias=%s, type=%s)", expectedName, expectedAlias, expectedType));
      }

      @Override
      protected boolean matchesSafely(JSONObject jsonObject) {
        String actualName = (String) jsonObject.query("/name");
        String actualAlias = (String) jsonObject.query("/alias");
        String actualType = (String) jsonObject.query("/type");
        return expectedName.equals(actualName) &&
            (Strings.isNullOrEmpty(actualAlias) && Strings.isNullOrEmpty(expectedAlias) ||
                expectedAlias.equals(actualAlias)) &&
            expectedType.equals(actualType);
      }
    };
  }

  public static TypeSafeMatcher<JSONArray> rows(Object... expectedObjects) {
    return new TypeSafeMatcher<JSONArray>() {
      @Override
      public void describeTo(Description description) {
        description.appendText(String.join(",", Arrays.asList(expectedObjects).toString()));
      }

      @Override
      protected boolean matchesSafely(JSONArray array) {
        if (array.length() != expectedObjects.length) {
          return false;
        }

        for (int i = 0; i < expectedObjects.length; i++) {
          Object expected = expectedObjects[i];
          boolean isEqual;

          // Use similar() because JSONObject/JSONArray.equals() only check if same reference
          if (expected instanceof JSONObject) {
            isEqual = ((JSONObject) expected).similar(array.get(i));
          } else if (expected instanceof JSONArray) {
            isEqual = ((JSONArray) expected).similar(array.get(i));
          } else {
            isEqual = expected.equals(array.get(i));
          }

          if (!isEqual) {
            return false;
          }
        }
        return true;
      }
    };
  }

  public static TypeSafeMatcher<JSONArray> closeTo(Number... values) {
    final double error = 1e-10;
    return new TypeSafeMatcher<JSONArray>() {
      @Override
      protected boolean matchesSafely(JSONArray item) {
        List<Number> expectedValues = new ArrayList<>(Arrays.asList(values));
        List<Number> actualValues = new ArrayList<>();
        item.iterator().forEachRemaining(v -> actualValues.add((Number) v));
        return actualValues.stream()
            .allMatch(v -> valuesAreClose(v, expectedValues.get(actualValues.indexOf(v))));
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.join(",", Arrays.asList().toString()));
      }

      private boolean valuesAreClose(Number v1, Number v2) {
        return Math.abs(v1.doubleValue() - v2.doubleValue()) <= error;
      }
    };
  }

  public static TypeSafeMatcher<JSONObject> columnPattern(String regex) {
    return new TypeSafeMatcher<JSONObject>() {
      @Override
      protected boolean matchesSafely(JSONObject jsonObject) {
        return ((String) jsonObject.query("/name")).matches(regex);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("(column_pattern=%s)", regex));
      }
    };
  }

  public static TypeSafeMatcher<JSONObject> columnName(String name) {
    return new TypeSafeMatcher<JSONObject>() {
      @Override
      protected boolean matchesSafely(JSONObject jsonObject) {
        return jsonObject.query("/name").equals(name);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText(String.format("(name=%s)", name));
      }
    };
  }


  /**
   * Tests if a string is equal to another string, ignore the case and whitespace.
   */
  public static class IsEqualIgnoreCaseAndWhiteSpace extends TypeSafeMatcher<String> {
    private final String string;

    public IsEqualIgnoreCaseAndWhiteSpace(String string) {
      if (string == null) {
        throw new IllegalArgumentException("Non-null value required");
      }
      this.string = string;
    }

    @Override
    public boolean matchesSafely(String item) {
      return ignoreCase(ignoreSpaces(string)).equals(ignoreCase(ignoreSpaces(item)));
    }

    @Override
    public void describeMismatchSafely(String item, Description mismatchDescription) {
      mismatchDescription.appendText("was ").appendValue(item);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("a string equal to ")
          .appendValue(string)
          .appendText(" ignore case and white space");
    }

    public String ignoreSpaces(String toBeStripped) {
      return toBeStripped.replaceAll("\\s+", "").trim();
    }

    public String ignoreCase(String toBeLower) {
      return toBeLower.toLowerCase();
    }

    public static Matcher<String> equalToIgnoreCaseAndWhiteSpace(String expectedString) {
      return new IsEqualIgnoreCaseAndWhiteSpace(expectedString);
    }
  }
}
