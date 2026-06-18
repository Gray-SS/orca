package com.orca.compiler.core.text;

public abstract class Source {
    private final String name;

    public Source(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }
}
