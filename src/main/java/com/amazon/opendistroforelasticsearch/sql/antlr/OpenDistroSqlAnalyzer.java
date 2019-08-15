package com.amazon.opendistroforelasticsearch.sql.antlr;

import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlLexer;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableNameContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.StringDistance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.FieldMappings;
import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.IndexMappings;
import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.state;

/**
 * Facade for ANTLR generated parser to avoid boilerplate code.
 */
public class OpenDistroSqlAnalyzer {

    /**
     * Generate parse tree for the query to perform syntax and semantic analysis.
     * Runtime exception with clear message is thrown for any verification error.
     *
     * @param sql   original query
     */
    public void analyze(String sql) {
        analyzeSemantic(
            analyzeSyntax(
                createParser(
                    createLexer(sql))));
    }

    private OpenDistroSqlParser createParser(Lexer lexer) {
        return new OpenDistroSqlParser(
                   new CommonTokenStream(lexer));
    }

    private OpenDistroSqlLexer createLexer(String sql) {
         return new OpenDistroSqlLexer(
                    new CaseChangingCharStream(
                        CharStreams.fromString(sql), true));
    }

    private ParseTree analyzeSyntax(OpenDistroSqlParser parser) {
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
                throw new VerificationException(StringUtils.format(
                    "ANTLR parser failed due to syntax error by offending symbol [%s] at position [%d]: ", offendingSymbol, charPositionInLine));
            }
        });
        return parser.root();
    }

    private void analyzeSemantic(ParseTree tree) {
        tree.accept(new OpenDistroSqlParserBaseVisitor<Attribute>() {
            private final SymbolTable<String, FieldMappings> typesBySymbol = new SymbolTable<>();

            @Override
            public Attribute visitQuerySpecification(QuerySpecificationContext ctx) {
                // Always visit FROM clause first to define symbols
                visit(ctx.fromClause());

                for (int i = 0; i < ctx.getChildCount(); i++) {
                    if (ctx.getChild(i) != ctx.fromClause()) {
                        visit(ctx.getChild(i));
                    }
                }
                return Attribute.EMPTY;
            }

            @Override
            public Attribute visitTableName(TableNameContext ctx) {
                String indexName = ctx.fullId().uid(0).simpleId().ID().getSymbol().getText();
                IndexMappings indexMappings = state().getFieldMappings(new String[]{indexName});
                if (indexMappings == null) {
                    throw new VerificationException(StringUtils.format("Index name or pattern [%s] doesn't match any existing index", indexName));
                }

                FieldMappings mappings = indexMappings.firstMapping().firstMapping();
                typesBySymbol.define("", mappings);
                return Attribute.EMPTY;
            }

            @Override
            public Attribute visitFullColumnName(OpenDistroSqlParser.FullColumnNameContext ctx) {
                final String fieldName = ctx.uid().simpleId().ID().getText();
                FieldMappings fieldMappings = typesBySymbol.resolve("");
                Map<String, Object> mappings = fieldMappings.mapping(fieldName);
                if (mappings == null) {
                    List<String> suggestedWords = new StringSimilarity(fieldMappings.allNames()).similarTo(fieldName);
                    throw new VerificationException(StringUtils.format(
                        "Field [%s] cannot be found. Did you mean [%s]?", fieldName, String.join(", ", suggestedWords)));
                }
                return Attribute.EMPTY;
            }
        });
    }


    /**
     * Custom stream to convert to upper/lower case before sending to lexer.
     * https://github.com/antlr/antlr4/blob/master/doc/case-insensitive-lexing.md#custom-character-streams-approach
     * https://github.com/parrt/antlr4/blob/case-insensitivity-doc/doc/resources/CaseChangingCharStream.java
     */
    private static class CaseChangingCharStream implements CharStream {

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


    /**
     * Attribute on tree
     */
    private static class Attribute {
        public static final Attribute EMPTY = new Attribute("");

        private final String type;

        private Attribute(String type) {
            this.type = type;
        }

    }


    /**
     * Symbol table for CSA (context-sensitive analysis)
     * @param <Symbol>  type of symbol
     * @param <Value>   type of information associated with symbol
     */
    private static class SymbolTable<Symbol, Value> {
        private final List<SymbolTable<Symbol, Value>> children = new ArrayList<>();
        private final Map<Symbol, Value> valuesBySymbol = new HashMap<>();

        public void define(Symbol symbol, Value value) {
            valuesBySymbol.put(symbol, value);
        }

        public Value resolve(Symbol symbol) {
            return valuesBySymbol.get(symbol); //Ignore multiple levels/scopes
        }
    }


    /**
     * String similarity for similar string(s) computation
     */
    public static class StringSimilarity {
        private final StringDistance algorithm = new LevenshteinDistance();
        private final Collection<String> candidates;

        public StringSimilarity(Collection<String> candidates) {
            this.candidates = Collections.unmodifiableCollection(candidates);
        }

        /**
         * Calculate similarity distance between target and candidate strings.
         * @param target string to match
         * @return       one or more most similar strings
         */
        public List<String> similarTo(String target) {
            float max = 0;
            String result = target; // get only one for now
            for (String name : candidates) {
                float dist = algorithm.getDistance(target, name);
                if (dist > max) {
                    result = name;
                    max = dist;
                }
            }
            return Arrays.asList(result);
        }
    }

}
