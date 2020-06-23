package uk.ac.ebi.intact.app.internal.model.styles.utils;

import java.awt.*;

public enum InteractionType {
    COLOCOALIZATION("colocalization", "MI_0403", new Color(165, 165, 165), false),
    ASSOCIATION("association", "MI_0914", new Color(97, 131, 196), false),
    PHYSICAL_ASSOCIATION("physical association", "MI_0915", new Color(178, 101, 188), false),
    DIRECT_INTERACTION("direct interaction", "MI_0407", new Color(184, 54, 75), true),
    PHOSPHORYLATION_R("phosphorylation reaction", "MI_0217", new Color(231, 111, 61), true),
    DEPHOSPHORYLATION_R("dephosphorylation reaction", "MI_0203", new Color(231, 111, 61), true),
    PHOSPHORYLATION("phosphorylation", "", new Color(231, 111, 61), false),
    DEPHOSPHORYLATION("dephosphorylation", "", new Color(231, 111, 61), false);

    public final String name;
    public final String MI_ID;
    public final Color defaultColor;
    public final boolean queryChildren;

    InteractionType(String name, String MI_ID, Color defaultColor, boolean queryChildren) {
        this.name = name;
        this.MI_ID = MI_ID;
        this.defaultColor = defaultColor;
        this.queryChildren = queryChildren;
    }
}
