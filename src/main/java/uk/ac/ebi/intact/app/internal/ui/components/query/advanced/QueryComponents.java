package uk.ac.ebi.intact.app.internal.ui.components.query.advanced;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class QueryComponents {
    private String entity;
    private String operator;
    private String name;
    private String userInput;
    private String userInput2;
    private boolean negated;
    private boolean isRuleSet;
    private QueryComponents parent;
    private List<QueryComponents> children;

    public QueryComponents() {
        this.children = new ArrayList<>();
    }

    public void addChild(QueryComponents child) {
        if (child != null) {
            child.setParent(this);
            this.children.add(child);
        }
    }
}
