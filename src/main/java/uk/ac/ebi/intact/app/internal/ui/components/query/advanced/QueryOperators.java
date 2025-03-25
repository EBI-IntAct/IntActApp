package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.setButtonIntactPurple;
import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.setButtonWhite;

public class QueryOperators {

    @Getter
    private String ruleOperator = "AND";

    AdvancedSearchQueryComponent advancedSearchQueryComponent;

    @Getter
    private final ArrayList<OneRuleBuilderPanel> rules;

    @Getter
    private final ArrayList<RuleSetBuilder> ruleSetBuilders;


    public QueryOperators(AdvancedSearchQueryComponent advancedSearchQueryComponent,
                          ArrayList<OneRuleBuilderPanel> rules,
                          ArrayList<RuleSetBuilder> ruleSetBuilders) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
        this.rules = rules;
        this.ruleSetBuilders = ruleSetBuilders;
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
            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
        });

        orButton.addActionListener(e -> {
            setButtonWhite(andButton);
            setButtonIntactPurple(orButton);
            ruleOperator = "OR";
            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
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
            OneRuleBuilderPanel rule = new OneRuleBuilderPanel(advancedSearchQueryComponent);

            parentContainer.add(rule.getOneRuleBuilderPanel());
            rule.setEntityComboboxSelected(); //triggers the actionListener to set up the other comboBoxes

            rules.add(rule);
            parentContainer.revalidate();
            parentContainer.repaint();

            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
        });

        ruleSetButton.addActionListener(e -> {
            RuleSetBuilder ruleSet = new RuleSetBuilder(
                    advancedSearchQueryComponent);

            parentContainer.add(ruleSet.getRuleSetPanel());

            ruleSetBuilders.add(ruleSet);
            parentContainer.revalidate();
            parentContainer.repaint();

            advancedSearchQueryComponent.getQueryTextField()
                    .setText(advancedSearchQueryComponent.getFullQuery());
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
