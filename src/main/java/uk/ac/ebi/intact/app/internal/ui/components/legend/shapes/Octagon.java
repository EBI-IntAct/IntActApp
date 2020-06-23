package uk.ac.ebi.intact.app.internal.ui.components.legend.shapes;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2009 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */


import java.awt.*;
import java.awt.geom.GeneralPath;


public class Octagon extends AbstractNodeShape {
    private static final float SQRT2 = (float) Math.sqrt(2.0);
    private static final float SQRT2plus2 = 2.0f + SQRT2;
    private Shape shape;


    public Octagon(int width, int height, Color color) {
        super(width, height, color);

        GeneralPath path = new GeneralPath();

        final float xx = width / SQRT2plus2;
        final float xz = xx * SQRT2;

        final float yx = height / SQRT2plus2;
        final float yz = yx * SQRT2;

        path.moveTo(0, 0 + yx);
        path.lineTo(0, 0 + yx + yz);
        path.lineTo(0 + xx, height);
        path.lineTo(0 + xx + xz, height);
        path.lineTo(width, 0 + yx + yz);
        path.lineTo(width, 0 + yx);
        path.lineTo(0 + xx + xz, 0);
        path.lineTo(0 + xx, 0);

        path.closePath();
        shape = path;
    }


    @Override
    protected Shape getShape() {
        return shape;
    }
}

