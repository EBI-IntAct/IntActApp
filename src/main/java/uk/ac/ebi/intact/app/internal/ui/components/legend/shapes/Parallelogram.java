package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;

import java.awt.*;
import java.awt.geom.GeneralPath;

public class Parallelogram extends AbstractNodeShape {
    private final GeneralPath path;

    public Parallelogram(int width, int height, Color color) {
        super(width, height, color);
        path = new GeneralPath();

        path.moveTo(0, 0);

        path.lineTo((2 * width) / 3.0f, 0);
        path.lineTo(width, height);
        path.lineTo((width) / 3.0f, height);

        path.closePath();
    }

    @Override
    protected Shape getShape() {
        return path;
    }
}
