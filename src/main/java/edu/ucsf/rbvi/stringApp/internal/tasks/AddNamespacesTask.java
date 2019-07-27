package edu.ucsf.rbvi.stringApp.internal.tasks;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.stringApp.internal.model.Annotation;
import edu.ucsf.rbvi.stringApp.internal.model.Databases;
import edu.ucsf.rbvi.stringApp.internal.model.Species;
import edu.ucsf.rbvi.stringApp.internal.model.StringManager;
import edu.ucsf.rbvi.stringApp.internal.model.StringNetwork;
import edu.ucsf.rbvi.stringApp.internal.ui.SearchOptionsPanel;
import edu.ucsf.rbvi.stringApp.internal.utils.ModelUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;

public class AddNamespacesTask extends AbstractTask implements ObservableTask, TaskObserver {
	
	final StringManager manager;
	// private StringNetwork stringNetwork;
	private CyNetwork net;
	//private CyNetwork loadedNetwork = null;
	private final Logger logger = Logger.getLogger(CyUserLog.NAME);
	private TaskMonitor monitor;

	//@Tunable(description="Network to set as a STRING network", 
	//         longDescription=StringToModel.CY_NETWORK_LONG_DESCRIPTION,
	//        exampleStringValue=StringToModel.CY_NETWORK_EXAMPLE_STRING,
	//         context="nogui", required=true)
	//public CyNetwork network = null;

	
	public AddNamespacesTask(final StringManager manager, final CyNetwork net) {
		this.manager = manager;
		this.net = net;
	}

	public void run(TaskMonitor monitor) {
		this.monitor = monitor;
		monitor.setTitle("Add namespaces to column names");

		// Do a little sanity checking
		if (net == null) {
			monitor.showMessage(TaskMonitor.Level.ERROR, "No network specified");
			return;
		}

		//if (ModelUtils.isStringNetwork(net) && ModelUtils.ifHaveStringNS(net))  {
		//	monitor.showMessage(TaskMonitor.Level.ERROR, "Network '"+net+"' is already a STRING network and has namespaces");
		//	return;
		//}

		// Get node columns to copy
		CyTable nodeTable = net.getDefaultNodeTable();
		HashMap<CyColumn, String> fromToColumns = new HashMap<CyColumn, String>();
		for (CyColumn col : nodeTable.getColumns()) {
			String columnName = col.getName();
			if (ModelUtils.namespacedNodeAttributes.contains(columnName)) {
				//  add STRINGDB namespace 
				fromToColumns.put(col, ModelUtils.STRINGDB_NAMESPACE + ModelUtils.NAMESPACE_SEPARATOR + columnName);
			} else if (columnName.startsWith(ModelUtils.TISSUE_NAMESPACE) || columnName.startsWith(ModelUtils.COMPARTMENT_NAMESPACE)) {
				// add tissues or compartments namespace
				fromToColumns.put(col, columnName.replaceFirst(" ", ModelUtils.NAMESPACE_SEPARATOR));
			} 
		}

		// Copy data for selected columns
		for (CyNode node : net.getNodeList()) {
			for (CyColumn oldCol : fromToColumns.keySet()) {
				ModelUtils.createColumnIfNeeded(nodeTable, oldCol.getType(), fromToColumns.get(oldCol));
				Object v = nodeTable.getRow(node.getSUID()).getRaw(oldCol.getName());
				nodeTable.getRow(node.getSUID()).set(fromToColumns.get(oldCol), v);
			}
		}
		
		// delete old columns
		//for (CyColumn oldCol : fromToColumns.keySet()) {
		//	ModelUtils.deleteColumnIfExisting(nodeTable, oldCol.getName());
		//}

		// Edge columns
		CyTable edgeTable = net.getDefaultEdgeTable();
		fromToColumns = new HashMap<CyColumn, String>();
		for (CyColumn col : edgeTable.getColumns()) {
			if (ModelUtils.namespacedEdgeAttributes.contains(col.getName())) {
				//  add STRINGDB namespace 
				fromToColumns.put(col, ModelUtils.STRINGDB_NAMESPACE + ModelUtils.NAMESPACE_SEPARATOR + col.getName());				
			} 
		}

		// Copy data for selected columns
		for (CyEdge edge : net.getEdgeList()) {
			for (CyColumn oldCol : fromToColumns.keySet()) {
				ModelUtils.createColumnIfNeeded(edgeTable, oldCol.getType(), fromToColumns.get(oldCol));
				Object v = edgeTable.getRow(edge.getSUID()).getRaw(oldCol.getName());
				edgeTable.getRow(edge.getSUID()).set(fromToColumns.get(oldCol), v);
			}
		}

		// Network columns
		ModelUtils.setDataVersion(net, manager.getOldDataVersion());
		ModelUtils.setNetURI(net, "");
	}

	@Override
	public void cancel() {

	}

	@Override
	public void allFinished(FinishStatus arg0) {

	}

	@Override
	public void taskFinished(ObservableTask arg0) {

	}

	@Override
	public <R> R getResults(Class<? extends R> clzz) {
		if (clzz.equals(CyNetwork.class)) {
			return (R) net;
		} else if (clzz.equals(Long.class)) {
			if (net == null)
				return null;
			return (R) net.getSUID();
		} else if (clzz.equals(JSONResult.class)) {
			JSONResult res = () -> {
				if (net == null) return "{}";
				else return "{\"network\": "+net.getSUID()+"}";
      };
      return (R)res;
		} else if (clzz.equals(String.class)) {
			if (net == null)
				return (R) "No network was set";
			String resp = "Set network '"
					+ net.getRow(net).get(CyNetwork.NAME, String.class);
			resp += " as STRING network";
			return (R) resp;
		}
		return null;
	}

}