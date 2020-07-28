package uk.ac.ebi.intact.app.internal.tasks.clone.factories;

import org.cytoscape.model.CyTable;
import org.cytoscape.task.AbstractTableTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.clone.CloneTableTask;

public class CloneTableTaskFactory extends AbstractTableTaskFactory {
    private final Manager manager;

    public CloneTableTaskFactory(Manager manager) {
        this.manager = manager;
    }

    @Override
    public TaskIterator createTaskIterator(CyTable table) {
        return new TaskIterator(new CloneTableTask(manager, table));
    }
}
