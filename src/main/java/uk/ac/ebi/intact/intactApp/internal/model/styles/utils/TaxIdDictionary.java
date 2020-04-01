package uk.ac.ebi.intact.intactApp.internal.model.styles.utils;

import java.util.HashMap;
import java.util.Map;

public class TaxIdDictionary {
    private static final Map<Long, String> taxIdToDescriptor = new HashMap<>() {{
        put(562L, "Escherichia coli");
        put(4932L, "Saccharomyces cerevisiae");
        put(9606L, "Homo sapiens");
        put(10090L, "Mus musculus");
        put(3702L, "Arabidopsis thaliana");
        put(7227L, "Drosophila melanogaster");
        put(6239L, "Caenorhabditis elegans");
        put(-2L, "Chemical Synthesis");
        put(33090L, "Plants");
        put(33208L, "Other animals");
        put(40674L, "Mammals");
        put(4751L, "Fungi");
        put(2L, "Bacteria");
        put(10239L, "Viruses");
        put(2157L, "Archaea");
    }};

    public static String getTaxIdDescriptor(Long taxId) {
        return taxIdToDescriptor.get(taxId);
    }
}
