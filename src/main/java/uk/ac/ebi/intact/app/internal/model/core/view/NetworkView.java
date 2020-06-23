package uk.ac.ebi.intact.app.internal.model.core.view;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewChangedEvent;
import uk.ac.ebi.intact.app.internal.model.filters.Filter;
import uk.ac.ebi.intact.app.internal.model.filters.edge.*;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeSpeciesFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.NodeTypeFilter;
import uk.ac.ebi.intact.app.internal.model.filters.node.OrphanNodeFilter;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class NetworkView {
    private Thread thread;
    public final transient Manager manager;
    public final transient Network network;
    public final transient CyNetworkView cyView;
    private Type type = Type.COLLAPSED;
    public final transient Set<Node> visibleNodes = new HashSet<>();
    public final transient Set<Edge> visibleEdges = new HashSet<>();
    private final List<Filter<?>> filters = new ArrayList<>();
    private boolean filtersSilenced = false;

    public NetworkView(Manager manager, CyNetworkView cyView, boolean loadData) {
        this.manager = manager;
        if (cyView != null) {
            this.cyView = cyView;
            this.network = manager.data.getNetwork(cyView.getModel());
            setupFilters(loadData);
        } else {
            this.cyView = null;
            this.network = null;
        }
    }

    public List<Filter<?>> getFilters() {
        return new ArrayList<>(filters);
    }

    private void setupFilters(boolean loadData) {
        filters.add(new NodeTypeFilter(this));
        filters.add(new NodeSpeciesFilter(this));

        filters.add(new EdgeMIScoreFilter(this));
        filters.add(new EdgeDetectionMethodFilter(this));
        filters.add(new EdgeHostOrganismFilter(this));
        filters.add(new EdgeExpansionTypeFilter(this));
        filters.add(new EdgeTypeFilter(this));
        filters.add(new EdgeMutationFilter(this));

        filters.add(new OrphanNodeFilter(this)); // Must be after edge filters

        if (loadData) load();

        manager.data.fireIntactViewChangedEvent(new IntactViewChangedEvent(manager, this));
    }

    public void save() {
        if (thread != null && thread.isAlive()) thread.interrupt();
        thread = new Thread(() -> {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                CyNetwork cyNetwork = this.network.getCyNetwork();
                cyNetwork.getRow(cyNetwork).set(ModelUtils.NET_VIEW_STATE, objectMapper.writeValueAsString(this));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void load() {
        CyNetwork cyNetwork = this.network.getCyNetwork();
        String jsonText = cyNetwork.getRow(cyNetwork).get(ModelUtils.NET_VIEW_STATE, String.class);
        if (jsonText == null || jsonText.isBlank()) return;
        try {
            JsonNode json = new ObjectMapper().readTree(jsonText);
            type = Type.valueOf(json.get("type").textValue());
            List<JsonNode> filterDataList = StreamSupport.stream(json.get("filters").spliterator(), false).collect(Collectors.toList());
            for (Filter<?> filter: filters) {
                for (Iterator<JsonNode> iterator = filterDataList.iterator(); iterator.hasNext(); ) {
                    JsonNode filterJson = iterator.next();
                    if (filter.load(filterJson)) {
                        iterator.remove();
                        break;
                    }
                }
            }
            filter();
        } catch (JsonProcessingException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void silenceFilters(boolean filtersSilenced) {
        this.filtersSilenced = filtersSilenced;
    }

    public void filter() {
        if (filtersSilenced) return;
        visibleNodes.clear();
        visibleEdges.clear();

        List<Node> nodesToFilter = network.getINodes();
        List<? extends Edge> edgesToFilter = (getType() == Type.COLLAPSED) ? network.getCollapsedIEdges() : network.getEvidenceIEdges();

        visibleNodes.addAll(nodesToFilter);
        visibleEdges.addAll(edgesToFilter);

        for (Filter<?> filter : filters) {
            filter.filterView();
        }

        nodesToFilter.removeAll(visibleNodes);
        nodesToFilter.forEach(nodeToHide -> cyView.getNodeView(nodeToHide.node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false));
        visibleNodes.forEach(nodeToHide -> cyView.getNodeView(nodeToHide.node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true));

        edgesToFilter.removeAll(visibleEdges);
        edgesToFilter.forEach(edgeToHide -> cyView.getEdgeView(edgeToHide.edge).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false));
        visibleEdges.forEach(edgeToHide -> cyView.getEdgeView(edgeToHide.edge).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true));

        save();
    }


    @Override
    public String toString() {
        return "View of " + network.toString();
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
        filter();
        save();
    }

    public enum Type {
        COLLAPSED("COLLAPSED"),
        EXPANDED("EXPANDED"),
        MUTATION("MUTATION");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }
}
