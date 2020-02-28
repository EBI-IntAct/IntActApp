package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.Species;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.LoadInteractions;
import uk.ac.ebi.intact.intactApp.internal.tasks.LoadSpeciesInteractions;
import uk.ac.ebi.intact.intactApp.internal.tasks.LoadTermsTask;

import java.util.List;
import java.util.Map;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    final IntactNetwork stringNet;
    final String species;
    final List<String> stringIds;
    final Map<String, String> queryTermMap;
    int taxon;
    int confidence;
    int additionalNodes;
    String useDATABASE;

    public ImportNetworkTaskFactory(final IntactNetwork stringNet, final String species,
                                    int taxon, int confidence, int additional_nodes,
                                    final List<String> stringIds,
                                    final Map<String, String> queryTermMap,
                                    final String useDATABASE) {
        this.stringNet = stringNet;
        this.taxon = taxon;
        this.confidence = confidence;
        this.additionalNodes = additional_nodes;
        this.stringIds = stringIds;
        this.species = species;
        this.queryTermMap = queryTermMap;
        this.useDATABASE = useDATABASE;
    }

    public TaskIterator createTaskIterator() {
        if (stringIds == null) {
            return new TaskIterator(
                    new LoadSpeciesInteractions(stringNet, species, taxon, confidence,
                            Species.getSpeciesOfficialName(String.valueOf(taxon)),
                            useDATABASE));
        } else if (stringNet.getNetwork() == null) {
            return new TaskIterator(new LoadInteractions(stringNet, species, taxon,
                    confidence, additionalNodes, stringIds,
                    queryTermMap, "", useDATABASE));
        }
        return new TaskIterator(new LoadTermsTask(stringNet, species, taxon, confidence,
                additionalNodes, stringIds, queryTermMap, useDATABASE));
    }

    public boolean isReady() {
        return stringNet.getManager().haveURIs();
    }
}
