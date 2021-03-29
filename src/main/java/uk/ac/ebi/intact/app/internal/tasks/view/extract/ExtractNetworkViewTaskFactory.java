package uk.ac.ebi.intact.app.internal.tasks.view.extract;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.AbstractViewTaskFactory;


public class ExtractNetworkViewTaskFactory extends AbstractViewTaskFactory {

    public ExtractNetworkViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExtractNetworkViewTask(manager, currentView));
    }
}
