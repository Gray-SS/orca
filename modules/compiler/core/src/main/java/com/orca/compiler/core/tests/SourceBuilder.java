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
    public SourceBuilder declareLetVariable(String name, String initializer, String type) {
        appendFormat("let %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public SourceBuilder declareLetVariable(String name, String initializer) {
        appendFormat("let %s := %s;\n", name, initializer);
        return this;
    }

    public SourceBuilder declareVarVariable(String name, String initializer, String type) {
        appendFormat("var %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public SourceBuilder declareVarVariable(String name, String initializer) {
        appendFormat("var %s := %s;\n", name, initializer);
        return this;
    }

    public SourceBuilder declareConstVariable(String name, String initializer, String type) {
        appendFormat("const %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public String build() {
        return builder.toString().stripTrailing();
    }
}
