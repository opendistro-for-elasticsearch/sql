package org.nlpcn.es4sql.matchtoterm.rewrite;

import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLInListExpr;
import com.alibaba.druid.sql.ast.expr.SQLInSubQueryExpr;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectOrderByItem;
import com.alibaba.druid.sql.ast.statement.SQLTableSource;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.MySqlSelectGroupByExpr;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlSelectQueryBlock;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse;
import org.elasticsearch.client.Client;
import java.util.Arrays;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.HashMap;


import static org.elasticsearch.action.admin.indices.mapping.get.GetFieldMappingsResponse.FieldMappingMetaData;

/**
 * Visitor to rewrite AST (abstract syntax tree) for supporting term_query in WHERE and IN condition
 * Simple changing the matchQuery() to termQuery() will not work when mapping is both text and keyword
 * The approach is to implement SQLIdentifier.visit() based on the correct field mapping.
 */

public class TermFieldRewriter extends MySqlASTVisitorAdapter {

    private Deque<TermFieldScope> environment = new ArrayDeque<>();
    private Client client ;
    private TermRewriterFilter filterType;

    public TermFieldRewriter(Client client) {
        this.client = client;
        this.filterType = TermRewriterFilter.COMMA;
    }

    public TermFieldRewriter(Client client, TermRewriterFilter filterType) {
        this.client = client;
        this.filterType = filterType;
    }

    @Override
    public boolean visit(MySqlSelectQueryBlock query) {
        environment.push(new TermFieldScope());
        Map<String, String> indexToType = new HashMap<>();
        collect(query.getFrom(), indexToType, curScope().getAliases());
        curScope().setMapper(getMappings(indexToType));
        if ((this.filterType == TermRewriterFilter.COMMA || this.filterType == TermRewriterFilter.MULTI_QUERY)
            && !hasIdenticalMappings(curScope(), indexToType)) {
            throw new VerificationException("When using multiple indices, the mappings must be identical.");
        }
        return true;
    }

    @Override
    public void endVisit(MySqlSelectQueryBlock query) {
        environment.pop();
    }

    @Override
    public boolean visit(SQLSelectItem sqlSelectItem) {
        return false;
    }

    @Override
    public boolean visit(SQLJoinTableSource tableSource) {
        return false;
    }

    @Override
    public boolean visit(SQLExprTableSource tableSource) {
        return false;
    }

