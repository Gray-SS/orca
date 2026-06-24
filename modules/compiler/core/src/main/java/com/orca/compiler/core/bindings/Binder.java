package com.orca.compiler.core.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.orca.compiler.core.Compilation;
import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.Debug;
import com.orca.compiler.core.boundtree.BoundExpression;
import com.orca.compiler.core.boundtree.BoundNode;
import com.orca.compiler.core.boundtree.BoundStatement;
import com.orca.compiler.core.boundtree.BoundVariableDeclarator;
import com.orca.compiler.core.boundtree.ConstantFolding;
import com.orca.compiler.core.boundtree.constants.BoundConstant;
import com.orca.compiler.core.boundtree.constants.BoundIntConstant;
import com.orca.compiler.core.boundtree.expressions.BoundArrayAccessExpr;
import com.orca.compiler.core.boundtree.expressions.BoundArrayLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundAssignmentExpr;
import com.orca.compiler.core.boundtree.expressions.BoundBinaryExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConstructObjectExpr;
import com.orca.compiler.core.boundtree.expressions.BoundConversionExpr;
import com.orca.compiler.core.boundtree.expressions.BoundErrorExpr;
import com.orca.compiler.core.boundtree.expressions.BoundLiteralExpr;
import com.orca.compiler.core.boundtree.expressions.BoundMethodCallExpr;
import com.orca.compiler.core.boundtree.expressions.BoundOperators;
import com.orca.compiler.core.boundtree.expressions.BoundReferenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundSequenceExpr;
import com.orca.compiler.core.boundtree.expressions.BoundUnaryExpr;
import com.orca.compiler.core.boundtree.statements.BoundErrorStmt;
import com.orca.compiler.core.boundtree.statements.BoundExpressionStmt;
import com.orca.compiler.core.boundtree.statements.BoundVariableDeclStmt;
import com.orca.compiler.core.diagnostics.DiagnosticCollector;
import com.orca.compiler.core.lexer.TokenKind;
import com.orca.compiler.core.semantics.OverloadResolver;
import com.orca.compiler.core.semantics.SemanticErrors;
import com.orca.compiler.core.semantics.SemanticModel;
import com.orca.compiler.core.semantics.SymbolInfo;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.ConstructorSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.sources.SourceVariableSymbol;
import com.orca.compiler.core.symbols.synthesized.SynthesizedVariableSymbol;
import com.orca.compiler.core.syntax.ExpressionSyntax;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.expressions.ArrayAccessExpr;
import com.orca.compiler.core.syntax.expressions.ArrayLiteralExpression;
import com.orca.compiler.core.syntax.expressions.AssignmentExpr;
import com.orca.compiler.core.syntax.expressions.AssignmentOperatorKind;
import com.orca.compiler.core.syntax.expressions.BinaryExpr;
import com.orca.compiler.core.syntax.expressions.BinaryOperatorKind;
import com.orca.compiler.core.syntax.expressions.ErrorExpressionSyntax;
import com.orca.compiler.core.syntax.expressions.IdentifierExpr;
import com.orca.compiler.core.syntax.expressions.InvocationExpr;
import com.orca.compiler.core.syntax.expressions.LiteralExpr;
import com.orca.compiler.core.syntax.expressions.MemberAccessExpr;
import com.orca.compiler.core.syntax.expressions.UnaryAssignmentExpr;
import com.orca.compiler.core.syntax.expressions.UnaryExpr;
import com.orca.compiler.core.syntax.expressions.UnaryOperatorKind;
import com.orca.compiler.core.syntax.nodes.IdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.QualifiedIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SimpleIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.SpecialTypeIdentifierSyntax;
import com.orca.compiler.core.syntax.nodes.VariableDeclaratorSyntax;
import com.orca.compiler.core.syntax.statements.ErrorStatementSyntax;
import com.orca.compiler.core.syntax.types.ArrayTypeSyntax;
import com.orca.compiler.core.syntax.types.ErrorTypeSyntax;
import com.orca.compiler.core.syntax.types.IdentifierTypeSyntax;
import com.orca.compiler.core.syntax.types.SpecialTypeSyntax;
import com.orca.compiler.core.syntax.types.TypeSyntax;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.typesystem.ArrayType;
import com.orca.compiler.core.typesystem.Conversions;
import com.orca.compiler.core.typesystem.LangType;

public abstract class Binder {

    private final Binder parent;

    public Binder(Binder parent) {
        this.parent = parent;
    }

    protected final Binder getParent() {
        return parent;
    }

    protected Compilation getCompilation() {
        return parent.getCompilation();
    }

    protected SemanticModel getSemanticModel() {
        return parent.getSemanticModel();
    }

    protected DiagnosticCollector getDiagnosticCollector() {
        return parent.getDiagnosticCollector();
    }

    protected final BoundNode getBoundNodeFor(SyntaxNode syntax) throws CompilerException {
        Debug.requireNotNull(syntax, "Syntax node cannot be null");

        var semanticModel = getSemanticModel();
        return semanticModel.bind(syntax);
    }

    protected final Binder getBinderFor(SyntaxNode syntax) throws CompilerException {
        Debug.requireNotNull(syntax, "Syntax node cannot be null");

        var semanticModel = getSemanticModel();
        return semanticModel.getBinder(syntax);
    }

    /**
     * Resolves a symbol name by searching the scope hierarchy. First checks the
     * current binder's scope, then delegates to parent recursively. This
     * implements lexical scoping: inner scopes shadow outer scopes.
     */
    public @Nullable
    Symbol lookupSymbol(String name) {
        Debug.requireNotNullOrEmpty(name, "Symbol name cannot be null or empty");

        if (parent != null) {
            return parent.lookupSymbol(name);
        }

        return null;
    }

