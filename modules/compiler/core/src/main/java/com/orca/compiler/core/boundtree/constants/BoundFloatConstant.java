package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundFloatConstant extends BoundNumericConstant {

    private final float value;

    public BoundFloatConstant(float value) {
        this.value = value;
    }

    public float value() {
        return value;
    }

    @Override
    public LangType type() {
        return LangType.Float;
    }

    @Override
    public BoundNumericConstant negate() {
        return new BoundFloatConstant(-value);
    }

    @Override
    public BoundNumericConstant add(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundFloatConstant(this.value + b.value());
            case BoundShortConstant s ->
                new BoundFloatConstant(this.value + s.value());
            case BoundIntConstant i ->
                new BoundFloatConstant(this.value + i.value());
            case BoundLongConstant l ->
                new BoundFloatConstant(this.value + l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value + f.value);
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value + d.value());
        };
    }

    @Override
    public BoundNumericConstant subtract(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundFloatConstant(this.value - b.value());
            case BoundShortConstant s ->
                new BoundFloatConstant(this.value - s.value());
            case BoundIntConstant i ->
                new BoundFloatConstant(this.value - i.value());
            case BoundLongConstant l ->
                new BoundFloatConstant(this.value - l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value - f.value);
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value - d.value());
        };
    }

    @Override
    public BoundNumericConstant divide(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundFloatConstant(this.value / b.value());
            case BoundShortConstant s ->
                new BoundFloatConstant(this.value / s.value());
            case BoundIntConstant i ->
                new BoundFloatConstant(this.value / i.value());
            case BoundLongConstant l ->
                new BoundFloatConstant(this.value / l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value / f.value);
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value / d.value());
        };
    }

    @Override
    public BoundNumericConstant multiply(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                new BoundFloatConstant(this.value * b.value());
            case BoundShortConstant s ->
                new BoundFloatConstant(this.value * s.value());
            case BoundIntConstant i ->
                new BoundFloatConstant(this.value * i.value());
            case BoundLongConstant l ->
                new BoundFloatConstant(this.value * l.value());
            case BoundFloatConstant f ->
                new BoundFloatConstant(this.value * f.value);
            case BoundDoubleConstant d ->
                new BoundDoubleConstant(this.value * d.value());
        };
    }

    @Override
    public int compareTo(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b ->
                Float.compare(this.value, b.value());
            case BoundShortConstant s ->
                Float.compare(this.value, s.value());
            case BoundIntConstant i ->
                Float.compare(this.value, i.value());
            case BoundLongConstant l ->
                Float.compare(this.value, l.value());
            case BoundFloatConstant f ->
                Float.compare(this.value, f.value);
            case BoundDoubleConstant d ->
                Double.compare(this.value, d.value());
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
                new BoundIntConstant((int) value);
            case Long ->
                new BoundLongConstant((long) value);
            case Float ->
                this;
            case Double ->
                new BoundDoubleConstant((double) value);
            case Bool, Char, String, None ->
                throw new IllegalArgumentException("Cannot convert float to " + targetType);
        };
    }
}
