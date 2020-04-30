package uk.ac.ebi.intact.intactApp.internal.model;

import org.cytoscape.view.model.CyNetworkView;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeChangeEvent;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeChangeListener;
import uk.ac.ebi.intact.intactApp.internal.ui.components.slider.RangeSlider;

public class IntactNetworkView implements RangeChangeListener {
    private IntactManager manager;
    private IntactNetwork network;
    private CyNetworkView view;
    private Type type = Type.COLLAPSED;
    private Range miScoreRange = new Range();

    public IntactNetworkView(IntactManager manager, CyNetworkView view) {
        this.manager = manager;
        this.view = view;
        this.network = manager.getIntactNetwork(view.getModel());
    }

    public IntactNetwork getNetwork() {
        return network;
    }

    public CyNetworkView getView() {
        return view;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Range getMiScoreRange() {
        return new Range(miScoreRange);
    }

    @Override
    public void rangeChanged(RangeChangeEvent event) {
        RangeSlider rangeSlider = event.getRangeSlider();
        miScoreRange.lowerValue = rangeSlider.getValue();
        miScoreRange.upperValue = rangeSlider.getUpperValue();
    }

    @Override
    public String toString() {
        return "IntactNetworkView{" +
                "network=" + network +
                ", type=" + type +
                ", miScoreRange=" + miScoreRange +
                '}';
    }

    public enum Type {
        COLLAPSED("collapsed"),
        EXPANDED("expanded"),
        MUTATION("mutation");

        private String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static class Range {
        public int lowerValue = 0;
        public int upperValue = 100;

        public Range() {
        }

        public Range(Range range) {
            this.lowerValue = range.lowerValue;
            this.upperValue = range.upperValue;
        }

        @Override
        public String toString() {
            return "Range{" +
                    "lowerValue=" + lowerValue +
                    ", upperValue=" + upperValue +
                    '}';
        }
    }
}
