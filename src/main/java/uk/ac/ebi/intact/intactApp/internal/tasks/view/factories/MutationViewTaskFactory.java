package uk.ac.ebi.intact.intactApp.internal.tasks.view.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.view.MutationViewTask;

public class MutationViewTaskFactory extends AbstractHiderTaskFactory {
    public MutationViewTaskFactory(IntactManager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MutationViewTask(manager, stringToModel, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
