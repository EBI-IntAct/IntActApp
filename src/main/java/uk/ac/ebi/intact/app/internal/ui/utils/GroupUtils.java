package uk.ac.ebi.intact.app.internal.ui.utils;

import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.GroupingCVPanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.model.styles.UIColors.lightBackground;
import static uk.ac.ebi.intact.app.internal.utils.CollectionUtils.groupBy;
import static uk.ac.ebi.intact.app.internal.utils.CollectionUtils.groupByMultipleKeys;

public class GroupUtils {
    public static <E> void groupElementsInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, lightBackground, panelFillingFunction, groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, lightBackground, panelFillingFunction, groupByMultipleKeys(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupByMultipleKeys(toGroup, elementToTitle));
    }

    private static <E> void buildSubGroupPanels(JPanel toFill, Color background, PanelFillingFunction<E> panelFillingFunction, Map<String, List<E>> groupedElements) {
        for (String elementTitle : groupedElements.keySet().stream().sorted().collect(Collectors.toList())) {
            VerticalPanel panel = new VerticalPanel(background);
            List<E> elementsOfGroup = groupedElements.get(elementTitle);
            toFill.add(new CollapsablePanel(String.format("%s (%d)", elementTitle, elementsOfGroup.size()), panel, true));
            panelFillingFunction.apply(panel, elementsOfGroup);
        }
    }

    public interface PanelFillingFunction<E> {
        void apply(JPanel toFill, Iterable<E> elements);
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, CVTerm> elementToCVTerm, OpenBrowser openBrowser, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, lightBackground, panelFillingFunction, groupBy(toGroup, elementToCVTerm), openBrowser);
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, CVTerm> elementToCVTerm, OpenBrowser openBrowser, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, groupBy(toGroup, elementToCVTerm), openBrowser);
    }

    private static <E> void buildSubGroupPanels(JPanel toFill, Color background, PanelFillingFunction<E> panelFillingFunction, Map<CVTerm, List<E>> groupedElements, OpenBrowser openBrowser) {
        for (CVTerm term : groupedElements.keySet().stream().sorted().collect(Collectors.toList())) {
            VerticalPanel panel = new VerticalPanel(background);
            List<E> elementsOfGroup = groupedElements.get(term);
            panelFillingFunction.apply(panel, elementsOfGroup);
            toFill.add(new GroupingCVPanel<E>(panel, term, elementsOfGroup, background, true, openBrowser));
        }
    }
}
