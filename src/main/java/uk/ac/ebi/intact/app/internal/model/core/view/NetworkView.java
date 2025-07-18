package uk.ac.ebi.intact.app.internal.model.core.view;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;

import org.cytoscape.group.CyGroup;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

import uk.ac.ebi.intact.app.internal.model.core.elements.edges.EvidenceEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.filters.edge.*;
import uk.ac.ebi.intact.app.internal.model.filters.node.OrthologyGroupingDatabaseFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeSpeciesFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeTypeFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.OrphanNodeFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;
import uk.ac.ebi.intact.app.internal.tasks.query.QueryFilters;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NetworkView implements FilterUpdatedListener {
    private transient Thread thread;
    public final transient Manager manager;
    private final transient Network network;
    @Getter
    public final transient CyNetworkView cyView;
    private final List<Filter<?>> filters = new ArrayList<>();
    private boolean filtersSilenced = false;
    private boolean listeningToFilterUpdate = true;
    private Type type;
    @JsonIgnore
    private final HideTaskFactory hideTaskFactory;
    @JsonIgnore
    private final UnHideTaskFactory unHideTaskFactory;

    public NetworkView(Manager manager, CyNetworkView cyView, QueryFilters queryFilters, Type type) {
        this(manager, cyView, type);
        if (cyView != null) {
            setupFiltersWithParams(queryFilters);
        }
    }

    public NetworkView(Manager manager, CyNetworkView cyView, boolean loadData, Type type) {
        this(manager, cyView, type);
        if (cyView != null) {
            setupFiltersAndLoadData(loadData);
        }
    }

    private NetworkView(Manager manager, CyNetworkView cyView, Type type) {
        this.manager = manager;
        this.manager.utils.registerAllServices(this, new Properties());
        hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.utils.getService(UnHideTaskFactory.class);
        if (cyView != null) {
            this.cyView = cyView;
            this.network = manager.data.getNetwork(cyView.getModel());
            this.type = type != null ? type : Type.SUMMARY;
        } else {
            this.cyView = null;
            this.network = null;
        }
    }

    public void accordStyleToType() {
        manager.style.getStyle(type).applyStyle(cyView);
    }

    public List<Filter<?>> getFilters() {
        return new ArrayList<>(filters);
    }

    private void setupFiltersAndLoadData(boolean loadData) {
        setupFilters(null);
        if (loadData) load();
        totalFilter();
    }

    private void setupFiltersWithParams(QueryFilters queryFilters) {
        setupFilters(queryFilters);
        totalFilter();
    }

    private void setupFilters(QueryFilters queryFilters) {
        filters.add(new NodeTypeFilter(this, queryFilters));
        filters.add(new NodeSpeciesFilter(this, queryFilters));
        filters.add(new OrphanEdgeFilter(this)); // Run after node filters to make OrphanNode take into account node filters

        filters.add(new EdgeMIScoreFilter(this, queryFilters));
        filters.add(new EdgeInteractionDetectionMethodFilter(this, queryFilters));
        filters.add(new EdgeParticipantDetectionMethodFilter(this, queryFilters));
        filters.add(new EdgeHostOrganismFilter(this, queryFilters));
        filters.add(new EdgeExpansionTypeFilter(this, queryFilters));
        filters.add(new EdgeTypeFilter(this, queryFilters));
        filters.add(new EdgeMutationFilter(this, queryFilters));
        filters.add(new EdgePositiveFilter(this, queryFilters));

        filters.add(new OrphanNodeFilter(this)); // Must be after edge filters

        filters.add(new OrthologyGroupingDatabaseFilter(this));
    }

    public void resetFilters() {
        this.listeningToFilterUpdate = false;
        filters.forEach(Filter::reset);
        save();
        filter();
        this.listeningToFilterUpdate = true;
    }

    public void totalFilter() {
        Network network = getNetwork();
        (this.type != Type.SUMMARY ? network.getSummaryCyEdges() : network.getEvidenceCyEdges()).forEach(cyEdge -> {
            if (cyEdge == null) return;
            View<CyEdge> edgeView = cyView.getEdgeView(cyEdge);
            if (edgeView == null) return;
            edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
        });
        filter();
    }

    public void filter() {
        if (filtersSilenced) return;
        getNetwork().getVisibleNodes().clear();
        getNetwork().getVisibleEvidenceEdges().clear();
        getNetwork().getVisibleSummaryEdges().clear();

        Network network = getNetwork();

        getNetwork().getVisibleNodes().addAll(network.getNodes());
        getNetwork().getVisibleEvidenceEdges().addAll(network.getEvidenceEdges());
        getNetwork().getVisibleSummaryEdges().addAll(network.getSummaryEdges());

        for (Filter<?> filter : filters) {
            filter.filterView();
        }

        List<Node> nodesToHide = network.getNodes();
        nodesToHide.removeAll(getNetwork().getVisibleNodes());
        nodesToHide.forEach(nodeToHide -> {
            View<CyNode> nodeView = cyView.getNodeView(nodeToHide.cyNode);
            if (nodeView != null) nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
        });

        Set<Long> visibleNodeIds = new HashSet<>();
        getNetwork().getVisibleNodes().forEach(nodeToShow -> {
            visibleNodeIds.add(nodeToShow.cyNode.getSUID());
            View<CyNode> nodeView = cyView.getNodeView(nodeToShow.cyNode);
            if (nodeView != null) nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
        });

        // Hide or show node groups based on the visible nodes
        getNetwork().getCyNetwork().getNodeList().forEach(node -> {
            if (getNetwork().getGroupManager().isGroup(node, getNetwork().getCyNetwork())) {
                CyGroup group = getNetwork().getGroupManager().getGroup(node, getNetwork().getCyNetwork());
                View<CyNode> nodeView = cyView.getNodeView(node);
                if (nodeView != null) {
                    if (group.getNodeList().stream().anyMatch(subNode -> visibleNodeIds.contains(subNode.getSUID()))) {
                        nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
                    } else {
                        nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
                    }
                }
            }
        });

        List<EvidenceEdge> evidenceEdgesToHide = network.getEvidenceEdges();
        evidenceEdgesToHide.removeAll(getNetwork().getVisibleEvidenceEdges());

        List<SummaryEdge> summaryEdgesToHide = network.getSummaryEdges();
        summaryEdgesToHide.removeAll(getNetwork().getVisibleSummaryEdges());

        network.getSummaryEdges().forEach(SummaryEdge::updateSummary);
        save();

        Collection<CyEdge> visibleEdges = new HashSet<>();
        Collection<CyEdge> cyEdgesToHide = new HashSet<>();
        if (type == Type.SUMMARY) {
            visibleEdges.addAll(getNetwork().getVisibleSummaryCyEdges());
            cyEdgesToHide.addAll(getNetwork().getEvidenceCyEdges());
            summaryEdgesToHide.forEach(summaryEdge -> cyEdgesToHide.add(summaryEdge.cyEdge));
        } else {
            visibleEdges.addAll(getNetwork().getVisibleEvidenceCyEdges());
            cyEdgesToHide.addAll(getNetwork().getSummaryCyEdges());
            evidenceEdgesToHide.forEach(summaryEdge -> cyEdgesToHide.add(summaryEdge.cyEdge));
        }
        manager.utils.execute(hideTaskFactory.createTaskIterator(cyView, null, cyEdgesToHide));
        manager.utils.execute(unHideTaskFactory.createTaskIterator(cyView, null, visibleEdges));
        manager.utils.fireEvent(new ViewUpdatedEvent(manager, this));
    }

    public Set<String> getPropertyValuesOfFilter(Class<? extends DiscreteFilter<?>> filterClass) {
        for (Filter<?> filter : filters) {
            if (filterClass == filter.getClass()) {
                return ((DiscreteFilter<?>) filter).getPropertiesLabels();
            }
        }
        return null;
    }

    public void silenceFilters(boolean filtersSilenced) {
        this.filtersSilenced = filtersSilenced;
    }

    public void save() {
        if (thread != null && thread.isAlive()) thread.interrupt();
        thread = new Thread(() -> {
            try {
                ObjectMapper om = new ObjectMapper();
                om.setVisibility(om.getSerializationConfig().
                        getDefaultVisibilityChecker().
                        withFieldVisibility(JsonAutoDetect.Visibility.ANY).
                        withGetterVisibility(JsonAutoDetect.Visibility.NONE));
                CyNetwork cyNetwork = getNetwork().getCyNetwork();
                NetworkFields.VIEW_STATE.setValue(cyNetwork.getRow(cyNetwork), om.writeValueAsString(this));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void load() {
        CyNetwork cyNetwork = getNetwork().getCyNetwork();
        String jsonText = NetworkFields.VIEW_STATE.getValue(cyNetwork.getRow(cyNetwork));
        if (jsonText == null || jsonText.isBlank()) return;
        try {
            JsonNode json = new ObjectMapper().readTree(jsonText);
            String type = json.get("type").textValue();
            this.type = type != null && !type.isEmpty() ? Type.valueOf(type) : Type.SUMMARY;

            List<JsonNode> filterDataList = StreamSupport.stream(json.get("filters").spliterator(), false).collect(Collectors.toList());
            for (Filter<?> filter : filters) {
                for (Iterator<JsonNode> iterator = filterDataList.iterator(); iterator.hasNext(); ) {
                    JsonNode filterJson = iterator.next();
                    if (filter.load(filterJson)) {
                        iterator.remove();
                        break;
                    }
                }
            }
        } catch (JsonProcessingException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return "View of " + getNetwork().toString();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        filter();
        save();
    }

    @Override
    public void handleEvent(FilterUpdatedEvent event) {
        if (listeningToFilterUpdate && event.getFilter().getNetworkView() == this) {
            filter();
        }
    }

    public enum Type {
        SUMMARY("SUMMARY", "IntAct - Summary"),
        EVIDENCE("EVIDENCE", "IntAct - Evidence"),
        MUTATION("MUTATION", "IntAct - Mutation");

        private final String name;
        public final String styleName;

        Type(String name, String styleName) {
            this.name = name;
            this.styleName = styleName;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public Network getNetwork() {
        return Objects.requireNonNull(network);
    }
}
