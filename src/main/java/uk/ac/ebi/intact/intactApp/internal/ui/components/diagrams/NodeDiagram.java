package uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams;

import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.styles.MutationIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.utils.CollectionUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeDiagram extends InteractorDiagram {
    public final IntactNode node;

    public NodeDiagram(IntactNode node, List<Feature> features) {
        super(node);
        this.node = node;
        Set<OntologyIdentifier> featureTypeIDs = features.stream().map(feature -> feature.typeIdentifier).collect(Collectors.toSet());
        if (CollectionUtils.anyCommonElement(featureTypeIDs, FeatureClassifier.mutation.innerIdentifiers)) {
            shape.setBorderColor(MutationIntactStyle.mutatedColor);
            shape.setBorderThickness(4);
        }
    }
}
