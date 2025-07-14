package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;

import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

public class RulePanel {

    JPanel oneRule = new JPanel();

    public JComboBox<String> entityComboBox = new JComboBox<>(Field.getEntities());
    public JComboBox<String> entityPropertiesCombobox = new JComboBox<>();
    public JComboBox<String> operatorsComboBox = new JComboBox<>();


    public JTextArea firstBracket = new JTextArea("[");
    public JTextField userInputProperty = new JTextField();
    public JTextArea textTO = new JTextArea(" TO ");
    public JTextField userInputProperty2 = new JTextField();
    public JTextArea lastBracket = new JTextArea("]");

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    public RulePanel(AdvancedSearchQueryComponent advancedSearchQueryComponent) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        getOneRuleBuilderPanel();
        oneRule.add(getDeletePanelButton(oneRule, advancedSearchQueryComponent));
    }

    public JPanel getOneRuleBuilderPanel() {
        oneRule.setBorder(BorderFactory.createTitledBorder("Rule"));
        oneRule.setLayout(new BoxLayout(oneRule, BoxLayout.X_AXIS));
        oneRule.setVisible(true);
        setUpDisplay();

        oneRule.add(getEntityComboBox());
        oneRule.add(getEntityPropertiesComboBox());
        oneRule.add(getOperatorsComboBox());
        oneRule.add(firstBracket);
        oneRule.add(getUserInputProperty(userInputProperty));
        oneRule.add(textTO);
        oneRule.add(getUserInputProperty(userInputProperty2));
        oneRule.add(lastBracket);

        oneRule.revalidate();
        oneRule.repaint();

        return oneRule;
    }

    private JComboBox<String> getEntityComboBox() {
        setCorrectDimensions(entityComboBox);

        entityComboBox.addActionListener(e -> {
            setUpEntityPropertiesCombobox((String) entityComboBox.getSelectedItem());
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        return entityComboBox;
    }

    private JComboBox<String> getEntityPropertiesComboBox() {
        setCorrectDimensions(entityPropertiesCombobox);

        entityPropertiesCombobox.addItemListener(e -> {
            setUpOperatorsCombobox((String) entityPropertiesCombobox.getSelectedItem(),
                    (String) entityComboBox.getSelectedItem());
        });
        return entityPropertiesCombobox;
    }

    private JComboBox<String> getOperatorsComboBox() {
        setCorrectDimensions(operatorsComboBox);

        operatorsComboBox.addActionListener(e -> {
            setUserInput2Visible();
            userInputProperty.setVisible(operatorsComboBox.getSelectedItem() != null && isUserInputNeeded());
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        return operatorsComboBox;
    }

    private void setUserInput2Visible(){
        boolean visible = operatorsComboBox.getSelectedItem() != null && isUserInput2needed();
        userInputProperty2.setVisible(visible);
        firstBracket.setVisible(visible);
        lastBracket.setVisible(visible);
        textTO.setVisible(visible);
    }

    private void setUpDisplay(){
        firstBracket.setMaximumSize(new Dimension(10,20));
        lastBracket.setMaximumSize(new Dimension(10,20));
        textTO.setMaximumSize(new Dimension(10,20));

        firstBracket.setEditable(false);
        lastBracket.setEditable(false);
        textTO.setEditable(false);

        firstBracket.setBackground(UIManager.getColor("Panel.background"));
        lastBracket.setBackground(UIManager.getColor("Panel.background"));
        textTO.setBackground(UIManager.getColor("Panel.background"));
    }

    private JTextField getUserInputProperty(JTextField userInputProperty) {
        setCorrectDimensions(userInputProperty);
        // This handles actions like clicking Enter
        userInputProperty.addActionListener(e -> {
                    advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
                }
        );
        // This handles situations like writing a value and clicking 'Submit query' without clicking enter.
        // When the user click the 'Submit query' button, this input loses focus and the query text is updated.
        userInputProperty.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
            }
        });
        return userInputProperty;
    }

    public boolean isUserInputNeeded() {
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && !(operatorSelected.equals("TRUE") || operatorSelected.equals("FALSE"));
    }

    private boolean isUserInput2needed() {
        String operatorSelected = (String) operatorsComboBox.getSelectedItem();
        return operatorSelected != null && (operatorSelected.equals("∈") || operatorSelected.equals("∉"));
    }

    private void setUpEntityPropertiesCombobox(String entitySelected) {
        entityPropertiesCombobox.removeAllItems();

        for (Field field : Field.values()) {
            if (entitySelected != null && field.getEntity().equalsIgnoreCase(entitySelected.trim())) {
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
