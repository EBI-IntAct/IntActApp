package uk.ac.ebi.intact.intactApp.internal.tasks.intacts;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories.SelectEdgesTaskFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MutationViewTask extends AbstractHiderTask {

    public MutationViewTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory) {
        super(manager, hideTaskFactory, unHideTaskFactory);
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        IntactNetwork iNetwork = manager.getIntactNetwork(manager.getCurrentNetwork());
        IntactNetworkView iView = manager.getCurrentIntactNetworkView();
        CyNetworkView view = iView.getView();
        if (iView.getType() == IntactNetworkView.Type.COLLAPSED) {
            CyNetwork network = iNetwork.getNetwork();
            Set<CyEdge> edgesToSelect = iNetwork.getCollapsedEdges().stream()
                    .filter(cyEdge -> network.getRow(cyEdge).get(CyNetwork.SELECTED, Boolean.class))
                    .map(iNetwork::getEvidenceEdges)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(network, edgesToSelect).createTaskIterator());
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(view, null, iNetwork.getCollapsedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(view, null, iNetwork.getExpandedEdges()));
        }
        if (iView.getType() != IntactNetworkView.Type.MUTATION) {
            manager.intactViewTypeChanged(IntactNetworkView.Type.MUTATION, iView);
        }
    }
}