    /**
     * Similar to lookupSymbol but returns all symbols with the given name in
     * the scope hierarchy, to support overload sets.
     *
     * @param name The name of the symbol to look up.
     * @return An iterable of symbols with the given name, starting from the
     * innermost scope to the outermost. If no symbols are found, returns an
     * empty iterable.
     */
    public List<Symbol> lookupSymbols(String name) {
        Debug.requireNotNullOrEmpty(name, "Symbol name cannot be null or empty");

        if (parent != null) {
            return parent.lookupSymbols(name);
        }

        return new ArrayList<>();
    }

    public List<Symbol> lookupSymbols(IdentifierSyntax identifier) {
        Debug.requireNotNull(identifier, "Identifier cannot be null");

        return lookupSymbols(identifier.text());
    }

    public TypeSymbol lookupType(String name) {
        var symbol = lookupSymbol(name);
        if (symbol instanceof TypeSymbol typeSymbol) {
            return typeSymbol;
        }

        return null;
    }

    public SymbolInfo getSymbolInfo(@Nonnull IdentifierSyntax identifier) {
        if (identifier.isError()) {
            return SymbolInfo.notFound(identifier);
        }

        var compilation = getCompilation();
        Symbol externalSymbol = compilation.resolveExternalSymbol(identifier.qualifiedName());
        if (externalSymbol != null) {
            return new SymbolInfo(externalSymbol, identifier);
        }

        return switch (identifier) {
            case SpecialTypeIdentifierSyntax special -> {
                var specialTypeSymbol = compilation.getTypeRegistry().bindSpecialType(special.getKind());
                yield new SymbolInfo(specialTypeSymbol, special);
            }
            case SimpleIdentifierSyntax simple ->
                getSimpleSymbolInfo(simple);
            case QualifiedIdentifierSyntax qualified ->
                getQualifiedSymbolInfo(qualified);
        };
    }

    public Symbol resolveSymbol(IdentifierSyntax identifier) {
        var symbolInfo = getSymbolInfo(identifier);
        switch (symbolInfo.failureReason()) {
            case None -> {
            }
            case Ambiguous -> {
                throw SemanticErrors.ambiguousIdentifier(symbolInfo.syntax(), symbolInfo.symbol().name(), lookupSymbols(identifier));
            }
            case NotFound -> {
                throw SemanticErrors.undeclaredIdentifier(symbolInfo.syntax(), symbolInfo.symbol().name());
            }
            case InstanceAccessOnStaticOnly -> {
                throw SemanticErrors.instanceMemberAccessOnStaticContext(symbolInfo.syntax(), symbolInfo.symbol());
            }
            case StaticAccessOnInstanceOnly -> {
                throw SemanticErrors.staticMemberAccessOnInstanceContext(symbolInfo.syntax(), symbolInfo.symbol());
            }
            case NotATypeOrNamespace -> {
                throw SemanticErrors.symbolUsedLikeATypeOrNamespace(symbolInfo.syntax(), symbolInfo.symbol());
            }
            case NotAValue -> {
                throw SemanticErrors.symbolNotAValue(symbolInfo.syntax(), symbolInfo.symbol());
            }
        }

        return symbolInfo.symbol();
    }

    private SymbolInfo getSimpleSymbolInfo(SimpleIdentifierSyntax simple) {
        var symbol = lookupSymbol(simple.name());
        if (symbol != null) {
            return new SymbolInfo(symbol, simple);
        }

        return SymbolInfo.notFound(simple);
    }

    private SymbolInfo getQualifiedSymbolInfo(QualifiedIdentifierSyntax qualified) {
        var leftSymbolInfo = getSymbolInfo(qualified.left());
        if (leftSymbolInfo.isFailure()) {
            return leftSymbolInfo;
        }

        var leftSymbol = leftSymbolInfo.symbol();
        if (!(leftSymbol instanceof NamespaceOrTypeSymbol ownerSymbol)) {
            // throw SemanticErrors.symbolUsedLikeATypeOrNamespace(qualified.left(), leftSymbolInfo);
            return new SymbolInfo(leftSymbol, qualified.left(), SymbolInfo.FailureReason.NotATypeOrNamespace);
        }

        String memberName = qualified.right().name();
        List<Symbol> members;
        if (ownerSymbol instanceof TypeSymbol typeSymbol) {
            members = typeSymbol.getMembersWithExtensions(memberName);
        } else {
            members = ownerSymbol.getMembers(memberName);
        }

        if (members.isEmpty()) {
            // throw SemanticErrors.memberNotFound(qualified.right(), memberName, ownerSymbol);
            return SymbolInfo.notFound(qualified.right());
        }

        var staticMembers = members.stream()
                .filter(Symbol::canBeAccessedThroughStatic)
                .toList();

        if (staticMembers.isEmpty()) {
            // throw SemanticErrors.instanceMemberAccessOnStaticContext(qualified.right(), members.get(0));
            return new SymbolInfo(Symbol.missing(qualified.right().name()), qualified.right(), SymbolInfo.FailureReason.StaticAccessOnInstanceOnly);
        }

        if (staticMembers.size() == 1) {
            return new SymbolInfo(staticMembers.get(0), qualified.right(), SymbolInfo.FailureReason.None);
        }

        var callables = staticMembers.stream()
                .filter(m -> m instanceof CallableSymbol)
                .map(m -> (CallableSymbol) m)
                .toList();

        if (callables.size() != staticMembers.size()) {
            // throw SemanticErrors.ambiguousMemberAccess(qualified.right(), memberName, ownerSymbol, staticMembers);
            return new SymbolInfo(Symbol.missing(qualified.right().name()), qualified.right(), SymbolInfo.FailureReason.Ambiguous);
        }

        return new SymbolInfo(callables.get(0), qualified.right(), SymbolInfo.FailureReason.None);
    }

