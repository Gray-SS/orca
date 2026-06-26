package com.orca.compiler.core.semantics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import com.google.common.base.Preconditions;
import com.orca.compiler.core.Debug;
import com.orca.compiler.core.bindings.MemberBinder;
import com.orca.compiler.core.diagnostics.DiagnosticBag;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.VariableSymbol;
import com.orca.compiler.core.symbols.sources.SourceExtensionSymbol;
import com.orca.compiler.core.symbols.sources.SourceFieldSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceNominalTypeSymbol;
import com.orca.compiler.core.symbols.sources.SourceParameterSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.symbols.Lazy;
import com.orca.compiler.core.syntax.members.CollectionDeclarationSyntax;
import com.orca.compiler.core.syntax.members.ImplBlockSyntax;
import com.orca.compiler.core.syntax.members.MemberSyntax;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.members.VariableDeclarationSyntax;
import com.orca.compiler.core.typesystem.CollectionType;
import com.orca.compiler.core.typesystem.LangType;

/**
 * Drives the four-phase symbol-declaration lifecycle for a single
 * {@link SemanticModel}.
 */
public final class SymbolDeclarator {

    private final SemanticModel model;
    private final DiagnosticCollector diagnostics = new DiagnosticCollector();

    private boolean importsResolved = false;
    private boolean typesDeclared = false;
    private boolean methodsDeclared = false;
    private boolean remainingSymbolsDeclared = false;

    /**
     * Package-private: only {@link SemanticModel} may construct this.
     */
    SymbolDeclarator(SemanticModel model) {
        this.model = model;
    }

    /**
     * Returns a snapshot of all diagnostics produced during declaration. Safe
     * to call at any point; phases that have not yet run contribute nothing.
     */
    public DiagnosticBag getDiagnostics() {
        return diagnostics.freeze();
    }

    public void runPhase(Phase phase) {
        switch (phase) {
            case RESOLVE_IMPORTS ->
                resolveImports();
            case DECLARE_TYPES ->
                declareTypes();
            case DECLARE_METHODS ->
                declareMethods();
            case DECLARE_REMAINING_SYMBOLS ->
                declareRemainingSymbols();
        }
    }

    private void resolveImports() {
        if (importsResolved) {
            return;
        }
        importsResolved = true;

        var unit = model.getCompilationUnit();
        model.getBinder(unit); // initializes model.globalBinder as a side-effect

        var seenImports = new HashSet<String>();
        for (var importSyntax : unit.imports()) {
            var identifier = importSyntax.identifier();
            var symbolName = identifier.name();

            if (!seenImports.add(symbolName)) {
                continue;
            }

            var lazyImport = new Lazy<>(() -> {
                var importedSymbol = model.globalBinder.resolveSymbol(importSyntax.identifier());
                if (importedSymbol == null) {
                    throw SemanticErrors.undeclaredPackage(importSyntax, importSyntax.identifier().text(), java.util.List.of());
                }
                return importedSymbol;
            });

            model.globalBinder.registerLazyImportSymbol(symbolName, lazyImport);
        }
    }

    private void declareTypes() {
        if (typesDeclared) {
            return;
        }
        typesDeclared = true;

        var unit = model.getCompilationUnit();
        MemberBinder binder = (MemberBinder) model.getBinder(unit);

        for (var member : unit.members()) {
            if (member instanceof CollectionDeclarationSyntax decl) {
                try {
                    declareCollection(binder, decl);
                } catch (com.orca.compiler.core.CompilerException e) {
                    diagnostics.report(e.diagnostic());
                }
            }
        }
    }

    private void declareMethods() {
        if (methodsDeclared) {
            return;
        }
        methodsDeclared = true;

        var unit = model.getCompilationUnit();
        MemberBinder binder = (MemberBinder) model.getBinder(unit);

        for (var member : unit.members()) {
            try {
                switch (member) {
                    case MethodDeclarationSyntax methodDecl ->
                        declareNamespaceMethod(binder, methodDecl);
                    case ImplBlockSyntax implBlock ->
                        declareTypeMembers(binder, implBlock, true);
                    default -> {
                    }
                }
            } catch (com.orca.compiler.core.CompilerException e) {
                diagnostics.report(e.diagnostic());
            }
        }
    }

