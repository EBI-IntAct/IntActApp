package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.panels.VerticalPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;

import javax.swing.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class EdgeBasics extends AbstractEdgeElement {

    public EdgeBasics(IntactEdge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        fillContent();
    }

    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        VerticalPanel collapsedEdgesPanel = new VerticalPanel(backgroundColor);
        for (IntactEvidenceEdge iEEdge : edge.edges.values()) {
            collapsedEdgesPanel.add(LinkUtils.createIntactEdgeLink(openBrowser, iEEdge));
        }
        CollapsablePanel collapsablePanel = new CollapsablePanel("Collapsed edges (" + edge.edges.size() + ")", collapsedEdgesPanel, true);
        collapsablePanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(collapsablePanel);
    }

    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        if (edge.hostOrganism != null) {
            LinePanel organism = new LinePanel(backgroundColor);
            organism.add(new JLabel("Found in " + edge.hostOrganism.replaceFirst("In ", "")));
            organism.add(Box.createHorizontalStrut(4));
            organism.add(LinkUtils.createSpecieLink(openBrowser, edge.hostOrganismTaxId));
            organism.add(Box.createHorizontalGlue());
            content.add(organism);
        }
        {
            LinePanel ebiInfo = new LinePanel(backgroundColor);
            ebiInfo.add(LinkUtils.createIntactEdgeLink(openBrowser, edge));
            ebiInfo.add(new JLabel(" (MI Score = " + edge.miScore + ")"));
            ebiInfo.add(Box.createHorizontalGlue());
            content.add(ebiInfo);
        }
        content.add(new JLabel("Detected with " + edge.detectionMethod));
        if (edge.expansionType != null && !edge.expansionType.isEmpty()) {
            content.add(new JLabel("Expanded with a " + edge.expansionType));
        }
        if (edge.pubMedId != null && !edge.pubMedId.isEmpty()) {
            LinePanel publication = new LinePanel(backgroundColor);
            publication.add(new JLabel("Described in "));
            publication.add(new JLink("PubMed - " + edge.pubMedId, "https://www.ncbi.nlm.nih.gov/pubmed/" + edge.pubMedId, openBrowser));
            publication.add(Box.createHorizontalGlue());
            content.add(publication);
        }
    }

}
