package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.write.ExportTableTaskFactory;
import org.cytoscape.work.*;
import org.cytoscape.work.util.ListMultipleSelection;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ExportEnrichmentTask extends AbstractTask {

    @Tunable(description = "Select tables to export", gravity = 1.0)
    public ListMultipleSelection<String> availableTables = new ListMultipleSelection<>();
    @Tunable(description = "Save Table(s) as", params = "input=false",
            tooltip = "<html>For multiple tables, the table type will be appended to the " +
                    "full path.  For example, <i>/tmp/foo_</i> will become <i>/tmp/foo_InterPro</i>.<br/> " +
                    "If only one table is to be exported, this field will be used as full file name.<br/>" +
                    "Note: for convenience spaces are replaced by underscores.</html>", gravity = 2.0)
    // public String name = "test";
    public File prefix = null;
    private IntactManager manager;
    private CyNetwork network;
    private Set<CyTable> enrichmentTables;

    public ExportEnrichmentTask(IntactManager manager, CyNetwork network) {
        this.manager = manager;
        this.network = network;
        getAvailableTables();
        // file = new File("test.csv");
    }

    public ExportEnrichmentTask(IntactManager manager, CyNetwork network, CyTable table,
                                File file) {
        this.manager = manager;
        this.network = network;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        ExportTableTaskFactory exportTF = manager.getService(ExportTableTaskFactory.class);
        List<String> selectedTables = availableTables.getSelectedValues();
        if (selectedTables != null && selectedTables.size() > 0 && prefix != null) {
            for (String selectedTable : selectedTables) {
                File file;
                if (selectedTables.size() > 1)
                    file = new File(prefix.getAbsolutePath() + trimmedTable(selectedTable));
                else
                    file = prefix;
                for (CyTable enTable : enrichmentTables) {
                    if (enTable.getTitle().equals(selectedTable)) {
                        taskMonitor.showMessage(TaskMonitor.Level.INFO,
                                "export table " + selectedTable + " to " + file.getAbsolutePath());
                        TaskIterator ti = exportTF.createTaskIterator(enTable, file);
                        insertTasksAfterCurrentTask(ti);
                    }
                }
            }
        }
    }

    private String trimmedTable(String table) {
        return table.substring("STRING Enrichment: ".length()).replace(' ', '_');
    }

    private void getAvailableTables() {
        enrichmentTables = ModelUtils.getEnrichmentTables(this.manager, this.network);
        List<String> enrichmentTableNames = new ArrayList<>();
        for (CyTable table : enrichmentTables) {
            enrichmentTableNames.add(table.getTitle());
        }
        Collections.sort(enrichmentTableNames);
        availableTables = new ListMultipleSelection<>(enrichmentTableNames);
        if (enrichmentTableNames.size() > 0)
            availableTables.setSelectedValues(Collections.singletonList(enrichmentTableNames.get(0)));

    }

    @ProvidesTitle
    public String getTitle() {
        return "Export STRING Enrichment tables";
    }
}
