package uk.ac.ebi.intact.app.internal.model.events;

import lombok.Getter;
import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class OrthologyDatabaseUpdatedEvent extends AbstractCyEvent<Manager> {
    @Getter
    private final String newOrthologyDatabase;

    public OrthologyDatabaseUpdatedEvent(Manager source, String newOrthologyDatabase) {
        super(source, OrthologyDatabaseUpdatedListener.class);
        this.newOrthologyDatabase = newOrthologyDatabase;
    }
}
