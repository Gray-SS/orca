package com.orca.compiler.core.boundtree.expressions;

import java.util.List;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.syntax.expressions.AssignmentOperatorKind;
import com.orca.compiler.core.syntax.expressions.BinaryOperatorKind;
import com.orca.compiler.core.syntax.expressions.UnaryOperatorKind;
import com.orca.compiler.core.typesystem.LangType;

public sealed interface BoundOperator
        permits BoundOperator.Unary, BoundOperator.Binary, BoundOperator.Assignment {

    LangType resultType();

    List<LangType> operandTypes();

    String getDisplayName();

    String getOperatorText();

    public record Assignment(AssignmentOperatorKind kind, LangType targetType, LangType valueType) implements BoundOperator {

        public static final Assignment SIMPLE_ASSIGNMENT = new Assignment(AssignmentOperatorKind.Simple, LangType.Any, LangType.Any);

        public Assignment {
            Preconditions.checkNotNull(targetType, "resultType cannot be null");
            Preconditions.checkNotNull(valueType, "rightType cannot be null");
        }

        @Override
        public LangType resultType() {
            return targetType;
        }

        @Override
        public List<LangType> operandTypes() {
            return List.of(targetType, valueType);
        }

        @Override
        public String getDisplayName() {
            return String.format("%s %s %s", targetType.displayName(), getOperatorText(), valueType.displayName());
        }

        @Override
        public String getOperatorText() {
            return kind.getOperatorText();
        }
    }

    public record Binary(BinaryOperatorKind kind, LangType resultType, LangType leftType, LangType rightType) implements BoundOperator {

        public static final BoundOperator.Binary MODULO = new Binary(BinaryOperatorKind.Modulo, LangType.Int, LangType.Int, LangType.Int);
        public static final BoundOperator.Binary STRING_CONCATENATION = new Binary(BinaryOperatorKind.StringConcatenation, LangType.String, LangType.String, LangType.String);
        public static final BoundOperator.Binary LOGICAL_AND = new Binary(BinaryOperatorKind.LogicalAnd, LangType.Bool, LangType.Bool, LangType.Bool);
        public static final BoundOperator.Binary LOGICAL_OR = new Binary(BinaryOperatorKind.LogicalOr, LangType.Bool, LangType.Bool, LangType.Bool);

        public Binary {
            Preconditions.checkNotNull(resultType, "resultType cannot be null");
            Preconditions.checkNotNull(leftType, "leftType cannot be null");
            Preconditions.checkNotNull(rightType, "rightType cannot be null");
        }

        @Override
        public List<LangType> operandTypes() {
            return List.of(leftType, rightType);
        }

        @Override
        public String getDisplayName() {
            return String.format("%s %s %s", leftType.displayName(), getOperatorText(), rightType.displayName());
        }

        @Override
        public String getOperatorText() {
            return kind.getOperatorText();
        }
    }

    public record Unary(UnaryOperatorKind kind, LangType resultType, LangType operandType) implements BoundOperator {

        public static final BoundOperator.Unary LOGICAL_NOT = new Unary(UnaryOperatorKind.LogicalNot, LangType.Bool, LangType.Bool);

        public Unary {
            Preconditions.checkNotNull(resultType, "resultType cannot be null");
            Preconditions.checkNotNull(operandType, "operandType cannot be null");
        }

        @Override
        public List<LangType> operandTypes() {
            return List.of(operandType);
        }

        @Override
        public String getDisplayName() {
            return String.format("%s %s", kind.getOperatorText(), operandType.displayName());
        }

        @Override
        public String getOperatorText() {
            return kind.getOperatorText();
        }
    }
}
