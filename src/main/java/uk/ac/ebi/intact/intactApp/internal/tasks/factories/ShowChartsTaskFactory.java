package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ShowChartsTask;
import uk.ac.ebi.intact.intactApp.internal.ui.EnrichmentCytoPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class ShowChartsTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;
    final CytoPanel cytoPanel;
    EnrichmentCytoPanel panel;

    public ShowChartsTaskFactory(final IntactManager manager) {
        this.manager = manager;
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
    }

    public TaskIterator createTaskIterator() {
        if (!isReady())
            return new TaskIterator();
        return new TaskIterator(new ShowChartsTask(manager, panel));
    }

    public boolean isReady() {
        CyNetwork net = manager.getCurrentNetwork();
        if (net == null)
            return false;

        if (!ModelUtils.isIntactNetwork(net))
            return false;

        if (cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Enrichment") < 0) {
            return false;
        }

        panel = (EnrichmentCytoPanel) cytoPanel.getComponentAt(
                cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Enrichment"));
        // System.out.println("panel = "+panel);

        return panel != null;
    }
}
