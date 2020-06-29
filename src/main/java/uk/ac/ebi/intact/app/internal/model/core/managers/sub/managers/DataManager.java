package uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.*;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewChangedEvent;
import uk.ac.ebi.intact.app.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.app.internal.utils.tables.ModelUtils;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.EdgeFields;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.FeatureFields;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.IdentifierFields;
import uk.ac.ebi.intact.app.internal.utils.tables.fields.models.NetworkFields;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.utils.tables.ModelUtils.*;

public class DataManager implements
        SessionLoadedListener,
        NetworkAddedListener,
        NetworkViewAddedListener,
        NetworkAboutToBeDestroyedListener,
        NetworkViewAboutToBeDestroyedListener {
    private final Manager manager;
    final CyRootNetworkManager rootNetworkManager;
    final List<IntactNetworkCreatedListener> intactNetworkCreatedListeners = new ArrayList<>();
    final List<IntactViewTypeChangedListener> intactViewTypeChangedListeners = new ArrayList<>();
    final Map<CyNetwork, Network> networkMap;
    final Map<CyNetworkView, NetworkView> intactNetworkViewMap;

    public DataManager(Manager manager) {
        this.manager = manager;
        networkMap = new HashMap<>();
        intactNetworkViewMap = new HashMap<>();
        rootNetworkManager = manager.utils.getService(CyRootNetworkManager.class);
    }

    public void loadCurrentSession() {
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        for (CyNetwork cyNetwork : manager.utils.getService(CyNetworkManager.class).getNetworkSet()) {
            if (!isIntactNetwork(cyNetwork)) continue;
            Network network = new Network(manager);
            addIntactNetwork(network, cyNetwork);
            fireIntactNetworkCreated(network);
            linkNetworkTablesFromTableData(network);
            network.completeMissingNodeColorsFromTables();
            for (CyNetworkView view : networkViewManager.getNetworkViews(cyNetwork)) {
                addNetworkView(view, true);
            }
        }
    }


    public CyNetwork createNetwork(String name) {
        CyNetwork cyNetwork = manager.utils.getService(CyNetworkFactory.class).createNetwork();
        CyNetworkManager netMgr = manager.utils.getService(CyNetworkManager.class);

        Set<CyNetwork> nets = netMgr.getNetworkSet();
        Set<CyNetwork> allNets = new HashSet<>(nets);
        for (CyNetwork net : nets) {
            allNets.add(((CySubNetwork) net).getRootNetwork());
        }
        // See if this name is already taken by a network or a network collection (root network)
        int index = -1;
        boolean match = false;
        for (CyNetwork net : allNets) {
            String netName = net.getRow(net).get(CyNetwork.NAME, String.class);
            if (netName.equals(name)) {
                match = true;
            } else if (netName.startsWith(name)) {
                String subname = netName.substring(name.length());
                if (subname.startsWith(" - ")) {
                    try {
                        int v = Integer.parseInt(subname.substring(3));
                        if (v >= index)
                            index = v + 1;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        if (match && index < 0) {
            name = name + " - 1";
        } else if (index > 0) {
            name = name + " - " + index;
        }
        cyNetwork.getRow(cyNetwork).set(CyNetwork.NAME, name);

        return cyNetwork;
    }

    public void addIntactNetwork(Network network, CyNetwork cyNetwork) {
        networkMap.put(cyNetwork, network);
        network.setNetwork(cyNetwork);
    }

    public String getNetworkName(CyNetwork net) {
        return net.getRow(net).get(CyNetwork.NAME, String.class);
    }

    public CyNetworkView createNetworkView(CyNetwork cyNetwork) {
        CyNetworkView view = manager.utils.getService(CyNetworkViewFactory.class)
                .createNetworkView(cyNetwork);
        if (networkMap.containsKey(cyNetwork)) {
            networkMap.get(cyNetwork).hideExpandedEdgesOnViewCreation(view);
            manager.style.intactStyles.get(CollapsedIntactStyle.type).applyStyle(view);
        }
        return view;
    }

    public void addNetwork(CyNetwork cyNetwork) {
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        if (!networkManager.networkExists(cyNetwork.getSUID())) {
            networkManager.addNetwork(cyNetwork);
        }
        manager.utils.getService(CyApplicationManager.class).setCurrentNetwork(cyNetwork);
    }

    public NetworkView addNetworkView(CyNetworkView cyView, boolean loadData) {
        NetworkView view = new NetworkView(manager, cyView, loadData);
        intactNetworkViewMap.put(cyView, view);
        return view;
    }

    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
        CyNetwork cyNetwork = e.getNetwork();
        // delete enrichment tables
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        Network network = networkMap.get(cyNetwork);
        if (network != null) {
            tableManager.deleteTable(network.getFeaturesTable().getSUID());
            tableManager.deleteTable(network.getIdentifiersTable().getSUID());
            // remove as string network
            networkMap.remove(cyNetwork);

        }
    }

    @Override
    public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
        intactNetworkViewMap.remove(e.getNetworkView());
    }

    @Override
    public void handleEvent(NetworkAddedEvent e) {
        CyNetwork newNetwork = e.getNetwork();
        CyRootNetwork rootNetwork = rootNetworkManager.getRootNetwork(newNetwork);
        CySubNetwork baseNetwork = rootNetwork.getBaseNetwork();
        if (baseNetwork.getSUID().equals(newNetwork.getSUID())) return;
        addSubNetwork(newNetwork, baseNetwork);
    }

    private void addSubNetwork(CyNetwork newNetwork, CySubNetwork baseNetwork) {
        if (networkMap.containsKey(baseNetwork)) {
            Network parent = networkMap.get(baseNetwork);
            Network newINetwork = new Network(manager);
            addIntactNetwork(newINetwork, newNetwork);
            ModelUtils.buildSubTablesForSubIntactNetwork(newINetwork, parent);
        }
    }

    @Override
    public void handleEvent(NetworkViewAddedEvent e) {
        if (networkMap.containsKey(e.getNetworkView().getModel())) {
            addNetworkView(e.getNetworkView(), false);
        }
    }

    @Override
    public void handleEvent(SessionLoadedEvent event) {
        // Create string networks for any networks loaded by string
        networkMap.clear();
        intactNetworkViewMap.clear();
        Set<CyNetwork> cyNetworks = event.getLoadedSession().getNetworks();
        List<Network> networks = new ArrayList<>();
        for (CyNetwork cyNetwork : cyNetworks) {
            if (ModelUtils.isIntactNetwork(cyNetwork)) {
                if (ModelUtils.ifHaveIntactNS(cyNetwork)) {
                    Network network = new Network(manager);
                    addIntactNetwork(network, cyNetwork);
                    network.completeMissingNodeColorsFromTables();
                    networks.add(network);
                }
            }
        }

        linkIntactTablesToNetwork(event.getLoadedSession().getTables(), getEdgeTableMapping(networks));

        for (CyNetworkView view : event.getLoadedSession().getNetworkViews()) {
            if (ModelUtils.isIntactNetwork(view.getModel())) {
                addNetworkView(view, true);
            }
        }

        NetworkView currentView = getCurrentIntactNetworkView();
        if (currentView != null) {
            fireIntactViewChangedEvent(new IntactViewChangedEvent(manager, currentView));
            manager.utils.showResultsPanel();
        } else {
            manager.utils.hideResultsPanel();
        }
    }

    Map<CyNetwork, Map<Long, Long>> getEdgeTableMapping(List<Network> networks) {
        Map<CyNetwork, Map<Long, Long>> edgeMapping = new HashMap<>();
        for (Network network : networks) {
            Map<Long, Long> networkEdgeMapping = new HashMap<>();
            CyNetwork cyNetwork = network.getCyNetwork();
            edgeMapping.put(cyNetwork, networkEdgeMapping);
            for (CyRow edgeRow : cyNetwork.getDefaultEdgeTable().getAllRows()) {
                Long id = EdgeFields.ID.getValue(edgeRow);
                if (id == null) continue;
                Long suid = edgeRow.get(CyEdge.SUID, Long.class);
                networkEdgeMapping.put(id, suid);

            }
        }
        return edgeMapping;
    }

    void linkCollapsedEdgesIdsToSUIDs(CyTableMetadata tableM, Map<CyNetwork, Map<Long, Long>> edgeMapping) {
        CyTable edgeTable = tableM.getTable();
        CyColumn collapsedIntactIdsColumn = EdgeFields.C_INTACT_IDS.getColumn(edgeTable);
        if (collapsedIntactIdsColumn == null) return;

        Map<Long, Long> networkEdgeMapping = edgeMapping.get(rootNetworkManager.getRootNetwork(tableM.getNetwork()).getSubNetworkList().get(0));
        if (networkEdgeMapping == null) return;

        for (CyRow row : edgeTable.getAllRows()) {
            List<Long> ids = EdgeFields.C_INTACT_IDS.getValue(row);
            if (ids == null) continue;
            List<Long> list = ids.stream().filter(Objects::nonNull).map(networkEdgeMapping::get).collect(Collectors.toList());
            EdgeFields.C_INTACT_SUIDS.setValue(row, list);
        }
    }


    void linkIntactTablesToNetwork(Collection<CyTableMetadata> tables, Map<CyNetwork, Map<Long, Long>> edgeMapping) {
        for (CyTableMetadata tableM : tables) {
            linkCollapsedEdgesIdsToSUIDs(tableM, edgeMapping);
            CyTable table = tableM.getTable();
            CyColumn networkUUIDColumn = NetworkFields.UUID.getColumn(table);
            if (networkUUIDColumn == null) continue;

            List<String> uuids = networkUUIDColumn.getValues(String.class);
            if (uuids.isEmpty()) continue;
            for (Network network : networkMap.values()) {
                CyNetwork cyNetwork = network.getCyNetwork();
                CyRow netRow = cyNetwork.getRow(cyNetwork);
                if (NetworkFields.UUID.getValue(netRow).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                    if (IdentifierFields.ID.getColumn(table) != null) {
                        network.setIdentifiersTable(table);
                    } else if (FeatureFields.EDGES_ID.getColumn(table) != null) {
                        network.setFeaturesTable(table);
                        Map<Long, Long> networkEdgeMapping = edgeMapping.get(cyNetwork);
                        for (CyRow featureRow : table.getAllRows()) {
                            List<Long> edgeIds = FeatureFields.EDGES_ID.getValue(featureRow);
                            if (edgeIds == null || edgeIds.isEmpty()) continue;
                            FeatureFields.EDGES_SUID.setValue(featureRow, edgeIds.stream().map(networkEdgeMapping::get).collect(Collectors.toList()));
                        }
                    }
                    break;
                }
            }

        }
    }

    //================= Data getters =================//
    public CyNetwork getCurrentCyNetwork() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetwork();
    }

    public Network getNetwork(CyNetwork cyNetwork) {
        if (networkMap.containsKey(cyNetwork))
            return networkMap.get(cyNetwork);
        return null;
    }

    public Network getCurrentNetwork() {
        return networkMap.get(getCurrentCyNetwork());
    }

    public Network[] getIntactNetworks() {
        return networkMap.values().toArray(Network[]::new);
    }

    public CyNetworkView getCurrentCyView() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetworkView();
    }

    public NetworkView getNetworkView(CyNetworkView view) {
        return intactNetworkViewMap.get(view);
    }

    public NetworkView getCurrentIntactNetworkView() {
        return intactNetworkViewMap.get(getCurrentCyView());
    }

    public NetworkView[] getViews() {
        return intactNetworkViewMap.values().toArray(NetworkView[]::new);
    }

    //================= IntactNetworkCreated =================//

    public void fireIntactNetworkCreated(Network network) {
        for (IntactNetworkCreatedListener listener : intactNetworkCreatedListeners) {
            listener.handleEvent(new IntactNetworkCreatedEvent(manager, network));
        }
    }

    public void addIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.add(listener);
    }

    public void removeIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.remove(listener);
    }


    //================= IntactViewTypeChanged =================//
    public void intactViewChanged(NetworkView.Type newType, NetworkView view) {
        manager.style.intactStyles.get(newType).applyStyle(view.cyView);
        view.setType(newType);
        fireIntactViewChangedEvent(new IntactViewChangedEvent(manager, view));
    }

    public void fireIntactViewChangedEvent(IntactViewChangedEvent event) {
        for (IntactViewTypeChangedListener listener : intactViewTypeChangedListeners) {
            listener.handleEvent(event);
        }
    }

    public void addIntactViewChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.add(listener);
    }

    public void removeIntactViewChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.remove(listener);
    }
}
