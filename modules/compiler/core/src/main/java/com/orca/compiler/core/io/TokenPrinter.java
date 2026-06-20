package com.orca.compiler.core.io;

import java.util.List;

import com.orca.compiler.core.lexer.Token;

public final class TokenPrinter {

    public static void printTokens(List<Token> tokens) {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
