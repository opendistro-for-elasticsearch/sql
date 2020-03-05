package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TermField {
    private final String fieldName;
    private final Object value;
}
