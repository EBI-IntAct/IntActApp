package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.Field;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Objects;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.getDeletePanelButton;
import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.setCorrectDimensions;

public class RulePanel extends RuleContainer {

    public JComboBox<String> entityComboBox = new JComboBox<>(Field.getEntities());
    public JComboBox<String> entityPropertiesCombobox = new JComboBox<>();
    public JComboBox<String> operatorsComboBox = new JComboBox<>();


    public JLabel firstBracket = new JLabel("[");
    public JTextField userInputProperty = new JTextField();
    public JLabel textTO = new JLabel(" TO ");
    public JTextField userInputProperty2 = new JTextField();
    public JLabel lastBracket = new JLabel("]");

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    public RulePanel(AdvancedSearchQueryComponent advancedSearchQueryComponent, @Nullable RuleSetPanel parent) {
        this.parent = parent;
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Rule"),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)
        ));
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));
        container.setVisible(true);

        container.add(getEntityComboBox());
        container.add(getEntityPropertiesComboBox());
        container.add(getOperatorsComboBox());
        container.add(firstBracket);
        container.add(getUserInputProperty(userInputProperty));
        container.add(textTO);
        container.add(getUserInputProperty(userInputProperty2));
        container.add(lastBracket);
        container.add(Box.createHorizontalGlue());

        container.add(getDeletePanelButton(advancedSearchQueryComponent, this));
        container.add(Box.createHorizontalStrut(5));


        container.setMaximumSize(new Dimension(Integer.MAX_VALUE, container.getPreferredSize().height));
        container.revalidate();
        container.repaint();
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

        entityPropertiesCombobox.addItemListener(e -> setUpOperatorsCombobox(
                (String) entityPropertiesCombobox.getSelectedItem(),
                (String) entityComboBox.getSelectedItem())
        );
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

    private void setUserInput2Visible() {
        boolean visible = operatorsComboBox.getSelectedItem() != null && isUserInput2needed();
        userInputProperty2.setVisible(visible);
        firstBracket.setVisible(visible);
        lastBracket.setVisible(visible);
        textTO.setVisible(visible);
    }

    private JTextField getUserInputProperty(JTextField userInputProperty) {
        setCorrectDimensions(userInputProperty);
        // This handles actions like clicking Enter
        userInputProperty.addActionListener(e -> advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery())
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

    public void setEntityComboboxSelected() {
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
