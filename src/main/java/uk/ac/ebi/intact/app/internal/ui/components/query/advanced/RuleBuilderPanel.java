package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class RuleBuilderPanel extends JPanel {

    ArrayList<JComboBox<String>> entitiesComboBoxes = new ArrayList<>();
    ArrayList<JComboBox<String>> entitiesPropertiesComboBoxes = new ArrayList<>();
    ArrayList<JComboBox<String>> operatorsComboBoxes = new ArrayList<>();
    ArrayList<JTextField> userInputProperties = new ArrayList<>();
    ArrayList<JTextField> userInputProperties2 = new ArrayList<>();

    Dimension comboboxDimension = new Dimension(300, 20);

    private static String ruleOperator = "AND";

    private final JPanel rulesPanel = new JPanel();

    private final Color INTACT_PURPLE = new Color(104, 41, 124);

    public RuleBuilderPanel() {

    }


}
