package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;

public sealed abstract class BoundNumericConstant extends BoundConstant permits
        BoundByteConstant,
        BoundShortConstant,
        BoundIntConstant,
        BoundLongConstant,
        BoundFloatConstant,
        BoundDoubleConstant {

    /**
     * Negates the numeric constant.
     *
     * @return A new BoundNumericConstant that is the negation of this constant.
     */
    public abstract BoundNumericConstant negate();

    /**
     * Adds another numeric constant to this one.
     *
     * @param other The other constant to add.
     * @return A new BoundNumericConstant that is the sum of this and the other
     * constant.
     */
    public abstract BoundNumericConstant add(BoundNumericConstant other);

    /**
     * Subtracts another numeric constant from this one.
     *
     * @param other The other constant to subtract.
     * @return A new BoundNumericConstant that is the difference of this and the
     * other constant.
     */
    public abstract BoundNumericConstant subtract(BoundNumericConstant other);

    /**
     * Multiplies this numeric constant by another.
     *
     * @param other The other constant to multiply by.
     * @return A new BoundNumericConstant that is the product of this and the
     * other constant.
     */
    public abstract BoundNumericConstant multiply(BoundNumericConstant other);

    /**
     * Divides this numeric constant by another.
     *
     * @param other The other constant to divide by.
     * @return A new BoundNumericConstant that is the quotient of this and the
     * other constant.
     */
    public abstract BoundNumericConstant divide(BoundNumericConstant other);

    /**
     * Compares this constant to another constant.
     *
     * @param other The other constant to compare with.
     * @return A negative integer if this constant is less than the other
     * constant, zero if they are equal, and a positive integer if this constant
     * is greater than the other constant.
     */
    public abstract int compareTo(BoundNumericConstant other);

    @Override
    public BoundBoolConstant isEqualTo(BoundConstant other) {
        if (other instanceof BoundNumericConstant numericConstant) {
            return new BoundBoolConstant(compareTo(numericConstant) == 0);
        }

        throw new IllegalArgumentException("Cannot compare numeric constant with non-numeric constant of type " + other.type());
    }

    /**
     * Checks if this constant is greater than another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is greater
     * than the other constant, and false otherwise.
     */
    public BoundBoolConstant isGreaterThan(BoundNumericConstant other) {
        return new BoundBoolConstant(compareTo(other) > 0);
    }

    /**
     * Checks if this constant is greater than or equal to another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is greater
     * than or equal to the other constant, and false otherwise.
     */
    public BoundBoolConstant isGreaterThanOrEqual(BoundNumericConstant other) {
        return new BoundBoolConstant(compareTo(other) >= 0);
    }

    /**
     * Checks if this constant is less than another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is less
     * than the other constant, and false otherwise.
     */
    public BoundBoolConstant isLessThan(BoundNumericConstant other) {
        return new BoundBoolConstant(compareTo(other) < 0);
    }

    /**
     * Checks if this constant is less than or equal to another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is less
     * than or equal to the other constant, and false otherwise.
     */
    public BoundBoolConstant isLessThanOrEqual(BoundNumericConstant other) {
        return new BoundBoolConstant(compareTo(other) <= 0);
    }

    @Override
    public BoundConstant applyUnaryOperator(BoundOperator.Unary operator) {
        return switch (operator.kind()) {
            case Negation ->
                negate();
            default ->
                super.applyUnaryOperator(operator);
        };
    }

    @Override
    public BoundConstant applyBinaryOperator(BoundOperator.Binary operator, BoundConstant other) {
        return switch (operator.kind()) {
            case Addition ->
                add((BoundNumericConstant) other);
            case Subtraction ->
                subtract((BoundNumericConstant) other);
            case Multiplication ->
                multiply((BoundNumericConstant) other);
            case Division ->
                divide((BoundNumericConstant) other);
            case GreaterThan ->
                isGreaterThan((BoundNumericConstant) other);
            case GreaterThanOrEqual ->
                isGreaterThanOrEqual((BoundNumericConstant) other);
            case LessThan ->
                isLessThan((BoundNumericConstant) other);
            case LessThanOrEqual ->
                isLessThanOrEqual((BoundNumericConstant) other);

            default ->
                super.applyBinaryOperator(operator, other);
        };
    }
}
