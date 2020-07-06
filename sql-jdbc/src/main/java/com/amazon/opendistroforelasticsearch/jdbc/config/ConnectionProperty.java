/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.config;

/**
 * Represents a Connection configuration property.
 * <p>
 * A property has an associated raw value and a parsed value.
 * The raw value is any Object that is provided by a user when setting
 * the property, while the parsed value is the effective value computed
 * from the raw value.
 * <p>
 * The raw value and parsed value need not have similar instance types
 * since properties may accept a varied set of input raw values to compute
 * the parsed value e.g a String "true" or "false" may be accepted to
 * compute a Boolean property.
 * <p>
 * During Connection initialization, all defined connection properties
 * are expected to be initialized with the value provided by the user or
 * a null if no value was provided by the user. Each property defines
 * its own behavior of how it gets initialized.
 *
 * @param <T> The type of the parsed value of the property.
 */
public abstract class ConnectionProperty<T> {

    private final String key;
    private Object rawValue;
    private T parsedValue;
    private boolean parsed = false;

    public ConnectionProperty(final String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    /**
     * @return the raw value provided to set the property
     */
    public Object getRawValue() {
        return rawValue;
    }

    /**
     * Set the property using a specified raw value.
     *
     * @param rawValue the raw value to use
     *
     * @throws ConnectionPropertyException if the raw input value can not
     *         be parsed or fails validation constraints applicable on the
     *         property value.
     */
    public void setRawValue(Object rawValue) throws ConnectionPropertyException {
        this.rawValue = rawValue;
        this.parsed = false;
        parse();
    }

    /**
     * Returns the computed value of the property after parsing the
     * raw value provided for the property.
     *
     * @return the effective value of the property
     *
     * @throws IllegalStateException is a valid  value has not been
     *         set for this property
     */
    public T getValue() {
        verifyParsed();
        return parsedValue;
    }

    /**
     * @return true if the raw property value has been successfully parsed
     *         to compute the parsed value, false otherwise
     */
    public boolean isParsed() {
        return parsed;
    }

    /**
     * The default value the property should be set to in the absence of
     * an explicitly configured value.
     *
     * @return default value for the property
     */
    public abstract T getDefault();

    /**
     * Method that pre-processes a supplied raw value for a
     * property prior to it being passed into the property's
     * parseValue function.
     * <p>
     * This function is meant to offload any value pre-processing
     * like trimming of String input values prior to the value being
     * used in the parseValue function.
     * <p>
     * Currently, the only pre-processing applied is whitespace trimming of
     * the input in case the raw input is a String. Subclass properties may
     * override this method to modify or extend the default pre-processing
     * of their raw input values.
     *
     * @param value - The raw value provided for the property
     *
     * @return The value that should be used by the parseValue function
     */
    protected Object preProcess(Object value) {
        if (value instanceof String) {
            return ((String) value).trim();
        } else {
            return value;
        }
    }

    /**
     * Given a raw value for a property, the method returns the actual
     * value that the property should be set to.
     *
     * @param rawValue raw property input value
     *
     * @return the actual value the property should be set to
     *
     * @throws ConnectionPropertyException if the raw input value can not
     *         be parsed or fails validation constraints applicable on the
     *         property value.
     */
    protected abstract T parseValue(Object rawValue) throws ConnectionPropertyException;

    /**
     * Execute parsing of the raw value
     *
     * @throws ConnectionPropertyException if the raw input value can not
     *         be parsed or fails validation constraints applicable on the
     *         property value.
     */
    private void parse() throws ConnectionPropertyException {
        if (!parsed) {
            this.parsedValue = parseValue(preProcess(rawValue));
            parsed = true;
        }
    }

    /**
     * Verify if the supplied value for this property was successfully
     * parsed.
     *
     * @throws {@link IllegalStateException} if a valid property value
     *         has not been provided.
     */
    private void verifyParsed() {
        if (!isParsed()) {
            throw new IllegalStateException(String.format("Property %s is not yet successfully parsed.", getKey()));
        }
    }
}
