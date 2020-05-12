package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.ExpandViewTask;

public class ExpandViewTaskFactory extends AbstractHiderTaskFactory {

    public ExpandViewTaskFactory(IntactManager manager) {
        super(manager);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExpandViewTask(manager, hideTaskFactory, unHideTaskFactory));
    }
}
