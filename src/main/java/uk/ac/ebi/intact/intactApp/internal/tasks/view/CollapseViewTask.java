package uk.ac.ebi.intact.intactApp.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;

public class CollapseViewTask extends AbstractHiderTask {

    public CollapseViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        collapseEdgesIfNeeded();
        if (iView != null && iView.getType() != IntactNetworkView.Type.COLLAPSED) {
            manager.data.intactViewChanged(IntactNetworkView.Type.COLLAPSED, iView);
        }
    }
}
