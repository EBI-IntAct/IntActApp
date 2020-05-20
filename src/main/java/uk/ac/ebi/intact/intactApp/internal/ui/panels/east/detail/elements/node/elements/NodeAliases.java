package uk.ac.ebi.intact.intactApp.internal.ui.panels.east.detail.elements.node.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.model.core.IntactNode;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils;

import javax.swing.*;

public class NodeAliases extends AbstractNodeElement {
    private final JsonNode aliases;

    public NodeAliases(IntactNode iNode, OpenBrowser openBrowser, JsonNode aliases) {
        super("Aliases", iNode, openBrowser);
        this.aliases = aliases;
        fillContent();
    }

    @Override
    protected void fillContent() {
        if (aliases != null && !aliases.isNull() && !aliases.isEmpty()) {

            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            GroupUtils.groupElementsInPanel(content, aliases,
                    aliasData -> StringUtils.capitalize(aliasData.get("type").get("shortName").textValue()),
                    (toFill, aliasesOfTypeData) -> {
                        for (JsonNode aliasData : aliasesOfTypeData) {
                            toFill.add(new JLabel(aliasData.get("name").textValue()));
                        }
                    });
        } else {
            setVisible(false);
        }
    }
}
