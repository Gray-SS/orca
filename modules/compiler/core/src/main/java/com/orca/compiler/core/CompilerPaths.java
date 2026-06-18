package com.orca.compiler.core;

import java.nio.file.Path;

public final class CompilerPaths {

    public static Path getFromCwd(String relativePath) {
        var cwd = Path.of(System.getProperty("user.dir"));
        return cwd.resolve(relativePath).normalize();
    }
}
