package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.tasks.query.TermsResolvingTask;
import uk.ac.ebi.intact.app.internal.ui.components.query.SearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.panels.options.OptionsPanel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

public class ExactQueryTaskFactory extends AbstractNetworkSearchTaskFactory {
    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/ICON_PNG/Cropped_Gradient790.png");
    static String INTACT_ID = "uk.ac.ebi.intact.query";
    static URL INTACT_URL;
    static String INTACT_NAME = "IntAct Exact Query";
    static String INTACT_DESC = "Query an IntAct network form a list of interactors designated either by their id or their name.";

    static {
        try {
            INTACT_URL = new URL("https://www.ebi.ac.uk/intact/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    Manager manager;
    private SearchQueryComponent queryComponent = null;

    public ExactQueryTaskFactory(Manager manager) {
        super(INTACT_ID, INTACT_NAME, INTACT_DESC, icon, INTACT_URL);
        this.manager = manager;
    }

    public boolean isReady() {
        return queryComponent.getQueryText() != null && queryComponent.getQueryText().length() > 0;
    }

    public TaskIterator createTaskIterator() {
        String terms = queryComponent.getQueryText();
        return new TaskIterator(new TermsResolvingTask(new Network(manager), terms, "Exact Query Preview", true));
    }

    public JComponent getQueryComponent() {
        if (queryComponent == null) queryComponent = new SearchQueryComponent();
        return queryComponent;
    }

    @Override
    public JComponent getOptionsComponent() {
        return new OptionsPanel(manager, OptionManager.Scope.SEARCH);
    }
}
