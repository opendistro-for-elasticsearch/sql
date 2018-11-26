package org.elasticsearch.plugin.nlpcn.executors;

import java.util.stream.Stream;

/**
 * Created by Eliran on 26/12/2015.
 */
public class ActionRequestRestExecuterFactory {
    public static RestExecutor createExecutor(String format) {
        if(format == null || format.equals("")){
            return new ElasticDefaultRestExecutor();
        }
        if(format.equalsIgnoreCase("csv")){
            return new CSVResultRestExecutor();
        }

        if (Stream.of("jdbc", "table", "raw").anyMatch(format::equalsIgnoreCase)) {
            return new PrettyFormatRestExecutor(format);
        }
        return null;
    }
}
