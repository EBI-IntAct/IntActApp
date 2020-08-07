package uk.ac.ebi.intact.app.internal.model.events;

import org.cytoscape.event.AbstractCyEvent;
import uk.ac.ebi.intact.app.internal.ui.components.slider.RangeSlider;

public class RangeChangeEvent extends AbstractCyEvent<RangeSlider> {
    public RangeChangeEvent(RangeSlider source) {
        super(source, RangeChangeListener.class);
    }
}
