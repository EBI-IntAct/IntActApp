package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import uk.ac.ebi.intact.app.internal.managers.Manager;

public abstract class AbstractHiderTaskFactory extends AbstractTaskFactory {
    final protected Manager manager;
    final protected boolean currentView;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public AbstractHiderTaskFactory(Manager manager, boolean currentView) {
        this.manager = manager;
        this.currentView = currentView;
        hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.utils.getService(UnHideTaskFactory.class);
    }
}
