package com.orca.compiler.core.boundtree;

public record BoundLabel(String name) {
    public BoundLabel(String name) {
        this.name = name;
    }
}
