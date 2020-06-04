/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.sql.parser.Token;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield.NestedFieldProjection;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.ElasticSqlExprParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.parser.SqlParser;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.maker.QueryMaker;
import com.amazon.opendistroforelasticsearch.sql.legacy.rewriter.nestedfield.NestedFieldRewriter;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.HasFieldWithValue;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class NestedFieldProjectionTest {

    @Test
    public void regression() {
        assertThat(query("SELECT region FROM team"), is(anything()));
        assertThat(query("SELECT region FROM team WHERE nested(employees.age) = 30"), is(anything()));
        assertThat(query("SELECT * FROM team WHERE region = 'US'"), is(anything()));
    }

    @Test
    public void nestedFieldSelectAll() {
        assertThat(
            query("SELECT nested(employees.*) FROM team"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    path("employees"),
                                    innerHits("employees.*")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void nestedFieldInSelect() {
         assertThat(
            query("SELECT nested(employees.firstname) FROM team"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    path("employees"),
                                    innerHits("employees.firstname")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void regularAndNestedFieldInSelect() {
        assertThat(
            query("SELECT region, nested(employees.firstname) FROM team"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    path("employees"),
                                    innerHits("employees.firstname")
                                )
                            )
                        )
                    )
                ),
                fetchSource("region")
            )
        );
    }

    /*
    // Should be integration test
    @Test
    public void nestedFieldInWhereSelectAll() {}
    */

    @Test
    public void nestedFieldInSelectAndWhere() {
        assertThat(
            query("SELECT nested(employees.firstname) " +
                  "  FROM team " +
                  "    WHERE nested(employees.age) = 30"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    path("employees"),
                                    innerHits("employees.firstname")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void regularAndNestedFieldInSelectAndWhere() {
        assertThat(
            query("SELECT region, nested(employees.firstname) " +
                  "  FROM team " +
                  "    WHERE nested(employees.age) = 30"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    innerHits("employees.firstname")
                                )
                            )
                        )
                    )
                ),
                fetchSource("region")
            )
        );
    }

    @Test
    public void multipleSameNestedFields() {
        assertThat(
            query("SELECT nested(employees.firstname), nested(employees.lastname) " +
                "  FROM team " +
                "    WHERE nested(\"employees\", employees.age = 30 AND employees.firstname LIKE 'John')"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                nestedQuery(
                                    path("employees"),
                                    innerHits("employees.firstname", "employees.lastname")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void multipleDifferentNestedFields() {
        assertThat(
            query("SELECT region, nested(employees.firstname), nested(manager.name) " +
                  "  FROM team " +
                  "    WHERE nested(employees.age) = 30 AND nested(manager.age) = 50"),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            must(
                                boolQuery(
                                    must(
                                        nestedQuery(
                                            path("employees"),
                                            innerHits("employees.firstname")
                                        ),
                                        nestedQuery(
                                            path("manager"),
                                            innerHits("manager.name")
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                fetchSource("region")
            )
        );
    }


    @Test
    public void leftJoinWithSelectAll() {
        assertThat(
            query("SELECT * FROM team AS t LEFT JOIN t.projects AS p "),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            should(
                                boolQuery(
                                    mustNot(
                                        nestedQuery(
                                            path("projects")
                                        )
                                    )
                                ),
                                nestedQuery(
                                    path("projects"),
                                    innerHits("projects.*")
                                )
                            )
                        )
                    )
                )
            )
        );
    }

    @Test
    public void leftJoinWithSpecificFields() {
        assertThat(
            query("SELECT t.name, p.name, p.started_year FROM team AS t LEFT JOIN t.projects AS p "),
            source(
                boolQuery(
                    filter(
                        boolQuery(
                            should(
                                boolQuery(
                                    mustNot(
                                        nestedQuery(
                                            path("projects")
                                        )
                                    )
                                ),
                                nestedQuery(
                                    path("projects"),
                                    innerHits("projects.name", "projects.started_year")
                                )
                            )
                        )
                    )
                ),
                fetchSource("name")
            )
        );
    }

    private Matcher<SearchSourceBuilder> source(Matcher<QueryBuilder> queryMatcher) {
        return featureValueOf("query", queryMatcher, SearchSourceBuilder::query);
    }

    private Matcher<SearchSourceBuilder> source(Matcher<QueryBuilder> queryMatcher,
                                                Matcher<FetchSourceContext> fetchSourceMatcher) {
        return allOf(
            featureValueOf("query", queryMatcher, SearchSourceBuilder::query),
            featureValueOf("fetchSource", fetchSourceMatcher, SearchSourceBuilder::fetchSource)
        );
    }

    /** Asserting instanceOf and continue other chained matchers of subclass requires explicity cast */
    @SuppressWarnings("unchecked")
    private Matcher<QueryBuilder> boolQuery(Matcher<BoolQueryBuilder> matcher) {
        return (Matcher) allOf(instanceOf(BoolQueryBuilder.class), matcher);
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    private final Matcher<QueryBuilder> nestedQuery(Matcher<NestedQueryBuilder>... matchers) {
        return (Matcher) both(is(Matchers.<NestedQueryBuilder>instanceOf(NestedQueryBuilder.class))).
                            and(allOf(matchers));
    }

    @SafeVarargs
    private final FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>> filter(Matcher<QueryBuilder>... matchers) {
        return hasClauses("filter", BoolQueryBuilder::filter, matchers);
    }

    @SafeVarargs
    private final FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>> must(Matcher<QueryBuilder>... matchers) {
        return hasClauses("must", BoolQueryBuilder::must, matchers);
    }

    @SafeVarargs
    private final FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>> mustNot(Matcher<QueryBuilder>... matchers) {
        return hasClauses("must_not", BoolQueryBuilder::mustNot, matchers);
    }

    @SafeVarargs
    private final FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>> should(Matcher<QueryBuilder>... matchers) {
        return hasClauses("should", BoolQueryBuilder::should, matchers);
    }

    /**  Hide contains() assertion to simplify */
    @SafeVarargs
    private final FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>> hasClauses(String name,
                                                                                  Function<BoolQueryBuilder, List<QueryBuilder>> func,
                                                                                  Matcher<QueryBuilder>... matchers) {
        return new FeatureMatcher<BoolQueryBuilder, List<QueryBuilder>>(contains(matchers), name, name) {
            @Override
            protected List<QueryBuilder> featureValueOf(BoolQueryBuilder query) {
                return func.apply(query);
            }
        };
    }

    private Matcher<NestedQueryBuilder> path(String expected) {
        return HasFieldWithValue.hasFieldWithValue("path", "path", is(equalTo(expected)));
    }

    /** Skip intermediate property along the path. Hide arrayContaining assertion to simplify. */
    private FeatureMatcher<NestedQueryBuilder, String[]> innerHits(String... expected) {
        return featureValueOf("innerHits",
                              arrayContaining(expected),
                              (nestedQuery -> nestedQuery.innerHit().getFetchSourceContext().includes()));
    }

    @SuppressWarnings("unchecked")
    private Matcher<FetchSourceContext> fetchSource(String... expected) {
        if (expected.length == 0) {
            return anyOf(is(nullValue()),
                         featureValueOf("includes", is(nullValue()), FetchSourceContext::includes),
                         featureValueOf("includes", is(emptyArray()), FetchSourceContext::includes));
        }
        return featureValueOf("includes", contains(expected), fetchSource -> Arrays.asList(fetchSource.includes()));
    }

    private <T, U> FeatureMatcher<T, U> featureValueOf(String name, Matcher<U> subMatcher, Function<T, U> getter) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return getter.apply(actual);
            }
        };
    }

    private SearchSourceBuilder query(String sql) {
        SQLQueryExpr expr = parseSql(sql);
        if (sql.contains("nested")) {
            return translate(expr).source();
        }

        expr = rewrite(expr);
        return translate(expr).source();
    }

    private SearchRequest translate(SQLQueryExpr expr) {
        try {
            Client mockClient = Mockito.mock(Client.class);
            SearchRequestBuilder request = new SearchRequestBuilder(mockClient, SearchAction.INSTANCE);
            Select select = new SqlParser().parseSelect(expr);

            DefaultQueryAction action = new DefaultQueryAction(mockClient, select);
            action.initialize(request);
            action.setFields(select.getFields());

            if (select.getWhere() != null) {
                request.setQuery(QueryMaker.explain(select.getWhere(), select.isQuery));
            }
            new NestedFieldProjection(request).project(select.getFields(), select.getNestedJoinType());
            return request.request();
        }
        catch (SqlParseException e) {
            throw new ParserException("Illegal sql expr: " + expr.toString());
        }
    }

    private SQLQueryExpr parseSql(String sql) {
        ElasticSqlExprParser parser = new ElasticSqlExprParser(sql);
        SQLExpr expr = parser.expr();
        if (parser.getLexer().token() != Token.EOF) {
            throw new ParserException("Illegal sql: " + sql);
        }
        return (SQLQueryExpr) expr;
    }

    private SQLQueryExpr rewrite(SQLQueryExpr expr) {
        expr.accept(new NestedFieldRewriter());
        return expr;
    }
}
