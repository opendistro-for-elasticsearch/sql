package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * Function signature is composed by function name and arguments list.
 */
@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class FunctionSignature {
    private final FunctionName functionName;
    private final List<ExprType> paramTypeList;

    /**
     * Check the function signature match or not.
     *
     * @return 0: exactly match, Integer.MAX: not match, by widening rule small number means better match.
     */
    public int match(FunctionSignature functionSignature) {
        if (!functionName.equals(functionSignature.getFunctionName())) {
            throw new ExpressionEvaluationException(
                    String.format("expression name: %s and %s doesn't match",
                            functionName,
                            functionSignature.getFunctionName()));
        }
        List<ExprType> functionTypeList = functionSignature.getParamTypeList();
        if (paramTypeList.size() != functionTypeList.size()) {
            throw new ExpressionEvaluationException(
                    String.format("%s expression expected %d argument, but actually are %d",
                            functionName,
                            paramTypeList.size(),
                            functionTypeList.size()));
        }
        int matchDegree = 0;
        for (int i = 0; i < paramTypeList.size(); i++) {
            ExprType paramType = paramTypeList.get(i);
            ExprType funcType = functionTypeList.get(i);
            int match = WideningTypeRule.distance(paramType, funcType);
            if (match == Integer.MAX_VALUE) {
                return match;
            } else {
                matchDegree += match;
            }
        }
        return matchDegree;
    }
}
