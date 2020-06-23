package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.CollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils;

import javax.swing.*;

public class EdgeAnnotations extends AbstractEdgeElement {
    private final JsonNode annotationsData;

    public EdgeAnnotations(Edge iEdge, OpenBrowser openBrowser, JsonNode annotationsData) {
        super("Annotations", iEdge, openBrowser);
        this.annotationsData = annotationsData;
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(CollapsedEdge edge) {
    }

    @Override
    protected void fillEvidenceEdgeContent(EvidenceEdge edge) {
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
