package uk.ac.ebi.intact.intactApp.internal.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.model.core.edges.IntactEdge;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewChangedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.filters.Filter;
import uk.ac.ebi.intact.intactApp.internal.model.filters.edge.*;
import uk.ac.ebi.intact.intactApp.internal.model.filters.node.NodeSpeciesFilter;
import uk.ac.ebi.intact.intactApp.internal.model.filters.node.NodeTypeFilter;
import uk.ac.ebi.intact.intactApp.internal.model.filters.node.OrphanNodeFilter;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class IntactNetworkView {
    private Thread thread;
    public final transient IntactManager manager;
    public final transient IntactNetwork network;
    public final transient CyNetworkView view;
    private Type type = Type.COLLAPSED;
    public final transient Set<IntactNode> visibleNodes = new HashSet<>();
    public final transient Set<IntactEdge> visibleEdges = new HashSet<>();
    private final List<Filter<?>> filters = new ArrayList<>();
    private boolean filtersSilenced = false;

    public IntactNetworkView(IntactManager manager, CyNetworkView view, boolean loadData) {
        this.manager = manager;
        if (view != null) {
            this.view = view;
            this.network = manager.data.getIntactNetwork(view.getModel());
            setupFilters(loadData);
        } else {
            this.view = null;
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
                CyNetwork network = this.network.getNetwork();
                network.getRow(network).set(ModelUtils.NET_VIEW_STATE, objectMapper.writeValueAsString(this));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void load() {
        CyNetwork network = this.network.getNetwork();
        String jsonText = network.getRow(network).get(ModelUtils.NET_VIEW_STATE, String.class);
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

        List<IntactNode> nodesToFilter = network.getINodes();
        List<? extends IntactEdge> edgesToFilter = (getType() == Type.COLLAPSED) ? network.getCollapsedIEdges() : network.getEvidenceIEdges();

        visibleNodes.addAll(nodesToFilter);
        visibleEdges.addAll(edgesToFilter);

        for (Filter<?> filter : filters) {
            filter.filterView();
        }

        nodesToFilter.removeAll(visibleNodes);
        nodesToFilter.forEach(nodeToHide -> view.getNodeView(nodeToHide.node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false));
        visibleNodes.forEach(nodeToHide -> view.getNodeView(nodeToHide.node).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, true));

        edgesToFilter.removeAll(visibleEdges);
        edgesToFilter.forEach(edgeToHide -> view.getEdgeView(edgeToHide.edge).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, false));
        visibleEdges.forEach(edgeToHide -> view.getEdgeView(edgeToHide.edge).setVisualProperty(BasicVisualLexicon.EDGE_VISIBLE, true));

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
