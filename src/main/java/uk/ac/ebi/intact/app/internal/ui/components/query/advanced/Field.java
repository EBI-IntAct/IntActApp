package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;

import java.time.Year;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public enum Field {
    P_ID("id","Identifiers", "string", "participant"),
    P_ID_A("idA","Identifier", "string", "participantA"),
    P_ID_B("idB","Identifier", "string", "participantB"),
    P_IDENTIFIER("identifier","Identifiers, Alternatives, Aliases", "string", "participant"),
    P_ALT_ID_A("altidA","Alternative id", "string", "participantA"),
    P_ALT_ID_B("altidB","Alternative id", "string", "participantB"),
    P_ALIAS("alias","Alias", "string", "participant"),
    P_ALIAS_A("aliasA","Alias", "string", "participantA"),
    P_ALIAS_B("aliasB","Alias", "string", "participantB"),
    PUB_YEAR("pubyear","Publication year", "int-range", "publication", "[2000 TO " + Year.now().getValue() + "]", new String[]{"∈", "∉"}),
    PUB_AUTHORS("pubauthors","Publication author(s)", "string", "publication"),
    PUB_FIRST_AUTH("pubauth","Publication 1st author(s)", "string", "publication"),
    PUB_ID("pubid","Publication Identifier(s)", "string", "publication"),
    P_TAX_ID_A("taxidA","Taxon id or Species", "string", "participantA"),
    P_TAX_ID_B("taxidB","Taxon id or Species", "string", "participantB"),
    TAX_ID_HOST("taxidHost","Taxon id or Species of Host organism", "string", "interaction"),
    P_SPECIES("species","Taxon id or Species", "string", "participant"),
    I_TYPE("type","Interaction type(s)", "string", "interaction"),
    I_DET_METHOD("detmethod","Interaction Detection method(s)", "string", "interaction"),
    I_ID("interaction_id","Interaction identifier(s)", "string", "interaction"),
    P_BIO_ROLE_A("pbioroleA","Biological role", "string", "participantA"),
    P_BIO_ROLE_B("pbioroleB","Biological role", "string", "participantB"),
    P_BIO_ROLE("pbiorole","Biological role", "string", "participant"),
    P_TYPE("ptype","Interactor type", "string", "participant"),
    P_TYPE_A("ptypeA","Interactor type", "string", "participantA"),
    P_TYPE_B("ptypeB","Interactor type", "string", "participantB"),
    P_XREF("pxref","Interactor xref", "string", "participant"),
    P_XREF_A("pxrefA","Interactor xref", "string", "participantA"),
    P_XREF_B("pxrefB","Interactor xref", "string", "participantB"),
    INTACT_MI_SCORE("intact-miscore","IntAct MI Score", "float-range", "interaction", "[0 TO 1]", new String[]{"∈", "∉"}),
    P_GENE_NAME("geneName","Gene Name", "string", "participant"),
    P_GENE_NAME_A("geneNameA","Gene Name", "string", "participantA"),
    P_GENE_NAME_B("geneNameB","Gene Name", "string", "participantB"),
    I_XREF("xref","Interaction xref", "string", "interaction"),
    I_ANNOTATION("annot","Interaction annotation(s)", "string", "interaction"),
    R_DATE("rdate","Release date", "date-range", "curationMetadata", "[20030101 TO " + Year.now().getValue() + "1231]", new String[]{"∈", "∉"}),
    U_DATE("udate","Update date", "date-range", "curationMetadata", "[20030101 TO " + Year.now().getValue() + "1231]", new String[]{"∈", "∉"}),
    NEGATIVE("negative","Negative interaction", "boolean", "interaction", "TRUE", new String[]{"TRUE", "FALSE"}),
    P_MUTATION_A("mutationA","Mutation of Interactor A", "boolean", "participantA", "TRUE", new String[]{"TRUE", "FALSE"}),
    P_MUTATION_B("mutationB","Mutation of Interactor B", "boolean", "participantB", "TRUE", new String[]{"TRUE", "FALSE"}),
    P_MUTATION("mutation","Mutation of Interactor", "boolean", "participant", "TRUE", new String[]{"TRUE", "FALSE"}),
    COMPLEX("complex","Complex expansion", "category", "interaction", "-"), //TODO:add the spoke expansion
    P_FEATURE_TYPE_A("ftypeA","Feature type", "string", "participantA"),
    P_FEATURE_TYPE_B("ftypeB","Feature type", "string", "participantB"),
    P_FEATURE_TYPE("ftype","Feature type", "string", "participant"),
    P_IDENTIFICATION_METHOD("pmethod","Interactor identification method", "string", "participant"),
    P_IDENTIFICATION_METHOD_A("pmethodA","Interactor identification method", "string", "participantA"),
    P_IDENTIFICATION_METHOD_B("pmethodB","Interactor identification method", "string", "participantB"),
    STOICHIOMETRY("stc","Stoichiometry", "string", "participant", "TRUE", new String[]{"TRUE", "FALSE"}),
    I_PARAMETERS("param","Interaction parameters", "string", "interaction", "TRUE", new String[]{"TRUE", "FALSE"}),
    SOURCE_DB("source","Source database", "string", "curationMetadata");

    private final String miqlQuery;
    private final String name;
    private final String type;
    private final String entity;
    private final String defaultValue;
    private final String[] operators;

    public static final Map<String, Field> ENTITY_NAME_TO_FIELD = new LinkedHashMap<>();
    public static final Map<String, Field> MIQL_TO_FIELD = new HashMap<>();

    static {
        for (Field field : values()) {
            ENTITY_NAME_TO_FIELD.put(joinEntityName(field.getEntity(), field.getName()).toLowerCase(), field);
            MIQL_TO_FIELD.put(field.getMiqlQuery().toLowerCase(), field);
        }
    }

    Field(String miqlQuery, String name, String type, String entity) {
        this(miqlQuery, name, type, entity, null, new String[]{"=", "≠", "in", "not in"});
    }

    Field(String miqlQuery, String name, String type, String entity, String defaultValue) {
        this(miqlQuery, name, type, entity, defaultValue, new String[]{"=", "≠", "in", "not in"});
    }

    Field(String miqlQuery, String name, String type, String entity, String defaultValue, String[] operators) {
        this.miqlQuery = miqlQuery;
        this.name = name;
        this.type = type;
        this.entity = entity;
        this.defaultValue = defaultValue;
        this.operators = operators;
    }

    public static Field getFieldFromNameAndEntity(String name, String entity) {
        return ENTITY_NAME_TO_FIELD.get(joinEntityName(entity, name).toLowerCase());
    }

    public static Field getFieldsFromMiQL(String miqlQuery) {
        return MIQL_TO_FIELD.get(miqlQuery.toLowerCase());
    }

    public static String[] getEntities() {
        return ENTITY_NAME_TO_FIELD.values().stream()
                .map(Field::getEntity)
                .distinct()
                .toArray(String[]::new);
    }

    public static String getMiQlRegex() {
        return ENTITY_NAME_TO_FIELD.values().stream()
                .map(Field::getMiqlQuery)
                .distinct()
                .collect(Collectors.joining("|"));
    }

    public static String joinEntityName(String entity, String name) {
        return entity + "-" + name;
    }
}
