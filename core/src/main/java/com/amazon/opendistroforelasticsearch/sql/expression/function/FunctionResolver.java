package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
@RequiredArgsConstructor
public class FunctionResolver {
    @Getter
    private final FunctionName functionName;
    @Singular("functionBundle")
    private final Map<FunctionSignature, FunctionExpressionBuilder> functionBundle;

    public FunctionExpressionBuilder resolve(FunctionSignature unresolvedSignature) {
        PriorityQueue<Map.Entry<Integer, FunctionSignature>> functionMatchQueue = new PriorityQueue<>(
                Comparator.comparing(Map.Entry::getKey));

        for (FunctionSignature functionSignature : functionBundle.keySet()) {
            int matchingDegree = unresolvedSignature.match(functionSignature);
            if (matchingDegree == 0) {
                return functionBundle.get(functionSignature);
            } else if (matchingDegree != Integer.MAX_VALUE) {
                functionMatchQueue.add(new AbstractMap.SimpleEntry<>(matchingDegree, functionSignature));
            }
        }

        if (functionMatchQueue.isEmpty()) {
            throw new IllegalStateException(
                    String.format("%s function expected %s, but get %s", functionName,
                            formatFunctions(functionBundle.keySet()),
                            formatTypes(unresolvedSignature.getParamTypeList())
                    ));
        } else {
            return functionBundle.get(functionMatchQueue.peek().getValue());
        }
    }

    private String formatFunctions(Set<FunctionSignature> functionSignatures) {
        return functionSignatures.stream().map(fs -> formatTypes(fs.getParamTypeList()))
                .collect(Collectors.joining(",", "{", "}"));
    }

    private String formatTypes(List<ExprType> types) {
        return types.stream()
                .map(Enum::toString)
                .collect(Collectors.joining(",", "[", "]"));
    }
}
