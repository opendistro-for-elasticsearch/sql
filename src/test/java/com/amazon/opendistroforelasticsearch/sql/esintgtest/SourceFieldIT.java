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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

public class SourceFieldIT extends SQLIntegTestCase {

	@Override
	protected void init() throws Exception {
		loadIndex(Index.ACCOUNT);
	}

	@Test
	public void includeTest() throws IOException {
		SearchHits response = query(String.format("SELECT include('*name','*ge'),include('b*'),include('*ddre*'),include('gender') FROM %s/account LIMIT 1000", TEST_INDEX_ACCOUNT));
		for (SearchHit hit : response.getHits()) {
			Set<String> keySet = hit.getSourceAsMap().keySet();
			for (String field : keySet) {
				Assert.assertTrue(field.endsWith("name") || field.endsWith("ge") || field.startsWith("b") || field.contains("ddre") || field.equals("gender"));
			}
		}

	}
	
	@Test
	public void excludeTest() throws IOException {

		SearchHits response = query(String.format("SELECT exclude('*name','*ge'),exclude('b*'),exclude('*ddre*'),exclude('gender') FROM %s/account LIMIT 1000", TEST_INDEX_ACCOUNT));

		for (SearchHit hit : response.getHits()) {
			Set<String> keySet = hit.getSourceAsMap().keySet();
			for (String field : keySet) {
				Assert.assertFalse(field.endsWith("name") || field.endsWith("ge") || field.startsWith("b") || field.contains("ddre") || field.equals("gender"));
			}
		}
	}
	
	@Test
	public void allTest() throws IOException {

		SearchHits response = query(String.format("SELECT exclude('*name','*ge'),include('b*'),exclude('*ddre*'),include('gender') FROM %s/account LIMIT 1000", TEST_INDEX_ACCOUNT));

		for (SearchHit hit : response.getHits()) {
			Set<String> keySet = hit.getSourceAsMap().keySet();
			for (String field : keySet) {
				Assert.assertFalse(field.endsWith("name") || field.endsWith("ge") ||  field.contains("ddre") );
				Assert.assertTrue(field.startsWith("b") || field.equals("gender"));
			}
		}
	}

	private SearchHits query(String query) throws IOException {
		final JSONObject jsonObject = executeQuery(query);

		final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
				NamedXContentRegistry.EMPTY,
				LoggingDeprecationHandler.INSTANCE,
				jsonObject.toString());
		return SearchResponse.fromXContent(parser).getHits();
	}

}
