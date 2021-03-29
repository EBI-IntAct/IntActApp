package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.core.network.Network;
import uk.ac.ebi.intact.app.internal.tasks.query.AugmentNetworkTask;
import uk.ac.ebi.intact.app.internal.tasks.query.CreateNetworkTask;

import java.util.List;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    private final Network network;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final boolean applyLayout;
    private final String netName;


    public ImportNetworkTaskFactory(final Network network, final List<String> intactAcs, boolean includeNeighbours, boolean applyLayout, String netName) {
        this.network = network;
        this.intactAcs = intactAcs;
        this.includeNeighbours = includeNeighbours;
        this.applyLayout = applyLayout;
        this.netName = netName;
    }

    public TaskIterator createTaskIterator() {
        if (network.getCyNetwork() == null) {
            return new TaskIterator(new CreateNetworkTask(network, intactAcs, includeNeighbours, applyLayout, netName));
        }
        return new TaskIterator(new AugmentNetworkTask(network, intactAcs));
    }

    public boolean isReady() {
        return true;
    }
}
