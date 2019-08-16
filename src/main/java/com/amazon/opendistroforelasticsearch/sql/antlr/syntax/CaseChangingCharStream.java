package com.amazon.opendistroforelasticsearch.sql.antlr.syntax;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.misc.Interval;

/**
 * Custom stream to convert to upper/lower case before sending to lexer.
 * https://github.com/antlr/antlr4/blob/master/doc/case-insensitive-lexing.md#custom-character-streams-approach
 * https://github.com/parrt/antlr4/blob/case-insensitivity-doc/doc/resources/CaseChangingCharStream.java
 */
public class CaseChangingCharStream implements CharStream {

    final CharStream stream;
    final boolean upper;

    /**
     * Constructs a new CaseChangingCharStream wrapping the given {@link CharStream} forcing
     * all characters to upper case or lower case.
     * @param stream The stream to wrap.
     * @param upper If true force each symbol to upper case, otherwise force to lower.
     */
    public CaseChangingCharStream(CharStream stream, boolean upper) {
        this.stream = stream;
        this.upper = upper;
    }

    @Override
    public String getText(Interval interval) {
        return stream.getText(interval);
    }

    @Override
    public void consume() {
        stream.consume();
    }

    @Override
    public int LA(int i) {
        int c = stream.LA(i);
        if (c <= 0) {
            return c;
        }
        if (upper) {
            return Character.toUpperCase(c);
        }
        return Character.toLowerCase(c);
    }

    @Override
    public int mark() {
        return stream.mark();
    }

    @Override
    public void release(int marker) {
        stream.release(marker);
    }

    @Override
    public int index() {
        return stream.index();
    }

    @Override
    public void seek(int index) {
        stream.seek(index);
    }

    @Override
    public int size() {
        return stream.size();
    }

    @Override
    public String getSourceName() {
        return stream.getSourceName();
    }
}
