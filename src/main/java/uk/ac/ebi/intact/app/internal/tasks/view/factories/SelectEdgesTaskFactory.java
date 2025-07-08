package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import lombok.AllArgsConstructor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.tasks.view.SelectEdgesTask;

import java.util.Collection;

@AllArgsConstructor
public class SelectEdgesTaskFactory extends AbstractTaskFactory {

    private final CyNetwork network;
    private final Collection<Long> edgeIdsToSelect;

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SelectEdgesTask(network, edgeIdsToSelect));
    }
}