    /** Fix null parent problem which is required when visiting SQLIdentifier */
    public boolean visit(SQLInListExpr inListExpr) {
        inListExpr.getExpr().setParent(inListExpr);
        return true;
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean visit(SQLIdentifierExpr expr) {
        if (isValidIdentifierForTerm(expr)) {
            Map<String, Object> source = null;
            if (this.filterType == TermRewriterFilter.COMMA || this.filterType == TermRewriterFilter.MULTI_QUERY) {
                if (curScope().getFinalMapping().containsKey(expr.getName())) {
                    source  = curScope().getFinalMapping().get(expr.getName()).sourceAsMap();
                } else {
                    return true;
                }

            } else if (this.filterType == TermRewriterFilter.JOIN) {
                String[] arr = expr.getName().split("\\.", 2);
                if (arr.length < 2)  throw new VerificationException("table alias or field name missing");
                String alias = arr[0];
                String fullFieldName = arr[1];

                String index = curScope().getAliases().get(alias);
                String type = curScope().getMapper().get(index).entrySet().iterator().next().getKey();
                if (curScope().getMapper().get(index).get(type).containsKey(fullFieldName)) {
                    source = curScope().getMapper().get(index).get(type).get(fullFieldName).sourceAsMap();
                } else {
                    return true;
                }
            }

            String keywordAlias = isBothTextAndKeyword(source);
            if (keywordAlias != null) {
                expr.setName(expr.getName() + "." + keywordAlias);
            }
        }
        return true;
    }

    public void collect(SQLTableSource tableSource, Map<String, String> indexToType,  Map<String, String> aliases) {
        if (tableSource instanceof SQLExprTableSource) {
            String table = null;
            if (((SQLExprTableSource) tableSource).getExpr() instanceof SQLIdentifierExpr) {
                table = ((SQLIdentifierExpr) ((SQLExprTableSource) tableSource).getExpr()).getName();
                indexToType.put(
                    table,
                    null
                );
            } else if (((SQLExprTableSource) tableSource).getExpr() instanceof SQLBinaryOpExpr) {
                table = ((SQLIdentifierExpr)((SQLBinaryOpExpr) ((SQLExprTableSource) tableSource).getExpr()).getLeft()).getName();
                indexToType.put(
                    table,
                    ((SQLIdentifierExpr)((SQLBinaryOpExpr) ((SQLExprTableSource) tableSource).getExpr()).getRight()).getName()
                );
            }
            if (tableSource.getAlias() != null) {
                aliases.put(tableSource.getAlias(), table);
            } else {
                aliases.put(table, table);
            }

        } else if (tableSource instanceof SQLJoinTableSource) {
            collect(((SQLJoinTableSource) tableSource).getLeft(), indexToType, aliases);
            collect(((SQLJoinTableSource) tableSource).getRight(), indexToType, aliases);
        }
    }

    /** Current scope which is top of the stack */
    private TermFieldScope curScope() {
        return environment.peek();
    }

    public String isBothTextAndKeyword(Map<String, Object> source) {
        String innerFieldName = null;
        if (source != null) {
            innerFieldName =  source.keySet().iterator().next();
        } else {
            return null;
        }

        // 1. fieldname in FieldMappingData is not same when type is object
        // 2. source will always have one key which will be the field name

        if (((Map) source.get(innerFieldName)).containsKey("fields")) {
            for (Object key : ((Map) ((Map) source.get(innerFieldName)).get("fields") ).keySet()) {
                if (key instanceof String &&
                    ((Map) ((Map) ((Map) source.get(innerFieldName)).get("fields") ).get(key)).get("type").equals("keyword")) {
                    return (String) key;
                }
            }
        }
        return null;
    }

    public boolean isValidIdentifierForTerm(SQLIdentifierExpr expr) {
        /**
         * Only for following conditions Identifier will be modified
         *  Where:  WHERE identifier = 'something'
         *  IN list: IN ('Tom', 'Dick', ;'Harry')
         *  IN subquery: IN (SELECT firstname from accounts/account where firstname = 'John')
         *  Group by: GROUP BY state , employer , ...
         *  Order by: ORDER BY firstname, lastname , ...
         *
         * NOTE: Does not impact fields on ON condition clause in JOIN as we skip visiting SQLJoinTableSource
         */
        return
            !expr.getName().startsWith("_")
            && (
                (   expr.getParent() instanceof SQLBinaryOpExpr
                    && ((SQLBinaryOpExpr) expr.getParent()).getOperator() == SQLBinaryOperator.Equality
                )
                || expr.getParent() instanceof SQLInListExpr
                || expr.getParent() instanceof SQLInSubQueryExpr
                || expr.getParent() instanceof SQLSelectOrderByItem
                || expr.getParent() instanceof MySqlSelectGroupByExpr
            );
    }

    public boolean hasIdenticalMappings(TermFieldScope scope, Map<String, String> indexToType) {
        if (scope.getMapper().isEmpty()) {
            throw new VerificationException("Unknown index " + indexToType.keySet());
        }

        for (Map<String, Map<String, FieldMappingMetaData>> typeMappings: scope.getMapper().values()) {
            for (Map<String, FieldMappingMetaData> fieldMappings: typeMappings.values()) {
                /**
                 * TODO: check for a better way of comparing two fieldMappings
                 * ES 6.5 does have a equals() method implemented on FieldMappingMetaData which can be used here
                 */
                String[] tempFieldMappingsAsArray = fieldMappings.keySet().stream().toArray(String[]::new);
                Arrays.sort(tempFieldMappingsAsArray);

                if (curScope().getFinalMappingAsArray() == null) {
                    // We need this for comparison
                    curScope().setFinalMappingAsArray(tempFieldMappingsAsArray);
                    // We need this to lookup for rewriting
                    curScope().setFinalMapping(fieldMappings);
                }

                if (!Arrays.equals(curScope().getFinalMappingAsArray(), tempFieldMappingsAsArray)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<String, Map<String, Map<String, FieldMappingMetaData>>> getMappings(Map<String, String> indexToType) {
        String[] allIndexes = indexToType.keySet().stream().toArray(String[]::new);
        // GetFieldMappingsRequest takes care of wildcard index expansion
        GetFieldMappingsRequest request = new GetFieldMappingsRequest().indices(allIndexes).types("*").fields("*").local(true);
        GetFieldMappingsResponse response = client.admin().indices().getFieldMappings(request).actionGet();
        return response.mappings();
    }

    public enum TermRewriterFilter {
        COMMA(","), // No joins, multiple tables in SELECT
        JOIN("JOIN"), // All JOINs
        MULTI_QUERY("MULTI_QUERY"); // MINUS and UNION

        public final String name;

        TermRewriterFilter(String name){
            this.name = name;
        }

        public static String toString(TermRewriterFilter filter) {
            return filter.name;
        }
    }
}
