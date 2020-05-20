/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.ppl.config.PPLServiceConfig;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryResponse;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class PPLServiceTest {
    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
            PPLServiceConfig.class);
    private PPLService pplService = context.getBean(PPLService.class);
    @Test
    public void testExecuteShouldPass() {
        pplService.execute(new PPLQueryRequest("search source=t a=1", null), new ResponseListener<PPLQueryResponse>() {
            @Override
            public void onResponse(PPLQueryResponse pplQueryResponse) {

            }

            @Override
            public void onFailure(Exception e) {
                Assert.fail();
            }
        });
    }

    @Test
    public void testExecuteWithIllegalQueryShouldBeCaughtByHandler() {
        pplService.execute(new PPLQueryRequest("search", null), new ResponseListener<PPLQueryResponse>() {
            @Override
            public void onResponse(PPLQueryResponse pplQueryResponse) {
                Assert.fail();
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
    }
}