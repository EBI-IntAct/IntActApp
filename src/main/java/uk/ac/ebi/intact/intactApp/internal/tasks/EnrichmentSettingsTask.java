package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.factories.ShowChartsTaskFactory;

public class EnrichmentSettingsTask extends AbstractTask {

    @ContainsTunables
    public EnrichmentSettings enrichmentSettings;
    @Tunable(description = "Make these settings the default",
            longDescription = "Unless this is set to true, these settings only apply to the current network",
            tooltip = "<html>Unless this is set to true, these settings only apply to the current network.</html>")
    public boolean makeDefault = false;
    private IntactManager manager;
    private CyNetwork network;

    public EnrichmentSettingsTask(IntactManager manager) {
        this.network = manager.getCurrentNetwork();
        this.manager = manager;
        enrichmentSettings = new EnrichmentSettings(manager, network);
    }

    @Override
    public void run(TaskMonitor arg0) throws Exception {
        if (makeDefault) {
            manager.setTopTerms(null, enrichmentSettings.nTerms.getValue());
            manager.setOverlapCutoff(null, enrichmentSettings.overlapCutoff.getValue());
            manager.setEnrichmentPalette(null, enrichmentSettings.defaultPalette.getSelectedValue());
            manager.setChartType(null, enrichmentSettings.chartType.getSelectedValue());
            manager.updateSettings();
        }

        manager.setTopTerms(network, enrichmentSettings.nTerms.getValue());
        manager.setOverlapCutoff(network, enrichmentSettings.overlapCutoff.getValue());
        manager.setEnrichmentPalette(network, enrichmentSettings.defaultPalette.getSelectedValue());
        manager.setChartType(network, enrichmentSettings.chartType.getSelectedValue());

        // TODO: maybe this is a way to automatically apply settings?
        TaskManager<?, ?> tm = (TaskManager<?, ?>) manager.getService(TaskManager.class);
        tm.execute(new ShowChartsTaskFactory(manager).createTaskIterator());
    }

    @ProvidesTitle
    public String getTitle() {
        return "Network-specific settings for STRING Enrichment table";
    }
}
