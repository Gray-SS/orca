package com.orca.compiler.core.boundtree.constants;

import com.orca.compiler.core.typesystem.LangType;

public final class BoundByteConstant extends BoundNumericConstant {
    private final byte value;

    public BoundByteConstant(byte value) {
        this.value = value;
    }

    public byte value() {
        return value;
    }

    @Override
    public BoundNumericConstant negate() {
        return new BoundByteConstant((byte) -value);
    }

    @Override
    public BoundNumericConstant add(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundByteConstant((byte) (value + b.value()));
            case BoundShortConstant s -> new BoundShortConstant((short) (value + s.value()));
            case BoundIntConstant i -> new BoundIntConstant(value + i.value());
            case BoundLongConstant l -> new BoundLongConstant(value + l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value + f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value + d.value());
        };
    }

    @Override
    public BoundNumericConstant subtract(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundByteConstant((byte) (value - b.value()));
            case BoundShortConstant s -> new BoundShortConstant((short) (value - s.value()));
            case BoundIntConstant i -> new BoundIntConstant(value - i.value());
            case BoundLongConstant l -> new BoundLongConstant(value - l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value - f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value - d.value());
        };
    }

    @Override
    public BoundNumericConstant multiply(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundByteConstant((byte) (value * b.value()));
            case BoundShortConstant s -> new BoundShortConstant((short) (value * s.value()));
            case BoundIntConstant i -> new BoundIntConstant(value * i.value());
            case BoundLongConstant l -> new BoundLongConstant(value * l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value * f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value * d.value());
        };
    }

    @Override
    public BoundNumericConstant divide(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> new BoundByteConstant((byte) (value / b.value()));
            case BoundShortConstant s -> new BoundShortConstant((short) (value / s.value()));
            case BoundIntConstant i -> new BoundIntConstant(value / i.value());
            case BoundLongConstant l -> new BoundLongConstant(value / l.value());
            case BoundFloatConstant f -> new BoundFloatConstant(value / f.value());
            case BoundDoubleConstant d -> new BoundDoubleConstant(value / d.value());
        };
    }

    @Override
    public int compareTo(BoundNumericConstant other) {
        return switch (other) {
            case BoundByteConstant b -> Byte.compare(value, b.value());
            case BoundShortConstant s -> Short.compare(value, s.value());
            case BoundIntConstant i -> Integer.compare(value, i.value());
            case BoundLongConstant l -> Long.compare(value, l.value());
            case BoundFloatConstant f -> Float.compare(value, f.value());
            case BoundDoubleConstant d -> Double.compare(value, d.value());
            default -> throw new UnsupportedOperationException("Incompatible constant types");
        };
    }

    @Override
    public LangType type() {
        return LangType.Byte;
    }

    @Override
    public BoundConstant convertTo(LangType targetType) {
        return switch (targetType.getPrimitiveKind()) {
            case Byte -> this;
            case Short -> new BoundShortConstant(value);
            case Int -> new BoundIntConstant(value);
            case Long -> new BoundLongConstant(value);
            case Float -> new BoundFloatConstant(value);
            case Double -> new BoundDoubleConstant(value);
            case Bool, Char, String, None -> throw new IllegalArgumentException("Cannot convert byte to " + targetType);
        };
    }
}
