package uk.ac.ebi.intact.intactApp.internal.tasks.factories;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.tasks.SetLabelAttributeTask;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

public class SetLabelAttributeTaskFactory extends AbstractNetworkTaskFactory {

    final IntactManager manager;

    public SetLabelAttributeTaskFactory(IntactManager manager) {
        this.manager = manager;
    }

    public boolean isReady(CyNetwork network) {
        return (ModelUtils.isStringNetwork(network) && manager.showEnhancedLabels());
    }

    public TaskIterator createTaskIterator(CyNetwork network) {
        return new TaskIterator(new SetLabelAttributeTask(manager, network));
    }

}
