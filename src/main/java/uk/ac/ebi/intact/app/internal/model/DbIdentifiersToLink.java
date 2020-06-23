package uk.ac.ebi.intact.app.internal.model;

import com.fasterxml.jackson.databind.JsonNode;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.Identifier;
import uk.ac.ebi.intact.app.internal.model.core.ontology.OntologyIdentifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class DbIdentifiersToLink {
    private enum Database {
        UNIPROT("uniprotkb", "UniProt", s -> "https://www.uniprot.org/uniprot/" + s),
        INTACT("intact", "IntAct", s -> "https://www.ebi.ac.uk/intact/molecule/" + s),
        CH_EBI("chebi", "ChEBI", s -> "https://www.ebi.ac.uk/chebi/searchId.do?chebiId=" + s),
        CH_EMBL("chembl", "ChEMBL", s -> "https://www.ebi.ac.uk/chembl/compound_report_card/" + s),
        DIP("dip", "DIP", s -> "https://dip.doe-mbi.ucla.edu/dip/DIPview.cgi?PK=" + s.replace("DIP-", "").substring(0, s.length() - 5)),
        REF_SEQ("refseq", "RefSeq", s -> "https://www.ncbi.nlm.nih.gov/search/all/?term=" + s),
        GENBANK_PROTEIN("genbank_protein_gi", "GenBank Protein", s -> "https://www.ncbi.nlm.nih.gov/protein/" + s),
        ENSEMBL("ensembl", "Ensembl", s -> "http://www.ensembl.org/id/" + s),
        MATRIX_DB("matrixdb", "MatrixDB", s -> "http://matrixdb.univ-lyon1.fr/cgi-bin/current/newPort?type=biomolecule&value=" + s),
        RNA_CENTRAL("rnacentral", "RNA central", s -> "https://rnacentral.org/rna/" + s),
        HGNC("hgnc", "HGNC", s -> "https://www.genenames.org/data/gene-symbol-report/#!/hgnc_id/" + s),
        COMPLEX_PORTAL("complex portal", "Complex Portal", s -> "https://www.ebi.ac.uk/complexportal/complex/" + s),
        MINT("mint", "MINT", s -> "https://mint.bio.uniroma2.it/index.php/results-interactions/?id=" + s),
        INTERPRO("interpro", "InterPro", s -> "https://www.ebi.ac.uk/interpro/entry/InterPro/" + s),
        UNIPARC("uniparc", "UniParc", s -> "https://www.uniprot.org/uniparc/" + s + "?sort=score");

        String name;
        String fancyName;
        Function<String, String> queryFunction;

        Database(String name, String fancyName, Function<String, String> queryFunction) {
            this.name = name;
            this.fancyName = fancyName;
            this.queryFunction = queryFunction;
        }
    }

    private static final Map<String, Database> dbNameToDatabase = new HashMap<>();
    private static final Map<OntologyIdentifier, String> unknownDatabaseMIIdToSearchURLRegex = new HashMap<>();
    private static final Set<OntologyIdentifier> unresolvedUnknownDatabaseMIId = new HashSet<>();

    static {
        for (Database database : Database.values()) {
            dbNameToDatabase.put(database.name, database);
        }
    }

    public static String getLink(Identifier identifier) {
        if (dbNameToDatabase.containsKey(identifier.databaseName)) {
            return dbNameToDatabase.get(identifier.databaseName).queryFunction.apply(identifier.id);
        } else if (unknownDatabaseMIIdToSearchURLRegex.containsKey(identifier.databaseIdentifier)) {
            return unknownDatabaseMIIdToSearchURLRegex.get(identifier.databaseIdentifier).replaceAll("\\$\\{ac}", identifier.id);
        } else if (unresolvedUnknownDatabaseMIId.contains(identifier.databaseIdentifier)) {
            return "";
        } else {
            try {
                JsonNode root = HttpUtils.getJsonForUrl(identifier.databaseIdentifier.getDetailsURL());
                if (root != null) {
                    for (JsonNode info : root.get("_embedded").get("terms").get(0).get("obo_xref")) {
                        if (info.get("database").textValue().equals("search-url")) {
                            String searchURLRegex = info.get("description").textValue();
                            unknownDatabaseMIIdToSearchURLRegex.put(identifier.databaseIdentifier, searchURLRegex);
                            return searchURLRegex.replaceAll("\\$\\{ac}", identifier.id);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
            unresolvedUnknownDatabaseMIId.add(identifier.databaseIdentifier);
            return "";
        }
    }


    public static String getFancyDatabaseName(Identifier identifier) {
        if (!dbNameToDatabase.containsKey(identifier.databaseName)) {
            return identifier.databaseName.toUpperCase();
        }
        return dbNameToDatabase.get(identifier.databaseName).fancyName;
    }


}
