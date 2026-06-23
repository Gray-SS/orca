package com.orca.compiler.core.text;

import java.nio.file.Files;
import java.nio.file.Path;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;

public class FileSource extends TextSource {

    private String _content;

    public FileSource(String path) {
        super(path);
    }

    public String getPathString() {
        return name();
    }

    public Path getPath() {
        return Path.of(name());
    }

    @Override
    public String content() {
        if (_content == null) {
            _content = readFileContent(getPath());
        }

        return _content;
    }

    @Override
    public String formatLocation(SourceLocation location) {
        return String.format("%s:%d:%d", name(), location.line(), location.col());
    }

    @Override
    public String formatSpan(SourceSpan span) {
        return String.format("%s:%d:%d - %d:%d", name(), span.loc().line(), span.loc().col(), span.endLoc().line(), span.endLoc().col());
    }

    public boolean exists() {
        return Files.exists(getPath());
    }

    private static String readFileContent(Path path) {
        try {
            return java.nio.file.Files.readString(path);
        } catch (java.io.IOException e) {
            throw new CompilerException(DiagnosticFactory.inputIoError(e.getMessage()));
        }
    }
}
