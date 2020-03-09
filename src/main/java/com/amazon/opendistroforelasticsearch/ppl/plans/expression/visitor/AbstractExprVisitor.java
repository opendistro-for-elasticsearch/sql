/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.ppl.plans.expression.visitor;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.And;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.AttributeReference;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.EqualTo;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Or;

public class AbstractExprVisitor<T> implements ExprVisitor<T> {

    public T visitLiteral(Literal node) {
        return visitChildren(node);
    }

    public T visitAttributeReference(AttributeReference node) {
        return visitChildren(node);
    }

    public T visitEqualTo(EqualTo node) {
        return visitChildren(node);
    }

    public T visitAnd(And node) {
        return visitChildren(node);
    }

    public T visitOr(Or node) {
        return visitOr(node);
    }
}
