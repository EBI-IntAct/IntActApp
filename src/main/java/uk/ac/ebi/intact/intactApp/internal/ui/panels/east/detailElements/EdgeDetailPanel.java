package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detailElements;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.MIScoreSliderUI;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeChangeEvent;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeChangeListener;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeSlider;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;


public class EdgeDetailPanel extends AbstractDetailPanel implements RangeChangeListener {
    JPanel scorePanel;

    private static Color bg = new Color(229, 229, 229);
    public static RangeSlider scoreSlider;

    public EdgeDetailPanel(final IntactManager manager) {
        super(manager);

        setBackground(new Color(255, 255, 255, 0));
        init();
        revalidate();
        repaint();
    }

    private void init() {
        setLayout(new GridBagLayout());
        {
            EasyGBC c = new EasyGBC();
            {
                scoreSlider = new RangeSlider(0, 100);
                scoreSlider.setUI(new MIScoreSliderUI(scoreSlider));
                scoreSlider.setForeground(Color.LIGHT_GRAY);
                scoreSlider.setValue(0);
                scoreSlider.setUpperValue(100);
                scoreSlider.addRangeChangeListener(this);

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


    protected void doFilter(String type) {
    }


    public void networkChanged(IntactNetwork newNetwork) {
        this.currentINetwork = newNetwork;
    }

    public void selectedEdges(Collection<CyEdge> edges) {
    }

    public void networkViewChanged(CyNetworkView view) {
        if (currentIView != null) {
            scoreSlider.removeRangeChangeListener(currentIView);
        }
        scoreSlider.silentRangeChangeEvents();
        currentIView = manager.getIntactNetworkView(view);
        scoreSlider.addRangeChangeListener(currentIView);

        IntactNetworkView.Range miScoreRange = currentIView.getMiScoreRange();
        scoreSlider.setValue(miScoreRange.lowerValue);
        scoreSlider.setUpperValue(miScoreRange.upperValue);
        scoreSlider.enableRangeChangeEvents();
    }


    @Override
    public void rangeChanged(RangeChangeEvent event) {
        RangeSlider slider = event.getRangeSlider();
        double lower = slider.getValue() / 100d;
        double upper = slider.getUpperValue() / 100d;
        Set<CyEdge> hiddenEdges = new HashSet<>();

        String namespace;
        List<CyEdge> toFilter;
        if (currentIView.getType() == IntactNetworkView.Type.COLLAPSED) {
            namespace = ModelUtils.COLLAPSED_NAMESPACE;
            toFilter = currentINetwork.getCollapsedEdges();
            hiddenEdges.addAll(currentINetwork.getExpandedEdges());
        } else {
            namespace = ModelUtils.INTACTDB_NAMESPACE;
            toFilter = currentINetwork.getExpandedEdges();
            hiddenEdges.addAll(currentINetwork.getCollapsedEdges());
        }

        for (CyEdge edge : toFilter) {
            View<CyEdge> edgeView = currentIView.getView().getEdgeView(edge);
            CyRow edgeRow = currentINetwork.getNetwork().getRow(edge);

            double score = edgeRow.get(namespace, "mi score", Double.class);

            if (score > lower && score < upper) {
                hiddenEdges.remove(edge);
                for (CyNode node : new CyNode[]{edge.getSource(), edge.getTarget()}) {
                    currentIView.getView().getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                }
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
            } else {
                hiddenEdges.add(edge);
                edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
                for (CyNode node : new CyNode[]{edge.getSource(), edge.getTarget()}) {
                    if (hiddenEdges.containsAll(currentINetwork.getNetwork().getAdjacentEdgeList(node, CyEdge.Type.ANY))) {
                        currentIView.getView().getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
                    }
                }
            }
        }
    }
}
