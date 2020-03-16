package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.FilterEnrichmentTableTask;
import uk.ac.ebi.intact.intactApp.internal.ui.EnrichmentCytoPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class FilterEnrichmentTableTaskFactory extends AbstractTaskFactory {
    final IntactManager manager;
    final CytoPanel cytoPanel;
    boolean show = false;
    EnrichmentCytoPanel panel;

    public FilterEnrichmentTableTaskFactory(final IntactManager manager) {
        this.manager = manager;
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new FilterEnrichmentTableTask(manager, panel));
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

        return panel != null;
    }
}
