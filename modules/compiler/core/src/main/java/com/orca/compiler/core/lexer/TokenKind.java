package com.orca.compiler.core.lexer;

/**
 * Represents the different kinds of symbols (tokens) that can be produced by
 * the lexer. This includes keywords, identifiers, literals, operators, and
 * punctuation.
 */
public enum TokenKind {
    // === Special symbols ===
    EOF,
    Identifier,
    BadToken,
    // === Keywords ===
    // FinalKeyword,
    VarKeyword,
    LetKeyword,
    ConstKeyword,
    CollKeyword,
    DefKeyword,
    ForKeyword,
    WhileKeyword,
    IfKeyword,
    ElseKeyword,
    ReturnKeyword,
    ArrayKeyword,
    IsKeyword,
    ImplKeyword,
    PackageKeyword,
    ImportKeyword,
    // === Literals ===
    IntegerLiteral,
    FloatLiteral,
    StringLiteral,
    BoolLiteral,
    // === Operators and punctuation ===
    Equals,
    Plus,
    PlusPlus,
    PlusEquals,
    ColonEquals,
    Minus,
    MinusMinus,
    MinusEquals,
    Bang,
    Star,
    StarEquals,
    Slash,
    SlashEquals,
    Percent,
    PercentEquals,
    BangEqual,
    DoubleEquals,
    GreaterThan,
    LessThan,
    GreaterThanEq,
    LessThanEq,
    LParen,
    RParen,
    LBrace,
    RBrace,
    LBracket,
    RBracket,
    Dot,
    DoubleAmpersand,
    DoublePipe,
    Semicolon,
    Colon,
    DoubleColon,
    Comma,
    Arrow
}
