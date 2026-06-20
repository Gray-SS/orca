package com.orca.compiler.core.codegen;

import com.orca.compiler.core.boundtree.BoundProgram;

public final class Emitter {

    public static final String JAVA_CLASS_INIT_NAME = "<clinit>";
    public static final String JAVA_CTOR_NAME = "<init>";
    public static final String JAVA_OBJECT_INTERNAL_NAME = "java/lang/Object";

    private final ClassSink sink;
    private final BoundProgram boundProgram;

    public Emitter(BoundProgram program, ClassSink sink) {
        this.boundProgram = program;
        this.sink = sink;
    }

    public void emit() {
        var namespaceEmitter = new NamespaceEmitter(this, boundProgram.getGlobalNamespace());
        namespaceEmitter.write();
    }

    public void writeClass(String internalName, byte[] bytecode) {
        sink.accept(internalName, bytecode);
    }
}
