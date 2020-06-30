package uk.ac.ebi.intact.app.internal.model.tables.fields.models;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.ListField;

public class EdgeFields {
    public static final Field<Long> SUID = new Field<>(Table.EDGE, Field.Namespace.NULL, CyEdge.SUID, null, Long.class);
    public static final Field<String> AC = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Accession", "ac", String.class);
    public static final Field<String> NAME = new Field<>(Table.EDGE, Field.Namespace.NULL, CyNetwork.NAME, null, String.class);
    public static final Field<String> TYPE = new Field<>(Table.EDGE, Field.Namespace.NULL, CyEdge.INTERACTION, "interaction_type", String.class);
    public static final Field<String> TYPE_MI_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Type MI Id", "interaction_type_mi_identifier", String.class);
    public static final Field<Double> MI_SCORE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "MI Score", "mi_score", Double.class);
    public static final Field<String> EXPANSION_TYPE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Expansion type", "expansion_type", String.class);
    public static final Field<String> HOST_ORGANISM = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Host organism", "host_organism", String.class);
    public static final Field<Long> HOST_ORGANISM_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Host organism taxon id", "host_organism_tax_id", Long.class);
    public static final Field<String> PUBMED_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "PubMed Id", "pubmed_id", String.class);
    public static final Field<String> INTERACTION_DETECTION_METHOD = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Interaction detection method", "interaction_detection_method", String.class);
    public static final Field<String> INTERACTION_DETECTION_METHOD_MI_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Interaction detection method MI Id", "interaction_detection_method_mi_identifier", String.class);
    public static final Field<String> PARTICIPANT_DETECTION_METHOD = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Participant detection method", "participant_detection_method", String.class);
    public static final Field<String> PARTICIPANT_DETECTION_METHOD_MI_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Participant detection method MI Id", "participant_detection_method_mi_identifier", String.class);
    public static final Field<Boolean> AFFECTED_BY_MUTATION = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Affected by mutation", null, Boolean.class, false);

    public static final Field<Boolean> C_IS_COLLAPSED = new Field<>(Table.EDGE, Field.Namespace.INTACT, "is collapsed", null, Boolean.class, false);
    public static final ListField<Long> C_INTACT_IDS = new ListField<>(Table.EDGE, Field.Namespace.INTACT, "collapsed edges Id", Long.class);
    public static final ListField<Long> C_INTACT_SUIDS = new ListField<>(Table.EDGE, Field.Namespace.INTACT, "collapsed edges SUID", Long.class);
    public static final Field<Integer> C_NB_EDGES = new Field<>(Table.EDGE, Field.Namespace.INTACT, "# evidences", null, Integer.class);

    public static final Field<Long> ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "ID", "id", Long.class);

    public static final Field<String> SOURCE_BIOLOGICAL_ROLE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Source biological role", null, String.class);
    public static final Field<String> SOURCE_BIOLOGICAL_ROLE_MI_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Source biological role MI Id", null, String.class);
    public static final ListField<String> SOURCE_FEATURES = new ListField<>(Table.EDGE, Field.Namespace.FEATURE, "Source features", String.class);

    public static final Field<String> TARGET_BIOLOGICAL_ROLE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Target biological role", null, String.class);
    public static final Field<String> TARGET_BIOLOGICAL_ROLE_MI_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Target biological role MI Id", null, String.class);
    public static final ListField<String> TARGET_FEATURES = new ListField<>(Table.EDGE, Field.Namespace.FEATURE, "Target features", String.class);
}
