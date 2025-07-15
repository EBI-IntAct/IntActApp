package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleContainer;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import java.awt.*;

public class AdvancedSearchUtils {
    final static Color INTACT_PURPLE = new Color(104, 41, 124);
    private static final ImageIcon delete = IconUtils.createImageIcon("/Buttons/delete.png", 30, 30);


    public static void setCorrectDimensions(JComponent component) {
        Dimension comboboxDimension = new Dimension(240, 25);
        component.setPreferredSize(comboboxDimension);
        component.setMinimumSize(comboboxDimension);
        component.setMaximumSize(comboboxDimension);
    }

    public static void setButtonIntactPurple(JButton button) {
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(INTACT_PURPLE);
        button.setForeground(Color.WHITE);
    }

    public static void setButtonWhite(JButton button) {
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setBackground(Color.WHITE);
        button.setForeground(INTACT_PURPLE);
    }


    public static JButton getDeletePanelButton(AdvancedSearchQueryComponent advancedSearchQueryComponent, RuleContainer ruleContainer) {
        JButton deleteButton = new JButton(delete);
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        Dimension maximumSize = new Dimension(30, 30);
        deleteButton.setSize(maximumSize);
        deleteButton.setPreferredSize(maximumSize);
        deleteButton.setMaximumSize(maximumSize);

        setButtonIntactPurple(deleteButton);

        deleteButton.addActionListener(e -> {
            ruleContainer.delete();
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        return deleteButton;
    }

}
