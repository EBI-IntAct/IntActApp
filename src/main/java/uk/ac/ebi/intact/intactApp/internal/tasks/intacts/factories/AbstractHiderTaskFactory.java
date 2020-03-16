package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

public abstract class AbstractHiderTaskFactory extends AbstractTaskFactory {
    protected IntactManager manager;
    protected CyNetworkView view;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public AbstractHiderTaskFactory(IntactManager manager, CyNetworkView view) {
        this.manager = manager;
        this.view = view;
        hideTaskFactory = manager.getService(HideTaskFactory.class);
        unHideTaskFactory = manager.getService(UnHideTaskFactory.class);
    }
}
