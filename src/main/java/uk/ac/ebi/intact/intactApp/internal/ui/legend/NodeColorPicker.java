package uk.ac.ebi.intact.intactApp.internal.ui.legend;

import org.cytoscape.util.swing.CyColorChooser;
import uk.ac.ebi.intact.intactApp.internal.ui.IntactLegendPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;


public class NodeColorPicker extends JPanel implements MouseListener {
    protected String descriptor;
    protected Color currentColor;
    protected final NodeColorPicker nodeColorPicker;
    protected Ball ball;
    protected boolean italic;

    protected JLabel label;
    protected List<ColorChangedListener> listeners = new ArrayList<>();

    public NodeColorPicker(String descriptor, Color currentColor, boolean italic) {
        this.descriptor = descriptor;
        this.currentColor = currentColor;
        this.italic = italic;
        nodeColorPicker = this;
        init();
        setBackground(IntactLegendPanel.transparent);
    }

    public void setCurrentColor(Color color) {
        currentColor = color;
        ball.setColor(color);
    }

    private void init() {
        setLayout(new BorderLayout());

        ball = new Ball(currentColor, 30);
        add(ball, BorderLayout.WEST);

        label = new JLabel(descriptor, SwingConstants.LEFT);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setBackground(IntactLegendPanel.transparent);
        label.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
        if (italic) {
            label.setFont(new Font(label.getFont().getName(), Font.ITALIC, label.getFont().getSize()));
        }
        add(label, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));
        addMouseListener(this);

    }

    public void addColorChangedListener(ColorChangedListener listener) {
        listeners.add(listener);
    }

    public static class ColorChangedEvent extends AWTEvent {
        public final Color newColor;

        public ColorChangedEvent(Object source, int id, Color newColor) {
            super(source, id);
            this.newColor = newColor;
        }
    }

    public interface ColorChangedListener extends EventListener {
        void colorChanged(ColorChangedEvent colorChangedEvent);
    }

    private class Ball extends JButton {
        Color color;
        int diameter;

        public Ball(Color color, int diameter) {
            this.color = color;
            this.diameter = diameter;
            Dimension preferredSize = new Dimension(diameter, diameter);
            setPreferredSize(preferredSize);
            setMinimumSize(preferredSize);
            addMouseListener(nodeColorPicker);
//            setMaximumSize(preferredSize);
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, diameter, diameter);
        }

        public void setColor(Color color) {
            this.color = color;
            repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        currentColor = CyColorChooser.showDialog(nodeColorPicker, "Choose " + descriptor + " colors", currentColor);
        if (currentColor != null) {
            ball.setColor(currentColor);

            for (ColorChangedListener listener : listeners) {
                listener.colorChanged(new ColorChangedEvent(this, ActionEvent.ACTION_PERFORMED, currentColor));
            }
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
