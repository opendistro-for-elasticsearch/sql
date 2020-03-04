package com.amazon.opendistroforelasticsearch.ppl.antlr;

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLLexer;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.tree.ParseTree;

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
