package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant.ParticipantCVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant.ParticipantListField;

public class EdgeFields {
    public static final Field<String> AC = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Accession", "ac", String.class);
    public static final Field<String> NAME = new Field<>(Table.EDGE, Field.Namespace.NULL, CyNetwork.NAME, null, String.class);
    public static final CVField TYPE = new CVField(Table.EDGE, Field.Namespace.NULL, CyEdge.INTERACTION, "interaction_type");
    public static final Field<Double> MI_SCORE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "MI Score", "mi_score", Double.class);
    public static final Field<String> EXPANSION_TYPE = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Expansion type", "expansion_type", String.class);
    public static final Field<String> HOST_ORGANISM = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Host organism", "host_organism", String.class);
    public static final Field<Long> HOST_ORGANISM_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Host organism taxon id", "host_organism_tax_id", Long.class);
    public static final Field<String> PUBMED_ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "PubMed Id", "pubmed_id", String.class);
    public static final CVField INTERACTION_DETECTION_METHOD = new CVField(Table.EDGE, Field.Namespace.INTACT, "Interaction detection method", "interaction_detection_method");
    public static final CVField PARTICIPANT_DETECTION_METHOD = new CVField(Table.EDGE, Field.Namespace.INTACT, "Participant detection method", "participant_detection_method");
    public static final Field<Boolean> AFFECTED_BY_MUTATION = new Field<>(Table.EDGE, Field.Namespace.INTACT, "Affected by mutation", null, Boolean.class, Boolean.FALSE);

    public static final Field<Boolean> IS_SUMMARY = new Field<>(Table.EDGE, Field.Namespace.SUMMARY, "Is summary", null, Boolean.class, Boolean.FALSE);
    public static final ListField<Long> SUMMARY_EDGES_SUID = new ListField<>(Table.EDGE, Field.Namespace.SUMMARY, "Summarized edges SUID", Long.class);
    public static final Field<Integer> SUMMARY_NB_EDGES = new Field<>(Table.EDGE, Field.Namespace.SUMMARY, "# Summarized edges", null, Integer.class, false);

    public static final ParticipantCVField BIOLOGICAL_ROLE = new ParticipantCVField(Table.EDGE, "biological role", "participant_biological_role_name", "participant_biological_role_mi_identifier", SourceOntology.MI);
    public static final ParticipantCVField EXPERIMENTAL_ROLE = new ParticipantCVField(Table.EDGE, "experimental role", "participant_experimental_role_name", "participant_experimental_role_mi_identifier", SourceOntology.MI);
    public static final ParticipantListField<String> FEATURES = new ParticipantListField<>(Table.EDGE, "features", String.class);

    public static final Field<Long> ID = new Field<>(Table.EDGE, Field.Namespace.INTACT, "ID", "id", Long.class);
}
