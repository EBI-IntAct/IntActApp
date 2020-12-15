package uk.ac.ebi.intact.app.internal.tasks.feedback.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.feedback.FeedbackTask;

public class FeedbackTaskFactory extends AbstractTaskFactory {
    private Manager manager;

    public FeedbackTaskFactory(Manager manager) {
        this.manager = manager;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return new TaskIterator(new FeedbackTask(manager));
    }
}
