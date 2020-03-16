package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowResultsPanelTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowResultsPanelTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;
    boolean show = false;

    public ShowResultsPanelTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ShowResultsPanelTask(manager, this, show));
    }

    public void reregister() {
        manager.unregisterService(this, TaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.IntAct");

        if (ShowResultsPanelTask.isPanelRegistered(manager)) {
            props.setProperty(TITLE, "Hide results panel");
            show = false;
        } else {
            props.setProperty(TITLE, "Show results panel");
            show = true;
        }
        props.setProperty(MENU_GRAVITY, "5.0");
        props.setProperty(IN_MENU_BAR, "true");
        props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
        manager.registerService(this, TaskFactory.class, props);
    }

    public boolean isReady() {
        // We always want to be able to shut it off
        if (!show) return true;

        CyNetwork net = manager.getCurrentNetwork();
        if (net == null) return false;

        return ModelUtils.isIntactNetwork(net);
    }
}

