package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.Feature;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.components.diagrams.NodeDiagram;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class NodeSchematic extends AbstractNodeElement {

    private NodeDiagram nodeDiagram;
    private final List<Feature> features;

    public NodeSchematic(IntactNode iNode, List<Feature> features, OpenBrowser openBrowser) {
        super(null, iNode, openBrowser);
        this.features = features;
        fillContent();
    }

    @Override
    protected void fillContent() {
        content.setLayout(new GridBagLayout());
        nodeDiagram = new NodeDiagram(iNode, features);
        EasyGBC c = new EasyGBC();
        content.add(nodeDiagram, c.anchor("west").noExpand());
        content.add(Box.createHorizontalGlue(), c.right().anchor("west").expandHoriz());
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (nodeDiagram != null) {
            nodeDiagram.setBackground(bg);
        }
    }
}
