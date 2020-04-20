package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class FunctionSignature {
    private final FunctionName functionName;
    private final List<ExprType> paramTypeList;

    /**
     * The small number means good match.
     *
     * @return 0: exactly match, Integer.MAX: not match
     */
    public int match(FunctionSignature functionSignature) {
        List<ExprType> functionTypeList = functionSignature.paramTypeList;
        if (paramTypeList.size() != functionTypeList.size()) {
            throw new UnsupportedOperationException(
                    String.format("%s expression %d argument, but actually are %d",
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
