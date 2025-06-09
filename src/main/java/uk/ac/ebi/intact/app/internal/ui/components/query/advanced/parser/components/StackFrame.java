package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class StackFrame {
    int start;
    RuleSet ruleSet;
}
