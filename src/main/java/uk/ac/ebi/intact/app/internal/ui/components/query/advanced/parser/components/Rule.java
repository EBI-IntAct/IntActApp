package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.parser.components;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Rule implements RuleComponent {
    String miql;
    String operator;
    String entity;
    String userInput1;
    String userInput2;
    String name;

    public Rule(String miql, String operator, String entity, String userInput1, String userInput2, String name) {
        this.miql = miql;
        this.operator = operator;
        this.entity = entity;
        this.userInput1 = userInput1;
        this.userInput2 = userInput2;
        this.name = name;
    }

    @Override
    public Rule getRule(int i) {
        return this;
    }
}