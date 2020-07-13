package uk.ac.ebi.intact.app.internal.ui.components.slider;

import uk.ac.ebi.intact.app.internal.model.events.RangeChangeEvent;
import uk.ac.ebi.intact.app.internal.model.events.RangeChangeListener;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * An extension of JSlider to select a range of values using two thumb controls.
 * The thumb controls are used to select the lower and upper value of a range
 * with predetermined minimum and maximum values.
 * 
 * <p>Note that RangeSlider makes use of the default BoundedRangeModel, which 
 * supports an inner range defined by a value and an extent.  The upper value
 * returned by RangeSlider is simply the lower value plus the extent.</p>
 */
public class RangeSlider extends JSlider {

    protected RangeChangeEvent rangeChangeEvent = new RangeChangeEvent(this);
    protected boolean fireRangeChanged = true;
    private final List<RangeChangeListener> rangeChangeListeners = new ArrayList<>();

    /**
     * Constructs a RangeSlider with default minimum and maximum values of 0
     * and 100.
     */
    public RangeSlider() {
        initSlider();
    }

    /**
     * Constructs a RangeSlider with the specified default minimum and maximum 
     * values.
     */
    public RangeSlider(int min, int max) {
        super(min, max);
        initSlider();
    }

    /**
     * Initializes the slider by setting default properties.
     */
    private void initSlider() {
        setOrientation(HORIZONTAL);
        setOpaque(true);
        setUI(new RangeSliderUI(this));
    }


    /**
     * Overrides the superclass method to install the UI delegate to draw two
     * thumbs.
     */
    @Override
    public void updateUI() {
        // Update UI for slider labels.  This must be called after updating the
        // UI of the slider.  Refer to JSlider.updateUI().
        updateLabelUIs();
    }

    /**
     * Returns the lower value in the range.
     */
    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * Sets the lower value in the range.
     */
    @Override
    public void setValue(int value) {
        int oldValue = getValue();
        if (oldValue == value) {
            return;
        }

        // Compute new value and extent to maintain upper value.
        int oldExtent = getExtent();
        int newValue = Math.max(getMinimum(), value);
        int newExtent = oldExtent + oldValue - newValue;

        // Set new value and extent, and fire a single change event.
        getModel().setRangeProperties(newValue, newExtent, getMinimum(), 
            getMaximum(), getValueIsAdjusting());
    }

    /**
     * Returns the upper value in the range.
     */
    public int getUpperValue() {
        return getValue() + getExtent();
    }

    /**
     * Sets the upper value in the range.
     */
    public void setUpperValue(int value) {
        // Compute new extent.
        int lowerValue = getValue();
        int newExtent = Math.min(Math.max(0, value - lowerValue), getMaximum() - lowerValue);
        
        // Set extent to set upper value.
        setExtent(newExtent);
    }

    public void addRangeChangeListener(RangeChangeListener listener) {
        rangeChangeListeners.add(listener);
    }

    public void removeRangeChangeListener(RangeChangeListener listener) {
        rangeChangeListeners.remove(listener);
    }

    public void silentRangeChangeEvents() {
        fireRangeChanged = false;
    }

    public void enableRangeChangeEvents () {
        fireRangeChanged = true;
    }


    @Override
    protected void fireStateChanged() {
        if (fireRangeChanged) {
            super.fireStateChanged();
            for (RangeChangeListener listener: rangeChangeListeners) {
                listener.handleRangeChanged(rangeChangeEvent);
            }
        }
    }
}
