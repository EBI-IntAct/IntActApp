package uk.ac.ebi.intact.intactApp.internal.tasks.view.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.view.ExpandViewTask;

public class ExpandViewTaskFactory extends AbstractHiderTaskFactory {

    public ExpandViewTaskFactory(IntactManager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExpandViewTask(manager,stringToModel, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
