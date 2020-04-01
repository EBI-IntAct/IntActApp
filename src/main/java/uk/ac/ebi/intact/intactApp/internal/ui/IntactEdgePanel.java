package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactViewType;
import uk.ac.ebi.intact.intactApp.internal.ui.range.slider.basic.MIScoreSliderUI;
import uk.ac.ebi.intact.intactApp.internal.ui.range.slider.basic.RangeSlider;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.List;
import java.util.*;


public class IntactEdgePanel extends AbstractIntactPanel implements ChangeListener {
    JPanel scorePanel;
    private Map<CyNetwork, Map<String, Boolean>> colors;
    private static Color bg = new Color(229, 229, 229);

    public IntactEdgePanel(final IntactManager manager) {
        super(manager);
        filters.get(currentNetwork).put("mi score", new HashMap<>());
        setBackground(new Color(255, 255, 255, 0));
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
            {
                RangeSlider scoreSlider = new RangeSlider(0, 100);
                scoreSlider.setUI(new MIScoreSliderUI(scoreSlider));
                scoreSlider.setForeground(Color.LIGHT_GRAY);
                scoreSlider.setValue(0);
                scoreSlider.setUpperValue(100);
                scoreSlider.addChangeListener(this);

                scorePanel = new JPanel(new GridBagLayout());
                scorePanel.setBackground(bg);
                EasyGBC d = new EasyGBC();
                JLabel label = new JLabel("MI Score");

                scoreSlider.setBackground(bg);
                scorePanel.add(label, d.anchor("west").noExpand());
                scorePanel.add(scoreSlider, d.right().anchor("west").expandHoriz());
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
                Double v = edgeRow.get(namespace, type, Double.class);
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


    private void updateScore() {
        scorePanel.removeAll();
        init();
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


    @Override
    public void stateChanged(ChangeEvent e) {
        RangeSlider slider = (RangeSlider) e.getSource();
        double lower = slider.getValue() / 100d;
        double upper = slider.getUpperValue() / 100d;
        CyNetworkView view = manager.getCurrentNetworkView();
        Set<CyEdge> hiddenEdges = new HashSet<>();

        String namespace;
        List<CyEdge> toFilter;
        if (manager.getNetworkViewType(view) == IntactViewType.COLLAPSED) {
            namespace = ModelUtils.COLLAPSED_NAMESPACE;
            toFilter = manager.getIntactNetwork(currentNetwork).getCollapsedEdges();
            hiddenEdges.addAll(manager.getIntactNetwork(currentNetwork).getExpandedEdges());
        } else {
            namespace = ModelUtils.INTACTDB_NAMESPACE;
            toFilter = manager.getIntactNetwork(currentNetwork).getExpandedEdges();
            hiddenEdges.addAll(manager.getIntactNetwork(currentNetwork).getCollapsedEdges());
        }

        for (CyEdge edge : toFilter) {
            View<CyEdge> edgeView = view.getEdgeView(edge);
            CyRow edgeRow = currentNetwork.getRow(edge);

            double score = edgeRow.get(namespace, "mi score", Double.class);

            if (score > lower && score < upper) {
                hiddenEdges.remove(edge);
                for (CyNode node : new CyNode[]{edge.getSource(), edge.getTarget()}) {
                    view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                }
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
            } else {
                hiddenEdges.add(edge);
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
                for (CyNode node : new CyNode[]{edge.getSource(), edge.getTarget()}) {
                    if (hiddenEdges.containsAll(currentNetwork.getAdjacentEdgeList(node, CyEdge.Type.ANY))) {
                        view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
                    }
                }
            }
        }
    }
}
