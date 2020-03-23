package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactViewType;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IntactEdgePanel extends AbstractIntactPanel {
    JPanel scorePanel;
    private Map<CyNetwork, Map<String, Boolean>> colors;

    public IntactEdgePanel(final IntactManager manager) {
        super(manager);
        filters.get(currentNetwork).put("mi score", new HashMap<>());
        setBackground(new Color(255,255,255,0));
        colors = new HashMap<>();
        colors.put(currentNetwork, new HashMap<>());

        init();
        revalidate();
        repaint();
    }

    private void init() {
        setLayout(new GridBagLayout());
        {
            EasyGBC c = new EasyGBC();
            add(new JSeparator(SwingConstants.HORIZONTAL), c.anchor("west").expandHoriz());
            JComponent scoreSlider = createFilterSlider("mi score", "MI-Score", currentNetwork, true, 100.0);
            {
                scorePanel = new JPanel();
                scorePanel.setLayout(new GridBagLayout());
                EasyGBC d = new EasyGBC();
                scorePanel.add(scoreSlider, d.anchor("west").expandHoriz());
            }
            add(scorePanel, c.down().anchor("west").expandHoriz());
        }
    }


    void doFilter(String type) {
        Map<String, Double> filter = filters.get(currentNetwork).get(type);
        CyNetworkView view = manager.getCurrentNetworkView();

        String namespace;
        List<CyEdge> toFilter;
        if (manager.getNetworkViewType(view) == IntactViewType.COLLAPSED) {
            namespace = ModelUtils.COLLAPSED_NAMESPACE;
            toFilter = manager.getIntactNetwork(currentNetwork).getCollapsedEdges();
        } else {
            namespace = ModelUtils.INTACTDB_NAMESPACE;
            toFilter = manager.getIntactNetwork(currentNetwork).getExpandedEdges();
        }

        for (CyEdge edge : toFilter) {
            View<CyEdge> edgeView = view.getEdgeView(edge);
            CyRow edgeRow = currentNetwork.getRow(edge);
            boolean show = true;
            for (String lbl : filter.keySet()) {
//                    System.out.println(namespace + "::" + type);
                Double v = edgeRow.get(namespace, type, Double.class);
//                    System.out.println(v);
                double nv = filter.get(lbl);
                if ((v == null && nv > 0) || v < nv) {
                    show = false;
                    break;
                }
            }
            if (show) {
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
            } else {
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
            }

        }
    }

    void doColors() {
        Map<String, Boolean> color = colors.get(currentNetwork);
        Map<String, Color> colorMap = manager.getChannelColors();
        CyNetworkView view = manager.getCurrentNetworkView();
        for (CyEdge edge : currentNetwork.getEdgeList()) {
            CyRow edgeRow = currentNetwork.getRow(edge);
            double max = -1;
            Color clr = null;
            for (String lbl : color.keySet()) {
                if (!color.get(lbl))
                    continue;
                Double v = edgeRow.get(ModelUtils.INTACTDB_NAMESPACE, lbl, Double.class);
                if (v != null && v > max) {
                    max = v;
                    clr = colorMap.get(lbl);
                }
            }
            if (clr == null)
                view.getEdgeView(edge).clearValueLock(BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
            else
                view.getEdgeView(edge).setLockedValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, clr);
        }
    }

    private void updateScore() {
        scorePanel.removeAll();
        JComponent scoreSlider = createFilterSlider("mi score", "MI-Score", currentNetwork, true, 100.0);


        if (!filters.get(currentNetwork).containsKey("mi score")) {
            filters.get(currentNetwork).put("mi score", new HashMap<>());
        }

        EasyGBC d = new EasyGBC();
        scorePanel.add(scoreSlider, d.anchor("west").expandHoriz());
    }


    public void networkChanged(CyNetwork newNetwork) {
        this.currentNetwork = newNetwork;
        if (!filters.containsKey(currentNetwork)) {
            filters.put(currentNetwork, new HashMap<>());
            filters.get(currentNetwork).put("score", new HashMap<>());
        }
        if (!colors.containsKey(currentNetwork)) {
            colors.put(currentNetwork, new HashMap<>());
        }
        updateScore();
    }

    public void selectedEdges(Collection<CyEdge> edges) {
    }
}
