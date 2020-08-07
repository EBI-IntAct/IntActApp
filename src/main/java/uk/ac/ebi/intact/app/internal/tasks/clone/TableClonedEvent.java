package uk.ac.ebi.intact.app.internal.tasks.clone;

import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.model.CyTable;

public class TableClonedEvent extends AbstractCyEvent<CloneTableTask> {
    private final CyTable originalTable;
    private final CyTable clonedTable;

    public TableClonedEvent(CloneTableTask source, CyTable originalTable, CyTable clonedTable) {
        super(source, TableClonedListener.class);
        this.originalTable = originalTable;
        this.clonedTable = clonedTable;
    }

    public CyTable getOriginalTable() {
        return originalTable;
    }

    public CyTable getClonedTable() {
        return clonedTable;
    }
}
