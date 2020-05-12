package uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes;

import java.awt.*;

public class Diamond extends AbstractNodeShape {

    public Diamond(int width, int height, Color color) {
        super(width, height, color);
    }

    @Override
    protected Shape getShape() {
        return new Polygon(
                new int[]{width / 2, width, width / 2, 0},
                new int[]{0, height / 2, height, height / 2},
                4);
    }
}

