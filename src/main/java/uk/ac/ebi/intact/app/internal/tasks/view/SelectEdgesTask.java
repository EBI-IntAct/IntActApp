package uk.ac.ebi.intact.app.internal.tasks.view;

import lombok.AllArgsConstructor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;

@AllArgsConstructor
public class SelectEdgesTask extends AbstractTask {
    private final CyNetwork cyNetwork;
    private final Collection<Long> edgeIdsToSelect;

    @Override
    public void run(TaskMonitor taskMonitor) {
        cyNetwork.getEdgeList().forEach(edge -> {
            CyRow row = cyNetwork.getRow(edge);
            if (row != null) {
                row.set(CyNetwork.SELECTED, edgeIdsToSelect.contains(edge.getSUID()));
            }
        });
    }
}
