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
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.AntlrParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Entry point for ANTLR generated parser to perform strict syntax and semantic analysis.
 */
public class OpenDistroSqlAnalyzer {

    /**
     * Build lexer and parser to perform syntax analysis.
     * Runtime exception with clear message is thrown for any verification error.
     *
     * @param sql   original query
     * @return      parse tree
     */
    public ParseTree analyzeSyntax(String sql) {
        OpenDistroSqlParser parser = createParser(createLexer(sql));
        parser.addErrorListener(new SyntaxAnalysisErrorListener());
        return parser.root();
    }

    public ParseTree analyzeSemantic(ParseTree tree, LocalClusterState clusterState) {
        tree.accept(new AntlrParseTreeVisitor<>(new SemanticAnalyzer(clusterState)));
        return tree;
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
        parser.addErrorListener(new SyntaxAnalysisErrorListener());
        return parser.root();
    }

    private void analyzeSemantic(ParseTree tree) {
        //TODO: implement semantic analysis in next stage
    }

}
