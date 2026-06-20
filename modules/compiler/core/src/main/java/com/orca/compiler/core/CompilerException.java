package com.orca.compiler.core;

import com.orca.compiler.core.diagnostics.Diagnostic;

public class CompilerException extends RuntimeException {

    private final Diagnostic diagnostic;

    public CompilerException(Diagnostic diagnostic) {
        super(diagnostic.message());
        this.diagnostic = diagnostic;
    }

    public static CompilerException wrap(Diagnostic diagnostic) {
        return new CompilerException(diagnostic);
    }

    public Diagnostic diagnostic() {
        return diagnostic;
    }
}
