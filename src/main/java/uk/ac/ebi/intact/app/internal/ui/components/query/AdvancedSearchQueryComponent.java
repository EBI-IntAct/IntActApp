package uk.ac.ebi.intact.app.internal.ui.components.query;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.QueryComponents;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Objects;

public class AdvancedSearchQueryComponent {

    ArrayList<JComboBox<String>> entitiesComboBoxes = new ArrayList<>();
    ArrayList<JComboBox<String>> entitiesPropertiesComboBoxes = new ArrayList<>();
    ArrayList<JComboBox<String>> operatorsComboBoxes = new ArrayList<>();
    ArrayList<JTextField> userInputProperties = new ArrayList<>();
    ArrayList<JTextField> userInputProperties2 = new ArrayList<>();

    Dimension comboboxDimension = new Dimension(300, 20);

    static JTextField queryTextField = new JTextField("Query: ");
    static JFrame frame = new JFrame("Advanced Search Query Builder");
    static int frameWidth = 2000;

    private static String ruleOperator = "AND";
    private final JPanel rulesPanel = new JPanel();

    private int numberOfRuleBuilders = 0;
//    private int currentBuilderIndex = 0;

    public static void main(String[] args) {
        AdvancedSearchQueryComponent component = new AdvancedSearchQueryComponent();
        component.getFrame();
    }

