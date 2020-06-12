package uk.ac.ebi.intact.intactApp.internal.tasks.query.factories;

import org.cytoscape.application.swing.search.AbstractNetworkSearchTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
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
import java.util.List;
import java.util.Map;

public class IntactBroadSearchTaskFactory extends AbstractNetworkSearchTaskFactory implements TaskObserver {
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
    private IntactNetwork intactNetwork = null;
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

        intactNetwork = new IntactNetwork(manager);
        return new TaskIterator(new TermsResolvingTask(intactNetwork, 0, terms, false));
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
    public void allFinished(FinishStatus finishStatus) {
    }


    @Override
    public void taskFinished(ObservableTask task) {
        if (!(task instanceof TermsResolvingTask)) {
            return;
        }

        Map<String, List<Interactor>> interactorsToResolve = intactNetwork.getInteractorsToResolve();
        if (showNoResults(interactorsToResolve)) return;
        intactNetwork.hasNoAmbiguity();
        SwingUtilities.invokeLater(() -> {
            JDialog d = new JDialog();
            d.setTitle("Query preview");
            d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
            ResolveTermsPanel panel = new ResolveTermsPanel(manager, intactNetwork);
            d.setContentPane(panel);
            d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            d.pack();
            d.setVisible(true);
        });

    }

    private boolean showNoResults(Map<String, List<Interactor>> resolvedInteractors) {
        if (resolvedInteractors == null || resolvedInteractors.size() == 0) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Your query returned no results",
                    "No results", JOptionPane.ERROR_MESSAGE));
            return true;
        }
        return false;
    }
}
