package com.amazon.opendistroforelasticsearch.sql.executor.cursor;

public class AggregationCursorContext implements CursorContext{

    private static final CursorType cursorType = CursorType.AGGREGATION;

    public CursorType getCursorType() {
        return cursorType;
    }
}
