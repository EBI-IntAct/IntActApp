package uk.ac.ebi.intact.intactApp.internal.tasks.intacts.factories;


import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.intacts.CollapseViewTask;

public class CollapseViewTaskFactory extends AbstractHiderTaskFactory {

    public CollapseViewTaskFactory(IntactManager manager, CyNetworkView view) {
        super(manager, view);
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new CollapseViewTask(manager, view,hideTaskFactory, unHideTaskFactory));
    }
}
