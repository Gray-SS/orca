package com.orca.compiler.core.semantics;

import java.util.List;

import javax.annotation.Nullable;

import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.Conversions;
import com.orca.compiler.core.typesystem.LangType;

public final class OverloadResolver {

    public static boolean isApplicable(CallableSymbol candidate, List<LangType> argumentTypes) {
        return Conversions.cost(candidate.type().parameterTypes(), argumentTypes) != Conversions.INCOMPATIBLE_COST;
    }

    public static LookupResult<CallableSymbol> resolveMatchingOverload(List<? extends CallableSymbol> candidates, List<LangType> argumentTypes) {
        return resolveMatchingOverload(candidates, argumentTypes, null);
    }

    public static LookupResult<CallableSymbol> resolveMatchingOverload(List<? extends CallableSymbol> candidates, List<LangType> argumentTypes, @Nullable LangType expectedReturnType) {
        int minimalCost = Conversions.INCOMPATIBLE_COST;
        List<CallableSymbol> foundCandidates = new java.util.ArrayList<>();

        for (CallableSymbol candidate : candidates) {
            var parameterTypes = candidate.type().parameterTypes();
            var parameters = candidate.parameters();
            var isVariadic = !parameters.isEmpty() && parameters.getLast().isVarargs();

            int cost = isVariadic
                    ? calculateVariadicCost(parameterTypes, argumentTypes)
                    : calculateNonVariadicCost(parameterTypes, argumentTypes);

            if (cost == Conversions.INCOMPATIBLE_COST) {
                continue;
            }

            if (cost < minimalCost) {
                minimalCost = cost;
                foundCandidates.clear();
                foundCandidates.add(candidate);
            } else if (cost == minimalCost) {
                foundCandidates.add(candidate);
            }
        }

        if (expectedReturnType != null && foundCandidates.size() > 1) {
            var returnTypeFiltered = foundCandidates.stream()
                    .filter(candidate -> candidate.returnType().isAssignableTo(expectedReturnType))
                    .toList();

            if (returnTypeFiltered.size() == 1) {
                return LookupResult.singleMatch(returnTypeFiltered.get(0));
            }

            if (!returnTypeFiltered.isEmpty()) {
                foundCandidates = new java.util.ArrayList<>(returnTypeFiltered);
            }
        }

        return switch (foundCandidates.size()) {
            case 0 ->
                LookupResult.noMatch();
            case 1 ->
                LookupResult.singleMatch(foundCandidates.get(0));
            default ->
                LookupResult.ambiguousMatch(foundCandidates);
        };
    }

    private static int calculateVariadicCost(List<LangType> parameterTypes, List<LangType> argumentTypes) {
        if (argumentTypes.size() < parameterTypes.size() - 1) {
            return Conversions.INCOMPATIBLE_COST;
        }

        int totalCost = 500; // Base cost for variadic match (prefer non-variadic matches over variadic ones)

        for (int i = 0; i < parameterTypes.size() - 1; i++) {
            var paramType = parameterTypes.get(i);
            var argType = argumentTypes.get(i);

            int cost = Conversions.cost(paramType, argType);
            if (cost == Conversions.INCOMPATIBLE_COST) {
                return Conversions.INCOMPATIBLE_COST;
            }

            totalCost += cost;
        }

        var variadicParamType = parameterTypes.get(parameterTypes.size() - 1);
        var variadicElementType = ((ArrayType) variadicParamType).getElementType();

        for (int i = parameterTypes.size() - 1; i < argumentTypes.size(); i++) {
            var argType = argumentTypes.get(i);

            int cost = Conversions.cost(variadicElementType, argType);
            if (cost == Conversions.INCOMPATIBLE_COST) {
                return Conversions.INCOMPATIBLE_COST;
            }

            totalCost += cost;
        }

        return totalCost;
    }

    private static int calculateNonVariadicCost(List<LangType> parameterTypes, List<LangType> argumentTypes) {
        if (parameterTypes.size() != argumentTypes.size()) {
            return Conversions.INCOMPATIBLE_COST;
        }

        int totalCost = 0;

        for (int i = 0; i < parameterTypes.size(); i++) {
            var paramType = parameterTypes.get(i);
            var argType = argumentTypes.get(i);

            int cost = Conversions.cost(paramType, argType);
            if (cost == Conversions.INCOMPATIBLE_COST) {
                return Conversions.INCOMPATIBLE_COST;
            }

            totalCost += cost;
        }

        return totalCost;
    }
}
