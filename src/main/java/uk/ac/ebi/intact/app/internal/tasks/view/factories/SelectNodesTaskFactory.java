package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import lombok.AllArgsConstructor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.tasks.view.SelectNodesTask;

import java.util.Collection;

@AllArgsConstructor
public class SelectNodesTaskFactory extends AbstractTaskFactory {

    private final CyNetwork network;
    private final Collection<Long> nodeIdsToSelect;

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SelectNodesTask(network, nodeIdsToSelect));
    }
}
