package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;

import javax.swing.*;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public class EdgeBasics extends AbstractEdgeElement {

    public EdgeBasics(Edge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        fillContent();
    }

    protected void fillSummaryEdgeContent(SummaryEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        VerticalPanel summaryEdgesPanel = new VerticalPanel(lightBackground);
        for (EvidenceEdge evidenceEdge : edge.getSubEdges().values()) {
            summaryEdgesPanel.add(LinkUtils.createIntactEdgeLink(openBrowser, evidenceEdge));
        }
        CollapsablePanel collapsablePanel = new CollapsablePanel("Summarized edges (" + edge.subEdgeSUIDs.size() + ")", summaryEdgesPanel, true);
        collapsablePanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(collapsablePanel);
    }

    protected void fillEvidenceEdgeContent(EvidenceEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (edge.hostOrganism != null) {
            LinePanel organism = new LinePanel(lightBackground);
            organism.add(new JLabel("Found in " + edge.hostOrganism.replaceFirst("In ", "")));
            organism.add(Box.createHorizontalStrut(4));
            organism.add(LinkUtils.createSpecieLink(openBrowser, edge.hostOrganismTaxId));
            organism.add(Box.createHorizontalGlue());
            content.add(organism);
        }
        {
            LinePanel ebiInfo = new LinePanel(lightBackground);
            ebiInfo.add(LinkUtils.createIntactEdgeLink(openBrowser, edge));
            ebiInfo.add(new JLabel(" (MI Score = " + edge.miScore + ")"));
            ebiInfo.add(Box.createHorizontalGlue());
            content.add(ebiInfo);
        }
        {
            LinePanel line = new LinePanel(lightBackground);
            line.add(new JLabel("Interaction detected with "));
            line.add(LinkUtils.createCVTermLink(openBrowser, edge.interactionDetectionMethod));
            line.add(Box.createHorizontalGlue());
            content.add(line);
        }
        {
            LinePanel line = new LinePanel(lightBackground);
            line.add(new JLabel("Participants detected with "));
            line.add(LinkUtils.createCVTermLink(openBrowser, edge.participantDetectionMethod));
            line.add(Box.createHorizontalGlue());
            content.add(line);
        }

        if (edge.expansionType != null && !edge.expansionType.isEmpty()) {
            content.add(new JLabel("Expanded with a " + edge.expansionType));
        }
        if (edge.pubMedId != null && !edge.pubMedId.isEmpty()) {
            LinePanel publication = new LinePanel(lightBackground);
            publication.add(new JLabel("Described in "));
            publication.add(new JLink("European PMC - " + edge.pubMedId, "https://europepmc.org/search?query=" + edge.pubMedId, openBrowser));
            publication.add(Box.createHorizontalGlue());
            content.add(publication);
        }
    }

}
