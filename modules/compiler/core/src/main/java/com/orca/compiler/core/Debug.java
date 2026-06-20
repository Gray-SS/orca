package com.orca.compiler.core;

import com.orca.compiler.core.io.AnsiColor;
import com.orca.compiler.core.io.AnsiConsole;

public final class Debug {

    public static void log(String message) {
        System.out.println("DEBUG: " + message);
    }

    public static void warning(String message) {
        AnsiConsole.pushColor(AnsiColor.YELLOW, AnsiColor.BOLD);
        System.out.print("WARNING: ");
        AnsiConsole.popColor(2);

        System.out.println(message);
    }

    public static void requireNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void requireNotNullOrEmpty(String str, String message) {
        if (str == null || str.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new IllegalArgumentException(message + " (expected: " + expected + ", actual: " + actual + ")");
        }
    }

    public static void shouldNotReachHere(String message) {
        throw new IllegalStateException(message);
    }
}
