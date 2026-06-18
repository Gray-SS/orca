package com.orca.compiler.core;

public final class Logger {
    private final CompilationContext context;

    public Logger(CompilationContext context) {
        this.context = context;
    }

    public void debug(String message) {
        if (!isSilentMode() && isDebugEnabled()) {
            System.out.println("[DEBUG] " + message);
        }
    }

    public void info(String message) {
        if (!isSilentMode()) {
            System.out.println(message);
        }
    }

    public void error(String message) {
        if (!isSilentMode()) {
            System.err.println("[ERROR] " + message);
        }
    }

    private boolean isDebugEnabled() {
        return context.hasFlag(CompilerFlag.DEBUG);
    }

    private boolean isSilentMode() {
        return context.hasFlag(CompilerFlag.SILENT_MODE);
    }
}
