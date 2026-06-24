package com.orca.compiler.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundMethod;
import com.orca.compiler.core.boundtree.BoundNamespace;
import com.orca.compiler.core.boundtree.BoundProgram;
import com.orca.compiler.core.boundtree.BoundType;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.statements.BoundBlockStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.codegen.ClassDirWriter;
import com.orca.compiler.core.codegen.CompilationMetadata;
import com.orca.compiler.core.codegen.Emitter;
import com.orca.compiler.core.codegen.JarPackager;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.externals.ExternalSymbolResolver;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.semantics.SymbolDeclarator;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.NamespaceSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.SymbolModifiers;
import com.orca.compiler.core.symbols.sources.SourceExtensionSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceNamespaceSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.symbols.synthesized.GlobalNamespaceSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedEntryClassSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedMethodSymbol;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxTree;
import com.orca.compiler.core.syntax.members.MethodDeclarationSyntax;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.LangType;

public final class Compilation {

    private final CompilerOptions options;

    private final ExternalSymbolResolver resolver;
    private final List<SemanticModel> semanticModels;

    private final TypeRegistry typeRegistry = new TypeRegistry(this);
    private final GlobalNamespaceSymbol globalNamespace = new GlobalNamespaceSymbol(this);

    private CompilationResult<BoundProgram> cachedBoundProgramResult;

    private boolean declarationsComplete = false;
    private boolean boundProgramComputed = false;

    public Compilation(CompilerOptions options) {
        this.options = options;
        this.semanticModels = initializeSemanticModels(options);
        this.resolver = new ExternalSymbolResolver(options.getClassPaths(), options.includeJdk());
    }

    public List<SyntaxTree> getSyntaxTrees() {
        return semanticModels.stream()
                .map(SemanticModel::getSyntaxTree)
                .toList();
    }

    public SemanticModel getSemanticModel(TextSource source) {
        for (var semanticModel : semanticModels) {
            if (semanticModel.getSource() == source) {
                return semanticModel;
            }
        }

        throw new IllegalArgumentException("No semantic model found for source: " + source);
    }

    public SemanticModel getSemanticModel(SyntaxTree syntaxTree) {
        return getSemanticModel(syntaxTree.source());
    }

    public CompilationResult<BoundProgram> getBoundProgram() {
        if (!boundProgramComputed) {
            cachedBoundProgramResult = computeBoundProgram();
            boundProgramComputed = true;
        }

        return cachedBoundProgramResult;
    }

    private CompilationResult<BoundProgram> computeBoundProgram() {
        var diagnosticCollector = new DiagnosticCollector();

        try {
            ensureDeclarationsComplete();

            var boundNamespace = new BoundNamespace(globalNamespace);
            for (SemanticModel semanticModel : semanticModels) {
                var bindingResult = semanticModel.bindFullSemanticModel();
                diagnosticCollector.mergeFrom(bindingResult.diagnostics());

                if (bindingResult instanceof CompilationResult.Success<BoundNamespace> success) {
                    var boundNamespaceFromModel = success.value();
                    if (boundNamespaceFromModel.getSymbol().isGlobalNamespace()) {
                        boundNamespaceFromModel.getMethods().forEach(boundNamespace::addMethod);
                        boundNamespaceFromModel.getTypes().forEach(boundNamespace::addType);
                        boundNamespaceFromModel.getVariables().forEach(boundNamespace::addVariable);
                        boundNamespaceFromModel.getNamespaces().forEach(boundNamespace::addNamespace);
                    } else {
                        boundNamespace.addNamespace(boundNamespaceFromModel);
                    }
                }
            }

            for (var extension : collectExtensions()) {
                bindExtension(boundNamespace, extension);
            }

            if (options.getOutputKind() == OutputKind.APPLICATION) {
                var entryClassSymbol = getEntryClassSymbol();
                var boundEntryClass = bindEntryClassSymbol(boundNamespace, entryClassSymbol);
                boundNamespace.addType(boundEntryClass);
            }

            var boundProgram = new BoundProgram(boundNamespace);
            var diagnostics = diagnosticCollector.freeze();
            if (diagnostics.anyError()) {
                return CompilationResult.failure(diagnostics);
            }

            return CompilationResult.success(diagnostics, boundProgram);
        } catch (CompilerException e) {
            diagnosticCollector.report(e.diagnostic());
            return CompilationResult.failure(diagnosticCollector.freeze());
        }
    }

