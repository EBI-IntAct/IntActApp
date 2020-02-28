package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.Databases;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

// TODO: [Optional] Improve non-gui mode
public class StitchWebServiceClient extends AbstractWebServiceGUIClient
        implements NetworkImportWebServiceClient, SearchWebServiceClient {
    IntactManager manager;

    public StitchWebServiceClient(IntactManager manager) {
        super(manager.getNetworkURL(), "STITCH: protein/compound query", "<html>STITCH is a resource to explore known and predicted interactions of chemicals and proteins. Chemicals are linked to other chemicals and proteins by evidence derived from experiments, databases and the literature.  <p>STITCH contains interactions for between 300,000 small molecules and 2.6 million proteins from 1133 organisms.</p></html>");
        this.manager = manager;
        super.gui = new GetTermsPanel(manager, Databases.STITCH.getAPIName(), false);
    }

    public TaskIterator createTaskIterator(Object query) {
        if (query == null)
            throw new NullPointerException("null query");
        return new TaskIterator();
    }

}
