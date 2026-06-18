package com.orca.lsp;

public final class Logger {

    public static void debug(String message) {
        System.out.println("[DEBUG] " + message);
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void warn(String message) {
        System.err.println("[WARN] " + message);
    }

    public static void error(String message) {
        System.err.println("[ERROR] " + message);
    }
}
