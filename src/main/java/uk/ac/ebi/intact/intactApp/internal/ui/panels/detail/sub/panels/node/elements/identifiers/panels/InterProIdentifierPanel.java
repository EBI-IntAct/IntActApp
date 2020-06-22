package uk.ac.ebi.intact.intactApp.internal.ui.panels.detail.sub.panels.node.elements.identifiers.panels;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.core.Identifier;
import uk.ac.ebi.intact.intactApp.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.intactApp.internal.ui.utils.GroupUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InterProIdentifierPanel extends IdentifierPanel {
    private static final Map<String, Term> interProTerms = new HashMap<>();

    protected InterProIdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(identifiers, openBrowser);
    }

    @Override
    protected void fillContent() {
        identifiers.stream()
                .map(identifier -> identifier.id)
                .filter(id -> !interProTerms.containsKey(id))
                .forEach(id -> {
                    try {
                        JsonNode root = HttpUtils.getJsonForUrl("https://www.ebi.ac.uk/interpro/api/entry/InterPro/" + id + "?format=json");
                        if (root != null) {
                            Term term = new Term(root.get("metadata"));
                            interProTerms.put(term.accession, term);
                        }
                    } catch (Exception ignored) {
                    }

                });
        List<Term> terms = identifiers.stream()
                .map(identifier -> interProTerms.get(identifier.id))
                .collect(Collectors.toList());
        GroupUtils.groupElementsInPanel(content, terms, term -> term.type, (toFill, typeTerms) -> {
            for (Term term : typeTerms) {
                JLink termLink = new JLink(String.format("%s - %s", term.accession, term.name), "https://www.ebi.ac.uk/interpro/entry/InterPro/" + term.accession, openBrowser);
                termLink.setToolTipText(term.description);
                toFill.add(termLink);
            }
        });
    }


    static class Term {
        final String accession;
        final String name;
        final String description;
        final String type;

        public Term(JsonNode interProNode) {
            accession = interProNode.get("accession").textValue();
            name = interProNode.get("name").get("name").textValue();
            description = "<html>" + interProNode.get("description").get(0).textValue().replaceAll("\\s*\\[\\[[a-zA-Z0-9:, \\[\\]]*]]", "") + "</html>";
            type = StringUtils.capitalize(interProNode.get("type").textValue()).replaceAll("_", " ");
        }
    }
}
