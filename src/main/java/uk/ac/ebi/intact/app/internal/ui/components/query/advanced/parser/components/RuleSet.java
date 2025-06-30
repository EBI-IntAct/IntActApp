package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class RuleSet implements RuleComponent {
    public String condition;
    public List<RuleComponent> rules;

    public RuleSet() {
        this.condition = "AND";
        this.rules = new ArrayList<>();
    }

    @Override
    public Rule getRule(int i) {
        return rules.get(i).getRule(i);
    }
}