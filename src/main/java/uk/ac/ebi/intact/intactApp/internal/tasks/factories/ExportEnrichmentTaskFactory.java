package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm.TermCategory;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ExportEnrichmentTableTask;
import uk.ac.ebi.intact.intactApp.internal.ui.EnrichmentCytoPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class ExportEnrichmentTaskFactory extends AbstractNetworkTaskFactory {
    // implements ExportTableTaskFactory {

    final CytoPanel cytoPanel;
    EnrichmentCytoPanel panel;
    private IntactManager manager;

    public ExportEnrichmentTaskFactory(IntactManager manager) {
        this.manager = manager;
        CySwingApplication swingApplication = manager.getService(CySwingApplication.class);
        cytoPanel = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
        if (cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Enrichment") > 0) {
            panel = (EnrichmentCytoPanel) cytoPanel.getComponentAt(
                    cytoPanel.indexOfComponent("uk.ac.ebi.intact.intactApp.Enrichment"));
        }
    }

    public boolean isReady(CyNetwork network) {
        return ModelUtils.getEnrichmentTables(manager, network).size() > 0;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        return new TaskIterator(new ExportEnrichmentTableTask(manager, network, panel,
                ModelUtils.getEnrichmentTable(manager, network, TermCategory.ALL.getTable())));
    }

}
