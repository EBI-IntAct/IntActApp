package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge;

import com.google.common.collect.Comparators;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.styles.UIColors;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.components.spinner.LoadingSpinner;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes.EdgeBasics;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes.EdgeDetails;
import uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.edge.attributes.EdgeParticipants;
import uk.ac.ebi.intact.app.internal.ui.panels.filters.FilterPanel;
import uk.ac.ebi.intact.app.internal.ui.utils.EasyGBC;
import uk.ac.ebi.intact.app.internal.ui.utils.LinkUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.Comparator.comparing;


public class EdgeDetailPanel extends AbstractDetailPanel {
    private final EasyGBC layoutHelper = new EasyGBC();
    private JPanel edgesPanel;
    private CollapsablePanel selectedEdges;
    private final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

    private final ConcurrentHashMap<Edge, EdgePanel> edgeToPanel = new ConcurrentHashMap<>();
    private final JPanel mainPanel = new JPanel(new GridBagLayout());
    private final JPanel filtersPanel = new JPanel(new GridBagLayout());
    private final EasyGBC filterHelper = new EasyGBC();
    private final Map<Class<? extends Filter>, FilterPanel> filterPanels = new HashMap<>();
    private Integer maxSelectedEdgeInfoShown;

    public EdgeDetailPanel(final Manager manager) {
        super(manager, manager.option.MAX_SELECTED_EDGE_INFO_SHOWN.getValue(), "edges");
        maxSelectedEdgeInfoShown = manager.option.MAX_SELECTED_EDGE_INFO_SHOWN.getValue();
        init();
        revalidate();
        repaint();
    }

    private void init() {
        setBackground(UIColors.lightBackground);
        setLayout(new GridBagLayout());
        createScrollablePanel();
    }

    public void setupFilters(List<Filter<? extends Edge>> edgeFilters) {
        for (Filter<? extends Edge> filter : edgeFilters) {
            if (!filterPanels.containsKey(filter.getClass())) {
                FilterPanel<?> filterPanel = FilterPanel.createFilterPanel(filter, manager);
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
        mainPanel.setBackground(UIColors.lightBackground);
        JScrollPane scrollPane = new JScrollPane(mainPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        add(scrollPane, layoutHelper.down().anchor("west").expandBoth());
        CollapsablePanel filtersCP = new CollapsablePanel("Filters", filtersPanel, false);
        filtersCP.setBackground(UIColors.lightBackground);
        mainPanel.add(filtersCP, layoutHelper.anchor("northwest").down().expandHoriz());
        mainPanel.add(createEdgesPanel(), layoutHelper.anchor("north").down().expandHoriz());
        mainPanel.add(Box.createVerticalGlue(), layoutHelper.down().expandVert());
    }

    private JPanel createEdgesPanel() {
        edgesPanel = new JPanel(new GridBagLayout());
        edgesPanel.setBackground(UIColors.lightBackground);
        edgesPanel.add(loadingSpinner, layoutHelper.anchor("west").noExpand());
        selectedEdges(CyTableUtil.getEdgesInState(currentNetwork.getCyNetwork(), CyNetwork.SELECTED, true));

        selectedEdges = new CollapsablePanel("Selected edges info", edgesPanel, false);
        return selectedEdges;
    }


    public void networkChanged(Network newNetwork) {
        this.currentNetwork = newNetwork;
        selectedEdges(newNetwork.getSelectedCyEdges());
    }

    public volatile boolean selectionRunning;
    private final LoadingSpinner loadingSpinner = new LoadingSpinner();

    public void selectedEdges(Collection<CyEdge> cyEdges) {
        if (checkCurrentNetwork() && checkCurrentView()) {
            selectionRunning = true;
            loadingSpinner.start();
            maxSelectedEdgeInfoShown = manager.option.MAX_SELECTED_EDGE_INFO_SHOWN.getValue();

            List<Edge> edges = cyEdges.stream()
                    .map(currentNetwork::getEdge)
                    .filter(Objects::nonNull)
                    .filter(this::isEdgeVisible)
                    .collect(Comparators.greatest(maxSelectedEdgeInfoShown, comparing(o -> o.miScore)));

            for (Edge edge : edges) {
                if (!selectionRunning) break;

                edgeToPanel.computeIfAbsent(edge, keyEdge -> {
                    EdgePanel edgePanel = new EdgePanel(keyEdge);
                    edgePanel.setAlignmentX(LEFT_ALIGNMENT);
                    edgesPanel.add(edgePanel, layoutHelper.anchor("west").down().expandHoriz());
                    return edgePanel;
                });

            }

            if (edges.size() < maxSelectedEdgeInfoShown) {
                edgesPanel.remove(limitExceededPanel);
            } else {
                limitExceededPanel.setLimit(maxSelectedEdgeInfoShown);
                edgesPanel.add(limitExceededPanel, layoutHelper.expandHoriz().down());
            }

            HashSet<Edge> unselectedEdges = new HashSet<>(edgeToPanel.keySet());
            unselectedEdges.removeAll(edges);
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

    private boolean isEdgeVisible(Edge edge) {
        return currentView.getType().equals(NetworkView.Type.SUMMARY)
                ? currentNetwork.getVisibleSummaryEdges().contains(edge)
                : currentNetwork.getVisibleEvidenceEdges().contains(edge);
    }


    public void networkViewChanged(CyNetworkView view) {
        currentView = manager.data.getNetworkView(view);
    }

    private Future<?> lastSelection;

    public void viewUpdated() {
        if (lastSelection != null) lastSelection.cancel(true);
        lastSelection = executor.submit(() -> {
            hideDisabledFilters();
            selectedEdges(currentNetwork.getSelectedCyEdges());
        });
    }


    private class EdgePanel extends CollapsablePanel {

        private final EdgeParticipants edgeParticipants;

        public EdgePanel(Edge edge) {
            super("", !(selectedEdges == null || selectedEdges.collapseAllButton.isExpanded()));
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            content.setAlignmentX(LEFT_ALIGNMENT);
            setBackground(UIColors.lightBackground);

            LinePanel header = new LinePanel(UIColors.lightBackground);
            if (edge instanceof SummaryEdge) {
                header.add(new JLabel("Summary edge between " + edge.source.name + " and " + edge.target.name + " (MI Score = " + edge.miScore + ")"));
            } else {
                EvidenceEdge evidenceEdge = (EvidenceEdge) edge;
                header.add(new JLabel("Evidence of "));
                header.add(LinkUtils.createCVTermLink(openBrowser, evidenceEdge.type));
                header.add(new JLabel(" between " + evidenceEdge.source.name + " and " + evidenceEdge.target.name));
            }

            setHeader(header);

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
