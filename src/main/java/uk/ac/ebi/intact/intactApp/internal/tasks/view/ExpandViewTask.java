package uk.ac.ebi.intact.intactApp.internal.tasks.view;

import org.cytoscape.command.StringToModel;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;

public class ExpandViewTask extends AbstractHiderTask {
    public ExpandViewTask(IntactManager manager, StringToModel stringToModel, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, stringToModel, hideTaskFactory, unHideTaskFactory, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        expandEdgesIfNeeded();
        if (iView != null && iView.type != IntactNetworkView.Type.EXPANDED) {
            manager.data.intactViewTypeChanged(IntactNetworkView.Type.EXPANDED, iView);
        }
    }
}
