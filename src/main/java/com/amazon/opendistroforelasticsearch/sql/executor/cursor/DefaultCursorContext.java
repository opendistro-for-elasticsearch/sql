package com.amazon.opendistroforelasticsearch.sql.executor.cursor;

public class DefaultCursorContext implements CursorContext {

    private static final CursorType cursorType = CursorType.DEFAULT;

    public CursorType getCursorType() {
        return cursorType;
    }
}