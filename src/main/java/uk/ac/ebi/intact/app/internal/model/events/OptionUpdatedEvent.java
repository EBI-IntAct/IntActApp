package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;

public class OptionUpdatedEvent extends AbstractCyEvent<OptionManager.Option<?>> {
    public OptionUpdatedEvent(OptionManager.Option source) {
        super(source, OptionUpdatedListener.class);
    }
}
