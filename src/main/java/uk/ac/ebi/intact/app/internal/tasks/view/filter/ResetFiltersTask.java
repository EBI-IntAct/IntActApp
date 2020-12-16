package uk.ac.ebi.intact.app.internal.tasks.view.filter;

import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;

public class ResetFiltersTask extends AbstractViewTask {

    public ResetFiltersTask(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    public ResetFiltersTask(Manager manager, NetworkView view) {
        super(manager, view);
    }

    @Override
    public void run(TaskMonitor taskMonitor)  {
        chooseData();
        if (chosenView == null) return;
        chosenView.resetFilters();
    }
}
