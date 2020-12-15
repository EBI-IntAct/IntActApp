package uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.node.attributes.identifiers.panels;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.Identifier;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.utils.GroupUtils;

import java.util.*;
import java.util.stream.Collectors;

public class ReactomeIdentifierPanel extends IdentifierPanel {
    private static final Map<String, Term> reactomeTerms = new HashMap<>();

    protected ReactomeIdentifierPanel(List<Identifier> identifiers, OpenBrowser openBrowser) {
        super(identifiers, openBrowser);
    }

    @Override
    protected void fillContent() {
        identifiers.stream()
                .map(identifier -> identifier.id)
                .filter(id -> !reactomeTerms.containsKey(id))
                .forEach(id -> {
                    JsonNode root = HttpUtils.getJsonForUrl("https://reactome.org/ContentService/data/event/" + id + "/ancestors");
                    if (root != null) {
                        Term term = new Term(root.get(0));
                        reactomeTerms.put(term.id, term);
                    } else {
                        reactomeTerms.put(id, null);
                    }
                });
        List<Term> terms = identifiers.stream()
                .map(identifier -> {
                    Term term = reactomeTerms.get(identifier.id);
                    if (term != null) term.qualifier = identifier.qualifier;
                    return term;
                })
                .collect(Collectors.toList());
        GroupUtils.groupElementsByMultipleKeysInPanel(content, terms, term -> term.topLevelPathways, (toFill, topLevelSubPathways) -> {
            for (Term term : topLevelSubPathways) {
                JLink termLink = new JLink(String.format("%s - %s", term.id, term.name), "https://reactome.org/content/detail/" + term.id, openBrowser,term.qualifier != null && term.qualifier.equals("identity"));
                toFill.add(termLink);
            }
        });
    }


    static class Term {
        final String id;
        final String name;
        final Set<String> topLevelPathways;
        String qualifier;

        public Term(JsonNode reactomeAncestors) {
            JsonNode currentPathway = reactomeAncestors.get(0);
            id = currentPathway.get("stId").textValue();
            name = currentPathway.get("displayName").textValue();
            topLevelPathways = new HashSet<>();
            for (JsonNode pathway : reactomeAncestors) {
                if (pathway.get("schemaClass").textValue().equals("TopLevelPathway")) {
                    topLevelPathways.add(pathway.get("displayName").textValue());
                }
            }
        }
    }
}
