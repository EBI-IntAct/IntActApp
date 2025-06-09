package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class StackFrame {
    int start;
    RuleSet ruleSet;
}
