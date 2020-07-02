package uk.ac.ebi.intact.app.internal.ui.panels.filters;

import uk.ac.ebi.intact.app.internal.model.core.elements.Element;
import uk.ac.ebi.intact.app.internal.model.filters.edge.EdgeMIScoreFilter;
import uk.ac.ebi.intact.app.internal.ui.components.slider.MIScoreSliderUI;
import uk.ac.ebi.intact.app.internal.model.events.RangeChangeEvent;
import uk.ac.ebi.intact.app.internal.model.events.RangeChangeListener;
import uk.ac.ebi.intact.app.internal.model.filters.ContinuousFilter;
import uk.ac.ebi.intact.app.internal.ui.components.slider.RangeSlider;

import javax.swing.*;
import java.awt.*;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;

public class ContinuousFilterPanel<T extends Element> extends FilterPanel<ContinuousFilter<T>> implements RangeChangeListener {
    private final RangeSlider slider = new RangeSlider(0, 100);

    public ContinuousFilterPanel(ContinuousFilter<T> filter) {
        super(filter);
        content.add(slider, layoutHelper.down().noExpand());
        content.add(Box.createHorizontalGlue(), layoutHelper.right().expandHoriz());
        if (filter instanceof EdgeMIScoreFilter) {
            slider.setUI(new MIScoreSliderUI(slider));
            expand();
        }
        slider.setForeground(Color.LIGHT_GRAY);
        slider.setBackground(lightBackground);
        slider.addRangeChangeListener(this);
        setupSlider();
    }

    public void updateFilter(ContinuousFilter<T> filter) {
        setupSlider();
    }


    private double slope;
    private double intercept;
    private boolean eventActivated = true;

    public void setupSlider() {
        intercept = filter.getMin();
        slope = (filter.getMax() - filter.getMin()) / 100.0;
        eventActivated = false;
        slider.setValue(getPositionOnSlider(filter.getCurrentMin()));
        slider.setUpperValue(getPositionOnSlider(filter.getCurrentMax()));
        eventActivated = true;
    }

    private int getPositionOnSlider(double value) {
        return (int) Math.round((value - intercept) / slope);
    }

    private double getValue(int positionOnSlider) {
        return slope * positionOnSlider + intercept;
    }

    @Override
    public void rangeChanged(RangeChangeEvent event) {
        if (eventActivated) filter.setCurrentPositions(getValue(slider.getValue()), getValue(slider.getUpperValue()));
    }
}
