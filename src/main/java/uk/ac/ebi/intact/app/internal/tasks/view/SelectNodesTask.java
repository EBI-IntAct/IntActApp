package uk.ac.ebi.intact.app.internal.tasks.view;

import lombok.AllArgsConstructor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import java.util.Collection;

@AllArgsConstructor
public class SelectNodesTask extends AbstractTask {
    private final CyNetwork cyNetwork;
    private final Collection<Long> nodeIdsToSelect;

    @Override
    public void run(TaskMonitor taskMonitor) {
        cyNetwork.getNodeList().forEach(node -> {
            CyRow row = cyNetwork.getRow(node);
            if (row != null) {
                row.set(CyNetwork.SELECTED, nodeIdsToSelect.contains(node.getSUID()));
            }
        });
    }
}
