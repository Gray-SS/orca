package com.orca.lsp;

import java.util.List;

import org.eclipse.lsp4j.SemanticTokensLegend;

public final class OrcaSemanticTokensLegend extends SemanticTokensLegend {

    public static final OrcaSemanticTokensLegend LEGEND;

    static {
        LEGEND = new OrcaSemanticTokensLegend(OrcaSemanticToken.Type.getLspNames(), OrcaSemanticToken.Modifier.getLspNames());
    }

    private OrcaSemanticTokensLegend(List<String> tokenTypes, List<String> tokenModifiers) {
        super(tokenTypes, tokenModifiers);
    }
}
