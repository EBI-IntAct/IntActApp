package uk.ac.ebi.intact.app.internal.tasks.view.filter;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SelectEdgesTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.SelectNodesTaskFactory;

import java.util.Set;
import java.util.stream.Collectors;

public class ApplyFiltersTask extends AbstractViewTask {

    public ApplyFiltersTask(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor)  {
        chooseData();
        if (chosenView != null && chosenNetwork != null) {

            CyNetwork cyNetwork = chosenNetwork.getCyNetwork();
            Set<Long> edgesToSelect;
            if (chosenView.getType() == NetworkView.Type.SUMMARY) {
                edgesToSelect = chosenNetwork.getVisibleSummaryCyEdges().stream()
                        .map(CyIdentifiable::getSUID)
                        .collect(Collectors.toSet());
            } else {
                edgesToSelect = chosenNetwork.getVisibleEvidenceCyEdges().stream()
                        .map(CyIdentifiable::getSUID)
                        .collect(Collectors.toSet());
            }
            Set<Long> nodesToSelect = chosenNetwork.getVisibleNodes().stream()
                    .map(node -> node.cyNode.getSUID())
                    .collect(Collectors.toSet());

            insertTasksAfterCurrentTask(new SelectEdgesTaskFactory(cyNetwork, edgesToSelect).createTaskIterator());
            insertTasksAfterCurrentTask(new SelectNodesTaskFactory(cyNetwork, nodesToSelect).createTaskIterator());
        }
    }
}
