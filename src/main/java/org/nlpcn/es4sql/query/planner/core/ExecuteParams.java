package org.nlpcn.es4sql.query.planner.core;

import java.util.EnumMap;

/**
 * Parameters needed for physical operator execution.
 */
public class ExecuteParams {

    /** Mapping from type to parameters */
    private EnumMap<ExecuteParamType, Object> params = new EnumMap<>(ExecuteParamType.class);

    public <T> void add(ExecuteParamType type, T param) {
        params.put(type, param);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(ExecuteParamType type) {
        return (T) params.get(type);
    }

    public enum ExecuteParamType {
        CLIENT, RESOURCE_MANAGER, EXTRA_QUERY_FILTER, TIMEOUT
    }

}
