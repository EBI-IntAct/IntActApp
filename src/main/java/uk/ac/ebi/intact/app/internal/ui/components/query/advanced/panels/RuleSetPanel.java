package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.QueryOperators;

import static uk.ac.ebi.intact.app.internal.ui.components.query.advanced.AdvancedSearchUtils.*;

import javax.swing.*;
import java.util.ArrayList;

@Getter
public class RuleSetPanel {

    private final JPanel ruleSetPanel = new JPanel();

    @Setter
    private QueryOperators queryOperators;

    private final ArrayList<RulePanel> rules = new ArrayList<>();

    private final ArrayList<RuleSetPanel> ruleSetPanels = new ArrayList<>();

    public RuleSetPanel(AdvancedSearchQueryComponent advancedSearchQueryComponent) {
        queryOperators = new QueryOperators(advancedSearchQueryComponent, rules, ruleSetPanels);
        ruleSetPanel.setBorder(BorderFactory.createTitledBorder("Rule Set"));
        ruleSetPanel.setLayout(new BoxLayout(ruleSetPanel, BoxLayout.Y_AXIS));
        ruleSetPanel.add(queryOperators.getButtons(ruleSetPanel));
        ruleSetPanel.add(getDeletePanelButton(ruleSetPanel));
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
        return getQueriesFromRuleBuilders(rules, this.queryOperators.getRuleOperator());
    }

    private String getQueryFromSubRuleSets(){
        if (!ruleSetPanels.isEmpty()) {
            StringBuilder subQuery = new StringBuilder("(");
            for (RuleSetPanel ruleSetPanel : ruleSetPanels) {
                subQuery.append(ruleSetPanel.getQueryFromRules());
            }
            return subQuery + ")";
        }
        else {
            return null;
        }
    }
    
    public void addRulePanel(RulePanel rulePanel) {
        rules.add(rulePanel);
        ruleSetPanel.add(rulePanel.getOneRuleBuilderPanel());
    }

    public void addRuleSetPanel(RuleSetPanel ruleSetPanelToAdd) {
        ruleSetPanels.add(ruleSetPanelToAdd);
        ruleSetPanel.add(ruleSetPanelToAdd.getRuleSetPanel());
        ruleSetPanel.revalidate();
        ruleSetPanel.repaint();
    }
}