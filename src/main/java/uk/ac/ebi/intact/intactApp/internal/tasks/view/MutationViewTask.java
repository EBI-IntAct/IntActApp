package uk.ac.ebi.intact.intactApp.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;

public class MutationViewTask extends AbstractHiderTask {
    public MutationViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        expandEdgesIfNeeded();
        if (iView != null && iView.getType() != IntactNetworkView.Type.MUTATION) {
            manager.data.intactViewChanged(IntactNetworkView.Type.MUTATION, iView);
        }
    }
}
