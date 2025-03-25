package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;

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

    public static String getQueriesFromRuleBuilders(ArrayList<OneRuleBuilderPanel> rules, String queryOperator) {
        if (!rules.isEmpty()){
            ArrayList<String> ruleBuilders = new ArrayList<>();
            for (OneRuleBuilderPanel oneRuleBuilderPanel : rules) {
                oneRuleBuilderPanel.getQuery();
                ruleBuilders.add(oneRuleBuilderPanel.getQuery());
            }
            return String.join(" " + queryOperator + " ", ruleBuilders);
        } else {
            return null;
        }
    }

}
