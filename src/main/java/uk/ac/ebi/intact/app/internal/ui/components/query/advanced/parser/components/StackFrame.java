package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StackFrame {
    int start;
    RuleSet ruleSet;

    public StackFrame(int start, RuleSet ruleSet) {
        this.start = start;
        this.ruleSet = ruleSet;
    }
}
