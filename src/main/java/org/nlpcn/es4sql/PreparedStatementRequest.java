package org.nlpcn.es4sql;

import org.json.JSONObject;

import java.util.List;

public class PreparedStatementRequest extends SqlRequest {

    private List<PreparedStatementParameter> parameters;
    private String sqlTemplate;

    public PreparedStatementRequest(String sql, JSONObject payloadJson, List<PreparedStatementParameter> parameters) {
        super(null, payloadJson);
        this.sqlTemplate = sql;
        this.parameters = parameters;
        this.sql = this.substituteParameters();
    }

    public List<PreparedStatementParameter> getParameters() {
        return this.parameters;
    }

    @Override
    public String getSql() {
        return this.sql;
    }

    public String getPreparedStatement() {
        return this.sqlTemplate;
    }

    private String substituteParameters() {
        if(this.sqlTemplate == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        int paramIndex = 0;
        int i = 0;
        while(i < this.sqlTemplate.length()) {
            char c = this.sqlTemplate.charAt(i);
            if (c == '\'') {
                // found string starting quote character, skip the string
                sb.append(c);
                i++;
                while (i < this.sqlTemplate.length()) {
                    char s = this.sqlTemplate.charAt(i);
                    sb.append(s);
                    if (s == '\'') {
                        if (this.sqlTemplate.charAt(i-1) == '\\') {
                            // this is an escaped single quote (\') still in the string
                            i++;
                        } else if ((i + 1) < this.sqlTemplate.length() && this.sqlTemplate.charAt(i+1) == '\'') {
                            // found 2 single quote {''} in a string, which is escaped single quote {'}
                            // move to next character
                            sb.append('\'');
                            i += 2;
                        } else {
                            // found the string ending single quote char
                            break;
                        }
                    } else {
                        // not single quote character, move on
                        i++;
                    }
                }
            } else if (c == '?') {
                // question mark "?" not in a string
                if(paramIndex >= this.parameters.size()) {
                    throw new IllegalStateException("Placeholder count is greater than parameter number "
                            + parameters.size() + " . Cannot convert PreparedStatement to sql query");
                }
                sb.append(this.parameters.get(paramIndex).getSqlSubstitutionValue());
                paramIndex++;
            } else {
                // other character, simply append
                sb.append(c);
            }
            i++;
        }

        return sb.toString();
    }

    //////////////////////////////////////////////////
    // Parameter related types below
    //////////////////////////////////////////////////
    public static enum ParameterType {
        BYTE,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BOOLEAN,
        STRING,
        KEYWORD,
        DATE,
        NULL;
    }

    public static class PreparedStatementParameter<T> {
        protected T value;

        public PreparedStatementParameter (T value) {
            this.value = value;
        }

        public String getSqlSubstitutionValue() {
            return String.valueOf(this.value);
        }

        public T getValue() {
            return this.value;
        }
    }

    public static class StringParameter extends PreparedStatementParameter<String> {

        public StringParameter(String value) {
            super(value);
        }

        @Override
        public String getSqlSubstitutionValue() {
            // TODO: investigate other injection prevention
            if (this.value == null) {
                return "null";
            }
            StringBuilder sb = new StringBuilder();
            sb.append('\''); // starting quote
            for (int i = 0; i<this.value.length(); i++) {
                char c = this.value.charAt(i);
                switch (c) {
                    case 0: sb.append('\\').append(0); break;
                    case '\n': sb.append('\\').append('n'); break;
                    case '\r': sb.append('\\').append('r'); break;
                    case '\\': sb.append('\\').append('\\'); break;
                    case '\'': sb.append('\\').append('\''); break;
                    case '\"': sb.append('\\').append('\"'); break;
                    default: sb.append(c);
                }
            }
            sb.append('\''); // ending quote
            return sb.toString();
        }
    }

    public static class NullParameter extends PreparedStatementParameter {

        public NullParameter() {
            super(null);
        }

        @Override
        public String getSqlSubstitutionValue() {
            return "null";
        }
    }
}
