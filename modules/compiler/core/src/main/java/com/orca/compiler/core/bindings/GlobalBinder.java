package com.orca.compiler.core.bindings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.symbols.Lazy;
import com.orca.compiler.core.symbols.ResolutionState;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.intrinsics.IntrinsicSymbol;

public final class GlobalBinder extends NamespaceBinder {

    private final SemanticModel semanticModel;

    private final DiagnosticCollector diagnosticCollector = new DiagnosticCollector();
    private final Map<String, Lazy<Symbol>> lazyImportedSymbols = new HashMap<>();
    private static final Map<String, List<IntrinsicSymbol>> intrinsicSymbols = new HashMap<>();

    public GlobalBinder(SemanticModel semanticModel) {
        super(null, semanticModel.getCompilation().getGlobalNamespace());
        this.semanticModel = semanticModel;
    }

    static {
        // Register intrinsic symbols
        for (var intrinsic : IntrinsicSymbol.getAllIntrinsics()) {
            intrinsicSymbols.computeIfAbsent(intrinsic.name(), k -> new ArrayList<>()).add(intrinsic);
        }
        System.out.println("Registered " + intrinsicSymbols.size() + " intrinsic symbols: " + intrinsicSymbols.keySet().stream().toList());
    }

    public void registerLazyImportSymbol(String name, Lazy<Symbol> lazySymbol) {
        if (lazyImportedSymbols.containsKey(name)) {
            throw new IllegalArgumentException("A lazy import symbol with the name '" + name + "' is already registered.");
        }

        lazyImportedSymbols.put(name, lazySymbol);
    }

    @Override
    protected Compilation getCompilation() {
        return semanticModel.getCompilation();
    }

    @Override
    protected SemanticModel getSemanticModel() {
        return semanticModel;
    }

    @Override
    protected DiagnosticCollector getDiagnosticCollector() {
        return diagnosticCollector;
    }

    @Override
    public @Nullable
    Symbol lookupSymbol(String name) {
        var compilation = getCompilation();
        var globalNamespace = compilation.getGlobalNamespace();

        var member = globalNamespace.getMember(name);
        if (member != null) {
            return member;
        }

        var intrinsicSymbolsList = intrinsicSymbols.get(name);
        if (intrinsicSymbolsList != null) {
            // Return the first intrinsic symbol with the given name
            return intrinsicSymbolsList.get(0);
        }

        var lazySymbol = lazyImportedSymbols.get(name);
        if (lazySymbol != null) {
            if (lazySymbol.getState() == ResolutionState.RESOLVING) {
                // Avoid infinite recursion in case of cycles
                return null;
            }
            return lazySymbol.resolve();
        }

        return super.lookupSymbol(name);
    }

    @Override
    public List<Symbol> lookupSymbols(String name) {
        var symbols = super.lookupSymbols(name);

        var lazySymbol = lazyImportedSymbols.get(name);
        if (lazySymbol != null) {
            symbols.add(lazySymbol.resolve());
        }

        var intrinsicSymbolsList = intrinsicSymbols.get(name);
        if (intrinsicSymbolsList != null) {
            symbols.addAll(intrinsicSymbolsList);
        }

        return symbols;
    }
}