    public List<Symbol> resolveSymbols(IdentifierSyntax identifier) throws CompilerException {
        return switch (identifier) {
            case SpecialTypeIdentifierSyntax special ->
                List.of(resolveSymbol(special));
            case SimpleIdentifierSyntax simple -> {
                yield lookupSymbols(simple.name());
            }
            case QualifiedIdentifierSyntax qualified -> {
                var leftSymbol = getCompilation().resolveExternalSymbol(qualified.qualifiedName());
                if (leftSymbol != null) {
                    yield List.of(leftSymbol);
                }

                leftSymbol = resolveSymbol(qualified.left());
                if (!(leftSymbol instanceof NamespaceOrTypeSymbol ownerSymbol)) {
                    throw SemanticErrors.symbolUsedLikeATypeOrNamespace(qualified.left(), leftSymbol);
                }

                if (ownerSymbol instanceof TypeSymbol typeSymbol) {
                    yield typeSymbol.getMembersWithExtensions(qualified.right().name());
                } else {
                    yield ownerSymbol.getMembers(qualified.right().name());
                }
            }
        };
    }

    public LangType resolveType(TypeSyntax typeSyntax) throws CompilerException {
        Debug.requireNotNull(typeSyntax, "Type syntax cannot be null");

        return switch (typeSyntax) {
            case ErrorTypeSyntax e ->
                LangType.Error;
            case SpecialTypeSyntax special -> {
                yield switch (special.getKind()) {
                    case Byte ->
                        LangType.Byte;
                    case Short ->
                        LangType.Short;
                    case Int ->
                        LangType.Int;
                    case Long ->
                        LangType.Long;
                    case Float ->
                        LangType.Float;
                    case Double ->
                        LangType.Double;
                    case Bool ->
                        LangType.Bool;
                    case Char ->
                        LangType.Char;
                    case String ->
                        LangType.String;
                    case Void ->
                        LangType.Void;
                    case Any ->
                        LangType.Any;
                };
            }
            case IdentifierTypeSyntax identifierTypeSyntax -> {
                Symbol symbol = resolveSymbol(identifierTypeSyntax.identifier());
                if (symbol instanceof TypeSymbol typeSymbol) {
                    yield typeSymbol.type();
                }

                throw SemanticErrors.symbolNotAType(identifierTypeSyntax.identifier(), symbol);
            }
            case ArrayTypeSyntax arrayTypeSyntax -> {
                var elementType = resolveType(arrayTypeSyntax.elementType());
                yield LangType.arrayOf(elementType);
            }
        };
    }

    public BoundNode bind(SyntaxNode syntax) throws CompilerException {
        Debug.requireNotNull(syntax, "Syntax node cannot be null");
        var semanticModel = getSemanticModel();
        if (semanticModel.isNodeCached(syntax)) {
            return semanticModel.getCachedBoundNode(syntax);
        }

        var boundNode = switch (syntax) {
            case StatementSyntax stmt ->
                bindStatement(stmt);
            case ExpressionSyntax expr ->
                bindExpr(expr);
            default ->
                throw new IllegalStateException("Unsupported syntax node for binding: " + syntax.getClass().getSimpleName() + " in " + getClass().getSimpleName());
        };

        semanticModel.cacheBoundNode(syntax, boundNode);
        boundNode.setSyntax(syntax);
        return boundNode;
    }

    public final BoundStatement bindStatement(StatementSyntax stmt) throws CompilerException {
        Debug.requireNotNull(stmt, "Statement syntax node cannot be null");

        if (stmt instanceof ErrorStatementSyntax error) {
            return new BoundErrorStmt(error);
        }

        var boundStatement = bindStatementInternal(stmt);
        boundStatement.setSyntax(stmt);

        return boundStatement;
    }

    protected BoundStatement bindStatementInternal(StatementSyntax stmt) throws CompilerException {
        Debug.requireNotNull(stmt, "Statement syntax node cannot be null");

        if (parent != null) {
            return parent.bindStatementInternal(stmt);
        }

        throw SemanticErrors.unexpectedError(stmt, "No binder found to bind statement: " + stmt + " in binder " + getClass().getSimpleName());
    }

    public final BoundExpression bindExpr(ExpressionSyntax expr) throws CompilerException {
        var bound = bindExprInternal(expr);
        bound.setSyntax(expr);

        return bound;
    }

    protected BoundExpression bindExprInternal(ExpressionSyntax expr) throws CompilerException {
        Objects.requireNonNull(expr);

        return switch (expr) {
            case ErrorExpressionSyntax e ->
                new BoundErrorExpr(e);
            case LiteralExpr e ->
                bindLiteralExpr(e);
            case IdentifierExpr e ->
                bindIdentifierExpr(e);
            case UnaryExpr e ->
                bindUnaryExpr(e);
            case BinaryExpr e ->
                bindBinaryExpr(e);
            case InvocationExpr e ->
                bindInvocationExpr(e, null);
            case AssignmentExpr e ->
                bindAssignmentExpr(e);
            case UnaryAssignmentExpr e ->
                bindUnaryAssignmentExpr(e);
            case MemberAccessExpr e ->
                bindMemberAccessExpr(e);
            case ArrayLiteralExpression e ->
                bindArrayLiteralExpr(e);
            case ArrayAccessExpr e ->
                bindArrayAccessExpr(e);

            default ->
                throw SemanticErrors.unexpectedError(expr, "Unsupported expression node: " + expr.getClass().getSimpleName());
        };
    }

