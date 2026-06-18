package com.orca.compiler.core.io;

import java.util.List;

import com.orca.compiler.core.syntax.SyntaxNode;
import com.orca.compiler.core.syntax.SyntaxTree;

public final class SyntaxTreePrinter {
    public static void print(SyntaxTree syntaxTree, int indentSize) {
        printNode(syntaxTree.root(), 0, indentSize);
    }

    private static void printNode(SyntaxNode node, int indentLevel, int indentSize) {
        // Print the current node with indentation
        List<SyntaxNode> children = node.children();
        System.out.println(" ".repeat(indentLevel * indentSize) + node.toString() + (children.isEmpty() ? "" : ":"));

        // Recursively print child nodes
        for (SyntaxNode child : children) {
            printNode(child, indentLevel + 1, indentSize);
        }
    }
}
