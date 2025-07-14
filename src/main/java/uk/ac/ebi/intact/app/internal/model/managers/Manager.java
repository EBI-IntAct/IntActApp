package uk.ac.ebi.intact.app.internal.model.managers;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedListener;
import uk.ac.ebi.intact.app.internal.model.managers.sub.managers.*;
import uk.ac.ebi.intact.app.internal.model.core.features.FeatureClassifier;

import java.util.Properties;

public class Manager {

    // Change this to false if you want to call IntAct test/dev APIs
    private static final boolean PROD_API = true;

    private static final String INTACT_WEBSERVICES = "https://www.ebi.ac.uk/intact/ws/";
    private static final String INTACT_TEST_WEBSERVICES = "https://wwwdev.ebi.ac.uk/intact/test/ws/";
    private static final String INTACT_DEV_WEBSERVICES = "https://wwwdev.ebi.ac.uk/intact/ws/";

    public static final String INTACT_GRAPH_WS = (PROD_API ? INTACT_WEBSERVICES : INTACT_DEV_WEBSERVICES) + "graph/";
    public static final String INTACT_INTERACTOR_WS = (PROD_API ? INTACT_WEBSERVICES : INTACT_TEST_WEBSERVICES) + "interactor/";
    public static final String INTACT_INTERACTION_WS = (PROD_API ? INTACT_WEBSERVICES : INTACT_TEST_WEBSERVICES) + "interaction/";


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
