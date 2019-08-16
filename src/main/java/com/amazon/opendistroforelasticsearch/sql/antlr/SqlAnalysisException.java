package com.amazon.opendistroforelasticsearch.sql.antlr;

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

public class SqlAnalysisException extends RuntimeException {

    public SqlAnalysisException(String template, Object... args) {
        super(StringUtils.format(template, args));
    }
}
