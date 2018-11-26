package org.elasticsearch.plugin.nlpcn;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorMessage {

    private Exception exception;

    private int status;
    private String type;
    private String reason;
    private String details;

    public ErrorMessage(Exception exception, int status) {
        this.exception = exception;
        this.status = status;

        this.type = fetchType();
        this.reason = fetchReason();
        this.details = fetchDetails();
    }

    private String fetchType() { return exception.getClass().getName(); }

    private String fetchReason() { return emptyStringIfNull(exception.getLocalizedMessage()); }

    private String fetchDetails() {
        // TODO When outputting to string, the JSON includes "\n" and "\t" explicitly for stack trace details
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);

        return sw.toString();
    }

    private String emptyStringIfNull(String str) { return str != null ? str : ""; }

    @Override
    public String toString() {
        JSONObject output = new JSONObject();

        output.put("status", status);
        output.put("error", getErrorAsJson());

        return output.toString(2);
    }

    private JSONObject getErrorAsJson() {
        JSONObject errorJson = new JSONObject();

        errorJson.put("type", type);
        errorJson.put("reason", reason);
        errorJson.put("details", details);

        return errorJson;
    }
}
