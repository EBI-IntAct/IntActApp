package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.elements.identifiers.panels;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.Identifier;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GOIdentifierPanel extends IdentifierPanel {
    private static final Map<String, Term> geneOntologyTerms = new HashMap<>();

    protected GOIdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(identifiers, openBrowser);
    }

    @Override
    protected void fillContent() {
        String ids = identifiers.stream()
                .map(identifier -> identifier.id)
                .filter(s -> !geneOntologyTerms.containsKey(s))
                .collect(Collectors.joining(","));

        JsonNode root = HttpUtils.getJsonForUrl("https://www.ebi.ac.uk/QuickGO/services/ontology/go/terms/" + URLEncoder.encode(ids, StandardCharsets.UTF_8));
        if (root != null) {
            for (JsonNode goNode : root.get("results")) {
                Term term = new Term(goNode);
                geneOntologyTerms.put(term.id, term);
            }
        }
        List<Term> terms = identifiers.stream()
                .map(identifier -> geneOntologyTerms.get(identifier.id))
                .collect(Collectors.toList());

        GroupUtils.groupElementsInPanel(content, terms, term -> term.aspect, (toFill, aspectTerms) -> {
            for (Term term : aspectTerms) {
                JLink termLink = new JLink(String.format("%s - %s", term.id, term.name), "https://www.ebi.ac.uk/QuickGO/term/" + URLEncoder.encode(term.id, StandardCharsets.UTF_8), openBrowser);
                termLink.setToolTipText(term.definition);
                toFill.add(termLink);
            }
        });


    }


    static class Term {
        final String id;
        final String name;
        final String definition;
        final String aspect;

        public Term(JsonNode goNode) {
            id = goNode.get("id").textValue();
            name = goNode.get("name").textValue();
            definition = goNode.get("definition").get("text").textValue();
            aspect = StringUtils.capitalize(goNode.get("aspect").textValue()).replaceAll("_", " ");
        }
    }
}
