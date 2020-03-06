package com.amazon.opendistroforelasticsearch.ppl.exception;

public class PPLFeatureDisabledException extends Exception {
    private static final long serialVersionUID = 1L;

    public PPLFeatureDisabledException(String message) {
        super(message);
    }
}
