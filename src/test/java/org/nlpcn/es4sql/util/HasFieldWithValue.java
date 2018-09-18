package org.nlpcn.es4sql.util;

import org.hamcrest.Factory;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.lang.reflect.Field;

/**
 * A matcher for private field value extraction along with matcher to assert its value.
 *
 * @param <T>   Type of target (actual) object
 * @param <U>   Type of field member (feature) extracted from target object by reflection
 */
public class HasFieldWithValue<T, U> extends FeatureMatcher<T, U> {

    private final String fieldName;

    /**
     * Construct a matcher. Reordered the argument list.
     *
     * @param name      Identifying text for mismatch message
     * @param desc      Descriptive text to use in describeTo
     * @param matcher   The matcher to apply to the feature
     */
    private HasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        super(matcher, desc, name);
        this.fieldName = name;
    }

    @Factory
    public static <T, U> HasFieldWithValue<T, U> hasFieldWithValue(String name, String desc, Matcher<? super U> matcher) {
        return new HasFieldWithValue<>(name, desc, matcher);
    }

    @Override
    protected U featureValueOf(T targetObj) {
        return getFieldValue(targetObj, fieldName);
    }

    @SuppressWarnings("unchecked")
    private U getFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return (U) field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
