package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.typesystem.LangType;

public sealed abstract class BoundConstant permits
        BoundNumericConstant,
        BoundStringConstant,
        BoundBoolConstant,
        BoundNullConstant {

    /**
     * Gets a string representation of the value of this constant for display
     * purposes.
     *
     * @return A string representation of the value of this constant.
     */
    public String getDisplayValue() {
        return switch (this) {
            case BoundByteConstant b ->
                Byte.toString(b.value());
            case BoundShortConstant s ->
                Short.toString(s.value());
            case BoundIntConstant i ->
                Integer.toString(i.value());
            case BoundLongConstant l ->
                Long.toString(l.value());
            case BoundFloatConstant f ->
                Float.toString(f.value());
            case BoundDoubleConstant d ->
                Double.toString(d.value());
            case BoundStringConstant s ->
                s.value();
            case BoundBoolConstant b ->
                Boolean.toString(b.value());
            case BoundNullConstant n ->
                "null";
        };
    }

    public static BoundConstant getDefaultValue(LangType type) {
        return switch (type.getPrimitiveKind()) {
            case Byte ->
                new BoundByteConstant((byte) 0);
            case Short ->
                new BoundShortConstant((short) 0);
            case Int ->
                new BoundIntConstant(0);
            case Long ->
                new BoundLongConstant(0L);
            case Float ->
                new BoundFloatConstant(0.0f);
            case Double ->
                new BoundDoubleConstant(0.0);
            case String ->
                new BoundStringConstant("");
            case Bool ->
                new BoundBoolConstant(false);
            case None, Char ->
                throw new IllegalArgumentException("Type '" + type + "' does not have a default value.");
        };
    }

    /**
     * Gets the type of this constant.
     *
     * @return The type of this constant.
     */
    public abstract LangType type();

    /**
     * Checks if this constant is equal to another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is equal to
     * the other constant, and false otherwise.
     */
    public abstract BoundBoolConstant isEqualTo(BoundConstant other);

    /**
     * Checks if this constant is not equal to another constant.
     *
     * @param other The other constant to compare with.
     * @return A new BoundBoolConstant that is true if this constant is not
     * equal to the other constant, and false otherwise.
     */
    public BoundBoolConstant isNotEqualTo(BoundConstant other) {
        return isEqualTo(other).negate();
    }

    /**
     * Converts this constant to a different type, if possible.
     *
     * @param targetType The type to convert this constant to.
     * @return A new BoundConstant that is the result of converting this
     * constant to the target type.
     * @throws IllegalArgumentException if the conversion is not possible.
     */
    public abstract BoundConstant convertTo(LangType targetType);

    /**
     * Applies a unary operator to this constant.
     *
     * @param operator The unary operator to apply.
     * @return A new BoundConstant that is the result of applying the operator
     * to this constant.
     */
    public BoundConstant applyUnaryOperator(BoundOperator.Unary operator) {
        throw new IllegalArgumentException("Unary operator '" + operator + "' is not defined for type '" + this.type() + "'");
    }

    /**
     * Applies a binary operator to this constant and another constant.
     *
     * @param operator The binary operator to apply.
     * @param other The other constant to apply the operator with.
     * @return A new BoundConstant that is the result of applying the operator
     * to this and the other constant.
     */
    public BoundConstant applyBinaryOperator(BoundOperator.Binary operator, BoundConstant other) {
        return switch (operator.kind()) {
            case Equal ->
                isEqualTo(other);
            case NotEqual ->
                isNotEqualTo(other);
            default ->
                throw new IllegalArgumentException("Binary operator '" + operator.getOperatorText() + "' is not implemented for types '" + this.type() + "' and '" + other.type() + "'");
        };
    }
}
