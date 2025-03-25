package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;
import java.util.ArrayList;

public class RuleSetBuilder {

    private final JPanel ruleSetPanel = new JPanel();
    private final String ruleOperator = "AND";
    private final AdvancedSearchQueryComponent advancedSearchQueryComponent;

    @Getter
    private final ArrayList<OneRuleBuilderPanel> rules = new ArrayList<>();

    @Getter
    private final ArrayList<RuleSetBuilder> ruleSetBuilders = new ArrayList<>();

    public RuleSetBuilder(AdvancedSearchQueryComponent advancedSearchQueryComponent) {
        this.advancedSearchQueryComponent = advancedSearchQueryComponent;
    }

    public JPanel getRuleSetPanel() {
        ruleSetPanel.setBorder(BorderFactory.createTitledBorder("Rule Set"));
        ruleSetPanel.setLayout(new BoxLayout(ruleSetPanel, BoxLayout.Y_AXIS));
        ruleSetPanel.add(new QueryOperators(advancedSearchQueryComponent, rules, ruleSetBuilders).getButtons(ruleSetPanel));
        return ruleSetPanel;
    }

    public String getQuery() {
        return "(" + getQueriesFromRuleBuilders(rules, ruleOperator) + ")";
    }
}

