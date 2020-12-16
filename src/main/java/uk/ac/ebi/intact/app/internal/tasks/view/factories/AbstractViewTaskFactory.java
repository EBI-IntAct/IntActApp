package uk.ac.ebi.intact.app.internal.tasks.view.factories;

import org.cytoscape.work.AbstractTaskFactory;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public abstract class AbstractViewTaskFactory extends AbstractTaskFactory {
    final protected Manager manager;
    final protected boolean currentView;

    public AbstractViewTaskFactory(Manager manager, boolean currentView) {
        this.manager = manager;
        this.currentView = currentView;
    }
}
