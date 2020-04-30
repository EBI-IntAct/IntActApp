package uk.ac.ebi.intact.intactApp.internal.ui.components.legend.shapes;

import javax.swing.*;
import java.awt.*;


abstract class AbstractNodeShape extends JComponent {
	protected int width;
	protected int height;
	protected Color color;

	public AbstractNodeShape(int width, int height, Color color) {
		this.width = width;
		this.height = height;
		this.color = color;
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setPaint(color);
		Insets insets = new Insets(0, 0, 0, 0);
		insets = getInsets(insets);
		g2.translate(insets.left, insets.top);
		g2.fill(getShape());
	}

	abstract protected Shape getShape();

	@Override
	public Dimension getPreferredSize() {
		Insets insets = new Insets(0, 0, 0, 0);
		insets = getInsets(insets);
		return new Dimension(insets.left + width + insets.right, insets.top + height + insets.bottom);
	}
}
