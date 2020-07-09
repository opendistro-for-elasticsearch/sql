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

package com.amazon.opendistroforelasticsearch.jdbc.protocol.http;

import com.amazon.opendistroforelasticsearch.jdbc.protocol.ColumnDescriptor;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.QueryResponse;
import com.amazon.opendistroforelasticsearch.jdbc.protocol.RequestError;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * {@link QueryResponse} implementation for the JSON protocol
 */
public class JsonQueryResponse implements QueryResponse {

    private List<SchemaEntry> schema;

    private List<List<Object>> datarows;

    private int size;

    private int total;

    private int status;

    private String cursor;

    private JsonRequestError error;

    @Override
    public List<SchemaEntry> getColumnDescriptors() {
        return schema;
    }

    @Override
    public List<List<Object>> getDatarows() {
        return datarows;
    }

    public void setSchema(List<SchemaEntry> schema) {
        this.schema = schema;
    }

    public void setDatarows(List<List<Object>> datarows) {
        this.datarows = datarows;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setCursor(String cursor) {
        this.cursor = cursor;
    }

    public void setError(JsonRequestError error) {
        this.error = error;
    }

    @Override
    public long getTotal() {
        return total;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public String getCursor() {
        return cursor;
    }

    @Override
    public RequestError getError() {
        return error;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonQueryResponse)) return false;
        JsonQueryResponse response = (JsonQueryResponse) o;
        return getSize() == response.getSize() &&
                getTotal() == response.getTotal() &&
                getStatus() == response.getStatus() &&
                getCursor() == response.getCursor() &&
                Objects.equals(schema, response.schema) &&
                Objects.equals(getDatarows(), response.getDatarows()) &&
                Objects.equals(getError(), response.getError());
    }

    @Override
    public int hashCode() {
        return Objects.hash(schema, getDatarows(), getSize(), getTotal(), getStatus(), getCursor(), getError());
    }

    @Override
    public String toString() {
        return "JsonQueryResponse{" +
                "schema=" + schema +
                "cursor=" + cursor +
                ", datarows=" + datarows +
                ", size=" + size +
                ", total=" + total +
                ", status=" + status +
                ", error=" + error +
                '}';
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SchemaEntry implements ColumnDescriptor {
        private String name;
        private String type;
        private String label;

        @JsonCreator
        public SchemaEntry(@JsonProperty("name") String name, @JsonProperty("type") String type,
                           @JsonProperty("alias") String label) {
            this.name = name;
            this.type = type;
            this.label = label;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SchemaEntry)) return false;
            SchemaEntry that = (SchemaEntry) o;
            return Objects.equals(getName(), that.getName()) &&
                    Objects.equals(getType(), that.getType()) &&
                    Objects.equals(getLabel(), that.getLabel());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getType(), getLabel());
        }

        @Override
        public String toString() {
            return "SchemaEntry{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", label='" + label + '\'' +
                    '}';
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JsonRequestError implements RequestError {

        private String reason;
        private String details;
        private String type;

        public void setReason(String reason) {
            this.reason = reason;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String getReason() {
            return reason;
        }

        @Override
        public String getDetails() {
            return details;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JsonRequestError)) return false;
            JsonRequestError that = (JsonRequestError) o;
            return Objects.equals(getReason(), that.getReason()) &&
                    Objects.equals(getDetails(), that.getDetails()) &&
                    Objects.equals(getType(), that.getType());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getReason(), getDetails(), getType());
        }

        @Override
        public String toString() {
            return "JsonRequestError{" +
                    "reason='" + reason + '\'' +
                    ", details='" + Objects.hashCode(details) + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
