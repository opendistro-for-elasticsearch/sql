package com.amazon.opendistroforelasticsearch.jdbc.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;

public class AwsCredentialsProviderProperty extends ConnectionProperty<AWSCredentialsProvider> {

    public static final String KEY = "awsCredentialsProvider";

    public AwsCredentialsProviderProperty() {
        super(KEY);
    }

    @Override
    public AWSCredentialsProvider getDefault() {
        return new DefaultAWSCredentialsProviderChain();
    }

    @Override
    protected AWSCredentialsProvider parseValue(Object rawValue) throws ConnectionPropertyException {
        if (null == rawValue) {
            return null;
        } else if (rawValue instanceof AWSCredentialsProvider) {
            return (AWSCredentialsProvider) rawValue;
        }

        throw new ConnectionPropertyException(getKey(),
                String.format("Property \"%s\" requires a valid AWSCredentialsProvider instance. " +
                        "Invalid value of type: %s specified.", getKey(), rawValue.getClass().getName()));
    }
}
