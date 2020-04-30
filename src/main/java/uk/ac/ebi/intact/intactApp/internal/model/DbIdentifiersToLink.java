package uk.ac.ebi.intact.intactApp.internal.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class DbIdentifiersToLink {
    private static final Map<String, Function<String, String>> dbNameToIdToQueryFunction = new HashMap<>();

    static {
        dbNameToIdToQueryFunction.put("uniprotkb", s -> "https://www.uniprot.org/uniprot/" + s);
        dbNameToIdToQueryFunction.put("intact", s -> "https://www.ebi.ac.uk/intact/molecule/" + s);
        dbNameToIdToQueryFunction.put("chebi", s -> "https://www.ebi.ac.uk/chebi/searchId.do?chebiId=" + s);
        dbNameToIdToQueryFunction.put("dip", s -> "https://dip.doe-mbi.ucla.edu/dip/DIPview.cgi?PK=" + s.replace("DIP-", "").substring(0, s.length() - 5));
    }

    private static final Map<String, String> dbNameToFancyName = new HashMap<>();

    static {
        dbNameToFancyName.put("uniprotkb", "UniProt");
        dbNameToFancyName.put("intact", "IntAct");
        dbNameToFancyName.put("chebi", "ChEBI");
        dbNameToFancyName.put("dip", "DIP");
    }

    public static String getLink(String dbName, String identifier) {
        return dbNameToIdToQueryFunction.get(dbName).apply(identifier);
    }

    public static String fancy(String dbName) {
        return dbNameToFancyName.get(dbName);
    }


}