    protected BoundVariableDeclarator bindVariableDeclarator(VariableDeclaratorSyntax declarator, boolean isLocal) throws CompilerException {
        final var name = declarator.identifier().text();

        final var modifierToken = declarator.modifierToken();
        final var modifierKind = modifierToken.kind();

        final var isConst = modifierKind == TokenKind.ConstKeyword;
        final var isLet = modifierKind == TokenKind.LetKeyword;
        final var requireInitializer = isConst || isLet;

        final var declaredType = declarator.type() != null ? resolveType(declarator.type()) : null;
        if (declaredType != null && declaredType.isVoid()) {
            throw SemanticErrors.voidVariableType(declarator, name);
        }

        BoundExpression boundInitializer = null;
        if (declarator.initializer() != null) {
            boundInitializer = (BoundExpression) bindExpr(declarator.initializer());

            if (declaredType != null) {
                boundInitializer = applyConversion(declarator, boundInitializer, declaredType);
            }
        }

        final var variableType = (declaredType != null) ? declaredType : (boundInitializer != null ? boundInitializer.type() : null);
        if (variableType == null) {
            throw SemanticErrors.cannotInferVariableType(declarator, name);
        }

        // Check constant type constraint before binding the initializer so the
        // correct diagnostic is reported even when the initializer itself fails.
        if (isConst && !variableType.isPrimitive()) {
            throw SemanticErrors.missingConstantBaseType(declarator, name);
        }

        if (requireInitializer && boundInitializer == null) {
            throw SemanticErrors.missingVariableInitializer(declarator, name, modifierToken.text());
        }

        // Initializer of a constant variable must be compile-time evaluable.
        SourceVariableSymbol variable;
        if (isConst) {
            if (boundInitializer == null) {
                // Already checked but makes the compiler happy
                throw SemanticErrors.missingVariableInitializer(declarator, name, modifierToken.text());
            }

            // Initializer must be compile-time evaluable.
            if (!boundInitializer.isCompileTimeFoldable()) {
                throw SemanticErrors.constantNonCompileTimeFoldableInitializer(declarator, name);
            }

            var constantValue = boundInitializer.getConstant();

            // example: const x := 20; -> 20 is an int literal but needs to be converted to the declared type (e.g., float) of the constant variable
            var convertedConstantValue = constantValue.convertTo(variableType);

            variable = SourceVariableSymbol.createConstant(null, name, declarator, convertedConstantValue, isLocal);
        } else {
            variable = SourceVariableSymbol.createLocal(name, declarator, variableType, isLet);
        }

        return new BoundVariableDeclarator(variable, boundInitializer);
    }

    protected BoundExpression bindExpectedExpr(ExpressionSyntax expr, LangType targetType) throws CompilerException {
        if (expr instanceof InvocationExpr callExpr) {
            return applyConversion(expr, bindInvocationExpr(callExpr, targetType), targetType);
        }

        return applyConversion(expr, bindValueExpr(expr), targetType);
    }

    protected BoundExpression bindValueExpr(ExpressionSyntax expr) throws CompilerException {
        var boundExpr = bindExpr(expr);
        if (boundExpr instanceof BoundReferenceExpr identifierExpr) {
            if (!(identifierExpr.getReferencedSymbol() instanceof ValueSymbol)) {
                throw SemanticErrors.symbolNotAValue(expr, identifierExpr.getReferencedSymbol());
            }
        }

        if (boundExpr.isCompileTimeFoldable()) {
            // Fold constant expressions at compile time to simplify later stages.
            return ConstantFolding.foldExpression(boundExpr);
        }

        return boundExpr;
    }

    protected BoundAssignmentExpr bindAssignmentExpr(AssignmentExpr expr) throws CompilerException {
        var boundTarget = bindExpr(expr.target());
        var boundInitializer = bindExpectedExpr(expr.initializer(), boundTarget.type());
        checkAssignmentTarget(boundTarget);

        var assignmentOperatorKind = switch (expr.operatorToken().kind()) {
            case Equals ->
                AssignmentOperatorKind.Simple;
            case PlusEquals ->
                AssignmentOperatorKind.AdditionAssignment;
            case MinusEquals ->
                AssignmentOperatorKind.SubtractionAssignment;
            case StarEquals ->
                AssignmentOperatorKind.MultiplicationAssignment;
            case SlashEquals ->
                AssignmentOperatorKind.DivisionAssignment;
            case PercentEquals ->
                AssignmentOperatorKind.ModuloAssignment;
            default ->
                throw SemanticErrors.unsupportedAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundInitializer.type());
        };

        var boundAssignmentOperatorResult = BoundOperators.bindAssignmentOperator(assignmentOperatorKind, boundTarget.type(), boundInitializer.type());

