package uk.ac.ebi.intact.intactApp.internal.model.styles.utils;

import java.util.*;
import java.util.stream.Collectors;

public enum Taxons {
    E_COLI(562L, "Escherichia coli", true),
    S_CEREVISIAE(4932L, "Saccharomyces cerevisiae", true),
    H_SAPIENS(9606L, "Homo sapiens", true),
    M_MUSCULUS(10090L, "Mus musculus",true),
    A_THALIANA(3702L, "Arabidopsis thaliana",true),
    D_MELANOGASTER(7227L, "Drosophila melanogaster",true),
    C_ELEGANS(6239L, "Caenorhabditis elegans",true),
    CHEMICAL_SYNTHESIS(-2L, "Chemical Synthesis",true),
    PLANTS(33090L, "Other plants", false),
    ANIMALS(33208L, "Other animals",false),
    MAMMALS(40674L, "Other mammals", false),
    FUNGI(4751L, "Other fungi", false),
    BACTERIA(2L, "Other bacteria",false),
    VIRUSES(10239L, "Other viruses",false),
    ARCHAEA(2157L, "Other archaea", false),
    ARTIFICIAL(81077L, "Other artificial molecules",false);

    public final long taxId;
    public final String descriptor;
    public final boolean isSpecies;
    private static final Map<Long, Taxons> taxIdToTaxons = new HashMap<>();
    static {
        for (Taxons taxon: Taxons.values()) {
            taxIdToTaxons.put(taxon.taxId, taxon);
        }
    }
    private static final List<Taxons> species =  Arrays.stream(Taxons.values()).filter(taxon -> taxon.isSpecies && taxon != CHEMICAL_SYNTHESIS).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());
    private static final List<Taxons> kingdoms =  Arrays.stream(Taxons.values()).filter(taxon -> !taxon.isSpecies).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());

    Taxons(long taxId, String descriptor, boolean isSpecies) {
        this.taxId = taxId;
        this.descriptor = descriptor;
        this.isSpecies = isSpecies;
    }

    public static List<Taxons> getSpecies() {
        return new ArrayList<>(species);
    }

    public static List<Taxons> getKingdoms() {
        return new ArrayList<>(kingdoms);
    }

    public static Taxons getTaxon(long taxId) {
        return taxIdToTaxons.get(taxId);
    }


}
