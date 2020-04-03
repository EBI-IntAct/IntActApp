package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.styles.from.model.ExpandedIntactStyle;

public class ExpandViewTask extends AbstractHiderTask {

    public ExpandViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        super(manager, hideTaskFactory, unHideTaskFactory);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        IntactNetwork intactNetwork = manager.getIntactNetwork(manager.getCurrentNetwork());
        IntactNetworkView iView = manager.getCurrentIntactNetworkView();

        if (iView.getType() != IntactNetworkView.Type.EXPANDED) {
            CyNetworkView view = iView.getView();
            manager.applyStyle(ExpandedIntactStyle.TITLE, view);
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(view, null, intactNetwork.getCollapsedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(view, null, intactNetwork.getExpandedEdges()));

            iView.setType(IntactNetworkView.Type.EXPANDED);
        }
    }
}
