package uk.ac.ebi.intact.app.internal.ui.components.panels;

import org.apache.commons.lang3.StringUtils;
import org.cytoscape.util.swing.OpenBrowser;
import uk.ac.ebi.intact.app.internal.model.core.identifiers.ontology.CVTerm;
import uk.ac.ebi.intact.app.internal.ui.components.labels.JLink;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Function;

public class GroupingCVPanel<E> extends CollapsablePanel {

    public GroupingCVPanel(JPanel content, CVTerm term, List<E> elements, Color background, boolean collapsed, OpenBrowser openBrowser) {
        this(content, term, cvTerm -> StringUtils.capitalize(cvTerm.value), elements, background, collapsed, openBrowser);
    }

    public GroupingCVPanel(JPanel content, CVTerm term, Function<CVTerm, String> termToTitle, List<E> elements, Color background, boolean collapsed, OpenBrowser openBrowser) {
        super("", content, collapsed);
        LinePanel header = new LinePanel(background);
        header.add(new JLink(termToTitle.apply(term), term.id.getUserAccessURL(), openBrowser));
        header.add(new JLabel(" (" + elements.size() + ")"));
        setHeader(header);
    }
}
