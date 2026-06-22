package com.orca.compiler.core.boundtree.expressions;

import java.util.ArrayList;
import java.util.List;

import com.orca.compiler.core.semantics.LookupResult;
import com.orca.compiler.core.syntax.expressions.AssignmentOperatorKind;
import com.orca.compiler.core.syntax.expressions.BinaryOperatorKind;
import com.orca.compiler.core.syntax.expressions.UnaryOperatorKind;
import com.orca.compiler.core.typesystem.Conversions;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundOperators {

    private BoundOperators() {
    }

    private static final List<BoundOperator.Assignment> assignmentOperators = initializeAssignmentOperators();
    private static final List<BoundOperator.Binary> binaryOperators = initializeBinaryOperators();
    private static final List<BoundOperator.Unary> unaryOperators = initializeUnaryOperators();

    public static BoundOperator.Binary bindBinaryOperatorOrThrow(BinaryOperatorKind operatorKind, LangType leftType, LangType rightType) {
        var result = bindBinaryOperator(operatorKind, leftType, rightType);
        if (result.isSingleMatch()) {
            return result.getSingle();
        }

        throw new IllegalArgumentException("Binary operator '" + operatorKind.getOperatorText() + "' is not defined for types '" + leftType + "' and '" + rightType + "'");
    }

    public static LookupResult<BoundOperator.Binary> bindBinaryOperator(BinaryOperatorKind operatorKind, LangType leftType, LangType rightType) {
        return bind(binaryOperators, operatorKind, List.of(leftType, rightType));
    }

    public static BoundOperator.Unary bindUnaryOperatorOrThrow(UnaryOperatorKind operatorKind, LangType operandType) {
        var result = bindUnaryOperator(operatorKind, operandType);
        if (result.isSingleMatch()) {
            return result.getSingle();
        }

        throw new IllegalArgumentException("Unary operator '" + operatorKind.getOperatorText() + "' is not defined for type '" + operandType + "'");
    }

    public static LookupResult<BoundOperator.Unary> bindUnaryOperator(UnaryOperatorKind operatorKind, LangType operandType) {
        return bind(unaryOperators, operatorKind, List.of(operandType));
    }

    public static BoundOperator.Assignment bindAssignmentOperatorOrThrow(AssignmentOperatorKind operatorKind, LangType leftType, LangType rightType) {
        var result = bindAssignmentOperator(operatorKind, leftType, rightType);
        if (result.isSingleMatch()) {
            return result.getSingle();
        }

        throw new IllegalArgumentException("Assignment operator '" + operatorKind.getOperatorText() + "' is not defined for types '" + leftType + "' and '" + rightType + "'");
    }

    public static LookupResult<BoundOperator.Assignment> bindAssignmentOperator(AssignmentOperatorKind operatorKind, LangType leftType, LangType rightType) {
        return bind(assignmentOperators, operatorKind, List.of(leftType, rightType));
    }

    private static <T extends BoundOperator> LookupResult<T> bind(List<T> operators, Enum<?> operatorKind, List<LangType> argumentTypes) {
        int minimalCost = Conversions.INCOMPATIBLE_COST;
        List<T> foundCandidates = new java.util.ArrayList<>();

        for (T candidate : operators) {
            if (!candidate.kind().equals(operatorKind)) {
                continue; // Operator text mismatch, skip candidate
            }

            var operandTypes = candidate.operandTypes();
            if (operandTypes.size() != argumentTypes.size()) {
                throw new IllegalStateException("Candidate operator '" + candidate.getDisplayName() + "' has " + operandTypes.size() + " operands, but " + argumentTypes.size() + " were provided.");
            }

            int cost = Conversions.cost(operandTypes, argumentTypes);
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

        return switch (foundCandidates.size()) {
            case 0 ->
                LookupResult.noMatch();
            case 1 ->
                LookupResult.singleMatch(foundCandidates.get(0));
            default ->
                LookupResult.ambiguousMatch(foundCandidates);
        };
    }

    private static List<BoundOperator.Unary> initializeUnaryOperators() {
        var result = new ArrayList<BoundOperator.Unary>();
        for (var type : LangType.getNumericTypes()) {
            result.add(new BoundOperator.Unary(UnaryOperatorKind.Identity, type, type));
            result.add(new BoundOperator.Unary(UnaryOperatorKind.Negation, type, type));
        }

        result.add(BoundOperator.Unary.LOGICAL_NOT);
        return result;
    }

    private static List<BoundOperator.Assignment> initializeAssignmentOperators() {
        var result = new java.util.ArrayList<BoundOperator.Assignment>();
        result.add(BoundOperator.Assignment.SIMPLE_ASSIGNMENT);

        for (var type : LangType.getNumericTypes()) {
            result.add(new BoundOperator.Assignment(AssignmentOperatorKind.AdditionAssignment, type, type));
            result.add(new BoundOperator.Assignment(AssignmentOperatorKind.SubtractionAssignment, type, type));
            result.add(new BoundOperator.Assignment(AssignmentOperatorKind.MultiplicationAssignment, type, type));
            result.add(new BoundOperator.Assignment(AssignmentOperatorKind.DivisionAssignment, type, type));
        }

        return result;
    }

    private static List<BoundOperator.Binary> initializeBinaryOperators() {
        var result = new java.util.ArrayList<BoundOperator.Binary>();

        result.add(BoundOperator.Binary.MODULO);
        result.add(BoundOperator.Binary.STRING_CONCATENATION);
        result.add(BoundOperator.Binary.LOGICAL_AND);
        result.add(BoundOperator.Binary.LOGICAL_OR);

        // Define equality operators
        registerEqualityOperator(result, LangType.String);
        registerEqualityOperator(result, LangType.Char);
        registerEqualityOperator(result, LangType.Bool);
        registerEqualityOperator(result, LangType.Any);

        // Define numeric operators on numeric types
        for (var numericType : LangType.getNumericTypes()) {
            result.add(new BoundOperator.Binary(BinaryOperatorKind.Addition, numericType, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.Subtraction, numericType, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.Multiplication, numericType, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.Division, numericType, numericType, numericType));

            // Define comparison operators on numeric types
            registerEqualityOperator(result, numericType);
            result.add(new BoundOperator.Binary(BinaryOperatorKind.LessThan, LangType.Bool, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.LessThanOrEqual, LangType.Bool, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.GreaterThan, LangType.Bool, numericType, numericType));
            result.add(new BoundOperator.Binary(BinaryOperatorKind.GreaterThanOrEqual, LangType.Bool, numericType, numericType));
        }

        return result;
    }

    private static void registerEqualityOperator(List<BoundOperator.Binary> operators, LangType operandType) {
        operators.add(new BoundOperator.Binary(BinaryOperatorKind.Equal, LangType.Bool, operandType, operandType));
        operators.add(new BoundOperator.Binary(BinaryOperatorKind.NotEqual, LangType.Bool, operandType, operandType));
    }
}
