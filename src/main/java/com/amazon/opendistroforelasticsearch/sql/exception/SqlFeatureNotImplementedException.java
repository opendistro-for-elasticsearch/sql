package com.amazon.opendistroforelasticsearch.sql.exception;

/**
 * Intended for cases when we knowingly omitted some case, letting users know that we didn't implemented feature, but
 * it may be implemented in future.
 */
public class SqlFeatureNotImplementedException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public SqlFeatureNotImplementedException(String message) {
        super(message);
    }

    public SqlFeatureNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }
}
