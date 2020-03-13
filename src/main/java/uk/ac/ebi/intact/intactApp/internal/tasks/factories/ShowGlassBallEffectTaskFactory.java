package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowGlassBallEffectTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowGlassBallEffectTaskFactory extends AbstractNetworkViewTaskFactory {

    final IntactManager manager;

    public ShowGlassBallEffectTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetworkView netView) {
        if (netView == null)
            return false;
        return ModelUtils.isIntactNetwork(netView.getModel());
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        return new TaskIterator(new ShowGlassBallEffectTask(manager, netView, this));
    }

    public void reregister() {
        manager.unregisterService(this, NetworkViewTaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.IntAct");
        if (manager.showGlassBallEffect()) {
            props.setProperty(TITLE, "Disable STRING glass balls effect");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "hide glass");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Hide the STRING glass ball effect on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Hide the STRING glass ball effect on the nodes");
        } else {
            props.setProperty(TITLE, "Enable STRING glass balls effect");
            props.setProperty(COMMAND_NAMESPACE, "string");
            props.setProperty(COMMAND, "show glass");
            props.setProperty(COMMAND_DESCRIPTION,
                    "Show the STRING glass ball effect on the nodes");
            props.setProperty(COMMAND_LONG_DESCRIPTION,
                    "Show the STRING glass ball effect on the nodes");
        }
        props.setProperty(COMMAND_SUPPORTS_JSON, "true");
        props.setProperty(COMMAND_EXAMPLE_JSON, "{}");
        props.setProperty(MENU_GRAVITY, "9.0");
        props.setProperty(IN_MENU_BAR, "true");
        manager.registerService(this, NetworkViewTaskFactory.class, props);
    }
}
