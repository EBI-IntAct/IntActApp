package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleContainer;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RulePanel;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels.RuleSetPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

public class QueryOperators {

    @Getter
    @Setter
    private String ruleOperator = "AND";

    private final JButton andButton = new JButton("AND");
    private final JButton orButton = new JButton("OR");

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    @Getter
    ArrayList<RuleContainer> panels;


    public QueryOperators(AdvancedSearchQueryComponent advancedSearchQueryComponent,
                          ArrayList<RuleContainer> panels) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        this.panels = panels;
    }

    public Box getAndOrButton() {
        Box buttonContainer = Box.createHorizontalBox();


        setButtonIntactPurple(andButton);


        setButtonWhite(orButton);

        andButton.addActionListener(e -> {
            setButtonIntactPurple(andButton);
            setButtonWhite(orButton);
            ruleOperator = "AND";
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        orButton.addActionListener(e -> {
            setButtonWhite(andButton);
            setButtonIntactPurple(orButton);
            ruleOperator = "OR";
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        buttonContainer.add(Box.createHorizontalStrut(3));
        buttonContainer.add(andButton);
        buttonContainer.add(orButton);

        buttonContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        return buttonContainer;
    }

    public Box getRuleAndRuleSetButton(boolean addDeleteButton, RuleSetPanel ruleSetPanel) {
        Box buttonContainer = Box.createHorizontalBox();

        JButton addRuleButton = new JButton("+ Rule");
        setButtonIntactPurple(addRuleButton);

        JButton ruleSetButton = new JButton("+ Ruleset");
        setButtonIntactPurple(ruleSetButton);


        addRuleButton.addActionListener(e -> {
            RulePanel rule = new RulePanel(advancedSearchQueryComponent, ruleSetPanel);
            ruleSetPanel.addRulePanel(rule);
            rule.setEntityComboboxSelected(); //triggers the actionListener to set up the other comboBoxes
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        ruleSetButton.addActionListener(e -> {
            RuleSetPanel ruleSet = new RuleSetPanel(advancedSearchQueryComponent, ruleSetPanel);
            ruleSetPanel.addRuleSetPanel(ruleSet);
            advancedSearchQueryComponent.setQueryText(advancedSearchQueryComponent.getFullQuery());
        });

        buttonContainer.add(addRuleButton);
        buttonContainer.add(Box.createHorizontalStrut(5));
        buttonContainer.add(ruleSetButton);
        if (addDeleteButton) {
            buttonContainer.add(Box.createHorizontalStrut(5));
            buttonContainer.add(getDeletePanelButton( advancedSearchQueryComponent, ruleSetPanel));
        }
        buttonContainer.add(Box.createHorizontalStrut(5));
        buttonContainer.setAlignmentX(Component.RIGHT_ALIGNMENT);

        return buttonContainer;

    }

    public Box getButtons(boolean addDeleteButton, RuleSetPanel ruleSetPanel) {
        Box container = Box.createHorizontalBox();
        container.add(getAndOrButton());
        container.add(Box.createHorizontalGlue());
        container.add(getRuleAndRuleSetButton(addDeleteButton, ruleSetPanel));
        Dimension size = new Dimension(Short.MAX_VALUE, 35);
        container.setMaximumSize(size);
        return container;
    }

    public void updateAndOrButtons() {
        if (ruleOperator.equals("AND")) {
            setButtonIntactPurple(andButton);
            setButtonWhite(orButton);
        } else {
            setButtonWhite(andButton);
            setButtonIntactPurple(orButton);
        }
    }
}
