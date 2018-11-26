package org.elasticsearch.plugin.nlpcn.executors;

import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.ErrorMessage;
import org.elasticsearch.plugin.nlpcn.Protocol;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import org.nlpcn.es4sql.query.QueryAction;

import java.util.Map;

import static org.elasticsearch.plugin.nlpcn.QueryActionElasticExecutor.executeAnyAction;

public class PrettyFormatRestExecutor implements RestExecutor {

    private final String format;

    public PrettyFormatRestExecutor(String format) {
        this.format = format.toLowerCase();
    }

    /**
     * Execute the QueryAction and return the REST response using the channel.
     */
    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) {
        String formattedResponse = execute(client, params, queryAction);
        BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, formattedResponse);
        channel.sendResponse(bytesRestResponse);
    }

    @Override
    public String execute(Client client, Map<String, String> params, QueryAction queryAction) {
        Protocol protocol;

        try {
            Object queryResult = executeAnyAction(client, queryAction);
            protocol = new Protocol(client, queryAction.getQuery(), queryResult, format);
        } catch (Exception e) {
            // TODO Might require some refactoring, Exceptions that happen in RestSqAction code before invoking execution
            // TODO are being caught in RestController (line 242) and being sent as a bytesRestResponse
            // ex. "SELECT * FROM WHERE balance > 30000", results in ParserException and ErrorMessage is never made
            protocol = new Protocol(e);
        }

        return protocol.format();
    }
}
