package uk.ac.ebi.intact.intactApp.internal.model.managers;

import org.cytoscape.service.util.CyServiceRegistrar;
import uk.ac.ebi.intact.intactApp.internal.model.core.FeatureClassifier;

public class IntactManager  {
    private static final String INTACT_WEBSERVICES = "https://wwwdev.ebi.ac.uk/intact/ws/";

    public static final String INTACT_GRAPH_WS = INTACT_WEBSERVICES + "graph/";
//    public static final String INTACT_INTERACTOR_WS = "http://127.0.0.1:8081/intact/ws/interactor/";
    public static final String INTACT_INTERACTOR_WS = INTACT_WEBSERVICES + "interactor/";
    public static final String INTACT_INTERACTION_WS = "http://127.0.0.1:8082/intact/ws/interaction/";


    public final IntactDataManager data;
    public final IntactStyleManager style;
    public final CytoUtils utils;


    public IntactManager(CyServiceRegistrar registrar) {
        utils = new CytoUtils(registrar);
        data = new IntactDataManager(this);
        data.loadCurrentSession();
        style = new IntactStyleManager(this);
        style.setupStyles();
        FeatureClassifier.initMIIdSets();
    }
}
