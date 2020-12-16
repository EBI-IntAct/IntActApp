package uk.ac.ebi.intact.app.internal.tasks.view;

import org.apache.commons.lang3.ArrayUtils;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public abstract class AbstractViewTask extends AbstractTask {
    protected final Manager manager;
    public boolean currentView;
    @Tunable(description = "Network view", longDescription = "Network view to manipulate. If not set, the current one will be used if possible.")
    public ListSingleSelection<NetworkView> view;
    protected CyNetworkView cyView;
    protected Network chosenNetwork;
    protected NetworkView chosenView;

    public AbstractViewTask(Manager manager, boolean currentView) {
        this.manager = manager;
        this.currentView = currentView;
        if (!currentView) {
            view = new ListSingleSelection<>(ArrayUtils.insert(0, manager.data.getViews(), new CurrentView(manager)));
        }
    }

    public AbstractViewTask(Manager manager, NetworkView networkView) {
        this.manager = manager;
        this.currentView = false;
        chosenView = networkView;
    }

    protected void chooseData() {
        if (!currentView) {
            if (chosenView == null) chosenView = view.getSelectedValue();
            if (chosenView instanceof CurrentView) chosenView = manager.data.getCurrentNetworkView();
        } else {
            chosenView = manager.data.getCurrentNetworkView();
        }
        if (chosenView != null) {
            cyView = chosenView.cyView;
            chosenNetwork = chosenView.getNetwork();
        }
    }

    public static class CurrentView extends NetworkView {

        public CurrentView(Manager manager) {
            super(manager, null, false, Type.SUMMARY);
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
