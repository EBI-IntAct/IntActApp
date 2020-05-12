package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactCollapsedEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEvidenceEdge;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.LinkUtils;

import javax.swing.*;

import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel.backgroundColor;

public class EdgeBasics extends AbstractEdgeElement {

    public EdgeBasics(IntactEdge iEdge, OpenBrowser openBrowser) {
        super(null, iEdge, openBrowser);
        fillContent();
    }

    protected void fillCollapsedEdgeContent(IntactCollapsedEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        JLabel description = new JLabel("Collapsed edge between " + edge.source.name + " and " + edge.target.name + " (MI Score = " + edge.miScore + ")");
        description.setAlignmentX(LEFT_ALIGNMENT);
        content.add(description);

        JPanel collapsedEdgesPanel = new JPanel();
        collapsedEdgesPanel.setLayout(new BoxLayout(collapsedEdgesPanel, BoxLayout.Y_AXIS));
        collapsedEdgesPanel.setBackground(backgroundColor);
        for (IntactEvidenceEdge iEEdge : edge.edges.values()) {
            collapsedEdgesPanel.add(createIntactEdgeLink(openBrowser, iEEdge));
        }
        CollapsablePanel collapsablePanel = new CollapsablePanel("Collapsed edges (" + edge.edges.size() + ")", collapsedEdgesPanel, true);
        collapsablePanel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(collapsablePanel);
    }

    protected void fillEvidenceEdgeContent(IntactEvidenceEdge edge) {
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(new JLabel("Evidence of " + edge.type + " between " + edge.source.name + " and " + edge.target.name));

        if (edge.hostOrganism != null) {
            JPanel organism = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            organism.setAlignmentX(LEFT_ALIGNMENT);
            organism.setBackground(backgroundColor);
            organism.add(new JLabel("Found in " + edge.hostOrganism.replaceFirst("In ", "")));
            organism.add(Box.createHorizontalStrut(4));
            organism.add(LinkUtils.createSpecieLink(edge.hostOrganismTaxId, openBrowser));
            organism.add(Box.createHorizontalGlue());
            content.add(organism);
        }
        {
            JPanel ebiInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            ebiInfo.setAlignmentX(LEFT_ALIGNMENT);
            ebiInfo.setBackground(backgroundColor);
            ebiInfo.add(createIntactEdgeLink(openBrowser, edge));
            ebiInfo.add(new JLabel(" (MI Score = " + edge.miScore + ")"));
            ebiInfo.add(Box.createHorizontalGlue());
            content.add(ebiInfo);
        }
        content.add(new JLabel("Detected with " + edge.detectionMethod));
        if (edge.expansionType != null && !edge.expansionType.isEmpty()) {
            content.add(new JLabel("Expanded with a " + edge.expansionType));
        }
        if (edge.pubMedId != null && !edge.pubMedId.isEmpty()) {
            JPanel publication = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            publication.setAlignmentX(LEFT_ALIGNMENT);
            publication.setBackground(backgroundColor);
            publication.add(new JLabel("Described in "));
            publication.add(new JLink("PubMed - " + edge.pubMedId, "https://www.ncbi.nlm.nih.gov/pubmed/" + edge.pubMedId, openBrowser));
            publication.add(Box.createHorizontalGlue());
            content.add(publication);
        }
    }

    private JLink createIntactEdgeLink(OpenBrowser openBrowser, IntactEvidenceEdge iEEdge) {
        return new JLink(iEEdge.ac, "https://www.ebi.ac.uk/intact/interaction/" + iEEdge.ac, openBrowser);
    }
}
