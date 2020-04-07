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

import com.google.common.base.Strings;
import com.google.common.primitives.Doubles;

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
     * @param format format string
     * @param args   arguments referenced by the format specifiers in the format string
     * @return A formatted string
     * @throws java.util.IllegalFormatException If a format string contains an illegal syntax, a format
     *                                          specifier that is incompatible with the given arguments,
     *                                          insufficient arguments given the format string, or other
     *                                          illegal conditions.
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
     * @param input the input String
     * @return the {@code String}, converted to lowercase
     * @see java.lang.String#toLowerCase(Locale)
     */
    public static String toLower(final String input) {
        return input.toLowerCase(Locale.ROOT);
    }

    /**
     * Converts all of the characters in this {@code String} to upper
     * case using the rules of the {@link Locale#ROOT} locale. This is equivalent to calling
     * {@link String#toUpperCase(Locale)} with {@link Locale#ROOT}.
     *
     * @param input the input String
     * @return the {@code String}, converted to uppercase
     * @see java.lang.String#toUpperCase(Locale)
     */
    public static String toUpper(final String input) {
        return input.toUpperCase(Locale.ROOT);
    }

    /**
     * Count how many occurrences of character in this input {@code Sequence}.
     *
     * @param input the input string
     * @param match char to be matched
     * @return number of occurrences
     */
    public static int countMatches(CharSequence input, char match) {
        return Math.toIntExact(input.chars().
                filter(c -> c == match).
                count());
    }

    /**
     *
     * @param text string
     * @param quote
     * @return An unquoted string whose outer pair of back-ticks (if any) has been removed
     */
    public static String unquoteSingleField(String text, String quote) {
        if (isQuoted(text, quote)) {
            return text.substring(quote.length(), text.length() - quote.length());
        }
        return text;
    }

    public static String unquoteSingleField(String text) {
        return unquoteSingleField(text, "`");
    }

    /**
     *
     * @param text
     * @return A string whose each dot-seperated field has been unquoted from back-ticks (if any)
     */
    public static String unquoteFullColumn(String text, String quote) {
        String[] strs = text.split("\\.");
        for (int i = 0; i < strs.length; i++) {
            String unquotedSubstr = unquoteSingleField(strs[i], quote);
            strs[i] = unquotedSubstr;
        }
        return String.join(".", strs);
    }

    public static String unquoteFullColumn(String text) {
        return unquoteFullColumn(text, "`");
    }

    public static boolean isQuoted(String text, String quote) {
        return !Strings.isNullOrEmpty(text) && text.startsWith(quote) && text.endsWith(quote);
    }

    public static boolean isNumeric(String text) {
        return Doubles.tryParse(text) != null;
    }

    public static String getFirstWord(String sql) {
        int endOfFirstWord = sql.indexOf(' ');
        return sql.substring(0, endOfFirstWord > 0 ? endOfFirstWord : sql.length()).toUpperCase();
    }

    private StringUtils() {
        throw new AssertionError(getClass().getCanonicalName() + " is a utility class and must not be initialized");
    }
}
