package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.ExpendTask;

public class ExpendTaskFactory extends AbstractTaskFactory {
    private IntactManager manager;
    private HideTaskFactory hideTaskFactory;
    private UnHideTaskFactory unHideTaskFactory;

    public ExpendTaskFactory(IntactManager manager) {
        this.manager = manager;
        hideTaskFactory = manager.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.getService(UnHideTaskFactory.class);
    }

    /**
     * Returns an iterator containing a sequence of <code>Task</code>s.
     *
     * @return an iterator containing a sequence of <code>Task</code>s.
     */
    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ExpendTask(manager, hideTaskFactory, unHideTaskFactory));
    }
}
