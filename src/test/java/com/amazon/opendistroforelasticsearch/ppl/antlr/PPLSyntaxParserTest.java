package com.amazon.opendistroforelasticsearch.ppl.antlr;

import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;


public class PPLSyntaxParserTest {

    @Test
    public void testSearchCommandShouldSuccess() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source='t' a=1 b=2");
        assertNotEquals(null, tree);
    }

    @Test
    public void testSearchCommandWithoutSourceShouldFail() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search a=1");
        assertNotEquals(null, tree);
    }
}
