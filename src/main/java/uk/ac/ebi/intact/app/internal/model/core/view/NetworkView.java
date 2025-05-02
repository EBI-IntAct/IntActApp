package uk.ac.ebi.intact.app.internal.model.core.view;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.FilterUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.filters.DiscreteFilter;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.filters.edge.*;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeSpeciesFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeTypeFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.OrphanNodeFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.OrthologGroupFilter;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NetworkView implements FilterUpdatedListener {
    private transient Thread thread;
    public final transient Manager manager;
    private final transient Network network;
    public final transient CyNetworkView cyView;
    public final transient Set<Node> visibleNodes = new HashSet<>();
    public final transient Set<Edge> visibleEdges = new HashSet<>();
    private final List<Filter<?>> filters = new ArrayList<>();
    private boolean filtersSilenced = false;
    private boolean listeningToFilterUpdate = true;
    private Type type;

    public NetworkView(Manager manager, CyNetworkView cyView, boolean loadData, Type type) {
        this.manager = manager;
        this.manager.utils.registerAllServices(this, new Properties());
        if (cyView != null) {
            this.cyView = cyView;
            this.network = manager.data.getNetwork(cyView.getModel());
            this.type = type != null ? type : Type.SUMMARY;
            setupFilters(loadData);
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

    private void setupFilters(boolean loadData) {
        filters.add(new NodeTypeFilter(this));
        filters.add(new NodeSpeciesFilter(this));
        filters.add(new OrthologGroupFilter(this));

        filters.add(new EdgeMIScoreFilter(this));
        filters.add(new EdgeInteractionDetectionMethodFilter(this));
        filters.add(new EdgeParticipantDetectionMethodFilter(this));
        filters.add(new EdgeHostOrganismFilter(this));
        filters.add(new EdgeExpansionTypeFilter(this));
        filters.add(new EdgeTypeFilter(this));
        filters.add(new EdgeMutationFilter(this));
        filters.add(new EdgePositiveFilter(this));

        filters.add(new OrphanNodeFilter(this)); // Must be after edge filters
        filters.add(new OrphanEdgeFilter(this));

        if (loadData) load();
        totalFilter();
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
        visibleNodes.clear();
        visibleEdges.clear();

        Network network = getNetwork();
        List<Node> nodesToFilter = network.getNodes();
        List<? extends Edge> edgesToFilter = (getType() == Type.SUMMARY) ? network.getSummaryEdges() : network.getEvidenceEdges();

        visibleNodes.addAll(nodesToFilter);
        visibleEdges.addAll(edgesToFilter);

        for (Filter<?> filter : filters) {
            filter.filterView();
        }

        nodesToFilter.removeAll(visibleNodes);
        nodesToFilter.forEach(nodeToHide -> {
            View<CyNode> nodeView = cyView.getNodeView(nodeToHide.cyNode);
            if (nodeView != null) nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
        });
        visibleNodes.forEach(nodeToShow -> {
            View<CyNode> nodeView = cyView.getNodeView(nodeToShow.cyNode);
            if (nodeView != null) nodeView.setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true);
        });

        edgesToFilter.removeAll(visibleEdges);
        edgesToFilter.forEach(edgeToHide -> {
            View<CyEdge> edgeView = cyView.getEdgeView(edgeToHide.cyEdge);
            if (edgeView != null) edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false);
        });

        visibleEdges.forEach(edgeToShow -> {
            View<CyEdge> edgeView = cyView.getEdgeView(edgeToShow.cyEdge);
            if (edgeView != null) edgeView.setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true);
        });

        save();
        manager.utils.fireEvent(new ViewUpdatedEvent(manager, this));
    }

    public Set<String> getPropertyValuesOfFilter(Class<? extends DiscreteFilter<?>> filterClass) {
        for (Filter<?> filter : filters) {
            if (filterClass == filter.getClass()) {
                return ((DiscreteFilter<?>) filter).getProperties();
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
