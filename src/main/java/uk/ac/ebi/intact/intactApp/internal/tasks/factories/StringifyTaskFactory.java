package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.StringifyTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class StringifyTaskFactory extends AbstractNetworkTaskFactory implements TaskFactory {
    final IntactManager manager;

    public StringifyTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork net) {
        if (!manager.haveURIs() || net == null) return false;

        // Are we already a string network?
        return !ModelUtils.isIntactNetwork(net);
    }

    public TaskIterator createTaskIterator(CyNetwork net) {
        return new TaskIterator(new StringifyTask(manager, net));
    }

    public boolean isReady() {
        return true;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new StringifyTask(manager, null));
    }

}

