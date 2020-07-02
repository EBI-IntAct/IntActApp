package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.EvidenceViewTask;

public class EvidenceViewTaskFactory extends AbstractHiderTaskFactory {

    public EvidenceViewTaskFactory(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new EvidenceViewTask(manager, hideTaskFactory, unHideTaskFactory, currentView));
    }
}
