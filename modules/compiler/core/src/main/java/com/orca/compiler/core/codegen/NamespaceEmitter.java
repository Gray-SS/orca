package com.orca.compiler.core.codegen;

import com.orca.compiler.core.boundtree.BoundNamespace;

public final class NamespaceEmitter {

    private final Emitter emitter;
    private final BoundNamespace boundNamespace;

    public NamespaceEmitter(Emitter emitter, BoundNamespace boundNamespace) {
        this.emitter = emitter;
        this.boundNamespace = boundNamespace;
    }

    public void write() {
        var packageClass = boundNamespace.getBoundPackageClass();
        var packageClassEmitter = new TypeEmitter(emitter, packageClass, true);
        packageClassEmitter.write();

        for (var t : boundNamespace.getTypes()) {
            var typeEmitter = new TypeEmitter(emitter, t, false);
            typeEmitter.write();
        }

        for (var ns : boundNamespace.getNamespaces()) {
            var namespaceEmitter = new NamespaceEmitter(emitter, ns);
            namespaceEmitter.write();
        }
    }
}
