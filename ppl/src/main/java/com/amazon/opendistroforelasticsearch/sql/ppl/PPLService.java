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
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PPLService {
    private final PPLSyntaxParser parser;

    public void execute(PPLQueryRequest request, ResponseListener<PPLQueryResponse> listener) {
        try {
            parser.analyzeSyntax(request.getRequest());
            listener.onResponse(new PPLQueryResponse());
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
