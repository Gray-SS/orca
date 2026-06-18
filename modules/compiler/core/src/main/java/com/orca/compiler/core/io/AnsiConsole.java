package com.orca.compiler.core.io;

import java.io.PrintStream;
import java.util.Stack;

public class AnsiConsole {
    private static final Stack<AnsiColor> colorCodes = new java.util.Stack<>();

    public static void pushColor(AnsiColor... colors) {
        pushColor(System.out, colors);
    }

    public static void pushColor(PrintStream stream, AnsiColor... colors) {
        for (AnsiColor color : colors) {
            colorCodes.push(color);
            stream.print(color.code());
        }
    }

    public static void popColor() {
        popColor(System.out);
    }

    public static void popColor(int count) {
        popColor(System.out, count);
    }

    public static void popColor(PrintStream stream, int count) {
        for (int i = 0; i < count; i++) {
            popColor(stream);
        }
    }

    public static void popColor(PrintStream stream) {
        if (colorCodes.isEmpty()) {
            throw new IllegalStateException("No color to pop");
        }

        AnsiColor poppedColor = colorCodes.pop();
        if (colorCodes.isEmpty()) {
            stream.print(AnsiColor.RESET.code());
        } else {
            if (poppedColor == AnsiColor.BOLD) {
                // If we popped a bold color, we need to reapply the previous colors to maintain the correct state.
                stream.print(AnsiColor.RESET.code());
                for (AnsiColor color : colorCodes) {
                    stream.print(color.code());
                }
            }

            stream.print(colorCodes.peek().code());
        }
    }

    public static void println(PrintStream stream, String text) {
        stream.println(text);
    }

    public static void print(PrintStream stream, String text) {
        stream.print(text);
    }
}
