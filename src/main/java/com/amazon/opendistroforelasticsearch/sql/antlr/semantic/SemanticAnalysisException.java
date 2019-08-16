package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.SqlAnalysisException;

/**
 * Exception for semantic analysis
 */
public class SemanticAnalysisException extends SqlAnalysisException {

    public SemanticAnalysisException(String template, Object... args) {
        super(template, args);
    }

}
