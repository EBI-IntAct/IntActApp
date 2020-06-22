package uk.ac.ebi.intact.intactApp.internal.tasks.query.factories;

import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.managers.sub.managers.IntactOptionManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.query.TermsResolvingTask;
import uk.ac.ebi.intact.intactApp.internal.ui.SearchQueryComponent;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.options.OptionsPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;

public class IntactBroadSearchTaskFactory extends AbstractNetworkSearchTaskFactory {
    private static final Icon icon = IconUtils.createImageIcon("/IntAct/DIGITAL/ICON_PNG/Cropped_Gradient790.png");
    static String INTACT_ID = "uk.ac.ebi.intact.search";
    static URL INTACT_URL;
    static String INTACT_NAME = "IntAct Broad Search";
    static String INTACT_DESC = "Search broadly all interactors which have given terms inside their name, ids or description, and build the network around";

    static {
        try {
            INTACT_URL = new URL("https://www.ebi.ac.uk/intact/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    IntactManager manager;
    private SearchQueryComponent queryComponent = null;

    public IntactBroadSearchTaskFactory(IntactManager manager) {
        super(INTACT_ID, INTACT_NAME, INTACT_DESC, icon, INTACT_URL);
        this.manager = manager;
    }

    public boolean isReady() {
        return queryComponent.getQueryText() != null && queryComponent.getQueryText().length() > 0;
    }

    public TaskIterator createTaskIterator() {
        String terms = queryComponent.getQueryText();
        return new TaskIterator(new TermsResolvingTask(new IntactNetwork(manager), terms, "Query preview", false));
    }


    public JComponent getQueryComponent() {
        if (queryComponent == null)
            queryComponent = new SearchQueryComponent();
        return queryComponent;
    }

    @Override
    public JComponent getOptionsComponent() {
        return new OptionsPanel(manager, IntactOptionManager.Scope.SEARCH);
    }
}
