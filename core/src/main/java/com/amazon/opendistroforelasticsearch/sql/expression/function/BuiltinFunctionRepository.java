package com.amazon.opendistroforelasticsearch.sql.expression.function;


import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builtin Function Repository
 */
@RequiredArgsConstructor
public class BuiltinFunctionRepository {
    private final Map<FunctionName, FunctionResolver> functionResolverMap;

    /**
     * Register {@link FunctionResolver} to the Builtin Function Repository.
     *
     * @param resolver {@link FunctionResolver} to be registered
     */
    public void register(FunctionResolver resolver) {
        functionResolverMap.put(resolver.getFunctionName(), resolver);
    }

    /**
     * Compile FunctionExpression
     *
     * @param functionName
     * @param expressions
     * @return
     */
    public FunctionExpression compile(FunctionName functionName, List<Expression> expressions) {
        FunctionExpressionBuilder resolvedFunctionBuilder = resolve(new FunctionSignature(functionName,
                expressions.stream().map(Expression::type).collect(Collectors.toList())));
        return resolvedFunctionBuilder.apply(expressions);
    }

    /**
     * Resolve the {@link FunctionExpressionBuilder} in Builtin Function Repository.
     *
     * @param functionSignature {@link FunctionSignature}
     * @return {@link FunctionExpressionBuilder}
     */
    public FunctionExpressionBuilder resolve(FunctionSignature functionSignature) {
        FunctionName functionName = functionSignature.getFunctionName();
        if (functionResolverMap.containsKey(functionName)) {
            return functionResolverMap.get(functionName).resolve(functionSignature);
        } else {
            throw new ExpressionEvaluationException(
                    String.format("unsupported function name: %s", functionName.getFunctionName()));
        }
    }
}
