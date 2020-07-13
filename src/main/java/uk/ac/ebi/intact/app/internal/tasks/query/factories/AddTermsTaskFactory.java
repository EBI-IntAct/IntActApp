package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.query.AddTermsTask;
import uk.ac.ebi.intact.app.internal.utils.ModelUtils;

public class AddTermsTaskFactory extends AbstractNetworkTaskFactory
        implements NetworkViewTaskFactory {
    final Manager manager;

    public AddTermsTaskFactory(final Manager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork network) {
        return ModelUtils.isIntactNetwork(network);
    }

    public TaskIterator createTaskIterator(CyNetwork network) {
        return new TaskIterator(new AddTermsTask(manager, network, null));
    }

    public boolean isReady(CyNetworkView cyView) {
        return ModelUtils.isIntactNetwork(cyView.getModel());
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        return new TaskIterator(new AddTermsTask(manager, netView.getModel(), netView));
    }
}
