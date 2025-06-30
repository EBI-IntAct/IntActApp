package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.OrthologyViewTask;

public class OrthologyViewTaskFactory extends AbstractHiderTaskFactory{

    public OrthologyViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new OrthologyViewTask(manager, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
