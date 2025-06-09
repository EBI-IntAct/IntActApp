package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.units.qual.A;

@AllArgsConstructor
@Getter
@Setter
public class Rule implements RuleComponent {
    String miql;
    String operator;
    String entity;
    String userInput1;
    String userInput2;
    String fieldName;

    @Override
    public Rule getRule(int i) {
        return this;
    }
}