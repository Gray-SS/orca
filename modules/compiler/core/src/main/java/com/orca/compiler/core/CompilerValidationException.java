package com.orca.compiler.core;

import java.nio.file.Path;

public sealed abstract class CompilerValidationException extends Exception {

    public CompilerValidationException(String message) {
        super(message);
    }

    public static final class InvalidSourceException extends CompilerValidationException {

        public InvalidSourceException(Path sourcePath, String message) {
            super(message);
        }
    }
}
