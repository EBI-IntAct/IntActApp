package uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.ColorSettingManager;

public class ColorSettingLoadedEvent extends AbstractCyEvent<ColorSettingManager> {

    public ColorSettingLoadedEvent(ColorSettingManager source) {
        super(source, ColorSettingLoadedListener.class);
    }
}
