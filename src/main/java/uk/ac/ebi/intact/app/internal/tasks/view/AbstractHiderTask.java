package uk.ac.ebi.intact.app.internal.tasks.view;

import org.cytoscape.model.CyEdge;
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
            Set<CyEdge> edgesToSelect = chosenNetwork.getEvidenceEdges().stream()
                    .filter(Edge::isSelected)
                    .map(edge -> chosenNetwork.getSummaryEdge(edge.cyEdge).cyEdge)
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getEvidenceCyEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getSummaryCyEdges()));
            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
        }
    }

    protected void expandEdgesIfNeeded() {
        chooseData();
        if (chosenView != null && chosenView.getType() == NetworkView.Type.SUMMARY) {
            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<CyEdge> edgesToSelect = chosenNetwork.getSummaryEdges().stream()
                    .filter(Edge::isSelected)
                    .map((SummaryEdge edge) -> chosenNetwork.getSimilarEvidenceCyEdges(edge.cyEdge))
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());
            insertTasksAfterCurrentTask(hideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getSummaryCyEdges()));
            insertTasksAfterCurrentTask(unHideTaskFactory.createTaskIterator(cyView, null, chosenNetwork.getEvidenceCyEdges()));
            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
        }
    }
}
