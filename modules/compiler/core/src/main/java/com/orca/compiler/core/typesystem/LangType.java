package com.orca.compiler.core.typesystem;

public sealed interface LangType permits BaseType {

    /**
     * The byte primitive type.
     */
    public static final PrimitiveType Byte = new PrimitiveType(PrimitiveTypeKind.Byte, "byte", "B");

    /**
     * The short primitive type.
     */
    public static final PrimitiveType Short = new PrimitiveType(PrimitiveTypeKind.Short, "short", "S");

    /**
     * The integer primitive type.
     */
    public static final PrimitiveType Int = new PrimitiveType(PrimitiveTypeKind.Int, "int", "I");

    /**
     * The long primitive type.
     */
    public static final PrimitiveType Long = new PrimitiveType(PrimitiveTypeKind.Long, "long", "J");

    /**
     * The float primitive type.
     */
    public static final PrimitiveType Float = new PrimitiveType(PrimitiveTypeKind.Float, "float", "F");

    /**
     * The double primitive type.
     */
    public static final PrimitiveType Double = new PrimitiveType(PrimitiveTypeKind.Double, "double", "D");

    /**
     * The character primitive type.
     */
    public static final PrimitiveType Char = new PrimitiveType(PrimitiveTypeKind.Char, "char", "C");

    /**
     * The string primitive type.
     */
    public static final PrimitiveType String = new PrimitiveType(PrimitiveTypeKind.String, "string", "Ljava/lang/String;");

    /**
     * The boolean primitive type.
     */
    public static final PrimitiveType Bool = new PrimitiveType(PrimitiveTypeKind.Bool, "bool", "Z");

    /**
     * The void type.
     */
    public static final VoidType Void = VoidType.INSTANCE;

    /**
     * The any type.
     */
    public static final AnyType Any = AnyType.INSTANCE;

    /**
     * The error type, used to represent results of failed type resolution or
     * type checking, to allow the compiler to continue and report more errors
     */
    public static final ErrorType Error = ErrorType.INSTANCE;

    /**
     * The opaque type, used to represent types that are not known or not
     * representable in the type system (e.g., external types).
     */
    public static final OpaqueType Opaque = OpaqueType.INSTANCE;

    /**
     * The unknown type, used to represent unknown types (e.g., type is not
     * resolved).
     */
    public static final UnknownType Unknown = UnknownType.INSTANCE;

    public static LangType[] getNumericTypes() {
        return new LangType[]{Byte, Short, Int, Long, Float, Double};
    }

    /**
     * Returns the kind of this type.
     *
     * @return The kind of this type.
     */
    public TypeKind kind();

    /**
     * Returns a human-readable name for this type, used in error messages and
     * debugging.
     *
     * @return A human-readable name for this type.
     */
    public String displayName();

    public default LangType unwrap() {
        LangType current = this;
        while (current instanceof NominalType nominal) {
            current = nominal.underlyingType();
        }

        return current;
    }

    /**
     * Returns the type system that this type belongs to.
     *
     * @return The type system that this type belongs to.
     */
    public default TypeSystem getTypeSystem() {
        return TypeSystem.getInstance();
    }

    /**
     * Checks if this type is equal to another object. Two types are considered
     * equal if they represent the same type in the type system, even if they
     * are different instances. For example, two instances of a class type with
     * the same name and type parameters should be considered equal, even if
     * they are different objects in memory.
     *
     * @param obj The object to compare with this type.
     * @return True if this type is equal to the other object, false otherwise.
     */
    @Override
    public boolean equals(Object obj);

    /**
     * Checks if a value of the source type can be assigned to a variable of
     * this type.
     *
     * @param source The source type.
     * @return True if a value of the source type can be assigned to a variable
     * of this type, false otherwise.
     * @implNote This method is not necessarily symmetric with
     * {@link #isAssignableTo(LangType)}, as some types may allow implicit
     * conversions in one direction but not the other.
     * @implNote This method checks for assignability, which may allow implicit
     * conversions, while {@link #isConvertibleFrom(LangType)} checks for
     * convertibility, which may allow both implicit and explicit conversions.
     */
    public default boolean isAssignableFrom(LangType source) {
        return getTypeSystem().isAssignable(this, source);
    }

