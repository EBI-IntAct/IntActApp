package uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public enum Taxons {
    D_MELANOGASTER(7227L, "Drosophila melanogaster", true, new Color(50, 147, 143)),
    C_ELEGANS(6239L, "Caenorhabditis elegans", true, new Color(74, 147, 121)),
    H_SAPIENS(9606L, "Homo sapiens", true, new Color(51, 94, 148)),
    M_MUSCULUS(10090L, "Mus musculus", true, new Color(28, 67, 156)),
    A_THALIANA(3702L, "Arabidopsis thaliana", true, new Color(46, 93, 46)),
    S_CEREVISIAE(4932L, "Saccharomyces cerevisiae", true, new Color(174, 125, 52)),
    E_COLI(562L, "Escherichia coli", true, new Color(137, 51, 54)),

    CHEMICAL_SYNTHESIS(-2L, "Chemical Synthesis", true, new Color(141, 102, 102)),

    ANIMALS(33208L, "Other animals", false, new Color(62, 181, 170)),
    MAMMALS(40674L, "Other mammals", false, new Color(86, 136, 192)),
    PLANTS(33090L, "Other plants", false, new Color(80, 162, 79)),
    EUGLENA(33682L, "Other Euglena", false, new Color(127, 184, 14)),
    FUNGI(4751L, "Other fungi", false, new Color(235, 144, 0)),
    AMOEBOZOA(554915L, "Other Amoebas", false, new Color(195, 84, 26)),
    BACTERIA(2L, "Other bacteria", false, new Color(178, 53, 57)),
    ARCHAEA(2157L, "Other archaea", false, new Color(172, 71, 101)),
    VIRUSES(10239L, "Other viruses", false, new Color(132, 100, 190)),
    ARTIFICIAL(81077L, "Other artificial molecules", false, new Color(101, 101, 101));

    public final long taxId;
    public final String descriptor;
    public final boolean isSpecies;
    public final Color defaultColor;
    private static final Map<Long, Taxons> taxIdToTaxons = new HashMap<>();

    static {
        for (Taxons taxon : Taxons.values()) {
            taxIdToTaxons.put(taxon.taxId, taxon);
        }
    }

    private static final List<Taxons> species = Arrays.stream(Taxons.values()).filter(taxon -> taxon.isSpecies && taxon != CHEMICAL_SYNTHESIS).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());
    private static final List<Taxons> kingdoms = Arrays.stream(Taxons.values()).filter(taxon -> !taxon.isSpecies).sorted(Comparator.comparing(o -> o.descriptor)).collect(Collectors.toList());

    Taxons(long taxId, String descriptor, boolean isSpecies, Color defaultColor) {
        this.taxId = taxId;
        this.descriptor = descriptor;
        this.isSpecies = isSpecies;
        this.defaultColor = defaultColor;
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



