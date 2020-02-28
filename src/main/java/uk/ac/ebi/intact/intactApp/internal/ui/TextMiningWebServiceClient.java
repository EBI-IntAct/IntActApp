package uk.ac.ebi.intact.intactApp.internal.ui;

import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
import org.cytoscape.io.webservice.SearchWebServiceClient;
import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
import org.cytoscape.work.TaskIterator;
import uk.ac.ebi.intact.intactApp.internal.model.IntactManager;

// TODO: [Optional] Improve non-gui mode
public class TextMiningWebServiceClient extends AbstractWebServiceGUIClient
        implements NetworkImportWebServiceClient, SearchWebServiceClient {
    IntactManager manager;

    public TextMiningWebServiceClient(IntactManager manager) {
        super(manager.getNetworkURL(), "STRING: PubMed query",
                "<html>Enter a Pubmed query and create a STRING network by finding all " +
                        "proteins mentioned in the resulting publications.<p>STRING is a database of " +
                        "known and predicted protein interactions.  The interactions include direct " +
                        "(physical) and indirect (functional) associations; they are derived from four " +
                        "sources: <ul><li>Genomic Context</li><li>High-throughput Experiments</li>" +
                        "<li>(Conserved) Coexpression</li><li>Previous Knowledge</li></ul>	 " +
                        "STRING quantitatively integrates interaction data from these sources " +
                        "for a large number of organisms, and transfers information between " +
                        "these organisms where applicable. The database currently covers 9,643,763 " +
                        "proteins from 2,031 organisms.</html>");
        this.manager = manager;
        super.gui = new PubMedQueryPanel(manager);
    }

    public TaskIterator createTaskIterator(Object query) {
        if (query == null)
            throw new NullPointerException("null query");
        return new TaskIterator();
    }

}
