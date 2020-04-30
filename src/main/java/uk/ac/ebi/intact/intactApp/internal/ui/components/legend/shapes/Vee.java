package uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes;

import java.awt.*;

public final class Vee extends AbstractNodeShape {
    public Vee(int width, int height, Color color) {
        super(width, height, color);
    }

    @Override
    protected Shape getShape() {
        return new Polygon(
                new int[]{0, width / 2, width, width / 2},
                new int[]{0, height / 3, 0, height},
                4);
    }

}

