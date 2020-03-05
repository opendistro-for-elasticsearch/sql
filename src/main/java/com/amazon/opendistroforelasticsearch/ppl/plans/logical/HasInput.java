package com.amazon.opendistroforelasticsearch.ppl.plans.logical;

public interface HasInput<T, R> {
    R withInput(T input);
}
