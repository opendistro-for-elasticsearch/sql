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

/**
 * Utils Class for remove the back-ticks of identifiers
 */
public class BackticksUnquoter {

    /**
     *
     * @param text
     * @return An unquoted string whose outer pair of back-ticks (if any) has been removed
     */
    public static String unquoteSingleField(String text) {
        if (text != null && text.startsWith("`") && text.endsWith("`")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }

    /**
     *
     * @param text
     * @return A string whose each dot-seperated field has been unquoted from back-ticks (if any)
     */
    public static String unquoteFullColumn(String text) {
        if (text == null) {
            return null;
        }
        String[] strs = text.split("\\.");
        for (int i = 0; i < strs.length; i++) {
            String unquotedSubstr = strs[i];
            if (unquotedSubstr != null && unquotedSubstr.startsWith("`") && unquotedSubstr.endsWith("`")) {
                unquotedSubstr = strs[i].substring(1, strs[i].length() - 1);
            }
            strs[i] = unquotedSubstr;
        }
        StringBuilder unquotedStrBuilder = new StringBuilder(strs[0]);
        for (int i = 1; i < strs.length; i++) {
            unquotedStrBuilder.append(".").append(strs[i]);
        }
        return unquotedStrBuilder.toString();
    }

    public BackticksUnquoter() {
    }

}
