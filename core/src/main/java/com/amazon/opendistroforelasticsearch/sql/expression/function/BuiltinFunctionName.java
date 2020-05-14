package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;

/**
 * Builtin Function Name
 */
@Getter
@RequiredArgsConstructor
public enum BuiltinFunctionName {
    ADD(FunctionName.of("add")),
    SUBTRACT(FunctionName.of("subtract")),
    MULTIPLY(FunctionName.of("multiply")),
    DIVIDE(FunctionName.of("divide")),
    MODULES(FunctionName.of("modules")),

    AND(FunctionName.of("and")),
    OR(FunctionName.of("or")),
    XOR(FunctionName.of("xor")),
    NOT(FunctionName.of("not")),
    NOTEQUAL(FunctionName.of("notequal")),
    EQUAL(FunctionName.of("equal")),

    TOSTRING(FunctionName.of("tostring")),

    /**
     * Aggregation Function.
     */
    AVG(FunctionName.of("avg")),
    SUM(FunctionName.of("sum"))
    ;

    private final FunctionName name;

    private static final Map<FunctionName, BuiltinFunctionName> ALL_NATIVE_FUNCTIONS;

    static {
        ImmutableMap.Builder<FunctionName, BuiltinFunctionName> builder = new ImmutableMap.Builder<>();
        for (BuiltinFunctionName func : BuiltinFunctionName.values()) {
            builder.put(func.getName(), func);
        }
        ALL_NATIVE_FUNCTIONS = builder.build();
    }

    public static Optional<BuiltinFunctionName> of(String str) {
        return Optional.ofNullable(ALL_NATIVE_FUNCTIONS.getOrDefault(FunctionName.of(str), null));
    }
}
