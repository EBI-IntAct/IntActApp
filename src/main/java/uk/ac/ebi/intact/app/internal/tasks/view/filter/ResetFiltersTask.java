package uk.ac.ebi.intact.app.internal.tasks.view.filter;

import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.AbstractViewTask;

public class ResetFiltersTask extends AbstractViewTask {

    public ResetFiltersTask(Manager manager, boolean currentView) {
        super(manager, currentView);
    }

    @Override
    public void run(TaskMonitor taskMonitor)  {
        chooseData();
        if (chosenView == null) return;
        chosenView.resetFilters();
    }
}
