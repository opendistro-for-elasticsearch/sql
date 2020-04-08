/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.antlr;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.PPLLexer;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.PPLParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * PPL Syntax Parser
 */
public class PPLSyntaxParser {
    public ParseTree analyzeSyntax(String sql) {
        PPLParser parser = createParser(createLexer(sql));
        parser.addErrorListener(new SyntaxAnalysisErrorListener());
        return parser.root();
    }

    private PPLParser createParser(Lexer lexer) {
        return new PPLParser(
                new CommonTokenStream(lexer));
    }

    private PPLLexer createLexer(String sql) {
        return new PPLLexer(
                new CaseInsensitiveCharStream(sql));
    }
}
