package uk.ac.ebi.intact.app.internal.tasks.details.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.tasks.details.ShowDetailPanelTask;
import uk.ac.ebi.intact.app.internal.model.core.managers.Manager;
import uk.ac.ebi.intact.app.internal.utils.tables.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowDetailPanelTaskFactory extends AbstractTaskFactory {
    final Manager manager;
    boolean show = false;

    public ShowDetailPanelTaskFactory(final Manager manager) {
        this.manager = manager;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ShowDetailPanelTask(manager, this, show));
    }

    public void reregister() {
        manager.utils.unregisterService(this, TaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.IntAct");

        if (ShowDetailPanelTask.isPanelRegistered(manager)) {
            props.setProperty(TITLE, "Hide results panel");
            show = false;
        } else {
            props.setProperty(TITLE, "Show results panel");
            show = true;
        }
        props.setProperty(MENU_GRAVITY, "5.0");
        props.setProperty(IN_MENU_BAR, "true");
        props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
        manager.utils.registerService(this, TaskFactory.class, props);
    }

    public boolean isReady() {
        // We always want to be able to shut it off
        if (!show) return true;

        CyNetwork net = manager.data.getCurrentCyNetwork();
        if (net == null) return false;

        return ModelUtils.isIntactNetwork(net);
    }
}

