package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;

public class CollapseViewTask extends AbstractHiderTask {

    public CollapseViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        collapseEdgesIfNeeded();
        if (chosenView != null && chosenView.getType() != NetworkView.Type.SUMMARY) {
            manager.data.intactViewChanged(NetworkView.Type.SUMMARY, chosenView);
        }
    }
}
