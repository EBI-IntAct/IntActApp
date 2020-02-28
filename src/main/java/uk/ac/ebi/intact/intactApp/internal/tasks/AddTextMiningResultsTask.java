package uk.ac.ebi.intact.intactApp.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;
import uk.ac.ebi.intact.intactApp.internal.model.IntactNetwork;
import uk.ac.ebi.intact.intactApp.internal.model.TextMiningResult;
import uk.ac.ebi.intact.intactApp.internal.utils.ModelUtils;

import java.util.List;

public class AddTextMiningResultsTask extends AbstractTask {
    final IntactNetwork stringNet;
    final List<TextMiningResult> tmResults;

    public AddTextMiningResultsTask(final IntactNetwork stringNet, final List<TextMiningResult> tmResults) {
        this.stringNet = stringNet;
        this.tmResults = tmResults;
    }

    public void run(TaskMonitor monitor) {
        IntactManager manager = stringNet.getManager();
        ModelUtils.addTextMiningResults(manager, tmResults, stringNet.getNetwork());
    }

    @ProvidesTitle
    public String getTitle() {
        return "Adding text mining columns";
    }
}
