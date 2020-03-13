package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowEnhancedLabelsTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowEnhancedLabelsTaskFactory extends AbstractNetworkViewTaskFactory {

    final IntactManager manager;

    public ShowEnhancedLabelsTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetworkView netView) {
        if (netView == null)
            return false;
        return ModelUtils.isIntactNetwork(netView.getModel()) && manager.haveEnhancedGraphics();
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        return new TaskIterator(new ShowEnhancedLabelsTask(manager, netView, this));
    }

    public void reregister() {
        manager.unregisterService(this, NetworkViewTaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.IntAct");
        if (manager.showEnhancedLabels() && manager.haveEnhancedGraphics()) {
            props.setProperty(TITLE, "Don't show STRING style labels");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "hide labels");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Hide the STRING style labels on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Hide the STRING style labels on the nodes");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
        } else if (manager.haveEnhancedGraphics()) {
            props.setProperty(TITLE, "Show STRING style labels");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "show labels");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Show the STRING style labels on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Show the STRING style labels on the nodes");
            props.setProperty(COMMAND_SUPPORTS_JSON, "true");
            props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
        }
        props.setProperty(MENU_GRAVITY, "8.0");
        props.setProperty(IN_MENU_BAR, "true");
        manager.registerService(this, NetworkViewTaskFactory.class, props);
    }
}
