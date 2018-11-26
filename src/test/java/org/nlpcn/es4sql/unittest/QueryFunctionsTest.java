package org.nlpcn.es4sql.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.AbstractQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.Mockito;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.ESActionFactory;
import org.nlpcn.es4sql.query.QueryAction;

import java.sql.SQLFeatureNotSupportedException;

import static org.elasticsearch.index.query.QueryBuilders.constantScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;
import static org.elasticsearch.index.query.QueryBuilders.multiMatchQuery;
import static org.elasticsearch.index.query.QueryBuilders.nestedQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.wildcardQuery;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_PHRASE;

public class QueryFunctionsTest {

    private static final String SELECT_ALL = "SELECT *";
    private static final String FROM_ACCOUNTS = "FROM " + TEST_INDEX_ACCOUNT + "/account";
    private static final String FROM_NESTED = "FROM " + TEST_INDEX_NESTED_TYPE + "/nestedType";
    private static final String FROM_PHRASE = "FROM " + TEST_INDEX_PHRASE + "/phrase";

    @Test
    public void query() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE QUERY('CA')"
            ),
            contains(
                queryStringQuery("CA")
            )
        );
    }

    @Test
    public void matchQueryRegularField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MATCH_QUERY(firstname, 'Ayers')"
            ),
            contains(
                matchQuery("firstname", "Ayers")
            )
        );
    }

    @Test
    public void matchQueryNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE MATCH_QUERY(NESTED(comment.data), 'aa')"
            ),
            contains(
                nestedQuery("comment", matchQuery("comment.data", "aa"), ScoreMode.None)
            )
        );
    }

    @Test
    public void scoreQuery() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE SCORE(MATCH_QUERY(firstname, 'Ayers'), 10)"
            ),
            contains(
                constantScoreQuery(
                    matchQuery("firstname", "Ayers")
                ).boost(10)
            )
        );
    }

    @Test
    public void scoreQueryWithNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE SCORE(MATCH_QUERY(NESTED(comment.data), 'ab'), 10)"
            ),
            contains(
                constantScoreQuery(
                    nestedQuery("comment", matchQuery("comment.data", "ab"), ScoreMode.None)
                ).boost(10)
            )
        );
    }

    @Test
    public void wildcardQueryRegularField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE WILDCARD_QUERY(city.keyword, 'B*')"
            ),
            contains(
                wildcardQuery("city.keyword", "B*")
            )
        );
    }

    @Test
    public void wildcardQueryNestedField() {
        assertThat(
            query(
                FROM_NESTED,
                "WHERE WILDCARD_QUERY(nested(comment.data), 'a*')"
            ),
            contains(
                nestedQuery("comment", wildcardQuery("comment.data", "a*"), ScoreMode.None)
            )
        );
    }

    @Test
    public void matchPhraseQueryDefault() {
        assertThat(
            query(
                FROM_PHRASE,
                "WHERE MATCH_PHRASE(phrase, 'brown fox')"
            ),
            contains(
                matchPhraseQuery("phrase", "brown fox")
            )
        );
    }

    @Test
    public void matchPhraseQueryWithSlop() {
        assertThat(
            query(
                FROM_PHRASE,
                "WHERE MATCH_PHRASE(phrase, 'brown fox', slop=2)"
            ),
            contains(
                matchPhraseQuery("phrase", "brown fox").slop(2)
            )
        );
    }

    @Test
    public void multiMatchQuerySingleField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Ayers', fields='firstname')"
            ),
            contains(
                multiMatchQuery("Ayers").field("firstname")
            )
        );
    }

    @Test
    public void multiMatchQueryWildcardField() {
        assertThat(
            query(
                FROM_ACCOUNTS,
                "WHERE MULTI_MATCH(query='Ay', fields='*name', type='phrase_prefix')"
            ),
            contains(
                multiMatchQuery("Ay").
                                field("*name").
                                type(MultiMatchQueryBuilder.Type.PHRASE_PREFIX)
            )
        );
    }

    private String query(String from, String... statements) {
        return explain(SELECT_ALL + " " + from + " " + String.join(" ", statements));
    }

    private String explain(String sql) {
        try {
            Client mockClient = Mockito.mock(Client.class);
            QueryAction queryAction = ESActionFactory.create(mockClient, sql);

            return queryAction.explain().explain();
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in: " + sql);
        }
    }

    private Matcher<String> contains(AbstractQueryBuilder queryBuilder) {
        return containsString(Strings.toString(queryBuilder, false, false));
    }
}
