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

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlLexer;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Facade for ANTLR generated parser to avoid boilerplate code.
 */
public class OpenDistroSqlAnalyzer {

    /**
     * Generate parse tree for the query to perform syntax and semantic analysis.
     * Runtime exception with clear message is thrown for any verification error.
     *
     * @param sql   original query
     */
    public void analyze(String sql) {
        analyzeSemantic(
            analyzeSyntax(
                createParser(
                    createLexer(sql))));
    }

    private OpenDistroSqlParser createParser(Lexer lexer) {
        return new OpenDistroSqlParser(
                   new CommonTokenStream(lexer));
    }

    private OpenDistroSqlLexer createLexer(String sql) {
         return new OpenDistroSqlLexer(
                    new CaseInsensitiveCharStream(sql));
    }

    private ParseTree analyzeSyntax(OpenDistroSqlParser parser) {
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                    int line, int charPositionInLine, String msg, RecognitionException e) {

                CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
                String query = tokens.getText();
                Token offendingToken = (Token) offendingSymbol;
                throw new SyntaxAnalysisException(
                    "Failed to parse query due to syntax error by offending symbol [%s] at: '%s...' ",
                    offendingToken.getText(), query.substring(0, offendingToken.getStopIndex() + 1)
                );
            }
        });
        return parser.root();
    }

    private void analyzeSemantic(ParseTree tree) {
    }

}
