package com.orca.compiler.core.semantics;

import java.util.List;

import com.orca.compiler.core.CompilerException;
import com.orca.compiler.core.boundtree.expressions.BoundOperator;
import com.orca.compiler.core.diagnostics.Diagnostic;
import com.orca.compiler.core.diagnostics.DiagnosticFactory;
import com.orca.compiler.core.symbols.CallableSymbol;
import com.orca.compiler.core.symbols.MethodSymbol;
import com.orca.compiler.core.symbols.NamespaceOrTypeSymbol;
import com.orca.compiler.core.symbols.Symbol;
import com.orca.compiler.core.symbols.TypeSymbol;
import com.orca.compiler.core.symbols.ValueSymbol;
import com.orca.compiler.core.symbols.sources.SourceMethodSymbol;
import com.orca.compiler.core.symbols.sources.SourceSymbol;
import com.orca.compiler.core.syntax.StatementSyntax;
import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxToken;
import com.orca.compiler.core.syntax.nodes.ParameterSyntax;
import com.orca.compiler.core.text.IHaveSpan;
import com.orca.compiler.core.text.TextSource;
import com.orca.compiler.core.typesystem.LangType;

public final class SemanticErrors {

    public static CompilerException unsupportedFeature(IHaveSpan span, String message) {
        return wrap(DiagnosticFactory.unsupportedFeature(span, message));
    }

    public static CompilerException undeclaredType(IHaveSpan span, String typeName) {
        return wrap(DiagnosticFactory.undeclaredType(span, typeName));
    }

    public static CompilerException instanceMemberAccessOnNonNamedType(IHaveSpan span, String memberName) {
        return wrap(DiagnosticFactory.instanceMemberAccessOnNonNamedType(span, memberName));
    }

    public static CompilerException instanceMemberAccessOnStaticContext(IHaveSpan span, Symbol accessedSymbol) {
        return wrap(DiagnosticFactory.instanceMemberAccessOnStaticContext(span, accessedSymbol));
    }

    public static CompilerException staticMemberAccessOnInstanceContext(IHaveSpan span, Symbol accessedSymbol) {
        return wrap(DiagnosticFactory.staticMemberAccessOnInstanceContext(span, accessedSymbol));
    }

    public static CompilerException noMatchingOverload(IHaveSpan span, String functionName, List<LangType> argumentTypes, List<? extends CallableSymbol> candidates) {
        return wrap(DiagnosticFactory.noMatchingOverload(span, functionName, argumentTypes, candidates));
    }

    public static CompilerException ambiguousMemberAccess(IHaveSpan span, String memberName, NamespaceOrTypeSymbol type, List<? extends Symbol> ambiguousSymbols) {
        return wrap(DiagnosticFactory.ambiguousMemberAccess(span, memberName, type, ambiguousSymbols));
    }

    public static CompilerException ambiguousIdentifier(IHaveSpan span, String identifierName, List<? extends Symbol> ambiguousSymbols) {
        return wrap(DiagnosticFactory.ambiguousIdentifier(span, identifierName, ambiguousSymbols));
    }

    public static CompilerException ambiguousOverload(IHaveSpan span, String functionName, List<? extends Symbol> ambiguousSymbols) {
        return wrap(DiagnosticFactory.ambiguousOverload(span, functionName, ambiguousSymbols));
    }

    public static CompilerException ambiguousMainFunction(List<SourceMethodSymbol> mainCandidates) {
        return wrap(DiagnosticFactory.ambiguousMainFunction(mainCandidates));
    }

    public static CompilerException missingMainFunction() {
        return wrap(DiagnosticFactory.missingMainFunction());
    }

