package com.orca.compiler.core.tests;

import java.util.function.Consumer;

public abstract class SourceBuilder {

    protected final StringBuilder builder = new StringBuilder();

    protected SourceBuilder appendFormat(String format, Object... args) {
        builder.append(String.format(format, args));
        return this;
    }

    public static <T extends SourceBuilder> String acceptAndBuild(T instance, Consumer<T> consumer) {
        consumer.accept(instance);
        return instance.build();
    }

    // --- Variable declarations (valid in top-level, local, and impl-block contexts) ---
    public SourceBuilder appendImmutableVariableDecl(String name, String initializer, String type) {
        appendFormat("let %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public SourceBuilder appendImmutableVariableDecl(String name, String initializer) {
        appendFormat("let %s := %s;\n", name, initializer);
        return this;
    }

    public SourceBuilder appendMutableVariableDecl(String name, String initializer, String type) {
        appendFormat("var %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public SourceBuilder appendMutableVariableDecl(String name, String initializer) {
        appendFormat("var %s := %s;\n", name, initializer);
        return this;
    }

    public SourceBuilder appendConstVariableDecl(String name, String initializer, String type) {
        appendFormat("const %s: %s = %s;\n", name, type, initializer);
        return this;
    }

    public String build() {
        return builder.toString().stripTrailing();
    }
}
