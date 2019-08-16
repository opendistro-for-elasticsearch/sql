package com.amazon.opendistroforelasticsearch.sql.antlr.semantic;

import com.amazon.opendistroforelasticsearch.sql.antlr.StringSimilarity;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.BinaryComparasionPredicateContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.DecimalLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.FullColumnNameContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.ScalarFunctionCallContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.StringLiteralContext;
import static com.amazon.opendistroforelasticsearch.sql.antlr.parser.OpenDistroSqlParser.TableNameContext;
import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.FieldMappings;
import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.IndexMappings;
import static com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState.state;

/**
 *  Semantic analysis
 */
public class OpenDistroSemanticAnalyzer extends OpenDistroSqlParserBaseVisitor<Attribute> {

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
        if (indexMappings == null) { // ES API throws its own IndexNotFoundException before this
            throw new VerificationException(StringUtils.format("Index name or pattern [%s] doesn't match any existing index", indexName));
        }

        FieldMappings mappings = indexMappings.firstMapping().firstMapping();
        typesBySymbol.define("", mappings);
        return Attribute.EMPTY;
    }

    @Override
    public Attribute visitFullColumnName(FullColumnNameContext ctx) {
        final String fieldName = ctx.uid().simpleId().ID().getText();
        FieldMappings fieldMappings = typesBySymbol.resolve("");
        Map<String, Object> mappings = fieldMappings.mapping(fieldName);
        if (mappings == null) {
            List<String> suggestedWords = new StringSimilarity(fieldMappings.allNames()).similarTo(fieldName);
            throw new VerificationException(StringUtils.format(
                "Field [%s] cannot be found. Did you mean [%s]?", fieldName, String.join(", ", suggestedWords)));
        }
        return new Attribute((String) mappings.get("type"));
    }

    // This check should be able to accomplish in grammar
    @Override
    public Attribute visitScalarFunctionCall(ScalarFunctionCallContext ctx) {
        if (ctx.scalarFunctionName().functionNameBase().ABS() != null) {
            Attribute argAttribute = visit(ctx.functionArgs());
            if (!argAttribute.isNumber()) {
                throw new VerificationException(StringUtils.format(
                    "Function ABS can only work with number instead of %s. Usage ABS(number)", argAttribute));
            }
            return argAttribute;
        }
        return super.visitScalarFunctionCall(ctx);
    }

    // Better semantic check example for overloading operator '='
    @Override
    public Attribute visitBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
        String op = ctx.comparisonOperator().getText();
        if ("=".equals(op) || "<".equals(op) || ">".equals(op)) {
            Attribute leftAttr = visit(ctx.predicate(0));
            Attribute rightAttr = visit(ctx.predicate(1));
            if (!leftAttr.isCompatible(rightAttr)) {
                throw new VerificationException(StringUtils.format(
                    "Type of left side %s and right side %s are not compatible for operator ['%s'].", leftAttr, rightAttr, op));
            }
            return leftAttr;
        }
        return super.visitBinaryComparasionPredicate(ctx);
    }

    @Override
    public Attribute visitStringLiteral(StringLiteralContext ctx) {
        return new Attribute("text");
    }

    @Override
    public Attribute visitDecimalLiteral(DecimalLiteralContext ctx) {
        return new Attribute("long");
    }
}
