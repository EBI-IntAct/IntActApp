package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.MutationViewTask;

public class MutationViewTaskFactory extends AbstractHiderTaskFactory {
    public MutationViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MutationViewTask(manager, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
