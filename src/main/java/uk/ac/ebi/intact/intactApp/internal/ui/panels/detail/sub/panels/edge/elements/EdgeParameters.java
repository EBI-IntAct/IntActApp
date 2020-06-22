package uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.edge.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.ontology.OntologyIdentifier;
import uk.ac.ebi.intact.intactApp.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils;

import javax.swing.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;

public class EdgeParameters extends AbstractEdgeElement {
    private final JsonNode parametersData;

    public EdgeParameters(IntactEdge iEdge, OpenBrowser openBrowser, JsonNode parametersData) {
        super("Parameters", iEdge, openBrowser);
        this.parametersData = parametersData;
        fillContent();
    }

    @Override
    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
    }

    @Override
    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        if (parametersData != null && !parametersData.isNull() && !parametersData.isEmpty()) {
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            GroupUtils.groupElementsInPanel(content, parametersData,
                    annotationData -> StringUtils.capitalize(annotationData.get("type").get("shortName").textValue()),
                    (toFill, annotationsDataOfTopic) -> {
                        for (JsonNode annotationDataOfTopic : annotationsDataOfTopic) {
                            LinePanel parameter = new LinePanel(backgroundColor);
                            parameter.add(fancyValue(annotationDataOfTopic.get("value").textValue()));
                            parameter.add(Box.createHorizontalStrut(4));

                            JsonNode unit = annotationDataOfTopic.get("unit");
                            VerticalPanel unitPanel = new VerticalPanel(getBackground());
                            unitPanel.add(Box.createVerticalStrut(5)); //TODO Align the unit panel in a more clever way
                            unitPanel.add(new JLink(
                                    unit.get("shortName").textValue(),
                                    new OntologyIdentifier(unit.get("identifier").textValue()).getUserAccessURL(),
                                    openBrowser
                            ));
                            parameter.add(unitPanel);
                            toFill.add(parameter);
                        }
                    }
            );
        } else {
            setVisible(false);
        }
    }

    private static JLabel fancyValue(String rawValue) {
        String[] parts = rawValue.split("x");
        if (parts.length == 2) {
            Double decimalPart = Double.parseDouble(parts[0]);
            String[] poweringPart = parts[1].split("\\^");
            if (poweringPart.length == 2) {
                return new JLabel(String.format("<html>%.3f x %s<sup>%s</sup></html>", decimalPart, poweringPart[0], poweringPart[1].replaceAll("[()]", "")));
            }
        }
        return new JLabel(rawValue);
    }
}