    private void declareRemainingSymbols() {
        if (remainingSymbolsDeclared) {
            return;
        }
        remainingSymbolsDeclared = true;

        var unit = model.getCompilationUnit();
        MemberBinder binder = (MemberBinder) model.getBinder(unit);

        for (var member : unit.members()) {
            if (!(member instanceof CollectionDeclarationSyntax)) {
                try {
                    declareNamespaceMember(binder, member, false);
                } catch (com.orca.compiler.core.CompilerException e) {
                    diagnostics.report(e.diagnostic());
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers — declaration logic
    // -------------------------------------------------------------------------
    private void declareNamespaceMember(MemberBinder binder, MemberSyntax syntax, boolean methodsOnly) {
        if (methodsOnly) {
            switch (syntax) {
                case MethodDeclarationSyntax methodDecl ->
                    declareNamespaceMethod(binder, methodDecl);
                case ImplBlockSyntax implBlock ->
                    declareTypeMembers(binder, implBlock, true);
                default -> {
                }
            }
        } else {
            switch (syntax) {
                case VariableDeclarationSyntax constantDecl ->
                    declareVariable(binder, constantDecl);
                case ImplBlockSyntax implBlock ->
                    declareTypeMembers(binder, implBlock, false);
                default -> {
                }
            }
        }
    }

    private void declareCollection(MemberBinder binder, CollectionDeclarationSyntax decl) {
        Preconditions.checkArgument(decl != null, "Collection declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof NamespaceSymbol ownerNamespace)) {
            throw new IllegalStateException("Collection declarations should only be allowed directly under namespaces");
        }

        var collectionName = decl.identifier().text();
        if (collectionName.isEmpty() || !Character.isUpperCase(collectionName.charAt(0))) {
            throw SemanticErrors.lowercaseCollectionName(decl, collectionName);
        }

        var lazyType = new Lazy<>(() -> resolveCollectionTypeShape(decl));
        var symbol = new SourceNominalTypeSymbol(ownerNamespace, collectionName, decl, lazyType);

        model.symbolCache.put(decl, symbol);

        var seenNames = new HashSet<String>();
        for (int i = 0; i < decl.fields().size(); i++) {
            var fieldDecl = decl.fields().get(i);
            var fieldName = fieldDecl.identifier().text();
            if (!seenNames.add(fieldName)) {
                throw SemanticErrors.fieldRedeclared(decl, fieldName, collectionName);
            }

            var fieldSymbol = new SourceFieldSymbol(fieldName, fieldDecl, symbol, i, new Lazy<>(() -> binder.resolveType(fieldDecl.type())));
            model.symbolCache.put(fieldDecl, fieldSymbol);
            symbol.addMember(fieldSymbol);
        }

        declare(ownerNamespace, symbol);
    }

    private void declareTypeMembers(MemberBinder parentBinder, ImplBlockSyntax decl, boolean methodsOnly) {
        Preconditions.checkArgument(decl != null, "Impl block syntax cannot be null");

        var resolvedType = parentBinder.resolveType(decl.type());

        var comp = model.getCompilation();
        var targetTypeSymbol = comp.getTypeRegistry().bindType(resolvedType);

        TypeSymbol ownerTypeSymbol = targetTypeSymbol;
        if (!targetTypeSymbol.isSource()) {
            var globalNamespace = comp.getGlobalNamespace();
            var extensionSymbol = new SourceExtensionSymbol(globalNamespace, targetTypeSymbol, decl);
            targetTypeSymbol.addExtension(extensionSymbol);
            ownerTypeSymbol = extensionSymbol;
        }

        var binder = new MemberBinder(parentBinder, ownerTypeSymbol);
        model.symbolCache.put(decl, ownerTypeSymbol);

        for (var member : decl.members()) {
            declareTypeMember(binder, member, methodsOnly);
        }
    }

    private void declareTypeMember(MemberBinder binder, MemberSyntax syntax, boolean methodsOnly) {
        if (methodsOnly) {
            if (syntax instanceof MethodDeclarationSyntax methodDecl) {
                declareMethod(binder, methodDecl);
            }
        } else {
            switch (syntax) {
                case VariableDeclarationSyntax constantDecl ->
                    declareVariable(binder, constantDecl);
                case ImplBlockSyntax implBlock ->
                    throw SemanticErrors.forbiddenNestedDeclaration(syntax, "impl block");
                case CollectionDeclarationSyntax ignored ->
                    throw SemanticErrors.forbiddenNestedDeclaration(syntax, "collection");
                default -> {
                }
            }
        }
    }

    private void declareMethod(MemberBinder binder, MethodDeclarationSyntax decl) {
        Preconditions.checkArgument(decl != null, "Method declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof TypeSymbol ownerType)) {
            throw new IllegalStateException("MemberBinder's owner should always be a TypeSymbol when declaring methods");
        }

        var name = decl.identifier().text();
        var returnType = (decl.returnType() == null) ? LangType.Void : binder.resolveType(decl.returnType());
        var params = new ArrayList<SourceParameterSymbol>();
        var seenParameterNames = new HashSet<String>();
        var firstReceiverParamIdx = -1;

        for (int i = 0; i < decl.parameters().size(); i++) {
            var p = decl.parameters().get(i);

            if (p.isReceiver()) {
                if (firstReceiverParamIdx != -1) {
                    throw SemanticErrors.multipleReceiverParameters(p, decl.parameters().get(firstReceiverParamIdx), ownerType.name());
                }
                if (i != 0) {
                    throw SemanticErrors.receiverParameterNotFirst(p, ownerType.name());
                }
                firstReceiverParamIdx = i;
                continue;
            }

            var pname = p.identifier().text();
            var parameter = new SourceParameterSymbol(pname, p, i, binder.resolveType(p.type()));
            params.add(parameter);

            if (!seenParameterNames.add(pname)) {
                var existing = params.stream().filter(pr -> pr.name().equals(pname)).findFirst()
                        .orElseThrow(() -> new IllegalStateException("Parameter should be in the list"));
                throw SemanticErrors.symbolRedeclared(existing, parameter);
            }
        }

        var method = new SourceMethodSymbol(name, decl, owner, returnType, params, firstReceiverParamIdx == -1);
        declare(owner, method);
    }

    private MethodSymbol declareNamespaceMethod(MemberBinder binder, MethodDeclarationSyntax decl) {
        Preconditions.checkArgument(binder != null, "Binder cannot be null");
        Preconditions.checkArgument(decl != null, "Method declaration cannot be null");

        var owner = binder.getOwnerSymbol();
        if (!(owner instanceof NamespaceSymbol ownerNamespace)) {
            throw new IllegalStateException("Function declarations should only be allowed directly under namespaces");
        }

        var name = decl.identifier().text();
        var returnType = (decl.returnType() == null) ? LangType.Void : binder.resolveType(decl.returnType());
        var params = new ArrayList<SourceParameterSymbol>();
        var seenParameterNames = new HashSet<String>();

        for (int i = 0; i < decl.parameters().size(); i++) {
            var p = decl.parameters().get(i);
            if (p.isReceiver()) {
                throw SemanticErrors.receiverParameterInFunction(p, decl.identifier().text());
            }

            var paramName = p.identifier().text();
            var parameter = new SourceParameterSymbol(paramName, p, i, binder.resolveType(p.type()));
            params.add(parameter);

            if (!seenParameterNames.add(paramName)) {
                var existing = params.stream().filter(pr -> pr.name().equals(paramName)).findFirst()
                        .orElseThrow(() -> new IllegalStateException("Parameter should be in the list"));
                throw SemanticErrors.symbolRedeclared(existing, parameter);
            }
        }

        var method = new SourceMethodSymbol(name, decl, ownerNamespace, returnType, params, true);
        declare(ownerNamespace, method);
        return method;
    }

    private VariableSymbol declareVariable(MemberBinder binder, VariableDeclarationSyntax decl) {
        Preconditions.checkArgument(decl != null, "Variable declaration cannot be null");
        Preconditions.checkArgument(decl.variableDeclarator() != null, "Variable declarator cannot be null");

        var declarator = decl.variableDeclarator();
        var owner = binder.getOwnerSymbol();

        var bound = binder.bindVariableDeclarator(declarator, false, owner);
        declare(owner, (SourceSymbol) bound.variable());

        if (declarator.initializer() != null && bound.initializer() != null) {
            model.cacheBoundNode(declarator.initializer(), bound.initializer());
        }

        return bound.variable();
    }

    private void declare(NamespaceOrTypeSymbol owner, SourceSymbol symbol) {
        Preconditions.checkArgument(owner != null, "Owner cannot be null");
        Preconditions.checkArgument(symbol != null, "Symbol cannot be null");

        if (symbol instanceof CallableSymbol callableSymbol) {
            var members = owner.getMembers(symbol.name());
            var parameterTypes = callableSymbol.type().parameterTypes();
            for (var member : members) {
                if (member instanceof CallableSymbol existingCallable) {
                    if (existingCallable.type().parameterTypes().equals(parameterTypes)) {
                        switch (existingCallable) {
                            case MethodSymbol m ->
                                throw SemanticErrors.methodRedeclared((SourceMethodSymbol) symbol, m);
                            default ->
                                throw SemanticErrors.symbolRedeclared(existingCallable, symbol);
                        }
                    }
                } else {
                    throw SemanticErrors.symbolRedeclared(member, symbol, symbol.name());
                }
            }
        } else {
            var existing = owner.getMember(symbol.name());
            if (existing != null) {
                throw SemanticErrors.symbolRedeclared(existing, symbol);
            }
        }

        model.symbolCache.put(symbol.declaringSyntax(), symbol);
        owner.addMember(symbol);
    }

    private LangType resolveCollectionTypeShape(CollectionDeclarationSyntax decl) {
        Preconditions.checkArgument(decl != null, "Collection declaration cannot be null");
        var symbol = (SourceNominalTypeSymbol) model.symbolCache.get(decl);
        Debug.requireNotNull(symbol, "Collection symbol should have been created in the declaration phase");

        var fieldTypes = new LinkedHashMap<String, LangType>();
        for (int i = 0; i < decl.fields().size(); i++) {
            var fieldDecl = decl.fields().get(i);
            var fieldSymbol = (SourceFieldSymbol) model.symbolCache.get(fieldDecl);
            fieldTypes.put(fieldSymbol.name(), fieldSymbol.type());
        }

        return new CollectionType(fieldTypes);
    }

    public enum Phase {
        RESOLVE_IMPORTS,
        DECLARE_TYPES,
        DECLARE_METHODS,
        DECLARE_REMAINING_SYMBOLS
    }
}
