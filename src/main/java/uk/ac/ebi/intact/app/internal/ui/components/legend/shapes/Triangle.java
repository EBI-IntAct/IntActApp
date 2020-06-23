package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;

import java.awt.*;

public class Triangle extends AbstractNodeShape {

    public Triangle(int width, int height, Color color) {
        super(width, height, color);
    }

    @Override
    protected Shape getShape() {
        return new Polygon(
                new int[]{width / 2, width, 0},
                new int[]{0, height, height},
                3);
    }
}
