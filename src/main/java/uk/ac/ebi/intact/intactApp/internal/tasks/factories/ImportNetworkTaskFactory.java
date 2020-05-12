package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.LoadInteractions;
import uk.ac.ebi.intact.intactApp.internal.tasks.LoadTermsTask;

import java.util.List;
import java.util.Map;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    final IntactNetwork intactNetwork;
    final String species;
    final List<String> stringIds;
    final Map<String, String> queryTermMap;
    int taxon;
    int confidence;
    int additionalNodes;
    String useDATABASE;

    public ImportNetworkTaskFactory(final IntactNetwork intactNetwork, final String species,
                                    int taxon, int confidence, int additional_nodes,
                                    final List<String> stringIds,
                                    final Map<String, String> queryTermMap,
                                    final String useDATABASE) {
        this.intactNetwork = intactNetwork;
        this.taxon = taxon;
        this.confidence = confidence;
        this.additionalNodes = additional_nodes;
        this.stringIds = stringIds;
        this.species = species;
        this.queryTermMap = queryTermMap;
        this.useDATABASE = useDATABASE;
    }

    public TaskIterator createTaskIterator() {
        if (intactNetwork.getNetwork() == null) {
            return new TaskIterator(new LoadInteractions(intactNetwork, species, taxon,
                    confidence, additionalNodes, stringIds,
                    queryTermMap, "", useDATABASE));
        }
        return new TaskIterator(new LoadTermsTask(intactNetwork, species, taxon, confidence,
                additionalNodes, stringIds, queryTermMap, useDATABASE));
    }

    public boolean isReady() {
        return intactNetwork.getManager().haveURIs();
    }
}
