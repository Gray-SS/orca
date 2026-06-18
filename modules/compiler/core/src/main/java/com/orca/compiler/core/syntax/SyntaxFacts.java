package com.orca.compiler.core.syntax;

import java.util.Map;

import com.orca.compiler.core.lexer.SpecialTypeKind;
import com.orca.compiler.core.lexer.TokenKind;

public class SyntaxFacts {

    private final static Map<String, TokenKind> keywords = Map.ofEntries(
            Map.entry("def", TokenKind.DefKeyword),
            Map.entry("if", TokenKind.IfKeyword),
            Map.entry("else", TokenKind.ElseKeyword),
            Map.entry("while", TokenKind.WhileKeyword),
            Map.entry("for", TokenKind.ForKeyword),
            Map.entry("return", TokenKind.ReturnKeyword),
            Map.entry("is", TokenKind.IsKeyword),
            Map.entry("true", TokenKind.BoolLiteral),
            Map.entry("false", TokenKind.BoolLiteral),
            Map.entry("var", TokenKind.VarKeyword),
            Map.entry("let", TokenKind.LetKeyword),
            Map.entry("const", TokenKind.ConstKeyword),
            Map.entry("coll", TokenKind.CollKeyword),
            Map.entry("impl", TokenKind.ImplKeyword),
            Map.entry("package", TokenKind.PackageKeyword),
            Map.entry("import", TokenKind.ImportKeyword),
            Map.entry("ARRAY", TokenKind.ArrayKeyword)
    );

    public static Map<String, TokenKind> getWellKnownKeywords() {
        return keywords;
    }

    public static boolean isWellKnownKeyword(String text) {
        return keywords.containsKey(text);
    }

    public static boolean isSpecialTypeKeyword(String text) {
        return getSpecialTypeKind(text) != null;
    }

    public static SpecialTypeKind getSpecialTypeKind(String text) {
        return switch (text) {
            case "byte" ->
                SpecialTypeKind.Byte;
            case "short" ->
                SpecialTypeKind.Short;
            case "int" ->
                SpecialTypeKind.Int;
            case "long" ->
                SpecialTypeKind.Long;
            case "float" ->
                SpecialTypeKind.Float;
            case "double" ->
                SpecialTypeKind.Double;
            case "bool" ->
                SpecialTypeKind.Bool;
            case "string" ->
                SpecialTypeKind.String;
            case "char" ->
                SpecialTypeKind.Char;
            case "void" ->
                SpecialTypeKind.Void;
            case "any" ->
                SpecialTypeKind.Any;
            default ->
                null;
        };
    }

    public static boolean isAssignmentOperator(TokenKind kind) {
        return switch (kind) {
            case Equals, PlusEquals, MinusEquals, StarEquals, SlashEquals, PercentEquals ->
                true;
            default ->
                false;
        };
    }

    public static boolean isUnaryAssignmentOperator(TokenKind kind) {
        return switch (kind) {
            case PlusPlus, MinusMinus ->
                true;
            default ->
                false;
        };
    }

    public static boolean isBinaryOperator(TokenKind kind) {
        return switch (kind) {
            case Plus, Minus, Star, Slash, Percent, DoubleEquals, EqualsSlashEquals, LessThan, LessThanEq, GreaterThan, GreaterThanEq, DoubleAmpersand, DoublePipe ->
                true;
            default ->
                false;
        };
    }

    public static int getBinaryOperatorPrecedence(TokenKind operatorKind) {
        return switch (operatorKind) {
            case DoublePipe ->
                1;
            case DoubleAmpersand ->
                2;
            case DoubleEquals, EqualsSlashEquals, LessThan, LessThanEq, GreaterThan, GreaterThanEq ->
                3;
            case Plus, Minus ->
                4;
            case Star, Slash, Percent ->
                5;
            default ->
                0;
        };
    }

    public static TokenKind getKeywordKind(String text) {
        return keywords.get(text);
    }
}
