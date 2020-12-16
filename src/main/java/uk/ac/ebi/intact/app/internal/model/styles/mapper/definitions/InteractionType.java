package uk.ac.ebi.intact.app.internal.model.styles.mapper.definitions;


import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum InteractionType {
    COLOCALIZATION("colocalization", "MI_0403", new Color(216, 216, 216), false, true, null),
    ASSOCIATION("association", "MI_0914", new Color(186, 228, 188), false, true, null),
    PHYSICAL_ASSOCIATION("physical association", "MI_0915",new Color(123, 204, 196), false, true, ASSOCIATION),
    DIRECT_INTERACTION("direct interaction", "MI_0407", new Color(67, 162, 202) , true, true, PHYSICAL_ASSOCIATION),
    ENZYMATIC_REACTION("enzymatic reaction", "MI_0414", new Color(8, 104, 172), true, true, DIRECT_INTERACTION),
    PHOSPHORYLATION_R("phosphorylation reaction", "MI_0217", new Color(253, 141, 60), true, false, ENZYMATIC_REACTION),
    PHOSPHORYLATION("phosphorylation", "", new Color(253, 141, 60), false, true, ENZYMATIC_REACTION),
    DEPHOSPHORYLATION_R("dephosphorylation reaction", "MI_0203",new Color(247, 104, 161), true, false, ENZYMATIC_REACTION),
    DEPHOSPHORYLATION("dephosphorylation", "", new Color(247, 104, 161), false, true, ENZYMATIC_REACTION);

    public final String name;
    public final String MI_ID;
    public final Color defaultColor;
    public final boolean queryChildren;
    public final boolean displayInLegend;
    public final InteractionType parent;

    InteractionType(String name, String MI_ID, Color defaultColor, boolean queryChildren, boolean displayInLegend, InteractionType parent) {
        this.name = name;
        this.MI_ID = MI_ID;
        this.defaultColor = defaultColor;
        this.queryChildren = queryChildren;
        this.displayInLegend = displayInLegend;
        this.parent = parent;
    }


    public static List<InteractionType> getLegendTypes() {
        return Arrays.stream(values()).filter(interactionType -> interactionType.displayInLegend).collect(Collectors.toList());
    }
}