    public static CompilerException valueNotCallable(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.valueNotCallable(span, symbol));
    }

    public static CompilerException typeNotConstructible(IHaveSpan span, TypeSymbol type) {
        return wrap(DiagnosticFactory.typeNotConstructible(span, type));
    }

    public static CompilerException typeMismatch(IHaveSpan span, LangType expected, LangType actual) {
        return wrap(DiagnosticFactory.typeMismatch(span, expected, actual));
    }

    public static CompilerException typeMismatch(IHaveSpan span, List<LangType> expectedTypes, LangType actual) {
        return wrap(DiagnosticFactory.typeMismatch(span, expectedTypes, actual));
    }

    public static CompilerException argumentTypeMismatch(IHaveSpan span, int argumentPosition, LangType expected, LangType actual) {
        return wrap(DiagnosticFactory.argumentTypeMismatch(span, argumentPosition, expected, actual));
    }

    public static CompilerException argumentTypeMismatch(IHaveSpan span, int argumentPosition, java.util.List<LangType> expectedTypes, LangType actual) {
        return wrap(DiagnosticFactory.argumentTypeMismatch(span, argumentPosition, expectedTypes, actual));
    }

    public static CompilerException memberNotFound(IHaveSpan span, String memberName, NamespaceOrTypeSymbol type) {
        return wrap(DiagnosticFactory.memberNotFound(span, memberName, type));
    }

    public static CompilerException memberNotFound(IHaveSpan span, String memberName, LangType type) {
        return wrap(DiagnosticFactory.memberNotFound(span, memberName, type));
    }

    public static CompilerException argumentCountMismatch(IHaveSpan span, String arityName, int expected, int actual) {
        return wrap(DiagnosticFactory.argumentCountMismatch(span, arityName, expected, actual));
    }

    public static CompilerException argumentCountMismatch(IHaveSpan span, String arityName, List<Integer> expectedArities, int actual) {
        return wrap(DiagnosticFactory.argumentCountMismatch(span, arityName, expectedArities, actual));
    }

    public static CompilerException symbolNotAValue(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.symbolNotAValue(span, symbol));
    }

    public static CompilerException undeclaredIdentifier(IHaveSpan span, String symbolName) {
        return wrap(DiagnosticFactory.undeclaredIdentifier(span, symbolName));
    }

    public static CompilerException symbolNotAFunction(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.symbolNotAFunction(span, symbol));
    }

    public static CompilerException symbolUsedLikeATypeOrNamespace(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.symbolUsedLikeATypeOrNamespace(span, symbol));
    }

    public static CompilerException symbolNotAType(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.symbolNotAType(span, symbol));
    }

    public static CompilerException symbolNotAVariable(IHaveSpan span, Symbol symbol) {
        return wrap(DiagnosticFactory.symbolNotAVariable(span, symbol));
    }

    public static CompilerException symbolRedeclared(Symbol existingSymbol, SourceSymbol newSymbol) {
        return wrap(DiagnosticFactory.symbolRedeclared(existingSymbol, newSymbol));
    }

    public static CompilerException symbolRedeclared(Symbol existingSymbol, IHaveSpan newSymbolSpan, String symbolName) {
        return wrap(DiagnosticFactory.symbolRedeclared(existingSymbol, newSymbolSpan, symbolName));
    }

    public static CompilerException undeclaredPackage(IHaveSpan span, String packageName, List<String> suggestions) {
        return wrap(DiagnosticFactory.undeclaredPackage(span, packageName, suggestions));
    }

    public static CompilerException functionRedeclared(IHaveSpan newSpan, IHaveSpan existingSpan, String functionName) {
        return wrap(DiagnosticFactory.functionRedeclared(newSpan, existingSpan, functionName));
    }

    public static CompilerException expressionNotCallable(IHaveSpan span) {
        return wrap(DiagnosticFactory.expressionNotCallable(span));
    }

    public static CompilerException implicitConversionUnsupported(IHaveSpan span, LangType fromType, LangType toType) {
        return wrap(DiagnosticFactory.implicitConversionUnsupported(span, fromType, toType));
    }

    public static CompilerException lowercaseCollectionName(IHaveSpan span, String collectionName) {
        return wrap(DiagnosticFactory.lowercaseCollectionName(span, collectionName));
    }

    public static CompilerException reservedTypeName(IHaveSpan span, String collectionName) {
        return wrap(DiagnosticFactory.reservedTypeName(span, collectionName));
    }

    public static CompilerException recursiveCollectionDeclaration(IHaveSpan span, String collectionName) {
        return wrap(DiagnosticFactory.recursiveCollectionDeclaration(span, collectionName));
    }

    public static CompilerException fieldRedeclared(IHaveSpan span, String fieldName, String collectionName) {
        return wrap(DiagnosticFactory.fieldRedeclared(span, fieldName, collectionName));
    }

    public static CompilerException noTopLevelExecutableStatementsForImplicitMainFunction() {
        return wrap(DiagnosticFactory.noTopLevelExecutableStatementsForImplicitMainFunction());
    }

    public static CompilerException topLevelStatementsWithExplicitMainFunction(IHaveSpan span) {
        return wrap(DiagnosticFactory.topLevelExecutableStatementsWithExplicitMainFunction(span));
    }

    public static CompilerException topLevelStatementsNotAllowed(List<StatementSyntax> topLevelStatements) {
        return wrap(DiagnosticFactory.topLevelStatementsNotAllowed(topLevelStatements));
    }

    public static CompilerException invalidDeclarationOrder(IHaveSpan span, String message) {
        return wrap(DiagnosticFactory.invalidDeclarationOrder(span, message));
    }

    public static CompilerException forbiddenNestedDeclaration(IHaveSpan span, String declType) {
        return wrap(DiagnosticFactory.forbiddenNestedDeclaration(span, declType));
    }

    public static CompilerException missingPackageDirective(TextSource source) {
        return wrap(DiagnosticFactory.missingPackageDirective(source));
    }

    public static CompilerException constVariableCannotBeMutable(IHaveSpan span, String variableName) {
        return wrap(DiagnosticFactory.constVariableCannotBeMutable(span, variableName));
    }

    public static CompilerException immutableVariableMissingInitializer(IHaveSpan span, String variableName, String modifier) {
        return wrap(DiagnosticFactory.immutableVariableMissingInitializer(span, variableName, modifier));
    }

    public static CompilerException immutableAssignment(IHaveSpan span, ValueSymbol symbol) {
        return wrap(DiagnosticFactory.immutableAssignment(span, symbol));
    }

    public static CompilerException cannotInferVariableType(IHaveSpan span, String variableName) {
        return wrap(DiagnosticFactory.cannotInferVariableType(span, variableName));
    }

    public static CompilerException missingConstantBaseType(IHaveSpan span, String constName) {
        return wrap(DiagnosticFactory.missingConstantBaseType(span, constName));
    }

    public static CompilerException constantNonCompileTimeFoldableInitializer(IHaveSpan span, String constName) {
        return wrap(DiagnosticFactory.constantNonCompileTimeFoldableInitializer(span, constName));
    }

    public static CompilerException localConstantDeclaration(IHaveSpan span, String name) {
        return wrap(DiagnosticFactory.localConstantDeclaration(span, name));
    }

    public static CompilerException voidFieldType(IHaveSpan span, String fieldName) {
        return wrap(DiagnosticFactory.voidFieldType(span, fieldName));
    }

    public static CompilerException voidVariableType(IHaveSpan span, String varName) {
        return wrap(DiagnosticFactory.voidVariableType(span, varName));
    }

    public static CompilerException incompleteReturnsPath(IHaveSpan span, CallableSymbol callable) {
        return wrap(DiagnosticFactory.incompleteReturnPaths(span, callable));
    }

    public static CompilerException forLoopStepMustBeAssignment(IHaveSpan span) {
        return wrap(DiagnosticFactory.forLoopStepMustBeAssignment(span));
    }

    public static CompilerException missingCondition(IHaveSpan span, SyntaxNode constructSyntax) {
        return wrap(DiagnosticFactory.missingCondition(span, constructSyntax));
    }

    public static CompilerException implOnNonCollectionType(IHaveSpan span) {
        return wrap(DiagnosticFactory.implOnNonCollectionType(span));
    }

    public static CompilerException multipleReceiverParameters(ParameterSyntax first, ParameterSyntax second, String implName) {
        return wrap(DiagnosticFactory.multipleReceiverParameters(first, second, implName));
    }

    public static CompilerException receiverParameterNotFirst(ParameterSyntax receiverSyntax, String implName) {
        return wrap(DiagnosticFactory.receiverParameterNotFirst(receiverSyntax, implName));
    }

    public static CompilerException receiverParameterInFunction(ParameterSyntax receiverSyntax, String functionName) {
        return wrap(DiagnosticFactory.receiverParameterInFunction(receiverSyntax, functionName));
    }

    public static CompilerException missingReceiverParameter(IHaveSpan span, String methodName, String implName) {
        return wrap(DiagnosticFactory.missingReceiverParameter(span, methodName, implName));
    }

    public static CompilerException methodRedeclared(SourceMethodSymbol incoming, MethodSymbol existing) {
        return wrap(DiagnosticFactory.methodRedeclared(incoming, existing));
    }

    public static CompilerException missingReturnValue(IHaveSpan span, CallableSymbol callable) {
        return wrap(DiagnosticFactory.missingReturnValue(span, callable));
    }

    public static CompilerException returnValueInVoidFunction(IHaveSpan span, CallableSymbol callable) {
        return wrap(DiagnosticFactory.returnValueInVoidFunctionLike(span, callable));
    }

    public static CompilerException stringIndexAssignment(IHaveSpan span) {
        return wrap(DiagnosticFactory.stringIndexAssignment(span));
    }

    public static CompilerException invalidAssignmentTarget(IHaveSpan span) {
        return wrap(DiagnosticFactory.invalidAssignmentTarget(span));
    }

    public static CompilerException unsupportedIndexOperator(IHaveSpan span, LangType baseType, LangType indexType) {
        return wrap(DiagnosticFactory.unsupportedIndexOperator(span, baseType, indexType));
    }

    public static CompilerException unsupportedAssignmentOperator(SyntaxToken operatorToken, LangType targetType, LangType valueType) {
        return wrap(DiagnosticFactory.unsupportedAssignmentOperator(operatorToken, targetType, valueType));
    }

    public static CompilerException ambiguousAssignmentOperator(SyntaxToken operatorToken, LangType targetType, LangType valueType, List<BoundOperator.Assignment> candidates) {
        return wrap(DiagnosticFactory.ambiguousAssignmentOperator(operatorToken, targetType, valueType, candidates));
    }

    public static CompilerException ambiguousUnaryOperator(SyntaxToken operatorToken, LangType operandType, List<BoundOperator.Unary> candidates) {
        return wrap(DiagnosticFactory.ambiguousUnaryOperator(operatorToken, operandType, candidates));
    }

    public static CompilerException unsupportedUnaryOperator(SyntaxToken operatorToken, LangType operandType) {
        return wrap(DiagnosticFactory.unsupportedUnaryOperator(operatorToken, operandType));
    }

    public static CompilerException ambiguousBinaryOperator(SyntaxToken operatorToken, LangType leftOperandType, LangType rightOperandType, List<BoundOperator.Binary> candidates) {
        return wrap(DiagnosticFactory.ambiguousBinaryOperator(operatorToken, leftOperandType, rightOperandType, candidates));
    }

    public static CompilerException unsupportedBinaryOperator(SyntaxToken operatorToken, LangType leftOperandType, LangType rightOperandType) {
        return wrap(DiagnosticFactory.unsupportedBinaryOperator(operatorToken, leftOperandType, rightOperandType));
    }

    public static CompilerException unexpectedError(IHaveSpan span, String message) {
        return wrap(DiagnosticFactory.unexpectedError(span, message));
    }

    private static CompilerException wrap(Diagnostic diagnostic) {
        return CompilerException.wrap(diagnostic);
    }
}
