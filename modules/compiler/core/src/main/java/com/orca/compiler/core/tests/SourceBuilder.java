package com.orca.compiler.core.tests;

import java.util.function.Consumer;

public abstract class SourceBuilder {

    protected final StringBuilder builder = new StringBuilder();

    protected SourceBuilder appendFormat(String format, Object... args) {
        builder.append(String.format(format, args));
        return this;
    }

    public static final String QUALIFIED_NAME_SEPARATOR = "::";

    public static String buildQualifiedName(String... parts) {
        return String.join(QUALIFIED_NAME_SEPARATOR, parts);
    }

    public static TopLevelSourceBuilder create(String packageName) {
        return new TopLevelSourceBuilder().withPackage(packageName);
    }

    public static TopLevelSourceBuilder create() {
        return new TopLevelSourceBuilder().withDefaultPackage();
    }

    public static <T extends SourceBuilder> String acceptAndBuild(T instance, Consumer<T> consumer) {
        consumer.accept(instance);
        return instance.build();
    }

    // --- Variable declarations ---
    public SourceBuilder declareVariable(String modifier, String name, String initializer, String type, boolean isMutable) {
        appendFormat("%s%s%s: %s = %s;\n", modifier, isMutable ? " mut " : " ", name, type, initializer);
        return this;
    }

    public SourceBuilder declareVariable(String modifier, String name, String initializer, boolean isMutable) {
        appendFormat("%s%s%s = %s;\n", modifier, isMutable ? " mut " : " ", name, initializer);
        return this;
    }

    public SourceBuilder declareLetVariable(String name, String initializer, String type) {
        declareVariable("let", name, initializer, type, false);
        return this;
    }

    public SourceBuilder declareLetVariable(String name, String initializer) {
        declareVariable("let", name, initializer, false);
        return this;
    }

    public SourceBuilder declareLetMutVariable(String name, String initializer, String type) {
        declareVariable("let", name, initializer, type, true);
        return this;
    }

    public SourceBuilder declareLetMutVariable(String name, String initializer) {
        declareVariable("let", name, initializer, true);
        return this;
    }

    public SourceBuilder declareConstVariable(String name, String initializer, String type) {
        declareVariable("const", name, initializer, type, false);
        return this;
    }

    public String build() {
        return builder.toString().stripTrailing();
    }
}
