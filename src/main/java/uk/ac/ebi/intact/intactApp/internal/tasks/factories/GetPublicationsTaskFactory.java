package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetEnrichmentTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetEnrichmentTaskSwing;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.List;

public class GetPublicationsTaskFactory extends AbstractNetworkTaskFactory implements NetworkViewTaskFactory {
    public static String EXAMPLE_JSON = GetEnrichmentTask.EXAMPLE_JSON_PUBL;
    final IntactManager manager;
    // ShowEnrichmentPanelTaskFactory showEnrichmentFactory;
    ShowPublicationsPanelTaskFactory showFactoryPubl;
    boolean hasGUI = false;

    public GetPublicationsTaskFactory(final IntactManager manager, boolean hasGUI) {
        this.manager = manager;
        // showEnrichmentFactory = null;
        showFactoryPubl = null;
        this.hasGUI = hasGUI;
    }

    public boolean isReady(CyNetwork network) {
        if (manager.haveURIs() && ModelUtils.isIntactNetwork(network)) {
            List<String> netSpecies = ModelUtils.getEnrichmentNetSpecies(network);
            return netSpecies.size() > 0;
        }
        return false;
    }

    public TaskIterator createTaskIterator(CyNetwork network) {
        if (hasGUI) {
            return new TaskIterator(new GetEnrichmentTaskSwing(manager, network, null, null, showFactoryPubl, true));
        } else {
            return new TaskIterator(new GetEnrichmentTask(manager, network, null, null, showFactoryPubl, true));
        }
    }

    public boolean isReady(CyNetworkView netView) {
        if (manager.haveURIs() && ModelUtils.isIntactNetwork(netView.getModel())) {
            List<String> netSpecies = ModelUtils.getEnrichmentNetSpecies(netView.getModel());
            return netSpecies.size() > 0;
        }
        return false;
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        if (hasGUI) {
            return new TaskIterator(new GetEnrichmentTaskSwing(manager, netView.getModel(), netView, null, showFactoryPubl, true));
        } else {
            return new TaskIterator(new GetEnrichmentTask(manager, netView.getModel(), netView, null, showFactoryPubl, true));
        }
    }

    public void setShowPublicationsPanelFactory(ShowPublicationsPanelTaskFactory showFactory) {
        this.showFactoryPubl = showFactory;
    }
}
