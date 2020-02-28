package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.Databases;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

// TODO: [Optional] Improve non-gui mode
public class IntactWebServiceClient extends AbstractWebServiceGUIClient
        implements NetworkImportWebServiceClient, SearchWebServiceClient {
    IntactManager manager;

    public IntactWebServiceClient(IntactManager manager) {
        super(manager.getNetworkURL(), "STRING: protein query", "<html>STRING is a database of known and predicted protein interactions.  The interactions include direct (physical) and indirect (functional) associations; they are derived from four sources: <ul><li>Genomic Context</li><li>High-throughput Experiments</li><li>(Conserved) Coexpression</li><li>Previous Knowledge</li></ul>	 STRING quantitatively integrates interaction data from these sources for a large number of organisms, and transfers information between these organisms where applicable. </html>");
        this.manager = manager;
        super.gui = new GetTermsPanel(manager, Databases.STRING.getAPIName(), false);
    }

    public TaskIterator createTaskIterator(Object query) {
        if (query == null)
            throw new NullPointerException("null query");
        return new TaskIterator();
    }

}
