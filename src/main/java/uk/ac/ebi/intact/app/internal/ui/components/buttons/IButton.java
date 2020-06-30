package uk.ac.ebi.intact.app.internal.ui.components.buttons;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//TODO change color of foreground when disabled
public class IButton extends JButton implements PropertyChangeListener {
    private String text;
    private Color disabledColor = Color.WHITE;

    public IButton() {
    }

    public IButton(String text) {
        super("<html>" + text + "</html>");
        this.text = text;
//        addPropertyChangeListener(this);
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        this.disabledColor = disabledColor;
    }

    private static String colorToHexa(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        String hexa = colorToHexa(disabledColor);
//        System.out.println(hexa);
//        setText(isEnabled() ? "<html>" + text + "</html>" : "<html><font color= white>" + text + "</font></html>");
//        repaint();
    }
}
