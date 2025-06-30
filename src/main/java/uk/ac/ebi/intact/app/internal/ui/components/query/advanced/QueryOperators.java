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

    private final JButton andButton = new JButton("AND");
    private final JButton orButton = new JButton("OR");

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


        setButtonIntactPurple(andButton);


        setButtonWhite(orButton);

        andButton.addActionListener(e -> {
            setButtonIntactPurple(andButton);
            setButtonWhite(orButton);
            ruleOperator = "AND";
            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
            advancedSearchQueryComponent.highlightQuery(advancedSearchQueryComponent.getQueryTextField().getText());
        });

        orButton.addActionListener(e -> {
            setButtonWhite(andButton);
            setButtonIntactPurple(orButton);
            ruleOperator = "OR";
            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
            advancedSearchQueryComponent.highlightQuery(advancedSearchQueryComponent.getQueryTextField().getText());
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
            advancedSearchQueryComponent.highlightQuery(advancedSearchQueryComponent.getQueryTextField().getText());
        });

        ruleSetButton.addActionListener(e -> {
            RuleSetPanel ruleSet = new RuleSetPanel(
                    advancedSearchQueryComponent);

            parentContainer.add(ruleSet.getRuleSetPanel());

            panels.add(ruleSet);
            parentContainer.revalidate();
            parentContainer.repaint();

            advancedSearchQueryComponent.getQueryTextField().setText(advancedSearchQueryComponent.getFullQuery());
            advancedSearchQueryComponent.highlightQuery(advancedSearchQueryComponent.getQueryTextField().getText());
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
