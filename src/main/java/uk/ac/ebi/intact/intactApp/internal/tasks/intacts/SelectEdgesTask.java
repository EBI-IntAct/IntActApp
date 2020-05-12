package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;

public class SelectEdgesTask extends AbstractTask {
    private final CyNetwork network;
    private final Collection<CyEdge> edgesToSelect;

    public SelectEdgesTask(CyNetwork network, Collection<CyEdge> edgesToSelect) {
        this.network = network;
        this.edgesToSelect = edgesToSelect;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        for (CyEdge edge : edgesToSelect) {
            network.getRow(edge).set(CyNetwork.SELECTED, true);
        }
    }
}
