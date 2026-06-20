package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundDoubleConstant extends BoundNumericConstant {

    private final double value;

    public BoundDoubleConstant(double value) {
        this.value = value;
    }

    public double value() {
        return value;
    }

    @Override
    public BoundNumericConstant negate() {
        return new BoundDoubleConstant(-value);
    }

    @Override
    public BoundNumericConstant add(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundDoubleConstant(this.value + b.value());
            case BoundShortConstant s ->
                new BoundDoubleConstant(this.value + s.value());
            case BoundIntConstant i ->
                new BoundDoubleConstant(this.value + i.value());
            case BoundLongConstant l ->
                new BoundDoubleConstant(this.value + l.value());
            case BoundFloatConstant f ->
                new BoundDoubleConstant(this.value + f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value + d.value());
        };
    }

    @Override
    public BoundNumericConstant subtract(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundDoubleConstant(this.value - b.value());
            case BoundShortConstant s ->
                new BoundDoubleConstant(this.value - s.value());
            case BoundIntConstant i ->
                new BoundDoubleConstant(this.value - i.value());
            case BoundLongConstant l ->
                new BoundDoubleConstant(this.value - l.value());
            case BoundFloatConstant f ->
                new BoundDoubleConstant(this.value - f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value - d.value());
        };
    }

    @Override
    public BoundNumericConstant multiply(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundDoubleConstant(this.value * b.value());
            case BoundShortConstant s ->
                new BoundDoubleConstant(this.value * s.value());
            case BoundIntConstant i ->
                new BoundDoubleConstant(this.value * i.value());
            case BoundLongConstant l ->
                new BoundDoubleConstant(this.value * l.value());
            case BoundFloatConstant f ->
                new BoundDoubleConstant(this.value * f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value * d.value());
        };
    }

    @Override
    public BoundNumericConstant divide(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundDoubleConstant(this.value / b.value());
            case BoundShortConstant s ->
                new BoundDoubleConstant(this.value / s.value());
            case BoundIntConstant i ->
                new BoundDoubleConstant(this.value / i.value());
            case BoundLongConstant l ->
                new BoundDoubleConstant(this.value / l.value());
            case BoundFloatConstant f ->
                new BoundDoubleConstant(this.value / f.value());
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value / d.value());
        };
    }

    @Override
    public int compareTo(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                Double.compare(this.value, b.value());
            case BoundShortConstant s ->
                Double.compare(this.value, s.value());
            case BoundIntConstant i ->
                Double.compare(this.value, i.value());
            case BoundLongConstant l ->
                Double.compare(this.value, l.value());
            case BoundFloatConstant f ->
                Double.compare(this.value, f.value());
            case BoundDoubleConstant d ->
                Double.compare(this.value, d.value());
        };
    }

    @Override
    public LangType type() {
        return LangType.Double;
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        return switch (targetType.getPrimitiveKind()) {
            case Byte ->
                new BoundByteConstant((byte) value);
            case Short ->
                new BoundShortConstant((short) value);
            case Int ->
                new BoundIntConstant((int) value);
            case Long ->
                new BoundLongConstant((long) value);
            case Float ->
                new BoundFloatConstant((float) value);
            case Double ->
                this;
            case Bool, Char, String, None ->
                throw new IllegalArgumentException("Cannot convert double to " + targetType);
        };
    }
}
