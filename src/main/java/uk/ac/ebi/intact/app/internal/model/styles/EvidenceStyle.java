package uk.ac.ebi.intact.app.internal.model.styles;

import org.cytoscape.model.CyEdge;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import uk.ac.ebi.intact.app.internal.model.core.view.NetworkView;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.utils.TimeUtils;
import uk.ac.ebi.intact.app.internal.model.tables.fields.enums.EdgeFields;

import java.awt.*;

public class EvidenceStyle extends Style {
    public final static NetworkView.Type type = NetworkView.Type.EVIDENCE;

    public EvidenceStyle(Manager manager, VisualStyle style) {
        super(manager, style);
    }

    public EvidenceStyle(Manager manager) {
        super(manager);
    }

    @Override
    protected void setEdgeLineTypeStyle() {
        DiscreteMapping<String, LineType> shapeToLineType = (DiscreteMapping<String, LineType>) discreteFactory.createVisualMappingFunction(EdgeFields.EXPANSION_TYPE.toString(), String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
        shapeToLineType.putMapValue("null", LineTypeVisualProperty.SOLID);
        shapeToLineType.putMapValue("spoke expansion", LineTypeVisualProperty.EQUAL_DASH);

        style.addVisualMappingFunction(shapeToLineType);
    }

    @Override
    protected void setEdgePaintStyle() {
        DiscreteMapping<String, Paint> interactionTypeToColor = (DiscreteMapping<String, Paint>) discreteFactory.createVisualMappingFunction(CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
        interactionTypeToColor.putAll(StyleMapper.edgeTypeToPaint);
        style.addVisualMappingFunction(interactionTypeToColor);
        style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(153, 153, 153));
        addMissingEdgePaint(interactionTypeToColor);
    }

    private void addMissingEdgePaint(DiscreteMapping<String, Paint> interactionTypeToColor) {
        new Thread(() -> {
            while (StyleMapper.edgeTypesNotReady()) {
                TimeUtils.sleep(100);
            }
            interactionTypeToColor.putAll(StyleMapper.edgeTypeToPaint);
        }).start();

    }


    @Override
    public NetworkView.Type getStyleViewType() {
        return type;
    }
}
