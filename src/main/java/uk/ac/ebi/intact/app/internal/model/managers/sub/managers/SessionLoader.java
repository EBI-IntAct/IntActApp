package uk.ac.ebi.intact.app.internal.model.managers.sub.managers;

import org.cytoscape.model.*;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.events.ViewUpdatedEvent;
import uk.ac.ebi.intact.app.internal.model.tables.Table;
import uk.ac.ebi.intact.app.internal.model.tables.fields.model.ListField;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.FeatureFields;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.NetworkFields;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SessionLoader implements SessionLoadedListener {
    final Manager manager;

    public SessionLoader(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void handleEvent(SessionLoadedEvent event) {
        // Create string networks for any networks loaded by string
        manager.data.networkMap.clear();
        manager.data.networkViewMap.clear();
        CySession loadedSession = event.getLoadedSession();
        Set<CyNetwork> cyNetworks = loadedSession.getNetworks();
        for (CyNetwork cyNetwork : cyNetworks) {
            if (ModelUtils.isIntactNetwork(cyNetwork)) {
                if (ModelUtils.ifHaveIntactNS(cyNetwork)) {
                    updateSUIDList(cyNetwork.getDefaultEdgeTable(), EdgeFields.SUMMARY_EDGES_SUID, CyEdge.class, loadedSession);
                    Network network = new Network(manager);
                    manager.data.addNetwork(network, cyNetwork);
                    network.completeMissingNodeColorsFromTables();
                }
            }
        }

        linkIntactTablesToNetwork(loadedSession.getTables(), loadedSession);

        for (CyNetworkView view : loadedSession.getNetworkViews()) {
            if (ModelUtils.isIntactNetwork(view.getModel())) {
                manager.data.addNetworkView(view, true);
            }
        }

        NetworkView currentView = manager.data.getCurrentNetworkView();
        if (currentView != null) {
            manager.utils.fireEvent(new ViewUpdatedEvent(manager, currentView));
            manager.utils.showResultsPanel();
        } else {
            manager.utils.hideResultsPanel();
        }
    }

    void linkIntactTablesToNetwork(Collection<CyTableMetadata> tables, CySession loadingSession) {
        for (CyTableMetadata tableM : tables) {
            CyTable table = tableM.getTable();

            CyColumn networkUUIDColumn = NetworkFields.UUID.getColumn(table);
            if (networkUUIDColumn == null) continue;
            List<String> uuids = networkUUIDColumn.getValues(String.class);
            if (uuids.isEmpty()) continue;

            if (Table.FEATURE.containsAllFields(table)) {
                updateSUIDList(table, FeatureFields.EDGES_SUID, CyEdge.class, loadingSession);
                for (Network network : manager.data.networkMap.values()) {
                    CyNetwork cyNetwork = network.getCyNetwork();
                    CyRow netRow = cyNetwork.getRow(cyNetwork);
                    if (NetworkFields.UUID.getValue(netRow).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                        network.setFeaturesTable(table);
                    }
                }
            }

            if (Table.IDENTIFIER.containsAllFields(table)) {
                for (Network network : manager.data.networkMap.values()) {
                    CyNetwork cyNetwork = network.getCyNetwork();
                    if (NetworkFields.UUID.getValue(cyNetwork.getRow(cyNetwork)).equals(uuids.get(0))) { // If the UUID referenced in defaultValue belong to this network
                        network.setIdentifiersTable(table);
                    }
                }
            }
        }
    }

    private void updateSUIDList(CyTable sourceTable, ListField<Long> linkField, Class<? extends CyIdentifiable> targetType, CySession loadingSession) {
        for (CyRow row : sourceTable.getAllRows()) {
            List<Long> suids = linkField.getValue(row);
            if (!suids.isEmpty() && loadingSession.getObject(suids.get(0), targetType) == null) return;
            linkField.map(row, oldSUID -> {
                CyIdentifiable object = loadingSession.getObject(oldSUID, targetType);
                if (object != null) return object.getSUID();
                return null;
            });
        }
    }
}
