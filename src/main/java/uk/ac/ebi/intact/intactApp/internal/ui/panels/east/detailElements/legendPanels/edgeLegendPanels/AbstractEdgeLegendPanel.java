package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements.legendPanels.edgeLegendPanels;

import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;

import javax.swing.*;

import java.awt.*;

import static uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel.backgroundColor;

public abstract class AbstractEdgeLegendPanel extends JPanel {
    protected EasyGBC layoutHelper = new EasyGBC();

    public AbstractEdgeLegendPanel() {
        super(new GridBagLayout());
        setBackground(backgroundColor);
    }
}
