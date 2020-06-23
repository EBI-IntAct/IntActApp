package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;


import java.awt.*;
import java.awt.geom.GeneralPath;

public class Hexagon extends AbstractNodeShape {
    private final GeneralPath path;

    public Hexagon(int width, int height, Color color) {
        super(width, height, color);

        double side = Math.min(width, height);

        double x = side / 4.0;                  // horizontal
        double z = x * 2.0;                       // hypotenuse
        double y = z * Math.sin(Math.PI / 3.0);   // vertical

        path = new GeneralPath();

        path.moveTo(0, z);
        path.lineTo(x, z + y);
        path.lineTo(x + z, z + y);
        path.lineTo(side, z);
        path.lineTo(x + z, side -z - y);
        path.lineTo(x, side -z - y);
        path.closePath();
    }


    @Override
    protected Shape getShape() {
        return path;
    }
}

