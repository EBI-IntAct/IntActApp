package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements;

import org.cytoscape.model.*;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.events.RangeChangeEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.RangeChangeListener;
import uk.ac.ebi.intact.intactApp.internal.ui.components.CollapsablePanel;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.MIScoreSliderUI;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeSlider;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.AbstractDetailPanel;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements.EdgeBasics;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.edge.elements.EdgeParticipants;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;
import uk.ac.ebi.intact.intactApp.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class EdgeDetailPanel extends AbstractDetailPanel implements RangeChangeListener {
    private EasyGBC layoutHelper = new EasyGBC();
    private JPanel scorePanel;
    private JPanel edgesPanel;
    public static RangeSlider scoreSlider;
    private final int MAXIMUM_SELECTED_EDGE_SHOWN = 100;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);


    private final Map<CyEdge, EdgePanel> edgeToPanel = new HashMap<>();

    public EdgeDetailPanel(final IntactManager manager) {
        super(manager);

        init();
        revalidate();
        repaint();
    }

    private void init() {
        setBackground(backgroundColor);
        setLayout(new GridBagLayout());
        createScorePanel();
        createScrollablePanel();

    }

    private void createScorePanel() {
        scoreSlider = new RangeSlider(0, 100);
        scoreSlider.setUI(new MIScoreSliderUI(scoreSlider));
        scoreSlider.setForeground(Color.LIGHT_GRAY);
        scoreSlider.setValue(0);
        scoreSlider.setUpperValue(100);
        scoreSlider.addRangeChangeListener(this);
        Color bg = new Color(229, 229, 229);
        scorePanel = new JPanel(new GridBagLayout());
        scorePanel.setBackground(bg);
        EasyGBC d = new EasyGBC();
        JLabel label = new JLabel("MI Score");

        scoreSlider.setBackground(bg);
        scorePanel.add(label, d.anchor("west").noExpand());
        scorePanel.add(scoreSlider, d.right().anchor("west").expandHoriz());
        add(scorePanel, layoutHelper.down().anchor("west").expandHoriz());
    }

    private void createScrollablePanel() {
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(backgroundColor);
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBlockIncrement(10);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane, layoutHelper.down().anchor("west").expandBoth());
        mainPanel.add(createEdgesPanel(), layoutHelper.anchor("north").down().expandHoriz());
        mainPanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
    }

    private JPanel createEdgesPanel() {
        edgesPanel = new JPanel(new GridBagLayout());
        edgesPanel.setBackground(backgroundColor);

        if (currentINetwork != null) {
            List<CyEdge> edges = CyTableUtil.getEdgesInState(currentINetwork.getNetwork(), CyNetwork.SELECTED, true);
            EasyGBC c = new EasyGBC();
            int counter = 0;
            for (CyEdge edge : edges) {
                if (counter++ > MAXIMUM_SELECTED_EDGE_SHOWN)
                    break;
                EdgePanel edgePanel = new EdgePanel(edge);
                edgePanel.setAlignmentX(LEFT_ALIGNMENT);

                edgesPanel.add(edgePanel, c.anchor("west").down().expandHoriz());
                edgeToPanel.put(edge, edgePanel);
            }
        }

        return new CollapsablePanel("Selected edges", edgesPanel, false, 10);
    }


    protected void doFilter(String type) {
    }


    public void networkChanged(IntactNetwork newNetwork) {
        this.currentINetwork = newNetwork;
        selectedEdges(newNetwork.getSelectedEdges());
    }

    public volatile boolean selectionRunning;

    public void selectedEdges(Collection<CyEdge> edges) {
        // Clear the nodes panel
        selectionRunning = true;
        int edgeCounter = 0;
        for (CyEdge edge : edges) {
            if (!selectionRunning || edgeCounter++ > MAXIMUM_SELECTED_EDGE_SHOWN) {
                break;
            }
            if (!edgeToPanel.containsKey(edge)) {
                EdgePanel edgePanel = new EdgePanel(edge);
                edgePanel.setAlignmentX(LEFT_ALIGNMENT);

                edgesPanel.add(edgePanel, layoutHelper.anchor("west").down().expandHoriz());
                edgeToPanel.put(edge, edgePanel);
            }
        }

        HashSet<CyEdge> unselectedNodes = new HashSet<>(edgeToPanel.keySet());
        unselectedNodes.removeAll(edges);
        for (CyEdge unselectedEdge : unselectedNodes) {
            EdgePanel edgePanel = edgeToPanel.get(unselectedEdge);
            edgePanel.delete();
            edgesPanel.remove(edgePanel);
            edgeToPanel.remove(unselectedEdge);
        }

        selectionRunning = false;
        revalidate();
        repaint();
    }

    private class EdgePanel extends CollapsablePanel {

        private final EdgeParticipants edgeParticipants;

        public EdgePanel(CyEdge edge) {
            super("", false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(backgroundColor);

            IntactEdge iEdge = IntactEdge.createIntactEdge(currentINetwork, edge);
            setLabel(iEdge.name);

            content.add(new EdgeBasics(iEdge, openBrowser));
            edgeParticipants = new EdgeParticipants(iEdge, openBrowser);
            content.add(edgeParticipants);
        }

        public void delete() {
            edgeParticipants.delete();
        }
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

    public void viewTypeChanged() {
        executor.execute(() -> {
                    TimeUtils.sleep(1000);
                    selectedEdges(currentINetwork.getSelectedEdges());
                }
        );
    }


    @Override
    public void rangeChanged(RangeChangeEvent event) {
        RangeSlider slider = event.getRangeSlider();
        double lower = slider.getValue() / 100d;
        double upper = slider.getUpperValue() / 100d;
        Set<CyEdge> hiddenEdges = new HashSet<>();

        List<CyEdge> toFilter;
        if (currentIView.getType() == IntactNetworkView.Type.COLLAPSED) {
            toFilter = currentINetwork.getCollapsedEdges();
            hiddenEdges.addAll(currentINetwork.getExpandedEdges());
        } else {
            toFilter = currentINetwork.getExpandedEdges();
            hiddenEdges.addAll(currentINetwork.getCollapsedEdges());
        }

        for (CyEdge edge : toFilter) {
            View<CyEdge> edgeView = currentIView.getView().getEdgeView(edge);
            CyRow edgeRow = currentINetwork.getNetwork().getRow(edge);

            double score = edgeRow.get(ModelUtils.MI_SCORE, Double.class);

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
