package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class QueryComponents {
    private String entity;
    private String operator;
    private String userInput;
    private String userInput2;
    private boolean negated;
    private String name;

    @Override
    public String toString() {
        return "Entity: " + entity + ", Operator: " + operator + ", UserInput: " + userInput +
                (userInput2 != null ? ", UserInput2: " + userInput2 : "");
    }
}
