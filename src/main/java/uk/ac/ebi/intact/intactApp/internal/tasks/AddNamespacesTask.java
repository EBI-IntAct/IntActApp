package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AddNamespacesTask extends AbstractTask {

    final IntactManager manager;
    private final Logger logger = Logger.getLogger(CyUserLog.NAME);
    private Set<CyNetwork> networks;
    private TaskMonitor monitor;

    // @Tunable(description = "Add column namespaces to current STRING networks",
    //		longDescription = "Upgrade loaded STRING networks to work with the new side panel of stringApp v1.5 by adding column namespaces to the column names",
    //		exampleStringValue = "true", gravity = 1.0, required = true)
    // public boolean upgrade = true;

    public AddNamespacesTask(final IntactManager manager, final Set<CyNetwork> networks) {
        this.manager = manager;
        this.networks = networks;
    }

    public AddNamespacesTask(final IntactManager manager) {
        this.manager = manager;
        networks = new HashSet<>();
    }

    public void run(TaskMonitor aMonitor) {
        this.monitor = aMonitor;
        monitor.setTitle("Add namespaces to column names");
        // System.out.println("running task add namespaces");
        for (CyNetwork net : networks) {
            // System.out.println("checking network: " + net.toString());
            // Set old data version for each network
            ModelUtils.setDataVersion(net, manager.getOldDataVersion());
            ModelUtils.setNetURI(net, "");

            // If user wants to upgrade, add namespaces to the node and edge columns
            // if (upgrade) {
            // System.out.println("Adding namespaces to old STRING network: " + net.toString());
            monitor.setStatusMessage("Adding namespaces to detected STRING network: " + net.toString());

            // Get node columns to copy
            CyTable nodeTable = net.getDefaultNodeTable();
            HashMap<CyColumn, String> fromToColumns = new HashMap<>();
            for (CyColumn col : nodeTable.getColumns()) {
                String columnName = col.getName();
                if (ModelUtils.namespacedNodeAttributes.contains(columnName)) {
                    // add STRINGDB namespace
                    fromToColumns.put(col, ModelUtils.INTACTDB_NAMESPACE
                            + ModelUtils.NAMESPACE_SEPARATOR + columnName);
                    continue;
                }
                if (columnName.startsWith(ModelUtils.TISSUE_NAMESPACE)
                        || columnName.startsWith(ModelUtils.COMPARTMENT_NAMESPACE)) {
                    // add tissues or compartments namespace
                    fromToColumns.put(col,
                            columnName.replaceFirst(" ", ModelUtils.NAMESPACE_SEPARATOR));
                }
            }

            // Copy data for selected columns
            for (CyNode node : net.getNodeList()) {
                for (CyColumn oldCol : fromToColumns.keySet()) {
                    ModelUtils.createColumnIfNeeded(nodeTable, oldCol.getType(),
                            fromToColumns.get(oldCol));
                    Object v = nodeTable.getRow(node.getSUID()).getRaw(oldCol.getName());
                    nodeTable.getRow(node.getSUID()).set(fromToColumns.get(oldCol), v);
                }
            }

            // delete old columns
            // for (CyColumn oldCol : fromToColumns.keySet()) {
            // ModelUtils.deleteColumnIfExisting(nodeTable, oldCol.getName());
            // }

            // Edge columns
            CyTable edgeTable = net.getDefaultEdgeTable();
            fromToColumns = new HashMap<>();
            for (CyColumn col : edgeTable.getColumns()) {
                if (ModelUtils.namespacedEdgeAttributes.contains(col.getName())) {
                    // add STRINGDB namespace
                    fromToColumns.put(col, ModelUtils.INTACTDB_NAMESPACE
                            + ModelUtils.NAMESPACE_SEPARATOR + col.getName());
                }
            }

            // Copy data for selected columns
            for (CyEdge edge : net.getEdgeList()) {
                for (CyColumn oldCol : fromToColumns.keySet()) {
                    ModelUtils.createColumnIfNeeded(edgeTable, oldCol.getType(),
                            fromToColumns.get(oldCol));
                    Object v = edgeTable.getRow(edge.getSUID()).getRaw(oldCol.getName());
                    edgeTable.getRow(edge.getSUID()).set(fromToColumns.get(oldCol), v);
                }
            }
        }
    }

    @Override
    public void cancel() {

    }

}
