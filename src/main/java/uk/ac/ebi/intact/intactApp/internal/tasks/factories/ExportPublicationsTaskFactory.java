package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.EnrichmentTerm.TermCategory;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.ExportEnrichmentTableTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class ExportPublicationsTaskFactory extends AbstractNetworkTaskFactory {
    // implements ExportTableTaskFactory {

    private IntactManager manager;

    public ExportPublicationsTaskFactory(IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork network) {
        return ModelUtils.getEnrichmentTables(manager, network).size() > 0;
    }

    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
        return new TaskIterator(new ExportEnrichmentTableTask(manager, network, null,
                ModelUtils.getEnrichmentTable(manager, network, TermCategory.PMID.getTable())));
    }

}
