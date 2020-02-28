package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.*;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import uk.ac.ebi.intact.intactApp.internal.model.EntityIdentifier;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.utils.StringResults;

import java.util.List;

public class DiseaseQueryTask extends AbstractTask implements ObservableTask {
    final IntactManager manager;

    @Tunable(description = "Disease query", required = true,
            longDescription = "Enter the name (or partial name) of a disease",
            exampleStringValue = "alzheimers")
    public String disease = null;

    @Tunable(description = "Species",
            longDescription = "Species name.  This should be the actual " +
                    "taxonomic name (e.g. homo sapiens, not human)",
            exampleStringValue = "homo sapiens")
    public String species = null;

    @Tunable(description = "Taxon ID",
            longDescription = "The species taxonomy ID.  See the NCBI taxonomy home page for IDs",
            exampleStringValue = "9606")
    public int taxonID = -1;

    @Tunable(description = "Maximum additional interactors",
            longDescription = "The maximum number of proteins to return in addition to the query set",
            exampleStringValue = "100")
    public BoundedInteger limit = new BoundedInteger(1, 100, 10000, false, false);

    @Tunable(description = "Confidence cutoff",
            longDescription = "The confidence score reflects the cumulated evidence that this " +
                    "interaction exists.  Only interactions with scores greater than " +
                    "this cutoff will be returned",
            exampleStringValue = "0.4")
    public BoundedDouble cutoff = new BoundedDouble(0.0, 0.4, 1.0, false, false);

    private List<Species> speciesList;

    private CyNetwork loadedNetwork;

    public DiseaseQueryTask(final IntactManager manager) {
        this.manager = manager;
        speciesList = Species.getSpecies();
        // Set Human as the default
        for (Species s : speciesList) {
            if (s.toString().equals("Homo sapiens")) {
                species = s.toString();
                break;
            }
        }
    }

    public void run(TaskMonitor monitor) {
        monitor.setTitle("STRING Disease Query");
        // Sanity check the inputs
        boolean found;
        Species sp = null;
        for (Species s : speciesList) {
            if (s.toString().equals(species) || s.getTaxId() == taxonID) {
                found = true;
                sp = s;
                break;
            }
        }
        if (sp == null) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "Unknown or missing species");
            throw new RuntimeException("Unknown or missing species");
        }

        IntactNetwork intactNetwork = new IntactNetwork(manager);
        int confidence = (int) (cutoff.getValue() * 100);

        // Create the network from a disease query
        GetDiseaseTermsTask dTask = new GetDiseaseTermsTask(manager, sp.getTaxId(), disease);
        manager.execute(new TaskIterator(dTask), true);
        List<EntityIdentifier> matches = dTask.getMatchedTerms();
        if (matches == null || matches.size() == 0) {
            monitor.showMessage(TaskMonitor.Level.ERROR, "Query '" + disease + "' returned no results");
            throw new RuntimeException("Query '" + disease + "' returned no results");
        }
        EntityIdentifier entity = matches.get(0);
        monitor.showMessage(TaskMonitor.Level.INFO, "Loading proteins for " + entity.getPrimaryName());
        AbstractTask getIds =
                new GetStringIDsFromDiseasesTask(intactNetwork, sp, limit.getValue(),
                        confidence, entity.getIdentifier(),
                        entity.getPrimaryName());
        manager.execute(new TaskIterator(getIds), true);
        loadedNetwork = intactNetwork.getNetwork();
        if (loadedNetwork == null)
            throw new RuntimeException("Query '" + disease + "' returned no results");
    }

    @Override
    public <R> R getResults(Class<? extends R> clzz) {
        return StringResults.getResults(clzz, loadedNetwork);
    }

    @Override
    public List<Class<?>> getResultClasses() {
        return StringResults.getResultClasses();
    }

}
