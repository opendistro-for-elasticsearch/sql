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

package com.amazon.opendistro.sql.intgtest;

import com.amazon.opendistro.sql.exception.SqlParseException;
import org.junit.Test;
import com.amazon.opendistro.sql.query.SqlElasticSearchRequestBuilder;

import java.io.IOException;
import java.sql.SQLFeatureNotSupportedException;

/**
 * 定製方法查詢．
 * @author ansj
 *
 */
public class MethodQueryTest {

	/**
	 * query 搜索就是　，　lucene 原生的搜素方式 注意这个例子中ｖａｌｕｅ可以随便命名 "query" :
	 * {query_string" : {"query" : "address:880 Holmes Lane"}
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void queryTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) MainTestSuite.getSearchDao().explain("select address from elasticsearch-sql_test_index_account where q= query('address:880 Holmes Lane') limit 3").explain();
		System.out.println(select);
	}

	/**
	 * matchQuery 是利用分词结果进行单个字段的搜索． "query" : { "match" : { "address" :
	 * {"query":"880 Holmes Lane", "type" : "boolean" } } }
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void matchQueryTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) MainTestSuite.getSearchDao()
			.explain("select address from elasticsearch-sql_test_index_account where address= matchQuery('880 Holmes Lane') limit 3")
			.explain();
		System.out.println(select);
	}

	/**
	 * matchQuery 是利用分词结果进行单个字段的搜索． "query" : { "bool" : { "must" : { "bool" : {
	 * "should" : [ { "constant_score" : { "query" : { "match" : { "address" : {
	 * "query" : "Lane", "type" : "boolean" } } }, "boost" : 100.0 } }, {
	 * "constant_score" : { "query" : { "match" : { "address" : { "query" :
	 * "Street", "type" : "boolean" } } }, "boost" : 0.5 } } ] } } } }
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void scoreQueryTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) MainTestSuite.getSearchDao()
			.explain("select address from elasticsearch-sql_test_index_account where address= score(matchQuery('Lane'),100) or address= score(matchQuery('Street'),0.5)  order by _score desc limit 3")
			.explain();
		System.out.println(select);
	}

	/**
	 * wildcardQuery 是用通配符的方式查找某个term 　比如例子中 l*e means leae ltae ....
	 * "wildcard": { "address" : { "wildcard" : "l*e" } }
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void wildcardQueryTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) MainTestSuite.getSearchDao()
			.explain("select address from elasticsearch-sql_test_index_account where address= wildcardQuery('l*e')  order by _score desc limit 3")
			.explain();
		System.out.println(select);
	}
	
	
	/**
	 * matchPhraseQueryTest 短语查询完全匹配．
	 * "address" : {
                "query" : "671 Bristol Street",
                "type" : "phrase"
              }
	 * 
	 * @throws IOException
	 * @throws SqlParseException
	 */
	@Test
	public void matchPhraseQueryTest() throws IOException, SqlParseException, SQLFeatureNotSupportedException {
        SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) MainTestSuite.getSearchDao()
			.explain("select address from elasticsearch-sql_test_index_account where address= matchPhrase('671 Bristol Street')  order by _score desc limit 3")
			.explain();

		System.out.println(select);
	}
}
