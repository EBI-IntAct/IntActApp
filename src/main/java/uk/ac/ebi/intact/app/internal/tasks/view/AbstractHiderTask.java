package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.hide.HideTaskFactory;
import org.cytoscape.task.hide.UnHideTaskFactory;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.Edge;
import uk.ac.ebi.intact.app.internal.model.core.elements.edges.SummaryEdge;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SelectEdgesTaskFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractHiderTask extends AbstractViewTask {
    protected HideTaskFactory hideTaskFactory;
    protected UnHideTaskFactory unHideTaskFactory;


    public AbstractHiderTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, boolean currentView) {
        super(manager, currentView);
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
    }

    public AbstractHiderTask(Manager manager, HideTaskFactory hideTaskFactory, UnHideTaskFactory unHideTaskFactory, NetworkView networkView) {
        super(manager, networkView);
        this.hideTaskFactory = hideTaskFactory;
        this.unHideTaskFactory = unHideTaskFactory;
    }

    protected void collapseEdgesIfNeeded() {
        chooseData();
        if (chosenView != null && chosenView.getType() != NetworkView.Type.SUMMARY) {
            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<Long> edgesToSelect = chosenNetwork.getEvidenceEdges().stream()
                    .filter(Edge::isSelected)
                    .map(edge -> chosenNetwork.getSummaryEdge(edge.cyEdge))
                    .filter(edge -> chosenNetwork.getVisibleSummaryEdges().contains(edge))
                    .map(edge -> edge.cyEdge.getSUID())
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
            manager.data.viewChanged(NetworkView.Type.SUMMARY, chosenView);
        }
    }

    protected void expandEdgesIfNeeded(NetworkView.Type newViewType) {
        chooseData();
        if (chosenView != null && chosenView.getType() != newViewType) {
            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<Long> edgesToSelect = chosenNetwork.getSummaryEdges().stream()
                    .filter(Edge::isSelected)
                    .map((SummaryEdge edge) -> chosenNetwork.getSimilarEvidenceCyEdges(edge.cyEdge))
                    .flatMap(List::stream)
                    .filter(edge -> chosenNetwork.getVisibleEvidenceEdges().contains(chosenNetwork.getEvidenceEdge(edge)))
                    .map(CyIdentifiable::getSUID)
                    .collect(Collectors.toSet());
            manager.utils.info("edgesToSelect = " + edgesToSelect.size());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
            manager.data.viewChanged(newViewType, chosenView);
        }
    }
}
