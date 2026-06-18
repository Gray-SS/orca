package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.typesystem.LangType;

public final class BoundIntConstant extends BoundNumericConstant {

    private final int value;

    public BoundIntConstant(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    @Override
    public LangType type() {
        return LangType.Int;
    }

    @Override
    public BoundIntConstant negate() {
        return new BoundIntConstant(-value);
    }

    /**
     * Modulo this integer constant with another integer constant.
     *
     * @param other The other integer constant to modulo.
     * @return A new BoundIntConstant that is the remainder of this and the
     * other constant.
     */
    public BoundIntConstant modulo(BoundIntConstant other) {
        return new BoundIntConstant(this.value % other.value);
    }

    @Override
    public BoundNumericConstant add(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundIntConstant(this.value + b.value());
            case BoundShortConstant s ->
                new BoundIntConstant(this.value + s.value());
            case BoundIntConstant i ->
                new BoundIntConstant(this.value + i.value);
            case BoundLongConstant l ->
                new BoundLongConstant(this.value + l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value + f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value + d.value());
        };
    }

    @Override
    public BoundNumericConstant subtract(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundIntConstant(this.value - b.value());
            case BoundShortConstant s ->
                new BoundIntConstant(this.value - s.value());
            case BoundIntConstant i ->
                new BoundIntConstant(this.value - i.value);
            case BoundLongConstant l ->
                new BoundLongConstant(this.value - l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value - f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value - d.value());
        };
    }

    @Override
    public BoundNumericConstant multiply(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundIntConstant(this.value * b.value());
            case BoundShortConstant s ->
                new BoundIntConstant(this.value * s.value());
            case BoundIntConstant i ->
                new BoundIntConstant(this.value * i.value);
            case BoundLongConstant l ->
                new BoundLongConstant(this.value * l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value * f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value * d.value());
        };
    }

    @Override
    public BoundNumericConstant divide(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundIntConstant(this.value / b.value());
            case BoundShortConstant s ->
                new BoundIntConstant(this.value / s.value());
            case BoundIntConstant i ->
                new BoundIntConstant(this.value / i.value);
            case BoundLongConstant l ->
                new BoundLongConstant(this.value / l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value / f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value / d.value());
        };
    }

    @Override
    public int compareTo(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                Integer.compare(this.value, b.value());
            case BoundShortConstant s ->
                Integer.compare(this.value, s.value());
            case BoundIntConstant i ->
                Integer.compare(this.value, i.value);
            case BoundLongConstant l ->
                Long.compare(this.value, l.value());
            case BoundFloatConstant f ->
                Float.compare(this.value, f.value());
            case BoundDoubleConstant d ->
                Double.compare(this.value, d.value());
            default ->
                throw new IllegalArgumentException("Cannot compare int constant with non-numeric constant of type " + other.type());
        };
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        return switch (targetType.getPrimitiveKind()) {
            case Byte ->
                new BoundByteConstant((byte) value);
            case Short ->
                new BoundShortConstant((short) value);
            case Int ->
                this;
            case Long ->
                new BoundLongConstant((long) value);
            case Float ->
                new BoundFloatConstant((float) value);
            case Double ->
                new BoundDoubleConstant((double) value);
            case Bool, Char, String, None ->
                throw new IllegalArgumentException("Cannot convert int to " + targetType);
        };
    }

    @Override
    public BoundConstant applyBinaryOperator(BoundOperator.Binary operator, BoundConstant other) {
        if (operator == BoundOperator.Binary.MODULO) {
            if (other instanceof BoundIntConstant otherInt) {
                return modulo(otherInt);
            }

            throw new IllegalArgumentException("Cannot apply operator " + operator.kind() + " to int constant and non-int constant of type " + other.type());
        }

        return super.applyBinaryOperator(operator, other);
    }
}
