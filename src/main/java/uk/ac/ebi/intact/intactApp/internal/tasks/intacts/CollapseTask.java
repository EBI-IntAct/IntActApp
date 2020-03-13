package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.styles.from.webservice.CollapsedIntactWebserviceStyle;

public class CollapseTask extends AbstractTask {
    private IntactManager manager;
    private HideTaskFactory hideTaskFactory;
    private UnHideTaskFactory unHideTaskFactory;

    public CollapseTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        this.manager = manager;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        IntactNetwork intactNetwork = manager.getIntactNetwork(manager.getCurrentNetwork());
        System.out.println(manager.getCurrentNetwork());
        System.out.println(intactNetwork);
        CyNetworkView networkView = manager.getCurrentNetworkView();
        System.out.println(manager.getCurrentNetworkView());
        if (!intactNetwork.isCollapsed()) {
            intactNetwork.setCollapsed(true);
            manager.applyStyle(CollapsedIntactWebserviceStyle.TITLE, networkView);
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(networkView, null, intactNetwork.getExpendedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(networkView, null, intactNetwork.getCollapsedEdges()));
        }
    }
}