    public JFrame getFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameWidth, 400);
        frame.setLayout(new GridLayout(4,1));

        frame.add(getQueryInputField());
        frame.add(getAndOrButton());

        rulesPanel.setAutoscrolls(true);
        rulesPanel.setLayout(new BoxLayout(rulesPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(rulesPanel);

        frame.add(scrollPane);

        JButton buildQueryButton = new JButton("Build query");
        buildQueryButton.addActionListener(e -> System.out.println(getMainQuery()));
        frame.add(buildQueryButton);

        frame.setVisible(true);
        return frame;
    }

    private void setUpEntitiesPropertiesCombobox(String entitySelected, int index) {
        if (index < 0 || index >= entitiesPropertiesComboBoxes.size()) {
            return;
        }

        JComboBox<String> entitiesCombobox = entitiesPropertiesComboBoxes.get(index);
        entitiesCombobox.removeAllItems();

        for (Field field : Field.values()) {
            if (field.getEntity().equalsIgnoreCase(entitySelected.trim())) {
                entitiesCombobox.addItem(field.getName());
            }
        }

        if (entitiesCombobox.getItemCount() > 0) {
            entitiesCombobox.setSelectedIndex(0);
        }

        setUpOperatorsCombobox((String) entitiesCombobox.getSelectedItem(), entitySelected, index);
    }

    private void setUpOperatorsCombobox(String entityPropertySelected, String entitySelected, int index) {
        if (index < 0 || index >= operatorsComboBoxes.size()) return;

        JComboBox<String> operatorsComboBox = operatorsComboBoxes.get(index);
        operatorsComboBox.removeAllItems();

        if (entityPropertySelected == null || entitySelected == null) return;

        Field field = Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected);
        if (field == null) return;

        if (field.getOperators() != null) {
            for (String operator : field.getOperators()) {
                operatorsComboBox.addItem(operator);
            }
        }

        if (operatorsComboBox.getItemCount() > 0) {
            operatorsComboBox.setSelectedIndex(0);
        }

        operatorsComboBox.revalidate();
        operatorsComboBox.repaint();
    }

    public JPanel getOneRuleBuilderPanel() {
        JPanel oneRuleBuilder = new JPanel();
        oneRuleBuilder.setBorder(BorderFactory.createTitledBorder("Rule"));
        oneRuleBuilder.setLayout(new BoxLayout(oneRuleBuilder, BoxLayout.X_AXIS));
        oneRuleBuilder.setVisible(true);

        oneRuleBuilder.add(getEntityComboBox());
        oneRuleBuilder.add(getEntityPropertiesComboBox(oneRuleBuilder));
        oneRuleBuilder.add(getOperatorsComboBox(oneRuleBuilder));
        oneRuleBuilder.add(getUserInputProperty());
        oneRuleBuilder.add(getUserInputProperty2(oneRuleBuilder));

        JButton deleteRuleButton = new JButton("Delete rule");
        deleteRuleButton.setSize(comboboxDimension);
        deleteRuleButton.setMaximumSize(comboboxDimension);

        deleteRuleButton.addActionListener(e -> deleteRule(oneRuleBuilder));
        oneRuleBuilder.add(deleteRuleButton);

        setActionListeners();

        numberOfRuleBuilders++;
        return oneRuleBuilder;
    }

    private void deleteRule(JPanel oneRuleBuilder) {
        int actualIndex = rulesPanel.getComponentZOrder(oneRuleBuilder);
        if (actualIndex == -1) return;

        rulesPanel.remove(oneRuleBuilder);

        if (actualIndex < entitiesComboBoxes.size()) {
            entitiesComboBoxes.remove(actualIndex);
            entitiesPropertiesComboBoxes.remove(actualIndex);
            operatorsComboBoxes.remove(actualIndex);
            userInputProperties.remove(actualIndex);
            userInputProperties2.remove(actualIndex);
        }

        numberOfRuleBuilders--;
        queryTextField.setText(getMainQuery());
        rulesPanel.revalidate();
        rulesPanel.repaint();
    }

    private JComboBox<String> getEntityComboBox() {
        JComboBox<String> entitiesComboBox = new JComboBox<>(Field.getEntities());
        entitiesComboBox.setSize(comboboxDimension);
        entitiesComboBox.setMaximumSize(comboboxDimension);

        entitiesComboBoxes.add(entitiesComboBox);

        entitiesComboBox.addActionListener(e -> {
            JComboBox<String> sourceComboBox = (JComboBox<String>) e.getSource();
            int index = entitiesComboBoxes.indexOf(sourceComboBox);
            setUpEntitiesPropertiesCombobox((String) sourceComboBox.getSelectedItem(), index);
            queryTextField.setText(getMainQuery());
        });

        return entitiesComboBox;
    }

    private JComboBox<String> getEntityPropertiesComboBox(JPanel oneRuleBuilder) {
        JComboBox<String> entitiesPropertiesComboBox = new JComboBox<>();
        entitiesPropertiesComboBox.setSize(comboboxDimension);
        entitiesPropertiesComboBox.setMaximumSize(comboboxDimension);

        entitiesPropertiesComboBoxes.add(entitiesPropertiesComboBox);
        entitiesPropertiesComboBox.addItemListener(e -> {
            int currentBuilderIndex = rulesPanel.getComponentZOrder(oneRuleBuilder);
            setUpOperatorsCombobox((String) entitiesPropertiesComboBoxes.get(currentBuilderIndex).getSelectedItem(),
                    (String) entitiesComboBoxes.get(currentBuilderIndex).getSelectedItem(), currentBuilderIndex);

            queryTextField.setText(getMainQuery());
        });

        return entitiesPropertiesComboBox;
    }

    private JComboBox<String> getOperatorsComboBox(JPanel oneRuleBuilder) {
        JComboBox<String> operatorsComboBox = new JComboBox<>();
        operatorsComboBox.setSize(comboboxDimension);
        operatorsComboBox.setMaximumSize(comboboxDimension);

        operatorsComboBoxes.add(operatorsComboBox);

        operatorsComboBox.addActionListener(e -> {
            int currentBuilderIndex = rulesPanel.getComponentZOrder(oneRuleBuilder);

            userInputProperties2.get(currentBuilderIndex)
                    .setVisible(operatorsComboBox.getSelectedItem() != null && isUserInput2needed(oneRuleBuilder));
            userInputProperties.get(currentBuilderIndex)
                    .setVisible(operatorsComboBox.getSelectedItem() != null && isUserInputNeeded(oneRuleBuilder));
            queryTextField.setText(getMainQuery());
        });

        return operatorsComboBox;
    }

    private JTextField getUserInputProperty() {
        JTextField userInputProperty = new JTextField(15);
        userInputProperty.setSize(comboboxDimension);
        userInputProperty.setMaximumSize(comboboxDimension);

        userInputProperties.add(userInputProperty);

        userInputProperty.addActionListener(e -> queryTextField.setText(getMainQuery()));

        return userInputProperty;
    }

    private JTextField getUserInputProperty2(JPanel oneRuleBuilder) {
        JTextField userInputProperty2 = new JTextField(15);
        userInputProperty2.setSize(comboboxDimension);
        userInputProperty2.setMaximumSize(comboboxDimension);

        userInputProperties2.add(userInputProperty2);
        userInputProperty2.addActionListener(e -> queryTextField.setText(getMainQuery()));

        return userInputProperty2;
    }

    private void setActionListeners() {
        queryTextField.addActionListener(e -> {
            modifyComboBoxesFromTextField(getQueryFromTextField());
            queryTextField.setText(getMainQuery());
        });
    }

    private boolean isUserInput2needed(JPanel oneRuleBuilder) {
        int currentBuilderIndex = rulesPanel.getComponentZOrder(oneRuleBuilder);
        String operatorSelected = (String) operatorsComboBoxes.get(currentBuilderIndex).getSelectedItem();
        return operatorSelected != null && (operatorSelected.equals("∈") || operatorSelected.equals("∉"));
    }

    private boolean isUserInputNeeded(JPanel oneRuleBuilder) {
        int currentBuilderIndex = rulesPanel.getComponentZOrder(oneRuleBuilder);
        String operatorSelected = (String) operatorsComboBoxes.get(currentBuilderIndex).getSelectedItem();
        return operatorSelected != null && !(operatorSelected.equals("TRUE") || operatorSelected.equals("FALSE"));
    }

    private JPanel getQueryInputField(){
        JPanel queryInputFieldContainer = new JPanel();
        queryInputFieldContainer.setBorder(BorderFactory.createTitledBorder("Built query"));
        queryTextField.setMinimumSize(new Dimension(frameWidth, 25));
        queryTextField.setPreferredSize(new Dimension(frameWidth/2, 25));
        queryTextField.setVisible(true);
        queryInputFieldContainer.add(queryTextField);
        return queryInputFieldContainer;
    }

    private JPanel getAndOrButton() {
        JButton andButton = new JButton("AND");
        andButton.setForeground(Color.MAGENTA);
        JButton orButton = new JButton("OR");
        JButton addRuleButton = new JButton("Add rule");

        Color INTACT_PURPLE = new Color(104, 41, 124);

        JPanel buttonContainer = new JPanel();

        andButton.addActionListener(e -> {
            andButton.setForeground(Color.MAGENTA);
            orButton.setForeground(Color.BLACK);
            ruleOperator = "AND";
            queryTextField.setText(getMainQuery());
        });

        orButton.addActionListener(e -> {
            andButton.setForeground(Color.BLACK);
            orButton.setForeground(Color.MAGENTA);
            ruleOperator = "OR";
            queryTextField.setText(getMainQuery());
        });

        addRuleButton.addActionListener(e -> {
            rulesPanel.add(getOneRuleBuilderPanel());
            rulesPanel.revalidate();
            rulesPanel.repaint();
            setEntitiesComboBoxesSelected(); //triggers the actionListener to set up the other comboboxes
            queryTextField.setText(getMainQuery());
        });

        buttonContainer.add(andButton);
        buttonContainer.add(orButton);
        buttonContainer.add(addRuleButton);



        return buttonContainer;
    }

    private void setEntitiesComboBoxesSelected(){
        for (JComboBox<String> comboBox : entitiesComboBoxes) {
            comboBox.setSelectedIndex(0);
        }
    }

    public String getQuery(int builderIndex) {
        if (builderIndex < 0 || builderIndex >= entitiesComboBoxes.size()) return "";

        String entityPropertySelected = entitiesPropertiesComboBoxes
                .get(builderIndex).getSelectedItem() != null ? (String) entitiesPropertiesComboBoxes.get(builderIndex).getSelectedItem() : "";

        String entitySelected = entitiesComboBoxes
                .get(builderIndex).getSelectedItem() != null ? (String) entitiesComboBoxes.get(builderIndex).getSelectedItem() : "";

        entitySelected = Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected) != null
                ? Objects.requireNonNull(Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected)).getMiqlQuery()
                : "";

        String operatorSelected = operatorsComboBoxes.get(builderIndex)
                .getSelectedItem() != null ? (String) operatorsComboBoxes.get(builderIndex).getSelectedItem() : "";

        String userInput = userInputProperties.get(builderIndex)
                .getText() != null ? userInputProperties.get(builderIndex).getText() : "";

        String userInput2 = userInputProperties2.get(builderIndex)
                .getText() != null ? userInputProperties2.get(builderIndex).getText() : "";

        boolean isLastIndex = builderIndex == entitiesComboBoxes.size() - 1;

        String query = "";

        if (operatorSelected != null) {
            switch (operatorSelected) {
                case "=":
                    query = entitySelected + ":" + userInput;
                    break;
                case "≠":
                    query = "NOT " + entitySelected + ":" + userInput;
                    break;
                case "in":
                    query = entitySelected + ":(" + userInput + ")";
                    break;
                case "not in":
                    query = "NOT " + entitySelected + ":(" + userInput + ")";
                    break;
                case "∈":
                    query = entitySelected + ":[" + userInput + " TO " + userInput2 + "]";
                    break;
                case "∉":
                    query = "NOT " + entitySelected + ":[" + userInput + " TO " + userInput2 + "]";
                    break;
                case "TRUE":
                    query = entitySelected + ":TRUE";
                    break;
                case "FALSE":
                    query = entitySelected + ":FALSE";
                    break;
                default:
                    return "";
            }
        }

        return isLastIndex ? query : query + " " + ruleOperator + " ";
    }

    public String getMainQuery(){
        StringBuilder mainQuery = new StringBuilder();
        for (int i = 0; i < numberOfRuleBuilders; i++) {
            mainQuery.append(getQuery(i));
        }
        return mainQuery.toString();
    }

    public String getQueryFromTextField(){
        return queryTextField.getText();
    }

    private void modifyComboBoxesFromTextField(String queryFromTextField){
        String[] queries = getQueriesFromTextField(queryFromTextField);
        modifyQueryBuildersNumberFromTextField(queries);

        for (int i = 0; i < queries.length; i++) {
            for (Field field : Field.FIELD_MAP.values()) {
                if (queryFromTextField.contains(field.getMiqlQuery())) {
                    QueryComponents queryComponents = parseQuery(queries[i].trim());

                    entitiesComboBoxes.get(i).setSelectedItem(queryComponents.getEntity());
                    entitiesPropertiesComboBoxes.get(i).setSelectedItem(queryComponents.getName());
                    operatorsComboBoxes.get(i).setSelectedItem(getOperatorFromTextField(queryFromTextField));
                    userInputProperties.get(i).setText(queryComponents.getUserInput());
                    userInputProperties2.get(i).setText(queryComponents.getUserInput2());
                }
            }
        }
    }

    private void modifyQueryBuildersNumberFromTextField(String[] queries){
        if (queries.length > entitiesComboBoxes.size()) {
            while (queries.length > entitiesComboBoxes.size()) {
                rulesPanel.add(getOneRuleBuilderPanel());
            }
        } else if (queries.length < numberOfRuleBuilders) {
            while (queries.length < numberOfRuleBuilders) {
                Component lastRule = rulesPanel.getComponents()[numberOfRuleBuilders-1];
                deleteRule((JPanel) lastRule);
            }
        }

        rulesPanel.revalidate();
        rulesPanel.repaint();
        queryTextField.setText(getMainQuery());
    }

    private String[] getQueriesFromTextField(String queryFromTextField){
        String[] queries;

        if (queryFromTextField.contains("AND")) {
            queries = queryFromTextField.split("AND");
        } else if (queryFromTextField.contains("OR")) {
            queries = queryFromTextField.split("OR");
        } else {
            queries = new String[]{queryFromTextField};
        }

        return queries;
    }

    private String getOperatorFromTextField(String queryFromTextField){
        if (queryFromTextField.contains("NOT")){
            if (queryFromTextField.contains(":[")){
                return "∉";
            } else if (queryFromTextField.contains(":(")){
                return "not in";
            } else if (queryFromTextField.contains(":")){
                return "≠";
            }
        } else {
            if (queryFromTextField.contains(":[")){
                return "∈";
            } else if (queryFromTextField.contains(":(")){
                return "in";
            } else if (queryFromTextField.contains(":")){
                return "=";
            } else if (queryFromTextField.contains("TRUE")){
                return "TRUE";
            } else if (queryFromTextField.contains("FALSE")){
                return "FALSE";
            }
        }
        return "";
    }

    public QueryComponents parseQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }

        QueryComponents components = new QueryComponents();

        if (query.startsWith("NOT ")) {
            query = query.substring(4);
            components.setNegated(true);
        }

        String[] parts;

        if (query.contains(":[")) {

            components.setOperator(components.isNegated() ? "∉" : "∈");
            parts = query.split(":\\[");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                String[] rangeValues = parts[1].replace("]", "").split(" TO ");
                if (rangeValues.length == 2) {
                    components.setUserInput(rangeValues[0]);
                    components.setUserInput2(rangeValues[1]);
                }
            }
        } else if (query.contains(":(")) {
            components.setOperator(components.isNegated() ? "not in" : "in");
            parts = query.split(":\\(");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                components.setUserInput(parts[1].replace(")", ""));
            }
        } else if (query.contains(":")) {
            parts = query.split(":");
            if (parts.length == 2) {
                Field field = Field.getFieldsFromMiQL(parts[0]);
                components.setEntity(field.getEntity());
                components.setName(field.getName());
                components.setUserInput(parts[1]);

                if (components.isNegated()) {
                    components.setOperator("≠");
                } else {
                    components.setOperator("=");
                }
            }
        }
        return components;
    }

}
