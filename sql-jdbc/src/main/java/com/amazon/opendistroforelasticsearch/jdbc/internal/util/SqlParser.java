/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.internal.util;


/**
 * Rudimentary SQL parser to help with very basic
 * driver side validations.
 */
public class SqlParser {

    public static int countParameterMarkers(String sql) {
        if (sql == null)
            return 0;

        int count = 0;

        for (int i=0; i < sql.length(); i++) {

            char ch = sql.charAt(i);

            switch (ch) {
                case '\'':
                case '\"':
                    i = locateQuoteEnd(sql, ch, i+1);
                    break;
                case '?':
                    count++;
                    break;
                case '-':
                case '/':
                    i = locateCommentEnd(sql, ch, i);
            }

        }
        return count;
    }

    private static int locateCommentEnd(String s, char commentStartChar, int commentStartIndex) {
        if (commentStartIndex + 1 > s.length())
            return commentStartIndex;

        int idx;

        if (commentStartChar == '-' && s.charAt(commentStartIndex + 1) == '-') {
            // single line comment
            idx = locateLineEnd(s, commentStartIndex + 2);

        } else if (commentStartChar == '/' && s.charAt(commentStartIndex + 1) == '*') {
            // multi line comment
            idx = s.indexOf("*/", commentStartIndex + 2);

        } else {
            // not on a comment
            return commentStartIndex;
        }
        
        if (idx == -1)
            throw new IllegalArgumentException("SQL text contains an unterminated comment.");
        else
            return idx;
    }

    private static int locateQuoteEnd(String s, char ch, int fromIndex) {
        int idx = s.indexOf(ch, fromIndex);
        if (idx == -1)
            throw new IllegalArgumentException("SQL text contains an unterminated string. " +
                    "This could possibly be due to mismatched quotes in the statement.");
        return idx;
    }


    private static int locateLineEnd(String s, int fromIndex) {
        int idx;

        for (idx=fromIndex; idx < s.length(); idx++) {
            char ch = s.charAt(idx);

            if (ch == '\r' || ch == '\n')
                break;
        }
        return idx;
    }
}
