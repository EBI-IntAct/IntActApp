package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.MutationViewTask;

public class MutationViewTaskFactory extends AbstractHiderTaskFactory {

    public MutationViewTaskFactory(IntactManager manager) {
        super(manager);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MutationViewTask(manager, hideTaskFactory, unHideTaskFactory));
    }
}