    /**
     * Compiles the program and emits the output to the specified output path.
     * If there are any compilation errors, they will be reported to the
     * diagnostics collector.
     *
     * @return true if the compilation was successful, false otherwise
     */
    public CompilationResult<Void> compile() {
        var boundProgramResult = getBoundProgram();

        switch (boundProgramResult) {
            case CompilationResult.Failure<BoundProgram> failure -> {
                return CompilationResult.failure(failure.diagnostics());
            }
            case CompilationResult.Success<BoundProgram> success -> {
                var boundProgram = success.value();

                var compilationDiagnostics = new DiagnosticCollector();
                compilationDiagnostics.mergeFrom(success.diagnostics());

                switch (options.getOutputFormat()) {
                    case JAR -> {
                        try {
                            emitJar(boundProgram, options.getOutputPath());
                        } catch (IOException e) {
                            compilationDiagnostics.report(DiagnosticFactory.inputIoError(e.getMessage()));
                            return CompilationResult.failure(compilationDiagnostics.freeze());
                        }
                    }
                    case CLASS_DIRECTORY -> {
                        emitClasses(boundProgram, options.getOutputPath());
                    }
                }

                return CompilationResult.success(compilationDiagnostics.freeze());
            }
        }
    }

    private void emitJar(BoundProgram program, Path outputPath) throws IOException {
        var classes = new LinkedHashMap<String, byte[]>();
        new Emitter(program, classes::put).emit();

        var metadata = CompilationMetadata.of(options, new ArrayList<>(classes.keySet()));
        JarPackager.pack(outputPath, metadata, classes);
    }

    private void emitClasses(BoundProgram program, Path outputPath) {
        new Emitter(program, new ClassDirWriter(outputPath)).emit();
    }

    private BoundType bindEntryClassSymbol(BoundNamespace boundNamespace, SynthesizedEntryClassSymbol entryClassSymbol) {
        var boundEntryClass = new BoundType(boundNamespace, entryClassSymbol);
        var mainMethodSymbol = entryClassSymbol.getMainMethodSymbol();

        var entryMainMethodSymbol = new SynthesizedMethodSymbol(entryClassSymbol, CompilerConstants.DEFAULT_ENTRY_METHOD, LangType.Void, List.of(LangType.arrayOf(LangType.String)), SymbolModifiers.STATIC);
        var argsParameter = entryMainMethodSymbol.getParameter(0);

        // Only forward String[] args if the user's main actually accepts them.
        var userMainParams = mainMethodSymbol.parameters();
        var callArgs = userMainParams.isEmpty()
                ? List.<BoundExpression>of()
                : List.<BoundExpression>of(BoundReferenceExpr.of(argsParameter));

        var entryMainBody = new BoundBlockStmt(
                new BoundExpressionStmt(
                        new BoundMethodCallExpr(
                                BoundReferenceExpr.of(mainMethodSymbol),
                                callArgs
                        )
                )
        );

        var boundEntryMainMethod = new BoundMethod(boundEntryClass, entryMainMethodSymbol, entryMainBody);
        boundEntryClass.addMethod(boundEntryMainMethod);

        return boundEntryClass;
    }

    private void bindExtension(BoundNamespace boundNamespace, SourceExtensionSymbol extension) {
        var boundType = new BoundType(boundNamespace, extension);
        boundNamespace.addType(boundType);

        for (var members : extension.getMembers()) {
            switch (members) {
                case SourceMethodSymbol methodSymbol -> {
                    var methodDecl = (MethodDeclarationSyntax) methodSymbol.declaringSyntax();
                    var semanticModel = getSemanticModelForSyntaxNode(methodDecl);

                    semanticModel.bindMethodDecl(boundType, methodDecl);
                }
                case SourceVariableSymbol variableSymbol -> {
                    var variableDecl = (VariableDeclaratorSyntax) variableSymbol.declaringSyntax();
                    var semanticModel = getSemanticModelForSyntaxNode(variableDecl);

                    semanticModel.bindVariableDecl(boundType, variableDecl);
                }
                default ->
                    throw new IllegalStateException("Unsupported member in extension: " + members.getClass().getName());
            }
        }
    }

    private SemanticModel getSemanticModelForSyntaxNode(SyntaxNode node) {
        for (var semanticModel : semanticModels) {
            if (semanticModel.getSyntaxTree().source() == node.source()) {
                return semanticModel;
            }
        }

        throw new IllegalStateException("No semantic model found for syntax node: " + node.getClass().getName());
    }

    private List<SourceExtensionSymbol> collectExtensions() {
        var extensions = new java.util.ArrayList<SourceExtensionSymbol>();
        for (var member : globalNamespace.getMembers()) {
            if (member instanceof SourceExtensionSymbol extension) {
                extensions.add(extension);
            }
        }

        return extensions;
    }

