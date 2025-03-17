package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

public class QueryComponents {
    private String entity;
    private String operator;
    private String userInput;
    private String userInput2;
    private boolean negated;

    // Getters and setters
    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getUserInput() { return userInput; }
    public void setUserInput(String userInput) { this.userInput = userInput; }

    public String getUserInput2() { return userInput2; }
    public void setUserInput2(String userInput2) { this.userInput2 = userInput2; }

    public boolean isNegated() { return negated; }
    public void setNegated(boolean negated) { this.negated = negated; }

    @Override
    public String toString() {
        return "Entity: " + entity + ", Operator: " + operator + ", UserInput: " + userInput +
                (userInput2 != null ? ", UserInput2: " + userInput2 : "");
    }
}
