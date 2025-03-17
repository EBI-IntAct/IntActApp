package uk.ac.ebi.intact.app.internal.ui.components.query;

import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.QueryComponents;

import javax.swing.*;
import java.awt.*;

public class AdvancedSearchQueryComponent {

    JComboBox<String> entitiesComboBox = new JComboBox<>(Field.getEntities());
    JComboBox<String> entitiesPropertiesComboBox = new JComboBox<>();
    JComboBox<String> operatorsComboBox = new JComboBox<>();
    JTextField userInputProperty = new JTextField(15);
    JTextField userInputProperty2 = new JTextField(15);
    JTextField queryTextField = new JTextField("Query: ");

    JPanel advancedSearchQueryBuilderPanel = new JPanel();

    public AdvancedSearchQueryComponent() {
        setUpAdvancedSearchQueryBuilderPanel();
    }

    private void setUpEntitiesPropertiesCombobox(String entitySelected) {
        entitiesPropertiesComboBox.removeAllItems();

        for (Field field : Field.values()) {
            if (field.getEntity().equalsIgnoreCase(entitySelected.trim())) {
                entitiesPropertiesComboBox.addItem(field.getName());
            }
        }

        if (entitiesPropertiesComboBox.getItemCount() > 0) {
            entitiesPropertiesComboBox.setSelectedIndex(0);
        }

        setUpOperatorsCombobox((String) entitiesPropertiesComboBox.getSelectedItem(), entitySelected);
    }

    private void setUpOperatorsCombobox(String entityPropertySelected, String entitySelected) {
        operatorsComboBox.removeAllItems();

        if (entityPropertySelected == null || entitySelected == null) {
            return;
        }

        Field field = Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected);

        if (field == null) {
            return;
        }

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

    public void setUpAdvancedSearchQueryBuilderPanel() {
        advancedSearchQueryBuilderPanel.setBorder(BorderFactory.createTitledBorder("Build MIQL query"));
        advancedSearchQueryBuilderPanel.setLayout(new BoxLayout(advancedSearchQueryBuilderPanel, BoxLayout.X_AXIS));

        advancedSearchQueryBuilderPanel.add(queryTextField);
        advancedSearchQueryBuilderPanel.add(entitiesComboBox);
        advancedSearchQueryBuilderPanel.add(entitiesPropertiesComboBox);
        advancedSearchQueryBuilderPanel.add(operatorsComboBox);
        advancedSearchQueryBuilderPanel.add(userInputProperty);
        advancedSearchQueryBuilderPanel.add(userInputProperty2);

        setActionListeners();

        JButton buildQueryButton = new JButton("Build MIQL query");
        buildQueryButton.addActionListener(e -> {
            System.out.println(getQuery());
        });
        advancedSearchQueryBuilderPanel.add(buildQueryButton);

    }

    private void setActionListeners(){
        entitiesComboBox.addActionListener(e -> {
            setUpEntitiesPropertiesCombobox((String) entitiesComboBox.getSelectedItem());
            queryTextField.setText(getQuery());
        });

        entitiesPropertiesComboBox.addItemListener(e -> {
            setUpOperatorsCombobox((String) entitiesPropertiesComboBox.getSelectedItem(),
                    (String) entitiesComboBox.getSelectedItem());
            queryTextField.setText(getQuery());
        });

        operatorsComboBox.addActionListener(e -> {
            userInputProperty2.setVisible(operatorsComboBox.getSelectedItem() != null && isUserInput2needed());
            userInputProperty.setVisible(operatorsComboBox.getSelectedItem() != null && isUserInputneeded());
            queryTextField.setText(getQuery());
        });

        userInputProperty.addActionListener(e -> {
            queryTextField.setText(getQuery());
        });

        userInputProperty2.addActionListener(e -> {
            queryTextField.setText(getQuery());
        });

        queryTextField.addActionListener(e -> {
            System.out.println(getQueryFromTextField());
            modifyComboBoxesFromTextField(getQueryFromTextField());
            queryTextField.setText(getQuery());
        });
    }

    private boolean isUserInput2needed(){
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && (operatorSelected.equals("∈") || operatorSelected.equals("∉"));
    }

    private boolean isUserInputneeded(){
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && !(operatorSelected.equals("TRUE") || operatorSelected.equals("FALSE"));
    }


    public JPanel getPanel() {
        return advancedSearchQueryBuilderPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Advanced Search Query Builder");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(2000, 500);
            frame.setLayout(new BorderLayout());

            AdvancedSearchQueryComponent component = new AdvancedSearchQueryComponent();
            frame.add(component.getPanel(), BorderLayout.CENTER);

            frame.setVisible(true);
        });
    }

    public String getQuery(){

        String entityPropertySelected = entitiesPropertiesComboBox.getSelectedItem() != null ? (String) entitiesPropertiesComboBox.getSelectedItem() : "";

        String entitySelected = entitiesComboBox.getSelectedItem() != null ? (String) entitiesComboBox.getSelectedItem() : "";

        entitySelected = Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected) != null
                ? Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected).getMiqlQuery()
                : "";

        String operatorSelected = operatorsComboBox.getSelectedItem() != null ? (String) operatorsComboBox.getSelectedItem() : "";
        String userInput = userInputProperty.getText() != null ? userInputProperty.getText() : "";
        String userInput2 = userInputProperty2.getText() != null ? userInputProperty2.getText() : "";

        switch (operatorSelected) {
            case "=":
                return entitySelected + ":" + userInput;
            case "≠":
                return "NOT " + entitySelected + ":" + userInput;
            case "in":
                return entitySelected + ":(" + userInput + ")";
            case "not in":
                return "NOT " + entitySelected + ":(" + userInput + ")";
            case "∈":
                return entitySelected + ":[" + userInput + " TO " + userInput2 + "]";
            case "∉":
                return "NOT " + entitySelected + ":[" + userInput + " TO " + userInput2 + "]";
            case "TRUE":
                return entitySelected + ":TRUE";
            case "FALSE":
                return entitySelected + ":FALSE";
            default:
                return null;
        }
    }

    public String getQueryFromTextField(){
        return queryTextField.getText();
    }

    private void modifyComboBoxesFromTextField(String queryFromTextField){
        for (Field field: Field.FIELD_MAP.values()) {
            if (queryFromTextField.contains(field.getMiqlQuery())) {
                entitiesComboBox.setSelectedItem(field.getEntity());
                entitiesPropertiesComboBox.setSelectedItem(field.getName());
                operatorsComboBox.setSelectedItem(getOperatorFromTextField(queryFromTextField));
                QueryComponents queryComponents = parseQuery(queryFromTextField);
                userInputProperty.setText(queryComponents.getUserInput());
                userInputProperty2.setText(queryComponents.getUserInput2());
            }
        }
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
                components.setEntity(parts[0]);
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
                components.setEntity(parts[0]);
                components.setUserInput(parts[1].replace(")", ""));
            }
        } else if (query.contains(":")) {
            parts = query.split(":");
            if (parts.length == 2) {
                components.setEntity(parts[0]);
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
