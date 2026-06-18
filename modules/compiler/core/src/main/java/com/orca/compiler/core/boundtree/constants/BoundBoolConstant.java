package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundBoolConstant extends BoundConstant {

    private final boolean value;

    public static final BoundBoolConstant TRUE = new BoundBoolConstant(true);
    public static final BoundBoolConstant FALSE = new BoundBoolConstant(false);

    public BoundBoolConstant(boolean value) {
        this.value = value;
    }

    /**
     * Negates this boolean constant.
     *
     * @return A new BoundBoolConstant that is the negation of this constant.
     */
    public BoundBoolConstant negate() {
        return new BoundBoolConstant(!value);
    }

    /**
     * Performs a logical AND operation between this boolean constant and
     * another boolean constant.
     *
     * @param other The other boolean constant to perform the AND operation
     * with.
     * @return A new BoundBoolConstant that is the result of the logical AND
     * operation between this and the other constant.
     */
    public BoundBoolConstant and(BoundBoolConstant other) {
        return new BoundBoolConstant(this.value && other.value);
    }

    /**
     * Performs a logical OR operation between this boolean constant and another
     * boolean constant.
     *
     * @param other The other boolean constant to perform the OR operation with.
     * @return A new BoundBoolConstant that is the result of the logical OR
     * operation between this and the other constant.
     */
    public BoundBoolConstant or(BoundBoolConstant other) {
        return new BoundBoolConstant(this.value || other.value);
    }

    @Override
    public BoundBoolConstant isEqualTo(BoundConstant other) {
        if (other instanceof BoundBoolConstant boolConstant) {
            return new BoundBoolConstant(this.value == boolConstant.value);
        }

        throw new IllegalArgumentException("Cannot compare boolean constant with non-boolean constant of type " + other.type());
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        if (targetType.isBool()) {
            return this;
        }

        throw new IllegalArgumentException("Cannot convert boolean constant to type " + targetType);
    }

    public boolean value() {
        return value;
    }

    @Override
    public LangType type() {
        return LangType.Bool;
    }

    @Override
    public BoundConstant applyUnaryOperator(BoundOperator.Unary operator) {
        if (operator == BoundOperator.Unary.LOGICAL_NOT) {
            return negate();
        }

        return super.applyUnaryOperator(operator);
    }

    @Override
    public BoundConstant applyBinaryOperator(BoundOperator.Binary operator, BoundConstant other) {
        if (operator == BoundOperator.Binary.LOGICAL_AND) {
            if (other instanceof BoundBoolConstant boolConstant) {
                return and(boolConstant);
            }

            throw new IllegalArgumentException("Operator " + operator.getOperatorText() + " is only defined for boolean constants. Found: " + other);
        } else if (operator == BoundOperator.Binary.LOGICAL_OR) {
            if (other instanceof BoundBoolConstant boolConstant) {
                return or(boolConstant);
            }

            throw new IllegalArgumentException("Operator " + operator.getOperatorText() + " is only defined for boolean constants. Found: " + other);
        }

        return super.applyBinaryOperator(operator, other);
    }
}
