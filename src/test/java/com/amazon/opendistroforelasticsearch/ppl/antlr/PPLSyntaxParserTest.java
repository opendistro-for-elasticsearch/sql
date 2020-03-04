package com.amazon.opendistroforelasticsearch.ppl.antlr;

import com.amazon.opendistroforelasticsearch.sql.antlr.syntax.SyntaxAnalysisException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertNotEquals;


public class PPLSyntaxParserTest {
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testSearchCommandShouldSuccess() {
        ParseTree tree = new PPLSyntaxParser().analyzeSyntax("search source=t a=1 b=2");
        assertNotEquals(null, tree);
    }

    @Test
    public void testSearchCommandWithoutSourceShouldFail() {
        exceptionRule.expect(SyntaxAnalysisException.class);
        exceptionRule.expectMessage("Failed to parse query due to offending symbol");

        new PPLSyntaxParser().analyzeSyntax("search a=1");
    }
}
