package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.*;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.events.VisualStyleSetEvent;
import org.cytoscape.view.vizmap.events.VisualStyleSetListener;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.NetworkCreatedEvent;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.SummaryStyle;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.*;
import uk.ac.ebi.intact.app.internal.tasks.clone.TableClonedEvent;
import uk.ac.ebi.intact.app.internal.tasks.clone.TableClonedListener;
import uk.ac.ebi.intact.app.internal.tasks.clone.factories.CloneTableTaskFactory;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.utils.ModelUtils.*;

public class DataManager implements
        NetworkAddedListener,
        NetworkViewAddedListener,
        NetworkAboutToBeDestroyedListener,
        NetworkViewAboutToBeDestroyedListener,
        VisualStyleSetListener,
        TableClonedListener,
        SessionAboutToBeLoadedListener {
    private final Manager manager;
    final CyRootNetworkManager rootNetworkManager;
    final CyNetworkViewManager networkViewManager;
    final HideTaskFactory hideTaskFactory;
    final UnHideTaskFactory unHideTaskFactory;
    final Map<CyNetwork, Network> networkMap;
    final Map<CyNetworkView, NetworkView> networkViewMap;

    public DataManager(Manager manager) {
        this.manager = manager;
        networkMap = new HashMap<>();
        networkViewMap = new HashMap<>();
        rootNetworkManager = manager.utils.getService(CyRootNetworkManager.class);
        networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.utils.getService(UnHideTaskFactory.class);
        manager.utils.registerAllServices(this, new Properties());
        // Load Fields
        System.out.println(NodeFields.SPECIES.toString());
        System.out.println(EdgeFields.SUMMARY_NB_EDGES.toString());
        System.out.println(NetworkFields.FEATURES_TABLE_REF.toString());
        System.out.println(FeatureFields.TYPE.toString());
        System.out.println(IdentifierFields.ID.toString());
    }

    public void loadCurrentSession() {
        manager.utils.getService(CySessionManager.class).getCurrentSession().getProperties();
        CyNetworkViewManager networkViewManager = manager.utils.getService(CyNetworkViewManager.class);
        for (CyNetwork cyNetwork : manager.utils.getService(CyNetworkManager.class).getNetworkSet()) {
            if (!isIntactNetwork(cyNetwork)) continue;
            Network network = new Network(manager);
            addNetwork(network, cyNetwork);
            linkNetworkTablesFromTableData(network);
            fireIntactNetworkCreated(network);
            network.completeMissingNodeColorsFromTables(true, null);
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

    public void addNetwork(Network network, CyNetwork cyNetwork) {
        networkMap.put(cyNetwork, network);
        network.setNetwork(cyNetwork);
    }

    public void setCurrentNetwork(CyNetwork cyNetwork) {
        CyNetworkManager networkManager = manager.utils.getService(CyNetworkManager.class);
        if (!networkManager.networkExists(cyNetwork.getSUID())) {
            networkManager.addNetwork(cyNetwork);
        }
        manager.utils.getService(CyApplicationManager.class).setCurrentNetwork(cyNetwork);
    }

    public CyNetworkView createNetworkView(CyNetwork cyNetwork) {
        CyNetworkView view = manager.utils.getService(CyNetworkViewFactory.class)
                .createNetworkView(cyNetwork);
        manager.style.setStylesFancy(true);
        if (networkMap.containsKey(cyNetwork)) {
            networkMap.get(cyNetwork).hideExpandedEdgesOnViewCreation(view);
            manager.style.styles.get(SummaryStyle.type).applyStyle(view);
        }
        return view;
    }

    public NetworkView addNetworkView(CyNetworkView cyView, boolean loadData) {
        return addNetworkView(cyView, loadData, NetworkView.Type.SUMMARY);
    }

    public NetworkView addNetworkView(CyNetworkView cyView, boolean loadData, NetworkView.Type type) {
        NetworkView view = new NetworkView(manager, cyView, loadData, type);
        networkViewMap.put(cyView, view);
        return view;
    }

    @Override
    public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
        CyNetwork cyNetwork = e.getNetwork();
        CyTableManager tableManager = manager.utils.getService(CyTableManager.class);
        Network network = networkMap.get(cyNetwork);
        if (network != null) {
            if (rootNetworkManager.getRootNetwork(cyNetwork).getSubNetworkList().size() == 1) {
                CyTable featuresTable = network.getFeaturesTable();
                if (featuresTable != null) tableManager.deleteTable(featuresTable.getSUID());
                CyTable identifiersTable = network.getIdentifiersTable();
                if (identifiersTable != null) tableManager.deleteTable(identifiersTable.getSUID());
            }

            networkMap.remove(cyNetwork);
        }
    }

    @Override
    public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
        networkViewMap.remove(e.getNetworkView());
    }

    public void removeNetwork(Network network) {
        if (network == null) return;
        CyNetwork cyNetwork = network.getCyNetwork();
        if (cyNetwork == null) return;
        networkMap.remove(cyNetwork);
        networkViewManager.getNetworkViews(cyNetwork)
                .forEach(view -> {
                    networkViewMap.remove(view);
                    networkViewManager.destroyNetworkView(view);
                });
    }

    private final Map<CyTable, Network> oldTablesToNewNetwork = new HashMap<>();

    private boolean loadingSession = false;

    public void setLoadingSession(boolean loadingSession) {
        this.loadingSession = loadingSession;
    }

    @Override
    public void handleEvent(SessionAboutToBeLoadedEvent e) {
        loadingSession = true;
    }

    /**
     * Handles management of new sub networks and cloned networks
     *
     * @param e
     */
    @Override
    public void handleEvent(NetworkAddedEvent e) {
        CyNetwork cyNetwork = e.getNetwork();
        CySubNetwork newNetwork = (CySubNetwork) cyNetwork;
        CySubNetwork parentCyNetwork = getParentCyNetwork(newNetwork, manager);
        if (parentCyNetwork != null) addSubNetwork(newNetwork, parentCyNetwork);
        else if (!loadingSession && ModelUtils.isIntactNetwork(cyNetwork) && !networkMap.containsKey(cyNetwork)) { // Cloned network
            Network network = new Network(manager);
            addNetwork(network, cyNetwork);
            CloneTableTaskFactory cloneTable = new CloneTableTaskFactory(manager);
            NetworkFields.UUID.setValue(cyNetwork.getRow(cyNetwork), UUID.randomUUID().toString());

            CyTable featuresTable = network.getFeaturesTable();
            oldTablesToNewNetwork.put(featuresTable, network);
            manager.utils.execute(cloneTable.createTaskIterator(featuresTable));

            CyTable identifiersTable = network.getIdentifiersTable();
            oldTablesToNewNetwork.put(identifiersTable, network);
            manager.utils.execute(cloneTable.createTaskIterator(identifiersTable));
        }
    }

    /**
     * At network cloning, handles the duplication of IntAct sub tables
     *
     * @param e
     */
    @Override
    public void handleEvent(TableClonedEvent e) {
        CyTable originalTable = e.getOriginalTable();
        CyTable clonedTable = e.getClonedTable();
        if (Table.IDENTIFIER.containsAllFields(originalTable)) {
            Network network = oldTablesToNewNetwork.remove(originalTable);
            NetworkFields.UUID.setAllValues(clonedTable, NetworkFields.UUID.getValue(network.getCyRow()));
            network.setIdentifiersTable(clonedTable);

        } else if (Table.FEATURE.containsAllFields(originalTable)) {
            Network network = oldTablesToNewNetwork.remove(originalTable);
            FeatureFields.EDGES_SUID.clearAllIn(clonedTable);
            for (CyRow edgeRow : network.getCyNetwork().getDefaultEdgeTable().getAllRows()) {
                Long edgeSUID = edgeRow.get(CyIdentifiable.SUID, Long.class);
                if (!EdgeFields.IS_SUMMARY.getValue(edgeRow)) {
                    Consumer<String> edgeRefAdder = featureAc -> FeatureFields.EDGES_SUID.addValue(clonedTable.getRow(featureAc), edgeSUID);
                    EdgeFields.FEATURES.SOURCE.forEachElement(edgeRow, edgeRefAdder);
                    EdgeFields.FEATURES.TARGET.forEachElement(edgeRow, edgeRefAdder);
                } else {
                    List<Long> summarizedEdgesSUID = network.getSimilarEvidenceCyEdges(network.getCyEdge(edgeSUID)).stream()
                            .map(CyIdentifiable::getSUID).collect(Collectors.toList());
                    EdgeFields.SUMMARIZED_EDGES_SUID.setValue(edgeRow, summarizedEdgesSUID);
                }
            }
            NetworkFields.UUID.setAllValues(clonedTable, NetworkFields.UUID.getValue(network.getCyRow()));
            network.setFeaturesTable(clonedTable);
        }
    }


    private static class NetworkViewTypeToSet {
        final NetworkView.Type type;
        boolean toSet = false;
        int counterOfStyleResetting = 0;

        private NetworkViewTypeToSet(NetworkView.Type type) {
            this.type = type;
        }
    }

    /**
     * Store sub network view types to affect them with {@link #handleEvent(NetworkViewAddedEvent) }
     */
    private final Map<CyNetwork, NetworkViewTypeToSet> networkTypesToSet = new HashMap<>();
    private final Map<NetworkView, NetworkViewTypeToSet> networkViewTypesToSet = new HashMap<>();

    /**
     * Register and manage sub networks at their creation.<br>
     * <p>
     * The created sub network view must be set to its direct parent type.<br>
     * To do so, addSubNetwork register the network view type of the direct parent within {@link #networkTypesToSet}.<br>
     * {@link #handleEvent(NetworkViewAddedEvent)} will then set the new sub network view to its parent view type.<br>
     * Cytoscape will then change from the correct view type to the root parent's one.<br>
     * {@link #handleEvent(VisualStyleSetEvent)} then interrupt Cytoscape behaviour to set it to the correct style.
     */
    private void addSubNetwork(CySubNetwork subCyNetwork, CySubNetwork parentCyNetwork) {
        if (networkMap.containsKey(parentCyNetwork)) {
            Network parentNetwork = networkMap.get(parentCyNetwork);
            handleSubNetworkEdges(subCyNetwork, parentNetwork);
            Network subNetwork = new Network(manager);
            addNetwork(subNetwork, subCyNetwork);

            for (CyNetworkView cyNetworkView : networkViewManager.getNetworkViews(parentCyNetwork)) {
                NetworkView networkView = networkViewMap.get(cyNetworkView);
                if (networkView == null) continue;
                networkTypesToSet.put(subCyNetwork, new NetworkViewTypeToSet(networkView.getType()));
                break;
            }

            subNetwork.setFeaturesTable(parentNetwork.getFeaturesTable());
            subNetwork.setIdentifiersTable(parentNetwork.getIdentifiersTable());
            NetworkFields.UUID.setValue(subCyNetwork.getRow(subCyNetwork), NetworkFields.UUID.getValue(parentCyNetwork.getRow(parentCyNetwork)));
            subNetwork.getNodes().forEach(Node::updateMutationStatus);
            subNetwork.getSummaryEdges().forEach(SummaryEdge::updateSummary);
        }
    }

    /**
     * Set sub network view style to their parent ones (stored in {@link #networkTypesToSet}) at creation, after {@link #addSubNetwork(CySubNetwork, CySubNetwork)}.<br>
     * Cytoscape will then change from the correct view type to the root parent's one.<br>
     * {@link #handleEvent(VisualStyleSetEvent)} then interrupt Cytoscape behaviour to set it to the correct style.<br><br>
     * <p>
     * This listener is triggered twice at sub network creation.
     */
    @Override
    public void handleEvent(NetworkViewAddedEvent e) {
        CyNetwork cyNetwork = e.getNetworkView().getModel();
        if (networkMap.containsKey(cyNetwork)) {
            if (networkTypesToSet.containsKey(cyNetwork)) {
                NetworkViewTypeToSet networkViewTypeToSet = networkTypesToSet.get(cyNetwork);
                if (!networkViewTypeToSet.toSet) networkViewTypeToSet.toSet = true; // Avoid work at first trigger
                else {
                    NetworkView networkView = addNetworkView(e.getNetworkView(), false, networkViewTypeToSet.type);
                    manager.style.styles.get(networkViewTypeToSet.type).applyStyle(e.getNetworkView());
                    networkViewTypesToSet.put(networkView, networkViewTypeToSet);
                    networkTypesToSet.remove(cyNetwork);
                }
            } else {
                NetworkView networkView = addNetworkView(e.getNetworkView(), false);
                manager.style.styles.get(NetworkView.Type.SUMMARY).applyStyle(networkView.cyView);
            }
        }
    }

    /**
     * Set sub networks view styles at their {@link #addSubNetwork(CySubNetwork, CySubNetwork) creation} to use the direct parent type instead of root parent one's, the default behaviour of Cytoscape.<br>
     * <p>
     * The style need to be set firstly by {@link #handleEvent(NetworkViewAddedEvent)},<br>
     * so that Cytoscape react by setting the wrong style,<br>
     * so that this listener is triggered to correct Cytoscape mistake.
     */
    @Override
    public void handleEvent(VisualStyleSetEvent e) {
        NetworkView networkView = getNetworkView(e.getNetworkView());
        if (networkView == null) return;
        if (networkViewTypesToSet.containsKey(networkView)) {
            NetworkViewTypeToSet correctType = networkViewTypesToSet.get(networkView);
            VisualStyle correctStyle = manager.style.styles.get(correctType.type).getStyle();
            VisualStyle currentStyle = manager.style.vmm.getVisualStyle(networkView.cyView);

            if (currentStyle != correctStyle) {
                manager.style.styles.get(networkView.getType()).applyStyle(networkView.cyView);
            }

            correctType.counterOfStyleResetting++;
            if (correctType.counterOfStyleResetting >= 3)
                networkViewTypesToSet.remove(networkView);
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

    public Network[] getNetworks() {
        return networkMap.values().toArray(Network[]::new);
    }

    public CyNetworkView getCurrentCyView() {
        return manager.utils.getService(CyApplicationManager.class).getCurrentNetworkView();
    }

    public NetworkView getNetworkView(CyNetworkView view) {
        return networkViewMap.get(view);
    }

    public List<NetworkView> getNetworkViews(Network network) {
        return networkViewManager.getNetworkViews(network.getCyNetwork()).stream().map(networkViewMap::get).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public NetworkView getCurrentNetworkView() {
        return networkViewMap.get(getCurrentCyView());
    }

    public NetworkView[] getViews() {
        return networkViewMap.values().toArray(NetworkView[]::new);
    }

    public void fireIntactNetworkCreated(Network network) {
        manager.utils.fireEvent(new NetworkCreatedEvent(manager, network));
    }

    public void viewChanged(NetworkView.Type newType, NetworkView view) {
        view.setType(newType);
        manager.style.styles.get(newType).applyStyle(view.cyView);
        manager.utils.fireEvent(new ViewUpdatedEvent(manager, view));
    }

}