    /**
     * Checks if a value of this type can be assigned to a variable of the
     * target type.
     *
     * @param target The target type.
     * @return True if a value of this type can be assigned to a variable of the
     * target type, false otherwise.
     * @implNote This method is not necessarily symmetric with
     * {@link #isAssignableFrom(LangType)}, as some types may allow implicit
     * conversions in one direction but not the other.
     * @implNote This method checks for assignability, which may allow implicit
     * conversions, while {@link #isConvertibleTo(LangType)} checks for
     * convertibility, which may allow both implicit and explicit conversions.
     */
    public default boolean isAssignableTo(LangType target) {
        return getTypeSystem().isAssignable(target, this);
    }

    /**
     * Checks if a value of the source type can be converted to this type.
     *
     * @param source The source type.
     * @return True if a value of the source type can be converted to this type,
     * false otherwise.
     * @implNote This method is not necessarily symmetric with
     * {@link #isAssignableTo(LangType)}, as some types may allow implicit
     * conversions in one direction but not the other.
     * @implNote This method checks for convertibility, which may allow both
     * implicit and explicit conversions, while
     * {@link #isAssignableFrom(LangType)} checks for assignability, which may
     * allow implicit conversions.
     */
    public default boolean isConvertibleFrom(LangType source) {
        return getTypeSystem().isConvertible(source, this);
    }

    /**
     * Checks if a value of this type can be converted to the target type.
     *
     * @param target The target type.
     * @return True if a value of this type can be converted to the target type,
     * false otherwise.
     * @implNote This method is not necessarily symmetric with
     * {@link #isConvertibleFrom(LangType)}, as some types may allow implicit
     * conversions in one direction but not the other.
     * @implNote This method checks for convertibility, which may allow both
     * implicit and explicit conversions, while
     * {@link #isAssignableTo(LangType)} checks for assignability, which may
     * allow implicit conversions.
     */
    public default boolean isConvertibleTo(LangType target) {
        return getTypeSystem().isConvertible(this, target);
    }

    /**
     * Checks if this type is structurally equivalent to another type.
     * Structural equivalence means that the two types have the same structure,
     * regardless of their names or nominal identities. For example, two
     * collection types with the same field names and field types would be
     * considered structurally equivalent, even if they have different names.
     *
     * @param other The other type to compare with this type.
     * @return True if this type is structurally equivalent to the other type,
     * false otherwise.
     */
    public default boolean isStructurallyEquivalentTo(LangType other) {
        return TypeComparator.areStructurallyEquivalent(this, other);
    }

    /**
     * Checks if a value of this type requires an explicit cast to be converted
     * to the target type.
     *
     * @param target The target type.
     * @return True if a value of this type requires an explicit cast to be
     * converted to the target type, false otherwise.
     */
    public default boolean requiresCastTo(LangType target) {
        return getTypeSystem().requiresCast(target, this);
    }

    /**
     * Checks if this type is the UNKNOWN type.
     *
     * @return True if this type is the UNKNOWN type, false otherwise.
     */
    public default boolean isUnknown() {
        return kind() == TypeKind.UNKNOWN;
    }

    /**
     * Checks if this type is the OPAQUE type.
     *
     * @return True if this type is the OPAQUE type, false otherwise.
     */
    public default boolean isOpaque() {
        return kind() == TypeKind.OPAQUE;
    }

    /**
     * Checks if this type is the ERROR type.
     *
     * @return True if this type is the ERROR type, false otherwise.
     */
    public default boolean isError() {
        return kind() == TypeKind.ERROR;
    }

    /**
     * Checks if this type is the ANY type.
     *
     * @return True if this type is the ANY type, false otherwise.
     */
    public default boolean isAny() {
        return kind() == TypeKind.ANY;
    }

    /**
     * Checks if this type is a primitive type.
     *
     * @return True if this type is a primitive type, false otherwise.
     */
    public default boolean isPrimitive() {
        return kind() == TypeKind.Primitive;
    }

    /**
     * Gets the primitive type kind of this type if this type is a primitive
     * type, or PrimitiveTypeKind.None if this type is not a primitive type.
     *
     * @return The primitive type kind of this type if this type is a primitive
     * type, or PrimitiveTypeKind.None if this type is not a primitive type.
     */
    public default PrimitiveTypeKind getPrimitiveKind() {
        return PrimitiveTypeKind.None;
    }

