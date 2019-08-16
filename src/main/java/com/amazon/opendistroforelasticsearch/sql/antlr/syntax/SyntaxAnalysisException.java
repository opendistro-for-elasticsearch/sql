package com.amazon.opendistroforelasticsearch.sql.antlr.syntax;

import com.amazon.opendistroforelasticsearch.sql.antlr.SqlAnalysisException;

/**
 * Exception for syntax analysis
 */
public class SyntaxAnalysisException extends SqlAnalysisException {

    public SyntaxAnalysisException(String template, Object... args) {
        super(template, args);
    }

}
