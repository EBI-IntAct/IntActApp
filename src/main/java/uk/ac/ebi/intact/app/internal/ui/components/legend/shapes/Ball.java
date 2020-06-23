package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class Ball extends AbstractNodeShape {

    public Ball(Color color, int diameter) {
        super(diameter, diameter, color);
    }

    @Override
    protected Shape getShape() {
        return new Ellipse2D.Double(0, 0, width, height);
    }
}