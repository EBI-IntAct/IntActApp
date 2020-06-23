package uk.ac.ebi.intact.app.internal.model.core.managers;

import org.cytoscape.service.util.CyServiceRegistrar;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;
import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.CytoUtils;
import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.DataManager;
import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.OptionManager;
import uk.ac.ebi.intact.app.internal.model.core.managers.sub.managers.StyleManager;

public class Manager {
    private static final String INTACT_WEBSERVICES = "https://wwwdev.ebi.ac.uk/intact/ws/";

    public static final String INTACT_GRAPH_WS = "https://www.ebi.ac.uk/intact/ws/graph/";
//    public static final String INTACT_GRAPH_WS = INTACT_WEBSERVICES + "graph/";
//    public static final String INTACT_INTERACTOR_WS = "http://127.0.0.1:8081/intact/ws/interactor/";
    public static final String INTACT_INTERACTOR_WS = INTACT_WEBSERVICES + "interactor/";
    public static final String INTACT_INTERACTION_WS = "http://127.0.0.1:8082/intact/ws/interaction/";


    public final CytoUtils utils;
    public final DataManager data;
    public final StyleManager style;
    public final OptionManager option;


    public Manager(CyServiceRegistrar registrar) {
        utils = new CytoUtils(registrar);
        FeatureClassifier.initMIIdSets();
        data = new DataManager(this);
        data.loadCurrentSession();
        style = new StyleManager(this);
        style.setupStyles();
        option = new OptionManager(this);
    }
}
