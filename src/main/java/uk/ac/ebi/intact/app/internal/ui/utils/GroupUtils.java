package uk.ac.ebi.intact.app.internal.ui.utils;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.LinePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.ui.panels.detail.sub.panels.AbstractDetailPanel.backgroundColor;
import static uk.ac.ebi.intact.app.internal.utils.CollectionUtils.groupBy;
import static uk.ac.ebi.intact.app.internal.utils.CollectionUtils.groupByMultipleKeys;

public class GroupUtils {
    public static <E> void groupElementsInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, backgroundColor, panelFillingFunction, groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, backgroundColor, panelFillingFunction, groupByMultipleKeys(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupByMultipleKeys(toGroup, elementToTitle));
    }

    private static <E> void buildSubGroupPanels(JPanel toFill, Color background, PanelFillingFunction<E> panelFillingFunction, Map<String, List<E>> groupedElements) {
        for (String elementTitle : groupedElements.keySet().stream().sorted().collect(Collectors.toList())) {
            VerticalPanel panel = new VerticalPanel(background);
            List<E> elementsOfGroup = groupedElements.get(elementTitle);
            panelFillingFunction.apply(panel, elementsOfGroup);
            toFill.add(new CollapsablePanel(String.format("%s (%d)", elementTitle, elementsOfGroup.size()), panel, true));
        }
    }

    public interface PanelFillingFunction<E> {
        void apply(JPanel toFill, Iterable<E> elements);
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, CVTerm> elementToCVTerm, OpenBrowser openBrowser, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, backgroundColor, panelFillingFunction, groupBy(toGroup, elementToCVTerm), openBrowser);
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, CVTerm> elementToCVTerm, OpenBrowser openBrowser, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupBy(toGroup, elementToCVTerm), openBrowser);
    }

    private static <E> void buildSubGroupPanels(JPanel toFill, Color background, PanelFillingFunction<E> panelFillingFunction, Map<CVTerm, List<E>> groupedElements, OpenBrowser openBrowser) {
        for (CVTerm term : groupedElements.keySet().stream().sorted().collect(Collectors.toList())) {
            VerticalPanel panel = new VerticalPanel(background);
            List<E> elementsOfGroup = groupedElements.get(term);
            panelFillingFunction.apply(panel, elementsOfGroup);
            CollapsablePanel collapsablePanel = new CollapsablePanel("", panel, true);
            LinePanel header = new LinePanel(background);
            header.add(new JLink(StringUtils.capitalize(term.value), term.id.getUserAccessURL(), openBrowser));
            header.add(new JLabel(" (" + elementsOfGroup.size() + ")"));
            collapsablePanel.setHeader(header);
            toFill.add(collapsablePanel);
        }
    }
}
