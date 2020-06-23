package uk.ac.ebi.intact.app.internal.ui.utils;

import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;
import uk.ac.ebi.intact.app.internal.ui.components.panels.CollapsablePanel;
import uk.ac.ebi.intact.app.internal.ui.components.panels.VerticalPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.ac.ebi.intact.app.internal.ui.panels.detail.AbstractDetailPanel.backgroundColor;

public class GroupUtils {
    public static <E> void groupElementsInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, backgroundColor, panelFillingFunction, CollectionUtils.groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, String> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, CollectionUtils.groupBy(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, backgroundColor, panelFillingFunction, CollectionUtils.groupByMultipleKeys(toGroup, elementToTitle));
    }

    public static <E> void groupElementsByMultipleKeysInPanel(JPanel toFill, Color background, Iterable<E> toGroup, Function<E, Set<String>> elementToTitle, PanelFillingFunction<E> panelFillingFunction) {
        buildSubGroupPanels(toFill, background, panelFillingFunction, CollectionUtils.groupByMultipleKeys(toGroup, elementToTitle));
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
}