        return switch (boundAssignmentOperatorResult.kind()) {
            case SINGLE_MATCH -> {
                var boundOperator = boundAssignmentOperatorResult.getSingle();
                yield new BoundAssignmentExpr(boundTarget, boundOperator, applyConversion(expr.initializer(), boundInitializer, boundTarget.type()));
            }
            case AMBIGUOUS -> {
                throw SemanticErrors.ambiguousAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundInitializer.type(), boundAssignmentOperatorResult.getCandidates());
            }
            case NO_MATCH -> {
                throw SemanticErrors.unsupportedAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundInitializer.type());
            }
        };
    }

    private BoundAssignmentExpr bindUnaryAssignmentExpr(UnaryAssignmentExpr expr) throws CompilerException {
        var boundTarget = bindExpr(expr.operand());
        checkAssignmentTarget(boundTarget);

        var assignmentOperatorKind = switch (expr.operatorToken().kind()) {
            case PlusPlus ->
                AssignmentOperatorKind.AdditionAssignment;
            case MinusMinus ->
                AssignmentOperatorKind.SubtractionAssignment;
            default ->
                throw SemanticErrors.unsupportedAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundTarget.type());
        };

        var boundConstant = BoundConstant.one(boundTarget.type());
        if (assignmentOperatorKind == AssignmentOperatorKind.SubtractionAssignment) {
            boundConstant = boundConstant.negate();
        }

        var boundAssignmentOperatorResult = BoundOperators.bindAssignmentOperator(assignmentOperatorKind, boundTarget.type(), boundConstant.type());

        return switch (boundAssignmentOperatorResult.kind()) {
            case SINGLE_MATCH -> {
                var boundOperator = boundAssignmentOperatorResult.getSingle();
                yield new BoundAssignmentExpr(boundTarget, boundOperator, new BoundLiteralExpr(boundConstant));
            }
            case AMBIGUOUS -> {
                throw SemanticErrors.ambiguousAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundTarget.type(), boundAssignmentOperatorResult.getCandidates());
            }
            case NO_MATCH -> {
                throw SemanticErrors.unsupportedAssignmentOperator(expr.operatorToken(), boundTarget.type(), boundTarget.type());
            }
        };
    }

    private BoundLiteralExpr bindLiteralExpr(LiteralExpr lit) {
        var token = lit.token();

        return BoundLiteralExpr.bind(token.kind(), token.text());
    }

    protected BoundExpression bindIdentifierExpr(IdentifierExpr expr) throws CompilerException {
        IdentifierSyntax identifierSyntax = expr.identifier();
        if (identifierSyntax instanceof QualifiedIdentifierSyntax qualified) {
            var ownerSymbol = resolveSymbol(qualified.left());
            if (!(ownerSymbol instanceof NamespaceOrTypeSymbol nsOrType)) {
                throw SemanticErrors.symbolUsedLikeATypeOrNamespace(qualified.left(), ownerSymbol);
            }

            return bindStaticMemberAccess(expr.identifier(), nsOrType, qualified.right().name());
        }

        var symbol = resolveSymbol(expr.identifier());
        if (symbol == null) {
            throw SemanticErrors.undeclaredIdentifier(expr.identifier(), expr.identifier().text());
        }

        var isCallable = symbol instanceof CallableSymbol;
        if (!isCallable) {
            return BoundReferenceExpr.of(symbol);
        }

        var symbols = resolveSymbols(expr.identifier());
        if (symbols.isEmpty()) {
            throw SemanticErrors.undeclaredIdentifier(expr.identifier(), expr.identifier().text());
        }

        if (symbols.size() == 1) {
            return BoundReferenceExpr.of(symbols.get(0));
        }

        var methods = symbols.stream()
                .filter(s -> s instanceof MethodSymbol)
                .map(s -> (MethodSymbol) s)
                .collect(Collectors.toList());

        if (methods.size() != symbols.size()) {
            throw SemanticErrors.ambiguousIdentifier(expr.identifier(), expr.identifier().text(), symbols);
        }

        return BoundReferenceExpr.of(methods);
    }

    private BoundExpression bindStaticMemberAccess(IHaveSpan span, NamespaceOrTypeSymbol ownerSymbol, String memberName) throws CompilerException {
        List<Symbol> members;
        if (ownerSymbol instanceof TypeSymbol typeSymbol) {
            members = typeSymbol.getMembersWithExtensions(memberName);
        } else {
            members = ownerSymbol.getMembers(memberName);
        }

        if (members.isEmpty()) {
            throw SemanticErrors.memberNotFound(span, memberName, ownerSymbol);
        }

        var staticMembers = members.stream()
                .filter(Symbol::canBeAccessedThroughStatic)
                .toList();

        if (staticMembers.isEmpty()) {
            throw SemanticErrors.instanceMemberAccessOnStaticContext(span, members.get(0));
        }

        if (staticMembers.size() == 1) {
            return BoundReferenceExpr.of(staticMembers.get(0));
        }

        var methods = staticMembers.stream()
                .filter(m -> m instanceof MethodSymbol)
                .map(m -> (MethodSymbol) m)
                .toList();

        if (methods.size() != staticMembers.size()) {
            throw SemanticErrors.ambiguousMemberAccess(span, memberName, ownerSymbol, staticMembers);
        }

        return BoundReferenceExpr.of(methods);
    }

    private BoundExpression bindInstanceMemberAccess(IHaveSpan span, BoundExpression receiver, NamespaceOrTypeSymbol ownerSymbol, String memberName) throws CompilerException {
        List<Symbol> members;
        if (ownerSymbol instanceof TypeSymbol typeSymbol) {
            members = typeSymbol.getMembersWithExtensions(memberName);
        } else {
            members = ownerSymbol.getMembers(memberName);
        }

        if (members.isEmpty()) {
            throw SemanticErrors.memberNotFound(span, memberName, ownerSymbol);
        }

        if (members.size() == 1) {
            var member = members.get(0);
            if (!member.canBeAccessedThroughInstance()) {
                throw SemanticErrors.staticMemberAccessOnInstanceContext(span, member);
            }

            return BoundReferenceExpr.of(receiver, member);
        }

        var methods = members.stream()
                .filter(m -> m instanceof MethodSymbol)
                .map(m -> (MethodSymbol) m)
                .toList();

        if (methods.size() != members.size()) {
            throw SemanticErrors.ambiguousMemberAccess(span, memberName, ownerSymbol, members);
        }

        var instanceCallables = methods.stream()
                .filter(Symbol::canBeAccessedThroughInstance)
                .toList();

        if (instanceCallables.isEmpty()) {
            throw SemanticErrors.staticMemberAccessOnInstanceContext(span, methods.get(0));
        }

        return BoundReferenceExpr.of(receiver, instanceCallables);
    }

    private BoundUnaryExpr bindUnaryExpr(UnaryExpr expr) {
        var boundOperand = bindValueExpr(expr.operand());
        var boundOperandType = boundOperand.type();

        var unaryOperatorKind = switch (expr.operatorToken().kind()) {
            case Plus ->
                UnaryOperatorKind.Identity;
            case Minus ->
                UnaryOperatorKind.Negation;
            case Bang ->
                UnaryOperatorKind.LogicalNot;
            default ->
                throw SemanticErrors.unsupportedUnaryOperator(expr.operatorToken(), boundOperandType);
        };

        var boundOperatorResult = BoundOperators.bindUnaryOperator(unaryOperatorKind, boundOperandType);
        return switch (boundOperatorResult.kind()) {
            case SINGLE_MATCH -> {
                var boundOperator = boundOperatorResult.getSingle();
                yield new BoundUnaryExpr(boundOperator, applyConversion(expr.operand(), boundOperand, boundOperator.operandType()));
            }
            case AMBIGUOUS -> {
                throw SemanticErrors.ambiguousUnaryOperator(expr.operatorToken(), boundOperandType, boundOperatorResult.getCandidates());
            }
            case NO_MATCH -> {
                throw SemanticErrors.unsupportedUnaryOperator(expr.operatorToken(), boundOperandType);
            }
        };
    }

    private BoundBinaryExpr bindBinaryExpr(BinaryExpr expr) throws CompilerException {
        var boundLeft = bindValueExpr(expr.left());
        var boundRight = bindValueExpr(expr.right());
        var leftType = boundLeft.type();
        var rightType = boundRight.type();

        var binaryOperatorKind = switch (expr.operatorToken().kind()) {
            case Plus ->
                leftType.isString() || rightType.isString()
                ? BinaryOperatorKind.StringConcatenation
                : BinaryOperatorKind.Addition;
            case Minus ->
                BinaryOperatorKind.Subtraction;
            case Star ->
                BinaryOperatorKind.Multiplication;
            case Slash ->
                BinaryOperatorKind.Division;
            case Percent ->
                BinaryOperatorKind.Modulo;
            case DoubleEquals ->
                BinaryOperatorKind.Equal;
            case BangEqual ->
                BinaryOperatorKind.NotEqual;
            case LessThan ->
                BinaryOperatorKind.LessThan;
            case LessThanEq ->
                BinaryOperatorKind.LessThanOrEqual;
            case GreaterThan ->
                BinaryOperatorKind.GreaterThan;
            case GreaterThanEq ->
                BinaryOperatorKind.GreaterThanOrEqual;
            case DoubleAmpersand ->
                BinaryOperatorKind.LogicalAnd;
            case DoublePipe ->
                BinaryOperatorKind.LogicalOr;
            default ->
                throw SemanticErrors.unsupportedBinaryOperator(expr.operatorToken(), leftType, rightType);
        };

        var boundOperatorResult = BoundOperators.bindBinaryOperator(binaryOperatorKind, leftType, rightType);
        return switch (boundOperatorResult.kind()) {
            case SINGLE_MATCH -> {
                var boundOperator = boundOperatorResult.getSingle();
                boundLeft = applyConversion(expr.left(), boundLeft, boundOperator.leftType());
                boundRight = applyConversion(expr.right(), boundRight, boundOperator.rightType());
                yield new BoundBinaryExpr(boundOperator, boundLeft, boundRight);
            }
            case AMBIGUOUS -> {
                throw SemanticErrors.ambiguousBinaryOperator(expr.operatorToken(), leftType, rightType, boundOperatorResult.getCandidates());
            }
            case NO_MATCH -> {
                throw SemanticErrors.unsupportedBinaryOperator(expr.operatorToken(), leftType, rightType);
            }
            default ->
                throw new IllegalStateException("Unexpected result kind: " + boundOperatorResult.kind());
        };
    }

    private BoundExpression bindInvocationExpr(InvocationExpr expr, @Nullable LangType expectedReturnType) throws CompilerException {
        if (!(bindExpr(expr.callee()) instanceof BoundReferenceExpr boundReference)) {
            throw SemanticErrors.expressionNotCallable(expr.callee());
        }

        var boundArgs = new ArrayList<BoundExpression>();

        if (boundReference instanceof BoundReferenceExpr.MemberAccessRef memberAccessRef) {
            var memberSymbol = memberAccessRef.getFirstReferencedSymbol();
            if (memberSymbol.isInstance()) {
                // foo.bar() -> Foo::bar(foo)
                // note: symbols treated as instance-only must still be accessed through member access expressions
                boundArgs.add(memberAccessRef.getReceiver());
            }
        }

        for (var arg : expr.arguments()) {
            boundArgs.add(bindValueExpr(arg));
        }

        var argumentTypes = boundArgs.stream()
                .map(BoundExpression::type)
                .collect(Collectors.toList());

        boundReference = bindCallee(boundReference, argumentTypes, expectedReturnType);
        var calledSymbol = (CallableSymbol) boundReference.getReferencedSymbol();

        var parameters = calledSymbol.parameters();
        var isVariadic = !parameters.isEmpty() && parameters.getLast().isVarargs();

        var expectedParameterCount = isVariadic ? parameters.size() - 1 : parameters.size();
        if (!isVariadic && expectedParameterCount != boundArgs.size()) {
            throw SemanticErrors.argumentCountMismatch(expr, calledSymbol.displayName(), expectedParameterCount, boundArgs.size());
        } else if (isVariadic && boundArgs.size() < expectedParameterCount) {
            throw SemanticErrors.argumentCountMismatch(expr, calledSymbol.displayName(), expectedParameterCount, boundArgs.size());
        }

        for (int i = 0; i < expectedParameterCount; i++) {
            var argument = boundArgs.get(i);
            var expectedType = parameters.get(i).type();

            if (!argument.type().isAssignableTo(expectedType)) {
                throw SemanticErrors.argumentTypeMismatch(expr.arguments().get(i), i, expectedType, argument.type());
            }

            boundArgs.set(i, applyConversion(argument.syntax(), argument, expectedType));
        }

        if (isVariadic) {
            var variadicParameter = parameters.getLast();
            var elementType = ((ArrayType) variadicParameter.type()).getElementType();
            var variadicArgs = new ArrayList<BoundExpression>();

            for (int i = expectedParameterCount; i < boundArgs.size(); i++) {
                var argument = boundArgs.get(i);
                if (!argument.type().isAssignableTo(elementType)) {
                    throw SemanticErrors.argumentTypeMismatch(expr.arguments().get(i), i, elementType, argument.type());
                }
                variadicArgs.add(applyConversion(argument.syntax(), argument, elementType));
            }

            // Pack variadic args into a fresh array and replace the tail of boundArgs with it.
            var countLit = new BoundLiteralExpr(new BoundIntConstant(variadicArgs.size()));
            var arrayType = LangType.arrayOf(elementType);
            var tmp = new SynthesizedVariableSymbol("varargs$", arrayType);
            var tmpRef = BoundReferenceExpr.of(tmp);

            var sideEffects = new ArrayList<BoundStatement>();
            sideEffects.add(new BoundVariableDeclStmt(tmp, new BoundArrayLiteralExpr(elementType, countLit)));
            for (int i = 0; i < variadicArgs.size(); i++) {
                var idxLit = new BoundLiteralExpr(new BoundIntConstant(i));
                var store = BoundAssignmentExpr.simpleAssignment(new BoundArrayAccessExpr(tmpRef, idxLit), variadicArgs.get(i));
                sideEffects.add(new BoundExpressionStmt(store));
            }

            boundArgs.subList(expectedParameterCount, boundArgs.size()).clear();
            boundArgs.add(new BoundSequenceExpr(sideEffects, tmpRef));
        }

        return switch (calledSymbol) {
            case MethodSymbol method ->
                new BoundMethodCallExpr(boundReference, boundArgs);
            case ConstructorSymbol ctor ->
                new BoundConstructObjectExpr(ctor, boundArgs);
            default ->
                throw new IllegalStateException("Unsupported callable symbol: " + calledSymbol.getClass().getSimpleName());
        };
    }

    private BoundReferenceExpr bindCallee(BoundReferenceExpr callee, List<LangType> argumentTypes, @Nullable LangType expectedReturnType) throws CompilerException {
        // Cases to handle:
        //  1. Instance-only symbols -> foo.myInstanceMethod() => foo.myInstanceMethod()
        //  2. Instance-static symbols -> foo.myInstanceMethod() => Foo::myInstanceMethod(foo)
        //      In this case the callee is a member access expression, but we treat it as a static call and pass the receiver as the first argument.
        //  3. Static symbols -> Foo::myStaticMethod() => Foo::myStaticMethod()

        // Instance callable symbol parameters are ordered as (instance, arg1, arg2, ...)
        // Static callable symbol parameters are ordered as (arg1, arg2, ...)
        return switch (callee) {
            case BoundReferenceExpr.MethodRef methodRef ->
                callee;
            case BoundReferenceExpr.ConstructorRef ctorRef ->
                callee;
            case BoundReferenceExpr.MemberAccessRef memberAccessRef -> {
                var resolvedCallee = bindCallee(memberAccessRef.getMemberExpr(), argumentTypes, expectedReturnType);
                yield BoundReferenceExpr.of(memberAccessRef.getReceiver(), resolvedCallee);
            }
            case BoundReferenceExpr.TypeRef typeRef -> {
                var typeSymbol = typeRef.getReferencedSymbol();
                var constructors = typeSymbol.getConstructors();
                if (constructors.isEmpty()) {
                    throw SemanticErrors.typeNotConstructible(callee, typeSymbol);
                }

                var overload = resolveCallableGroupOverload(callee, constructors, argumentTypes, expectedReturnType);
                yield BoundReferenceExpr.of(overload);
            }
            case BoundReferenceExpr.MethodGroupRef methodGroupRef -> {
                var methods = methodGroupRef.getReferencedSymbols().stream()
                        .filter(s -> s instanceof MethodSymbol)
                        .map(s -> (MethodSymbol) s)
                        .toList();

                if (methods.isEmpty()) {
                    throw SemanticErrors.expressionNotCallable(callee);
                }

                var overload = resolveCallableGroupOverload(callee, methods, argumentTypes, expectedReturnType);
                yield BoundReferenceExpr.of(overload);
            }
            case BoundReferenceExpr.MissingRef missingRef ->
                // Silently propagate the missing reference, as it will have already been reported as an error during binding.
                missingRef;
            case BoundReferenceExpr.SelfRef selfRef ->
                throw SemanticErrors.valueNotCallable(callee, selfRef.getReferencedSymbol());
            case BoundReferenceExpr.VariableRef variableRef ->
                throw SemanticErrors.valueNotCallable(callee, variableRef.getReferencedSymbol());
            case BoundReferenceExpr.FieldRef fieldRef ->
                throw SemanticErrors.valueNotCallable(callee, fieldRef.getReferencedSymbol());
            case BoundReferenceExpr.ParameterRef parameterRef ->
                throw SemanticErrors.valueNotCallable(callee, parameterRef.getReferencedSymbol());
        };
    }

    private CallableSymbol resolveCallableGroupOverload(IHaveSpan span, List<? extends CallableSymbol> candidates, List<LangType> argumentTypes, @Nullable LangType expectedReturnType) throws CompilerException {
        String name = candidates.stream()
                .findFirst()
                .map(CallableSymbol::name)
                .orElse("<unknown>");

        var lookupResult = OverloadResolver.resolveMatchingOverload(candidates, argumentTypes, expectedReturnType);
        return switch (lookupResult.kind()) {
            case NO_MATCH ->
                throw resolveNoMatchError(span, name, candidates, argumentTypes);
            case AMBIGUOUS ->
                throw SemanticErrors.ambiguousOverload(span, name, lookupResult.getCandidates());

            // If it's a single match, we can proceed with the call.
            case SINGLE_MATCH -> {
                var resolved = lookupResult.getSingle();
                if (!(resolved instanceof CallableSymbol)) {
                    throw new IllegalStateException("Overload resolution returned a symbol that is not a callable: " + resolved.getClass().getSimpleName());
                }

                yield (CallableSymbol) resolved;
            }
        };
    }

    private CompilerException resolveNoMatchError(IHaveSpan span, String name, List<? extends CallableSymbol> candidates, List<LangType> argumentTypes) {
        var sameArityCandidates = candidates.stream()
                .filter(c -> c.parameters().size() == argumentTypes.size())
                .toList();
        if (sameArityCandidates.size() == 1) {
            var candidate = sameArityCandidates.get(0);
            var params = candidate.parameters();
            for (int i = 0; i < params.size(); i++) {
                var expected = params.get(i).type();
                var actual = argumentTypes.get(i);
                if (!actual.isAssignableTo(expected)) {
                    return SemanticErrors.argumentTypeMismatch(span, i, expected, actual);
                }
            }
        }
        return SemanticErrors.noMatchingOverload(span, name, argumentTypes, candidates);
    }

    private BoundExpression bindMemberAccessExpr(MemberAccessExpr expr) throws CompilerException {
        var receiver = bindValueExpr(expr.instanceExpr());

        var compilation = getCompilation();
        var typeRegistry = compilation.getTypeRegistry();
        var ownerSymbol = typeRegistry.bindType(receiver.type());
        if (ownerSymbol == null) {
            throw SemanticErrors.memberNotFound(expr.memberIdentifier(), expr.memberName(), receiver.type());
        }

        if (expr.memberIdentifier().hasError()) {
            return new BoundReferenceExpr.MemberAccessRef(receiver, BoundReferenceExpr.of(Symbol.missing(expr.memberName())));
        }

        return bindInstanceMemberAccess(expr.memberIdentifier(), receiver, ownerSymbol, expr.memberName());
    }

    private BoundArrayLiteralExpr bindArrayLiteralExpr(ArrayLiteralExpression expr) throws CompilerException {
        var elementType = resolveType(expr.elementType());
        var boundLengthExpr = bindExpectedExpr(expr.length(), LangType.Int);

        return new BoundArrayLiteralExpr(elementType, boundLengthExpr);
    }

    private BoundArrayAccessExpr bindArrayAccessExpr(ArrayAccessExpr expr) throws CompilerException {
        var boundArrayExpr = bindValueExpr(expr.arrayExpr());
        var boundIndexExpr = bindExpectedExpr(expr.indexExpr(), LangType.Int);
        var baseType = boundArrayExpr.type();
        var indexType = boundIndexExpr.type();

        if (baseType == null || (!baseType.isString() && !baseType.isArray())) {
            throw SemanticErrors.unsupportedIndexOperator(expr, baseType, indexType);
        }

        return new BoundArrayAccessExpr(boundArrayExpr, boundIndexExpr);
    }

    private void checkAssignmentTarget(BoundExpression target) throws CompilerException {
        if (target instanceof BoundReferenceExpr refExpr) {
            var symbol = refExpr.getReferencedSymbol();
            if (symbol instanceof ValueSymbol valueSymbol && valueSymbol.isImmutable()) {
                throw SemanticErrors.immutableAssignment(target, valueSymbol);
            }

            return;
        }

        if (target instanceof BoundReferenceExpr.MemberAccessRef memberAccessRef) {
            checkAssignmentTarget(memberAccessRef.getMemberExpr());
            return; // The member itself can be assigned to, we just need to check if the member access expression is valid.
        }

        if (target instanceof BoundArrayAccessExpr boundArrayAccess) {
            var baseType = boundArrayAccess.arrayExpr().type();

            // Assignment to STRING[i] is not allowed (strings are immutable).
            if (baseType.isString()) {
                throw SemanticErrors.stringIndexAssignment(target);
            }

            return;
        }

        Debug.warning("Expression is not a valid assignment target: " + target + " of type " + target.getClass().getSimpleName());
        throw SemanticErrors.invalidAssignmentTarget(target);
    }

    protected final BoundExpression bindConditionExpr(SyntaxNode constructSyntax, ExpressionSyntax condition) throws CompilerException {
        var boundCondition = bindValueExpr(condition);
        if (!boundCondition.type().isBool()) {
            throw SemanticErrors.missingCondition(condition, constructSyntax);
        }

        return boundCondition;
    }

    private BoundExpression applyConversion(IHaveSpan span, BoundExpression expr, LangType targetType) throws CompilerException {
        LangType sourceType = expr.type();

        return switch (Conversions.classify(sourceType, targetType)) {
            case IDENTITY ->
                expr;
            case IMPLICIT ->
                new BoundConversionExpr(expr, targetType);
            case EXPLICIT -> {
                if (!expr.isCompileTimeFoldable() || !sourceType.isNumeric() || !targetType.isNumeric()) {
                    throw SemanticErrors.implicitConversionUnsupported(span, sourceType, targetType);
                }

                // FIXME: This allows implicit conversions with final variables but we shouldn't allow this.
                //        For example:
                //        byte b = 42; -> should be allowed
                //        byte c = myFinalIntVariable; -> should not be allowed because this allows implicit numeric narrowing conversion (can lead to unexpected bugs).
                var constant = expr.getConstant();
                var convertedConstant = constant.convertTo(targetType);
                yield new BoundLiteralExpr(convertedConstant);
            }
            case NONE ->
                throw SemanticErrors.typeMismatch(span, targetType, sourceType);
        };
    }
}
