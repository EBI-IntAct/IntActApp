package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.ui.EnrichmentCytoPanel;

import java.io.File;

public class ExportEnrichmentTableTask extends AbstractTask {

    @Tunable(description = "Save Table as", params = "input=false",
            tooltip = "<html>Note: for convenience spaces are replaced by underscores.</html>", gravity = 2.0)
    public File fileName = null;
    @Tunable(description = "Filtered terms only",
            longDescription = "Save only the enrichment terms after filtering.",
            exampleStringValue = "false", gravity = 3.0)
    public boolean filtered = false;
    private IntactManager manager;
    private EnrichmentCytoPanel enrichmentPanel;
    private CyTable selectedTable;

    public ExportEnrichmentTableTask(IntactManager manager, CyNetwork network, EnrichmentCytoPanel panel, CyTable table) {
        this.manager = manager;
        this.enrichmentPanel = panel;
        this.selectedTable = table;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        ExportTableTaskFactory exportTF = manager.getService(ExportTableTaskFactory.class);

        if (selectedTable != null && fileName != null) {
            File file = fileName;
            if (filtered && enrichmentPanel != null) {
                selectedTable = enrichmentPanel.getFilteredTable();
            }
            taskMonitor.showMessage(TaskMonitor.Level.INFO,
                    "export table " + selectedTable + " to " + file.getAbsolutePath());
            TaskIterator ti = exportTF.createTaskIterator(selectedTable, file);
            insertTasksAfterCurrentTask(ti);
        }
    }

    @ProvidesTitle
    public String getTitle() {
        return "Export STRING Enrichment table";
    }
}
