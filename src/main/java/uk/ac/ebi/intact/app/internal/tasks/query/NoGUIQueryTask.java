package uk.ac.ebi.intact.app.internal.tasks.query;

import org.cytoscape.work.AbstractTask;
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
            description = "Space separated terms to search among interactors ids and names to build the network around them.",
            required = true)
    public String seedTerms;

    @Tunable(context = "nogui", exampleStringValue = "9606, 559292", gravity = 1,
            description = "Comma separated taxon ids of seed interactors around which the network will be built.<br>" +
                    "If not given, all species will be accepted.")
    public String taxons;


    @Tunable(context = "nogui", exampleStringValue = "protein, peptide, small molecule, gene, dna, ds dna, ss dna, rna, complex", gravity = 2,
            description = "Comma separated allowed types of seeds interactor around which the network will be built.<br>" +
                    "If not given, all types will be allowed for seeds")
    public String types;

    @Tunable(context = "nogui", gravity = 3,
            description = "If true, resulting network will be made of given seed terms interactors and all interacting partner found for them.<br>" +
                    "If false, resulting network will only be constituted of given terms interactors and interaction between them.<br>" +
                    "Default value : True")
    public Boolean includeSeedPartners = true;

    @Tunable(context = "nogui", gravity = 4,
            description = "If true, the query will be an exact query.<br>" +
                    "If false, the query will be a fuzzy search<br>" +
                    "Default value : False")
    public Boolean exactQuery = false;

    @Tunable(context = "nogui", gravity = 4,
            description = "If negative or 0, all matching interactors will be used as seeds for network building.<br>" +
                    "If positive, only the top n interactors that matched terms will be used as seeds for network building<br>" +
                    "Default value : 0")
    public Integer maxInteractorsPerTerm = 0;

    @Tunable(context = "nogui", gravity = 5,
            description = "Name of the network to build", longDescription = "Name of the network to build.<br> If not given, will be IntAct network")
    public String netName;

    private final Manager manager;
    private Network network;

    public NoGUIQueryTask(Manager manager) {
        this.manager = manager;
    }

    private final List<String> interactorAcs = new ArrayList<>();

    @Override
    public void run(TaskMonitor taskMonitor) {
        network = new Network(manager);
        resolveTermsToAcs();
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
            Set<Long> allowedTaxIds = Arrays.stream(taxons.split("\\s*,\\s*"))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            interactorStream = interactorStream.filter(interactor -> allowedTaxIds.contains(interactor.taxId));
        }

        interactorAcs.addAll(interactorStream.map(interactor -> interactor.ac).collect(Collectors.toList()));
    }

    private void buildNetwork() {
        ImportNetworkTaskFactory factory = new ImportNetworkTaskFactory(network, interactorAcs, includeSeedPartners, netName);
        insertTasksAfterCurrentTask(factory.createTaskIterator());
    }
}
