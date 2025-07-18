package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ImportNetworkTaskFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoGUIQueryTask extends AbstractTask {
    @Tunable(context = "nogui", exampleStringValue = "gtp lrr* cell Q5S007 EBI-2624319", gravity = 0,
            description = "Space separated terms to search among interactors ids and names to build the network around them. <br>" +
                    "Example: \"gtp lrr* cell Q5S007 EBI-2624319\"",
            required = true)
    public String seedTerms;

    @Tunable(context = "nogui", exampleStringValue = "9606, 559292", gravity = 1,
            description = "Comma separated taxon ids of seed interactors around which the network will be built.<br>" +
                    "If not given, all species will be accepted<br>" +
                    "Example: \"9606, 559292\"")
    public String taxons;


    @Tunable(context = "nogui", exampleStringValue = "protein, peptide, small molecule, gene, dna, ds dna, ss dna, rna, complex", gravity = 2,
            description = "Comma separated allowed types of seeds interactor around which the network will be built.<br>" +
                    "If not given, all types will be allowed for seeds<br>" +
                    "Example: \"protein, peptide, small molecule, gene, dna, ds dna, ss dna, rna, complex\"")
    public String types;

    @Tunable(context = "nogui", gravity = 3,
            description = "If true, resulting network will be made of given seed terms interactors and all interacting partner found for them.<br>" +
                    "If false, resulting network will only be constituted of given terms interactors and interaction between them.<br>" +
                    "Default value : True")
    public Boolean includeSeedPartners = true;

    @Tunable(context = "nogui", gravity = 4,
            description = "If true, apply force directed layout algorithm after the extraction on the new network<br>" +
                    "If false, do not apply any layout algorithm: All elements of the extracted network will be stacked on top of each others visually.<br>" +
                    "Default value : True")
    public Boolean applyLayout = true;

    @Tunable(context = "nogui", gravity = 5,
            description = "If true, the query will be an exact query.<br>" +
                    "If false, the query will be a fuzzy search<br>" +
                    "Default value : False")
    public Boolean exactQuery = false;

    @Tunable(context = "nogui", gravity = 6,
            description = "If negative or 0, all matching interactors will be used as seeds for network building.<br>" +
                    "If positive, only the top n interactors that matched terms will be used as seeds for network building<br>" +
                    "Default value : 0")
    public Integer maxInteractorsPerTerm = 0;

    @Tunable(context = "nogui", gravity = 7,
            description = "Name of the network to build", longDescription = "Name of the network to build.<br> If not given, will be IntAct network")
    public String netName;

    @Tunable(context = "nogui", gravity = 8,
            description = "If false, the network is built when the query ends. If true, the task is performed in the background. Default: false.")
    public Boolean asynchronous = false;

    private final Manager manager;
    private Network network;

    public NoGUIQueryTask(Manager manager) {
        this.manager = manager;
    }

    private final List<String> interactorAcs = new ArrayList<>();

    @Override
    public void run(TaskMonitor taskMonitor) {
        network = new Network(manager);
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Collecting interactors");
        resolveTermsToAcs();
        taskMonitor.showMessage(TaskMonitor.Level.INFO, "Querying network from interactors");
        buildNetwork();
    }

    private void resolveTermsToAcs() {
        Set<Interactor> interactors = new HashSet<>();
        TermsResolvingTask task = new TermsResolvingTask(network, seedTerms, null, exactQuery);
        if (maxInteractorsPerTerm <= 0) {
            Map<String, List<Interactor>> interactorsToResolve = task.resolveTerms(seedTerms, 100);
            interactorsToResolve.values().forEach(interactors::addAll);
            task.completeAdditionalInteractors(new ArrayList<>(interactorsToResolve.keySet()), 100, null, null).values().forEach(interactors::addAll);
        } else task.resolveTerms(seedTerms, maxInteractorsPerTerm).values().forEach(interactors::addAll);

        Stream<Interactor> interactorStream = interactors.stream();

        if (types != null && !types.isBlank()) {
            Set<String> allowedTypes = Set.of(types.split("\\s*,\\s*"));
            interactorStream = interactorStream.filter(interactor -> allowedTypes.contains(interactor.typeName));
        }

        if (taxons != null && !taxons.isEmpty()) {
            Set<String> allowedTaxIds = Arrays.stream(taxons.split("\\s*,\\s*"))
                    .collect(Collectors.toSet());
            interactorStream = interactorStream.filter(interactor -> allowedTaxIds.contains(interactor.taxId));
        }

        interactorAcs.addAll(interactorStream.map(interactor -> interactor.ac).collect(Collectors.toList()));
    }

    private void buildNetwork() {
        ImportNetworkTaskFactory factory = new ImportNetworkTaskFactory(network, interactorAcs, includeSeedPartners, applyLayout, netName);
        TaskIterator taskIterator = factory.createTaskIterator();
        if (!asynchronous) {
            insertTasksAfterCurrentTask(taskIterator);
        } else {
            manager.utils.execute(taskIterator);
        }
    }
}
