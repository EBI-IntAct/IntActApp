package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactViewType;
import uk.ac.ebi.intact.intactApp.internal.model.styles.from.model.MutationIntactStyle;

public class MutationViewTask extends AbstractHiderTask {

    public MutationViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        super(manager, hideTaskFactory, unHideTaskFactory);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        IntactNetwork intactNetwork = manager.getIntactNetwork(manager.getCurrentNetwork());
        CyNetworkView view = manager.getCurrentNetworkView();

        if (manager.getNetworkViewType(view) == IntactViewType.COLLAPSED) {
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(view, null, intactNetwork.getCollapsedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(view, null, intactNetwork.getExpandedEdges()));
        }
        if (manager.getNetworkViewType(view) != IntactViewType.MUTATION) {
            manager.applyStyle(MutationIntactStyle.TITLE, view);
            manager.setNetworkViewType(view, IntactViewType.MUTATION);
        }
    }
}
