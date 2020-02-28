package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.SettingsTask;

public class SettingsTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;

    public SettingsTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new SettingsTask(manager));
    }

}
