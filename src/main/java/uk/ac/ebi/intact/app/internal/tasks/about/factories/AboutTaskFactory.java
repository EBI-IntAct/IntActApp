package uk.ac.ebi.intact.app.internal.tasks.about.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.about.AboutTask;

public class AboutTaskFactory extends AbstractTaskFactory {
    private Manager manager;

    public AboutTaskFactory(Manager manager) {
        this.manager = manager;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new AboutTask(manager));
    }
}
