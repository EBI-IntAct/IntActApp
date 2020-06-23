package uk.ac.ebi.intact.app.internal.tasks.query.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.app.internal.model.IntactNetwork;
import uk.ac.ebi.intact.app.internal.tasks.query.AddNewTermsTask;
import uk.ac.ebi.intact.app.internal.tasks.query.LoadInteractions;

import java.util.List;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    private final IntactNetwork intactNetwork;
    private final List<String> intactAcs;
    private final boolean includeNeighbours;
    private final String netName;


    public ImportNetworkTaskFactory(final IntactNetwork intactNetwork, final List<String> intactAcs, boolean includeNeighbours, String netName) {
        this.intactNetwork = intactNetwork;
        this.intactAcs = intactAcs;
        this.includeNeighbours = includeNeighbours;
        this.netName = netName;
    }

    public TaskIterator createTaskIterator() {
        if (intactNetwork.getNetwork() == null) {
            return new TaskIterator(new LoadInteractions(intactNetwork, intactAcs, includeNeighbours, netName));
        }
        return new TaskIterator(new AddNewTermsTask(intactNetwork, intactAcs));
    }

    public boolean isReady() {
        return true;
    }
}
