package uk.ac.ebi.intact.intactApp.internal.tasks.view;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;

public class SelectEdgesTask extends AbstractTask {
    private final CyNetwork cyNetwork;
    private final Collection<CyEdge> edgesToSelect;

    public SelectEdgesTask(CyNetwork cyNetwork, Collection<CyEdge> edgesToSelect) {
        this.cyNetwork = cyNetwork;
        this.edgesToSelect = edgesToSelect;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        for (CyEdge edge : edgesToSelect) {
            cyNetwork.getRow(edge).set(CyNetwork.SELECTED, true);
        }
    }
}
