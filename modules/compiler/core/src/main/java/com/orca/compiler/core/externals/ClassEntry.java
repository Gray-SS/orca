package com.orca.compiler.core.externals;

import com.google.common.base.Preconditions;

public final record ClassEntry(String qualifiedName) {

    public ClassEntry {
        Preconditions.checkNotNull(qualifiedName, "qualifiedName must not be null");
    }

    public String getSimpleName() {
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return qualifiedName; // No package, the qualified name is the simple name
        }

        return qualifiedName.substring(lastDotIndex + 1);
    }

    public String getPackageName() {
        int lastDotIndex = qualifiedName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return ""; // Default package
        }

        return qualifiedName.substring(0, lastDotIndex);
    }
}
