package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.tasks.query.AddNewTermsTask;
import uk.ac.ebi.intact.app.internal.tasks.query.LoadInteractions;

import java.util.List;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    private final Network network;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final String netName;


    public ImportNetworkTaskFactory(final Network network, final List<String> intactAcs, boolean includeNeighbours, String netName) {
        this.network = network;
        this.intactAcs = intactAcs;
        this.includeNeighbours = includeNeighbours;
        this.netName = netName;
    }

    public TaskIterator createTaskIterator() {
        if (network.getCyNetwork() == null) {
            return new TaskIterator(new LoadInteractions(network, intactAcs, includeNeighbours, netName));
        }
        return new TaskIterator(new AddNewTermsTask(network, intactAcs));
    }

    public boolean isReady() {
        return true;
    }
}
