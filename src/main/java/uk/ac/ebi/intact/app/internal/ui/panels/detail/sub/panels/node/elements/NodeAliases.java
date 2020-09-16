package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Node;
import uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils;

import javax.swing.*;

public class NodeAliases extends AbstractNodeElement {
    private final JsonNode aliases;

    public NodeAliases(Node node, OpenBrowser openBrowser, JsonNode aliases) {
        super("Aliases", node, openBrowser);
        this.aliases = aliases;
        if (aliases == null) setVisible(false);
        else fillContent();
    }

    @Override
    protected void fillContent() {
        if (aliases != null && !aliases.isNull() && !aliases.isEmpty()) {

            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
            GroupUtils.groupElementsInPanel(content, aliases,
                    aliasData -> StringUtils.capitalize(aliasData.get("type").get("shortName").textValue().replaceAll("iupac", "IUPAC").replaceAll("orf", "ORF")),
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
