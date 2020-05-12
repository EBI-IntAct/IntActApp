package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.AddTermsTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.RequeryTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import javax.swing.*;

public class AddTermsTaskFactory extends AbstractNetworkTaskFactory
        implements NetworkViewTaskFactory {
    final IntactManager manager;

    public AddTermsTaskFactory(final IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork network) {
        return manager.haveURIs() && ModelUtils.isIntactNetwork(network);
    }

    public TaskIterator createTaskIterator(CyNetwork network) {
        // check if we have a current STRING network and if not, notify user and ask to requery
        if (!ModelUtils.isCurrentDataVersion(network) && JOptionPane.showConfirmDialog(null,
                ModelUtils.REQUERY_MSG_USER, ModelUtils.REQUERY_TITLE, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            return new TaskIterator(new RequeryTask(manager, network));
        } else {
            return new TaskIterator(new AddTermsTask(manager, network, null));
        }
    }

    public boolean isReady(CyNetworkView netView) {
        return manager.haveURIs() && ModelUtils.isIntactNetwork(netView.getModel());
    }

    public TaskIterator createTaskIterator(CyNetworkView netView) {
        // check if we have a current STRING network and if not, notify user and ask to requery
        if (!ModelUtils.isCurrentDataVersion(netView.getModel()) && JOptionPane.showConfirmDialog(null,
                ModelUtils.REQUERY_MSG_USER, ModelUtils.REQUERY_TITLE, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION) {
            return new TaskIterator(new RequeryTask(manager, netView.getModel()));
        } else {
            return new TaskIterator(new AddTermsTask(manager, netView.getModel(), netView));
        }
    }
}
