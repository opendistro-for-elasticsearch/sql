package com.amazon.opendistroforelasticsearch.sql.unittest.bugs;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;

import java.sql.SQLFeatureNotSupportedException;

/**
 * Special cases for queries that had broken our parser in past.
 */
public class BuggedQueries {

    @Test(expected = ParserException.class)

    public void missingWhereAndFieldName() throws SQLFeatureNotSupportedException, SqlParseException {
        ESActionFactory.create(Mockito.mock(Client.class), "select * from products like 'CompassPoint*' limit 10");
    }
}
