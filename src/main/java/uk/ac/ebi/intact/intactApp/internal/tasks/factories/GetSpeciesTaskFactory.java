package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.GetSpeciesTask;

public class GetSpeciesTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;

    public GetSpeciesTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady() {
        return manager.haveURIs() && true;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new GetSpeciesTask(manager));
    }

}
