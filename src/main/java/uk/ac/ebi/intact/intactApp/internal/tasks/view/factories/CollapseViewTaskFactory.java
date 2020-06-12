package uk.ac.ebi.intact.intactApp.internal.tasks.view.factories;


import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.view.CollapseViewTask;

public class CollapseViewTaskFactory extends AbstractHiderTaskFactory {

    public CollapseViewTaskFactory(IntactManager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new CollapseViewTask(manager, stringToModel, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
