package uk.ac.ebi.intact.intactApp.internal.tasks.query.factories;

import org.apache.log4j.Logger;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.*;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.Interactor;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.query.TermsResolvingTask;
import uk.ac.ebi.intact.intactApp.internal.ui.SearchQueryComponent;
import uk.ac.ebi.intact.intactApp.internal.ui.panels.terms.resolution.ResolveTermsPanel;
import uk.ac.ebi.intact.intactApp.internal.utils.IconUtils;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IntactExactQueryTaskFactory extends AbstractNetworkSearchTaskFactory implements TaskObserver {
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


    private final Logger logger = Logger.getLogger(CyUserLog.NAME);
    IntactManager manager;
    private IntactNetwork intactNetwork = null;
    private SearchQueryComponent queryComponent = null;

    public IntactExactQueryTaskFactory(IntactManager manager) {
        super(INTACT_ID, INTACT_NAME, INTACT_DESC, icon, INTACT_URL);
        this.manager = manager;
    }

    public boolean isReady() {
        return queryComponent.getQueryText() != null && queryComponent.getQueryText().length() > 0;
    }

    public TaskIterator createTaskIterator() {
        String terms = queryComponent.getQueryText();

        intactNetwork = new IntactNetwork(manager);
        return new TaskIterator(new TermsResolvingTask(intactNetwork, 0, terms, true));
    }


    public JComponent getQueryComponent() {
        if (queryComponent == null)
            queryComponent = new SearchQueryComponent();
        return queryComponent;
    }

    public TaskObserver getTaskObserver() {
        return this;
    }


    @Override
    public JComponent getOptionsComponent() {
        return new OptionsPanel(manager, IntactOptionManager.Scope.SEARCH);
    }
}
