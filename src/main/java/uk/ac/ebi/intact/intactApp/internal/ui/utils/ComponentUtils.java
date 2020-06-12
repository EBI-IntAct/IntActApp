package uk.ac.ebi.intact.intactApp.internal.ui.utils;

import javax.swing.*;
import java.awt.*;
import java.util.function.BiConsumer;

public class ComponentUtils {
    public static JComponent resizeWidth(JComponent component, int width, SizeType sizeType) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.width = width;
        sizeType.setSize.accept(component, preferredSize);
        return component;
    }

    public static JComponent resizeHeight(JComponent component, int height, SizeType sizeType) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.height = height;
        sizeType.setSize.accept(component, preferredSize);
        return component;
    }

    public static Component resizeWidth(Component component, int width, SizeType sizeType) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.width = width;
        sizeType.setSize.accept(component, preferredSize);
        return component;
    }

    public static Component resizeHeight(Component component, int height, SizeType sizeType) {
        Dimension preferredSize = component.getPreferredSize();
        preferredSize.height = height;
        sizeType.setSize.accept(component, preferredSize);
        return component;
    }

    public enum SizeType {
        ALL((component, dimension) -> {
            component.setSize(dimension);
            component.setMinimumSize(dimension);
            component.setMaximumSize(dimension);
            component.setPreferredSize(dimension);
        }),
        STD(Component::setSize),
        MIN(Component::setMinimumSize),
        MAX(Component::setMaximumSize),
        PREF(Component::setPreferredSize);

        final BiConsumer<Component, Dimension> setSize;

        SizeType(BiConsumer<Component, Dimension> setSize) {
            this.setSize = setSize;
        }
    }
}
