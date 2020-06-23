package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;


public class RoundedRectangle extends AbstractNodeShape {

    public RoundedRectangle(int width, int height, Color color) {
        super(width, height, color);
    }

    @Override
    protected Shape getShape() {
        final float arcSize = Math.min(width, height) / 4f;
        return new RoundRectangle2D.Float(0, 0, width, height, arcSize, arcSize);
    }
}
