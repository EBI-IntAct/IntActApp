package uk.ac.ebi.intact.intactApp.internal.model.styles.utils;

import java.util.*;
import java.util.stream.Collectors;

public enum Taxon {
    E_COLI(562L, "Escherichia coli", true),
    S_CEREVISIAE(4932L, "Saccharomyces cerevisiae", true),
    H_SAPIENS(9606L, "Homo sapiens", true),
    M_MUSCULUS(10090L, "Mus musculus",true),
    A_THALIANA(3702L, "Arabidopsis thaliana",true),
    D_MELANOGASTER(7227L, "Drosophila melanogaster",true),
    C_ELEGANS(6239L, "Caenorhabditis elegans",true),
    CHEMICAL_SYNTHESIS(-2L, "Chemical Synthesis",true),
    PLANTS(33090L, "Plants", false),
    ANIMALS(33208L, "Other animals",false),
    MAMMALS(40674L, "Mammals", false),
    FUNGI(4751L, "Fungi", false),
    BACTERIA(2L, "Bacteria",false),
    VIRUSES(10239L, "Viruses",false),
    ARCHAEA(2157L, "Archaea", false);

    public final long taxId;
    public final String descriptor;
    public final boolean isSpecies;
    private static final Map<Long, Taxon> taxIdToTaxons = new HashMap<>();
    static {
        for (Taxon taxon: Taxon.values()) {
            taxIdToTaxons.put(taxon.taxId, taxon);
        }
    }
    private static final List<Taxon> species =  Arrays.stream(Taxon.values()).filter(taxon -> taxon.isSpecies && taxon != CHEMICAL_SYNTHESIS).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());
    private static final List<Taxon> kingdoms =  Arrays.stream(Taxon.values()).filter(taxon -> !taxon.isSpecies).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());

    Taxon(long taxId, String descriptor, boolean isSpecies) {
        this.taxId = taxId;
        this.descriptor = descriptor;
        this.isSpecies = isSpecies;
    }

    public static List<Taxon> getSpecies() {
        return new ArrayList<>(species);
    }

    public static List<Taxon> getKingdoms() {
        return new ArrayList<>(kingdoms);
    }

    public static Taxon getTaxon(long taxId) {
        return taxIdToTaxons.get(taxId);
    }
}
