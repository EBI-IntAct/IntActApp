package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rule implements RuleComponent {
    String field;
    String operator;
    String entity;
    String value;
    String name;

    public Rule(String field, String operator, String entity, String value, String name) {
        this.field = field;
        this.operator = operator;
        this.entity = entity;
        this.value = value;
        this.name = name;
    }

    @Override
    public Rule getRule(int i) {
        return this;
    }
}