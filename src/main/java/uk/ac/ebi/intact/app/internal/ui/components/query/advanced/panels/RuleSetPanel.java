package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import lombok.Getter;
import lombok.Setter;
import uk.ac.ebi.intact.app.internal.ui.components.query.AdvancedSearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.components.query.advanced.QueryOperators;

import javax.annotation.Nullable;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Getter
public class RuleSetPanel extends RuleContainer {

    private final AdvancedSearchQueryComponent queryComponent;

    @Setter
    private QueryOperators queryOperators;

    @Getter
    private final ArrayList<RuleContainer> panels = new ArrayList<>();

    public RuleSetPanel(AdvancedSearchQueryComponent advancedSearchQueryComponent, @Nullable RuleSetPanel parent) {
        this.parent = parent;
        queryComponent = advancedSearchQueryComponent;
        queryOperators = new QueryOperators(advancedSearchQueryComponent, panels);
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Rule set"),
                BorderFactory.createEmptyBorder(0, 0, 5, 0)
        ));
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(queryOperators.getButtons(parent != null, this));
        updateHeight();
    }


    public String getQuery() {
        return "(" + this.panels.stream().map(RuleContainer::getQuery).collect(Collectors.joining(" " + queryOperators.getRuleOperator() + " ")) + ")";
    }

    public void addRulePanel(RulePanel rulePanel) {
        panels.add(rulePanel);
        container.add(rulePanel.getContainer());
        updateHeight();
    }

    public void addRuleSetPanel(RuleSetPanel ruleSetPanelToAdd) {
        panels.add(ruleSetPanelToAdd);
        container.add(ruleSetPanelToAdd.getContainer());
        updateHeight();
    }

    public void clearContent() {
        panels.forEach(panel -> container.remove(panel.getContainer()));
        panels.clear();
    }

    public void updateHeight() {
        container.revalidate();
        container.setMaximumSize(new Dimension(Short.MAX_VALUE, container.getPreferredSize().height));
        container.repaint();
        if (parent != null) parent.updateHeight();
    }
}