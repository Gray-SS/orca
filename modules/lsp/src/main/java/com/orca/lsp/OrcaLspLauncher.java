package com.orca.lsp;

import java.io.PrintStream;

import org.eclipse.lsp4j.launch.LSPLauncher;
public class OrcaLspLauncher {

    public static void main(String[] args) throws Exception {
        // Capture the real stdout before redirecting it, so the JSON-RPC stream
        // is never polluted by stray System.out calls inside the compiler.
        PrintStream lspOut = System.out;
        System.setOut(System.err);

        var server = new OrcaLanguageServer();
        var launcher = LSPLauncher.createServerLauncher(server, System.in, lspOut);
        server.connect(launcher.getRemoteProxy());
        launcher.startListening().get();
    }
}
