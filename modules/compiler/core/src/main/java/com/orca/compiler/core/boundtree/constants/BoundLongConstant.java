package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundLongConstant extends BoundNumericConstant {
    private final long value;

    public BoundLongConstant(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public BoundNumericConstant negate() {
        return new BoundLongConstant(-value);
    }

    @Override
    public BoundNumericConstant add(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundLongConstant(value + b.value());
            case BoundShortConstant s -> new BoundLongConstant(value + s.value());
            case BoundIntConstant i -> new BoundLongConstant(value + i.value());
            case BoundLongConstant l -> new BoundLongConstant(value + l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value + f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value + d.value());
        };
    }

    @Override
    public BoundNumericConstant subtract(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundLongConstant(value - b.value());
            case BoundShortConstant s -> new BoundLongConstant(value - s.value());
            case BoundIntConstant i -> new BoundLongConstant(value - i.value());
            case BoundLongConstant l -> new BoundLongConstant(value - l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value - f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value - d.value());
        };
    }

    @Override
    public BoundNumericConstant multiply(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundLongConstant(value * b.value());
            case BoundShortConstant s -> new BoundLongConstant(value * s.value());
            case BoundIntConstant i -> new BoundLongConstant(value * i.value());
            case BoundLongConstant l -> new BoundLongConstant(value * l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value * f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value * d.value());
        };
    }

    @Override
    public BoundNumericConstant divide(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundLongConstant(value / b.value());
            case BoundShortConstant s -> new BoundLongConstant(value / s.value());
            case BoundIntConstant i -> new BoundLongConstant(value / i.value());
            case BoundLongConstant l -> new BoundLongConstant(value / l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value / f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value / d.value());
        };
    }

    @Override
    public int compareTo(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> Long.compare(value, b.value());
            case BoundShortConstant s -> Long.compare(value, s.value());
            case BoundIntConstant i -> Long.compare(value, i.value());
            case BoundLongConstant l -> Long.compare(value, l.value());
            case BoundFloatConstant f -> Float.compare(value, f.value());
            case BoundDoubleConstant d -> Double.compare(value, d.value());
        };
    }

    @Override
    public LangType type() {
        return LangType.Long;
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        return switch (targetType.getPrimitiveKind()) {
            case Byte -> new BoundByteConstant((byte) value);
            case Short -> new BoundShortConstant((short) value);
            case Int -> new BoundIntConstant((int) value);
            case Long -> this;
            case Float -> new BoundFloatConstant(value);
            case Double -> new BoundDoubleConstant(value);
            case Bool, Char, String, None -> throw new IllegalArgumentException("Cannot convert long to " + targetType);
        };
    }

}