    private SynthesizedEntryClassSymbol getEntryClassSymbol() {
        var mainCandidates = findMainFunctionCandidates();
        if (mainCandidates.isEmpty()) {
            throw CompilerException.wrap(DiagnosticFactory.missingMainFunction());
        } else if (mainCandidates.size() > 1) {
            throw CompilerException.wrap(DiagnosticFactory.ambiguousMainFunction(mainCandidates));
        }

        var mainFunction = mainCandidates.get(0);
        mainFunction.markAsEntryPoint();

        return new SynthesizedEntryClassSymbol(this, mainFunction);
    }

    private List<SourceMethodSymbol> findMainFunctionCandidates() {
        var candidates = new java.util.ArrayList<SourceMethodSymbol>();

        for (var member : globalNamespace.getMembersWithChildren()) {
            if (!member.name().equals(CompilerConstants.DEFAULT_ENTRY_METHOD)) {
                continue;
            }

            if (member instanceof SourceMethodSymbol methodSymbol && isMainFunctionCandidate(methodSymbol)) {
                candidates.add(methodSymbol);
            }
        }

        return candidates;
    }

    private boolean isMainFunctionCandidate(SourceMethodSymbol symbol) {
        if (!symbol.name().equals("main")) {
            return false;
        }

        var parameters = symbol.parameters();
        if (parameters.size() > 1) {
            return false;
        } else if (parameters.size() == 1) {
            var paramType = parameters.get(0).type();
            if (!(paramType instanceof ArrayType arrayType) || (arrayType.getElementType() != LangType.String)) {
                return false;
            }
        }

        return symbol.returnType().isVoid();
    }

    public List<String> suggestSymbols(IdentifierSyntax identifier) {
        var suggestions = new java.util.ArrayList<String>();

        // for (var source : context.arguments().getJarSources()) {
        //     // suggestions.addAll(source.suggestExternalPackageNamespaces(identifier.text()));
        //     // suggestions.addAll(source.suggestExternalClasses(identifier.text()));
        // }
        return suggestions;
    }

    public Symbol resolveSymbolFromGlobalNamespace(String fullName) {
        var nameParts = fullName.split("\\.");
        Symbol current = globalNamespace;
        for (var part : nameParts) {
            if (current instanceof NamespaceOrTypeSymbol ns) {
                current = ns.getMember(part);
                if (current == null) {
                    return null;
                }
            } else {
                return null;
            }
        }

        return current;
    }

    public Symbol resolveExternalSymbol(String name) {
        if (resolver.exists(name)) {
            return resolver.getOrCreateSymbol(name);
        }

        return null;
    }

    public NamespaceSymbol getOrCreatePackageNamespace(IdentifierSyntax identifier) {
        return getOrCreatePackageNamespace(globalNamespace, identifier);
    }

    public TypeRegistry getTypeRegistry() {
        return typeRegistry;
    }

    // public DiagnosticCollector diagnostics() {
    //     return diagnostics;
    // }
    public CompilerOptions options() {
        return options;
    }

    public GlobalNamespaceSymbol getGlobalNamespace() {
        return globalNamespace;
    }

    private List<SemanticModel> initializeSemanticModels(CompilerOptions options) {
        var result = new java.util.ArrayList<SemanticModel>();

        for (var source : options.getSources()) {
            result.add(new SemanticModel(this, source));
        }

        return result;
    }

    private NamespaceSymbol getOrCreatePackageNamespace(NamespaceSymbol owner, IdentifierSyntax identifier) {
        return switch (identifier) {
            case SpecialTypeIdentifierSyntax special ->
                throw new IllegalStateException("Special type identifiers cannot be used as namespaces: " + special.text());
            case SimpleIdentifierSyntax simple -> {
                var member = owner.getMember(simple.text());
                if (member == null) {
                    var newNamespace = new SourceNamespaceSymbol(owner, simple.text(), identifier);
                    owner.addMember(newNamespace);

                    yield newNamespace;
                }

                if (member instanceof NamespaceSymbol ns) {
                    yield ns;
                }

                throw new IllegalStateException("Name conflict: " + simple.text() + " in " + owner.getFullName() + " is not a namespace");
            }
            case QualifiedIdentifierSyntax qualified -> {
                var namespace = getOrCreatePackageNamespace(owner, qualified.left());
                yield getOrCreatePackageNamespace(namespace, qualified.right());
            }
        };
    }

    public void ensureDeclarationsComplete() {
        if (declarationsComplete) {
            return;
        }
        declarationsComplete = true;
        for (var phase : SymbolDeclarator.Phase.values()) {
            semanticModels.forEach(model -> model.runDeclarationPhase(phase));
        }
    }
}
