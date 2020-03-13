package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.SetConfidenceTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class SetConfidenceTaskFactory extends AbstractNetworkTaskFactory {
    final IntactManager manager;

    public SetConfidenceTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork net) {
        if (!manager.haveURIs() || net == null) return false;

        // Are we already a string network?
        if (ModelUtils.isIntactNetwork(net)) return false;

        return ModelUtils.isMergedIntactNetwork(net);
    }

    public TaskIterator createTaskIterator(CyNetwork net) {
        return new TaskIterator(new SetConfidenceTask(manager, net));
    }

}

