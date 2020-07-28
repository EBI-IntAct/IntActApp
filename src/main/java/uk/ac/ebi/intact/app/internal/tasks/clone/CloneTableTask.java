package uk.ac.ebi.intact.app.internal.tasks.clone;

import org.cytoscape.model.*;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CloneTableTask extends AbstractTask {
    public final Manager manager;
    public final CyTable toClone;
    public CyTable clone;
    public CyColumn primaryKey;
    private final static Pattern pattern = Pattern.compile("(?<root>.+) \\((?<counter>\\d+)\\)");
    private CyTableManager tableManager;

    public CloneTableTask(Manager manager, CyTable toClone) {
        this.manager = manager;
        this.toClone = toClone;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        if (toClone == null || manager == null) return;
        primaryKey = toClone.getPrimaryKey();
        tableManager = manager.utils.getService(CyTableManager.class);
        int rowCount = toClone.getRowCount();
        var size = CyTableFactory.InitialTableSize.LARGE;
        if (rowCount < 500) size = CyTableFactory.InitialTableSize.SMALL;
        else if (rowCount < 5000) size = CyTableFactory.InitialTableSize.MEDIUM;

        clone = manager.utils.getService(CyTableFactory.class).createTable(getTitle(), primaryKey.getName(), primaryKey.getType(), toClone.isPublic(), true, size);
        cloneColumns();
        cloneRows();
        tableManager.addTable(clone);
        manager.utils.fireEvent(new TableClonedEvent(this, toClone, clone));
    }

    public String getTitle() {
        Matcher matcher = pattern.matcher(toClone.getTitle());
        String root = matcher.find() ? matcher.group("root") : toClone.getTitle();
        int copyNumber = 0;
        for (CyTable table : tableManager.getAllTables(true)) {
            Matcher tableMatcher = pattern.matcher(table.getTitle());
            if (tableMatcher.find()) {
                copyNumber = Math.max(copyNumber, Integer.parseInt(tableMatcher.group("counter")));
            }
        }
        return String.format("%s (%d)", root, copyNumber + 1);
    }

    private void cloneColumns() {
        for (var col : toClone.getColumns()) {
            if (cancelled)
                return;

            final String name = col.getName();
            if (clone.getColumn(name) == null) {
                cloneColumn(col, clone);
            }
        }
    }


    private void cloneColumn(CyColumn col, CyTable subTable) {
        CyColumn checkCol = subTable.getColumn(col.getName());

        if (checkCol == null) {
            if (List.class.isAssignableFrom(col.getType()))
                subTable.createListColumn(col.getName(), col.getListElementType(), false);
            else
                subTable.createColumn(col.getName(), col.getType(), false);
        }
    }

    private void cloneRows() {
        for (CyRow from : toClone.getAllRows()) {
            cloneRow(from, clone.getRow(from.get(primaryKey.getName(), primaryKey.getType())));
        }
    }

    private void cloneRow(CyRow from, CyRow to) {
        for (CyColumn col : to.getTable().getColumns()) {
            String name = col.getName();
            if (name.equals(CyIdentifiable.SUID))
                continue;
            to.set(name, from.getRaw(name));
        }
    }
}
