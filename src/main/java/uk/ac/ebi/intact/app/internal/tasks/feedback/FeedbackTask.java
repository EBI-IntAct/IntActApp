package uk.ac.ebi.intact.app.internal.tasks.feedback;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import uk.ac.ebi.intact.app.internal.model.managers.Manager;

public class FeedbackTask extends AbstractTask {
    private Manager manager;

    public FeedbackTask(Manager manager) {
        this.manager = manager;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        manager.utils.getService(OpenBrowser.class).openURL("https://www.ebi.ac.uk/support/intact");
//        manager.utils.getService(OpenBrowser.class).openURL("https://github.com/EBI-IntAct/IntactApp/issues/new");
        // https://intact.atlassian.net/secure/CreateIssue.jspa?issuetype=10203&pid=10400 // Need guest acceptance
    }
}