    /**
     * Checks if this type is a numeric type.
     *
     * @return True if this type is a numeric type, false otherwise.
     */
    public default boolean isNumeric() {
        return isByte() || isShort() || isInt() || isLong() || isFloat() || isDouble();
    }

    /**
     * Checks if this type is the byte type.
     *
     * @return True if this type is the byte type, false otherwise.
     */
    public default boolean isByte() {
        return getPrimitiveKind() == PrimitiveTypeKind.Byte;
    }

    /**
     * Checks if this type is the short type.
     *
     * @return True if this type is the short type, false otherwise.
     */
    public default boolean isShort() {
        return getPrimitiveKind() == PrimitiveTypeKind.Short;
    }

    /**
     * Checks if this type is the int type.
     *
     * @return True if this type is the int type, false otherwise.
     */
    public default boolean isInt() {
        return getPrimitiveKind() == PrimitiveTypeKind.Int;
    }

    /**
     * Checks if this type is the long type.
     *
     * @return True if this type is the long type, false otherwise.
     */
    public default boolean isLong() {
        return getPrimitiveKind() == PrimitiveTypeKind.Long;
    }

    /**
     * Checks if this type is the float type.
     *
     * @return True if this type is the float type, false otherwise.
     */
    public default boolean isFloat() {
        return getPrimitiveKind() == PrimitiveTypeKind.Float;
    }

    /**
     * Checks if this type is the double type.
     *
     * @return True if this type is the double type, false otherwise.
     */
    public default boolean isDouble() {
        return getPrimitiveKind() == PrimitiveTypeKind.Double;
    }

    /**
     * Checks if this type is the BOOL type.
     *
     * @return True if this type is the BOOL type, false otherwise.
     */
    public default boolean isBool() {
        return getPrimitiveKind() == PrimitiveTypeKind.Bool;
    }

    /**
     * Checks if this type is the char type.
     *
     * @return True if this type is the char type, false otherwise.
     */
    public default boolean isChar() {
        return getPrimitiveKind() == PrimitiveTypeKind.Char;
    }

    /**
     * Checks if this type is the STRING type.
     *
     * @return True if this type is the STRING type, false otherwise.
     */
    public default boolean isString() {
        return getPrimitiveKind() == PrimitiveTypeKind.String;
    }

    /**
     * Checks if this type is an array type.
     *
     * @return True if this type is an array type, false otherwise.
     */
    public default boolean isArray() {
        return kind() == TypeKind.ARRAY;
    }

    /**
     * Checks if this type is a collection type.
     *
     * @return True if this type is a collection type, false otherwise.
     */
    public default boolean isCollection() {
        return kind() == TypeKind.COLLECTION;
    }

    /**
     * Checks if this type is a nominal type.
     *
     * @return True if this type is a nominal type, false otherwise.
     */
    public default boolean isNominal() {
        return kind() == TypeKind.NOMINAL;
    }

    /**
     * Checks if this type is the VOID type.
     *
     * @return True if this type is the VOID type, false otherwise.
     */
    public default boolean isVoid() {
        return kind() == TypeKind.VOID;
    }

    /**
     * Checks if this type is an instance of the expected type.
     *
     * @param <T> The expected type.
     * @param expectedType The expected type class.
     * @return True if this type is an instance of the expected type, false
     * otherwise.
     */
    public default <T extends LangType> boolean is(Class<T> expectedType) {
        return expectedType.isInstance(this);
    }

    /**
     * Casts this type to the expected type if it is an instance of the expected
     * type, otherwise throws a RuntimeException.
     *
     * @param <T> The expected type.
     * @param expectedType The expected type class.
     * @return This type cast to the expected type if it is an instance of the
     * expected type.
     */
    public default <T extends LangType> T as(Class<T> expectedType) {
        if (!expectedType.isInstance(this)) {
            throw new RuntimeException("Type is not of expected shape: " + this);
        }

        return expectedType.cast(this);
    }

    public static LangType arrayOf(LangType elementType) {
        return new ArrayType(elementType);
    }
}
