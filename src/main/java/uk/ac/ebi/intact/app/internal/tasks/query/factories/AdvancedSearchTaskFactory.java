package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.TaskIterator;

import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.tasks.query.AdvancedSearchTask;
import uk.ac.ebi.intact.app.internal.ui.components.query.SearchQueryComponent;
import uk.ac.ebi.intact.app.internal.ui.panels.options.OptionsPanel;
import uk.ac.ebi.intact.app.internal.utils.IconUtils;

import javax.swing.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class AdvancedSearchTaskFactory extends AbstractNetworkSearchTaskFactory {
    private static final Icon ICON = IconUtils.createImageIcon("/IntAct/DIGITAL/ICON_PNG/Cropped_Gradient790.png");
    private static final String INTACT_ID = "uk.ac.ebi.intact.advanced.search";
    private static final String INTACT_NAME = "IntAct Advanced Search";
    private static final String INTACT_DESC = "Make an advanced search query";
    private static final URL INTACT_URL = buildUrl();

    private SearchQueryComponent queryComponent = null;
    private Network network;

    Manager manager;

    private static final Logger LOGGER = Logger.getLogger(AdvancedSearchTaskFactory.class.getName());

    private static URL buildUrl() {
        try {
            return new URL("https://www.ebi.ac.uk/intact/home#advanced-search");
        } catch (MalformedURLException e) {
            LOGGER.warning(e.getMessage());
        }
        return null;
    }

    public AdvancedSearchTaskFactory(Manager manager) {
        super(INTACT_ID, INTACT_NAME, INTACT_DESC, ICON, INTACT_URL);
        this.manager = manager;
        this.network = new Network(manager);
    }

    public boolean isReady() {
        return queryComponent.getQueryText() != null && !queryComponent.getQueryText().isEmpty();
    }

    public TaskIterator createTaskIterator() {
        return new TaskIterator(new AdvancedSearchTask(manager, queryComponent.getQueryText(), true));
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
