package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils;

import javax.swing.*;

public class EdgeAnnotations extends AbstractEdgeElement {
    private final JsonNode annotationsData;

    public EdgeAnnotations(IntactEdge iEdge, OpenBrowser openBrowser, JsonNode annotationsData) {
        super("Annotations", iEdge, openBrowser);
        this.annotationsData = annotationsData;
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
    }

    @Override
    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        if (annotationsData != null && !annotationsData.isNull() && !annotationsData.isEmpty()){
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            GroupUtils.groupElementsInPanel(content, annotationsData,
                    annotationData -> StringUtils.capitalize(annotationData.get("topic").get("shortName").textValue()),
                    (toFill, annotationsDataOfTopic) -> {
                        for (JsonNode annotationDataOfTopic : annotationsDataOfTopic) {
                            toFill.add(new JLabel(annotationDataOfTopic.get("description").textValue()));
                        }
                    }
            );
        } else {
            setVisible(false);
        }
    }
}
