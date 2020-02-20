package com.amazon.opendistroforelasticsearch.sql.executor.cursor;

public class JoinCursorContext implements CursorContext {

    private static final CursorType cursorType = CursorType.JOIN;

    public CursorType getCursorType() {
        return cursorType;
    }
}
