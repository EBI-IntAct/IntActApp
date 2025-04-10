package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import lombok.Setter;

import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
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

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    @Getter
    ArrayList<Object> panels;


    public QueryOperators(AdvancedSearchQueryComponent advancedSearchQueryComponent,
                          ArrayList<Object> panels) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        this.panels = panels;
    }

    public JPanel getAndOrButton() {
        JPanel buttonContainer = new JPanel();

        JButton andButton = new JButton("AND");
        setButtonIntactPurple(andButton);

        JButton orButton = new JButton("OR");
        setButtonWhite(orButton);

        andButton.addActionListener(e -> {
            setButtonIntactPurple(andButton);
            setButtonWhite(orButton);
            ruleOperator = "AND";
            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
        });

        orButton.addActionListener(e -> {
            setButtonWhite(andButton);
            setButtonIntactPurple(orButton);
            ruleOperator = "OR";
            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
        });

        buttonContainer.add(andButton);
        buttonContainer.add(orButton);

        return buttonContainer;
    }

    public JPanel getRuleAndRuleSetButton(JPanel parentContainer) {
        JPanel buttonContainer = new JPanel();

        JButton addRuleButton = new JButton("+ Rule");
        setButtonIntactPurple(addRuleButton);

        JButton ruleSetButton = new JButton("+ Ruleset");
        setButtonIntactPurple(ruleSetButton);


        addRuleButton.addActionListener(e -> {
            RulePanel rule = new RulePanel(advancedSearchQueryComponent);

            parentContainer.add(rule.getOneRuleBuilderPanel());
            rule.setEntityComboboxSelected(); //triggers the actionListener to set up the other comboBoxes

            panels.add(rule);

            parentContainer.revalidate();
            parentContainer.repaint();

            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
        });

        ruleSetButton.addActionListener(e -> {
            RuleSetPanel ruleSet = new RuleSetPanel(
                    advancedSearchQueryComponent);

            parentContainer.add(ruleSet.getRuleSetPanel());

            panels.add(ruleSet);
            parentContainer.revalidate();
            parentContainer.repaint();

            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
        });

        buttonContainer.add(addRuleButton);
        buttonContainer.add(ruleSetButton);

        return buttonContainer;

    }

    public JPanel getButtons(JPanel parentContainer) {
        JPanel container = new JPanel();
        container.setLayout(new GridLayout(1,2));
        container.add(getAndOrButton());
        container.add(getRuleAndRuleSetButton(parentContainer));
        return container;
    }

}
