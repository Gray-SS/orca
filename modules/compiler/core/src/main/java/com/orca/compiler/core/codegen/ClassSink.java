package com.orca.compiler.core.codegen;

@FunctionalInterface
public interface ClassSink {

    void accept(String internalName, byte[] bytecode);
}
