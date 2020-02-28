package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.AddNamespacesTask;

import java.util.List;
import java.util.Set;

public class AddNamespacesTaskFactory extends AbstractTaskFactory implements TaskFactory {
    final IntactManager manager;

    public AddNamespacesTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(List<CyNetwork> networks) {
        return manager.haveURIs() && networks.size() != 0;
    }

    public TaskIterator createTaskIterator(Set<CyNetwork> networks) {
        return new TaskIterator(new AddNamespacesTask(manager, networks));
    }


    public boolean isReady() {
        return true;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new AddNamespacesTask(manager));
    }

}

