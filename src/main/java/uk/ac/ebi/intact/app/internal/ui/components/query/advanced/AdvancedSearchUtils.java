package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RulePanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleSetPanel;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class AdvancedSearchUtils {
    final static Color INTACT_PURPLE = new Color(104, 41, 124);

    public static void setCorrectDimensions(JComponent component) {
        Dimension comboboxDimension = new Dimension(300, 20);
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

    public static String getQueriesFromRuleBuilders(ArrayList<Object> panels, String queryOperator) {
        if (!panels.isEmpty()){
            ArrayList<String> ruleBuilders = new ArrayList<>();
            for (Object panel : panels) {
                if (panel instanceof RulePanel) {
                    RulePanel rulePanel = (RulePanel) panel;
                    ruleBuilders.add(rulePanel.getQuery());
                } else if (panel instanceof RuleSetPanel) {
                    RuleSetPanel ruleSetPanel = (RuleSetPanel) panel;
                    ruleBuilders.add(ruleSetPanel.getQuery());
                }
            }
            return String.join(" " + queryOperator + " ", ruleBuilders);
        } else {
            return null;
        }
    }

    public static JButton getDeletePanelButton(JPanel panelToDelete) {
        JButton deleteButton = new JButton("X");
        setButtonIntactPurple(deleteButton);

        deleteButton.addActionListener(e -> {
            Container parent = panelToDelete.getParent();
            if (parent != null) {
                parent.remove(panelToDelete);
                parent.revalidate();
                parent.repaint();
            }
        });

        return deleteButton;
    }


}
