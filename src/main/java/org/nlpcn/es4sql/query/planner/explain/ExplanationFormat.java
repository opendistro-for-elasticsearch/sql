package org.nlpcn.es4sql.query.planner.explain;

import java.util.Map;

/**
 * Explanation format
 */
public interface ExplanationFormat {

    /**
     * Initialize internal data structure
     * @param kvs   key-value pairs
     */
    void prepare(Map<String, String> kvs);

    /**
     * Start a new section in explanation.
     * @param name  section name
     */
    void start(String name);


    /**
     * Explain and add to current section.
     * @param object    object to be added to explanation
     */
    void explain(Object object);


    /**
     * End current section.
     */
    void end();

}
