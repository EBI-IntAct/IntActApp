package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ImportNetworkTaskFactory;
import uk.ac.ebi.intact.app.internal.ui.panels.terms.resolution.ResolveTermsPanel;
import uk.ac.ebi.intact.app.internal.utils.CollectionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TermsResolvingTask extends AbstractTask implements ObservableTask {
    private final Manager manager;
    private final Network network;
    private final String terms;
    private final String panelTitle;
    Map<String, List<Interactor>> interactorsToResolve = null;
    Map<String, Integer> totalInteractors = new HashMap<>();
    final boolean exactQuery;

    public TermsResolvingTask(Network network, String terms, String panelTitle, boolean exactQuery) {
        this.manager = network.manager;
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
            interactorsToResolve = resolveTerms(terms, manager.option.MAX_INTERACTOR_PER_TERM.getValue());

            if (showNoResults()) return;
            if (exactQuery && hasNoAmbiguity()) {
                importNetwork();
            } else {
                SwingUtilities.invokeLater(() -> {
                    JDialog d = new JDialog();
                    d.setTitle(panelTitle);
                    d.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                    ResolveTermsPanel panel = new ResolveTermsPanel(manager, network, !exactQuery, !exactQuery, this);
                    d.setContentPane(panel);
                    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    d.pack();
                    panel.setupDefaultButton();
                    d.setVisible(true);
                });
            }
            if (interactorsToResolve == null || interactorsToResolve.size() == 0) {
                monitor.showMessage(TaskMonitor.Level.ERROR, "Query returned no terms");
            }
        }
    }

    private boolean showNoResults() {
        if (interactorsToResolve == null || interactorsToResolve.values().stream().allMatch(List::isEmpty)) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "Your query returned no results",
                    "No results", JOptionPane.ERROR_MESSAGE));
            return true;
        }
        return false;
    }

    private void importNetwork() {
        TaskFactory factory = new ImportNetworkTaskFactory(network,
                interactorsToResolve.values().stream()
                        .flatMap(List::stream)
                        .map(interactor -> interactor.ac)
                        .collect(Collectors.toList()),
                manager.option.ADD_INTERACTING_PARTNERS.getValue(), getName());
        manager.utils.execute(factory.createTaskIterator());
    }

    public String getName() {
        String joinedTerms = String.join(", ", interactorsToResolve.keySet());
        if (joinedTerms.length() > 30) joinedTerms = joinedTerms.substring(0, 27) + "...";
        return "IntAct Network - " + joinedTerms;
    }


    @Override
    public <T> T getResults(Class<? extends T> type) {
        return null;
    }

    public Map<String, List<Interactor>> resolveTerms(final String terms, int maxInteractorsPerTerm) {
        Map<Object, Object> resolverData = new HashMap<>();
        resolverData.put("query", formatTerms(terms));
        resolverData.put("fuzzySearch", !exactQuery);
        resolverData.put("pageSize", maxInteractorsPerTerm);
        interactorsToResolve = Interactor.getInteractorsToResolve(HttpUtils.postJSON(Manager.INTACT_INTERACTOR_WS + "list/resolve", resolverData, manager), totalInteractors);
        network.completeMissingNodeColorsFromInteractors(interactorsToResolve);
        return interactorsToResolve;
    }

    private static String formatTerms(String terms) {
        String commaSeparatedTerms = terms.replaceAll("\n", ",").toUpperCase(); //TODO Remove replacement when backend handle \n //TODO Remove upper case when handled by backend
        if (commaSeparatedTerms.contains(","))
            return commaSeparatedTerms.replaceAll("\"", ""); //TODO Remove quote suppression when backend handle multiple quoted terms
        return commaSeparatedTerms;
    }

    public Map<String, List<Interactor>> completeAdditionalInteractors(List<String> termsToComplete, int maxInteractorPerTerm, Consumer<Integer> numberOfNewInteractorsConsumer, BooleanSupplier isCancelled) {
        HashMap<String, List<Interactor>> newInteractors = new HashMap<>();
        completeAdditionalInteractors(termsToComplete, exactQuery, newInteractors, 1, maxInteractorPerTerm, numberOfNewInteractorsConsumer, isCancelled);
        return newInteractors;
    }

    private void completeAdditionalInteractors(List<String> termsToComplete, boolean exactQuery, Map<String, List<Interactor>> newInteractors, int page, int maxInteractorPerTerm, Consumer<Integer> numberOfNewInteractorsConsumer, BooleanSupplier isCancelled) {
        if (isCancelled != null && isCancelled.getAsBoolean()) return;
        Map<Object, Object> resolverData = new HashMap<>();
        resolverData.put("query", buildQuery(termsToComplete));
        resolverData.put("fuzzySearch", !exactQuery);
        resolverData.put("pageSize", maxInteractorPerTerm);
        resolverData.put("page", page);
        Map<String, List<Interactor>> additionalInteractors = Interactor.getInteractorsToResolve(HttpUtils.postJSON(Manager.INTACT_INTERACTOR_WS + "list/resolve", resolverData, manager), totalInteractors);
        termsToComplete.removeIf(term -> additionalInteractors.get(term).isEmpty());
        additionalInteractors.forEach((term, interactors) -> {
            CollectionUtils.addAllToGroups(newInteractors, interactors, interactor -> term);
            CollectionUtils.addAllToGroups(interactorsToResolve, interactors, interactor -> term);
        });
        if (numberOfNewInteractorsConsumer != null)
            numberOfNewInteractorsConsumer.accept(newInteractors.values().stream().mapToInt(List::size).sum());
        if (!termsToComplete.isEmpty())
            completeAdditionalInteractors(termsToComplete, exactQuery, newInteractors, page + 1, maxInteractorPerTerm, numberOfNewInteractorsConsumer, isCancelled);

    }

    private static String buildQuery(List<String> termsToComplete) {
        if (termsToComplete.size() == 1) return String.format("\"%s\"", termsToComplete.get(0));
        return String.join(",", termsToComplete);
    }

    public Map<String, List<Interactor>> getInteractorsToResolve() {
        return interactorsToResolve;
    }

    public Map<String, Integer> getTotalInteractors() {
        return totalInteractors;
    }

    public boolean hasNoAmbiguity() {
        return interactorsToResolve.values().stream().allMatch(interactors -> interactors.size() <= 1);
    }
}

