package uk.ac.ebi.intact.intactApp.internal.tasks.query.factories;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.tasks.query.AddNewTermsTask;
import uk.ac.ebi.intact.intactApp.internal.tasks.query.LoadInteractions;

import java.util.List;

public class ImportNetworkTaskFactory extends AbstractTaskFactory {
    final IntactNetwork intactNetwork;
    final List<String> intactAcs;
    final List<Long> taxIds;
    final String netName;


    public ImportNetworkTaskFactory(final IntactNetwork intactNetwork, final List<String> intactAcs, List<Long> taxIds, String netName) {
        this.intactNetwork = intactNetwork;
        this.intactAcs = intactAcs;
        this.taxIds = taxIds;
        this.netName = netName;
    }

    public TaskIterator createTaskIterator() {
        if (intactNetwork.getNetwork() == null) {
            return new TaskIterator(new LoadInteractions(intactNetwork, intactAcs, taxIds, netName));
        }
        return new TaskIterator(new AddNewTermsTask(intactNetwork, intactAcs));
    }

    public boolean isReady() {
        return true;
    }
}
