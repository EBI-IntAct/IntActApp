package uk.ac.ebi.intact.app.internal.model.managers.sub.managers.color.settings.events;

import org.cytoscape.event.CyListener;

public interface ColorSettingLoadedListener extends CyListener {
     void handleEvent(ColorSettingLoadedEvent e);
}
