package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowImagesTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.Properties;

import static org.cytoscape.work.ServiceProperties.*;

public class ShowImagesTaskFactory extends AbstractNetworkViewTaskFactory implements TaskFactory {
    final IntactManager manager;
    final boolean show;

    public ShowImagesTaskFactory(final IntactManager manager) {
        this.manager = manager;
        this.show = false;
    }

    public ShowImagesTaskFactory(final IntactManager manager, final boolean show) {
        this.manager = manager;
        this.show = show;
    }

    @Override
    public boolean isReady(CyNetworkView netView) {
        if (netView == null) return false;
        return ModelUtils.isStringNetwork(netView.getModel());
    }

    @Override
    public boolean isReady() {
        return true;
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        return new TaskIterator(new ShowImagesTask(manager, netView, this));
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new ShowImagesTask(manager, show, this));
    }

    public void reregister() {
        manager.unregisterService(this, NetworkViewTaskFactory.class);
        Properties props = new Properties();
        props.setProperty(PREFERRED_MENU, "Apps.STRING");
        if (manager.showImage())
            props.setProperty(TITLE, "Don't show structure images");
        else
            props.setProperty(TITLE, "Show structure images");
        props.setProperty(MENU_GRAVITY, "7.0");
        props.setProperty(IN_MENU_BAR, "true");
        props.setProperty(INSERT_SEPARATOR_BEFORE, "true");
        manager.registerService(this, NetworkViewTaskFactory.class, props);
    }
}

