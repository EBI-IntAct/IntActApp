package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;

import java.util.Objects;

public class RulePanel {

    JPanel oneRule = new JPanel();

    public JComboBox<String> entityComboBox = new JComboBox<>(Field.getEntities());
    public JComboBox<String> entityPropertiesCombobox = new JComboBox<>();
    public JComboBox<String> operatorsComboBox = new JComboBox<>();

    public JTextField userInputProperty = new JTextField();
    public JTextField userInputProperty2 = new JTextField();

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    public RulePanel(AdvancedSearchQueryComponent advancedSearchQueryComponent) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        getOneRuleBuilderPanel();
        oneRule.add(getDeletePanelButton(oneRule));
    }

    public JPanel getOneRuleBuilderPanel() {
        oneRule.setBorder(BorderFactory.createTitledBorder("Rule"));
        oneRule.setLayout(new BoxLayout(oneRule, BoxLayout.X_AXIS));
        oneRule.setVisible(true);

        oneRule.add(getEntityComboBox());
        oneRule.add(getEntityPropertiesComboBox());
        oneRule.add(getOperatorsComboBox());
        oneRule.add(getUserInputProperty());
        oneRule.add(getUserInputProperty2());

        oneRule.revalidate();
        oneRule.repaint();

        return oneRule;
    }

    private JComboBox<String> getEntityComboBox() {
        setCorrectDimensions(entityComboBox);

        entityComboBox.addActionListener(e -> {
            setUpEntityPropertiesCombobox((String) entityComboBox.getSelectedItem());
            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
        });

        return entityComboBox;
    }

    private JComboBox<String> getEntityPropertiesComboBox() {
        setCorrectDimensions(entityPropertiesCombobox);

        entityPropertiesCombobox.addItemListener(e -> {
            setUpOperatorsCombobox((String) entityPropertiesCombobox.getSelectedItem(),
                    (String) entityComboBox.getSelectedItem());
//            queryTextField.setText(advancedSearchQueryComponent.getQueriesFromRuleBuilders());
        });
        return entityPropertiesCombobox;
    }

    private JComboBox<String> getOperatorsComboBox() {
        setCorrectDimensions(operatorsComboBox);

        operatorsComboBox.addActionListener(e -> {
            userInputProperty2.setVisible(operatorsComboBox.getSelectedItem() != null && isUserInput2needed());
            userInputProperty.setVisible(operatorsComboBox.getSelectedItem() != null && isUserInputNeeded());
            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
        });

        return operatorsComboBox;
    }

    private JTextField getUserInputProperty() {
        setCorrectDimensions(userInputProperty);
        userInputProperty.addActionListener(e ->
                advancedSearchQueryComponent.getQueryTextField()
                        .setText(advancedSearchQueryComponent.getFullQuery()));
        return userInputProperty;
    }

    private boolean isUserInputNeeded() {
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && !(operatorSelected.equals("TRUE") || operatorSelected.equals("FALSE"));
    }

    private JTextField getUserInputProperty2() {
        setCorrectDimensions(userInputProperty2);
        userInputProperty2.addActionListener(e ->
                advancedSearchQueryComponent.getQueryTextField()
                        .setText(advancedSearchQueryComponent.getFullQuery()));
        return userInputProperty2;
    }

    private boolean isUserInput2needed() {
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && (operatorSelected.equals("∈") || operatorSelected.equals("∉"));
    }

    private void setUpEntityPropertiesCombobox(String entitySelected) {
        entityPropertiesCombobox.removeAllItems();

        for (Field field : Field.values()) {
            if (field.getEntity().equalsIgnoreCase(entitySelected.trim())) {
                entityPropertiesCombobox.addItem(field.getName());
            }
        }

        if (entityPropertiesCombobox.getItemCount() > 0) {
            entityPropertiesCombobox.setSelectedIndex(0);
        }

        setUpOperatorsCombobox((String) entityPropertiesCombobox.getSelectedItem(), entitySelected);

        entityPropertiesCombobox.revalidate();
        entityPropertiesCombobox.repaint();
    }

    private void setUpOperatorsCombobox(String entityPropertySelected, String entitySelected) {
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

    public void setEntityComboboxSelected(){
        entityComboBox.setSelectedIndex(0);
    }

    public String getQuery() {

        String entityPropertySelected = entityPropertiesCombobox.getSelectedItem() != null ? (String) entityPropertiesCombobox.getSelectedItem() : "";
        String entitySelected = entityComboBox.getSelectedItem() != null ? (String) entityComboBox.getSelectedItem() : "";
        String operatorSelected = operatorsComboBox.getSelectedItem() != null ? (String) operatorsComboBox.getSelectedItem() : "";
        String userInput = userInputProperty.getText() != null ? userInputProperty.getText() : "";
        String userInput2 = userInputProperty2.getText() != null ? userInputProperty2.getText() : "";

        entitySelected = Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected) != null
                ? Objects.requireNonNull(Field.getFieldFromNameAndEntity(entityPropertySelected, entitySelected)).getMiqlQuery()
                : "";

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

        return query;
    }

}
