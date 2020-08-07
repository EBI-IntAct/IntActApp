package uk.ac.ebi.intact.app.internal.ui.components.diagrams;

import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.features.Feature;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.app.internal.model.styles.MutationStyle;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeDiagram extends InteractorDiagram {
    public final Node node;

    public NodeDiagram(Node node, List<Feature> features) {
        super(node);
        this.node = node;
        Set<OntologyIdentifier> featureTypeIDs = features.stream().map(feature -> feature.type.id).collect(Collectors.toSet());
        if (CollectionUtils.anyCommonElement(featureTypeIDs, FeatureClassifier.mutation.innerIdentifiers)) {
            shape.setBorderColor(MutationStyle.mutatedColor);
            shape.setBorderThickness(4);
        }
    }
}
