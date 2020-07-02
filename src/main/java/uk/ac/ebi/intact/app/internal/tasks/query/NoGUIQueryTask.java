package uk.ac.ebi.intact.app.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.app.internal.io.HttpUtils;
import uk.ac.ebi.intact.app.internal.tasks.query.factories.ImportNetworkTaskFactory;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.model.core.elements.nodes.Interactor;
import uk.ac.ebi.intact.app.internal.managers.Manager;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NoGUIQueryTask extends AbstractTask {
    @Tunable(context = "nogui", exampleStringValue = "gtp lrr* cell Q5S007 EBI-2624319", gravity = 0,
            description = "Space separated terms to search among interactors ids and names to build the network around them.")
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
            description = "Name of the network to build", longDescription = "Name of the network to build.<br> If not given, will be IntAct network")
    public String netName;

    private final Manager manager;

    public NoGUIQueryTask(Manager manager) {
        this.manager = manager;
    }

    private final List<String> interactorAcs = new ArrayList<>();

    @Override
    public void run(TaskMonitor taskMonitor) {
        resolveTermsToAcs();
        buildNetwork();
    }

    private void resolveTermsToAcs() {
        Map<Object, Object> postData = new HashMap<>();
        postData.put("query", String.join("\n",seedTerms.split("\\s+")));
        JsonNode resolutionResponse = HttpUtils.postJSON(Manager.INTACT_INTERACTOR_WS + "list/resolve", postData, manager);
        Map<String, List<Interactor>> interactorsToResolve = Interactor.getInteractorsToResolve(resolutionResponse, new HashMap<>());
        Stream<Interactor> interactors = interactorsToResolve.values().stream().flatMap(List::stream);

        if (types != null && !types.isBlank()) {
            Set<String> allowedTypes = Set.of(types.split("\\s*,\\s*"));
            interactors = interactors.filter(interactor -> allowedTypes.contains(interactor.type));
        }

        if (taxons != null && !taxons.isEmpty()) {
            Set<Long> allowedTaxIds = Arrays.stream(taxons.split("\\s*,\\s*"))
                    .map(Long::parseLong)
                    .collect(Collectors.toSet());
            interactors = interactors.filter(interactor -> allowedTaxIds.contains(interactor.taxId));
        }

        interactorAcs.addAll(interactors.map(interactor -> interactor.ac).collect(Collectors.toList()));
    }

    private void buildNetwork() {
        Network network = new Network(manager);
        ImportNetworkTaskFactory factory = new ImportNetworkTaskFactory(network, interactorAcs, includeSeedPartners, netName);
        insertTasksAfterCurrentTask(factory.createTaskIterator());
    }
}
