package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

public interface Visitor<T> {

    T visit(T plan);
}
