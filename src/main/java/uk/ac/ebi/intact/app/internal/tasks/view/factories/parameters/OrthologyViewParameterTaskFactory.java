package uk.ac.ebi.intact.app.internal.tasks.view.factories.parameters;

import lombok.Getter;
import lombok.Setter;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.view.factories.AbstractViewTaskFactory;
import uk.ac.ebi.intact.app.internal.tasks.view.parameters.OrthologyViewParameterTask;

public class OrthologyViewParameterTaskFactory extends AbstractViewTaskFactory {
    private final Manager manager;

    @Setter
    private NetworkView networkView;
    @Setter
    private boolean isParameterApplied;
    @Getter
    @Setter
    private String database;

    public OrthologyViewParameterTaskFactory(Manager manager, NetworkView networkView, boolean isParameterApplied, String database) {
        super(manager, false);
        this.manager = manager;
        this.networkView = networkView;
        this.isParameterApplied = isParameterApplied;
        this.database = database;
    }

    public OrthologyViewParameterTaskFactory(Manager manager, boolean currentNetwork) {
        super(manager, currentNetwork);
        this.manager = manager;
        this.isParameterApplied = true;
        this.networkView = currentNetwork ? manager.data.getCurrentNetworkView() : null;
    }

    @Override
    public TaskIterator createTaskIterator() {
        NetworkView view = networkView != null ? networkView : manager.data.getCurrentNetworkView();
        if (view == null) {
            throw new IllegalStateException("No network view available");
        }
        if (currentView){
            return new TaskIterator(new OrthologyViewParameterTask(manager, currentView));
        }
        return new TaskIterator(new OrthologyViewParameterTask(manager, view, isParameterApplied, database));
    }
}
