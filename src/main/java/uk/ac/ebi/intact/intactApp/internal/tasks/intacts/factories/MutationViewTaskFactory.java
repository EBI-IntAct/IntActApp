package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.MutationViewTask;

public class MutationViewTaskFactory extends AbstractHiderTaskFactory {

    public MutationViewTaskFactory(IntactManager manager,  CyNetworkView view) {
        super(manager, view);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new MutationViewTask(manager, view, hideTaskFactory, unHideTaskFactory));
    }
}
