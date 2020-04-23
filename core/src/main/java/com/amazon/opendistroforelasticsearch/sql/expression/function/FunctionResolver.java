package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.AbstractMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The Function Resolver hold the overload {@link FunctionExpressionBuilder} implementation.
 * is composed by {@link FunctionName} which identified the function name
 * and a map of {@link FunctionSignature} and {@link FunctionExpressionBuilder}
 * to represent the overloaded implementation
 */
@Builder
@RequiredArgsConstructor
public class FunctionResolver {
    @Getter
    private final FunctionName functionName;
    @Singular("functionBundle")
    private final Map<FunctionSignature, FunctionExpressionBuilder> functionBundle;

    /**
     * Resolve the {@link FunctionExpressionBuilder} by using input {@link FunctionSignature}.
     * If found the {@link FunctionExpressionBuilder} exactly match the input {@link FunctionSignature}, return it.
     * If applying the widening rule, found the most match one, return it.
     * If nothing found, throw {@link ExpressionEvaluationException}
     */
    public FunctionExpressionBuilder resolve(FunctionSignature unresolvedSignature) {
        PriorityQueue<Map.Entry<Integer, FunctionSignature>> functionMatchQueue = new PriorityQueue<>(
                Map.Entry.comparingByKey());

        for (FunctionSignature functionSignature : functionBundle.keySet()) {
            functionMatchQueue.add(
                    new AbstractMap.SimpleEntry<>(unresolvedSignature.match(functionSignature), functionSignature));
        }
        Map.Entry<Integer, FunctionSignature> bestMatchEntry = functionMatchQueue.peek();
        if (FunctionSignature.NOT_MATCH.equals(bestMatchEntry.getKey())) {
            throw new ExpressionEvaluationException(
                    String.format("%s function expected %s, but get %s", functionName,
                            formatFunctions(functionBundle.keySet()),
                            unresolvedSignature.formatTypes()
                    ));
        } else {
            return functionBundle.get(bestMatchEntry.getValue());
        }
    }

    private String formatFunctions(Set<FunctionSignature> functionSignatures) {
        return functionSignatures.stream().map(FunctionSignature::formatTypes)
                .collect(Collectors.joining(",", "{", "}"));
    }
}
