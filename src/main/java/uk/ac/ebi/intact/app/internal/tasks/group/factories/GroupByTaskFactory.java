package uk.ac.ebi.intact.app.internal.tasks.group.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.group.GroupByTask;

public class GroupByTaskFactory extends AbstractNetworkTaskFactory {
    private final Manager manager;

    public GroupByTaskFactory(Manager manager) {
        this.manager = manager;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        return new TaskIterator(new GroupByTask(manager, network));
    }

}
