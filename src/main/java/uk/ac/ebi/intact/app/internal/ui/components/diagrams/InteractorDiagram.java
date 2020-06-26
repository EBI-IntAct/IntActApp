package uk.ac.ebi.intact.app.internal.ui.components.diagrams;

import uk.ac.ebi.intact.app.internal.ui.components.labels.JLabel2D;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.events.StyleUpdatedListener;
import uk.ac.ebi.intact.app.internal.model.styles.IntactStyle;
import uk.ac.ebi.intact.app.internal.model.styles.mapper.StyleMapper;
import uk.ac.ebi.intact.app.internal.ui.components.legend.shapes.AbstractNodeShape;
import uk.ac.ebi.intact.app.internal.ui.utils.StyleUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

public class InteractorDiagram extends JPanel implements StyleUpdatedListener {
    private final JPanel shapePanel = new JPanel(new BorderLayout());
    protected final AbstractNodeShape shape;
    private final Interactor interactor;
    private JLabel2D label;

    public InteractorDiagram(Interactor interactor) {
        this.interactor = interactor;
        this.setBackground(new Color(0, 0, 0, 0));
        this.setOpaque(false);
        this.setLayout(new OverlayLayout(this));
        this.setAlignmentX(LEFT_ALIGNMENT);
        if (interactor.name != null && !interactor.name.isBlank()) {
            label = new JLabel2D(interactor.name, JLabel.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 12));
            label.setForeground(Color.WHITE);
            label.setOutlineColor(new Color(0, 0, 0, 130));
            label.setStroke(5);
            this.add(label);
        }
        shape = StyleUtils.nodeTypeToShape(interactor.type, IntactStyle.defaultNodeColor, 50);
        shapePanel.add(shape, BorderLayout.CENTER);
        StyleMapper.addStyleUpdatedListener(this);
        updateStyle();
        this.add(shapePanel);
    }

    public void updateStyle() {
        shapePanel.setOpaque(false);
        Color color = (Color) StyleMapper.taxIdToPaint.get(interactor.taxId);
        if (color == null) color = (Color) StyleMapper.kingdomColors.get(interactor.taxId);
        if (color != null) shape.setColor(color);
        repaint();
    }


    @Override
    public synchronized void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        label.addMouseListener(l);
    }

    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        if (shapePanel != null) {
            shapePanel.setBackground(bg);
        }
    }

    @Override
    public void handleStyleUpdatedEvent() {
        updateStyle();
    }
}
