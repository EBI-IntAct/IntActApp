package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

public abstract class AbstractHiderTask extends AbstractTask {
    protected IntactManager manager;

    @Tunable
    protected CyNetworkView view;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public AbstractHiderTask(IntactManager manager, CyNetworkView view, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        this.manager = manager;
        this.view = (view == null) ? manager.getCurrentNetworkView() : view;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
    }
}
