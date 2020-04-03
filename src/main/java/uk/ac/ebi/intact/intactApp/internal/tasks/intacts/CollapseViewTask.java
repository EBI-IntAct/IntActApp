package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.from.model.CollapsedIntactStyle;

public class CollapseViewTask extends AbstractHiderTask {

    public CollapseViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        super(manager, hideTaskFactory, unHideTaskFactory);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        IntactNetwork iNetwork = manager.getIntactNetwork(manager.getCurrentNetwork());
        IntactNetworkView iView = manager.getCurrentIntactNetworkView();
        if (iView.getType() != IntactNetworkView.Type.COLLAPSED) {
            CyNetworkView view = iView.getView();
            manager.applyStyle(CollapsedIntactStyle.TITLE, view);
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(view, null, iNetwork.getExpandedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(view, null, iNetwork.getCollapsedEdges()));

            iView.setType(IntactNetworkView.Type.COLLAPSED);
        }
    }
}
