package uk.ac.ebi.intact.intactApp.internal.model.managers;

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
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactNetworkCreatedListener;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedEvent;
import uk.ac.ebi.intact.intactApp.internal.model.events.IntactViewTypeChangedListener;
import uk.ac.ebi.intact.intactApp.internal.model.styles.CollapsedIntactStyle;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils.*;

public class IntactDataManager implements
        SessionLoadedListener,
        NetworkAddedListener,
        NetworkViewAddedListener,
        NetworkAboutToBeDestroyedListener,
        NetworkViewAboutToBeDestroyedListener {
    private final IntactManager manager;
    final CyRootNetworkManager rootNetworkManager;
    final List<IntactNetworkCreatedListener> intactNetworkCreatedListeners = new ArrayList<>();
    final List<IntactViewTypeChangedListener> intactViewTypeChangedListeners = new ArrayList<>();
    final Map<CyNetwork, IntactNetwork> intactNetworkMap;
    final Map<CyNetworkView, IntactNetworkView> intactNetworkViewMap;

    IntactDataManager(IntactManager manager) {
        this.manager = manager;
        intactNetworkMap = new HashMap<>();
        intactNetworkViewMap = new HashMap<>();
        rootNetworkManager = manager.utils.getService(CyRootNetworkManager.class);
    }

    public void loadCurrentSession() {
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        for (CyNetwork network : manager.utils.getService(CyNetworkManager.class).getNetworkSet()) {
            if (!isIntactNetwork(network)) continue;
            IntactNetwork intactNetwork = new IntactNetwork(manager);
            addIntactNetwork(intactNetwork, network);
            fireIntactNetworkCreated(intactNetwork);
            buildIntactNetworkTableFromExistingOne(intactNetwork);
            intactNetwork.completeMissingNodeColorsFromTables();
            for (CyNetworkView view : networkViewManager.getNetworkViews(network)) {
                addNetworkView(view);
            }
        }
    }


    public CyNetwork createNetwork(String name) {
        CyNetwork network = manager.utils.getService(CyNetworkFactory.class).createNetwork();
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
        network.getRow(network).set(CyNetwork.NAME, name);

        return network;
    }

    public void addIntactNetwork(IntactNetwork intactNetwork, CyNetwork network) {
        intactNetworkMap.put(network, intactNetwork);
        intactNetwork.setNetwork(network);
    }

    public String getNetworkName(CyNetwork net) {
        return net.getRow(net).get(CyNetwork.NAME, String.class);
    }

    public CyNetworkView createNetworkView(CyNetwork network) {
        CyNetworkView view = manager.utils.getService(CyNetworkViewFactory.class)
                .createNetworkView(network);
        if (intactNetworkMap.containsKey(network)) {
            intactNetworkMap.get(network).hideExpandedEdgesOnViewCreation(view);
            manager.style.intactStyles.get(CollapsedIntactStyle.type).applyStyle(view);
        }
        return view;
    }

    public void addNetwork(CyNetwork network) {
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        if (!networkManager.networkExists(network.getSUID())) {
            networkManager.addNetwork(network);
        }
        manager.utils.getService(CyApplicationManager.class).setCurrentNetwork(network);
    }

    public void addNetworkView(CyNetworkView view) {
        IntactNetworkView iView = new IntactNetworkView(manager, view);
        intactNetworkViewMap.put(view, iView);
    }

    public CyNetwork getCurrentNetwork() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetwork();
    }

    public CyNetworkView getCurrentNetworkView() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetworkView();
    }

    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
        CyNetwork network = e.getNetwork();
        // delete enrichment tables
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        IntactNetwork intactNetwork = intactNetworkMap.get(network);
        if (intactNetwork != null) {
            tableManager.deleteTable(intactNetwork.getFeaturesTable().getSUID());
            tableManager.deleteTable(intactNetwork.getIdentifiersTable().getSUID());
            // remove as string network
            intactNetworkMap.remove(network);

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
        if (intactNetworkMap.containsKey(baseNetwork)) {
            IntactNetwork parent = intactNetworkMap.get(baseNetwork);
            IntactNetwork newINetwork = new IntactNetwork(manager);
            addIntactNetwork(newINetwork, newNetwork);
            ModelUtils.buildSubTablesForSubIntactNetwork(newINetwork, parent);
        }
    }

    @Override
    public void handleEvent(NetworkViewAddedEvent e) {
        if (intactNetworkMap.containsKey(e.getNetworkView().getModel())) {
            addNetworkView(e.getNetworkView());
        }
    }

    @Override
    public void handleEvent(SessionLoadedEvent event) {
        // Create string networks for any networks loaded by string
        intactNetworkMap.clear();
        intactNetworkViewMap.clear();
        Set<CyNetwork> networks = event.getLoadedSession().getNetworks();
        List<IntactNetwork> iNetworks = new ArrayList<>();
        for (CyNetwork network : networks) {
            if (ModelUtils.isIntactNetwork(network)) {
                if (ModelUtils.ifHaveIntactNS(network)) {
                    IntactNetwork intactNetwork = new IntactNetwork(manager);
                    addIntactNetwork(intactNetwork, network);
                    intactNetwork.completeMissingNodeColorsFromTables();
                    iNetworks.add(intactNetwork);
                }
            }
        }

        linkIntactTablesToNetwork(event.getLoadedSession().getTables(), getEdgeTableMapping(iNetworks));

        for (CyNetworkView view : event.getLoadedSession().getNetworkViews()) {
            if (ModelUtils.isIntactNetwork(view.getModel())) {
                addNetworkView(view);
            }
        }


        if (ModelUtils.ifHaveIntactNS(getCurrentNetwork())) {
            CyApplicationManager applicationManager = manager.utils.getService(CyApplicationManager.class);
            applicationManager.setCurrentNetworkView(applicationManager.getCurrentNetworkView());
            manager.utils.showResultsPanel();
        } else {
            manager.utils.hideResultsPanel();
        }
    }

    Map<CyNetwork, Map<Long, Long>> getEdgeTableMapping(List<IntactNetwork> iNetworks) {
        Map<CyNetwork, Map<Long, Long>> edgeMapping = new HashMap<>();
        for (IntactNetwork iNetwork : iNetworks) {
            Map<Long, Long> networkEdgeMapping = new HashMap<>();
            CyNetwork network = iNetwork.getNetwork();
            edgeMapping.put(network, networkEdgeMapping);
            for (CyRow edgeRow : network.getDefaultEdgeTable().getAllRows()) {
                Long id = edgeRow.get(ModelUtils.INTACT_ID, Long.class);
                if (id == null) continue;
                Long suid = edgeRow.get(CyEdge.SUID, Long.class);
                networkEdgeMapping.put(id, suid);

            }
        }
        return edgeMapping;
    }

    void linkCollapsedEdgesIdsToSUIDs(CyTableMetadata tableM, Map<CyNetwork, Map<Long, Long>> edgeMapping) {
        CyTable edgeTable = tableM.getTable();
        CyColumn collapsedIntactIdsColumn = edgeTable.getColumn(ModelUtils.C_INTACT_IDS);
        if (collapsedIntactIdsColumn == null) return;

        Map<Long, Long> networkEdgeMapping = edgeMapping.get(rootNetworkManager.getRootNetwork(tableM.getNetwork()).getSubNetworkList().get(0));
        if (networkEdgeMapping == null) return;

        for (CyRow row : edgeTable.getAllRows()) {
            List<Long> ids = row.getList(ModelUtils.C_INTACT_IDS, Long.class);
            if (ids == null) continue;
            List<Long> list = ids.stream().filter(Objects::nonNull).map(networkEdgeMapping::get).collect(Collectors.toList());
            row.set(ModelUtils.C_INTACT_SUIDS, list);
        }
    }


    void linkIntactTablesToNetwork(Collection<CyTableMetadata> tables, Map<CyNetwork, Map<Long, Long>> edgeMapping) {
        for (CyTableMetadata tableM : tables) {
            linkCollapsedEdgesIdsToSUIDs(tableM, edgeMapping);
            CyTable table = tableM.getTable();
            CyColumn networkUUIDColumn = table.getColumn(ModelUtils.NET_UUID);
            if (networkUUIDColumn == null) continue;

            List<String> uuids = networkUUIDColumn.getValues(String.class);
            if (uuids.isEmpty()) continue;
            for (IntactNetwork iNetwork : intactNetworkMap.values()) {
                CyNetwork network = iNetwork.getNetwork();
                CyRow netRow = network.getRow(network);
                if (netRow.get(NET_UUID, String.class).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                    if (table.getColumn(IDENTIFIER_ID) != null) {
                        iNetwork.setIdentifiersTable(table);
                    } else if (table.getColumn(FEATURE_EDGE_IDS) != null) {
                        iNetwork.setFeaturesTable(table);
                        Map<Long, Long> networkEdgeMapping = edgeMapping.get(network);
                        for (CyRow featureRow : table.getAllRows()) {
                            List<Long> edgeIds = featureRow.getList(FEATURE_EDGE_IDS, Long.class);
                            if (edgeIds == null || edgeIds.isEmpty()) continue;
                            featureRow.set(ModelUtils.FEATURE_EDGE_SUIDS, edgeIds.stream().map(networkEdgeMapping::get).collect(Collectors.toList()));
                        }
                    }
                    break;
                }
            }

        }
    }
    //================= Data getters =================//

    public IntactNetwork getIntactNetwork(CyNetwork network) {
        if (intactNetworkMap.containsKey(network))
            return intactNetworkMap.get(network);
        return null;
    }

    public IntactNetwork getCurrentIntactNetwork() {
        return intactNetworkMap.get(getCurrentNetwork());
    }

    public IntactNetwork[] getIntactNetworks() {
        return intactNetworkMap.values().toArray(IntactNetwork[]::new);
    }

    public IntactNetworkView getIntactNetworkView(CyNetworkView view) {
        return intactNetworkViewMap.get(view);
    }

    public IntactNetworkView getCurrentIntactNetworkView() {
        return intactNetworkViewMap.get(getCurrentNetworkView());
    }

    public IntactNetworkView[] getIntactViews() {
        return intactNetworkViewMap.values().toArray(IntactNetworkView[]::new);
    }

    //================= IntactNetworkCreated =================//

    public void fireIntactNetworkCreated(IntactNetwork intactNetwork) {
        for (IntactNetworkCreatedListener listener : intactNetworkCreatedListeners) {
            listener.handleEvent(new IntactNetworkCreatedEvent(manager, intactNetwork));
        }
    }

    public void addIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.add(listener);
    }

    public void removeIntactNetworkCreatedListener(IntactNetworkCreatedListener listener) {
        intactNetworkCreatedListeners.remove(listener);
    }


    //================= IntactViewTypeChanged =================//
    public void intactViewTypeChanged(IntactNetworkView.Type newType, IntactNetworkView iView) {
        manager.style.intactStyles.get(newType).applyStyle(iView.view);
        iView.type = newType;
        fireIntactViewTypeChangedEvent(new IntactViewTypeChangedEvent(manager, newType));
    }

    void fireIntactViewTypeChangedEvent(IntactViewTypeChangedEvent event) {
        for (IntactViewTypeChangedListener listener : intactViewTypeChangedListeners) {
            listener.handleEvent(event);
        }
    }

    public void addIntactViewTypeChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.add(listener);
    }

    public void removeIntactViewTypeChangedListener(IntactViewTypeChangedListener listener) {
        intactViewTypeChangedListeners.remove(listener);
    }
}
