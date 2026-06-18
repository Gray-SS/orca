package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundStringConstant extends BoundConstant {

    private final String value;

    public BoundStringConstant(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public LangType type() {
        return LangType.String;
    }

    /**
     * Concatenates this string constant with another string constant and
     * returns a new BoundStringConstant.
     *
     * @param other The other BoundStringConstant to concatenate with this one.
     * @return A new BoundStringConstant that is the result of concatenating
     * this and the other constant.
     */
    public BoundStringConstant concat(BoundStringConstant other) {
        return new BoundStringConstant(this.value + other.value);
    }

    @Override
    public BoundBoolConstant isEqualTo(BoundConstant other) {
        if (other instanceof BoundStringConstant stringConstant) {
            return new BoundBoolConstant(this.value.equals(stringConstant.value));
        }

        throw new IllegalArgumentException("Cannot compare string constant with non-string constant of type " + other.type());
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        if (targetType.isString()) {
            return this;
        }

        throw new IllegalArgumentException("Cannot convert string constant to type " + targetType);
    }

    @Override
    public BoundConstant applyBinaryOperator(BoundOperator.Binary operator, BoundConstant other) {
        if (operator == BoundOperator.Binary.STRING_CONCATENATION) {
            if (other instanceof BoundStringConstant stringConstant) {
                return concat(stringConstant);
            }

            throw new IllegalArgumentException("Operator " + operator.getOperatorText() + " is only defined for string constants. Found: " + other);
        }

        return super.applyBinaryOperator(operator, other);
    }
}
