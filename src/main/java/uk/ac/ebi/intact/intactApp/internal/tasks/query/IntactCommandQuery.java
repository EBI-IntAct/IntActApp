package uk.ac.ebi.intact.intactApp.internal.tasks.query;

import com.fasterxml.jackson.databind.JsonNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import uk.ac.ebi.intact.intactApp.internal.io.HttpUtils;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.core.Interactor;
import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.query.factories.ImportNetworkTaskFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IntactCommandQuery extends AbstractTask {
    @Tunable(required = true, context = "nogui", exampleStringValue = "gtp lrr* cell Q5S007 EBI-2624319", gravity = 0,
            description = "Space separated terms to search among interactors ids and names to build the network around them.")
    public String terms;

    @Tunable(context = "nogui", exampleStringValue = "9606, 559292", gravity = 1,
            description = "Comma separated taxon ids of given terms. These terms will be converted to seed interactors on which partners will be attached to build the network.")
    public String seedTaxons;
    private final Set<Long> seedTaxIds = new HashSet<>();

    @Tunable(context = "nogui", exampleStringValue = "9606, 559292", gravity = 2,
            description = "Comma separated taxon ids of the desired network interactors species, both for the seeds and their partner.<br>" +
                    "If not given, all species will be accepted. If seedTaxons is given, this argument won't be used for seeds, only for their partner.<br>" +
                    "seedTaxons will automatically be added to global taxons")
    public String taxons;
    private final Set<Long> taxIds = new HashSet<>();


    @Tunable(context = "nogui", exampleStringValue = "protein, peptide, small molecule, gene, dna, ds dna, ss dna, rna, complex", gravity = 3,
            description = "Comma separated allowed types of seeds interactor around which the network will be built.<br>" +
                    "If not given, all types will be allowed for seeds")
    public String types;

    @Tunable(context = "nogui", gravity = 4,
            description = "Name of the network to build", longDescription = "Name of the network to build.<br> If not given, will be IntAct network")
    public String netName;

    private final IntactManager manager;

    public IntactCommandQuery(IntactManager manager) {
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
        postData.put("query", terms);
        JsonNode resolutionResponse = HttpUtils.postJSON(IntactManager.INTACT_INTERACTOR_WS + "list/resolve", postData, manager);
        Map<String, List<Interactor>> interactorsToResolve = Interactor.getInteractorsToResolve(resolutionResponse);
        Stream<Interactor> interactors = interactorsToResolve.values().stream().flatMap(List::stream);

        if (types != null && !types.isBlank()) {
            Set<String> availableTypes = Set.of(types.split("\\s*,\\s*"));
            interactors = interactors.filter(interactor -> availableTypes.contains(interactor.type));
        }

        if (taxons != null && !taxons.isEmpty()) {
            taxIds.addAll(Arrays.stream(taxons.split("\\s*,\\s*"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
        }

        if (seedTaxons != null && !seedTaxons.isEmpty()) {
            seedTaxIds.addAll(Arrays.stream(seedTaxons.split("\\s*,\\s*"))
                    .map(Long::parseLong)
                    .collect(Collectors.toList()));
        } else {
            seedTaxIds.addAll(taxIds);
        }

        if (!taxIds.isEmpty()) {
            taxIds.addAll(seedTaxIds);
        }

        if (!seedTaxIds.isEmpty()) {
            interactors = interactors.filter(interactor -> seedTaxIds.contains(interactor.taxId));
        }

        interactorAcs.addAll(interactors.map(interactor -> interactor.ac).collect(Collectors.toList()));
    }

    private void buildNetwork() {
        IntactNetwork network = new IntactNetwork(manager);
        ImportNetworkTaskFactory factory = new ImportNetworkTaskFactory(network, interactorAcs, new ArrayList<>(taxIds), netName);
        insertTasksAfterCurrentTask(factory.createTaskIterator());
    }
}
