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
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor.ESMappingLoader;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor.SemanticAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.visitor.TypeChecker;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.SemanticUnsupportedException;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.scope.SemanticContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.antlr.visitor.AntlrSqlParseTreeVisitor;
import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Entry point for ANTLR generated parser to perform strict syntax and semantic analysis.
 */
public class OpenDistroSqlAnalyzer {

    private static final Logger LOG = LogManager.getLogger();

    /** Original sql query */
    private final String sql;

    public OpenDistroSqlAnalyzer(String sql) {
        this.sql = sql;
    }

    public void analyze(LocalClusterState clusterState) {
        try {
            analyzeSemantic(
                analyzeSyntax(),
                clusterState
            );
        } catch (SemanticUnsupportedException e) {
            LOG.error("Skip analysis because of valid but unsupported semantic", e);
        }
    }

    /**
     * Build lexer and parser to perform syntax analysis only.
     * Runtime exception with clear message is thrown for any verification error.
     *
     * @return      parse tree
     */
    public ParseTree analyzeSyntax() {
        OpenDistroSqlParser parser = createParser(createLexer(sql));
        parser.addErrorListener(new SyntaxAnalysisErrorListener());
        return parser.root();
    }

    /**
     * Perform semantic analysis based on syntax analysis output - parse tree.
     * @param tree          parse tree
     * @param clusterState  cluster state required for index mapping query
     */
    public void analyzeSemantic(ParseTree tree, LocalClusterState clusterState, boolean isSuggestEnabled) {
        tree.accept(new AntlrSqlParseTreeVisitor<>(createAnalyzer(clusterState, isSuggestEnabled)));
    }

    public void analyzeSemantic(ParseTree tree, LocalClusterState clusterState) {
        analyzeSemantic(tree, clusterState, false);
    }

    /** Factory method for semantic analyzer to help assemble all required components together */
    private SemanticAnalyzer createAnalyzer(LocalClusterState clusterState, boolean isSuggestEnabled) {
        SemanticContext context = new SemanticContext(clusterState);
        ESMappingLoader mappingLoader = new ESMappingLoader(context, clusterState);
        TypeChecker typeChecker = new TypeChecker(context, isSuggestEnabled);
        return new SemanticAnalyzer(mappingLoader, typeChecker);
    }

    private OpenDistroSqlParser createParser(Lexer lexer) {
        return new OpenDistroSqlParser(
                   new CommonTokenStream(lexer));
    }

    private OpenDistroSqlLexer createLexer(String sql) {
         return new OpenDistroSqlLexer(
                    new CaseInsensitiveCharStream(sql));
    }

}
