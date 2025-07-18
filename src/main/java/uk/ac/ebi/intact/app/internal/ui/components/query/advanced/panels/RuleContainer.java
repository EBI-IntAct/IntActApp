package uk.ac.ebi.intact.app.internal.ui.components.query.advanced.panels;

import lombok.Getter;

import javax.annotation.Nullable;
import javax.swing.*;

public abstract class RuleContainer {
    @Getter
    protected JPanel container = new JPanel();
    @Nullable
    @Getter
    protected RuleSetPanel parent;

    abstract String getQuery();

    public void delete() {
        if (parent != null) {
            parent.getPanels().remove(this);
            parent.getContainer().remove(container);
            parent.updateHeight();
        }
    }
}
