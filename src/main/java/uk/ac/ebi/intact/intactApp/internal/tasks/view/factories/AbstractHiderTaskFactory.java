package uk.ac.ebi.intact.intactApp.internal.tasks.view.factories;

import org.cytoscape.command.StringToModel;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.AbstractTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;

public abstract class AbstractHiderTaskFactory extends AbstractTaskFactory {
    final protected IntactManager manager;
    final protected boolean currentView;
    protected StringToModel stringToModel;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public AbstractHiderTaskFactory(IntactManager manager, boolean currentView) {
        this.manager = manager;
        this.currentView = currentView;
        stringToModel = manager.utils.getService(StringToModel.class);
        hideTaskFactory = manager.utils.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.utils.getService(UnHideTaskFactory.class);
    }
}
