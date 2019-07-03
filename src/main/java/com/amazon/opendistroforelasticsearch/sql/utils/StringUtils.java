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

package com.amazon.opendistroforelasticsearch.sql.utils;

import java.util.Locale;

/**
 * Helper class for wrapping locale-dependent methods
 * of {@link String} class in equivalent locale-independent
 * methods which always use {@link Locale#ROOT|.
 */
public class StringUtils {

    /**
     * Returns a formatted string using the specified format string and
     * arguments, as well as the {@link Locale#ROOT} locale.
     *
     * @param  format
     *         format string
     *
     * @param  args
     *         arguments referenced by the format specifiers in the format string
     *
     * @throws  java.util.IllegalFormatException
     *          If a format string contains an illegal syntax, a format
     *          specifier that is incompatible with the given arguments,
     *          insufficient arguments given the format string, or other
     *          illegal conditions.
     *
     * @return  A formatted string
     *
     * @see java.lang.String#format(Locale, String, Object...)
     */
    public static String format(final String format, Object... args) {
        return String.format(Locale.ROOT, format, args);
    }

    /**
     * Converts all of the characters in this {@code String} to lower
     * case using the rules of the {@link Locale#ROOT} locale. This is equivalent to calling
     * {@link String#toLowerCase(Locale)} with {@link Locale#ROOT}.
     *
     * @param input
     *        the input String
     *
     * @return  the {@code String}, converted to lowercase
     *
     * @see     java.lang.String#toLowerCase(Locale)
     */
    public static String toLower(final String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    /**
     * Converts all of the characters in this {@code String} to upper
     * case using the rules of the {@link Locale#ROOT} locale. This is equivalent to calling
     * {@link String#toUpperCase(Locale)} with {@link Locale#ROOT}.
     *
     * @param input
     *        the input String
     *
     * @return  the {@code String}, converted to uppercase
     *
     * @see     java.lang.String#toUpperCase(Locale)
     */
    public static String toUpper(final String input) {
        return input.toUpperCase(Locale.ROOT);
    }

    private StringUtils() {
        throw new AssertionError(getClass().getCanonicalName() + " is a utility class and must not be initialized");
    }
}
