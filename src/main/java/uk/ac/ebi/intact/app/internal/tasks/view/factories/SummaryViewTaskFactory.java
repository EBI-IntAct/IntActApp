package uk.ac.ebi.intact.app.internal.tasks.view.factories;


import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.CollapseViewTask;

public class SummaryViewTaskFactory extends AbstractHiderTaskFactory {

    public SummaryViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new CollapseViewTask(manager, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
