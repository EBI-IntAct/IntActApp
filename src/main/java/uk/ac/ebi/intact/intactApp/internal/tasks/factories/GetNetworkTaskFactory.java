package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.*;

public class GetNetworkTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;

    public GetNetworkTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ProteinQueryTask(manager));
    }

    public boolean isReady() {
        return manager.haveURIs();
    }
}

