//package uk.ac.ebi.intact.intactApp.internal.ui;
//
//import org.cytoscape.io.webservice.NetworkImportWebServiceClient;
//import org.cytoscape.io.webservice.SearchWebServiceClient;
//import org.cytoscape.io.webservice.swing.AbstractWebServiceGUIClient;
//import org.cytoscape.work.TaskIterator;
//import uk.ac.ebi.intact.intactApp.internal.model.managers.IntactManager;
//import uk.ac.ebi.intact.intactApp.internal.ui.panels.terms.ResolveTermsPanel;
//
//// TODO: [Optional] Improve non-gui mode
//public class IntactWebServiceClient extends AbstractWebServiceGUIClient
//        implements NetworkImportWebServiceClient, SearchWebServiceClient {
//    IntactManager manager;
//
//    public IntactWebServiceClient(IntactManager manager) {
//        super(IntactManager.INTACT_GRAPH_WS, "IntAct: protein query", "");
//        this.manager = manager;
//        super.gui = new ResolveTermsPanel(manager);
//    }
//
//    public TaskIterator createTaskIterator(Object query) {
//        if (query == null)
//            throw new NullPointerException("null query");
//        return new TaskIterator();
//    }
//
//}
