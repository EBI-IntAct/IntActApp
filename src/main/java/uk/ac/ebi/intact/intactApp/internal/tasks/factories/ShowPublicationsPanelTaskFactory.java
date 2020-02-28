package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowPublicationsPanelTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowPublicationsPanelTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;
    boolean show = false;
    boolean noSignificant = false;

    public ShowPublicationsPanelTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ShowPublicationsPanelTask(manager, this, show, noSignificant));
    }

    public TaskIterator createTaskIterator(boolean show, boolean noSignificant) {
        return new TaskIterator(new ShowPublicationsPanelTask(manager, this, show, noSignificant));
    }

    public void reregister() {
        manager.unregisterService(this, TaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.STRING Enrichment");
        props.setProperty(COMMAND_NAMESPACE, "string");
        if (ShowPublicationsPanelTask.isPanelRegistered(manager)) {
            props.setProperty(TITLE, "Hide publications panel");
            props.setProperty(COMMAND, "hide publications");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Hide the publications panel");
            show = false;
        } else {
            props.setProperty(TITLE, "Show publications panel");
            props.setProperty(COMMAND, "show publications");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Show the publications panel");
            show = true;
        }
        props.setProperty(COMMAND_SUPPORTS_JSON, "true");
        props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
        props.setProperty(MENU_GRAVITY, "5.0");
        props.setProperty(IN_MENU_BAR, "true");
        manager.registerService(this, TaskFactory.class, props);
    }

    public boolean isReady() {
        // We always want to be able to shut it off
        if (!show)
            return true;

        CyNetwork net = manager.getCurrentNetwork();
        if (net == null)
            return false;

        return ModelUtils.isStringNetwork(net);
    }
}
