package com.amazon.opendistroforelasticsearch.sql.expression.function;


import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BuiltinFunctionRepository {
    private final Map<BuiltinFunctionName, FunctionResolver> functionResolverMap = new HashMap<>();

    public void register(FunctionResolver resolver) {
        functionResolverMap.put(resolver.getFunctionName(), resolver);
    }

    public FunctionExpression resolve(String name, List<Expression> expressions) {
        Optional<BuiltinFunctionName> functionName = BuiltinFunctionName.of(name);
        if (functionName.isPresent()) {
            FunctionExpressionBuilder resolvedFunctionBuilder = resolve(new FunctionSignature(functionName.get(),
                    expressions.stream().map(Expression::type).collect(Collectors.toList())));
            return resolvedFunctionBuilder.apply(expressions);
        } else {
            throw new UnsupportedOperationException("unsupported operation");
        }
    }

    public FunctionExpressionBuilder resolve(FunctionSignature functionSignature) {
        return functionResolverMap.get(functionSignature.getBuiltinFunctionName())
                .resolve(functionSignature);
    }
}
