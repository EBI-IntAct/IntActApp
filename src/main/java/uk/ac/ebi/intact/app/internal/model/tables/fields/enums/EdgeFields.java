package uk.ac.ebi.intact.app.internal.model.tables.fields.enums;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.SourceOntology;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.CVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.Field;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.FieldInitializer;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant.ParticipantCVField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.participant.ParticipantListField;

import java.util.ArrayList;
import java.util.List;

public class EdgeFields {
    public final static List<Field<?>> fields = new ArrayList<>();
    public final static List<FieldInitializer> initializers = new ArrayList<>();

    public static final Field<Long> SUID = new Field<>(fields, initializers, Field.Namespace.NULL, CyNetwork.SUID, null, Long.class, true);
    public static final Field<String> AC = new Field<>(fields, initializers, Field.Namespace.INTACT, "Accession", "ac", String.class, true);
    public static final Field<String> NAME = new Field<>(fields, initializers, Field.Namespace.NULL, CyNetwork.NAME, null, String.class, true);
    public static final CVField TYPE = new CVField(fields, initializers, Field.Namespace.NULL, CyEdge.INTERACTION, "interaction_type", true);
    public static final Field<Double> MI_SCORE = new Field<>(fields, initializers, Field.Namespace.INTACT, "MI Score", "mi_score", Double.class, true);
    public static final Field<Double> WEIGHT = new Field<>(fields, initializers, Field.Namespace.INTACT, "Weight", null, Double.class, false);
    public static final Field<String> EXPANSION_TYPE = new Field<>(fields, initializers, Field.Namespace.INTACT, "Expansion type", "expansion_type", String.class, true);
    public static final Field<String> HOST_ORGANISM = new Field<>(fields, initializers, Field.Namespace.INTACT, "Host organism", "host_organism", String.class, true);
    public static final Field<String> HOST_ORGANISM_ID = new Field<>(fields, initializers, Field.Namespace.INTACT, "Host organism taxon id", "host_organism_tax_id", String.class, true);
    public static final Field<String> PUBMED_ID = new Field<>(fields, initializers, Field.Namespace.INTACT, "PubMed Id", "pubmed_id", String.class, true);
    public static final CVField INTERACTION_DETECTION_METHOD = new CVField(fields, initializers, Field.Namespace.INTACT, "Interaction detection method", "interaction_detection_method", true);
    public static final CVField PARTICIPANT_DETECTION_METHOD = new CVField(fields, initializers, Field.Namespace.INTACT, "Participant detection method", "participant_detection_method", true);
    public static final Field<Boolean> AFFECTED_BY_MUTATION = new Field<>(fields, initializers, Field.Namespace.INTACT, "Affected by mutation", null, Boolean.class, true, Boolean.FALSE);

    public static final Field<Boolean> IS_SUMMARY = new Field<>(fields, initializers, Field.Namespace.SUMMARY, "Is summary", null, Boolean.class, true, Boolean.FALSE);
    public static final ListField<Long> SUMMARIZED_EDGES_SUID = new ListField<>(fields, initializers, Field.Namespace.SUMMARY, "Summarized edges SUID", Long.class, false);
    public static final Field<Integer> SUMMARY_NB_EDGES = new Field<>(fields, initializers, Field.Namespace.SUMMARY, "# Summarized edges", null, Integer.class, false, false);

    public static final ParticipantCVField BIOLOGICAL_ROLE = new ParticipantCVField(fields, initializers, "biological role", "participant_biological_role_name", "participant_biological_role_mi_identifier", SourceOntology.MI, true);
    public static final ParticipantCVField EXPERIMENTAL_ROLE = new ParticipantCVField(fields, initializers, "experimental role", "participant_experimental_role_name", "participant_experimental_role_mi_identifier", SourceOntology.MI, true);
    public static final ParticipantListField<String> FEATURES = new ParticipantListField<>(fields, initializers, "features", String.class, true);

    public static final Field<Long> ID = new Field<>(fields, initializers, Field.Namespace.INTACT, "ID", "id", Long.class, true);

    public static final Field<Boolean> IS_NEGATIVE_INTERACTION = new Field<>(fields, initializers, Field.Namespace.SUMMARY, "negative interaction", "is_negative", Boolean.class, true, Boolean.FALSE);
}
