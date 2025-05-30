package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;

import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class OrthologyViewTask extends AbstractHiderTask {

    public OrthologyViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        if (chosenView != null && chosenView.getType() != NetworkView.Type.ORTHOLOGY) {
            manager.data.viewChanged(NetworkView.Type.ORTHOLOGY, chosenView);
        }
    }
}
