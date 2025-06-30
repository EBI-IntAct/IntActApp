package uk.ac.ebi.intact.app.internal.model.managers;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.*;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;

import java.util.Properties;

public class Manager {
    private static final String INTACT_WEBSERVICES = "https://www.ebi.ac.uk/intact/ws/";
//    private static final String INTACT_WEBSERVICES = "https://wwwdev.ebi.ac.uk/intact/ws/";

    public static final String INTACT_GRAPH_WS = INTACT_WEBSERVICES + "graph/";
    public static final String INTACT_INTERACTOR_WS = INTACT_WEBSERVICES + "interactor/";
    public static final String INTACT_INTERACTION_WS = INTACT_WEBSERVICES + "interaction/";


    public final CytoUtils utils;
    public final DataManager data;
    public final StyleManager style;
    public final OptionManager option;


    public Manager(CyServiceRegistrar registrar) {
        utils = new CytoUtils(registrar);
        FeatureClassifier.initMIIdSets();
        data = new DataManager(this);
        data.loadCurrentSession();
        utils.registerService(new SessionLoader(this), SessionLoadedListener.class, new Properties());
        style = new StyleManager(this);
        style.setupStyles();
        option = new OptionManager(this);
    }
}
