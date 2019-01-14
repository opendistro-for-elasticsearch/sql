package org.nlpcn.es4sql;

import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.junit.Assert;
import org.junit.Test;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticRequestBuilder;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;

import static org.nlpcn.es4sql.TestsConstants.*;

/**
 * Created by Eliran on 16/10/2015.
 */
public class ShowTest {

    @Test
    public void showAll_atLeastOneIndexReturns() throws SqlParseException, SQLFeatureNotSupportedException {
        String query = "SHOW TABLES LIKE %";
        GetIndexResponse getIndexResponse = runShowQuery(query);
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getIndexResponse.getMappings();
        Assert.assertTrue(mappings.size() >= 1);

    }

    /**
     * The following test will fail because the new SHOW query supports the SQL match character underscore (_) but
     * this filtering happens in logic accessed through PrettyFormatRestExecutor and these tests are using old logic.
     *
     * Since TEST_INDEX_ACCOUNT contains underscores, prepareIndexRequestBuilder() just creates a request returning all
     * indices (which isn't filtered due to the reason mentioned above).
     *
     * Commenting out this test for posterity as it explains the behavior of this edge case in the GUI.
     */
//    @Test
//    public void showIndex_onlyOneIndexReturn() throws SqlParseException, SQLFeatureNotSupportedException {
//        String query = "SHOW TABLES LIKE "+ TEST_INDEX_ACCOUNT;
//        GetIndexResponse getIndexResponse = runShowQuery(query);
//        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getIndexResponse.getMappings();
//        Assert.assertEquals(1, mappings.size());
//        Assert.assertTrue(mappings.containsKey(TEST_INDEX_ACCOUNT));
//    }

    @Test
    public void showIndex_onlyOneIndexReturWithMoreThanOneTypes() throws SqlParseException, SQLFeatureNotSupportedException {
        String query = "SHOW TABLES LIKE " + TEST_INDEX + "%";
        GetIndexResponse getIndexResponse = runShowQuery(query);
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getIndexResponse.getMappings();
        Assert.assertTrue(mappings.size()>1);
    }

    /**
     * As the plan is to support ES 6.x, the update to SHOW/DESCRIBE has removed the parsing of index type since there
     * is only one type per index in ES 6.x
     *
     * Commenting out this test for posterity.
     */
//    @Test
//    public void showIndexType_onlyOneTypeReturn() throws SqlParseException, SQLFeatureNotSupportedException, IOException {
//        String query = String.format("show %s/account", TEST_INDEX_ACCOUNT);
//        GetIndexResponse getIndexResponse = runShowQuery(query);
//        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings = getIndexResponse.getMappings();
//        ImmutableOpenMap<String, MappingMetaData> typeToData = mappings.get(TEST_INDEX_ACCOUNT);
//        Assert.assertEquals(1,typeToData.size());
//    }

    private GetIndexResponse runShowQuery(String query) throws SqlParseException, SQLFeatureNotSupportedException {
        SearchDao searchDao = MainTestSuite.getSearchDao();
        SqlElasticRequestBuilder requestBuilder =  searchDao.explain(query).explain();
        return (GetIndexResponse) requestBuilder.get();
    }
}
