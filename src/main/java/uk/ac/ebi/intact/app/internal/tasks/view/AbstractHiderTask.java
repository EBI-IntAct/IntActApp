package uk.ac.ebi.intact.app.internal.tasks.view;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import uk.ac.ebi.intact.app.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SelectEdgesTaskFactory;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.model.managers.IntactManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHiderTask extends AbstractTask {
    protected IntactManager manager;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public final boolean currentView;
    @Tunable(description = "Network view", longDescription = "Network view to manipulate. If not set, the current one will be used if possible.", dependsOn = "currentView=false")
    public ListSingleSelection<IntactNetworkView> view;
    protected CyNetworkView cyView;
    protected IntactNetwork iNetwork;
    protected IntactNetworkView iView;

    public AbstractHiderTask(IntactManager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        this.manager = manager;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
        this.currentView = currentView;
        if (!currentView) {
            view = new ListSingleSelection<>(ArrayUtils.insert(0, manager.data.getIntactViews(), new CurrentIntactView(manager)));
        }
    }

    protected void collapseEdgesIfNeeded() {
        chooseData();
        if (iView != null && iView.getType() != IntactNetworkView.Type.COLLAPSED) {
            CyNetwork network = iNetwork.getNetwork();
            Set<CyEdge> edgesToSelect = iNetwork.getExpandedEdges().stream()
                    .filter(cyEdge -> network.getRow(cyEdge).get(CyNetwork.SELECTED, Boolean.class))
                    .map(iNetwork::getCollapsedEdge)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(network, edgesToSelect).createTaskIterator());
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, iNetwork.getExpandedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, iNetwork.getCollapsedEdges()));
        }
    }

    protected void expandEdgesIfNeeded() {
        chooseData();
        if (iView != null && iView.getType() == IntactNetworkView.Type.COLLAPSED) {
            CyNetwork network = iNetwork.getNetwork();
            Set<CyEdge> edgesToSelect = iNetwork.getCollapsedEdges().stream()
                    .filter(cyEdge -> network.getRow(cyEdge).get(CyNetwork.SELECTED, Boolean.class))
                    .map(iNetwork::getEvidenceEdges)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(network, edgesToSelect).createTaskIterator());
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, iNetwork.getCollapsedEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, iNetwork.getExpandedEdges()));
        }
    }

    private void chooseData() {
        if (!currentView) {
            iView = view.getSelectedValue();
            if (iView instanceof CurrentIntactView) iView = manager.data.getCurrentIntactNetworkView();
        } else {
            iView = manager.data.getCurrentIntactNetworkView();
        }
        if (iView != null) {
            cyView = iView.view;
            iNetwork = iView.network;
        }
    }

    private static class CurrentIntactView extends IntactNetworkView {

        public CurrentIntactView(IntactManager manager) {
            super(manager, null, false);
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
