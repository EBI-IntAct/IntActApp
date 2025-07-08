package uk.ac.ebi.intact.app.internal.tasks.view.filter;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.AbstractViewTaskFactory;

public class ApplyFiltersTaskFactory extends AbstractViewTaskFactory {

    public ApplyFiltersTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ApplyFiltersTask(manager, currentView));
    }
}
