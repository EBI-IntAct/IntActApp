package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public abstract class AbstractHiderTaskFactory extends AbstractViewTaskFactory {
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public AbstractHiderTaskFactory(Manager manager, boolean currentView) {
        super(manager,currentView);
        hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.utils.getService(UnHideTaskFactory.class);
    }
}
