package uk.ac.ebi.intact.intactApp.internal.view;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.Cy2DGraphicLayer;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class StringLayer implements Cy2DGraphicLayer {
    IntactManager manager;
    Rectangle2D bounds;
    BufferedImage image;

    public StringLayer(IntactManager manager, BufferedImage image) {
        this.manager = manager;
        this.image = image;
        if (image != null)
            bounds = new Rectangle2D.Double(0.0, 0.0, image.getWidth(), image.getHeight());
        else
            bounds = new Rectangle2D.Double(0.0, 0.0, 150, 150);
    }

    public void draw(Graphics2D g, Shape shape,
                     CyNetworkView networkView, View<? extends CyIdentifiable> view) {
        if (!(view.getModel() instanceof CyNode)) return;
        CyNetwork network = networkView.getModel();
        CyNode node = (CyNode) view.getModel();
        boolean usePill = ModelUtils.isCompound(network, node);

        Paint fill = view.getVisualProperty(BasicVisualLexicon.NODE_FILL_COLOR);
        Paint background = networkView.getVisualProperty(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
        boolean selected = false;
        if (network.getRow(view.getModel()).get(CyNetwork.SELECTED, Boolean.class))
            selected = true;
        if (usePill) {
            DrawPill ds = new DrawPill((Color) fill, (Color) background, image, shape, selected);
            ds.draw(g, bounds);
        } else {
            DrawSphere ds = new DrawSphere((Color) fill, (Color) background, image, shape, selected);
            ds.draw(g, bounds);
        }
    }

    public Rectangle2D getBounds2D() {
        return bounds;
    }

    public Paint getPaint(Rectangle2D bounds) {
        return null;
    }

    public CustomGraphicLayer transform(AffineTransform xform) {
        final Shape s = xform.createTransformedShape(bounds);
        bounds = s.getBounds2D();

        return this;
    }

}
