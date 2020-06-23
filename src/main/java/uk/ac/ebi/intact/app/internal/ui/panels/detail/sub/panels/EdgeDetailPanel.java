package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels;

import com.google.common.collect.Comparators;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.CollapsedEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.spinner.LoadingSpinner;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.AbstractDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements.EdgeBasics;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements.EdgeDetails;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.elements.EdgeParticipants;
import uk.ac.ebi.intact.app.internal.ui.panels.filters.FilterPanel;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Comparator.comparing;


public class EdgeDetailPanel extends AbstractDetailPanel {
    private final EasyGBC layoutHelper = new EasyGBC();
    private JPanel edgesPanel;
    private CollapsablePanel selectedEdges;
    private static final int MAXIMUM_SELECTED_EDGE_SHOWN = 100;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);

    private final Map<Edge, EdgePanel> edgeToPanel = new HashMap<>();
    private final JPanel mainPanel = new JPanel(new GridBagLayout());
    private final JPanel filtersPanel = new JPanel(new GridBagLayout());
    private final EasyGBC filterHelper = new EasyGBC();
    private final Map<Class<? extends Filter>, FilterPanel> filterPanels = new HashMap<>();

    public EdgeDetailPanel(final Manager manager) {
        super(manager, MAXIMUM_SELECTED_EDGE_SHOWN, "edges");
        init();
        revalidate();
        repaint();
    }

    private void init() {
        setBackground(backgroundColor);
        setLayout(new GridBagLayout());
//        createScorePanel();
        createScrollablePanel();
    }

    public void setupFilters(List<Filter<? extends Edge>> edgeFilters) {
        for (Filter<? extends Edge> filter : edgeFilters) {
            if (!filterPanels.containsKey(filter.getClass())) {
                FilterPanel<?> filterPanel = FilterPanel.createFilterPanel(filter);
                if (filterPanel != null) {
                    filtersPanel.add(filterPanel, filterHelper.down().expandHoriz());
                    filterPanels.put(filter.getClass(), filterPanel);
                }
            } else {
                filterPanels.get(filter.getClass()).setFilter(filter);
            }
        }
        hideDisabledFilters();
    }

    public void hideDisabledFilters() {
        for (FilterPanel<?> filterPanel : filterPanels.values()) {
            filterPanel.setVisible(filterPanel.getFilter().isEnabled());
        }
    }

    private void createScrollablePanel() {
        mainPanel.setBackground(backgroundColor);
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane, layoutHelper.down().anchor("west").expandBoth());
        CollapsablePanel filtersCP = new CollapsablePanel("Filters", filtersPanel, false);
        filtersCP.setBackground(backgroundColor);
        mainPanel.add(filtersCP, layoutHelper.anchor("northwest").down().expandHoriz());
//        mainPanel.add(Box.createHorizontalGlue(), layoutHelper.anchor("west").right().right().expandHoriz());
        mainPanel.add(createEdgesPanel(), layoutHelper.anchor("north").down().expandHoriz());
        mainPanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
    }

    private JPanel createEdgesPanel() {
        edgesPanel = new JPanel(new GridBagLayout());
        edgesPanel.setBackground(backgroundColor);
        edgesPanel.add(loadingSpinner, layoutHelper.anchor("west").noExpand());
        selectedEdges(CyTableUtil.getEdgesInState(currentINetwork.getCyNetwork(), CyNetwork.SELECTED, true));

        selectedEdges = new CollapsablePanel("Selected edges info", edgesPanel, false);
        return selectedEdges;
    }


    public void networkChanged(Network newNetwork) {
        this.currentINetwork = newNetwork;
        selectedEdges(newNetwork.getSelectedEdges());
    }

    public volatile boolean selectionRunning;
    private final LoadingSpinner loadingSpinner = new LoadingSpinner();

    public void selectedEdges(Collection<CyEdge> edges) {
        if (checkCurrentNetwork() && checkCurrentView()) {
            // Clear the nodes panel
            selectionRunning = true;
            loadingSpinner.start();

            List<Edge> iEdges = edges.stream()
                    .map(edge -> Edge.createIntactEdge(currentINetwork, edge))
                    .filter(this::isEdgeOfCurrentViewType)
                    .collect(Comparators.greatest(MAXIMUM_SELECTED_EDGE_SHOWN, comparing(o -> o.miScore)));


            for (Edge iEdge : iEdges) {
                if (!selectionRunning)
                    break;

                if (!edgeToPanel.containsKey(iEdge)) {
                    EdgePanel edgePanel = new EdgePanel(iEdge);
                    edgePanel.setAlignmentX(LEFT_ALIGNMENT);
                    edgesPanel.add(edgePanel, layoutHelper.anchor("west").down().expandHoriz());
                    edgeToPanel.put(iEdge, edgePanel);
                }

            }

            if (iEdges.size() < MAXIMUM_SELECTED_EDGE_SHOWN) {
                edgesPanel.remove(limitExceededPanel);
            } else {
                edgesPanel.add(limitExceededPanel, layoutHelper.expandHoriz().down());
            }

            HashSet<Edge> unselectedEdges = new HashSet<>(edgeToPanel.keySet());
            unselectedEdges.removeAll(iEdges);
            for (Edge unselectedEdge : unselectedEdges) {
                EdgePanel edgePanel = edgeToPanel.get(unselectedEdge);
                edgePanel.delete();
                edgesPanel.remove(edgePanel);
                edgeToPanel.remove(unselectedEdge);
            }
            EdgeParticipants.homogenizeNodeDiagramWidth();
            loadingSpinner.stop();
            selectionRunning = false;
        }
    }

    private boolean isEdgeOfCurrentViewType(Edge edge) {
        return (currentIView.getType() == NetworkView.Type.COLLAPSED && edge.collapsed) ||
                (currentIView.getType() != NetworkView.Type.COLLAPSED && !edge.collapsed);
    }


    public void networkViewChanged(CyNetworkView view) {
        currentIView = manager.data.getNetworkView(view);
    }

    public void viewTypeChanged() {
        executor.execute(() -> {
                    hideDisabledFilters();
                    TimeUtils.sleep(1000);
                    selectedEdges(currentINetwork.getSelectedEdges());
                }
        );
    }


    private class EdgePanel extends CollapsablePanel {

        private final EdgeParticipants edgeParticipants;

        public EdgePanel(Edge edge) {
            super("", !(selectedEdges == null || selectedEdges.collapseAllButton.isExpanded()));
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(backgroundColor);

            JLabel title;
            if (edge instanceof CollapsedEdge) {
                title = new JLabel("Collapsed edge between " + edge.source.name + " and " + edge.target.name + " (MI Score = " + edge.miScore + ")");
            } else {
                EvidenceEdge iEEdge = (EvidenceEdge) edge;
                title = new JLabel("Evidence of " + iEEdge.type + " between " + iEEdge.source.name + " and " + iEEdge.target.name);
            }

            setHeader(title);

            content.add(new EdgeBasics(edge, openBrowser));
            content.add(new EdgeDetails(edge, openBrowser));
            edgeParticipants = new EdgeParticipants(edge, openBrowser);
            content.add(edgeParticipants);
        }

        public void delete() {
            edgeParticipants.delete();
        }
    }
}
