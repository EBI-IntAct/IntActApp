package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.QueryOperators;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public class RuleSetPanel {

    private final JPanel ruleSetPanel = new JPanel();

    @Setter
    private QueryOperators queryOperators;

    @Getter
    private final ArrayList<Object> panels = new ArrayList<>();

    public RuleSetPanel(AdvancedSearchQueryComponent advancedSearchQueryComponent) {
        queryOperators = new QueryOperators(advancedSearchQueryComponent, panels);
        ruleSetPanel.setBorder(BorderFactory.createTitledBorder("Rule Set"));
        ruleSetPanel.setLayout(new BoxLayout(ruleSetPanel, BoxLayout.Y_AXIS));
        ruleSetPanel.add(queryOperators.getButtons(ruleSetPanel));
        ruleSetPanel.add(getDeletePanelButton(ruleSetPanel, advancedSearchQueryComponent));
    }

    public String getQuery(){
        String queryFromSubRuleSets = getQueryFromSubRuleSets();
        if (queryFromSubRuleSets != null) {
            return "(" + getQueryFromRules() + " " + this.queryOperators.getRuleOperator() + " " + getQueryFromSubRuleSets() + ")";
        } else {
            return "(" + getQueryFromRules() + ")";
        }
    }

    private String getQueryFromRules() {
        ArrayList<Object> rules = panels.stream()
                .filter(panel -> panel instanceof RulePanel)
                .map(panel -> (RulePanel) panel)
                .collect(Collectors.toCollection(ArrayList::new));

        return getQueriesFromRuleBuilders(rules, this.queryOperators.getRuleOperator());
    }

    private String getQueryFromSubRuleSets(){
        ArrayList<RuleSetPanel> subRuleSets = panels.stream()
                .filter(panel -> panel instanceof RuleSetPanel)
                .map(panel -> (RuleSetPanel) panel)
                .collect(Collectors.toCollection(ArrayList::new));

        if (!subRuleSets.isEmpty()) {
            StringBuilder subQuery = new StringBuilder();
            for (RuleSetPanel subRuleSet : subRuleSets) {
                subQuery.append(subRuleSet.getQuery());
            }
            return subQuery.toString();
        }

        return null;
    }
    
    public void addRulePanel(RulePanel rulePanel) {
        panels.add(rulePanel);
        ruleSetPanel.add(rulePanel.getOneRuleBuilderPanel());
    }

    public void addRuleSetPanel(RuleSetPanel ruleSetPanelToAdd) {
        panels.add(ruleSetPanelToAdd);
        ruleSetPanel.add(ruleSetPanelToAdd.getRuleSetPanel());
        ruleSetPanel.revalidate();
        ruleSetPanel.repaint();
    }
}