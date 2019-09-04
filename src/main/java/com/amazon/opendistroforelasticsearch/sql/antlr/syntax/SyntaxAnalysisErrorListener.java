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

package com.amazon.opendistroforelasticsearch.sql.antlr.syntax;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

/**
 * Syntax analysis error listener that handles any syntax error by throwing exception with useful information.
 */
public class SyntaxAnalysisErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                            int line, int charPositionInLine, String msg, RecognitionException e) {

        CommonTokenStream tokens = (CommonTokenStream) recognizer.getInputStream();
        //Token offendingToken = reviseOffendingToken(tokens, (Token) offendingSymbol);
        Token offendingToken = (Token) offendingSymbol;

        String query = tokens.getText();
        IntervalSet followSet = e.getExpectedTokens();
        //IntervalSet followSet = recognizer.getATN().nextTokens(recognizer.getInterpreter().
        //      atn.states.get(recognizer.getState()), e.getCtx());
        //IntervalSet followSet = recognizer.getATN().getExpectedTokens(recognizer.getState(), e.getCtx());
        throw new SyntaxAnalysisException(
            "Failed to parse query due to syntax error by offending symbol [%s] at: %s <--- HERE. "
                + "Expecting tokens %s. More details: %s",
            offendingToken.getText(), query.substring(0, offendingToken.getStopIndex() + 1),
            followSet.toString(recognizer.getVocabulary()), msg
        );
    }

    /** Look backward for more accurate offending symbol. */
    private Token reviseOffendingToken(CommonTokenStream tokens, Token original) {
        for (int i = original.getTokenIndex(); i > 0; i--) {
            if (isValidToken(tokens.get(i))) {
                return tokens.get(i);
            }
        }
        return tokens.get(0);
    }

    /** Check if single character token is valid or not, ex. '(', ',', ' ' etc. */
    private boolean isValidToken(Token token) {
        String text = token.getText();
        if (text.length() > 1) {
            return true;
        }

        char character = text.charAt(0);
        return Character.isLetterOrDigit(character); // Should work for unicode too
    }

}
