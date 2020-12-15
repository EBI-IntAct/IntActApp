package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.ui.components.labels.SelectableLabel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils;

import javax.swing.*;
import java.awt.*;

public class EdgeAnnotations extends AbstractEdgeAttribute {
    private final JsonNode annotationsData;

    public EdgeAnnotations(Edge edge, OpenBrowser openBrowser, JsonNode annotationsData) {
        super("Annotations", edge, openBrowser);
        this.annotationsData = annotationsData;
        if (annotationsData == null) setVisible(false);
        else fillContent();
    }

    @Override
    protected void fillSummaryEdgeContent(SummaryEdge edge) {
    }

    @Override
    protected void fillEvidenceEdgeContent(EvidenceEdge edge) {
        if (annotationsData != null && !annotationsData.isNull() && !annotationsData.isEmpty()) {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            GroupUtils.groupElementsInPanel(content, annotationsData,
                    annotationData -> StringUtils.capitalize(annotationData.get("topic").get("shortName").textValue()),
                    (toFill, annotationsDataOfTopic) -> {
                        int nbAnnotation = 0;
                        for (JsonNode annotationDataOfTopic : annotationsDataOfTopic) {
                            toFill.add(new SelectableLabel(annotationDataOfTopic.get("description").textValue()));
                            nbAnnotation++;
                        }

                        CollapsablePanel parent = (CollapsablePanel) toFill.getParent();
                        if (parent.getTitle().contains("Caution")) {
                            SelectableLabel caution = new SelectableLabel("Caution (" + nbAnnotation + ") ");
                            caution.setForeground(Color.white);
                            caution.setBackground(Color.red);
                            caution.setOpaque(true);
                            parent.setHeader(caution);
                        }
                    }
            );
        } else {
            setVisible(false);
        }
    }
}
