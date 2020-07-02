package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ImportNetworkTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.ResolveTermsPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TermsResolvingTask extends AbstractTask implements ObservableTask {
    private final Manager manager;
    private final Network network;
    private final String terms;
    private final String panelTitle;
    Map<String, List<Interactor>> interactorsToResolve = null;
    final boolean exactQuery;

    public TermsResolvingTask(Network network, String terms, String panelTitle, boolean exactQuery) {
        this.manager = network.getManager();
        this.network = network;
        this.terms = terms;
        this.panelTitle = panelTitle;
        this.exactQuery = exactQuery;
    }

    @Override
    public void run(TaskMonitor monitor) {
        monitor.setTitle("Solving term ambiguity");
        if (terms.isBlank()) {
            monitor.showMessage(TaskMonitor.Level.WARN, "Empty query");
        } else {
            interactorsToResolve = network.resolveTerms(terms, exactQuery);

            if (showNoResults()) return;
            if (exactQuery && network.hasNoAmbiguity()) {
                importNetwork();
            } else {
                SwingUtilities.invokeLater(() -> {
                    JDialog d = new JDialog();
                    d.setTitle(panelTitle);
                    d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    ResolveTermsPanel panel = new ResolveTermsPanel(manager, network, !exactQuery, !exactQuery);
                    d.setContentPane(panel);
                    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    d.pack();
                    d.setVisible(true);
                });
            }
            if (interactorsToResolve == null || interactorsToResolve.size() == 0) {
                monitor.showMessage(TaskMonitor.Level.ERROR, "Query returned no terms");
            }
        }
    }

    private boolean showNoResults() {
        if (interactorsToResolve == null || interactorsToResolve.isEmpty()) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Your query returned no results",
                    "No results", JOptionPane.ERROR_MESSAGE));
            return true;
        }
        return false;
    }

    private void importNetwork() {
        Map<String, String> acToTerm = new HashMap<>();
        List<String> intactAcs = network.combineAcs(acToTerm);
        TaskFactory factory = new ImportNetworkTaskFactory(network, intactAcs, manager.option.ADD_INTERACTING_PARTNERS.getValue(), null);
        manager.utils.execute(factory.createTaskIterator());
    }


    @Override
    public <T> T getResults(Class<? extends T> type) {
        return null;
    }
}

