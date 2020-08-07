package uk.ac.ebi.intact.app.internal.ui.utils;

import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.legend.shapes.*;

import java.awt.*;

public class StyleUtils {

    public static AbstractNodeShape nodeTypeToShape(String nodeType, Color color, int size) {
        NodeShape nodeShape = StyleMapper.nodeTypeToShape.get(nodeType);

        if (nodeShape == NodeShapeVisualProperty.TRIANGLE) {
            return new Triangle(size, size, color);
        } else if (nodeShape == NodeShapeVisualProperty.ROUND_RECTANGLE) {
            return new RoundedRectangle(size, size, color);
        } else if (nodeShape == BasicVisualLexicon.NODE_SHAPE.parseSerializableString("VEE")) {
            return new Vee(size, size, color);
        } else if (nodeShape == NodeShapeVisualProperty.DIAMOND) {
            return new Diamond(size, size, color);
        } else if (nodeShape == NodeShapeVisualProperty.HEXAGON) {
            return new Hexagon(size, size, color);
        } else if (nodeShape == NodeShapeVisualProperty.OCTAGON) {
            return new Octagon(size, size, color);
        } else if (nodeShape == NodeShapeVisualProperty.PARALLELOGRAM) {
            return new Parallelogram(size, size, color);
        } else {
            return new Ball(color, size);
        }
    }
}
