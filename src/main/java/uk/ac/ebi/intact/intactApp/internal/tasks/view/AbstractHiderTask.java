package uk.ac.ebi.intact.intactApp.internal.tasks.view;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.command.StringToModel;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetworkView;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.view.factories.SelectEdgesTaskFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHiderTask extends AbstractTask {
    protected IntactManager manager;
    protected StringToModel stringToModel;
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;

    public final boolean currentView;
    @Tunable(description = "Network view", longDescription = "Network view to manipulate. If not set, the current one will be used if possible.", dependsOn = "currentView=false")
    public ListSingleSelection<IntactNetworkView> view;
    protected CyNetworkView cyView;
    protected IntactNetwork iNetwork;
    protected IntactNetworkView iView;

    public AbstractHiderTask(IntactManager manager, StringToModel stringToModel, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        this.manager = manager;
        this.stringToModel = stringToModel;
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
        this.currentView = currentView;
        if (!currentView) {
            view = new ListSingleSelection<>(ArrayUtils.insert(0, manager.data.getIntactViews(), new CurrentIntactView(manager)));
        }
    }

    protected void collapseEdgesIfNeeded() {
        chooseData();
        if (iView != null && iView.type != IntactNetworkView.Type.COLLAPSED) {
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
        if (iView != null && iView.type == IntactNetworkView.Type.COLLAPSED) {
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
            super(manager, null);
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
