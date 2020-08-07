package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class EvidenceViewTask extends AbstractHiderTask {
    public EvidenceViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, hideTaskFactory, unHideTaskFactory, currentView);
    }

    public EvidenceViewTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, NetworkView networkView) {
        super(manager, hideTaskFactory, unHideTaskFactory, networkView);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        expandEdgesIfNeeded();
        if (chosenView != null && chosenView.getType() != NetworkView.Type.EVIDENCE) {
            manager.data.viewChanged(NetworkView.Type.EVIDENCE, chosenView);
        }
    }
}
