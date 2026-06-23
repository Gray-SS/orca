package com.orca.cli;

public final class ProgramExecutionException extends Exception {

    public ProgramExecutionException(String message) {
        super(message);
    }

    public ProgramExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
